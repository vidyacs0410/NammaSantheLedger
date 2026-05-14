package com.namma.santhe.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.namma.santhe.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val sessionManager: SessionManager
) : ViewModel() {
    private val _profileImage = MutableStateFlow(sessionManager.getProfileImage())
    val profileImage = _profileImage.asStateFlow()

    private val _businessType = MutableStateFlow(sessionManager.getBusinessType())
    val businessType = _businessType.asStateFlow()

    private val _address = MutableStateFlow(sessionManager.getAddress())
    val address = _address.asStateFlow()

    fun updateProfile(imageUri: String?, type: String, addressStr: String) {
        sessionManager.saveProfileData(imageUri, type, addressStr)
        _profileImage.value = imageUri
        _businessType.value = type
        _address.value = addressStr
    }
}
