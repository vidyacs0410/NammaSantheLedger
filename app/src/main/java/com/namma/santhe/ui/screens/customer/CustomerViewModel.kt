package com.namma.santhe.ui.screens.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namma.santhe.data.model.Customer
import com.namma.santhe.data.model.Transaction
import com.namma.santhe.data.repository.LedgerRepository
import com.namma.santhe.ui.screens.entry.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: LedgerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerId: Int = savedStateHandle.get<Int>("customerId") ?: 0

    val customer: StateFlow<Customer?> = repository.getCustomerById(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<Transaction>> = repository.getCustomerTransactions(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<Double> = repository.getCustomerBalance(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                _uiEvent.emit(UiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.Error(e.message ?: "Failed to delete"))
            }
        }
    }

    fun deleteCustomer() {
        viewModelScope.launch {
            try {
                customer.value?.let {
                    repository.deleteCustomer(it)
                    _uiEvent.emit(UiEvent.Success)
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.Error(e.message ?: "Failed to delete"))
            }
        }
    }
}
