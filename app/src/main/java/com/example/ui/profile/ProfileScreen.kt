package com.example.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import android.content.Intent
import android.net.Uri
import com.example.data.model.CreationOrder
import com.example.ui.StudioViewModel
import com.example.ui.components.StudioHeader
import com.example.ui.admin.FullScreenAdminModal
import com.example.ui.theme.StudioAccentPink
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.StudioTertiary

@Composable
fun ProfileScreen(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val completedOrders by viewModel.completedOrders.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var selectedOrderDetails by remember { mutableStateOf<CreationOrder?>(null) }
    var showAdminModal by remember { mutableStateOf(false) }
    var showAgentModal by remember { mutableStateOf(false) }

    if (viewModel.triggerOpenAdminModal) {
        showAdminModal = true
        viewModel.triggerOpenAdminModal = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        StudioHeader(
            title = "پرۆفایلی من",
            subtitle = "زانیاری کەسی، ئامار، و بەرهەمەکانت",
            badgeText = if (userProfile?.isUnlimited == true) "ئەندامی VIP" else "کریدت: ${userProfile?.logoCredits ?: 0} لۆگۆ"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedButton(
                onClick = { showAdminModal = true },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioPrimary),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("چوونەژوورەوەی بەڕێوەبەر (Admin Login)", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    if (userProfile?.isAgent == true) {
                        showAdminModal = true
                    } else {
                        showAgentModal = true 
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.15f), contentColor = Color(0xFF00E5FF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                if (userProfile?.isAgent == true) {
                    Text("🚀 چوونەژوورەوە بۆ ژووری بریکار", fontWeight = FontWeight.Bold)
                } else {
                    Text("🔑 چوونەژوورەوەی ئادمین بە کۆد", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (userProfile?.isAgent == true && !userProfile?.agentId.isNullOrEmpty()) {
            val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("مۆڵەتی بەڕێوەبەر", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(userProfile?.agentId ?: "", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                    }
                    IconButton(onClick = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(userProfile?.agentId ?: "")) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF00E5FF))
                    }
                }
            }
        }

        // Profile Card: Avatar, Name, Age, Edit Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(StudioPrimary, StudioSecondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "وێنەی پرۆفایل",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userProfile?.username ?: "بەکار‌هێنەر",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تەمەن: ${userProfile?.age ?: 22} ساڵ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (userProfile?.phoneNumber?.isNotBlank() == true) {
                        Text(
                            text = "مۆبایل: ${userProfile?.phoneNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (userProfile?.email?.isNotBlank() == true) {
                        Text(
                            text = "ئیمەیل: ${userProfile?.email}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "دەستکاریکردنی پرۆفایل",
                            tint = StudioPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.logout(); viewModel.setTab(com.example.ui.AppTab.HOME) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "چوونە دەرەوە",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Stats Row: چەند ڤیدیۆ و چەند لۆگۆ و چەند وێنەت کڕیوە یاخود دروستت کردوە
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileStatCard(
                title = "لۆگۆ",
                count = userProfile?.totalLogosPurchasedOrCreated ?: 0,
                color = StudioPrimary,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                title = "ڤیدیۆ",
                count = userProfile?.totalVideosPurchasedOrCreated ?: 0,
                color = StudioSecondary,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                title = "وێنە",
                count = userProfile?.totalImagesPurchasedOrCreated ?: 0,
                color = StudioTertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs: 1. بەرهەمە تەواوکراوەکان | 2. لە چاوەڕوانیدا (خەریکە من دروستی دەکەم)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تەواوکراوەکان (${completedOrders.size})", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = StudioAccentPink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("چاوەڕوانکراوەکان (${pendingOrders.size})", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        if (selectedTabIndex == 0) {
            // Completed Works
            if (completedOrders.isEmpty()) {
                EmptyStateBox(
                    message = "هێشتا هیچ بەرهەمێکی تەواوکراوت نییە.\nلۆگۆ یان وێنەیەک دروست بکە بۆ ئەوەی لێرە بە کواڵێتی باڵا بیبینیت."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(completedOrders) { order ->
                        CompletedOrderItem(
                            order = order,
                            onClick = { selectedOrderDetails = order },
                            onDownload = {
                                if (order.resultContent.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(order.resultContent))
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        context.startActivity(intent)
                                        viewModel.showToast("فایلەکە کرایەوە!")
                                    } catch (e: Exception) {
                                        viewModel.showToast("هەڵە لە کردنەوەی فایلەکە: فایلی کواڵێتی باڵا (${order.projectName}) بە سەرکەوتوویی پاشەکەوت کرا!")
                                    }
                                } else {
                                    viewModel.showToast("فایلی کواڵێتی باڵا (${order.projectName}) بە سەرکەوتوویی پاشەکەوت کرا!")
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // Pending Works (Waiting for creator)
            if (pendingOrders.isEmpty()) {
                EmptyStateBox(
                    message = "هیچ داواکارییەکی چاوەڕوانکراوت نییە.\nکاتێک داواکاری دەستی لۆگۆ یان ڤیدیۆ دەنێریت، لێرە تۆمار دەبێت تا بۆتی ئامادە دەکەم."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pendingOrders) { order ->
                        PendingOrderItem(
                            order = order,
                            onSimulateComplete = {
                                viewModel.simulateCreatorCompletingOrder(order.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit Profile Dialog

    if (showAgentModal) {
        var agentCode by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        
        Dialog(
            onDismissRequest = { showAgentModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF0D0D12).copy(alpha = 0.9f))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { showAgentModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                            }
                        }
                        
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("چوونەژوورەوەی ئادمین / بریکار", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("کۆدی مۆڵەتەکەت لێرە بنووسە بۆ چوونەژوورەوە.", color = Color.White.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = agentCode,
                            onValueChange = { agentCode = it; errorMsg = null },
                            label = { Text("کۆدی مۆڵەت (License Key)", color = Color.White.copy(alpha = 0.6f)) },
                            placeholder = { Text("نموونە: ADM-XXXX-XXX", color = Color.White.copy(alpha = 0.3f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                            )
                        )
                        
                        if (errorMsg != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(errorMsg!!, color = StudioAccentPink, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                if (agentCode.trim().startsWith("ADM-")) {
                                    viewModel.loginAsAgent(agentCode.trim())
                                    showAgentModal = false
                                    showAdminModal = true
                                } else {
                                    errorMsg = "کۆدەکە هەڵەیە! پێویستە بە ADM دەست پێبکات."
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(12.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF00E5FF)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("چوونەژوورەوە بۆ ژووری فرۆشتن", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAdminModal) {
        FullScreenAdminModal(viewModel = viewModel, onDismiss = { showAdminModal = false })
    }

    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile?.username ?: "") }
        var editAgeStr by remember { mutableStateOf((userProfile?.age ?: 22).toString()) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("دەستکاریکردنی زانیاری کەسی", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("ناو") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editAgeStr,
                        onValueChange = { editAgeStr = it },
                        label = { Text("تەمەن") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ageInt = editAgeStr.toIntOrNull() ?: 22
                        viewModel.updateProfileInfo(editName, ageInt, userProfile?.avatarUri ?: "")
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                ) {
                    Text("پاشەکەوتکردن")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditProfileDialog = false }) {
                    Text("پاشگەزبوونەوە")
                }
            }
        )
    }

    // View Completed Work Details Modal
    if (selectedOrderDetails != null) {
        val order = selectedOrderDetails!!
        Dialog(
            onDismissRequest = { selectedOrderDetails = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
            ) {
                // Blurred Background for aesthetic (if image)
                if ((order.type == "IMAGE" || order.type == "LOGO") && order.resultContent.startsWith("http")) {
                    AsyncImage(
                        model = order.resultContent,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(40.dp)
                            .background(Color.Black.copy(alpha = 0.6f))
                    )
                }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Content layer
                    if ((order.type == "IMAGE" || order.type == "LOGO") && order.resultContent.startsWith("http")) {
                        AsyncImage(
                            model = order.resultContent,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (order.type == "VIDEO") {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .shadow(24.dp, CircleShape, spotColor = Color.White)
                                    .clickable { /* Simulate Play */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "ڤیدیۆ فایل ئامادەیە", 
                                color = Color.White, 
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(StudioPrimary.copy(alpha = 0.2f))
                                    .border(1.dp, StudioPrimary.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = StudioPrimary,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                if (order.type == "PROMO_CODE") "پرۆمۆ کۆد" else "فایلی زیپ یان پرۆژە", 
                                color = Color.White, 
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            if (!order.resultContent.startsWith("http") && order.resultContent.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(order.resultContent, color = StudioEmerald, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    // Top Bar Layer
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha=0.9f), Color.Transparent)))
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = order.projectName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(
                            onClick = { selectedOrderDetails = null },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "داخستن", tint = Color.White)
                        }
                    }

                    // Bottom Bar Layer (Download Button)
                    var isDownloaded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha=0.9f))))
                            .padding(32.dp)
                    ) {
                        Button(
                            onClick = {
                                isDownloaded = true
                                if (order.resultContent.isNotBlank() && order.resultContent.startsWith("http")) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(order.resultContent))
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        context.startActivity(intent)
                                        viewModel.showToast("فایلەکە کرایەوە!")
                                    } catch (e: Exception) {
                                        viewModel.showToast("فایلەکە پاشەکەوت کرا.")
                                    }
                                } else {
                                    viewModel.showToast("فایلەکە بەردەست نییە یان کۆدە.")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = StudioEmerald),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(listOf(StudioEmerald, StudioEmerald.copy(alpha = 0.7f)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download, 
                                        contentDescription = null, 
                                        tint = Color.White, 
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        if (isDownloaded) "داگیرا" else "داگرتن / Save to Gallery", 
                                        color = Color.White, 
                                        fontSize = 18.sp, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }}

@Composable
fun CompletedOrderItem(
    order: CreationOrder,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = StudioEmerald.copy(alpha = 0.5f), spotColor = StudioEmerald)
            .clickable { onClick() }
            .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)))
        ) {
            if ((order.type == "IMAGE" || order.type == "LOGO") && order.resultContent.isNotBlank() && order.resultContent.startsWith("http")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = order.resultContent,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (order.type == "VIDEO") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(Color.Black, StudioPrimary.copy(alpha = 0.3f))))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .shadow(16.dp, CircleShape, spotColor = Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "لێدان",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            } else if (order.type == "PROMO_CODE") {
                // No big media preview for promo code
            } else if (order.resultContent.isNotBlank() && order.resultContent.startsWith("http")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(StudioPrimary.copy(alpha=0.2f), Color.Transparent)))
                        .border(1.dp, StudioPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "فایل",
                        tint = StudioPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (order.type == "PROMO_CODE" || order.resultContent.isBlank() || !(order.resultContent.startsWith("http"))) {
                    // Small fallback icon if no preview
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(listOf(StudioPrimary, StudioSecondary)))
                            .border(1.dp, Color.White.copy(alpha=0.3f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (order.type) {
                                "PROMO_CODE" -> Icons.Default.WorkspacePremium
                                else -> Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.projectName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${order.category} • ${order.dateFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = StudioEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.6f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StudioEmerald))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تەواوکراو بە کواڵێتی باڵا",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StudioEmerald
                                )
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .size(56.dp)
                        .background(StudioEmerald.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, StudioEmerald.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "داگرتن",
                        tint = StudioEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PendingOrderItem(
    order: CreationOrder,
    onSimulateComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = StudioAccentPink.copy(alpha = 0.4f), spotColor = StudioAccentPink),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Brush.linearGradient(listOf(StudioAccentPink.copy(alpha = 0.6f), Color.Transparent)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.projectName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${order.category} • بەروار: ${order.dateFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = StudioAccentPink.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioAccentPink.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = StudioAccentPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "چاوەڕوان بە",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = StudioAccentPink
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ڕێنماییەکانت: ${order.details.ifBlank { "داواکاری تۆمارکراوە و ڕەوانەی دیزاینەر کراوە" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun ProfileStatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = color.copy(alpha = 0.3f), spotColor = color)
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Brush.linearGradient(listOf(color.copy(alpha = 0.5f), Color.Transparent)))
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)))
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
