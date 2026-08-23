package com.example.yourbar.premium.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yourbar.databinding.FragmentPremiumCalculatorBinding

class PremiumCalculatorFragment : Fragment() {
    private var _binding: FragmentPremiumCalculatorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPremiumCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Сюда позже логику калькулятора Бюджет
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}