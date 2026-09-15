package com.example.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.random.Random
import java.util.concurrent.TimeUnit

object EmailJsHelper {
    const val SERVICE_ID = "service_YourStudio"
    const val TEMPLATE_ID = "template_jypc4lp"
    const val PUBLIC_KEY = "P5oDiZwDz6q7G9DGs"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Generates a random 6-digit verification OTP code
     */
    fun generate6DigitOtp(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    /**
     * Sends the OTP email using EmailJS official REST API
     */
    suspend fun sendOtpEmail(toEmail: String, otpCode: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val cleanEmail = toEmail.trim()
                Log.d("EmailJS", "ناردن بۆ ئەم ئیمەیڵە: $cleanEmail")
                
                val templateParams = JSONObject().apply {
                    put("to_email", cleanEmail)
                    put("otp_code", otpCode)
                }
                val payload = JSONObject().apply {
                    put("service_id", SERVICE_ID)
                    put("template_id", TEMPLATE_ID)
                    put("user_id", PUBLIC_KEY)
                    put("template_params", templateParams)
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = payload.toString().toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                    
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    Log.i("EmailJS", "Response code: ${response.code}, body: $responseBody")
                    
                    if (response.isSuccessful || response.code == 200) {
                        Result.success("کۆدی دڵنیابوونەوە بە سەرکەوتوویی نێردرا")
                    } else {
                        Log.e("EmailJS", "EmailJS error: $responseBody")
                        Result.failure(Exception("هەڵە لە ناردنی ئیمەیڵ: $responseBody"))
                    }
                }
            } catch (e: Exception) {
                Log.e("EmailJS", "Exception sending email: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
