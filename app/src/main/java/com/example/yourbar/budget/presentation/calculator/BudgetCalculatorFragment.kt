package com.example.yourbar.budget.presentation.calculator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.yourbar.R
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.models.SolidSinkType
import com.example.yourbar.budget.domain.calculator.models.SteelType
import com.example.yourbar.budget.domain.calculator.usecase.CalculateParamsUseCase
import com.example.yourbar.budget.presentation.calculator.dialog.BlenderShelfBottomSheet
import com.example.yourbar.budget.presentation.calculator.dialog.PocketBottomSheet
import com.example.yourbar.budget.presentation.calculator.dialog.SinkBottomSheet
import com.example.yourbar.budget.presentation.calculator.helper.AddToCartHelper
import com.example.yourbar.budget.presentation.calculator.model.StationConfig
import com.example.yourbar.budget.presentation.calculator.util.ButtonStateHelper
import com.example.yourbar.budget.presentation.calculator.util.SpinnerSetup
import com.example.yourbar.budget.presentation.viewmodel.BudgetCalculatorViewModel
import com.example.yourbar.budget.presentation.viewmodel.BudgetUiState
import com.example.yourbar.databinding.FragmentBudgetCalculatorBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class BudgetCalculatorFragment : Fragment() {

    private var _binding: FragmentBudgetCalculatorBinding? = null
    private val binding get() = _binding!!

    private var lastResult: CalculationResult? = null
    private var lastPipeMeters: Double = 0.0
    private val config = StationConfig()

    private lateinit var sharedPreferences: android.content.SharedPreferences

    private val calculateUseCase: CalculateParamsUseCase by inject()
    private val addToCartHelper: AddToCartHelper by inject()
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

        sharedPreferences =
            requireContext().getSharedPreferences("prices_settings", Context.MODE_PRIVATE)

        setupSpinners()
        setupButtons()
        observeViewModel()
        updateButtonStates()
    }

    // ── Спиннеры ──────────────────────────────────────────

    private fun setupSpinners() {
        SpinnerSetup.setup(
            binding.spFaucetHoles,
            listOf("0 шт", "1 шт", "2 шт", "3 шт", "4 шт", "5 шт"),
            requireContext()
        ) { if (areFieldsFilled()) calculate() }

        SpinnerSetup.setup(
            binding.spBackBoard,
            listOf("0 шт", "1 шт", "2 шт", "3 шт"),
            requireContext()
        ) { if (areFieldsFilled()) calculate() }

        SpinnerSetup.setup(
            binding.spAdjustableLeg,
            listOf("0 шт", "2 шт", "4 шт", "6 шт", "8 шт"),
            requireContext()
        ) { if (areFieldsFilled()) calculate() }
    }

    // ── Кнопки ────────────────────────────────────────────

    private fun setupButtons() {
        binding.btnCalculate.setOnClickListener {
            hideKeyboard()
            calculate()
        }
        binding.btnAddToCart.setOnClickListener { addToCart() }

        binding.btnAddAdditionalPocket.setOnClickListener {
            PocketBottomSheet(
                currentCount = config.pocketCount,
                currentHeightChoice = config.pocketHeightChoice
            ) { count, heightChoice ->
                config.pocketCount = count
                config.pocketHeightChoice = heightChoice
                updateButtonStates()
                if (areFieldsFilled()) {
                    hideKeyboard()
                    calculate()
                }
            }.show(requireContext())
        }

        binding.btnAddSolidSink.setOnClickListener {
            val widthMm = binding.etWidth.text.toString().trim().toIntOrNull() ?: 0
            val depthMm = binding.etDepth.text.toString().trim().toIntOrNull() ?: 0
            SinkBottomSheet(
                currentType = config.solidSinkType,
                stationWidthMm = widthMm,
                stationDepthMm = depthMm
            ) { type ->
                config.solidSinkType = type
                updateButtonStates()
                if (areFieldsFilled()) {
                    hideKeyboard()
                    calculate()
                }
            }.show(requireContext())
        }

        binding.btnAddShelfForBlender.setOnClickListener {
            BlenderShelfBottomSheet(
                currentWidthMm = config.blenderShelfWidthMm
            ) { widthMm ->
                config.blenderShelfWidthMm = widthMm
                updateButtonStates()
                if (areFieldsFilled()) {
                    hideKeyboard()
                    calculate()
                }
            }.show(requireContext())
        }
    }

    // ── Состояния кнопок ──────────────────────────────────

    private fun updateButtonStates() {
        updatePocketButton()
        updateSinkButton()
        updateShelfButton()
    }

    private fun updatePocketButton() {
        val btn = binding.btnAddAdditionalPocket
        if (config.pocketCount == 0) {
            ButtonStateHelper.setDefault(btn, "Добавить навесной карман", requireContext())
        } else {
            val stationWidth = binding.etWidth.text.toString().trim().toIntOrNull() ?: 0
            val pocketWidthMm = if (config.blenderShelfWidthMm > 0) {
                maxOf(stationWidth - config.blenderShelfWidthMm, 0)
            } else {
                stationWidth
            }
            ButtonStateHelper.setAdded(
                btn,
                "Карман: ${config.pocketCount} шт, ${pocketWidthMm} мм",
                requireContext()
            )
        }
    }

    private fun updateSinkButton() {
        val btn = binding.btnAddSolidSink
        if (config.solidSinkType == SolidSinkType.NONE) {
            ButtonStateHelper.setDefault(btn, "Добавить цельнотянутую мойку", requireContext())
        } else {
            ButtonStateHelper.setAdded(
                btn,
                "Мойка: ${config.solidSinkType.label} мм",
                requireContext()
            )
        }
    }

    private fun updateShelfButton() {
        val btn = binding.btnAddShelfForBlender
        if (config.blenderShelfWidthMm == 0) {
            ButtonStateHelper.setDefault(btn, "Добавить полку для блендера", requireContext())
        } else {
            ButtonStateHelper.setAdded(
                btn,
                "Полка для блендера: ${config.blenderShelfWidthMm} мм",
                requireContext()
            )
        }
    }

    // ── ViewModel ────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                BudgetUiState.Idle -> binding.tvPipeResult.visibility = View.GONE
                is BudgetUiState.Result -> {
                    binding.tvPipeResult.text =
                        "Труба 25×25: ${DecimalFormat("0.##").format(state.pipeMeters)} мп"
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

    // ── Расчёт ───────────────────────────────────────────

    private fun getFaucetHoleCount(): Int = binding.spFaucetHoles.selectedItemPosition
    private fun getBackBoardCount(): Int = binding.spBackBoard.selectedItemPosition
    private fun getAdjustableLegCount(): Int =
        binding.spAdjustableLeg.selectedItemPosition * 2

    private fun calculate() {
        val width = binding.etWidth.text.toString().trim().toIntOrNull()
        val depth = binding.etDepth.text.toString().trim().toIntOrNull()
        val height = binding.etHeight.text.toString().trim().toIntOrNull()

        hideAllResults()

        if (width == null || width <= 0) return showError("Введите корректную ширину")
        if (depth == null || depth <= 0) return showError("Введите корректную глубину")
        if (height == null || height <= 0) return showError("Введите корректную высоту")
        if (width <= 60 || depth <= 60 || height <= 60)
            return showError("Размеры должны быть больше 60 мм для расчёта трубы")

        val steelType = getSelectedSteelType() ?: return
        val thickness = getSelectedThickness() ?: return

        try {
            val result = calculateUseCase.execute(
                widthMm = width,
                depthMm = depth,
                steelType = steelType,
                thicknessMm = thickness,
                pocketCount = config.pocketCount,
                pocketHeightMm = config.pocketHeightMm,
                isShelfAdded = config.isShelfAdded,
                blenderShelfWidthMm = config.blenderShelfWidthMm,
                faucetHoleCount = getFaucetHoleCount(),
                backBoardCount = getBackBoardCount(),
                adjustableLegCount = getAdjustableLegCount(),
                solidSinkType = config.solidSinkType
            )
            lastResult = result
            renderResults(result, width, depth)
            viewModel.calculate(widthMm = width, depthMm = depth, heightMm = height)
        } catch (e: IllegalArgumentException) {
            showError(e.message ?: "Ошибка расчёта")
        } catch (_: Exception) {
            showError("Произошла непредвиденная ошибка")
        }
    }

    private fun renderResults(result: CalculationResult, width: Int, depth: Int) {
        val formatted = CalculationFormatter.format(result, config.pocketCount)
        val faucetHoleCount = getFaucetHoleCount()
        val backBoardCount = getBackBoardCount()
        val adjustableLegCount = getAdjustableLegCount()

        binding.tvResult.text = formatted.totalWeight
        binding.tvWeightAisi304.text = formatted.aisi304
        binding.tvWeightAisi430.text = formatted.aisi430

        val plywoodAreaSqM = (width * depth) / 1_000_000.0
        binding.tvCountertopWeight.text =
            "${formatted.countertop}\nФанера: ${"%.2f".format(plywoodAreaSqM)} м²"

        binding.tvSinkWeight.text = formatted.sink
        binding.tvInsertWeight.text = formatted.insert
        binding.tvPartitionsWeight.text = formatted.partitions

        binding.tvPocketWeightAddition.visibility =
            if (config.pocketCount > 0) View.VISIBLE else View.GONE
        if (config.pocketCount > 0) {
            binding.tvPocketWeightAddition.text = formatted.pocket
        }

        binding.tvSolidSinkResult.visibility =
            if (config.solidSinkType != SolidSinkType.NONE) View.VISIBLE else View.GONE
        if (config.solidSinkType != SolidSinkType.NONE) {
            binding.tvSolidSinkResult.text =
                "Цельнотянутая мойка: ${config.solidSinkType.label} мм — 1 шт"
        }

        binding.tvFaucetHolesResult.visibility =
            if (faucetHoleCount > 0) View.VISIBLE else View.GONE
        if (faucetHoleCount > 0) {
            binding.tvFaucetHolesResult.text = formatted.faucetHoles
        }

        binding.tvBackBoardResult.visibility =
            if (backBoardCount > 0) View.VISIBLE else View.GONE
        if (backBoardCount > 0) {
            binding.tvBackBoardResult.text = "Задний борт: $backBoardCount шт"
        }

        binding.tvAdjustableLegResult.visibility =
            if (adjustableLegCount > 0) View.VISIBLE else View.GONE
        if (adjustableLegCount > 0) {
            binding.tvAdjustableLegResult.text = "Регулируемая опора: $adjustableLegCount шт"
        }

        binding.tvBlenderShelfWeight.visibility =
            if (config.blenderShelfWidthMm > 0) View.VISIBLE else View.GONE
        if (config.blenderShelfWidthMm > 0) {
            binding.tvBlenderShelfWeight.text =
                "Полка для блендера:Ширина ${config.blenderShelfWidthMm} мм- " +
                        "${DecimalFormat("0.##").format(result.blenderShelfWeightKg)} кг"
        }

        showAllResults()
    }

    // ── Корзина ──────────────────────────────────────────

    private fun addToCart() {
        val width = binding.etWidth.text.toString().trim().toIntOrNull()
        val depth = binding.etDepth.text.toString().trim().toIntOrNull()
        val height = binding.etHeight.text.toString().trim().toIntOrNull()
        if (width == null || depth == null || height == null) return

        val steelType = getSelectedSteelType() ?: return
        val thickness = getSelectedThickness() ?: return
        val result = lastResult ?: run {
            showError("Сначала выполните расчёт")
            return
        }

        showNameDialog { enteredName ->
            viewLifecycleOwner.lifecycleScope.launch {
                addToCartHelper.execute(
                    name = enteredName,
                    widthMm = width,
                    depthMm = depth,
                    heightMm = height,
                    steelType = steelType,
                    thicknessMm = thickness,
                    config = config,
                    calculationResult = result,
                    pipeMeters = lastPipeMeters
                )
                Toast.makeText(
                    requireContext(),
                    "«$enteredName» добавлено в корзину",
                    Toast.LENGTH_SHORT
                ).show()

                val bottomNav =
                    requireActivity()
                        .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                            R.id.bottom_nav
                        )
                bottomNav.selectedItemId = R.id.dest_cart
            }
        }
    }

    // ── Диалог названия ──────────────────────────────────

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
                if (name.isNotEmpty()) name = name.replaceFirstChar { it.uppercase() }
                if (name.isEmpty()) onConfirm("Станция ${width}×${depth}×${height} мм")
                else onConfirm(name)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
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

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun hideAllResults() {
        listOf(
            binding.tvResult, binding.tvWeightAisi304, binding.tvWeightAisi430,
            binding.tvCountertopWeight, binding.tvPocketWeightAddition, binding.tvSinkWeight,
            binding.tvInsertWeight, binding.tvPartitionsWeight,
            binding.tvFaucetHolesResult, binding.tvBackBoardResult,
            binding.tvBlenderShelfWeight, binding.tvAdjustableLegResult,
            binding.tvSolidSinkResult,
            binding.tvPipeResult, binding.btnAddToCart
        ).forEach { it.visibility = View.GONE }
    }

    private fun showAllResults() {
        listOf(
            binding.tvResult, binding.tvWeightAisi304, binding.tvWeightAisi430,
            binding.tvCountertopWeight, binding.tvSinkWeight,
            binding.tvInsertWeight, binding.tvPartitionsWeight,
            binding.tvPipeResult, binding.btnAddToCart
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
