package com.example

import android.app.Activity
import android.content.Context

interface AuthService {
    suspend fun sendOtp(phoneNumber: String, activity: Activity): Result<Unit>
    suspend fun verifyOtp(code: String, context: Context): Result<AuthUser>
    suspend fun signInWithGoogle(context: Context): Result<AuthUser>
    suspend fun checkSession(context: Context): AuthUser?
    suspend fun saveProfile(context: Context, role: String, language: String, profileData: Map<String, Any>): Result<AuthUser>
    suspend fun logout(context: Context): Result<Unit>
}

data class AuthUser(
    val uid: String,
    val role: String,
    val language: String,
    val isProfileComplete: Boolean = false
)
