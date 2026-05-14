package com.namma.santhe.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namma.santhe.data.model.DailySummary
import com.namma.santhe.data.repository.LedgerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    repository: LedgerRepository
) : ViewModel() {

    private val _isMonthly = MutableStateFlow(false)
    val isMonthly: StateFlow<Boolean> = _isMonthly

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    val summary: StateFlow<DailySummary> = combine(
        _isMonthly, _selectedDate, _selectedMonth
    ) { isMonthly, date, month ->
        Triple(isMonthly, date, month)
    }.flatMapLatest { (monthly, date, month) ->
        if (monthly) {
            repository.getMonthlySummary(month)
        } else {
            repository.getDailySummary(date)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailySummary()
    )

    fun setMonthly(monthly: Boolean) {
        _isMonthly.value = monthly
    }

    fun previousDate() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDate() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }
}
