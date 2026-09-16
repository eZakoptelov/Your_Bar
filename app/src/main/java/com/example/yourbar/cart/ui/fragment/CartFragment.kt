package com.example.yourbar.cart.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
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

    private var markupPercent = 0.0

    companion object {
        private const val PREF_NAME = "prices_settings"
        private const val KEY_AISI304 = "price_aisi304"
        private const val KEY_AISI430 = "price_aisi430"
        private const val KEY_PIPE25 = "price_pipe25"
        private const val KEY_PIPE40 = "price_pipe40"
        private const val KEY_INSULATION = "price_insulation"
        private const val KEY_FAUCET_HOLE = "price_faucet_hole"
        private const val KEY_BACK_BOARD = "price_back_board"
        private const val KEY_ADJUSTABLE_LEG = "price_adjustable_leg"
        private const val KEY_SINK_400x400 = "price_sink_400x400"
        private const val KEY_SINK_400x500 = "price_sink_400x500"
        private const val KEY_SINK_500x500 = "price_sink_500x500"
        private const val KEY_SINK_500x400 = "price_sink_500x400"
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
            },
            getSinkPrice = { item ->
                when (item.solidSinkType) {
                    "SINK_400x400" -> getPrices().sink400x400
                    "SINK_400x500" -> getPrices().sink400x500
                    "SINK_500x500" -> getPrices().sink500x500
                    "SINK_500x400" -> getPrices().sink500x400
                    else -> 0.0
                }
            }
        )

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        binding.etMarkupPercent.doAfterTextChanged { editable ->
            val text = editable?.toString()?.trim() ?: ""
            markupPercent = if (text.isEmpty()) 0.0 else text.toDoubleOrNull() ?: 0.0
            updateTotal(cartRepository.items.value)
        }

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

    private fun getPrices(): Prices {
        return Prices(
            aisi304 = sharedPreferences.getFloat(KEY_AISI304, 0f).toDouble(),
            aisi430 = sharedPreferences.getFloat(KEY_AISI430, 0f).toDouble(),
            pipe25 = sharedPreferences.getFloat(KEY_PIPE25, 0f).toDouble(),
            pipe40 = sharedPreferences.getFloat(KEY_PIPE40, 0f).toDouble(),
            insulation = sharedPreferences.getFloat(KEY_INSULATION, 0f).toDouble(),
            faucetHole = sharedPreferences.getFloat(KEY_FAUCET_HOLE, 0f).toDouble(),
            backBoard = sharedPreferences.getFloat(KEY_BACK_BOARD, 0f).toDouble(),
            adjustableLeg = sharedPreferences.getFloat(KEY_ADJUSTABLE_LEG, 0f).toDouble(),
            sink400x400 = sharedPreferences.getFloat(KEY_SINK_400x400, 0f).toDouble(),
            sink400x500 = sharedPreferences.getFloat(KEY_SINK_400x500, 0f).toDouble(),
            sink500x500 = sharedPreferences.getFloat(KEY_SINK_500x500, 0f).toDouble(),
            sink500x400 = sharedPreferences.getFloat(KEY_SINK_500x400, 0f).toDouble()
        )
    }


    private fun calcItemPrice(item: CartItem, prices: Prices): Double {
        val solidSinkPrice = when (item.solidSinkType) {
            "SINK_400x400" -> prices.sink400x400
            "SINK_400x500" -> prices.sink400x500
            "SINK_500x500" -> prices.sink500x500
            "SINK_500x400" -> prices.sink500x400
            else -> 0.0
        }

        return item.weightAisi304Kg * prices.aisi304 +
                item.weightAisi430Kg * prices.aisi430 +
                item.pipeMeters * prices.pipe25 +
                item.insulationAreaSqM * prices.insulation +
                item.faucetHoleCount * prices.faucetHole +
                item.backBoardCount * prices.backBoard +
                item.adjustableLegCount * prices.adjustableLeg +
                solidSinkPrice
    }



    private fun updateTotal(items: List<CartItem>) {
        val prices = getPrices()
        val basePrice = items.sumOf { calcItemPrice(it, prices) }
        val finalPrice = basePrice * (1.0 + markupPercent / 100.0)

        binding.tvCartPrice.text = if (markupPercent > 0) {
            "Стоимость: ${"%.0f".format(finalPrice)} ₽ (+${"%.0f".format(markupPercent)}%)"
        } else {
            "Стоимость: ${"%.0f".format(finalPrice)} ₽"
        }
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
    val pipe40: Double,
    val insulation: Double,
    val faucetHole: Double,
    val backBoard: Double,
    val adjustableLeg: Double,
    val sink400x400: Double,
    val sink400x500: Double,
    val sink500x500: Double,
    val sink500x400: Double
)

