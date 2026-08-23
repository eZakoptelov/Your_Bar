package com.example.yourbar.budget.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yourbar.R
import com.example.yourbar.databinding.FragmentBudgetCalculatorBinding
import java.text.DecimalFormat

class BudgetCalculatorFragment : Fragment() {

    private var _binding: FragmentBudgetCalculatorBinding? = null
    private val binding get() = _binding!!

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
            calculateWeight()
        }
    }

    private fun calculateWeight() {
        // 1. Читаем ширину
        val widthText = binding.etWidth.text.toString().trim()
        if (widthText.isEmpty()) {
            showError("Введите ширину")
            return
        }
        val widthMm = widthText.toIntOrNull() ?: run {
            showError("Некорректная ширина")
            return
        }

        // 2. Читаем глубину и добавляем +100 мм
        val depthText = binding.etDepth.text.toString().trim()
        if (depthText.isEmpty()) {
            showError("Введите глубину")
            return
        }
        val depthBaseMm = depthText.toIntOrNull() ?: run {
            showError("Некорректная глубина")
            return
        }
        val depthMm = depthBaseMm + 100 // +100 мм автоматически

        // 3. Выбираем марку стали (плотность)
        val steelId = binding.rgSteelType.checkedRadioButtonId
        val density = when (steelId) {
            R.id.rbAisi304 -> 7900.0 // кг/м³
            R.id.rbAisi430 -> 7700.0 // чуть легче
            else -> {
                showError("Выберите марку стали")
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
                showError("Выберите толщину")
                return
            }
        }

        // 5. Считаем объём и вес
        // Переводим мм в метры: делим на 1000
        val widthM = widthMm / 1000.0
        val depthM = depthMm / 1000.0
        val thicknessM = thicknessMm / 1000.0

        val volume = widthM * depthM * thicknessM // м³
        val weight = volume * density // кг

        val df = DecimalFormat("0.##")
        binding.tvResult.text = "Вес: ${df.format(weight)} кг"
    }

    private fun showError(message: String) {
        binding.tvResult.text = "Ошибка: $message"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}