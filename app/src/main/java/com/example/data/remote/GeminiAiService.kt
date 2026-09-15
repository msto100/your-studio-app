package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateLogoDesign(
        category: String,
        projectName: String,
        details: String,
        feedback: String = "",
        iteration: Int = 1
    ): LogoAiResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val promptText = """
            You are a world-class graphic designer and brand identity architect.
            Design a modern, memorable logo concept for:
            - Category: $category
            - Project Name: $projectName
            - User Requirements: $details
            ${if (feedback.isNotBlank()) "- User Feedback / Changes requested: $feedback (Iteration $iteration)" else ""}

            CRITICAL REQUIREMENT: The visual logo emblem must NOT contain any text, letters, words, or character shapes at all. AI must describe ONLY the graphic icon/emblem/symbol to avoid distorted typography. The brand name text will be rendered separately via typography canvas.

            Return a structured response in the following format:
            CONCEPT_NAME: [Creative Catchy Name for Logo Concept]
            TAGLINE: [Punchy Slogan]
            PRIMARY_COLOR: [#HexCode]
            ACCENT_COLOR: [#HexCode]
            EMBLEM_SHAPE: [Circle / Shield / Hexagon / Minimalist Monogram / Geometric Polygon]
            DESIGN_EXPLANATION: [Kurdish or English explanation of the visual elements and why this fits the brand]
            PROMPT_CODE: [Clean emblem prompt for AI image generators without any text: Midjourney / DALL-E]
        """.trimIndent()

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val jsonBody = JSONObject().apply {
                    val contentsArray = JSONArray()
                    val contentObj = JSONObject()
                    val partsArray = JSONArray()
                    val partObj = JSONObject()
                    partObj.put("text", promptText)
                    partsArray.put(partObj)
                    contentObj.put("parts", partsArray)
                    contentsArray.put(contentObj)
                    put("contents", contentsArray)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseString = response.body?.string() ?: ""
                    val root = JSONObject(responseString)
                    val candidates = root.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text") ?: ""

                    if (text.isNotBlank()) {
                        return@withContext parseLogoAiResult(text, projectName, category)
                    }
                }
            } catch (e: Exception) {
                // Fallback to offline creative generator
            }
        }

        // High quality deterministic creative generator
        generateCreativeFallbackLogo(category, projectName, details, feedback, iteration)
    }

    /**
     * Vision-based Prompt Code Generator (Image-to-Prompt)
     * Analyzes uploaded image via Gemini Vision API and writes an elite Midjourney/AI prompt code
     */
    suspend fun generatePromoCodeForImage(
        context: Context,
        projectName: String,
        userName: String,
        sampleFileName: String,
        imageUri: Uri? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val visionInstructions = """
            You are an elite Vision AI and expert prompt engineer specializing in Midjourney v6, Leonardo AI, and ChatGPT.
            Analyze this image in detail and construct a comprehensive, hyper-realistic, professional English prompt code capturing:
            1. Art Style & Medium (e.g. cinematic photography, hyper-detailed 3D Octane render, luxury product shot)
            2. Lighting & Atmosphere (e.g. volumetric cinematic rim lighting, dramatic contrast, warm golden hour, soft studio diffuse)
            3. Color Palette & Grading (dominant tones, mood, cinematic color grading)
            4. Camera Angle & Lens (e.g. 85mm f/1.4 portrait lens, low-angle perspective, bokeh depth of field)
            5. Subject Details & Composition (textures, fine details, focal elements)
            6. Mandatory parameters: include --ar 16:9 --v 6.0 --style raw --stylize 250

            Context info: Project: $projectName, User: $userName, Reference: $sampleFileName.
            Provide a clean, copyable prompt starting with /imagine prompt: followed by the full English prompt.
        """.trimIndent()

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val jsonBody = JSONObject().apply {
                    val contentsArray = JSONArray()
                    val contentObj = JSONObject()
                    val partsArray = JSONArray()

                    // If user attached an image, encode it to base64 for Vision API
                    if (imageUri != null) {
                        val base64Data = getImageBase64(context, imageUri)
                        if (!base64Data.isNullOrBlank()) {
                            val inlineData = JSONObject().apply {
                                put("mime_type", "image/jpeg")
                                put("data", base64Data)
                            }
                            val imagePart = JSONObject().apply {
                                put("inline_data", inlineData)
                            }
                            partsArray.put(imagePart)
                        }
                    }

                    val textPart = JSONObject().apply {
                        put("text", visionInstructions)
                    }
                    partsArray.put(textPart)

                    contentObj.put("parts", partsArray)
                    contentsArray.put(contentObj)
                    put("contents", contentsArray)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody(mediaType))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val resString = response.body?.string() ?: ""
                    val root = JSONObject(resString)
                    val candidates = root.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text") ?: ""
                    if (text.isNotBlank()) {
                        return@withContext text.trim()
                    }
                }
            } catch (e: Exception) {
                // Fallback to structured prompt
            }
        }

        return@withContext """
/imagine prompt: cinematic commercial aesthetic of $projectName, artistic visual style inspired by $sampleFileName, hyper-detailed textures, volumetric atmospheric studio lighting, 8k resolution, Hasselblad H6D-100c 80mm lens, f/1.8, shallow depth of field, premium color grading with cinematic contrast, elegant composition --ar 16:9 --v 6.0 --style raw --stylize 250
        """.trimIndent()
    }

    private fun getImageBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Scale down if too large to ensure fast, reliable upload
            val maxDimension = 1024
            val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                val targetW = if (ratio > 1f) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (ratio > 1f) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseLogoAiResult(rawText: String, projectName: String, category: String): LogoAiResult {
        var conceptName = projectName
        var primaryColor = "#6366F1"
        var accentColor = "#EC4899"
        var emblem = "Shield"
        var explanation = rawText

        val lines = rawText.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("CONCEPT_NAME:", ignoreCase = true)) {
                conceptName = trimmed.substringAfter(":").trim()
            } else if (trimmed.startsWith("PRIMARY_COLOR:", ignoreCase = true)) {
                val hex = trimmed.substringAfter(":").trim()
                if (hex.startsWith("#") && hex.length in 7..9) primaryColor = hex
            } else if (trimmed.startsWith("ACCENT_COLOR:", ignoreCase = true)) {
                val hex = trimmed.substringAfter(":").trim()
                if (hex.startsWith("#") && hex.length in 7..9) accentColor = hex
            } else if (trimmed.startsWith("EMBLEM_SHAPE:", ignoreCase = true)) {
                emblem = trimmed.substringAfter(":").trim()
            }
        }

        return LogoAiResult(
            conceptName = conceptName,
            category = category,
            primaryColor = primaryColor,
            accentColor = accentColor,
            emblemShape = emblem,
            explanation = explanation,
            fullPromptCode = rawText
        )
    }

    private fun generateCreativeFallbackLogo(
        category: String,
        projectName: String,
        details: String,
        feedback: String,
        iteration: Int
    ): LogoAiResult {
        val palettes = listOf(
            Pair("#6366F1", "#06B6D4"), // Indigo + Cyan
            Pair("#EC4899", "#F59E0B"), // Pink + Amber
            Pair("#10B981", "#3B82F6"), // Emerald + Blue
            Pair("#8B5CF6", "#F43F5E"), // Purple + Rose
            Pair("#F97316", "#EAB308")  // Orange + Yellow
        )
        val palette = palettes[(projectName.hashCode().let { if (it < 0) -it else it } + iteration) % palettes.size]

        val shapes = listOf("Geometric Monogram", "Shield Emblem", "Cyber Circle", "Minimalist Crown", "Diamond Apex")
        val shape = shapes[(projectName.length + iteration) % shapes.size]

        val explanation = if (feedback.isNotBlank()) {
            "دیزاینی نوێکرایەوە بەپێی تێبینییەکانت: «$feedback». ڕەنگ و شێوەی هێماکە لەسەر شێوازی مۆدێرن و سەرنجڕاکێش ڕێکخرایەوە."
        } else {
            "دیزاینێکی مۆدێرن و تایبەت بە $category بۆ براندی $projectName بە بەکارهێنانی سیمبولی ئەندازەیی کەم-وێنە و کواڵێتی باڵا."
        }

        return LogoAiResult(
            conceptName = "$projectName Design #$iteration",
            category = category,
            primaryColor = palette.first,
            accentColor = palette.second,
            emblemShape = shape,
            explanation = explanation,
            fullPromptCode = "Vector minimalist logo for $projectName, $category, sleek modern icon, emblem $shape, vibrant gradient ${palette.first} to ${palette.second}, clean typography, vector SVG master --v 6.0"
        )
    }
}

data class LogoAiResult(
    val conceptName: String,
    val category: String,
    val primaryColor: String,
    val accentColor: String,
    val emblemShape: String,
    val explanation: String,
    val fullPromptCode: String,
    val imageUrl: String? = null
)
