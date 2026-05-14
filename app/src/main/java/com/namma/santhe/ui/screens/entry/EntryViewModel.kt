package com.namma.santhe.ui.screens.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namma.santhe.data.model.Customer
import com.namma.santhe.data.repository.LedgerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.namma.santhe.data.SessionManager
import javax.inject.Inject

sealed class UiEvent {
    object Success : UiEvent()
    data class Error(val msg: String) : UiEvent()
}

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val currentLanguage = sessionManager.languageFlow

    private val preSelectedCustomerId: Int = savedStateHandle.get<Int>("customerId") ?: -1

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    val entryAmount = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val customers: StateFlow<List<Customer>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getAllCustomers()
        } else {
            repository.searchCustomers(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customer dues map
    private val _customerDues = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val customerDues: StateFlow<Map<Int, Double>> = _customerDues.asStateFlow()

    init {
        if (preSelectedCustomerId > 0) {
            viewModelScope.launch {
                repository.getCustomerById(preSelectedCustomerId).firstOrNull()?.let {
                    _selectedCustomer.value = it
                }
            }
        }

        // Collect dues for all customers
        viewModelScope.launch {
            customers.collect { customerList ->
                customerList.forEach { customer ->
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
        _searchQuery.value = query
    }

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
    }

    fun clearCustomer() {
        _selectedCustomer.value = null
        entryAmount.value = ""
    }

    fun appendDigit(digit: String) {
        val current = entryAmount.value
        // Prevent multiple dots
        if (digit == "." && current.contains(".")) return
        // Limit decimal places to 2
        if (current.contains(".")) {
            val decimals = current.substringAfter(".")
            if (decimals.length >= 2) return
        }
        // Prevent leading zeros (except "0.")
        if (current == "0" && digit != ".") {
            entryAmount.value = digit
            return
        }
        entryAmount.value = current + digit
    }

    fun clearLast() {
        val current = entryAmount.value
        if (current.isNotEmpty()) {
            entryAmount.value = current.dropLast(1)
        }
    }

    fun submitUdari(note: String) {
        val amount = entryAmount.value.toDoubleOrNull()
        val customer = _selectedCustomer.value
        if (amount == null || amount <= 0.0) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.Error("Enter a valid amount"))
            }
            return
        }
        if (customer == null) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.Error("Select a customer"))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addUdari(customer.id, amount, note)
                _uiEvent.emit(UiEvent.Success)
                entryAmount.value = ""
                _selectedCustomer.value = null
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.Error(e.message ?: "Something went wrong"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitPayment(note: String) {
        val amount = entryAmount.value.toDoubleOrNull()
        val customer = _selectedCustomer.value
        if (amount == null || amount <= 0.0) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.Error("Enter a valid amount"))
            }
            return
        }
        if (customer == null) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.Error("Select a customer"))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.recordPayment(customer.id, amount, note)
                _uiEvent.emit(UiEvent.Success)
                entryAmount.value = ""
                _selectedCustomer.value = null
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.Error(e.message ?: "Something went wrong"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun addNewCustomer(name: String, phone: String): Customer? {
        return try {
            val id = repository.addCustomer(name, phone).toInt()
            val customer = Customer(id = id, name = name, phone = phone)
            _selectedCustomer.value = customer
            customer
        } catch (e: Exception) {
            _uiEvent.emit(UiEvent.Error(e.message ?: "Failed to add customer"))
            null
        }
    }
}
