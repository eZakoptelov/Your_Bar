package com.example.yourbar.budget.presentation.calculator.util

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.yourbar.R
import com.google.android.material.button.MaterialButton

object ButtonStateHelper {

    fun setDefault(btn: MaterialButton, text: String, context: Context) {
        btn.text = text
        btn.setBackgroundColor(
            ContextCompat.getColor(context, R.color.shelf_button_default)
        )
        btn.setTextColor(Color.BLACK)
    }

    fun setAdded(btn: MaterialButton, text: String, context: Context) {
        btn.text = text
        btn.setBackgroundColor(
            ContextCompat.getColor(context, R.color.shelf_button_added)
        )
        btn.setTextColor(Color.WHITE)
    }
}
