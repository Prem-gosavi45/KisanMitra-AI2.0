package com.example

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthService : AuthService {
    private val auth = FirebaseModule.auth
    private val db = FirebaseModule.firestore
    
    private var storedVerificationId: String? = null

    override suspend fun sendOtp(phoneNumber: String, activity: Activity): Result<Unit> {
        return suspendCoroutine { continuation ->
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // handled automatically by Play Services for auto-retrieval
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        continuation.resume(Result.failure(e))
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        storedVerificationId = verificationId
                        continuation.resume(Result.success(Unit))
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override suspend fun verifyOtp(code: String, context: Context): Result<AuthUser> {
        val verificationId = storedVerificationId
            ?: return Result.failure(Exception("Session expired. Please request OTP again."))
            
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return signInWithPhoneAuthCredential(credential)
    }

    private suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Result<AuthUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = checkUserDocument(result.user!!.uid)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Incorrect OTP or sign-in failed."))
        }
    }

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("992718293512-9bhmohphe3ge9t396pquvqpq0phb01d4.apps.googleusercontent.com")
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is GoogleIdTokenCredential) {
                val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = checkUserDocument(authResult.user!!.uid)
                Result.success(user)
            } else {
                Result.failure(Exception("Unexpected credential type"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Google Sign-In cancelled or failed: ${e.message}"))
        }
    }

    override suspend fun checkSession(context: Context): AuthUser? {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            try {
                checkUserDocument(currentUser.uid)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    private suspend fun checkUserDocument(uid: String): AuthUser {
        val doc = db.collection("users").document(uid).get().await()
        return if (doc.exists()) {
            val role = doc.getString("role") ?: "farmer"
            val language = doc.getString("language") ?: "en"
            AuthUser(uid, role, language, isProfileComplete = true)
        } else {
            AuthUser(uid, "farmer", "en", isProfileComplete = false)
        }
    }

    override suspend fun saveProfile(
        context: Context,
        role: String,
        language: String,
        profileData: Map<String, Any>
    ): Result<AuthUser> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val userData = profileData.toMutableMap()
            userData["uid"] = uid
            userData["role"] = role
            userData["language"] = language
            userData["createdAt"] = System.currentTimeMillis()
            
            db.collection("users").document(uid).set(userData).await()
            Result.success(AuthUser(uid, role, language, isProfileComplete = true))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save profile."))
        }
    }

    override suspend fun logout(context: Context): Result<Unit> {
        auth.signOut()
        return Result.success(Unit)
    }
}
