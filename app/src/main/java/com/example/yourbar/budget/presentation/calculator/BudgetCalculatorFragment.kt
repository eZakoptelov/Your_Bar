package com.example.yourbar.budget.presentation.calculator

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.yourbar.R
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.models.SolidSinkType
import com.example.yourbar.budget.domain.calculator.models.SteelType
import com.example.yourbar.budget.domain.calculator.usecase.CalculateParamsUseCase
import com.example.yourbar.budget.presentation.viewmodel.BudgetCalculatorViewModel
import com.example.yourbar.budget.presentation.viewmodel.BudgetUiState
import com.example.yourbar.cart.domain.usecase.AddToCartUseCase
import com.example.yourbar.databinding.FragmentBudgetCalculatorBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class BudgetCalculatorFragment : Fragment() {

    private var _binding: FragmentBudgetCalculatorBinding? = null
    private val binding get() = _binding!!

    private var lastResult: CalculationResult? = null
    private var lastPipeMeters: Double = 0.0
    private var solidSinkType: SolidSinkType = SolidSinkType.NONE

    private var pocketCount = 0           // 0, 1, 2
    private var pocketHeightChoice = 0    // 0 = 260 мм (250 чистый), 1 = 210 мм (200 чистый)

    private var blenderShelfWidthMm = 0   // 0 = полки нет, 300/400/500 = ширина


    private lateinit var sharedPreferences: android.content.SharedPreferences

    private val calculateUseCase: CalculateParamsUseCase by inject()
    private val addToCartUseCase: AddToCartUseCase by inject()
    private val viewModel: BudgetCalculatorViewModel by inject()

    companion object {
        private const val PREF_NAME = "prices_settings"
        private const val KEY_FAUCET_HOLE = "price_faucet_hole"
        private const val KEY_BACK_BOARD = "price_back_board"
        private const val KEY_ADJUSTABLE_LEG = "price_adjustable_leg"
    }

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

        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        setupPocketButton()
        setupSinkButton()
        setupBlenderShelfButton()
        setupFaucetHolesSpinner()
        setupBackBoardSpinner()
        setupAdjustableLegSpinner()
        setupButtons()
        observeViewModel()
    }

    // ── UI setup ──────────────────────────────────────────

    private fun setupPocketButton() {
        updatePocketButtonState()

        binding.btnAddAdditionalPocket.setOnClickListener {
            showPocketBottomSheet()
        }
    }

    private fun setupSinkButton() {
        updateSinkButtonState()

        binding.btnAddSolidSink.setOnClickListener {
            showSinkBottomSheet()
        }
    }

    private fun setupBlenderShelfButton() {
        updateBlenderShelfButtonState()
    }

    private fun showPocketBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.dialog_pockets_bottom_sheet, null)

        val rgCount = sheetView.findViewById<RadioGroup>(R.id.rgPocketCount)
        val rgHeight = sheetView.findViewById<RadioGroup>(R.id.rgPocketHeight)
        val btnApply = sheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApplyPockets)
        val tvHeightLabel = sheetView.findViewById<android.widget.TextView>(R.id.tvHeightLabel)

        rgCount.check(when (pocketCount) {
            1 -> R.id.rbPocket1
            2 -> R.id.rbPocket2
            else -> R.id.rbPocket0
        })

        rgHeight.check(if (pocketHeightChoice == 0) R.id.rbHeight260 else R.id.rbHeight210)

        fun updateHeightEnabled(enabled: Boolean) {
            for (i in 0 until rgHeight.childCount) {
                rgHeight.getChildAt(i).isEnabled = enabled
            }
            tvHeightLabel.alpha = if (enabled) 1f else 0.4f
            rgHeight.alpha = if (enabled) 1f else 0.4f
        }

        updateHeightEnabled(pocketCount > 0)

        rgCount.setOnCheckedChangeListener { _, checkedId ->
            val count = when (checkedId) {
                R.id.rbPocket1 -> 1
                R.id.rbPocket2 -> 2
                else -> 0
            }
            updateHeightEnabled(count > 0)
        }

        btnApply.setOnClickListener {
            pocketCount = when (rgCount.checkedRadioButtonId) {
                R.id.rbPocket1 -> 1
                R.id.rbPocket2 -> 2
                else -> 0
            }
            pocketHeightChoice = if (rgHeight.checkedRadioButtonId == R.id.rbHeight260) 0 else 1

            updatePocketButtonState()

            if (areFieldsFilled()) {
                hideKeyboard()
                calculate()
            }
            dialog.dismiss()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun updatePocketButtonState() {
        val btn = binding.btnAddAdditionalPocket
        if (pocketCount == 0) {
            btn.text = "Добавить навесной карман"
            btn.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.shelf_button_default)
            )
            btn.setTextColor(Color.BLACK)
        } else {
            val stationWidth = binding.etWidth.text.toString().trim().toIntOrNull() ?: 0
            val pocketWidthMm = if (blenderShelfWidthMm > 0) {
                maxOf(stationWidth - blenderShelfWidthMm, 0)
            } else {
                stationWidth
            }
            btn.text = "Карман: ${pocketCount} шт, ${pocketWidthMm} мм"
            btn.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.shelf_button_added)
            )
            btn.setTextColor(Color.WHITE)
        }
    }


    private fun getPocketHeightMm(): Int = if (pocketHeightChoice == 0) 260 else 210

    private fun setupFaucetHolesSpinner() {
        val options = listOf("0 шт", "1 шт", "2 шт", "3 шт", "4 шт", "5 шт")
        binding.spFaucetHoles.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spFaucetHoles.setSelection(0)
        binding.spFaucetHoles.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (areFieldsFilled()) calculate()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupBackBoardSpinner() {
        val options = listOf("0 шт", "1 шт", "2 шт", "3 шт")
        binding.spBackBoard.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spBackBoard.setSelection(0)
        binding.spBackBoard.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (areFieldsFilled()) calculate()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupAdjustableLegSpinner() {
        val options = listOf("0 шт", "2 шт", "4 шт", "6 шт", "8 шт")
        binding.spAdjustableLeg.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spAdjustableLeg.setSelection(0)
        binding.spAdjustableLeg.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (areFieldsFilled()) calculate()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupButtons() {
        binding.btnCalculate.setOnClickListener {
            hideKeyboard()
            calculate()
        }
        binding.btnAddToCart.setOnClickListener { addToCart() }
        binding.btnAddShelfForBlender.setOnClickListener {
            showBlenderShelfBottomSheet()
        }
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

    // ── Полка для блендера (Bottom Sheet) ─────────────────

    private fun showBlenderShelfBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.dialog_blender_shelf_bottom_sheet, null)

        val rgShelfWidth = sheetView.findViewById<RadioGroup>(R.id.rgShelfWidth)
        val btnApply = sheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApplyShelf)

        // Текущий выбор
        rgShelfWidth.check(when (blenderShelfWidthMm) {
            500 -> R.id.rbShelf500
            400 -> R.id.rbShelf400
            300 -> R.id.rbShelf300
            else -> R.id.rbShelfNone
        })

        btnApply.setOnClickListener {
            blenderShelfWidthMm = when (rgShelfWidth.checkedRadioButtonId) {
                R.id.rbShelf500 -> 500
                R.id.rbShelf400 -> 400
                R.id.rbShelf300 -> 300
                else -> 0
            }

            updateBlenderShelfButtonState()
            updatePocketButtonState()

            if (areFieldsFilled()) {
                hideKeyboard()
                calculate()
            }
            dialog.dismiss()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun updateBlenderShelfButtonState() {
        val btn = binding.btnAddShelfForBlender
        if (blenderShelfWidthMm == 0) {
            btn.text = "Добавить полку для блендера"
            btn.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.shelf_button_default)
            )
            btn.setTextColor(Color.BLACK)
        } else {
            btn.text = "Полка для блендера: ${blenderShelfWidthMm} мм"
            btn.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.shelf_button_added)
            )
            btn.setTextColor(Color.WHITE)
        }
    }


    // ── Расчёт ────────────────────────────────────────────

    private fun getFaucetHoleCount(): Int = binding.spFaucetHoles.selectedItemPosition
    private fun getBackBoardCount(): Int = binding.spBackBoard.selectedItemPosition
    private fun getAdjustableLegCount(): Int = binding.spAdjustableLeg.selectedItemPosition * 2

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

        val pocketHeightMm = getPocketHeightMm()
        val isShelfAdded = blenderShelfWidthMm > 0
        val faucetHoleCount = getFaucetHoleCount()
        val backBoardCount = getBackBoardCount()
        val adjustableLegCount = getAdjustableLegCount()

        try {
            val result = calculateUseCase.execute(
                widthMm = width,
                depthMm = depth,
                steelType = steelType,
                thicknessMm = thickness,
                pocketCount = pocketCount,
                pocketHeightMm = pocketHeightMm,
                isShelfAdded = isShelfAdded,
                blenderShelfWidthMm = blenderShelfWidthMm,
                faucetHoleCount = faucetHoleCount,
                backBoardCount = backBoardCount,
                adjustableLegCount = adjustableLegCount,
                solidSinkType = solidSinkType
            )
            lastResult = result

            val formatted = CalculationFormatter.format(result, pocketCount)

            binding.tvResult.text = formatted.totalWeight
            binding.tvWeightAisi304.text = formatted.aisi304
            binding.tvWeightAisi430.text = formatted.aisi430
            binding.tvCountertopWeight.text = formatted.countertop
            binding.tvSinkWeight.text = formatted.sink
            binding.tvInsertWeight.text = formatted.insert
            binding.tvPartitionsWeight.text = formatted.partitions

            // ── Карман для бутылок ──
            if (pocketCount > 0) {
                binding.tvPocketWeightAddition.text = formatted.pocket
                binding.tvPocketWeightAddition.visibility = View.VISIBLE
            } else {
                binding.tvPocketWeightAddition.visibility = View.GONE
            }

            // ── Цельнотянутая мойка ──
            if (solidSinkType != SolidSinkType.NONE) {
                binding.tvSolidSinkResult.text = "Цельнотянутая мойка: ${solidSinkType.label} мм — 1 шт"
                binding.tvSolidSinkResult.visibility = View.VISIBLE
            } else {
                binding.tvSolidSinkResult.visibility = View.GONE
            }

            // ── Отверстия для смесителя ──
            if (faucetHoleCount > 0) {
                binding.tvFaucetHolesResult.text = formatted.faucetHoles
                binding.tvFaucetHolesResult.visibility = View.VISIBLE
            } else {
                binding.tvFaucetHolesResult.visibility = View.GONE
            }

            // ── Задний борт ──
            if (backBoardCount > 0) {
                binding.tvBackBoardResult.text = "Задний борт: $backBoardCount шт"
                binding.tvBackBoardResult.visibility = View.VISIBLE
            } else {
                binding.tvBackBoardResult.visibility = View.GONE
            }

            // ── Регулируемая опора ──
            if (adjustableLegCount > 0) {
                binding.tvAdjustableLegResult.text = "Регулируемая опора: $adjustableLegCount шт"
                binding.tvAdjustableLegResult.visibility = View.VISIBLE
            } else {
                binding.tvAdjustableLegResult.visibility = View.GONE
            }
            // ── Полка для блендера ──
            if (blenderShelfWidthMm > 0) {
                binding.tvBlenderShelfWeight.text =
                    "Полка для блендера:Ширина ${blenderShelfWidthMm} мм- ${DecimalFormat("0.##").format(result.blenderShelfWeightKg)} кг"
                binding.tvBlenderShelfWeight.visibility = View.VISIBLE
            } else {
                binding.tvBlenderShelfWeight.visibility = View.GONE
            }


            showAllResults()
            viewModel.calculate(widthMm = width, depthMm = depth, heightMm = height)

        } catch (e: IllegalArgumentException) {
            showError(e.message ?: "Ошибка расчёта")
        } catch (_: Exception) {
            showError("Произошла непредвиденная ошибка")
        }
    }

    // ── Корзина ───────────────────────────────────────────

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

        val isShelfAdded = blenderShelfWidthMm > 0

        val faucetHolePricePerUnit = sharedPreferences.getFloat(KEY_FAUCET_HOLE, 0f).toDouble()
        val backBoardPricePerUnit = sharedPreferences.getFloat(KEY_BACK_BOARD, 0f).toDouble()
        val adjustableLegPricePerUnit = sharedPreferences.getFloat(KEY_ADJUSTABLE_LEG, 0f).toDouble()

        // Цены цельнотянутых моек
        val sink400x400Price = sharedPreferences.getFloat("price_sink_400x400", 0f).toDouble()
        val sink400x500Price = sharedPreferences.getFloat("price_sink_400x500", 0f).toDouble()
        val sink500x500Price = sharedPreferences.getFloat("price_sink_500x500", 0f).toDouble()
        val sink500x400Price = sharedPreferences.getFloat("price_sink_500x400", 0f).toDouble()

        showNameDialog { enteredName ->
            addToCartUseCase.execute(
                name = enteredName,
                widthMm = width,
                depthMm = depth,
                heightMm = height,
                steelType = steelType.name,
                thicknessMm = thickness,
                pocketsCount = pocketCount,
                calculationResult = result,
                pipeMeters = lastPipeMeters,
                isBlenderShelfAdded = isShelfAdded,
                blenderShelfWidthMm = blenderShelfWidthMm,
                faucetHolePricePerUnit = faucetHolePricePerUnit,
                backBoardPricePerUnit = backBoardPricePerUnit,
                adjustableLegPricePerUnit = adjustableLegPricePerUnit,
                solidSinkType = solidSinkType,
                sink400x400Price = sink400x400Price,
                sink400x500Price = sink400x500Price,
                sink500x500Price = sink500x500Price,
                sink500x400Price = sink500x400Price
            )
            Toast.makeText(requireContext(), "«$enteredName» добавлено в корзину", Toast.LENGTH_SHORT).show()

            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
            bottomNav.selectedItemId = R.id.dest_cart
        }
    }

    // ── Диалоги ───────────────────────────────────────────

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
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

    // ── Цельнотянутая мойка (Bottom Sheet) ────────────────

    private fun showSinkBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.dialog_sink_bottom_sheet, null)

        val rgSinkType = sheetView.findViewById<RadioGroup>(R.id.rgSinkType)
        val btnApply =
            sheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApplySink)
        val tvWarning = sheetView.findViewById<android.widget.TextView>(R.id.tvSinkWarning)

        rgSinkType.check(
            when (solidSinkType) {
                SolidSinkType.SINK_400x400 -> R.id.rbSink400x400
                SolidSinkType.SINK_400x500 -> R.id.rbSink400x500
                SolidSinkType.SINK_500x500 -> R.id.rbSink500x500
                SolidSinkType.SINK_500x400 -> R.id.rbSink500x400   // ← добавить
                SolidSinkType.NONE -> R.id.rbSinkNone
            }
        )

        val widthMm = binding.etWidth.text.toString().trim().toIntOrNull() ?: 0
        val depthMm = binding.etDepth.text.toString().trim().toIntOrNull() ?: 0

        fun checkFit(type: SolidSinkType): String? {
            if (type == SolidSinkType.NONE) return null

            if (type.depthMm + 100 > depthMm) {
                return "Мойка ${type.label} не влезет по глубине (нужно ${type.depthMm + 100} мм, станция $depthMm мм)"
            }

            if (type.widthMm + 150 >= widthMm) {
                return "Мойка ${type.label} не влезет по ширине (нужно ${type.widthMm + 120} мм, станция $widthMm мм)"
            }

            return null
        }


        fun updateWarning() {
            val selected = when (rgSinkType.checkedRadioButtonId) {
                R.id.rbSink400x400 -> SolidSinkType.SINK_400x400
                R.id.rbSink400x500 -> SolidSinkType.SINK_400x500
                R.id.rbSink500x500 -> SolidSinkType.SINK_500x500
                R.id.rbSink500x400 -> SolidSinkType.SINK_500x400
                else -> SolidSinkType.NONE
            }
            val warning = checkFit(selected)
            if (warning != null) {
                tvWarning.text = warning
                tvWarning.visibility = View.VISIBLE
                btnApply.isEnabled = false
                btnApply.alpha = 0.4f
            } else {
                tvWarning.visibility = View.GONE
                btnApply.isEnabled = true
                btnApply.alpha = 1f
            }
        }

        rgSinkType.setOnCheckedChangeListener { _, _ -> updateWarning() }
        updateWarning()

        btnApply.setOnClickListener {
            solidSinkType = when (rgSinkType.checkedRadioButtonId) {
                R.id.rbSink400x400 -> SolidSinkType.SINK_400x400
                R.id.rbSink400x500 -> SolidSinkType.SINK_400x500
                R.id.rbSink500x500 -> SolidSinkType.SINK_500x500
                R.id.rbSink500x400 -> SolidSinkType.SINK_500x400
                else -> SolidSinkType.NONE
            }

            updateSinkButtonState()

            if (areFieldsFilled()) {
                hideKeyboard()
                calculate()
            }
            dialog.dismiss()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

        private fun updateSinkButtonState() {
        val btn = binding.btnAddSolidSink
        if (solidSinkType == SolidSinkType.NONE) {
            btn.text = "Добавить цельнотянутую мойку"
            btn.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.shelf_button_default)
            )
            btn.setTextColor(Color.BLACK)
        } else {
            btn.text = "Мойка: ${solidSinkType.label} мм"
            btn.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.shelf_button_added)
            )
            btn.setTextColor(Color.WHITE)
        }
    }
}
