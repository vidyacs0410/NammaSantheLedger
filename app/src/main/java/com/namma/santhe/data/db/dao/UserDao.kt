package com.namma.santhe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.namma.santhe.data.db.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :identifier OR name = :identifier LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE phone = :phone")
    suspend fun updatePassword(phone: String, newPasswordHash: String)

    @Query("DELETE FROM users WHERE phone = :phone")
    suspend fun deleteUserByPhone(phone: String)
}
