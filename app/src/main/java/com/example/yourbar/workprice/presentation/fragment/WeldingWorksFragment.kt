package com.example.yourbar.workprice.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourbar.databinding.FragmentWeldingWorksBinding
import com.example.yourbar.workprice.presentation.adapter.WorkPriceListAdapter
import com.example.yourbar.workprice.presentation.viewmodel.WorkPriceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.launch

class WeldingWorksFragment : Fragment() {

    private var _binding: FragmentWeldingWorksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkPriceViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeldingWorksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WorkPriceListAdapter { entity ->
            viewModel.updatePrice(entity)
        }
        binding.rvWeldingWorks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeldingWorks.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weldingPrices.collect { items ->
                    adapter.submitList(items)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
