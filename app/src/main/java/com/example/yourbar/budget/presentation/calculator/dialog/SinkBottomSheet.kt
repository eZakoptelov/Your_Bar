package com.example.yourbar.budget.presentation.calculator.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.widget.TextView
import com.example.yourbar.R
import com.example.yourbar.budget.domain.calculator.models.SolidSinkType
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

class SinkBottomSheet(
    private val currentType: SolidSinkType,
    private val stationWidthMm: Int,
    private val stationDepthMm: Int,
    private val onApply: (SolidSinkType) -> Unit
) {
    fun show(context: Context) {
        val dialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_sink_bottom_sheet, null)

        val rgSinkType = sheetView.findViewById<RadioGroup>(R.id.rgSinkType)
        val btnApply = sheetView.findViewById<MaterialButton>(R.id.btnApplySink)
        val tvWarning = sheetView.findViewById<TextView>(R.id.tvSinkWarning)

        rgSinkType.check(
            when (currentType) {
                SolidSinkType.SINK_400x400 -> R.id.rbSink400x400
                SolidSinkType.SINK_400x500 -> R.id.rbSink400x500
                SolidSinkType.SINK_500x500 -> R.id.rbSink500x500
                SolidSinkType.SINK_500x400 -> R.id.rbSink500x400
                SolidSinkType.NONE -> R.id.rbSinkNone
            }
        )

        fun checkFit(type: SolidSinkType): String? {
            if (type == SolidSinkType.NONE) return null
            if (type.depthMm + 100 > stationDepthMm) {
                return "Мойка ${type.label} не влезет по глубине (нужно ${type.depthMm + 100} мм, станция $stationDepthMm мм)"
            }
            if (type.widthMm + 150 >= stationWidthMm) {
                return "Мойка ${type.label} не влезет по ширине (нужно ${type.widthMm + 120} мм, станция $stationWidthMm мм)"
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
                tvWarning.visibility = android.view.View.VISIBLE
                btnApply.isEnabled = false
                btnApply.alpha = 0.4f
            } else {
                tvWarning.visibility = android.view.View.GONE
                btnApply.isEnabled = true
                btnApply.alpha = 1f
            }
        }

        rgSinkType.setOnCheckedChangeListener { _, _ -> updateWarning() }
        updateWarning()

        btnApply.setOnClickListener {
            val type = when (rgSinkType.checkedRadioButtonId) {
                R.id.rbSink400x400 -> SolidSinkType.SINK_400x400
                R.id.rbSink400x500 -> SolidSinkType.SINK_400x500
                R.id.rbSink500x500 -> SolidSinkType.SINK_500x500
                R.id.rbSink500x400 -> SolidSinkType.SINK_500x400
                else -> SolidSinkType.NONE
            }
            onApply(type)
            dialog.dismiss()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }
}
