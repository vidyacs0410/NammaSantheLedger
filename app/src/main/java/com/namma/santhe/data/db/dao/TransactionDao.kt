package com.namma.santhe.data.db.dao

import androidx.room.*
import com.namma.santhe.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getTransactionsForCustomer(customerId: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(txn: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(txn: TransactionEntity)

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'UDARI' AND date >= :startOfDay
        """
    )
    fun getTodayTotalSales(startOfDay: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'PAYMENT' AND date >= :startOfDay
        """
    )
    fun getTodayTotalCollected(startOfDay: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'UDARI' AND date >= :startTime AND date <= :endTime
        """
    )
    fun getTotalSalesBetween(startTime: Long, endTime: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'PAYMENT' AND date >= :startTime AND date <= :endTime
        """
    )
    fun getTotalCollectedBetween(startTime: Long, endTime: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN type='UDARI' THEN amount ELSE -amount END), 0)
        FROM transactions
        """
    )
    fun getGlobalTotalDue(): Flow<Double>
}
