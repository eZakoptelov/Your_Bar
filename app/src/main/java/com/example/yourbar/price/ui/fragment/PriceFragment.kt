package com.example.yourbar.price.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yourbar.R
import com.example.yourbar.databinding.FragmentPriceBinding
import java.text.DecimalFormat
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

class PriceFragment : Fragment() {

    private var _binding: FragmentPriceBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val PREF_NAME = "prices_settings"
        const val KEY_AISI304 = "price_aisi304"
        const val KEY_AISI430 = "price_aisi430"
        const val KEY_PIPE25 = "price_pipe25"
        const val KEY_PIPE40 = "price_pipe40"
    }

    private lateinit var sharedPreferences: android.content.SharedPreferences

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

        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadPrices()
        // Устанавливаем знак ₽ как endIcon для всех полей
        setRubleEndIcon(binding.etPriceAisi304)
        setRubleEndIcon(binding.etPriceAisi430)
        setRubleEndIcon(binding.etPricePipe25)
        setRubleEndIcon(binding.etPricePipe40)

        binding.btnSavePrices.setOnClickListener {
            savePrices()
        }
    }
    private fun setRubleEndIcon(editText: TextInputEditText) {
        val text = "₽"
        val paint = android.graphics.Paint()
        paint.color = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        paint.textSize = editText.textSize
        paint.isAntiAlias = true

        val bounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // Создаём Drawable и реализуем ВСЕ обязательные методы
        val icon = object : android.graphics.drawable.Drawable() {
            override fun draw(canvas: android.graphics.Canvas) {
                canvas.drawText(text, 0f, bounds.height().toFloat(), paint)
            }

            override fun getIntrinsicWidth(): Int = bounds.width()

            override fun getIntrinsicHeight(): Int = bounds.height()

            // Эти три метода обязательно нужно добавить, чтобы убрать ошибку компиляции
            override fun getOpacity(): Int = android.graphics.PixelFormat.OPAQUE

            override fun setAlpha(alpha: Int) {
                // Для простого текста можно игнорировать прозрачность или менять alpha у paint
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                // Если нужен цветовой фильтр — применяем к paint, иначе игнорируем
                paint.colorFilter = colorFilter
            }
        }

        (editText.parent as? com.google.android.material.textfield.TextInputLayout)?.apply {
            endIconDrawable = icon
            endIconContentDescription = text
        }
    }



    private fun loadPrices() {
        fun Double?.format(): String =
            if (this == null || this <= 0) "" else DecimalFormat("0.##").format(this)

        val aisi304 = sharedPreferences.getFloat(KEY_AISI304, 0f).toDouble()
        val aisi430 = sharedPreferences.getFloat(KEY_AISI430, 0f).toDouble()
        val pipe25 = sharedPreferences.getFloat(KEY_PIPE25, 0f).toDouble()
        val pipe40 = sharedPreferences.getFloat(KEY_PIPE40, 0f).toDouble()

        binding.etPriceAisi304.setText(aisi304.format())
        binding.etPriceAisi430.setText(aisi430.format())
        binding.etPricePipe25.setText(pipe25.format())
        binding.etPricePipe40.setText(pipe40.format())
    }

    private fun savePrices() {
        // KTX extension: edit { } автоматически делает commit/apply и безопаснее
        fun String.toPriceOrNull(): Float? =
            if (isEmpty()) null else runCatching { toFloat() }.getOrNull()

        val aisi304 = binding.etPriceAisi304.text.toString().toPriceOrNull()
        val aisi430 = binding.etPriceAisi430.text.toString().toPriceOrNull()
        val pipe25 = binding.etPricePipe25.text.toString().toPriceOrNull()
        val pipe40 = binding.etPricePipe40.text.toString().toPriceOrNull()

        if (aisi304 == null && aisi430 == null && pipe25 == null && pipe40 == null) {
            showStatus("Нет цен для сохранения", false)
            return
        }

        // Используем KTX edit { }
        sharedPreferences.edit {
            aisi304?.let { putFloat(KEY_AISI304, it) }
            aisi430?.let { putFloat(KEY_AISI430, it) }
            pipe25?.let { putFloat(KEY_PIPE25, it) }
            pipe40?.let { putFloat(KEY_PIPE40, it) }
        }

        showStatus("Цены сохранены!", true)
        Toast.makeText(requireContext(), "Цены сохранены", Toast.LENGTH_SHORT).show()
    }

    private fun showStatus(text: String, isSuccess: Boolean) {
        binding.tvStatus.text = text
        val resId = if (isSuccess) R.color.status_success else R.color.status_error
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), resId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
