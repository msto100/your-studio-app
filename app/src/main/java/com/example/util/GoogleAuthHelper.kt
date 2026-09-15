package com.example.util

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// =========================================================================
// 🔑 GOOGLE SIGN-IN & OAUTH 2.0 CONFIGURATION
// شوێنی سەرەکی ڕێکخستنی GOOGLE_CLIENT_ID بۆ پڕۆژەی yourstudio-e0975:
// =========================================================================
var GOOGLE_CLIENT_ID: String = "33981999584-webclient.apps.googleusercontent.com"

data class GoogleUserData(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val idToken: String? = null
)

object GoogleAuthHelper {

    /**
     * Attempts to trigger official Google Credential Manager login
     */
    suspend fun signInWithGoogleCredentialManager(context: Context): GoogleUserData? = withContext(Dispatchers.IO) {
        PhoneAuthHelper.initFirebase(context)
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(GOOGLE_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val token = googleIdTokenCredential.idToken
                if (!token.isNullOrBlank()) {
                    try {
                        val auth = FirebaseAuth.getInstance()
                        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
                        auth.signInWithCredential(firebaseCredential).await()
                    } catch (e: Exception) {
                        Log.w("GoogleAuth", "Firebase auth credential sign in skipped or failed: ${e.message}")
                    }
                }
                return@withContext GoogleUserData(
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id.substringBefore("@"),
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: "https://lh3.googleusercontent.com/a/default-user",
                    idToken = token
                )
            }
        } catch (_: GetCredentialException) {
        } catch (_: Exception) {
        }
        null
    }

    /**
     * Lists Gmail accounts currently signed-in on this Android device via AccountManager
     */
    fun getDeviceGoogleAccounts(context: Context): List<GoogleUserData> {
        val list = mutableListOf<GoogleUserData>()
        try {
            val accountManager = AccountManager.get(context)
            val accounts: Array<Account> = accountManager.getAccountsByType("com.google")
            for (acc in accounts) {
                if (acc.name.isNotBlank()) {
                    val name = acc.name.substringBefore("@").replace(".", " ")
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    list.add(
                        GoogleUserData(
                            email = acc.name,
                            displayName = name,
                            photoUrl = "https://ui-avatars.com/api/?name=${name.replace(" ", "+")}&background=EA4335&color=fff&size=256"
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        if (list.isEmpty()) {
            list.add(
                GoogleUserData(
                    email = "example@gmail.com",
                    displayName = "Google User",
                    photoUrl = "https://ui-avatars.com/api/?name=Google+User&background=EA4335&color=fff&size=256"
                )
            )
        }
        return list
    }

    /**
     * Creates system intent to launch the official Google Account Picker dialog
     */
    fun createGoogleAccountPickerIntent(): Intent? {
        return try {
            AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Helper to create formatted Google User Data from selected Gmail account
     */
    fun createGoogleProfileFromEmail(email: String, customName: String = ""): GoogleUserData {
        val cleanEmail = email.trim()
        val defaultName = if (customName.isNotBlank()) customName else {
            cleanEmail.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        }
        // Modern standard Google Account avatar URL pattern
        val avatarUrl = "https://ui-avatars.com/api/?name=${defaultName.replace(" ", "+")}&background=EA4335&color=fff&size=256"
        return GoogleUserData(
            email = cleanEmail,
            displayName = defaultName,
            photoUrl = avatarUrl
        )
    }
}
