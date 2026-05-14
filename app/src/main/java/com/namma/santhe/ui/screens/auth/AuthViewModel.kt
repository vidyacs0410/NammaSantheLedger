package com.namma.santhe.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namma.santhe.data.SessionManager
import com.namma.santhe.data.repository.AuthRepository
import com.namma.santhe.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Temporary storage for signup before OTP verification
    var pendingSignupName = ""
    var pendingSignupPhone = ""
    var pendingSignupPassword = ""

    fun login(identifier: String, passwordRaw: String) {
        if (identifier.isBlank() || passwordRaw.isBlank()) {
            _authState.value = AuthState(error = "Fields cannot be empty")
            return
        }
        
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            val user = authRepository.findUser(identifier.trim())
            if (user == null) {
                _authState.value = AuthState(error = "username or phonenumber is invalid")
            } else if (!authRepository.verifyPassword(user, passwordRaw)) {
                _authState.value = AuthState(error = "password is invalid")
            } else {
                sessionManager.saveLoginSession(user.id, user.name, user.phone)
                _authState.value = AuthState(isSuccess = true)
            }
        }
    }

    fun initiateSignup(nameRaw: String, phoneRaw: String, passwordRaw: String, confirmPasswordRaw: String, onNavigateToOtp: () -> Unit) {
        val name = nameRaw.trim()
        val phone = phoneRaw.trim()
        if (name.isBlank() || phone.isBlank() || passwordRaw.isBlank() || confirmPasswordRaw.isBlank()) {
            _authState.value = AuthState(error = "Please fill all fields")
            return
        }
        if (!isValidIndianPhone(phone)) {
            _authState.value = AuthState(error = "Please enter a valid 10-digit Indian phone number")
            return
        }
        val passwordError = getPasswordValidationError(passwordRaw)
        if (passwordError != null) {
            _authState.value = AuthState(error = passwordError)
            return
        }
        if (passwordRaw != confirmPasswordRaw) {
            _authState.value = AuthState(error = "Passwords do not match")
            return
        }
        
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            if (authRepository.checkUserExists(phone)) {
                _authState.value = AuthState(error = "Phone number already registered")
            } else {
                _authState.value = AuthState()
                pendingSignupName = name
                pendingSignupPhone = phone
                pendingSignupPassword = passwordRaw
                notificationHelper.showOtpNotification("1234") // Simulate SMS arrival
                android.widget.Toast.makeText(notificationHelper.getContext(), "Simulated SMS: Your OTP is 1234", android.widget.Toast.LENGTH_LONG).show()
                onNavigateToOtp()
            }
        }
    }

    fun resendOtp() {
        notificationHelper.showOtpNotification("1234")
        android.widget.Toast.makeText(notificationHelper.getContext(), "Resent Simulated SMS: Your OTP is 1234", android.widget.Toast.LENGTH_LONG).show()
    }

    fun verifyOtpAndSignup(otp: String) {
        if (otp != "1234") { // Mock verification
            _authState.value = AuthState(error = "Invalid OTP. Use 1234 for testing.")
            return
        }
        
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            val success = authRepository.registerUser(pendingSignupName, pendingSignupPhone, pendingSignupPassword)
            if (success) {
                _authState.value = AuthState(isSuccess = true)
            } else {
                _authState.value = AuthState(error = "Failed to register user")
            }
        }
    }
    
    fun resetPassword(phone: String, newPasswordRaw: String, confirmNewPasswordRaw: String) {
        if (phone.isBlank() || newPasswordRaw.isBlank()) {
            _authState.value = AuthState(error = "Fields cannot be empty")
            return
        }
        val passwordError = getPasswordValidationError(newPasswordRaw)
        if (passwordError != null) {
            _authState.value = AuthState(error = passwordError)
            return
        }
        if (newPasswordRaw != confirmNewPasswordRaw) {
            _authState.value = AuthState(error = "Passwords do not match")
            return
        }
        
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            val success = authRepository.resetPassword(phone, newPasswordRaw)
            if (success) {
                _authState.value = AuthState(isSuccess = true) // Navigates back or to home
            } else {
                _authState.value = AuthState(error = "Phone number not found")
            }
        }
    }

    fun deleteUser(phone: String) {
        viewModelScope.launch {
            authRepository.deleteUser(phone)
            _authState.value = AuthState(error = "Account with $phone deleted successfully")
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
    
    fun resetState() {
        _authState.value = AuthState()
    }

    private fun isValidIndianPhone(phoneRaw: String): Boolean {
        val phone = phoneRaw.trim()
        val regex = Regex("^[6-9][0-9]{9}$")
        return regex.matches(phone)
    }

    private fun getPasswordValidationError(password: String): String? {
        if (password.length < 8) return "Password must be at least 8 characters long"
        if (!password.any { !it.isLetterOrDigit() }) return "Password must contain a special character"
        if (!password.any { it.isUpperCase() } || 
            !password.any { it.isLowerCase() } || 
            !password.any { it.isDigit() }) {
            return "Password is too weak"
        }
        return null
    }
}
