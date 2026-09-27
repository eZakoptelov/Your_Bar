package com.example.yourbar.budget.presentation.calculator.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.RadioGroup
import com.example.yourbar.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

class BlenderShelfBottomSheet(
    private val currentWidthMm: Int,
    private val onApply: (widthMm: Int) -> Unit
) {
    fun show(context: Context) {
        val dialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_blender_shelf_bottom_sheet, null)

        val rgShelfWidth = sheetView.findViewById<RadioGroup>(R.id.rgShelfWidth)
        val btnApply = sheetView.findViewById<MaterialButton>(R.id.btnApplyShelf)

        rgShelfWidth.check(when (currentWidthMm) {
            500 -> R.id.rbShelf500
            400 -> R.id.rbShelf400
            300 -> R.id.rbShelf300
            else -> R.id.rbShelfNone
        })

        btnApply.setOnClickListener {
            val widthMm = when (rgShelfWidth.checkedRadioButtonId) {
                R.id.rbShelf500 -> 500
                R.id.rbShelf400 -> 400
                R.id.rbShelf300 -> 300
                else -> 0
            }
            onApply(widthMm)
            dialog.dismiss()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }
}
