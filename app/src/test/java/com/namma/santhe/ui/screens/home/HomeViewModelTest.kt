package com.namma.santhe.ui.screens.home

import com.namma.santhe.data.model.Customer
import com.namma.santhe.data.model.DailySummary
import com.namma.santhe.data.model.Transaction
import com.namma.santhe.data.repository.LedgerRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: LedgerRepository
    private lateinit var viewModel: HomeViewModel

    private val customersFlow = MutableStateFlow(
        listOf(
            Customer(1, "Raju", "111"),
            Customer(2, "Savitha", "222"),
            Customer(3, "Manjunath", "333")
        )
    )
    private val totalDueFlow = MutableStateFlow(450.0)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk(relaxed = true)
        every { repository.getAllCustomers() } returns customersFlow
        every { repository.getGlobalTotalDue() } returns totalDueFlow
        every { repository.getCustomerBalance(any()) } returns MutableStateFlow(0.0)

        viewModel = HomeViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has all customers`() = runTest {
        val customers = viewModel.allCustomers.first()
        assertEquals(3, customers.size)
    }

    @Test
    fun `search filters customers by name`() = runTest {
        viewModel.onSearchQueryChange("Raj")
        advanceUntilIdle()

        val filtered = viewModel.allCustomers.value
        assertEquals(1, filtered.size)
        assertEquals("Raju", filtered[0].name)
    }

    @Test
    fun `empty search shows all customers`() = runTest {
        viewModel.onSearchQueryChange("Raj")
        advanceUntilIdle()
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        val all = viewModel.allCustomers.value
        assertEquals(3, all.size)
    }

    @Test
    fun `total outstanding reflects global due`() = runTest {
        val outstanding = viewModel.totalOutstanding.value
        assertEquals(450.0, outstanding, 0.01)
    }
}
