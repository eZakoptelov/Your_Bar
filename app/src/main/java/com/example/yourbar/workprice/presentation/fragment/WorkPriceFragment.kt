package com.example.yourbar.workprice.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yourbar.databinding.FragmentWorkPriceBinding
import com.example.yourbar.workprice.presentation.adapter.WorkPricePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class WorkPriceFragment : Fragment() {

    private var _binding: FragmentWorkPriceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = WorkPricePagerAdapter(this)
        binding.viewPagerWorkPrice.adapter = pagerAdapter

        TabLayoutMediator(
            binding.tabLayoutWorkPrice,
            binding.viewPagerWorkPrice
        ) { tab, position ->
            tab.text = when (position) {
                0 -> "Сварочные работы"
                1 -> "Слесарные работы"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
