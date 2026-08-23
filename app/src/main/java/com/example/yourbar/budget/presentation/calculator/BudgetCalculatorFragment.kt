package com.example.yourbar.budget.presentation.calculator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.yourbar.R
import com.example.yourbar.databinding.FragmentBudgetCalculatorBinding
import com.example.yourbar.budget.data.calculator.CalculatorRepository
import com.example.yourbar.budget.domain.calculator.models.CalculationInputParams
import com.example.yourbar.budget.domain.calculator.models.SteelType
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class BudgetCalculatorFragment : Fragment() {

    private var _binding: FragmentBudgetCalculatorBinding? = null
    private val binding get() = _binding!!

    // Внедряем репозиторий через Koin
    private val repository: CalculatorRepository by inject()

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

        binding.btnCalculate.setOnClickListener {
            hideKeyboard()
            calculateWeightAndArea()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun calculateWeightAndArea() {
        // 1. Сбор данных (валидация)
        val userWidthMm = binding.etWidth.text.toString().trim().toIntOrNull()
        val userDepthMm = binding.etDepth.text.toString().trim().toIntOrNull()

        if (userWidthMm == null || userWidthMm <= 0) {
            showError("Введите корректную ширину")
            return
        }
        if (userDepthMm == null || userDepthMm <= 0) {
            showError("Введите корректную глубину")
            return
        }

        val steelId = binding.rgSteelType.checkedRadioButtonId
        val steelType = when (steelId) {
            R.id.rbAisi304 -> SteelType.AISI_304
            R.id.rbAisi430 -> SteelType.AISI_430
            else -> {
                showError("Выберите марку стали")
                return
            }
        }

        val thicknessId = binding.rgThickness.checkedRadioButtonId
        val thicknessMm = when (thicknessId) {
            R.id.rb1mm -> 1.0
            R.id.rb1_5mm -> 1.5
            R.id.rb2mm -> 2.0
            else -> {
                showError("Выберите толщину")
                return
            }
        }

        // 2. Подготовка параметров
        val params = CalculationInputParams(
            widthMm = userWidthMm,
            depthMm = userDepthMm,
            steelType = steelType,
            thicknessMm = thicknessMm
        )

        // 3. Вызов логики
        try {
            val result = repository.calculate(params)

            val df = DecimalFormat("0.##")

            binding.tvResult.text = getString(R.string.result_weight, df.format(result.totalWeightKg))
            binding.tvCountertopWeight.text =
                getString(R.string.countertop_weight_line, df.format(result.countertopWeightKg))
            binding.tvPocketWeightAddition.text =
                getString(R.string.pocket_weight_line, df.format(result.pocketWeightKg))
            binding.tvSinkWeight.text =
                getString(R.string.sink_weight_line, df.format(result.sinkWeightKg))
            binding.tvInsertWeight.text =
                getString(R.string.insert_weight_line, df.format(result.insertWeightKg))
            binding.tvPartitionsWeight.text =
                getString(R.string.partitions_weight_line, df.format(result.partitionsWeightKg))

            // Раздельные веса
            binding.tvWeightAisi304.text =
                getString(R.string.aisi304_weight_line, df.format(result.weightAisi304Kg))
            binding.tvWeightAisi430.text =
                getString(R.string.aisi430_weight_line, df.format(result.weightAisi430Kg))

        } catch (e: IllegalArgumentException) {
            showError(e.message ?: "Ошибка расчёта")
        } catch (e: Exception) {
            showError("Произошла непредвиденная ошибка")
        }
    }

    private fun showError(message: String) {
        binding.tvResult.text = message
        // Очищаем остальные поля
        binding.tvCountertopWeight.text = ""
        binding.tvPocketWeightAddition.text = ""
        binding.tvSinkWeight.text = ""
        binding.tvInsertWeight.text = ""
        binding.tvPartitionsWeight.text = ""
        binding.tvWeightAisi304.text = ""
        binding.tvWeightAisi430.text = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
