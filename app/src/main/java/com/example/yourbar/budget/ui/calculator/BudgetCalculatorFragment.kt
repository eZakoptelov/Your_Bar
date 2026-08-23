package com.example.yourbar.budget.ui.calculator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.yourbar.R
import com.example.yourbar.databinding.FragmentBudgetCalculatorBinding
import java.text.DecimalFormat

class BudgetCalculatorFragment : Fragment() {

    private var _binding: FragmentBudgetCalculatorBinding? = null
    private val binding get() = _binding!!

    // Величина автоматического добавления к каждому размеру (в мм)
    private val autoAddMm = 90

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

    /**
     * Скрывает программную клавиатуру
     */
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // Скрываем клавиатуру, привязываясь к окну текущего фрагмента/активности
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun calculateWeightAndArea() {
        // 1. Читаем ширину
        val widthText = binding.etWidth.text.toString().trim()
        if (widthText.isEmpty()) {
            showError(getString(R.string.error_message, "Введите ширину"))
            return
        }
        val widthBaseMm = widthText.toIntOrNull() ?: run {
            showError(getString(R.string.error_message, "Некорректная ширина"))
            return
        }

        // 2. Читаем глубину
        val depthText = binding.etDepth.text.toString().trim()
        if (depthText.isEmpty()) {
            showError(getString(R.string.error_message, "Введите глубину"))
            return
        }
        val depthBaseMm = depthText.toIntOrNull() ?: run {
            showError(getString(R.string.error_message, "Некорректная глубина"))
            return
        }

        // Добавляем по +90 мм к ширине и глубине
        val widthMm = widthBaseMm + autoAddMm
        val depthMm = depthBaseMm + autoAddMm

        // 3. Выбираем марку стали (плотность)
        val steelId = binding.rgSteelType.checkedRadioButtonId
        val density = when (steelId) {
            R.id.rbAisi304 -> 7900.0 // кг/м³
            R.id.rbAisi430 -> 7700.0
            else -> {
                showError(getString(R.string.error_message, "Выберите марку стали"))
                return
            }
        }

        // 4. Выбираем толщину
        val thicknessId = binding.rgThickness.checkedRadioButtonId
        val thicknessMm = when (thicknessId) {
            R.id.rb1mm -> 1.0
            R.id.rb1_5mm -> 1.5
            R.id.rb2mm -> 2.0
            else -> {
                showError(getString(R.string.error_message, "Выберите толщину"))
                return
            }
        }

        // Переводим мм в метры для расчётов
        val widthM = widthMm / 1000.0
        val depthM = depthMm / 1000.0
        val thicknessM = thicknessMm / 1000.0

        // Считаем площадь (ширина × глубина) — уже с учётом +90 мм
        val area = widthM * depthM // м²

        // Считаем объём и вес
        val volume = area * thicknessM // м³
        val weight = volume * density // кг

        val df = DecimalFormat("0.##")

        // Выводим результаты через строковые ресурсы
        val formattedWeight = df.format(weight)
        val formattedArea = df.format(area)

        binding.tvResult.text = getString(R.string.result_weight, formattedWeight)
        binding.tvArea.text = getString(R.string.result_area, formattedArea)
        binding.tvDimensions.text = getString(R.string.result_dimensions, widthMm, depthMm)
    }

    private fun showError(message: String) {
        binding.tvResult.text = message
        binding.tvArea.text = ""
        binding.tvDimensions.text = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
