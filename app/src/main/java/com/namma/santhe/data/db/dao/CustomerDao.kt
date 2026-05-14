package com.namma.santhe.data.db.dao

import androidx.room.*
import com.namma.santhe.data.db.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Int): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN type='UDARI' THEN amount ELSE -amount END), 0)
        FROM transactions WHERE customerId = :customerId
        """
    )
    fun getTotalDueForCustomer(customerId: Int): Flow<Double>
}
