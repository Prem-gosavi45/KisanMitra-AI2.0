package com.example

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

class MockAuthService : AuthService {

    override suspend fun sendOtp(phoneNumber: String, activity: Activity): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }

    override suspend fun verifyOtp(code: String, context: Context): Result<AuthUser> {
        delay(500)
        if (code.length == 6) {
            val prefs = AppPreferences(context)
            // Just simulate getting current selected role and language from local preferences
            val role = prefs.roleFlow.firstOrNull() ?: "farmer"
            val language = prefs.languageFlow.firstOrNull() ?: "en"
            
            val user = AuthUser(
                uid = "demo-user-001",
                role = role,
                language = language,
                isProfileComplete = false // force profile setup to show
            )
            saveSession(context, user)
            return Result.success(user)
        }
        return Result.failure(Exception("Invalid OTP"))
    }

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> {
        delay(800) // 500-1000ms delay
        val prefs = AppPreferences(context)
        val role = prefs.roleFlow.firstOrNull() ?: "farmer"
        val language = prefs.languageFlow.firstOrNull() ?: "en"
        
        val user = AuthUser(
            uid = "demo-user-001",
            role = role,
            language = language,
            isProfileComplete = false // to mimic real auth logic flow
        )
        saveSession(context, user)
        return Result.success(user)
    }

    override suspend fun checkSession(context: Context): AuthUser? {
        val sharedPrefs = context.getSharedPreferences("mock_auth_session", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_authenticated", false)
        if (isLoggedIn) {
            return AuthUser(
                uid = sharedPrefs.getString("uid", "demo-user-001") ?: "demo-user-001",
                role = sharedPrefs.getString("role", "farmer") ?: "farmer",
                language = sharedPrefs.getString("language", "en") ?: "en",
                isProfileComplete = sharedPrefs.getBoolean("is_profile_complete", false)
            )
        }
        return null
    }

    override suspend fun saveProfile(
        context: Context,
        role: String,
        language: String,
        profileData: Map<String, Any>
    ): Result<AuthUser> {
        delay(500)
        val user = AuthUser(
            uid = "demo-user-001",
            role = role,
            language = language,
            isProfileComplete = true
        )
        saveSession(context, user)
        return Result.success(user)
    }

    override suspend fun logout(context: Context): Result<Unit> {
        val sharedPrefs = context.getSharedPreferences("mock_auth_session", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        return Result.success(Unit)
    }

    private fun saveSession(context: Context, user: AuthUser) {
        val sharedPrefs = context.getSharedPreferences("mock_auth_session", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putBoolean("is_authenticated", true)
            .putString("uid", user.uid)
            .putString("role", user.role)
            .putString("language", user.language)
            .putBoolean("is_profile_complete", user.isProfileComplete)
            .apply()
    }
}
