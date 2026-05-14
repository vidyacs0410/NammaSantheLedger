package com.namma.santhe.data.repository

import com.namma.santhe.data.db.dao.UserDao
import com.namma.santhe.data.db.entity.UserEntity
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {

    suspend fun registerUser(name: String, phone: String, passwordRaw: String): Boolean {
        // Check if user already exists
        val existing = userDao.getUserByPhone(phone)
        if (existing != null) return false

        val hash = hashPassword(passwordRaw)
        val user = UserEntity(name = name, phone = phone, passwordHash = hash)
        userDao.insertUser(user)
        return true
    }

    suspend fun findUser(identifier: String): UserEntity? {
        return userDao.getUserByIdentifier(identifier)
    }

    fun verifyPassword(user: UserEntity, passwordRaw: String): Boolean {
        return user.passwordHash == hashPassword(passwordRaw)
    }

    suspend fun loginUser(identifier: String, passwordRaw: String): UserEntity? {
        val user = findUser(identifier)
        if (user != null && verifyPassword(user, passwordRaw)) {
            return user
        }
        return null
    }

    suspend fun resetPassword(phone: String, newPasswordRaw: String): Boolean {
        val existing = userDao.getUserByPhone(phone)
        if (existing != null) {
            userDao.updatePassword(phone, hashPassword(newPasswordRaw))
            return true
        }
        return false
    }
    
    suspend fun checkUserExists(phone: String): Boolean {
        return userDao.getUserByPhone(phone) != null
    }

    suspend fun deleteUser(phone: String) {
        userDao.deleteUserByPhone(phone)
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
