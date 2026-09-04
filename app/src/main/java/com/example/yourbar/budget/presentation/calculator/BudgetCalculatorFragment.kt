package com.example.yourbar.budget.presentation.calculator

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.yourbar.R
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.models.SteelType
import com.example.yourbar.budget.domain.calculator.usecase.CalculateParamsUseCase
import com.example.yourbar.budget.presentation.viewmodel.BudgetCalculatorViewModel
import com.example.yourbar.budget.presentation.viewmodel.BudgetUiState
import com.example.yourbar.cart.domain.usecase.AddToCartUseCase
import com.example.yourbar.databinding.FragmentBudgetCalculatorBinding
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class BudgetCalculatorFragment : Fragment() {

    private var _binding: FragmentBudgetCalculatorBinding? = null
    private val binding get() = _binding!!

    private var lastResult: CalculationResult? = null
    private var lastPipeMeters: Double = 0.0

    private val calculateUseCase: CalculateParamsUseCase by inject()
    private val addToCartUseCase: AddToCartUseCase by inject()
    private val viewModel: BudgetCalculatorViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FRAGMENT", "onViewCreated, btnAddToCart visibility=${binding.btnAddToCart.visibility}")

        setupToggleButton()
        setupButtons()
        observeViewModel()
    }

    // ── UI setup ──────────────────────────────────────────

    private fun setupToggleButton() {
        updateToggleButtonColor()
        binding.tbRemoveAdditionalPocket.setOnCheckedChangeListener { _, _ ->
            updateToggleButtonColor()
            if (areFieldsFilled()) {
                hideKeyboard()
                calculate()
            }
        }
    }

    private fun setupButtons() {
        binding.btnCalculate.setOnClickListener {
            hideKeyboard()
            calculate()
        }
        binding.btnAddToCart.setOnClickListener { addToCart() }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                BudgetUiState.Idle -> binding.tvPipeResult.visibility = View.GONE
                is BudgetUiState.Result -> {
                    binding.tvPipeResult.text = "Труба 25×25: ${DecimalFormat("0.##").format(state.pipeMeters)} мп"
                    lastPipeMeters = state.pipeMeters
                    binding.tvPipeResult.visibility = View.VISIBLE
                }
                is BudgetUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    binding.tvPipeResult.visibility = View.GONE
                }
            }
        }
    }

    // ── Расчёт ────────────────────────────────────────────

    private fun calculate() {
        val width = binding.etWidth.text.toString().trim().toIntOrNull()
        val depth = binding.etDepth.text.toString().trim().toIntOrNull()
        val height = binding.etHeight.text.toString().trim().toIntOrNull()

        hideAllResults()

        // Валидация — единственное, что остаётся в фрагменте (это UI-логика)
        if (width == null || width <= 0) return showError("Введите корректную ширину")
        if (depth == null || depth <= 0) return showError("Введите корректную глубину")
        if (height == null || height <= 0) return showError("Введите корректную высоту")
        if (width <= 60 || depth <= 60 || height <= 60)
            return showError("Размеры должны быть больше 60 мм для расчёта трубы")

        val steelType = getSelectedSteelType() ?: return
        val thickness = getSelectedThickness() ?: return

        val additionalPocketsCount = if (binding.tbRemoveAdditionalPocket.isChecked) 0 else 1
        val totalPocketsCount = 1 + additionalPocketsCount

        try {
            val result = calculateUseCase.execute(
                widthMm = width,
                depthMm = depth,
                steelType = steelType,
                thicknessMm = thickness,
                additionalPocketsCount = additionalPocketsCount
            )
            lastResult = result

            // Вся работа со строками — в форматтере
            val formatted = CalculationFormatter.format(result, totalPocketsCount)

            binding.tvResult.text = formatted.totalWeight
            binding.tvWeightAisi304.text = formatted.aisi304
            binding.tvWeightAisi430.text = formatted.aisi430
            binding.tvCountertopWeight.text = formatted.countertop
            binding.tvPocketWeightAddition.text = formatted.pocket
            binding.tvSinkWeight.text = formatted.sink
            binding.tvInsertWeight.text = formatted.insert
            binding.tvPartitionsWeight.text = formatted.partitions

            showAllResults()
            viewModel.calculate(widthMm = width, depthMm = depth, heightMm = height)

        } catch (e: IllegalArgumentException) {
            showError(e.message ?: "Ошибка расчёта")
        } catch (e: Exception) {
            showError("Произошла непредвиденная ошибка")
        }
    }

    // ── Корзина ───────────────────────────────────────────

    private fun addToCart() {
       Log.d("ADD_TO_CART", "Кнопка нажата! lastResult=${lastResult != null}")
        val width = binding.etWidth.text.toString().trim().toIntOrNull()
        val depth = binding.etDepth.text.toString().trim().toIntOrNull()
        val height = binding.etHeight.text.toString().trim().toIntOrNull()
        if (width == null || depth == null || height == null) return

        val steelType = getSelectedSteelType() ?: return
        val thickness = getSelectedThickness() ?: return
        val pocketsCount = if (binding.tbRemoveAdditionalPocket.isChecked) 1 else 2

        val result = lastResult ?: run {
            showError("Сначала выполните расчёт")
            return
        }

        showNameDialog { enteredName ->
            Log.d("ADD_TO_CART", "Передаём имя: '$enteredName'")
            addToCartUseCase.execute(
                name = enteredName,                 // <-- сюда приходит название
                widthMm = width,
                depthMm = depth,
                heightMm = height,
                steelType = steelType.name,
                thicknessMm = thickness,
                pocketsCount = pocketsCount,
                calculationResult = result,
                pipeMeters = lastPipeMeters
            )
            Toast.makeText(requireContext(), "«$enteredName» добавлено в корзину", Toast.LENGTH_SHORT).show()
            // Сразу переходим в корзину

            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
            bottomNav.selectedItemId = R.id.dest_cart
        }
    }

// ── Сохранение и название  ───────────────────────────────────

    private fun showNameDialog(onConfirm: (String) -> Unit) {
        val width = binding.etWidth.text.toString().trim().toIntOrNull() ?: 0
        val depth = binding.etDepth.text.toString().trim().toIntOrNull() ?: 0
        val height = binding.etHeight.text.toString().trim().toIntOrNull() ?: 0

        val editText = android.widget.EditText(requireContext()).apply {
            hint = "Например: Барная станция №1"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine(true)
            setPadding(48, 32, 48, 16)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Название станции")
            .setMessage("Введите название для сохранения в корзину")
            .setView(editText)
            .setPositiveButton("Добавить") { dialog, _ ->
                var name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    name = name.replaceFirstChar { it.uppercase() }
                }

                Log.d("DIALOG", "Введённое имя: '$name'")

                if (name.isEmpty()) {
                    onConfirm("Станция ${width}×${depth}×${height} мм")
                } else {
                    onConfirm(name)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }





    // ── Вспомогательные ───────────────────────────────────

    private fun getSelectedSteelType(): SteelType? {
        return when (binding.rgSteelType.checkedRadioButtonId) {
            R.id.rbAisi304 -> SteelType.AISI_304
            R.id.rbAisi430 -> SteelType.AISI_430
            else -> { showError("Выберите марку стали"); null }
        }
    }

    private fun getSelectedThickness(): Double? {
        return when (binding.rgThickness.checkedRadioButtonId) {
            R.id.rb1mm -> 1.0
            R.id.rb1_5mm -> 1.5
            R.id.rb2mm -> 2.0
            else -> { showError("Выберите толщину"); null }
        }
    }

    private fun areFieldsFilled() = binding.etWidth.text?.isNotEmpty() == true &&
            binding.etDepth.text?.isNotEmpty() == true &&
            binding.etHeight.text?.isNotEmpty() == true

    private fun updateToggleButtonColor() {
        val button = binding.tbRemoveAdditionalPocket
        if (button.isChecked) {
            button.setBackgroundResource(R.drawable.toggle_rounded_red_bg)
            button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        } else {
            button.setBackgroundResource(R.drawable.toggle_rounded_bg)
            button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun hideAllResults() {
        listOf(
            binding.tvResult, binding.tvWeightAisi304, binding.tvWeightAisi430,
            binding.tvCountertopWeight, binding.tvPocketWeightAddition, binding.tvSinkWeight,
            binding.tvInsertWeight, binding.tvPartitionsWeight, binding.tvPipeResult,
            binding.btnAddToCart
        ).forEach { it.visibility = View.GONE }
    }

    private fun showAllResults() {
        listOf(
            binding.tvResult, binding.tvWeightAisi304, binding.tvWeightAisi430,
            binding.tvCountertopWeight, binding.tvPocketWeightAddition, binding.tvSinkWeight,
            binding.tvInsertWeight, binding.tvPartitionsWeight, binding.btnAddToCart
        ).forEach { it.visibility = View.VISIBLE }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
