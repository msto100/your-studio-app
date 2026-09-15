package com.example.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.LogoAiResult
import com.example.ui.StudioViewModel
import com.example.ui.components.SampleFileSelector
import com.example.ui.theme.GoldGradientEnd
import com.example.ui.theme.GoldGradientStart
import com.example.ui.theme.StudioAccentPink
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.util.KurdishCanvasHelper
import com.example.util.TelegramHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoCreationSheet(
    category: String,
    viewModel: StudioViewModel,
    userLoggedIn: Boolean,
    isUnlimited: Boolean,
    hasUnlimitedAiLogos: Boolean,
    defaultUsername: String,
    isGenerating: Boolean,
    generatedResult: LogoAiResult?,
    retryCount: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var sampleFile by remember { mutableStateOf("") }
    var sampleFileUri by remember { mutableStateOf<Uri?>(null) }
    var detailsText by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf(defaultUsername) }

    // Dialogs state
    var showMethodChoiceDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }

    // Kurdish Typography & Quick Customization Canvas state
    val coroutineScope = rememberCoroutineScope()
    var kurdishProjectName by remember(generatedResult, projectName) { mutableStateOf(projectName) }
    var selectedFontId by remember { mutableStateOf("vazirmatn") }
    var fontDropdownExpanded by remember { mutableStateOf(false) }
    var kurdishFontSize by remember { mutableFloatStateOf(38f) }
    var positionBelow by remember { mutableStateOf(true) }
    var composedLogoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var baseDownloadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isComposingCanvas by remember { mutableStateOf(false) }
    var showManualOrderDialog by remember { mutableStateOf(false) }
    var manualCalligraphyNotes by remember { mutableStateOf("") }

    LaunchedEffect(generatedResult) {
        baseDownloadedBitmap = null
    }

    LaunchedEffect(generatedResult, kurdishProjectName, selectedFontId, kurdishFontSize, positionBelow) {
        if (generatedResult != null) {
            // Debounce rapid input typing to avoid heavy bitmap thrashing and UI freeze
            kotlinx.coroutines.delay(250L)
            isComposingCanvas = true
            try {
                withContext(Dispatchers.IO) {
                    val baseBmp = if (!generatedResult.imageUrl.isNullOrBlank()) {
                        baseDownloadedBitmap ?: KurdishCanvasHelper.fetchBitmapFromUrl(generatedResult.imageUrl)
                            ?.also { baseDownloadedBitmap = it }
                    } else {
                        null
                    }

                    val rendered = KurdishCanvasHelper.renderLogoWithKurdishText(
                        context = context,
                        baseBitmap = baseBmp,
                        category = category,
                        kurdishText = kurdishProjectName.ifBlank { projectName },
                        fontId = selectedFontId,
                        subtitle = "",
                        primaryColorHex = generatedResult.primaryColor,
                        accentColorHex = generatedResult.accentColor,
                        textSizeSp = kurdishFontSize,
                        textColorInt = android.graphics.Color.WHITE,
                        positionBelow = positionBelow
                    )
                    withContext(Dispatchers.Main) {
                        composedLogoBitmap = rendered
                        isComposingCanvas = false
                    }
                }
            } catch (e: Exception) {
                isComposingCanvas = false
            }
        }
    }

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
            // Header with exact requested text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ئێستا تۆ لە بەشی لۆگۆی $category فەرمو زانیاریەکان تۆمار کە",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "داخستن")
                }
            }

            Text(
                text = "لۆگۆکەت دروست دەکرێ بە باشترین کواڵێتی بە ئارەزووی خۆت",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. فایل بۆ نمونەی لۆگۆ (ارەزوو ماندانە )
            SampleFileSelector(
                label = "فایل بۆ نمونەی لۆگۆ (ئارەزوومەندانە)",
                selectedFile = sampleFile,
                allowedExtensions = "png, jpg, webp, svg",
                onFileSelected = { sampleFile = it },
                onFileUriSelected = { sampleFileUri = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. زانیاریەکانی تایبەت بە لۆگۆکە تۆمار بکە
            OutlinedTextField(
                value = detailsText,
                onValueChange = { detailsText = it },
                label = { Text("زانیاریەکانی تایبەت بە لۆگۆکە تۆمار بکە") },
                placeholder = { Text("باسی شێواز، ڕەنگ، ئارەزوو، کۆنسێپت یان بیرۆکە بکە...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. ناوی پرۆجێکت
            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("ناوی پرۆجێکت") },
                placeholder = { Text("نموونە: Apex Media, Zana Coffee...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. ناوی بەکار هێنەر
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("ناوی بەکار هێنەر") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. دروست کردن ( 1 credit) بە ڕەنگی جیاواز و جوانتر
            Button(
                onClick = {
                    if (projectName.isBlank()) {
                        viewModel.showToast("تکایە ناوی پرۆجێکت بنووسە")
                        return@Button
                    }
                    showMethodChoiceDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioPrimary
                )
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "دروست کردن ( 1 credit)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Method Choice: "دروست کردنی خێرا" vs "دروستکردنی دەستی"
    if (showMethodChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showMethodChoiceDialog = false },
            title = {
                Text(
                    text = "شێوازی دروستکردنی لۆگۆ هەڵبژێرە",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "دەتوانیت ڕاستەوخۆ بە ژیری دەستکرد یان بە دیزاینی تایبەتی دەستی داوا بکەیت:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: دروست کردنی خێرا (AI)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, StudioSecondary, RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Button(
                            onClick = {
                                showMethodChoiceDialog = false
                                viewModel.startLogoCreation(
                                    category = category,
                                    projectName = projectName,
                                    userName = userName,
                                    details = detailsText,
                                    sampleFileName = sampleFile,
                                    isAiFast = true
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = StudioSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دروست کردنی خێرا (AI)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: دروستکردنی دەستی
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, StudioAccentPink, RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Button(
                                onClick = {
                                    showMethodChoiceDialog = false
                                    viewModel.startLogoCreation(
                                        category = category,
                                        projectName = projectName,
                                        userName = userName,
                                        details = detailsText,
                                        sampleFileName = sampleFile,
                                        isAiFast = false,
                                        sampleFileUri = sampleFileUri,
                                        onDispatchedToTelegram = { orderMsg ->
                                            TelegramHelper.openTelegramDirect(context, orderMsg)
                                        }
                                    )
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioAccentPink),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Brush, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "دروستکردنی دەستی",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Note underneath
                            Text(
                                text = "ئەوەیان کاتی زیاتری دەوێ !",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = StudioAccentPink,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showMethodChoiceDialog = false }) {
                    Text("پاشگەزبوونەوە")
                }
            }
        )
    }

    // Generated AI Logo Result Review Dialog ("لۆگۆکە چۆنە بە دڵە ؟")
    if (isGenerating) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("دروستکردنی لۆگۆ بە ژیری دەستکرد...", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = StudioSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("کەمێک چاوەڕوان بە، ژیری دەستکرد لۆگۆیەکی ناوازە بۆ $projectName دیزاین دەکات...")
                }
            },
            confirmButton = {}
        )
    }

    if (generatedResult != null && !isGenerating) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoReview() },
            title = {
                Text(
                    text = "پێشبینینی لۆگۆ بە فۆنتی کوردی",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preview of the rendered canvas with high-precision Kurdish typography
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF111827)) // bg-gray-900
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (composedLogoBitmap != null) {
                            Image(
                                bitmap = composedLogoBitmap!!.asImageBitmap(),
                                contentDescription = "پێشبینینی لۆگۆ و دەقی کوردی",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (!generatedResult.imageUrl.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model = generatedResult.imageUrl,
                                contentDescription = "پێشبینینی لۆگۆی Pollinations AI",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (isComposingCanvas && composedLogoBitmap == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF111827).copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = StudioEmerald, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "نەخشاندنی فۆنتی کوردی لەسەر وێنە...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // بەشی دەستکاری خێرای دەقی کوردی
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    tint = StudioEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "دەستکاری خێرای دەقی کوردی",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 1. گۆڕینی دەق ئەگەر هەڵەی تێدابوو
                            OutlinedTextField(
                                value = kurdishProjectName,
                                onValueChange = { kurdishProjectName = it },
                                label = { Text("ناوی پڕۆژە بە کوردی") },
                                placeholder = { Text("دەقی سەر وێنە بنووسە...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. هەڵبژاردنی شێوازی فۆنت (20 فۆنتی کوردی و عەرەبی)
                            val currentFont = com.example.util.KurdishFontHelper.kurdishFonts.find { it.id == selectedFontId }
                                ?: com.example.util.KurdishFontHelper.kurdishFonts.first()

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FontDownload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("هەڵبژاردنی شێوازی فۆنت:", style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { fontDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${currentFont.kurdishName} (${currentFont.name})",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = currentFont.styleDescription,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = fontDropdownExpanded,
                                        onDismissRequest = { fontDropdownExpanded = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(360.dp)
                                    ) {
                                        com.example.util.KurdishFontHelper.kurdishFonts.forEach { font ->
                                            val isCurrent = font.id == selectedFontId
                                            DropdownMenuItem(
                                                text = {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = font.kurdishName,
                                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isCurrent) StudioEmerald else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "(${font.name})",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        Text(
                                                            text = font.styleDescription,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                                            color = MaterialTheme.colorScheme.outline
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedFontId = font.id
                                                    fontDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. قەبارەی دەقەکە (Font Size)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FormatSize,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("قەبارەی فۆنت:", style = MaterialTheme.typography.labelMedium)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        Pair("بچووک", 28f),
                                        Pair("مامناوەند", 38f),
                                        Pair("گەورە", 48f)
                                    ).forEach { (label, size) ->
                                        val isSelected = kurdishFontSize == size
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { kurdishFontSize = size }
                                        ) {
                                            Text(
                                                text = label,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. شوێنی دەقەکە (لەژێر لۆگۆ / لەسەر وێنەکە)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("شوێنی دەق:", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        Pair("لەژێر لۆگۆ", true),
                                        Pair("لەسەر وێنەکە", false)
                                    ).forEach { (label, below) ->
                                        val isSelected = positionBelow == below
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) StudioEmerald else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) StudioEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { positionBelow = below }
                                        ) {
                                            Text(
                                                text = label,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لۆگۆکە چۆنە بە دڵە ؟",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                // Button 1: بەڵێ بە دڵمە (پاشەکەوتکردنی وێنەی نەخشێنراو بە دەقی کوردی)
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val bmp = composedLogoBitmap
                            val savedFile = if (bmp != null) {
                                KurdishCanvasHelper.saveBitmapToInternalStorage(context, bmp, projectName.ifBlank { "kurdish_logo" })
                            } else null

                            viewModel.acceptGeneratedLogo(savedFile?.absolutePath ?: generatedResult.imageUrl)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بەڵێ بە دڵمە")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Button 2: ناردن بۆ دروستکردنی دەستی
                    OutlinedButton(
                        onClick = {
                            showManualOrderDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("دروستکردنی دەستی")
                    }

                    // Button 3: نەخێر بە دڵم نیە بیگۆڕە
                    OutlinedButton(
                        onClick = {
                            showFeedbackDialog = true
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بیگۆڕە")
                    }
                }
            }
        )
    }

    // Manual Custom Design Dispatch Dialog
    if (showManualOrderDialog) {
        AlertDialog(
            onDismissRequest = { showManualOrderDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Brush, contentDescription = null, tint = StudioPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ناردن بۆ دیزاینەری دەستی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StudioPrimary,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ئەگەر فۆنتێکی زۆر تایبەت و دیزاینی خۆشنووسی دەستیت دەوێت، داواکارییەکەت لەگەڵ ئەم وێنە بەراییە و دەقە کوردییەکە ڕەوانەی بۆتی تیلیگرام دەکرێت تا دیزاینەر بە دەستی بۆت دروست بکات.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = manualCalligraphyNotes,
                        onValueChange = { manualCalligraphyNotes = it },
                        label = { Text("تێبینی و ستایلی خۆشنووسی (ئارەزوومەندانە)") },
                        placeholder = { Text("نموونە: ستایلی خۆشنووسی نەستەعلیق یان کوفی مۆدێرن...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val bmp = composedLogoBitmap
                            val savedFile = if (bmp != null) {
                                KurdishCanvasHelper.saveBitmapToInternalStorage(context, bmp, "manual_ref_${projectName.ifBlank { "kurdish" }}")
                            } else null
                            val fileUri = if (savedFile != null) Uri.fromFile(savedFile) else null

                            viewModel.sendLogoForManualCustomDesign(
                                category = category,
                                projectName = projectName,
                                userName = userName,
                                kurdishText = kurdishProjectName.ifBlank { projectName },
                                details = manualCalligraphyNotes,
                                logoBitmapUri = fileUri,
                                onDispatchedToTelegram = { msg ->
                                    TelegramHelper.openTelegramDirect(context, msg)
                                }
                            )
                            showManualOrderDialog = false
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ناردن بۆ تیلیگرام")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showManualOrderDialog = false }) {
                    Text("پاشگەزبوونەوە")
                }
            }
        )
    }

    // Feedback Dialog for regenerating logo
    if (showFeedbackDialog) {
        val hasUnlimited = isUnlimited || hasUnlimitedAiLogos
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = {
                Text(
                    text = "گۆڕینی لۆگۆ بەپێی ڕێنمایی نوێ",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // Notice: 5 times limit or unlimited
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = StudioPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasUnlimited) {
                                    "بەهۆی هەبوونی پاکێجی باڵا (٢٠$ یان ٣٥$)، لۆگۆی ئەی ئای بێ سنورە!"
                                } else {
                                    "ئاگاداری: 5 جار بۆت هەیە لۆگۆی نوێ دروست کەی ($retryCount/5)"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // بەشێکی بەتاڵ بێ و نوسرابێ (زانیاری نوێ)
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("زانیاری نوێ") },
                        placeholder = { Text("چ شتێک دەستکاری بکرێت؟ ڕەنگ، فۆنت، یان سیمبول؟") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                // بە ڕەنگێکی تر نوسرابێ (لۆگۆی نوێ دروست بکە)
                Button(
                    onClick = {
                        showFeedbackDialog = false
                        viewModel.requestNewLogoIteration(
                            newInstructions = feedbackText,
                            category = category,
                            projectName = projectName
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioAccentPink)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("لۆگۆی نوێ دروست بکە", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showFeedbackDialog = false }) {
                    Text("پاشگەزبوونەوە")
                }
            }
        )
    }
}
