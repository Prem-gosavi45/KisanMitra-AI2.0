package com.example

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Centralized Firebase initialization module.
 * Provides singleton instances for Auth and Firestore with error handling.
 */
object FirebaseModule {
    private const val TAG = "FirebaseModule"

    val auth: FirebaseAuth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase Auth", e)
            throw IllegalStateException("Firebase Auth initialization failed", e)
        }
    }

    val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase Firestore", e)
            throw IllegalStateException("Firebase Firestore initialization failed", e)
        }
    }
}
