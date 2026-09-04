package com.example.yourbar.budget.presentation.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourbar.R
import com.example.yourbar.budget.domain.calculator.models.SteelType
import com.example.yourbar.budget.domain.calculator.usecase.GetStationDetailsUseCase
import com.example.yourbar.cart.domain.CartItem
import com.example.yourbar.databinding.FragmentStationDetailsBinding
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class StationDetailsFragment : Fragment() {

    private var _binding: FragmentStationDetailsBinding? = null
    private val binding get() = _binding!!

    private val getDetailsUseCase: GetStationDetailsUseCase by inject()
    private val adapter = StationPartsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- СКРЫВАЕМ BOTTOM NAV ---
        val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
        bottomNav.visibility = View.GONE
        // -------------------------

        binding.rvParts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvParts.adapter = adapter

        val item = arguments?.getParcelable<CartItem>("cart_item")
            ?: run { requireActivity().onBackPressedDispatcher.onBackPressed(); return }

        showDetails(item)
    }

    private fun showDetails(item: CartItem) {
        val df = DecimalFormat("0.##")

        binding.tvStationName.text = item.displayName
        binding.tvStationDimensions.text =
            "Габариты: ${item.widthMm}×${item.depthMm}×${item.heightMm} мм"
        binding.tvStationTotalWeight.text = "Общий вес: ${df.format(item.totalWeightKg)} кг"
        binding.tvStationPipe.text = "Труба 25×25: ${df.format(item.pipeMeters)} мп"

        val steelType = when (item.steelType) {
            "AISI 304", "AISI_304" -> SteelType.AISI_304
            else -> SteelType.AISI_430
        }

        val additionalPocketsCount = (item.pocketsCount - 1).coerceAtLeast(0)

        val parts = getDetailsUseCase.execute(
            widthMm = item.widthMm,
            depthMm = item.depthMm,
            steelType = steelType,
            thicknessMm = item.thicknessMm,
            additionalPocketsCount = additionalPocketsCount
        )

        adapter.submitList(parts)
        binding.tvEmptyState.visibility = if (parts.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // --- ВОЗВРАЩАЕМ BOTTOM NAV ---
        val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            R.id.bottom_nav)
        bottomNav.visibility = View.VISIBLE
        // -----------------------------
        _binding = null
    }
}
