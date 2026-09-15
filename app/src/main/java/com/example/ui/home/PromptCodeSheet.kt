package com.example.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.ui.StudioViewModel
import com.example.ui.components.SampleFileSelector
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.util.TelegramHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptCodeSheet(
    viewModel: StudioViewModel,
    defaultUsername: String,
    isGenerating: Boolean,
    generatedCode: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var sampleImageFile by remember { mutableStateOf("") }
    var sampleImageUri by remember { mutableStateOf<Uri?>(null) }
    var projectName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf(defaultUsername) }
    var copiedToClipboard by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بەشی پرۆمۆ کۆدی وێنە (AI Prompt Code)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp
                    ),
                    color = StudioEmerald,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "داخستن")
                }
            }

            Text(
                text = "تەنها نمونەی وێنەکەت دابنێ؛ زیرەکی دەستکرد پرۆمپتێکی دەرهێنراو بە کواڵێتی Vision دەداتێ بە پارامیتەرەکانی ستایل، ڕووناکی و گۆشەی کامێرا.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. نمونەی وێنە (Image-to-Prompt via Vision)
            SampleFileSelector(
                label = "نمونەی وێنە (بۆ دروستکردنی پرۆمۆ کۆد بە Vision)",
                selectedFile = sampleImageFile,
                allowedExtensions = "png, jpg, webp, jpeg",
                onFileSelected = { sampleImageFile = it },
                onFileUriSelected = { sampleImageUri = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. ناوی پرۆجێکت
            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("ناوی پرۆجێکت / ستایل") },
                placeholder = { Text("نموونە: Cyberpunk Portrait, Vintage Luxury...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. ناوی بەکار هێنەر
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("ناوی بەکار هێنەر") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Button: وەرگرتنی پرۆمۆ کۆد (AI Prompt Code)
            Button(
                onClick = {
                    if (projectName.isBlank() && sampleImageFile.isBlank()) {
                        viewModel.showToast("تکایە وێنەی نموونە دابنێ یان ناوی پرۆجێکت بنووسە")
                        return@Button
                    }
                    viewModel.generateImagePromoCode(
                        projectName = projectName.ifBlank { "Cinematic Aesthetic" },
                        userName = userName,
                        sampleFileName = sampleImageFile,
                        sampleFileUri = sampleImageUri,
                        onDispatchedToTelegram = { orderMsg ->
                            TelegramHelper.openTelegramDirect(context, orderMsg)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "وەرگرتنی پرۆمۆ کۆد (AI Prompt Code)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (isGenerating) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("شیتاڵکردنی وێنە بە Vision API...", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = StudioEmerald)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("کەمێک چاوەڕوان بە، ژیری دەستکرد وێنەکەت شی دەکاتەوە و پرۆمپتێکی پرۆفیشناڵ بە تەواوی ڕێکدەخات...")
                }
            },
            confirmButton = {}
        )
    }

    if (generatedCode != null && !isGenerating) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPromoCodeResult() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = StudioEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("کۆدی ئامادەکراو (Code Block)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Modern Code Block Container
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A), // Dark terminal slate
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Code Block Header Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                }
                                Text(
                                    text = "AI_PROMPT.MD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = FontFamily.Monospace
                                )
                                // Fast Copy Code in Header
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StudioEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (copiedToClipboard) Icons.Default.Done else Icons.Default.ContentCopy,
                                        contentDescription = "کۆپیکردن",
                                        tint = StudioEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (copiedToClipboard) "کۆپی کرا" else "Copy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioEmerald
                                    )
                                }
                            }

                            // Code Block Body Text
                            Text(
                                text = generatedCode,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(14.dp),
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice card as requested by user
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StudioEmerald,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ئەم کۆدە کۆپی بکە و لە هەر سێرڤسێکی وەک Midjourney, ChatGPT, Bing یان Leonardo دایبنێ لەگەڵ وێنەی خۆت، ڕێک هەمان ئەم ستایلەت پێدەداتەوە.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                // Button: کۆپیکردنی کۆد (Copy Code)
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("AI Prompt Code", generatedCode)
                        clipboard.setPrimaryClip(clip)
                        copiedToClipboard = true
                        viewModel.showToast("کۆدەکە بە سەرکەوتوویی کۆپی کرا!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald)
                ) {
                    Icon(
                        imageVector = if (copiedToClipboard) Icons.Default.Done else Icons.Default.ContentCopy,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (copiedToClipboard) "کۆدەکە کۆپی کرا!" else "کۆپیکردنی کۆد (Copy Code)")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        viewModel.dismissPromoCodeResult()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("داخستن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
