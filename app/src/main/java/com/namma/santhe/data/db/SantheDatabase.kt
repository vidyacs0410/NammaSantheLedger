package com.namma.santhe.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.namma.santhe.data.db.dao.CustomerDao
import com.namma.santhe.data.db.dao.TransactionDao
import com.namma.santhe.data.db.dao.UserDao
import com.namma.santhe.data.db.entity.CustomerEntity
import com.namma.santhe.data.db.entity.TransactionEntity
import com.namma.santhe.data.db.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CustomerEntity::class, TransactionEntity::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SantheDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao

    class SeedCallback(
        private val provider: () -> SantheDatabase
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = provider()
                val customerDao = database.customerDao()
                val transactionDao = database.transactionDao()

                // Seed customers
                val rajuId = customerDao.insertCustomer(
                    CustomerEntity(name = "Raju", phone = "9876543210")
                ).toInt()
                val savithaId = customerDao.insertCustomer(
                    CustomerEntity(name = "Savitha", phone = "9123456789")
                ).toInt()
                val manjunathId = customerDao.insertCustomer(
                    CustomerEntity(name = "Manjunath", phone = "9988776655")
                ).toInt()

                val now = System.currentTimeMillis()
                val oneHourAgo = now - 3_600_000L
                val twoHoursAgo = now - 7_200_000L

                // Seed transactions — mix of UDARI and PAYMENT
                transactionDao.insertTransaction(
                    TransactionEntity(
                        customerId = rajuId,
                        amount = 250.0,
                        type = "UDARI",
                        note = "Vegetables",
                        date = twoHoursAgo
                    )
                )
                transactionDao.insertTransaction(
                    TransactionEntity(
                        customerId = rajuId,
                        amount = 100.0,
                        type = "PAYMENT",
                        note = "Partial payment",
                        date = oneHourAgo
                    )
                )
                transactionDao.insertTransaction(
                    TransactionEntity(
                        customerId = savithaId,
                        amount = 500.0,
                        type = "UDARI",
                        note = "Bangles",
                        date = twoHoursAgo
                    )
                )
                transactionDao.insertTransaction(
                    TransactionEntity(
                        customerId = savithaId,
                        amount = 200.0,
                        type = "PAYMENT",
                        note = "Cash payment",
                        date = now
                    )
                )
                transactionDao.insertTransaction(
                    TransactionEntity(
                        customerId = manjunathId,
                        amount = 350.0,
                        type = "UDARI",
                        note = "Fruits",
                        date = oneHourAgo
                    )
                )
                transactionDao.insertTransaction(
                    TransactionEntity(
                        customerId = manjunathId,
                        amount = 350.0,
                        type = "PAYMENT",
                        note = "Full payment",
                        date = now
                    )
                )
            }
        }
    }
}
