package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TelegramHelper {

    // =========================================================================
    // 🤖 TELEGRAM BOT API CONFIGURATION (تۆکنی فەرمی بۆتی تیلیگرام)
    // =========================================================================
    const val TELEGRAM_BOT_TOKEN: String = "8568264693:AAHtwSa7JDixDdKb9vs4P53rfngOGWpmMOc"

    // 👉 ژمارەی Chat IDی فەرمی (Official Telegram Chat ID):
    const val TELEGRAM_CHAT_ID: String = "6012594959"
    var customChatId: String = ""

    const val TELEGRAM_USERNAME = "YourStudi0"
    const val WHATSAPP_NUMBER = "+9647853297471"
    const val EMAIL_ADDRESS = "yourstudio450@gmail.com"
    const val PHONE_NUMBER = "+9647853297471"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Send message directly to Telegram Bot API via HTTP POST (fetch)
     */
    suspend fun sendToTelegramBot(
        formattedMessageHtml: String,
        targetChatId: String = customChatId.ifBlank { TELEGRAM_CHAT_ID }
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val chatId = targetChatId.ifBlank { customChatId }.ifBlank { TELEGRAM_CHAT_ID }.trim()
        if (chatId.isBlank()) {
            return@withContext Pair(false, "TELEGRAM_CHAT_ID دیاری نەکراوە")
        }

        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("text", formattedMessageHtml)
                put("parse_mode", "HTML")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val resBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Pair(true, "پەیامەکە بە سەرکەوتوویی بۆ بۆتی تیلیگرام نێردرا")
            } else {
                Pair(false, "هەڵە لە بۆتی تیلیگرام: ${response.code}")
            }
        } catch (e: Exception) {
            Pair(false, "هەڵەی پەیوەندی بە بۆت: ${e.localizedMessage}")
        }
    }

    /**
     * Sends an actual file (Blob/File from Uri) to Telegram Bot API via multipart/form-data.
     * Uses:
     * - sendVideo for video files (mp4, mov, mkv, avi)
     * - sendPhoto for standard image files (jpg, png, webp) if not too large
     * - sendDocument for larger files, svg, or general documents to preserve 100% original quality
     */
    suspend fun sendMediaFileToTelegramBot(
        context: Context,
        fileUri: Uri,
        fileName: String,
        caption: String,
        targetChatId: String = customChatId.ifBlank { TELEGRAM_CHAT_ID }
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val chatId = targetChatId.ifBlank { customChatId }.ifBlank { TELEGRAM_CHAT_ID }.trim()
        if (chatId.isBlank()) {
            return@withContext Pair(false, "TELEGRAM_CHAT_ID دیاری نەکراوە")
        }

        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
            val inputStream = contentResolver.openInputStream(fileUri)
                ?: return@withContext Pair(false, "نەتوانرا فایلەکە بخوێندرێتەوە")

            val fileBytes = inputStream.use { it.readBytes() }
            val fileSizeMb = fileBytes.size / (1024.0 * 1024.0)

            val lowerName = fileName.lowercase()
            val isVideo = lowerName.endsWith(".mp4") || lowerName.endsWith(".mov") ||
                    lowerName.endsWith(".mkv") || lowerName.endsWith(".avi") ||
                    mimeType.startsWith("video/")
            val isPhoto = lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                    lowerName.endsWith(".png") || lowerName.endsWith(".webp") ||
                    mimeType.startsWith("image/")

            // Decide API method based on media type and file size
            // Telegram Bot API limit: 50MB for bots. For photos, sendPhoto max is 10MB; above that or for SVG use sendDocument
            val (apiMethod, fieldName) = when {
                isVideo -> {
                    if (fileSizeMb > 48) Pair("sendDocument", "document") else Pair("sendVideo", "video")
                }
                isPhoto && !lowerName.endsWith(".svg") && fileSizeMb <= 10 -> Pair("sendPhoto", "photo")
                else -> Pair("sendDocument", "document")
            }

            val requestFileBody = okhttp3.RequestBody.create(mimeType.toMediaType(), fileBytes)

            val multipartBody = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("parse_mode", "HTML")
                .addFormDataPart("caption", caption.take(1024))
                .addFormDataPart(fieldName, fileName, requestFileBody)
                .build()

            val request = Request.Builder()
                .url("https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/$apiMethod")
                .post(multipartBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val resBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Pair(true, "فایلی $fileName بە سەرکەوتوویی بۆ بۆتی تیلیگرام نێردرا")
            } else {
                // Fallback: If sendPhoto/sendVideo failed, try as sendDocument
                if (apiMethod != "sendDocument") {
                    val fallbackBody = okhttp3.MultipartBody.Builder()
                        .setType(okhttp3.MultipartBody.FORM)
                        .addFormDataPart("chat_id", chatId)
                        .addFormDataPart("parse_mode", "HTML")
                        .addFormDataPart("caption", caption.take(1024))
                        .addFormDataPart("document", fileName, requestFileBody)
                        .build()
                    val fallbackReq = Request.Builder()
                        .url("https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendDocument")
                        .post(fallbackBody)
                        .build()
                    val fbResponse = httpClient.newCall(fallbackReq).execute()
                    if (fbResponse.isSuccessful) {
                        return@withContext Pair(true, "فایلەکە بە سەرکەوتوویی (document) نێردرا")
                    }
                }
                Pair(false, "هەڵە لە ناردنی فایل: ${response.code}")
            }
        } catch (e: Exception) {
            Pair(false, "هەڵە لە ناردنی فایلی $fileName: ${e.localizedMessage}")
        }
    }

    /**
     * Complete dispatcher for orders with one or multiple files:
     * Dispatches order details and uploads each selected file sequentially to the bot.
     */
    suspend fun sendOrderWithFilesToTelegram(
        context: Context,
        projectName: String,
        userName: String,
        requestType: String,
        category: String,
        notes: String,
        primaryFileUri: Uri? = null,
        primaryFileName: String = "",
        sampleFileUri: Uri? = null,
        sampleFileName: String = ""
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val hasAnyFile = (primaryFileUri != null && primaryFileName.isNotBlank()) ||
                (sampleFileUri != null && sampleFileName.isNotBlank())

        val baseCaption = """
            🚀 <b>داواکاری نوێ بۆ دروستکردن (Manual Order)</b>
            ━━━━━━━━━━━━━━━━━━
            📌 <b>ناوی پڕۆجێکت:</b> ${escapeHtml(projectName)}
            👤 <b>ناوی بەکارهێنەر:</b> ${escapeHtml(userName)}
            📁 <b>جۆری داواکاری:</b> ${escapeHtml(requestType)}
            🏷 <b>بەش:</b> ${escapeHtml(category)}
            📝 <b>تێبینییەکان و ڕێنمایی:</b>
            ${escapeHtml(notes)}
            📅 <b>بەروار:</b> ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}
            ━━━━━━━━━━━━━━━━━━
            <i>نێردراوە لە ئەپی YourStudio</i>
        """.trimIndent()

        var atLeastOneSuccess = false

        // 1. If primary file exists, send it with full caption
        if (primaryFileUri != null && primaryFileName.isNotBlank()) {
            val caption = "$baseCaption\n🎬 <b>فایلی سەرەکی:</b> ${escapeHtml(primaryFileName)}"
            val res = sendMediaFileToTelegramBot(
                context = context,
                fileUri = primaryFileUri,
                fileName = primaryFileName,
                caption = caption
            )
            if (res.first) atLeastOneSuccess = true
        }

        // 2. If sample file exists, send it
        if (sampleFileUri != null && sampleFileName.isNotBlank()) {
            val sampleCaption = if (primaryFileUri != null) {
                "📎 <b>نموونەی ستایل / فایلی دووەم:</b> ${escapeHtml(sampleFileName)}\n📌 پڕۆجێکت: ${escapeHtml(projectName)} (${escapeHtml(userName)})"
            } else {
                "$baseCaption\n📎 <b>فایلی هاوپێچکراو:</b> ${escapeHtml(sampleFileName)}"
            }
            val res = sendMediaFileToTelegramBot(
                context = context,
                fileUri = sampleFileUri,
                fileName = sampleFileName,
                caption = sampleCaption
            )
            if (res.first) atLeastOneSuccess = true
        }

        // 3. If no physical files were attached (e.g. text-only instructions), send text message
        if (!hasAnyFile) {
            val textHtml = formatOrderMessageHtml(
                projectName = projectName,
                userName = userName,
                requestType = requestType,
                category = category,
                notes = notes,
                sampleFile = sampleFileName,
                primaryFile = primaryFileName
            )
            val res = sendToTelegramBot(textHtml)
            return@withContext res
        }

        return@withContext Pair(atLeastOneSuccess, if (atLeastOneSuccess) "داواکاری و فایلەکان بە سەرکەوتوویی بۆ بۆتی تیلیگرام نێردران" else "نەتوانرا فایلەکان بنێردرێن بۆ بۆت")
    }

    fun formatOrderMessageHtml(
        projectName: String,
        userName: String,
        requestType: String,
        category: String,
        notes: String,
        sampleFile: String = "",
        primaryFile: String = ""
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return """
            🚀 <b>داواکاری نوێ بۆ دروستکردن (Manual Order)</b>
            ━━━━━━━━━━━━━━━━━━
            📌 <b>ناوی پڕۆجێکت:</b> ${escapeHtml(projectName)}
            👤 <b>ناوی بەکارهێنەر:</b> ${escapeHtml(userName)}
            📁 <b>جۆری داواکاری:</b> ${escapeHtml(requestType)}
            🏷 <b>بەش:</b> ${escapeHtml(category)}
            📝 <b>تێبینییەکان و ڕێنمایی:</b>
            ${escapeHtml(notes)}
            ${if (sampleFile.isNotBlank()) "📎 <b>نموونەی فایل:</b> ${escapeHtml(sampleFile)}\n" else ""}${if (primaryFile.isNotBlank()) "🎬 <b>فایلی سەرەکی:</b> ${escapeHtml(primaryFile)}\n" else ""}📅 <b>بەروار:</b> $dateStr
            ━━━━━━━━━━━━━━━━━━
            <i>نێردراوە لە ئەپی YourStudio</i>
        """.trimIndent()
    }

    fun formatContactMessageHtml(
        userName: String,
        subject: String,
        messageBody: String
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return """
            📩 <b>نامەی نوێ لە بەشی پەیوەندی (Contact)</b>
            ━━━━━━━━━━━━━━━━━━
            👤 <b>ناوی بەکارهێنەر:</b> ${escapeHtml(userName)}
            📌 <b>بابەت:</b> ${escapeHtml(subject.ifBlank { "پەیوەندی گشتی" })}
            💬 <b>دەقی نامە:</b>
            ${escapeHtml(messageBody)}
            📅 <b>بەروار و کات:</b> $dateStr
            ━━━━━━━━━━━━━━━━━━
            <i>نێردراوە لە ئەپی YourStudio</i>
        """.trimIndent()
    }

    fun formatPaymentReceiptHtml(
        userName: String,
        packageName: String,
        amount: String,
        gateway: String,
        receiptNumber: String,
        notes: String = ""
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return """
            💳 <b>وەسڵی پارەدانی نوێ (Payment Receipt)</b>
            ━━━━━━━━━━━━━━━━━━
            👤 <b>ناوی بەکارهێنەر:</b> ${escapeHtml(userName)}
            🎁 <b>پاکێج / داواکاری:</b> ${escapeHtml(packageName)}
            💰 <b>بڕی پارە:</b> ${escapeHtml(amount)}
            🏦 <b>دەروازەی پارەدان:</b> ${escapeHtml(gateway)}
            🧾 <b>ژمارەی وەسڵ / ترانزاکشن:</b> <code>${escapeHtml(receiptNumber)}</code>
            ${if (notes.isNotBlank()) "📝 <b>تێبینی / ڕەسمی وەسڵ:</b> ${escapeHtml(notes)}\n" else ""}📅 <b>بەروار:</b> $dateStr
            ━━━━━━━━━━━━━━━━━━
            <i>تکایە پشکنین بکە بۆ پشتڕاستکردنەوە</i>
        """.trimIndent()
    }

    fun formatOtpMessageHtml(
        userName: String,
        credential: String,
        isPhone: Boolean,
        isRegister: Boolean,
        code: String
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val purpose = if (isRegister) "دروستکردنی هەژماری نوێ" else "چوونەژوورەوە بۆ هەژمار"
        val methodType = if (isPhone) "ژمارەی مۆبایل" else "ناونیشانی ئیمەیڵ"
        return """
            🔐 <b>کۆدی دڵنیابوونەوەی هەژمار (Verification OTP)</b>
            ━━━━━━━━━━━━━━━━━━
            👤 <b>ناوی بەکارهێنەر:</b> ${escapeHtml(userName.ifBlank { "بەکارهێنەر" })}
            📱 <b>$methodType:</b> <code>${escapeHtml(credential)}</code>
            🔢 <b>کۆدی دڵنیابوونەوە (OTP):</b> <code>$code</code>
            🏷 <b>مەبەست:</b> $purpose
            📅 <b>کات:</b> $dateStr
            ━━━━━━━━━━━━━━━━━━
            <i>تکایە ئەم کۆدە لەناو کێڵگەی OTP لە ئەپەکەدا بنووسە بۆ چالاککردنی هەژمارەکەت.</i>
        """.trimIndent()
    }

    suspend fun sendOtpToTelegram(
        userName: String,
        credential: String,
        isPhone: Boolean,
        isRegister: Boolean,
        code: String
    ): Pair<Boolean, String> {
        val html = formatOtpMessageHtml(userName, credential, isPhone, isRegister, code)
        return sendToTelegramBot(html)
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    fun openTelegramDirect(context: Context, prefilledMessage: String = "") {
        try {
            val encodedMsg = URLEncoder.encode(prefilledMessage, "UTF-8")
            val tgUri = if (prefilledMessage.isNotBlank()) {
                Uri.parse("https://t.me/$TELEGRAM_USERNAME?text=$encodedMsg")
            } else {
                Uri.parse("https://t.me/$TELEGRAM_USERNAME")
            }
            val intent = Intent(Intent.ACTION_VIEW, tgUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تیلیگرام دەکرێتەوە لە وێبگەڕ...", Toast.LENGTH_SHORT).show()
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://t.me/$TELEGRAM_USERNAME")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(webIntent)
        }
    }

    fun openWhatsApp(context: Context, prefilledMessage: String = "") {
        try {
            val cleanPhone = WHATSAPP_NUMBER.replace("+", "").replace(" ", "")
            val encodedMsg = URLEncoder.encode(prefilledMessage, "UTF-8")
            val url = "https://wa.me/$cleanPhone?text=$encodedMsg"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "نەتوانرا واتساپ بکرێتەوە", Toast.LENGTH_SHORT).show()
        }
    }

        fun sendEmail(context: Context, prefilledMessage: String = "") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$EMAIL_ADDRESS")
                putExtra(Intent.EXTRA_SUBJECT, "پەیوەندی لە ڕێگەی ئەپی YourStudio")
                if (prefilledMessage.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, prefilledMessage)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ئیمەیڵ: $EMAIL_ADDRESS", Toast.LENGTH_LONG).show()
        }
    }

    fun dialPhone(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$PHONE_NUMBER")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ژمارەی پەیوەندی: $PHONE_NUMBER", Toast.LENGTH_LONG).show()
        }
    }

    fun formatOrderMessage(
        type: String,
        category: String,
        projectName: String,
        userName: String,
        details: String,
        sampleFile: String,
        primaryFile: String = ""
    ): String {
        return """
            🎨 داواکاری نوێ بۆ دروستکردن لە ئەپەوە:
            --------------------------------
            📁 جۆر: $type
            🏷 بەش: $category
            📌 ناوی پرۆجێکت: $projectName
            👤 بەکارهێنەر: $userName
            📝 زانیاری و ڕێنمایی:
            $details
            ${if (sampleFile.isNotBlank()) "📎 نموونە: $sampleFile" else ""}
            ${if (primaryFile.isNotBlank()) "🎬 فایلی سەرەکی ڤیدیۆ: $primaryFile" else ""}
            ⏰ کات: ئێستا
            --------------------------------
        """.trimIndent()
    }
}
