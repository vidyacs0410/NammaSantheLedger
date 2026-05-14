package com.namma.santhe.di

import android.content.Context
import androidx.room.Room
import com.namma.santhe.data.db.SantheDatabase
import com.namma.santhe.data.db.dao.CustomerDao
import com.namma.santhe.data.db.dao.TransactionDao
import com.namma.santhe.data.db.dao.UserDao
import com.namma.santhe.data.repository.AuthRepository
import com.namma.santhe.data.repository.LedgerRepository
import com.namma.santhe.data.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SantheDatabase {
        lateinit var database: SantheDatabase
        database = Room.databaseBuilder(
            context,
            SantheDatabase::class.java,
            "santhe_ledger.db"
        )
            .addCallback(SantheDatabase.SeedCallback { database })
            .fallbackToDestructiveMigration()
            .build()
        return database
    }

    @Provides
    fun provideCustomerDao(db: SantheDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideTransactionDao(db: SantheDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideRepository(
        customerDao: CustomerDao,
        transactionDao: TransactionDao
    ): LedgerRepository = LedgerRepository(customerDao, transactionDao)

    @Provides
    fun provideUserDao(db: SantheDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideAuthRepository(userDao: UserDao): AuthRepository = AuthRepository(userDao)

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager = SessionManager(context)

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): com.namma.santhe.util.NotificationHelper = com.namma.santhe.util.NotificationHelper(context)
}
