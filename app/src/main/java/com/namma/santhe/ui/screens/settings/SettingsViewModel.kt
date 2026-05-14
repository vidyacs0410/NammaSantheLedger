package com.namma.santhe.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.namma.santhe.data.SessionManager
import android.app.Application
import androidx.work.*
import com.namma.santhe.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val application: Application
) : ViewModel() {

    private val _language = MutableStateFlow(sessionManager.getLanguage())
    val language = _language.asStateFlow()

    private val _isDarkMode = MutableStateFlow(sessionManager.isDarkMode())
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(sessionManager.isNotificationsEnabled())
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    init {
        if (sessionManager.isNotificationsEnabled()) {
            scheduleReminders()
        }
    }

    companion object {
        private const val REMINDER_WORK_NAME = "ledger_daily_reminder"
    }

    fun setLanguage(lang: String) {
        sessionManager.setLanguage(lang)
        _language.value = lang
    }

    fun setDarkMode(enabled: Boolean) {
        sessionManager.setDarkMode(enabled)
        _isDarkMode.value = enabled
    }

    fun toggleNotifications() {
        val newValue = !_notificationsEnabled.value
        _notificationsEnabled.value = newValue
        sessionManager.setNotificationsEnabled(newValue)
        
        if (newValue) {
            scheduleReminders()
        } else {
            cancelReminders()
        }
    }

    private fun scheduleReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag(REMINDER_WORK_NAME)
            .build()

        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    private fun cancelReminders() {
        WorkManager.getInstance(application).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun clearSession() {
        sessionManager.clearSession()
    }
}
