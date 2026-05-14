package com.namma.santhe.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namma.santhe.data.model.Customer
import com.namma.santhe.data.repository.LedgerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.namma.santhe.data.SessionManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val currentLanguage = sessionManager.languageFlow

    val searchQuery = MutableStateFlow("")

    private val _allCustomers = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = combine(
        _allCustomers,
        searchQuery
    ) { customers, query ->
        if (query.isBlank()) {
            customers
        } else {
            customers.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOutstanding: StateFlow<Double> = repository.getGlobalTotalDue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Map of customerId -> due amount for display in cards
    private val _customerDues = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val customerDues: StateFlow<Map<Int, Double>> = _customerDues.asStateFlow()

    init {
        viewModelScope.launch {
            _allCustomers.collect { customers ->
                customers.forEach { customer ->
                    launch {
                        repository.getCustomerBalance(customer.id).collect { due ->
                            _customerDues.update { map ->
                                map + (customer.id to due)
                            }
                        }
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }
}
