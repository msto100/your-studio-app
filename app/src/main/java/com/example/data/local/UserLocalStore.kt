package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * LocalStorage equivalent for persistent user authentication data
 */
object UserLocalStore {
    private const val PREF_NAME = "studio_user_storage"
    private const val KEY_CURRENT_USER = "currentUser"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveCurrentUser(context: Context, name: String, email: String) {
        val json = """{"name":"$name","email":"$email","isLoggedIn":true}"""
        getPrefs(context).edit().putString(KEY_CURRENT_USER, json).apply()
    }

    fun clearCurrentUser(context: Context) {
        getPrefs(context).edit().remove(KEY_CURRENT_USER).apply()
    }

    fun getCurrentUserJson(context: Context): String? {
        return getPrefs(context).getString(KEY_CURRENT_USER, null)
    }
    
    // DEMO BACKEND: Save registered user credentials
    fun saveRegisteredUser(context: Context, email: String, password: String, name: String) {
        getPrefs(context).edit()
            .putString("user_pass_$email", password)
            .putString("user_name_$email", name)
            .apply()
    }
    
    fun isEmailRegistered(context: Context, email: String): Boolean {
        return getPrefs(context).contains("user_pass_$email")
    }
    
    fun verifyPassword(context: Context, email: String, password: String): Boolean {
        val savedPass = getPrefs(context).getString("user_pass_$email", null)
        return savedPass == password
    }
    
    fun getRegisteredName(context: Context, email: String): String {
        return getPrefs(context).getString("user_name_$email", "بەکارهێنەر") ?: "بەکارهێنەر"
    }
}
