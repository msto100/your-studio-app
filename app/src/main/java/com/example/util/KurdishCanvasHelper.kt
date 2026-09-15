package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object KurdishCanvasHelper {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Download Bitmap from HTTP URL if needed
     */
    suspend fun fetchBitmapFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                if (bytes != null && bytes.isNotEmpty()) {
                    return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            }
        } catch (e: Exception) {
            // Log or fallback
        }
        null
    }

    /**
     * Render the logo emblem and precisely etch Kurdish text onto the canvas with professional styling and real Kurdish fonts.
     * Supports:
     * - 20 Standard Kurdish & Arabic Google Fonts via KurdishFontHelper.
     * - Positioning: Below the emblem (clean studio brand layout) or Over the bottom of the emblem with backdrop.
     * - Custom Kurdish font size and color.
     * - RTL direction shaping and drop shadows.
     */
    suspend fun renderLogoWithKurdishText(
        context: Context,
        baseBitmap: Bitmap?,
        category: String,
        kurdishText: String,
        fontId: String = "vazirmatn",
        subtitle: String = "",
        primaryColorHex: String = "#4F46E5",
        accentColorHex: String = "#06B6D4",
        textSizeSp: Float = 42f,
        textColorInt: Int = Color.WHITE,
        positionBelow: Boolean = true
    ): Bitmap = withContext(Dispatchers.IO) {
        val width = 1024
        val height = if (positionBelow) 1200 else 1024

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // 1. Draw rich dark studio background with subtle radial/linear glow
        val bgPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor("#0F1018"), Color.parseColor("#08080E")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Draw real AI generated emblem if available (No fake geometric shapes!)
        if (baseBitmap != null) {
            val destRect = if (positionBelow) {
                val emblemSize = 780
                val left = (width - emblemSize) / 2
                val top = 70
                Rect(left, top, left + emblemSize, top + emblemSize)
            } else {
                Rect(0, 0, width, height)
            }
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            canvas.drawBitmap(baseBitmap, null, destRect, paint)
        }

        // 3. Draw Kurdish Text using chosen Kurdish Font & RTL Shaping
        if (kurdishText.isNotBlank()) {
            val kurdishTypeface = KurdishFontHelper.getTypeface(context, fontId)

            val textPaint = android.text.TextPaint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                color = textColorInt
                textSize = textSizeSp * 2.2f // Scaled for 1024px canvas
                typeface = kurdishTypeface
                // Drop shadow for high-contrast readability
                setShadowLayer(14f, 0f, 6f, Color.parseColor("#B3000000"))
            }

            val textY: Float
            if (positionBelow) {
                textY = 1040f
            } else {
                // Solid crisp dark banner (bg-gray-900: #111827) for high-performance rendering
                val bannerHeight = 220f
                val bannerTop = height - bannerHeight
                val bannerPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#111827") // bg-gray-900
                }
                canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bannerPaint)
                textY = height - 105f
            }

            // Draw primary Kurdish Project Name with RTL direction
            canvas.save()
            canvas.drawText(kurdishText, width / 2f, textY, textPaint)
            canvas.restore()

            // Draw Category or Subtitle if available
            val sub = if (subtitle.isNotBlank()) subtitle else category
            if (sub.isNotBlank()) {
                val subPaint = android.text.TextPaint().apply {
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    color = Color.parseColor("#B3D4D4D8")
                    textSize = 28f
                    typeface = kurdishTypeface
                    letterSpacing = 0.04f
                    setShadowLayer(6f, 0f, 3f, Color.parseColor("#80000000"))
                }
                canvas.drawText(sub, width / 2f, textY + 54f, subPaint)
            }
        }

        resultBitmap
    }

    /**
     * Save the rendered Bitmap directly to storage (Cache / Files) as high quality PNG
     */
    suspend fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        fileNamePrefix: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "saved_logos").apply { if (!exists()) mkdirs() }
            val cleanName = fileNamePrefix.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_")
            val file = File(dir, "kurdish_logo_${cleanName}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}
