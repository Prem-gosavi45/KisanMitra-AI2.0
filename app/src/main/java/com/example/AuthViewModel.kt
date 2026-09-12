package com.example

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class CodeSent(val verificationId: String = "mock_verification_id") : AuthState()
    data class Success(val role: String, val language: String) : AuthState()
    data class RequiresProfileSetup(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    // Current: MockAuthService. Future: FirebaseAuthService.
    private val authService: AuthService = MockAuthService()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authService.sendOtp(phoneNumber, activity)
            if (result.isSuccess) {
                _authState.value = AuthState.CodeSent()
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Verification failed")
            }
        }
    }

    fun verifyOtp(code: String, context: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authService.verifyOtp(code, context)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                if (user.isProfileComplete) {
                    _authState.value = AuthState.Success(user.role, user.language)
                } else {
                    _authState.value = AuthState.RequiresProfileSetup(user.uid)
                }
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Incorrect OTP")
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authService.signInWithGoogle(context)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                if (user.isProfileComplete) {
                    _authState.value = AuthState.Success(user.role, user.language)
                } else {
                    _authState.value = AuthState.RequiresProfileSetup(user.uid)
                }
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
            }
        }
    }

    fun checkSession(context: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val user = authService.checkSession(context)
            if (user != null) {
                if (user.isProfileComplete) {
                    _authState.value = AuthState.Success(user.role, user.language)
                } else {
                    _authState.value = AuthState.RequiresProfileSetup(user.uid)
                }
            } else {
                _authState.value = AuthState.Initial
            }
        }
    }

    fun saveProfile(context: Context, uid: String, role: String, language: String, profileData: Map<String, Any>) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authService.saveProfile(context, role, language, profileData)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(role, language)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to save profile.")
            }
        }
    }
    
    fun logout(context: Context) {
        viewModelScope.launch {
            authService.logout(context)
            _authState.value = AuthState.Initial
        }
    }

    fun resetState() {
        _authState.value = AuthState.Initial
    }
}