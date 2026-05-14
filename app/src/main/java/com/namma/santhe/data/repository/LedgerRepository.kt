package com.namma.santhe.data.repository

import com.namma.santhe.data.db.dao.CustomerDao
import com.namma.santhe.data.db.dao.TransactionDao
import com.namma.santhe.data.db.entity.CustomerEntity
import com.namma.santhe.data.db.entity.TransactionEntity
import com.namma.santhe.data.model.Customer
import com.namma.santhe.data.model.DailySummary
import com.namma.santhe.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LedgerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao
) {

    // ── Customer operations ──

    suspend fun addCustomer(name: String, phone: String): Long {
        return customerDao.insertCustomer(
            CustomerEntity(name = name, phone = phone)
        )
    }

    fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.searchCustomers(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getCustomerById(id: Int): Flow<Customer?> {
        return customerDao.getCustomerById(id).map { it?.toDomain() }
    }

    fun getCustomerBalance(customerId: Int): Flow<Double> {
        return customerDao.getTotalDueForCustomer(customerId)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer.toEntity())
    }

    // ── Transaction operations ──

    suspend fun addUdari(customerId: Int, amount: Double, note: String) {
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = customerId,
                amount = amount,
                type = "UDARI",
                note = note
            )
        )
    }

    suspend fun recordPayment(customerId: Int, amount: Double, note: String) {
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = customerId,
                amount = amount,
                type = "PAYMENT",
                note = note
            )
        )
    }

    fun getCustomerTransactions(customerId: Int): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForCustomer(customerId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    // ── Summary operations ──

    fun getGlobalTotalDue(): Flow<Double> {
        return transactionDao.getGlobalTotalDue()
    }

    fun getDailySummary(date: LocalDate): Flow<DailySummary> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1

        return combine(
            transactionDao.getTotalSalesBetween(startOfDay, endOfDay),
            transactionDao.getTotalCollectedBetween(startOfDay, endOfDay)
        ) { sales, collected ->
            DailySummary(
                totalSales = sales,
                totalCollected = collected,
                totalPending = sales - collected
            )
        }
    }

    fun getMonthlySummary(yearMonth: YearMonth): Flow<DailySummary> {
        val startOfMonth = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val endOfMonth = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1

        return combine(
            transactionDao.getTotalSalesBetween(startOfMonth, endOfMonth),
            transactionDao.getTotalCollectedBetween(startOfMonth, endOfMonth)
        ) { sales, collected ->
            DailySummary(
                totalSales = sales,
                totalCollected = collected,
                totalPending = sales - collected
            )
        }
    }

    // ── Mappers ──

    private fun CustomerEntity.toDomain() = Customer(
        id = id,
        name = name,
        phone = phone,
        createdAt = createdAt
    )

    private fun Customer.toEntity() = CustomerEntity(
        id = id,
        name = name,
        phone = phone,
        createdAt = createdAt
    )

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        customerId = customerId,
        amount = amount,
        type = type,
        note = note,
        date = date
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        customerId = customerId,
        amount = amount,
        type = type,
        note = note,
        date = date
    )
}
