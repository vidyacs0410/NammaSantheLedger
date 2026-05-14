package com.namma.santhe.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("santhe_session", Context.MODE_PRIVATE)

    fun saveLoginSession(userId: Int, name: String, phone: String) {
        prefs.edit().apply {
            putBoolean("IS_LOGGED_IN", true)
            putInt("USER_ID", userId)
            putString("USER_NAME", name)
            putString("USER_PHONE", phone)
            apply()
        }
    }

    fun updateName(newName: String) {
        prefs.edit().putString("USER_NAME", newName).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    fun getUserName(): String {
        return prefs.getString("USER_NAME", "User") ?: "User"
    }

    fun getUserPhone(): String {
        return prefs.getString("USER_PHONE", "") ?: ""
    }

    fun saveProfileData(imageUri: String?, businessType: String, address: String) {
        prefs.edit().apply {
            putString("PROFILE_IMAGE", imageUri)
            putString("BUSINESS_TYPE", businessType)
            putString("ADDRESS", address)
            apply()
        }
    }

    fun getProfileImage(): String? {
        return prefs.getString("PROFILE_IMAGE", null)
    }

    fun getBusinessType(): String {
        return prefs.getString("BUSINESS_TYPE", "Personal Use") ?: "Personal Use"
    }

    fun getAddress(): String {
        return prefs.getString("ADDRESS", "") ?: ""
    }

    private val _languageFlow = MutableStateFlow(getLanguage())
    val languageFlow = _languageFlow.asStateFlow()

    fun setLanguage(lang: String) {
        prefs.edit().putString("APP_LANGUAGE", lang).apply()
        _languageFlow.value = lang
    }

    fun getLanguage(): String = prefs.getString("APP_LANGUAGE", "kn") ?: "kn"

    private val _darkModeFlow = MutableStateFlow(isDarkMode())
    val darkModeFlow = _darkModeFlow.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("DARK_MODE", enabled).apply()
        _darkModeFlow.value = enabled
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("DARK_MODE", false)

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("APP_LOCK_ENABLED", enabled).apply()
    }

    fun isAppLockEnabled(): Boolean = prefs.getBoolean("APP_LOCK_ENABLED", false)
    
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("NOTIFICATIONS_ENABLED", enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean("NOTIFICATIONS_ENABLED", true)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
