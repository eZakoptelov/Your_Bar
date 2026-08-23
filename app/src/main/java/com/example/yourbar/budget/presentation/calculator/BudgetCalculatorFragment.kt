package com.example.yourbar.budget.presentation.calculator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.yourbar.R
import com.example.yourbar.databinding.FragmentBudgetCalculatorBinding
import com.example.yourbar.budget.data.calculator.CalculatorRepository
import com.example.yourbar.budget.domain.calculator.models.CalculationInputParams
import com.example.yourbar.budget.domain.calculator.models.SteelType
import com.example.yourbar.budget.presentation.viewmodel.BudgetCalculatorViewModel
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class BudgetCalculatorFragment : Fragment() {

    private var _binding: FragmentBudgetCalculatorBinding? = null
    private val binding get() = _binding!!

    private val repository: CalculatorRepository by inject()
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

        updateToggleButtonColor()

        // Toggle не запускает расчёт
        binding.tbRemoveAdditionalPocket.setOnCheckedChangeListener { _, _ ->
            updateToggleButtonColor()
        }

        binding.btnCalculate.setOnClickListener {
            hideKeyboard()
            calculateWeightAndArea()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                com.example.yourbar.budget.presentation.viewmodel.BudgetUiState.Idle -> {
                    binding.tvPipeResult.visibility = View.GONE
                }
                is com.example.yourbar.budget.presentation.viewmodel.BudgetUiState.Result -> {
                    val df = DecimalFormat("0.##")
                    binding.tvPipeResult.text = "Труба 25×25: ${df.format(state.pipeMeters)} мп"
                    binding.tvPipeResult.visibility = View.VISIBLE
                }
                is com.example.yourbar.budget.presentation.viewmodel.BudgetUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    binding.tvPipeResult.visibility = View.GONE
                }
            }
        }
    }

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

    private fun calculateWeightAndArea() {
        val userWidthMm = binding.etWidth.text.toString().trim().toIntOrNull()
        val userDepthMm = binding.etDepth.text.toString().trim().toIntOrNull()
        val userHeightMm = binding.etHeight.text.toString().trim().toIntOrNull()

        // Сразу скрываем все результаты — на случай повторного расчёта
        hideAllResults()

        if (userWidthMm == null || userWidthMm <= 0) {
            return showError("Введите корректную ширину")
        }
        if (userDepthMm == null || userDepthMm <= 0) {
            return showError("Введите корректную глубину")
        }
        if (userHeightMm == null || userHeightMm <= 0) {
            return showError("Введите корректную высоту")
        }

        if (userWidthMm <= 60 || userDepthMm <= 60 || userHeightMm <= 60) {
            return showError("Размеры должны быть больше 60 мм для расчёта трубы")
        }

        val steelId = binding.rgSteelType.checkedRadioButtonId
        val steelType = when (steelId) {
            R.id.rbAisi304 -> SteelType.AISI_304
            R.id.rbAisi430 -> SteelType.AISI_430
            else -> {
                return showError("Выберите марку стали")
            }
        }

        val thicknessId = binding.rgThickness.checkedRadioButtonId
        val thicknessMm = when (thicknessId) {
            R.id.rb1mm -> 1.0
            R.id.rb1_5mm -> 1.5
            R.id.rb2mm -> 2.0
            else -> {
                return showError("Выберите толщину")
            }
        }

        val isRemoveToggled = binding.tbRemoveAdditionalPocket.isChecked
        val additionalPocketsCount = if (isRemoveToggled) 0 else 1
        val totalPocketsCount = 1 + additionalPocketsCount

        val params = CalculationInputParams(
            widthMm = userWidthMm,
            depthMm = userDepthMm,
            steelType = steelType,
            thicknessMm = thicknessMm,
            additionalPocketsCount = additionalPocketsCount
        )

        try {
            val result = repository.calculate(params)
            val df = DecimalFormat("0.##")

            // Заполняем тексты
            binding.tvResult.text = getString(R.string.result_weight, df.format(result.totalWeightKg))
            binding.tvWeightAisi304.text =
                getString(R.string.aisi304_weight_line, df.format(result.weightAisi304Kg))
            binding.tvWeightAisi430.text =
                getString(R.string.aisi430_weight_line, df.format(result.weightAisi430Kg))
            binding.tvCountertopWeight.text =
                getString(R.string.countertop_weight_line, df.format(result.countertopWeightKg))

            val pocketLabel = when (totalPocketsCount) {
                1 -> "Карман для бутылок (1 шт):"
                2 -> "Карман для бутылок (2 шт):"
                else -> "Карман для бутылок ($totalPocketsCount шт):"
            }
            binding.tvPocketWeightAddition.text = "$pocketLabel ${df.format(result.pocketWeightKg + result.additionalPocketsTotalWeightKg)} кг"

            binding.tvSinkWeight.text =
                getString(R.string.sink_weight_line, df.format(result.sinkWeightKg))
            binding.tvInsertWeight.text =
                getString(R.string.insert_weight_line, df.format(result.insertWeightKg))
            binding.tvPartitionsWeight.text =
                getString(R.string.partitions_weight_line, df.format(result.partitionsWeightKg))

            // Показываем ВСЕ результаты сразу
            showAllResults()

            // Параллельно запускаем расчёт трубы
            viewModel.calculate(
                widthMm = userWidthMm,
                depthMm = userDepthMm,
                heightMm = userHeightMm
            )

        } catch (e: IllegalArgumentException) {
            showError(e.message ?: "Ошибка расчёта")
        } catch (e: Exception) {
            showError("Произошла непредвиденная ошибка")
        }
    }

    /**
     * Скрывает все TextView с результатами (visibility = GONE)
     */
    private fun hideAllResults() {
        binding.tvResult.visibility = View.GONE
        binding.tvWeightAisi304.visibility = View.GONE
        binding.tvWeightAisi430.visibility = View.GONE
        binding.tvCountertopWeight.visibility = View.GONE
        binding.tvPocketWeightAddition.visibility = View.GONE
        binding.tvSinkWeight.visibility = View.GONE
        binding.tvInsertWeight.visibility = View.GONE
        binding.tvPartitionsWeight.visibility = View.GONE
        binding.tvPipeResult.visibility = View.GONE
    }

    /**
     * Показывает все TextView с результатами (visibility = VISIBLE)
     */
    private fun showAllResults() {
        binding.tvResult.visibility = View.VISIBLE
        binding.tvWeightAisi304.visibility = View.VISIBLE
        binding.tvWeightAisi430.visibility = View.VISIBLE
        binding.tvCountertopWeight.visibility = View.VISIBLE
        binding.tvPocketWeightAddition.visibility = View.VISIBLE
        binding.tvSinkWeight.visibility = View.VISIBLE
        binding.tvInsertWeight.visibility = View.VISIBLE
        binding.tvPartitionsWeight.visibility = View.VISIBLE
        // tvPipeResult будет показан отдельно в observer ViewModel
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        // Результаты уже скрыты через hideAllResults() в начале calculateWeightAndArea
    }
}
