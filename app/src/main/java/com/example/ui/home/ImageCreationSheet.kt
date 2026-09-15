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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudioViewModel
import com.example.ui.components.SampleFileSelector
import com.example.ui.theme.StudioTertiary
import com.example.util.TelegramHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCreationSheet(
    category: String,
    viewModel: StudioViewModel,
    defaultUsername: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var sampleImageFile by remember { mutableStateOf("") }
    var sampleImageUri by remember { mutableStateOf<Uri?>(null) }
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
                    color = StudioTertiary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "داخستن")
                }
            }

            Text(
                text = "لەم بەشە وێنەکانتان بۆ ئیدیت دەکرێ بە باشترین کواڵێتی بە ئارەزووی خۆت",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. نمونەی وێنە
            SampleFileSelector(
                label = "فایل بۆ وێنەی سەرەکی یان نمونە (ئارەزوومەندانە)",
                selectedFile = sampleImageFile,
                allowedExtensions = "png, jpg, jpeg, svg, webp",
                onFileSelected = { sampleImageFile = it },
                onFileUriSelected = { sampleImageUri = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. زانیاریەکانی تایبەت بە وێنەکە
            OutlinedTextField(
                value = detailsText,
                onValueChange = { detailsText = it },
                label = { Text("زانیاریەکانی تایبەت بە وێنەکە تۆمار بکە") },
                placeholder = { Text("باسی ڕەنگ، پاککردنەوەی باکگراوند، چاککردنی ڕووناکی، پۆرترێت...") },
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
                placeholder = { Text("نموونە: Product Retouch, Car Portrait...") },
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

            // 5. ناردن بۆ ئیدیت (1 credit)
            Button(
                onClick = {
                    if (projectName.isBlank()) {
                        viewModel.showToast("تکایە ناوی پرۆجێکت بنووسە")
                        return@Button
                    }
                    isSending = true
                    val isAi = category.contains("AI", ignoreCase = true)
                    viewModel.submitImageOrder(
                        category = category,
                        projectName = projectName,
                        userName = userName,
                        details = detailsText,
                        sampleImageName = sampleImageFile,
                        isAiFast = isAi,
                        sampleImageUri = sampleImageUri,
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
                colors = ButtonDefaults.buttonColors(containerColor = StudioTertiary)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ناردن بۆ دروستکردن ( 1 credit)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
