package com.example.yourbar.workprice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourbar.workprice.data.WorkPriceEntity
import com.example.yourbar.workprice.data.WorkPriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WorkPriceViewModel(
    private val repository: WorkPriceRepository
) : ViewModel() {

    val weldingPrices: Flow<List<WorkPriceEntity>> = repository.observeWelding()
    val locksmithPrices: Flow<List<WorkPriceEntity>> = repository.observeLocksmith()

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }

    fun updatePrice(entity: WorkPriceEntity) {
        viewModelScope.launch {
            repository.updatePrice(entity)
        }
    }
}
