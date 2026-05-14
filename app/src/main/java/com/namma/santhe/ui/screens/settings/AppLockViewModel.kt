package com.namma.santhe.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.namma.santhe.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isAppLockEnabled = MutableStateFlow(sessionManager.isAppLockEnabled())
    val isAppLockEnabled = _isAppLockEnabled.asStateFlow()

    fun setAppLockEnabled(enabled: Boolean) {
        sessionManager.setAppLockEnabled(enabled)
        _isAppLockEnabled.value = enabled
    }
}
