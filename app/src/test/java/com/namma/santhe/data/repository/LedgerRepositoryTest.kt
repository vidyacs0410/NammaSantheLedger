package com.namma.santhe.data.repository

import com.namma.santhe.data.db.dao.CustomerDao
import com.namma.santhe.data.db.dao.TransactionDao
import com.namma.santhe.data.db.entity.CustomerEntity
import com.namma.santhe.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LedgerRepositoryTest {

    private lateinit var fakeCustomerDao: FakeCustomerDao
    private lateinit var fakeTransactionDao: FakeTransactionDao
    private lateinit var repository: LedgerRepository

    @Before
    fun setup() {
        fakeCustomerDao = FakeCustomerDao()
        fakeTransactionDao = FakeTransactionDao()
        repository = LedgerRepository(fakeCustomerDao, fakeTransactionDao)
    }

    @Test
    fun `addCustomer inserts and returns id`() = runTest {
        val id = repository.addCustomer("Raju", "1234567890")
        assertEquals(1L, id)

        val customers = repository.getAllCustomers().first()
        assertEquals(1, customers.size)
        assertEquals("Raju", customers[0].name)
    }

    @Test
    fun `addUdari creates UDARI transaction`() = runTest {
        val customerId = repository.addCustomer("Test", "000").toInt()
        repository.addUdari(customerId, 250.0, "Vegetables")

        val txns = repository.getCustomerTransactions(customerId).first()
        assertEquals(1, txns.size)
        assertEquals("UDARI", txns[0].type)
        assertEquals(250.0, txns[0].amount, 0.01)
    }

    @Test
    fun `recordPayment creates PAYMENT transaction`() = runTest {
        val customerId = repository.addCustomer("Test", "000").toInt()
        repository.recordPayment(customerId, 100.0, "Cash")

        val txns = repository.getCustomerTransactions(customerId).first()
        assertEquals(1, txns.size)
        assertEquals("PAYMENT", txns[0].type)
    }

    @Test
    fun `getCustomerBalance returns correct due`() = runTest {
        val customerId = repository.addCustomer("Test", "000").toInt()
        repository.addUdari(customerId, 500.0, "")
        repository.recordPayment(customerId, 200.0, "")

        val balance = repository.getCustomerBalance(customerId).first()
        assertEquals(300.0, balance, 0.01)
    }
}

// ── Fake DAOs for unit testing ──

class FakeCustomerDao : CustomerDao {
    private val customers = mutableListOf<CustomerEntity>()
    private var nextId = 1

    private val flow = MutableStateFlow<List<CustomerEntity>>(emptyList())

    override fun getAllCustomers(): Flow<List<CustomerEntity>> = flow

    override fun searchCustomers(query: String): Flow<List<CustomerEntity>> =
        flow.map { list -> list.filter { it.name.contains(query, ignoreCase = true) } }

    override fun getCustomerById(id: Int): Flow<CustomerEntity?> =
        flow.map { list -> list.find { it.id == id } }

    override suspend fun insertCustomer(customer: CustomerEntity): Long {
        val id = nextId++
        val entity = customer.copy(id = id)
        customers.add(entity)
        flow.value = customers.toList()
        return id.toLong()
    }

    override suspend fun updateCustomer(customer: CustomerEntity) {
        val index = customers.indexOfFirst { it.id == customer.id }
        if (index >= 0) {
            customers[index] = customer
            flow.value = customers.toList()
        }
    }

    override suspend fun deleteCustomer(customer: CustomerEntity) {
        customers.removeAll { it.id == customer.id }
        flow.value = customers.toList()
    }

    override fun getTotalDueForCustomer(customerId: Int): Flow<Double> =
        flowOf(0.0) // Will be computed via FakeTransactionDao
}

class FakeTransactionDao : TransactionDao {
    private val transactions = mutableListOf<TransactionEntity>()
    private var nextId = 1

    private val flow = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override fun getTransactionsForCustomer(customerId: Int): Flow<List<TransactionEntity>> =
        flow.map { list -> list.filter { it.customerId == customerId } }

    override suspend fun insertTransaction(txn: TransactionEntity): Long {
        val id = nextId++
        transactions.add(txn.copy(id = id))
        flow.value = transactions.toList()
        return id.toLong()
    }

    override suspend fun deleteTransaction(txn: TransactionEntity) {
        transactions.removeAll { it.id == txn.id }
        flow.value = transactions.toList()
    }

    override fun getTodayTotalSales(startOfDay: Long): Flow<Double> =
        flow.map { list ->
            list.filter { it.type == "UDARI" && it.date >= startOfDay }.sumOf { it.amount }
        }

    override fun getTodayTotalCollected(startOfDay: Long): Flow<Double> =
        flow.map { list ->
            list.filter { it.type == "PAYMENT" && it.date >= startOfDay }.sumOf { it.amount }
        }

    override fun getGlobalTotalDue(): Flow<Double> =
        flow.map { list ->
            list.sumOf { if (it.type == "UDARI") it.amount else -it.amount }
        }
}
