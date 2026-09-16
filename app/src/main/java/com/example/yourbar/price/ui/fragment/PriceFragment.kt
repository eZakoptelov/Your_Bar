package com.example.yourbar.price.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.yourbar.R
import com.example.yourbar.budget.data.price.PriceRepository
import com.example.yourbar.databinding.FragmentPriceBinding
import com.example.yourbar.price.domain.models.BudgetPrices
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class PriceFragment : Fragment() {

    private var _binding: FragmentPriceBinding? = null
    private val binding get() = _binding!!

    private val priceRepository: PriceRepository by inject()

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

        setRubleEndIcon(binding.etPriceAisi304)
        setRubleEndIcon(binding.etPriceAisi430)
        setRubleEndIcon(binding.etPricePipe25)
        setRubleEndIcon(binding.etPricePipe40)
        setRubleEndIcon(binding.etPriceInsulation)
        setRubleEndIcon(binding.etPriceFaucetHole)
        setRubleEndIcon(binding.etPriceBackBoard)
        setRubleEndIcon(binding.etPriceAdjustableLeg)
        setRubleEndIcon(binding.etPriceSink400x400)
        setRubleEndIcon(binding.etPriceSink400x500)
        setRubleEndIcon(binding.etPriceSink500x500)
        setRubleEndIcon(binding.etPriceSink500x400)


        binding.btnSavePrices.setOnClickListener {
            savePrices()
        }
    }

    private fun loadPricesIntoFields() {
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
            adjustableLegPerPiece = binding.etPriceAdjustableLeg.text.toString().toDoubleOrNull()
                ?: 0.0,
            sink400x400PerPiece = binding.etPriceSink400x400.text.toString().toDoubleOrNull()
                ?: 0.0,
            sink400x500PerPiece = binding.etPriceSink400x500.text.toString().toDoubleOrNull()
                ?: 0.0,
            sink500x500PerPiece = binding.etPriceSink500x500.text.toString().toDoubleOrNull() ?: 0.0,
            sink500x400PerPiece = binding.etPriceSink500x400.text.toString().toDoubleOrNull() ?: 0.0

            )

        priceRepository.save(prices)
        showStatus("Цены сохранены!", true)
        Toast.makeText(requireContext(), "Цены сохранены", Toast.LENGTH_SHORT).show()
    }

    private fun setRubleEndIcon(editText: TextInputEditText) {
        val text = "₽"
        val paint = android.graphics.Paint()
        paint.color = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        paint.textSize = editText.textSize
        paint.isAntiAlias = true

        val bounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val icon = object : android.graphics.drawable.Drawable() {
            override fun draw(canvas: android.graphics.Canvas) {
                canvas.drawText(text, 0f, bounds.height().toFloat(), paint)
            }

            override fun getIntrinsicWidth(): Int = bounds.width()
            override fun getIntrinsicHeight(): Int = bounds.height()
            override fun getOpacity(): Int = android.graphics.PixelFormat.OPAQUE

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
            }
        }

        (editText.parent as? com.google.android.material.textfield.TextInputLayout)?.apply {
            endIconDrawable = icon
            endIconContentDescription = text
        }
    }

    private fun showStatus(text: String, isSuccess: Boolean) {
        binding.tvStatus.text = text
        val resId = if (isSuccess) R.color.status_success else R.color.status_error
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), resId))
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
