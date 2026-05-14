package com.namma.santhe.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.namma.santhe.data.db.SantheDatabase
import com.namma.santhe.data.db.entity.CustomerEntity
import com.namma.santhe.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomerDaoTest {

    private lateinit var database: SantheDatabase
    private lateinit var customerDao: CustomerDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            SantheDatabase::class.java
        ).allowMainThreadQueries().build()
        customerDao = database.customerDao()
        transactionDao = database.transactionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveCustomer() = runBlocking {
        val customer = CustomerEntity(name = "Test User", phone = "1234567890")
        val id = customerDao.insertCustomer(customer).toInt()

        val result = customerDao.getCustomerById(id).first()
        assertNotNull(result)
        assertEquals("Test User", result?.name)
        assertEquals("1234567890", result?.phone)
    }

    @Test
    fun searchCustomersByName() = runBlocking {
        customerDao.insertCustomer(CustomerEntity(name = "Raju", phone = "111"))
        customerDao.insertCustomer(CustomerEntity(name = "Rajesh", phone = "222"))
        customerDao.insertCustomer(CustomerEntity(name = "Savitha", phone = "333"))

        val results = customerDao.searchCustomers("Raj").first()
        assertEquals(2, results.size)
        assertTrue(results.all { it.name.contains("Raj") })
    }

    @Test
    fun getTotalDueForCustomer() = runBlocking {
        val customerId = customerDao.insertCustomer(
            CustomerEntity(name = "Test", phone = "000")
        ).toInt()

        // Add UDARI of 500
        transactionDao.insertTransaction(
            TransactionEntity(customerId = customerId, amount = 500.0, type = "UDARI", note = "Test")
        )
        // Add PAYMENT of 200
        transactionDao.insertTransaction(
            TransactionEntity(customerId = customerId, amount = 200.0, type = "PAYMENT", note = "Test")
        )

        val due = customerDao.getTotalDueForCustomer(customerId).first()
        assertEquals(300.0, due, 0.01)
    }

    @Test
    fun deleteCustomerCascadesTransactions() = runBlocking {
        val customer = CustomerEntity(name = "ToDelete", phone = "999")
        val id = customerDao.insertCustomer(customer).toInt()

        transactionDao.insertTransaction(
            TransactionEntity(customerId = id, amount = 100.0, type = "UDARI", note = "Test")
        )

        val customerToDelete = customerDao.getCustomerById(id).first()!!
        customerDao.deleteCustomer(customerToDelete)

        val txns = transactionDao.getTransactionsForCustomer(id).first()
        assertTrue(txns.isEmpty())
    }
}
