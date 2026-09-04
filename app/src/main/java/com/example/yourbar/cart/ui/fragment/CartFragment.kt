package com.example.yourbar.cart.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourbar.R
import com.example.yourbar.cart.data.CartRepository
import com.example.yourbar.cart.domain.CartItem
import com.example.yourbar.cart.ui.adapter.CartAdapter
import com.example.yourbar.databinding.FragmentCartBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartRepository: CartRepository by inject()
    private lateinit var adapter: CartAdapter

    private lateinit var sharedPreferences: android.content.SharedPreferences

    companion object {
        private const val PREF_NAME = "prices_settings"
        private const val KEY_AISI304 = "price_aisi304"
        private const val KEY_AISI430 = "price_aisi430"
        private const val KEY_PIPE25 = "price_pipe25"
        private const val KEY_PIPE40 = "price_pipe40"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        adapter = CartAdapter(
            onRemove = { id -> cartRepository.remove(id) },
            onClick = { item ->
                val bundle = Bundle().apply {
                    putParcelable("cart_item", item)
                }
                findNavController().navigate(R.id.stationDetailsFragment, bundle)
            }
        )

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        binding.btnClearCart.setOnClickListener {
            cartRepository.clear()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartRepository.items.collect { items ->
                    adapter.submitList(items)
                    updateTotal(items)
                    binding.tvEmptyCart.visibility =
                        if (items.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvCart.visibility =
                        if (items.isEmpty()) View.GONE else View.VISIBLE
                    binding.btnClearCart.isEnabled = items.isNotEmpty()
                }
            }
        }
    }

    // ── Чтение цен из SharedPreferences ──
    private fun getPrices(): Prices {
        return Prices(
            aisi304 = sharedPreferences.getFloat(KEY_AISI304, 0f).toDouble(),
            aisi430 = sharedPreferences.getFloat(KEY_AISI430, 0f).toDouble(),
            pipe25 = sharedPreferences.getFloat(KEY_PIPE25, 0f).toDouble(),
            pipe40 = sharedPreferences.getFloat(KEY_PIPE40, 0f).toDouble()
        )
    }

    // ── Расчёт цены одной станции ──
    private fun calcItemPrice(item: CartItem, prices: Prices): Double {
        return item.weightAisi304Kg * prices.aisi304 +
                item.weightAisi430Kg * prices.aisi430 +
                item.pipeMeters * prices.pipe25
        // pipe40 пока не используется — нет метража в CartItem
    }

    // ── Итог по корзине ──
    private fun updateTotal(items: List<CartItem>) {
        val prices = getPrices()
        val total304 = items.sumOf { it.weightAisi304Kg }
        val total430 = items.sumOf { it.weightAisi430Kg }
        val totalPipe = items.sumOf { it.pipeMeters }
        val totalPrice = items.sumOf { calcItemPrice(it, prices) }

        // Веса и труба — в tvCartTotal
        binding.tvCartTotal.text = buildString {
            append("AISI 430: ${"%.1f".format(total430)} кг")
            append("  |  AISI 304: ${"%.1f".format(total304)} кг")
            append("  |  Труба 25×25: ${"%.1f".format(totalPipe)} мп")
        }

        // Цена — в tvCartPrice (отдельная строка, крупнее)
        binding.tvCartPrice.text = "Стоимость: ${"%.0f".format(totalPrice)} ₽"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Prices(
    val aisi304: Double,
    val aisi430: Double,
    val pipe25: Double,
    val pipe40: Double
)
