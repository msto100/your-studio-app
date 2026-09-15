package com.example.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.OPENAI_API_KEY
import com.example.ui.StudioViewModel
import com.example.ui.components.StudioHeader
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.TelegramColor
import com.example.ui.theme.WhatsAppColor
import com.example.util.TelegramHelper

@Composable
fun ContactScreen(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()

    var senderName by remember { mutableStateOf(userProfile?.username ?: "") }
    var messageSubject by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }
    var isSendingToBot by remember { mutableStateOf(false) }
    var showApiConfigDialog by remember { mutableStateOf(false) }

    var inputChatId by remember { mutableStateOf(TelegramHelper.customChatId.ifBlank { TelegramHelper.TELEGRAM_CHAT_ID }) }
    var inputOpenAiKey by remember { mutableStateOf(OPENAI_API_KEY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 85.dp)
    ) {
        StudioHeader(
            title = "پەیوەندی - Contact",
            subtitle = "پەیوەندی ڕاستەوخۆ بە دیزاینەر و بۆتی فەرمی تیلیگرام"
        )


        // 3 Contact Channels: Telegram, WhatsApp, Direct Phone
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ڕێگاکانی پەیوەندیکردن",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Telegram
                ContactChannelItem(
                    title = "تیلیگرام (Telegram)",
                    subtitle = "@${TelegramHelper.TELEGRAM_USERNAME}",
                    iconColor = TelegramColor,
                    onClick = {
                        TelegramHelper.openTelegramDirect(context, "سڵاو دیزاینەر، دەمەوێت پەیوەندیت پێوە بکەم دەربارەی دیزاین...")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // WhatsApp
                ContactChannelItem(
                    title = "واتساپ (WhatsApp)",
                    subtitle = TelegramHelper.WHATSAPP_NUMBER,
                    iconColor = WhatsAppColor,
                    onClick = {
                        TelegramHelper.openWhatsApp(context, "سڵاو دیزاینەر، دەمەوێت پەیوەندیت پێوە بکەم دەربارەی دیزاین...")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Email
                ContactChannelItem(
                    title = "نامە لە ڕێگەی ئیمەیڵ (Email)",
                    subtitle = TelegramHelper.EMAIL_ADDRESS,
                    iconColor = StudioPrimary,
                    isEmail = true,
                    onClick = {
                        TelegramHelper.sendEmail(context, "سڵاو دیزاینەر، دەمەوێت پەیوەندیت پێوە بکەم دەربارەی دیزاین...")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct in-app message form to send straight to creator's Telegram
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        tint = TelegramColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ناردنی نامە بە فەرمی بۆ بۆتی تیلیگرام",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ئەم نامەیە لە ڕێگەی Telegram Bot API ڕاستەوخۆ دەگاتە دەستی دیزاینەر بە شێوازی فۆرماتکراو.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("ناوی تۆ") },
                    placeholder = { Text("ناوی تەواوت لێرە بنووسە") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = messageSubject,
                    onValueChange = { messageSubject = it },
                    label = { Text("بابەتی پەیوەندی") },
                    placeholder = { Text("نموونە: پرسیار دەربارەی پاکێجی لۆگۆ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = messageBody,
                    onValueChange = { messageBody = it },
                    label = { Text("دەقی نامەکەت") },
                    placeholder = { Text("لێرە پەیامەکەت بنووسە...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (messageBody.isBlank()) {
                            viewModel.showToast("تکایە دەقی نامەکەت بنووسە")
                            return@Button
                        }
                        isSendingToBot = true
                        viewModel.sendContactMessageToBot(
                            senderName = senderName.ifBlank { userProfile?.username ?: "بەکارهێنەر" },
                            subject = messageSubject,
                            messageBody = messageBody,
                            onFinished = {
                                isSendingToBot = false
                                messageBody = ""
                                messageSubject = ""
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramColor),
                    enabled = !isSendingToBot
                ) {
                    if (isSendingToBot) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ناردن بۆ بۆت...")
                    } else {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ناردنی نامە بە فەرمی بۆ بۆت",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional direct telegram client button
                OutlinedButton(
                    onClick = {
                        if (messageBody.isBlank()) {
                            viewModel.showToast("تکایە دەقی نامەکەت بنووسە")
                            return@OutlinedButton
                        }
                        val formattedMsg = """
                            📩 نامەی نوێ لە ئەپەوە:
                            --------------------------------
                            👤 لە لایەن: $senderName
                            📌 بابەت: ${messageSubject.ifBlank { "پەیوەندی گشتی" }}
                            💬 دەقی نامە:
                            $messageBody
                            --------------------------------
                        """.trimIndent()
                        TelegramHelper.openTelegramDirect(context, formattedMsg)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("یان کردنەوە لە ئەپی تیلیگرام", fontSize = 13.sp)
                }
            }
        }
    }

    // API & Bot Configuration Dialog
    if (showApiConfigDialog) {
        AlertDialog(
            onDismissRequest = { showApiConfigDialog = false },
            title = {
                Text(
                    text = "ڕێکخستنی Bot API و OpenAI",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تۆکنی فەرمی بۆت بە سەرکەوتوویی بەستراوەتەوە:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = TelegramHelper.TELEGRAM_BOT_TOKEN,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputChatId,
                        onValueChange = { inputChatId = it },
                        label = { Text("TELEGRAM_CHAT_ID") },
                        placeholder = { Text("ئایدی چاتەکەت (نموونە: 123456789)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputOpenAiKey,
                        onValueChange = { inputOpenAiKey = it },
                        label = { Text("OPENAI_API_KEY") },
                        placeholder = { Text("sk-...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        TelegramHelper.customChatId = inputChatId.trim()
                        OPENAI_API_KEY = inputOpenAiKey.trim()
                        viewModel.showToast("ڕێکخستنەکان پاشەکەوت کران!")
                        showApiConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald)
                ) {
                    Text("پاشەکەوتکردن")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApiConfigDialog = false }) {
                    Text("داخستن")
                }
            }
        )
    }
}

@Composable
fun ContactChannelItem(
    title: String,
    subtitle: String,
    iconColor: Color,
    isEmail: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        title.contains("تەلەفۆن") -> Icons.Default.Call
                        title.contains("واتساپ") -> Icons.Default.Message
                        else -> Icons.Default.Send
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = iconColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "کردنەوە",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = iconColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
