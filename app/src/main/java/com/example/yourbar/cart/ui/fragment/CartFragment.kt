package com.example.yourbar.cart.ui.fragment

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
import com.example.yourbar.budget.data.price.PriceRepository
import com.example.yourbar.cart.data.repository.CartRepository
import com.example.yourbar.cart.domain.CartItem
import com.example.yourbar.cart.ui.adapter.CartAdapter
import com.example.yourbar.databinding.FragmentCartBinding
import com.example.yourbar.workprice.data.WorkPriceRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartRepository: CartRepository by inject()
    private val priceRepository: PriceRepository by inject()
    private val workPriceRepository: WorkPriceRepository by inject()
    private lateinit var adapter: CartAdapter

    private val isAdmin: Boolean
        get() = activity?.intent?.getBooleanExtra("is_admin", false) ?: false

    private var markupPercent = 0.0
    private var weldingPrices: Map<String, Double> = emptyMap()
    private var locksmithPrices: Map<String, Double> = emptyMap()

    private var currentItems: List<CartItem> = emptyList()

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

        adapter = CartAdapter(
            isAdmin = isAdmin,
            onRemove = { id ->
                viewLifecycleOwner.lifecycleScope.launch {
                    cartRepository.remove(id)
                }
            },
            onClick = { item ->
                val bundle = Bundle().apply {
                    putParcelable("cart_item", item)
                }
                findNavController().navigate(R.id.stationDetailsFragment, bundle)
            },
            getSinkPrice = { item ->
                val prices = getPrices()
                when (item.solidSinkType) {
                    "SINK_400x400" -> prices.sink400x400
                    "SINK_400x500" -> prices.sink400x500
                    "SINK_500x500" -> prices.sink500x500
                    "SINK_500x400" -> prices.sink500x400
                    null -> 0.0
                    else -> 0.0
                }
            }
        )

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        // ── Надбавка и итоговая цена доступны всем ──
        binding.etMarkupPercent.visibility = View.VISIBLE
        binding.tvCartPrice.visibility = View.VISIBLE

        binding.etMarkupPercent.doAfterTextChanged { editable ->
            val text = editable?.toString()?.trim() ?: ""
            markupPercent = if (text.isEmpty()) 0.0 else text.toDoubleOrNull() ?: 0.0
            updateTotal(currentItems)
        }

        binding.btnClearCart.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                cartRepository.clear()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartRepository.items.collect { items ->
                    currentItems = items
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                workPriceRepository.observeWelding().collect { entities ->
                    weldingPrices = entities.associate { it.title to it.price.toDouble() }
                    updateTotal(currentItems)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                workPriceRepository.observeLocksmith().collect { entities ->
                    locksmithPrices = entities.associate { it.title to it.price.toDouble() }
                    updateTotal(currentItems)
                }
            }
        }
    }

    private fun getPrices(): Prices {
        val p = priceRepository.load()
        return Prices(
            aisi304 = p.aisi304PerKg,
            aisi430 = p.aisi430PerKg,
            pipe25 = p.pipe25PerM,
            pipe40 = p.pipe40PerM,
            insulation = p.insulationPerM2,
            plywood = p.plywoodPerM2,
            adjustableLeg = p.adjustableLegPerPiece,
            sink400x400 = p.sink400x400PerPiece,
            sink400x500 = p.sink400x500PerPiece,
            sink500x500 = p.sink500x500PerPiece,
            sink500x400 = p.sink500x400PerPiece
        )
    }

    private fun calcWeldingCost(item: CartItem): Double {
        val weldCountertop = weldingPrices["Сварка столешницы"] ?: 0.0
        val weldFrame = weldingPrices["Сварка каркаса из 25 трубы"] ?: 0.0
        val weldSinkBody = weldingPrices["Сварка корпуса мойки"] ?: 0.0
        val weldPocket = weldingPrices["Сварка кармана для бутылок"] ?: 0.0
        val weldSolidSink = weldingPrices["Сварка цельнотянутой мойки"] ?: 0.0
        val weldBlenderShelf = weldingPrices["Сварка полки для блендера"] ?: 0.0

        return weldCountertop +
                weldFrame +
                weldSinkBody +
                (if (item.pocketsCount > 0) weldPocket * item.pocketsCount else 0.0) +
                (if (item.solidSinkType != "NONE" && item.solidSinkType != null) weldSolidSink else 0.0) +
                (if (item.isBlenderShelfAdded) weldBlenderShelf else 0.0)
    }

    private fun calcLocksmithCost(item: CartItem): Double {
        val lockCountertop = locksmithPrices["Изготовление столешницы"] ?: 0.0
        val lockWeldedSink = locksmithPrices["Изготовление сварной мойки"] ?: 0.0
        val lockPerforatedInsert = locksmithPrices["Изготовление перфорированной вставки"] ?: 0.0
        val lockRemovablePartitions = locksmithPrices["Изготовление съёмных перегородок"] ?: 0.0
        val lockGlueBase = locksmithPrices["Проклейка основания столешницы(фанера)"] ?: 0.0
        val lockFaucetHole = locksmithPrices["Отверстие под смеситель"] ?: 0.0
        val lockBackBoard = locksmithPrices["Изготовление борта"] ?: 0.0
        val lockBlenderShelf = locksmithPrices["Изготовление полки для блендера"] ?: 0.0
        val lockPocket = locksmithPrices["Изготовление кармана для бутылок"] ?: 0.0

        return lockCountertop +
                lockWeldedSink +
                lockPerforatedInsert +
                lockRemovablePartitions +
                lockGlueBase +
                (if (item.faucetHoleCount > 0) lockFaucetHole * item.faucetHoleCount else 0.0) +
                (if (item.backBoardCount > 0) lockBackBoard * item.backBoardCount else 0.0) +
                (if (item.isBlenderShelfAdded) lockBlenderShelf else 0.0) +
                (if (item.pocketsCount > 0) lockPocket * item.pocketsCount else 0.0)
    }

    private fun calcItemPrice(item: CartItem, prices: Prices): Double {
        val solidSinkPrice = when (item.solidSinkType) {
            "SINK_400x400" -> prices.sink400x400
            "SINK_400x500" -> prices.sink400x500
            "SINK_500x500" -> prices.sink500x500
            "SINK_500x400" -> prices.sink500x400
            null -> 0.0
            else -> 0.0
        }

        val plywoodAreaSqM = (item.widthMm * item.depthMm) / 1_000_000.0

        val materialPrice = item.weightAisi304Kg * prices.aisi304 +
                item.weightAisi430Kg * prices.aisi430 +
                item.pipeMeters * prices.pipe25 +
                item.insulationAreaSqM * prices.insulation +
                plywoodAreaSqM * prices.plywood +
                item.adjustableLegCount * prices.adjustableLeg +
                solidSinkPrice

        return materialPrice + calcWeldingCost(item) + calcLocksmithCost(item)
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
    val plywood: Double,
    val adjustableLeg: Double,
    val sink400x400: Double,
    val sink400x500: Double,
    val sink500x500: Double,
    val sink500x400: Double
)
