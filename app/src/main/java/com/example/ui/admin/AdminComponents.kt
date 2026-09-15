package com.example.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.StudioViewModel
import com.example.ui.premium.PaymentModal
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioAccentPink
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

val Obsidian = Color(0xFF0D0D12)
val RoyalViolet = Color(0xFF5A228B)
val RoyalVioletLight = Color(0xFF8B48D1)
val NeonBlue = Color(0xFF00E5FF)

@Composable
fun FullScreenAdminModal(viewModel: StudioViewModel, onDismiss: () -> Unit) {
    val profile by viewModel.userProfile.collectAsState()
    var isLoggedIn by remember { mutableStateOf(profile?.isAgent == true) }
    var loggedInRole by remember { mutableStateOf(if (profile?.isAgent == true) "agent" else "super") }
    var loggedInAgentId by remember { mutableStateOf(profile?.agentId ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Obsidian
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(RoyalViolet.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(RoyalViolet, NeonBlue)))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("پانیلی بەڕێوەبەر", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                if (isLoggedIn) {
                                    Text(if (loggedInRole == "super") "خاوەن کار / Super Admin" else "بریکار / Agent Admin", color = RoyalVioletLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }

                    if (!isLoggedIn) {
                        AdminLoginScreen(onLoginSuccess = { role, agentId -> 
                            loggedInRole = role
                            loggedInAgentId = agentId
                            isLoggedIn = true 
                        })
                    } else {
                        AdminDashboardScreen(viewModel = viewModel, role = loggedInRole, agentId = loggedInAgentId)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLoginScreen(onLoginSuccess: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RoyalVioletLight, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("چوونەژوورەوەی شاهانە", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(32.dp))

        AdminTextField(value = email, onValueChange = { email = it; errorMsg = null }, label = "ئیمەیڵ (Email)", icon = Icons.Default.Email)
        Spacer(modifier = Modifier.height(16.dp))
        AdminTextField(value = password, onValueChange = { password = it; errorMsg = null }, label = "وشەی نهێنی (Password)", icon = Icons.Default.Lock, isPassword = true)
        Spacer(modifier = Modifier.height(16.dp))
        AdminTextField(value = pin, onValueChange = { pin = it; errorMsg = null }, label = "پین کۆد (PIN Code)", icon = Icons.Default.VpnKey, isPassword = true)



        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMsg!!, color = StudioAccentPink, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (email.trim() == "mustafamandelawi@gmail.com" &&
                    password == "mustafa admin 2762" &&
                    pin == "Admin2762"
                ) {
                    onLoginSuccess("super", "")
                } else {
                    errorMsg = "زانیارییەکان هەڵەیە!"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = RoyalViolet),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(RoyalViolet, NeonBlue))),
                contentAlignment = Alignment.Center
            ) {
                Text("چوونەژوورەوە", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = RoyalVioletLight) },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        singleLine = maxLines == 1,
        maxLines = maxLines,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonBlue,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = NeonBlue
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun AdminDashboardScreen(viewModel: StudioViewModel, role: String = "super", agentId: String = "") {
    var selectedTab by remember { mutableIntStateOf(1) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AdminTabItem(
                text = "پرۆمۆ کۆد",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            AdminTabItem(
                text = "ناردنی داواکاری",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
            if (role == "super") {
                AdminTabItem(
                    text = "ئاماری بریکارەکان",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (selectedTab == 0) {
                AdminPromoCodeTab(role = role, agentId = agentId)
            } else if (selectedTab == 1) {
                AdminDeliveryTab(viewModel = viewModel)
            } else if (selectedTab == 2 && role == "super") {
                OwnerAnalyticsTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminTabItem(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor by animateColorAsState(if (isSelected) RoyalViolet.copy(alpha = 0.6f) else Color.Transparent)
    val textColor by animateColorAsState(if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun AdminPromoCodeTab(role: String = "super", agentId: String = "") {
    val clipboardManager = LocalClipboardManager.current
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var pendingPaymentPkg by remember { mutableStateOf<Triple<String, String, Double>?>(null) }
    var selectedPackageIndex by remember { mutableIntStateOf(0) }

    val packages: List<Triple<String, String, Double>> = listOf(
        Triple("تاک: 1 لۆگۆ", "YS-LG1-", 1.0),
        Triple("تاک: 1 ڤیدیۆ", "YS-VD1-", 3.0),
        Triple("تاک: 1 وێنە", "YS-IM1-", 1.0),
        Triple("3 پرۆمۆ کۆدی وێنە", "YS-PR3-", 0.89),
        Triple("پاکێجی 5$ (7 لۆگۆ، 3 وێنە)", "YS-PK5-", 5.0),
        Triple("پاکێجی 7$ (7 پرۆمۆ، 5 وێنە)", "YS-PK7-", 7.0),
        Triple("پاکێجی 10$ گشتگیر", "YS-PK10-", 10.0),
        Triple("پاکێجی 20$", "YS-PK20-", 20.0),
        Triple("پاکێجی 35$ VIP", "YS-VIP-", 35.0)
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("هەڵبژاردنی پاکێج بۆ پرۆمۆ کۆد:", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Glassmorphism Package Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            packages.forEachIndexed { index, pkg ->
                val isSelected = selectedPackageIndex == index
                val bgColor = if (isSelected) RoyalViolet.copy(alpha = 0.3f) else Color.Transparent
                val borderColor = if (isSelected) NeonBlue else Color.Transparent
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .clickable { selectedPackageIndex = index }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icon based on package type
                        val icon = when {
                            pkg.first.contains("VIP") -> Icons.Default.WorkspacePremium
                            pkg.first.contains("پاکێجی") -> Icons.Default.AutoAwesome
                            pkg.first.contains("ڤیدیۆ") -> Icons.Default.Movie
                            pkg.first.contains("لۆگۆ") -> Icons.Default.Brush
                            else -> Icons.Default.Image
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = pkg.first,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    // Price tag
                    Column(horizontalAlignment = Alignment.End) {
                        val originalPrice = pkg.third
                        val isAgent = role == "agent"
                        val finalPrice = if (isAgent) (originalPrice * 0.4) else originalPrice
                        val formattedFinal = "$" + String.format(java.util.Locale.US, "%.2f", finalPrice).removeSuffix(".00")
                        val formattedOriginal = "$" + String.format(java.util.Locale.US, "%.2f", originalPrice).removeSuffix(".00")
                        
                        if (isAgent) {
                            Surface(
                                color = NeonBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "-60% OFF", 
                                    color = NeonBlue, 
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), 
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formattedOriginal,
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formattedFinal,
                                    color = NeonBlue,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        } else {
                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = formattedOriginal,
                                    color = NeonBlue,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val pkg = packages[selectedPackageIndex]
                if (role == "agent") {
                    val price = pkg.third * 0.4
                    pendingPaymentPkg = Triple("redeem_${pkg.second}", pkg.first, price)
                } else {
                    val prefix = pkg.second
                    val randomChars = (1..6).map { ('A'..'Z').random() }.joinToString("")
                    val randomNums = (1..4).map { ('0'..'9').random() }.joinToString("")
                    generatedCode = "$prefix$randomChars$randomNums"
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(12.dp, RoundedCornerShape(16.dp), spotColor = RoyalViolet),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalViolet),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("دروستکردنی کۆد", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (generatedCode != null) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(20.dp), spotColor = NeonBlue),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(2.dp, NeonBlue)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ئامادەیە بۆ فرۆشتن!", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("کۆدی ڕیدیم دروستکرا بە کواڵێتی باڵا:", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha=0.3f))
                    ) {
                        Text(
                            text = generatedCode!!,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
                            color = NeonBlue,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { clipboardManager.setText(AnnotatedString(generatedCode!!)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("کۆپیکردنی کۆدەکە", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDeliveryTab(viewModel: StudioViewModel) {
    var deliveryOrderId by remember { mutableStateOf("") }
    var deliveryContent by remember { mutableStateOf("") }
    var deliveryTypeExpanded by remember { mutableStateOf(false) }
    var deliveryTypeIndex by remember { mutableIntStateOf(0) }
    val deliveryTypes = listOf("وێنە / لینکی ڤیدیۆ یان درایڤ", "دەق و تێبینی")
    var isDelivering by remember { mutableStateOf(false) }
    
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = uri.lastPathSegment ?: "فایلی هەڵبژێردراو"
            deliveryContent = uri.toString()
        }
    }

    // Infinite transition for pulse effect
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AdminTextField(
            value = deliveryOrderId,
            onValueChange = { deliveryOrderId = it },
            label = "Order ID (وەک: ORD-12345)",
            icon = Icons.Default.Tag
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Upload Zone (Dropzone style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(2.dp, Brush.linearGradient(listOf(RoyalViolet.copy(alpha=pulseAlpha), NeonBlue.copy(alpha=pulseAlpha))), RoundedCornerShape(20.dp))
                .clickable { filePickerLauncher.launch("*/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedFileUri != null) {
                // Preview selected file
                AsyncImage(
                    model = selectedFileUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().background(Color.Black)
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(selectedFileName, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                selectedFileUri = null
                                selectedFileName = ""
                                deliveryContent = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioAccentPink.copy(alpha = 0.8f))
                        ) {
                            Text("گۆڕین / سڕینەوە", color = Color.White)
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = NeonBlue,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "فایلی ئەسڵی لێرە دابنێ یان لە گەلەری هەڵیبژێرە",
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AdminTextField(
            value = deliveryContent,
            onValueChange = { deliveryContent = it },
            label = "یان لینکی درایڤ / دەق بە دەستی بنووسە",
            icon = Icons.Default.Link,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (deliveryOrderId.isNotBlank() && deliveryContent.isNotBlank()) {
                    isDelivering = true
                    viewModel.completeOrderByOrderId(
                        orderIdString = deliveryOrderId.trim(),
                        content = deliveryContent.trim(),
                        onSuccess = {
                            isDelivering = false
                            deliveryOrderId = ""
                            deliveryContent = ""
                            selectedFileUri = null
                            selectedFileName = ""
                        },
                        onError = {
                            isDelivering = false
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = NeonBlue),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            enabled = !isDelivering
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(RoyalViolet, NeonBlue))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isDelivering) "لە ناردندایە..." else "ناردن و تەواوکردنی کار", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = Color.White
                )
            }
        }
    }
}

object AgentSalesTracker {
    // In a real app, this would be in ViewModel / Database. 
    // Here we use an object for demo/memory state.
    var totalSalesByAgent = mutableMapOf<String, Double>()
    
    fun recordSale(agentId: String, amount: Double) {
        val current = totalSalesByAgent[agentId] ?: 0.0
        totalSalesByAgent[agentId] = current + amount
    }
}

@Composable
fun OwnerAnalyticsTab(viewModel: StudioViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ئاماری بریکارەکان (Agent Analytics)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("20% قازانجی خاوەن کار لە فرۆشی بریکارەکان", color = RoyalVioletLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        val sales = AgentSalesTracker.totalSalesByAgent
        
        if (sales.isEmpty()) {
            Text("هیچ فرۆشێک لەلایەن بریکارەکانەوە نەکراوە.", color = Color.White.copy(alpha = 0.5f))
        } else {
            sales.forEach { (agent, total) ->
                val ownerCut = total * 0.20
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RoyalVioletLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بریکار: $agent", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("کۆی گشتی فرۆش:", color = Color.White.copy(alpha = 0.7f))
                            Text("\$${String.format("%.2f", total)}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("پشکی خاوەن کار (20%):", color = NeonBlue)
                            Text("\$${String.format("%.2f", ownerCut)}", color = NeonBlue, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("بەرواری بەسەرچوون:", color = Color.White.copy(alpha = 0.7f))
                            Text("30 ڕۆژی تر", color = Color.White) // Mock date
                        }
                    }
                }
            }
        }
    }
}
