package com.example.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudioViewModel
import com.example.ui.components.SampleFileSelector
import com.example.ui.theme.StudioSecondary
import com.example.util.TelegramHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCreationSheet(
    category: String,
    viewModel: StudioViewModel,
    defaultUsername: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var mainVideoFile by remember { mutableStateOf("") }
    var mainVideoUri by remember { mutableStateOf<Uri?>(null) }

    var sampleVideoFile by remember { mutableStateOf("") }
    var sampleVideoUri by remember { mutableStateOf<Uri?>(null) }

    var detailsText by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf(defaultUsername) }
    var isSending by remember { mutableStateOf(false) }

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
                    text = "ئێستا تۆ لە بەشی $category فەرمو زانیاریەکان تۆمار کە",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = StudioSecondary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "داخستن")
                }
            }

            Text(
                text = "هەموو ئەم بەشانە پێویستی بە ڤیدیۆی خۆتە ئێمە تەنها بۆتی ئیدیت دەکەین بە باشترین کواڵێتی و بە ئارەزووی خۆت",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. ڤیدیۆی سەرەکی بۆ ئیدیت (mp4, mkv, mov, avi, video/*)
            SampleFileSelector(
                label = "فایلی سەرەکی ڤیدیۆ بۆ ئیدیت (mp4, mkv, mov, avi)",
                selectedFile = mainVideoFile,
                allowedExtensions = "video/*,video/mp4,video/quicktime,video/x-matroska,.mkv,.mov,.avi",
                onFileSelected = { mainVideoFile = it },
                onFileUriSelected = { mainVideoUri = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. نمونەی ڤیدیۆ (ئارەزوومەندانە)
            SampleFileSelector(
                label = "فایل بۆ نمونەی ئیدیت / ستایل (ئارەزوومەندانە)",
                selectedFile = sampleVideoFile,
                allowedExtensions = "video/*,video/mp4,video/quicktime,video/x-matroska,.mkv,.mov,.avi",
                onFileSelected = { sampleVideoFile = it },
                onFileUriSelected = { sampleVideoUri = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. زانیاریەکانی تایبەت بە ئیدیتی ڤیدیۆکە
            OutlinedTextField(
                value = detailsText,
                onValueChange = { detailsText = it },
                label = { Text("زانیاریەکانی تایبەت بە ڤیدیۆکە تۆمار بکە") },
                placeholder = { Text("باسی بڕین، ڕەنگ، ژێرنوس، دەنگ، گۆرانی، کاریگەری تایبەت بکە...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. ناوی پرۆجێکت
            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("ناوی پرۆجێکت") },
                placeholder = { Text("نموونە: Vlog Highlight, Reels Promo...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. ناوی بەکار هێنەر
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("ناوی بەکار هێنەر") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6. ناردن بۆ ئیدیت (1 credit)
            Button(
                onClick = {
                    if (projectName.isBlank()) {
                        viewModel.showToast("تکایە ناوی پرۆجێکت بنووسە")
                        return@Button
                    }
                    isSending = true
                    viewModel.submitVideoEditOrder(
                        category = category,
                        projectName = projectName,
                        userName = userName,
                        details = detailsText,
                        sampleVideoName = sampleVideoFile,
                        mainVideoName = mainVideoFile,
                        mainVideoUri = mainVideoUri,
                        sampleVideoUri = sampleVideoUri,
                        onDispatchedToTelegram = { orderMsg ->
                            TelegramHelper.openTelegramDirect(context, orderMsg)
                        }
                    )
                    onDismiss()
                },
                enabled = !isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudioSecondary)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ناردن بۆ ئیدیت ( 1 credit)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
