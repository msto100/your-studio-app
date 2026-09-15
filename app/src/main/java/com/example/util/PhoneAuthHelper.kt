package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

// =========================================================================
// 🔑 OFFICIAL FIREBASE CONFIGURATION
// پڕۆژەی فەرمی: yourstudio-e0975
// =========================================================================
const val FIREBASE_API_KEY: String = "AIzaSyBbhjcqhYzRWrNEZGUyVr84qjQ1fw5qcOc"
const val FIREBASE_AUTH_DOMAIN: String = "yourstudio-e0975.firebaseapp.com"
const val FIREBASE_PROJECT_ID: String = "yourstudio-e0975"
const val FIREBASE_STORAGE_BUCKET: String = "yourstudio-e0975.firebasestorage.app"
const val FIREBASE_SENDER_ID: String = "33981999584"
const val FIREBASE_APP_ID: String = "1:33981999584:web:7a33d4c0cf21b6588ce385"
const val FIREBASE_MEASUREMENT_ID: String = "G-MY30V3E9VK"

object PhoneAuthHelper {

    /**
     * Guarantees Firebase is initialized with official project credentials
     */
    fun initFirebase(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(FIREBASE_API_KEY)
                    .setApplicationId(FIREBASE_APP_ID)
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .setStorageBucket(FIREBASE_STORAGE_BUCKET)
                    .setGcmSenderId(FIREBASE_SENDER_ID)
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.i("FirebaseInit", "Firebase successfully initialized for project $FIREBASE_PROJECT_ID")
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Firebase initialization: ${e.message}")
        }
    }

    /**
     * Normalizes phone number into international format, specifically for Iraq (+964)
     * e.g. "0750 123 4567" -> "+9647501234567"
     * "7501234567" -> "+9647501234567"
     */
    fun formatIraqiPhoneNumber(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() }
        return when {
            rawPhone.startsWith("+964") -> rawPhone.replace(" ", "")
            digits.startsWith("964") -> "+$digits"
            digits.startsWith("07") -> "+964" + digits.substring(1)
            digits.startsWith("7") -> "+964$digits"
            else -> if (digits.isNotBlank()) "+964$digits" else "+9647500000000"
        }
    }

    /**
     * Trigger Firebase Phone Authentication SMS OTP directly to user phone
     */
    fun sendFirebasePhoneAuth(
        activity: Activity,
        rawPhone: String,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationFailed: (errorMessage: String) -> Unit,
        onInstantVerification: (credential: PhoneAuthCredential) -> Unit
    ) {
        initFirebase(activity)
        val formattedPhone = formatIraqiPhoneNumber(rawPhone)
        try {
            val auth = FirebaseAuth.getInstance()
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    onInstantVerification(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("FirebasePhoneAuth", "Verification failed: ${e.message}")
                    onVerificationFailed(e.message ?: "کێشەیەک لە سێرڤەری Firebase ڕوویدا.")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.i("FirebasePhoneAuth", "SMS code sent successfully: $verificationId")
                    onCodeSent(verificationId)
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e("FirebasePhoneAuth", "Error starting verification: ${e.message}")
            onVerificationFailed(e.message ?: "سێرڤەری Firebase پەیوەندی نەبەسترا.")
        }
    }

    /**
     * Verifies the 6-digit SMS code against the Firebase verificationId
     */
    fun verifyFirebaseOtp(
        context: Context? = null,
        verificationId: String,
        smsCode: String,
        onSuccess: (credential: PhoneAuthCredential) -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        if (context != null) {
            initFirebase(context)
        }
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
            val auth = FirebaseAuth.getInstance()
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess(credential)
                    } else {
                        val err = task.exception?.localizedMessage ?: "کۆدی پشتڕاستکردنەوە هەڵەیە یان بەسەرچووە."
                        onFailure(err)
                    }
                }
        } catch (e: Exception) {
            onFailure(e.localizedMessage ?: "کێشەیەک لە پشتڕاستکردنەوە ڕوویدا.")
        }
    }
}
