package com.example.yourbar.budget.presentation.calculator.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.widget.TextView
import com.example.yourbar.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

class PocketBottomSheet(
    private val currentCount: Int,
    private val currentHeightChoice: Int,
    private val onApply: (count: Int, heightChoice: Int) -> Unit
) {
    fun show(context: Context) {
        val dialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_pockets_bottom_sheet, null)

        val rgCount = sheetView.findViewById<RadioGroup>(R.id.rgPocketCount)
        val rgHeight = sheetView.findViewById<RadioGroup>(R.id.rgPocketHeight)
        val btnApply = sheetView.findViewById<MaterialButton>(R.id.btnApplyPockets)
        val tvHeightLabel = sheetView.findViewById<TextView>(R.id.tvHeightLabel)

        rgCount.check(when (currentCount) {
            1 -> R.id.rbPocket1
            2 -> R.id.rbPocket2
            else -> R.id.rbPocket0
        })

        rgHeight.check(if (currentHeightChoice == 0) R.id.rbHeight260 else R.id.rbHeight210)

        fun updateHeightEnabled(enabled: Boolean) {
            for (i in 0 until rgHeight.childCount) {
                rgHeight.getChildAt(i).isEnabled = enabled
            }
            tvHeightLabel.alpha = if (enabled) 1f else 0.4f
            rgHeight.alpha = if (enabled) 1f else 0.4f
        }

        updateHeightEnabled(currentCount > 0)

        rgCount.setOnCheckedChangeListener { _, checkedId ->
            val count = when (checkedId) {
                R.id.rbPocket1 -> 1
                R.id.rbPocket2 -> 2
                else -> 0
            }
            updateHeightEnabled(count > 0)
        }

        btnApply.setOnClickListener {
            val count = when (rgCount.checkedRadioButtonId) {
                R.id.rbPocket1 -> 1
                R.id.rbPocket2 -> 2
                else -> 0
            }
            val heightChoice = if (rgHeight.checkedRadioButtonId == R.id.rbHeight260) 0 else 1
            onApply(count, heightChoice)
            dialog.dismiss()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }
}
