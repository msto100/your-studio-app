package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// =========================================================================
// 🔑 OPENAI / CHATGPT API CONFIGURATION
// شوێنی تەرخانکراو لەسەرەوەی کۆد بۆ دانانی کلیلی OPENAI_API_KEY:
// =========================================================================
var OPENAI_API_KEY: String = "" // لێرە کلیلی فەرمی OpenAI دابنێ: "sk-..."

data class OpenAiImageResult(
    val isSuccess: Boolean,
    val imageUrl: String? = null,
    val revisedPrompt: String? = null,
    val errorMessage: String? = null,
    val rawResponse: String? = null
)

object OpenAiService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Send generation request directly to OpenAI DALL-E Image API
     */
    suspend fun generateDalleImage(
        prompt: String,
        category: String,
        projectName: String,
        details: String
    ): OpenAiImageResult = withContext(Dispatchers.IO) {
        val apiKey = OPENAI_API_KEY.ifBlank {
            try {
                val field = BuildConfig::class.java.getField("OPENAI_API_KEY")
                field.get(null) as? String ?: ""
            } catch (e: Exception) {
                ""
            }
        }.trim()

        if (apiKey.isBlank()) {
            return@withContext OpenAiImageResult(
                isSuccess = false,
                errorMessage = "تکایە کلیلی OPENAI_API_KEY لە سەرەوەی کۆد یان ڕێکخستن دابنێ."
            )
        }

        try {
            val enrichedPrompt = buildEnrichedPrompt(category, projectName, details, prompt)

            val jsonBody = JSONObject().apply {
                put("model", "dall-e-3")
                put("prompt", enrichedPrompt)
                put("n", 1)
                put("size", "1024x1024")
                put("quality", "standard")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val root = JSONObject(responseString)
                val dataArray = root.optJSONArray("data")
                val firstItem = dataArray?.optJSONObject(0)
                val url = firstItem?.optString("url")
                val revised = firstItem?.optString("revised_prompt")

                if (!url.isNullOrBlank()) {
                    return@withContext OpenAiImageResult(
                        isSuccess = true,
                        imageUrl = url,
                        revisedPrompt = revised ?: enrichedPrompt,
                        rawResponse = responseString
                    )
                }
            }

            val errorMsg = try {
                val errObj = JSONObject(responseString).optJSONObject("error")
                errObj?.optString("message") ?: "هەڵە لە وەڵامی OpenAI (${response.code})"
            } catch (e: Exception) {
                "کۆدی وەڵام: ${response.code}"
            }

            OpenAiImageResult(
                isSuccess = false,
                errorMessage = errorMsg,
                rawResponse = responseString
            )
        } catch (e: Exception) {
            OpenAiImageResult(
                isSuccess = false,
                errorMessage = "هەڵەی پەیوەندی بە سێرڤەری OpenAI: ${e.localizedMessage}"
            )
        }
    }

    private fun buildEnrichedPrompt(
        category: String,
        projectName: String,
        details: String,
        customPrompt: String
    ): String {
        val extraNotes = if (customPrompt.isNotBlank()) " Additional instructions: $customPrompt." else ""
        return "CRITICAL INSTRUCTION: DO NOT generate, render, or write ANY text, letters, words, alphabet, or typography on this logo image. Create ONLY a clean, standalone visual graphic emblem, iconic symbol, minimalist luxury vector badge, or artistic illustration without any written words or characters. Theme: \"$projectName\" in category \"$category\". Details: ${details.ifBlank { "Modern aesthetic emblem" }}.$extraNotes Perfectly centered composition, masterwork graphic design, 8k resolution vector minimalism, solid clean dark studio background."
    }
}
