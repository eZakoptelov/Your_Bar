package com.example.yourbar.workprice.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yourbar.workprice.presentation.fragment.LocksmithWorksFragment
import com.example.yourbar.workprice.presentation.fragment.WeldingWorksFragment

class WorkPricePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WeldingWorksFragment()
            1 -> LocksmithWorksFragment()
            else -> throw IllegalArgumentException("Unknown position $position")
        }
    }
}