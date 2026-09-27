package com.example.yourbar.budget.presentation.calculator.util

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

object SpinnerSetup {

    fun setup(
        spinner: Spinner,
        options: List<String>,
        context: Context,
        onChanged: () -> Unit
    ) {
        spinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            options
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.setSelection(0)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
