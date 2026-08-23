package com.example.yourbar.exclusive.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yourbar.databinding.FragmentExclusiveCalculatorBinding

class ExclusiveCalculatorFragment: Fragment() {
    private var _binding: FragmentExclusiveCalculatorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExclusiveCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Сюда позже логику калькулятора
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}