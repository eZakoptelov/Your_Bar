package com.example.yourbar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.yourbar.databinding.FragmentAssemblyBinding
import com.example.yourbar.ui.assembly.BudgetFragment
import com.example.yourbar.ui.assembly.ExclusiveFragment
import com.example.yourbar.ui.assembly.PerfectumFragment
import com.example.yourbar.ui.assembly.PremiumFragment

class AssemblyFragment : Fragment() {

    private var _binding: FragmentAssemblyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssemblyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager: ViewPager2 = binding.viewPager
        val tabLayout = binding.tabLayout

        // Адаптер для ViewPager2
        val adapter = AssemblyViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Связываем TabLayout и ViewPager2
        com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Бюджет"
                1 -> tab.text = "Premium"
                2 -> tab.text = "Perfectum"
                3 -> tab.text = "Exclusive"
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Адаптер ViewPager2 (FragmentStateAdapter)
class AssemblyViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> BudgetFragment()
        1 -> PremiumFragment()
        2 -> PerfectumFragment()
        3 -> ExclusiveFragment()
        else -> throw IllegalArgumentException("Invalid position")
    }
}
