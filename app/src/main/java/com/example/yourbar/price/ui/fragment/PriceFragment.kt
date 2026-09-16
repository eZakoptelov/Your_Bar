package com.example.yourbar.price.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.yourbar.budget.data.price.PriceRepository
import com.example.yourbar.databinding.FragmentPriceBinding
import com.example.yourbar.price.domain.models.BudgetPrices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class PriceFragment : Fragment() {

    private var _binding: FragmentPriceBinding? = null
    private val binding get() = _binding!!

    private val priceRepository: PriceRepository by inject()

    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPricesIntoFields()

        setupAutoSave(
            binding.etPriceAisi304,
            binding.etPriceAisi430,
            binding.etPricePipe25,
            binding.etPricePipe40,
            binding.etPriceInsulation,
            binding.etPriceFaucetHole,
            binding.etPriceBackBoard,
            binding.etPriceAdjustableLeg,
            binding.etPriceSink400x400,
            binding.etPriceSink400x500,
            binding.etPriceSink500x500,
            binding.etPriceSink500x400
        )
    }

    private fun setupAutoSave(vararg fields: android.widget.EditText) {
        fields.forEach { field ->
            field.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isLoading) return
                    savePrices()
                }
            })
        }
    }

    private fun loadPricesIntoFields() {
        isLoading = true
        val prices = priceRepository.load()

        binding.etPriceAisi304.setText(prices.aisi304PerKg.toCleanString())
        binding.etPriceAisi430.setText(prices.aisi430PerKg.toCleanString())
        binding.etPricePipe25.setText(prices.pipe25PerM.toCleanString())
        binding.etPricePipe40.setText(prices.pipe40PerM.toCleanString())
        binding.etPriceInsulation.setText(prices.insulationPerM2.toCleanString())
        binding.etPriceFaucetHole.setText(prices.faucetHolePerPiece.toCleanString())
        binding.etPriceBackBoard.setText(prices.backBoardPerPiece.toCleanString())
        binding.etPriceAdjustableLeg.setText(prices.adjustableLegPerPiece.toCleanString())
        binding.etPriceSink400x400.setText(prices.sink400x400PerPiece.toCleanString())
        binding.etPriceSink400x500.setText(prices.sink400x500PerPiece.toCleanString())
        binding.etPriceSink500x500.setText(prices.sink500x500PerPiece.toCleanString())
        binding.etPriceSink500x400.setText(prices.sink500x400PerPiece.toCleanString())

        isLoading = false
    }

    private fun savePrices() {
        val prices = BudgetPrices(
            aisi304PerKg = binding.etPriceAisi304.text.toString().toDoubleOrNull() ?: 0.0,
            aisi430PerKg = binding.etPriceAisi430.text.toString().toDoubleOrNull() ?: 0.0,
            pipe25PerM = binding.etPricePipe25.text.toString().toDoubleOrNull() ?: 0.0,
            pipe40PerM = binding.etPricePipe40.text.toString().toDoubleOrNull() ?: 0.0,
            insulationPerM2 = binding.etPriceInsulation.text.toString().toDoubleOrNull() ?: 0.0,
            faucetHolePerPiece = binding.etPriceFaucetHole.text.toString().toDoubleOrNull() ?: 0.0,
            backBoardPerPiece = binding.etPriceBackBoard.text.toString().toDoubleOrNull() ?: 0.0,
            adjustableLegPerPiece = binding.etPriceAdjustableLeg.text.toString().toDoubleOrNull() ?: 0.0,
            sink400x400PerPiece = binding.etPriceSink400x400.text.toString().toDoubleOrNull() ?: 0.0,
            sink400x500PerPiece = binding.etPriceSink400x500.text.toString().toDoubleOrNull() ?: 0.0,
            sink500x500PerPiece = binding.etPriceSink500x500.text.toString().toDoubleOrNull() ?: 0.0,
            sink500x400PerPiece = binding.etPriceSink500x400.text.toString().toDoubleOrNull() ?: 0.0
        )

        lifecycleScope.launch(Dispatchers.IO) {
            priceRepository.save(prices)
        }
    }

    override fun onPause() {
        super.onPause()
        _binding?.let { savePrices() }
    }

    private fun Double.toCleanString(): String {
        if (this <= 0.0) return ""
        val formatted = DecimalFormat("0.##").format(this)
        return if (formatted.endsWith(".0")) formatted.dropLast(2) else formatted
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
