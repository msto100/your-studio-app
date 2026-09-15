package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.StudioViewModel
import com.example.ui.components.CategoryTile
import com.example.ui.components.SectionCard
import com.example.ui.components.StudioHeader
import com.example.ui.theme.GoldGradientEnd
import com.example.ui.theme.GoldGradientStart
import com.example.ui.theme.StudioAccentPink
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: StudioViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isGeneratingLogo by viewModel.isGeneratingLogo.collectAsState()
    val generatedLogoResult by viewModel.generatedLogoResult.collectAsState()
    val logoRetryCount by viewModel.logoRetryCount.collectAsState()
    val isGeneratingPromo by viewModel.isGeneratingPromoCode.collectAsState()
    val generatedPromoCode by viewModel.generatedPromoCode.collectAsState()

    var activeLogoCategory by remember { mutableStateOf<String?>(null) }
    var activeVideoCategory by remember { mutableStateOf<String?>(null) }
    var activeImageCategory by remember { mutableStateOf<String?>(null) }
    var showPromptCodeSheet by remember { mutableStateOf(false) }

    val logoCategories = listOf(
        Pair("لۆگۆی براند", Icons.Default.BrandingWatermark),
        Pair("لۆگۆی ئۆتۆمبێل", Icons.Default.DirectionsCar),
        Pair("لۆگۆی فرۆشگا", Icons.Default.Storefront),
        Pair("لۆگۆی کۆمپانیا", Icons.Default.Business),
        Pair("لۆگۆی کاڵا", Icons.Default.ShoppingBag),
        Pair("لۆگۆی گەیمینگ", Icons.Default.Gamepad),
        Pair("لۆگۆی ئاسایی", Icons.Default.Brush),
        Pair("لۆگۆی AI", Icons.Default.AutoAwesome),
        Pair("لۆگۆی تایبەت", Icons.Default.Lightbulb),
        Pair("لۆگۆی تر", Icons.Default.Psychology)
    )

    val videoCategories = listOf(
        Pair("ڤیدیۆی گەیمینگ", Icons.Default.Gamepad),
        Pair("ڤیدیۆی کۆمپانیا", Icons.Default.Business),
        Pair("ڤیدیۆی تایبەت", Icons.Default.Diversity3),
        Pair("ڤیدیۆی AI (بۆت دروست دەکرێ)", Icons.Default.AutoAwesome),
        Pair("ڤیدیۆی ڕێکلامی", Icons.Default.Movie),
        Pair("ڤیدیۆی گۆرانی", Icons.Default.MusicNote),
        Pair("ڤیدیۆی ژێرنوس", Icons.Default.Subtitles),
        Pair("بەرز کردنەوەی کواڵێتی ڤیدیۆ", Icons.Default.HighQuality)
    )

    val imageCategories = listOf(
        Pair("وێنەی کەسایەتی", Icons.Default.Face),
        Pair("وێنەی ئۆتۆمبێل", Icons.Default.DirectionsCar),
        Pair("وێنەی کاڵا", Icons.Default.ShoppingBag),
        Pair("وێنەی فرۆشگا", Icons.Default.Storefront),
        Pair("وێنەی کۆمپانیا", Icons.Default.Business),
        Pair("گۆڕینی وێنە", Icons.Default.Image),
        Pair("دروست کردنی وێنە بە AI", Icons.Default.AutoAwesome),
        Pair("بەرز کردنەوەی کواڵێتی وێنە", Icons.Default.HighQuality)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // App Header with user greeting and balance
        StudioHeader(
            title = "YourStudio AI",
            subtitle = "لۆگۆ • وێنە • ڤیدیۆ • پرۆمۆ کۆد",
            badgeText = if (userProfile?.isUnlimited == true) "بێ سنور 👑" else "${userProfile?.logoCredits ?: 0} لۆگۆ | ${userProfile?.videoCredits ?: 0} ڤیدیۆ",
            onBadgeClick = { viewModel.setTab(AppTab.PREMIUM) }
        )

        // Studio User Header Bar
        if (userProfile?.isLoggedIn == true) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(StudioPrimary, StudioSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (userProfile?.username?.take(1) ?: "U").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile?.username ?: "بەکارهێنەر",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StudioEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "چالاکە ✓",
                                        color = StudioEmerald,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            if (!userProfile?.email.isNullOrBlank()) {
                                Text(
                                    text = userProfile?.email ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "دەرچوون",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "دەرچوون (Logout)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.setTab(AppTab.ACCOUNT) },
                shape = RoundedCornerShape(16.dp),
                color = StudioPrimary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = StudioPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "بچۆ ژوورەوە یان هەژمار دروست بکە",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = StudioPrimary
                        )
                    }
                    Button(
                        onClick = { viewModel.setTab(AppTab.ACCOUNT) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                    ) {
                        Text("چوونەژوورەوە", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // VIP Hero Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(StudioPrimary, StudioSecondary)
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ئەپی تایبەت بە دروستکردن و ئیدیت",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "بە خێراترین کات و بەرزترین کواڵێتی لە ڕێگەی زیرەکی دەستکرد و دیزاینەری پسپۆڕ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { viewModel.setTab(AppTab.PREMIUM) },
                        color = Color.White
                    ) {
                        Text(
                            text = "کڕینی پاکێج",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SECTION 1: بەشی لۆگۆ
        SectionCard(
            title = "بەشی لۆگۆ",
            subtitle = "لۆگۆکەت دروست دەکرێ بە باشترین کواڵێتی بە ئارەزووی خۆت",
            accentColor = StudioPrimary
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                logoCategories.forEach { (catName, catIcon) ->
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryTile(
                            name = catName,
                            icon = catIcon,
                            color = StudioPrimary,
                            onClick = { activeLogoCategory = catName }
                        )
                    }
                }
            }
        }

        // SECTION 2: بەشی ڤیدیۆ ئیدیتینگ
        SectionCard(
            title = "بەشی ڤیدیۆ ئیدیتینگ",
            subtitle = "هەموو ئەم بەشانە پێویستی بە ڤیدیۆی خۆتە ئێمە تەنها بۆتی ئیدیت دەکەین بە باشترین کواڵێتی و بە ئارەزووی خۆت",
            accentColor = StudioSecondary
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                videoCategories.forEach { (catName, catIcon) ->
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryTile(
                            name = catName,
                            icon = catIcon,
                            color = StudioSecondary,
                            onClick = { activeVideoCategory = catName }
                        )
                    }
                }
            }
        }

        // SECTION 3: بەشی وێنە
        SectionCard(
            title = "بەشی وێنە",
            subtitle = "لەم بەشە وێنەکانتان بۆ ئیدیت دەکرێ بە باشترین کواڵێتی بە ئارەزووی خۆت",
            accentColor = StudioTertiary
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                imageCategories.forEach { (catName, catIcon) ->
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryTile(
                            name = catName,
                            icon = catIcon,
                            color = StudioTertiary,
                            onClick = { activeImageCategory = catName }
                        )
                    }
                }
            }
        }

        // SECTION 4: بەشی پرۆمۆ کۆدی وێنە
        SectionCard(
            title = "بەشی پرۆمۆ کۆدی وێنە",
            subtitle = "دروستکردنی پرۆمۆ کۆدی تایبەت بۆ بەکارهێنان لەگەڵ زیرەکی دەستکرد بەپێی نمونەی وێنەکەت",
            accentColor = StudioEmerald
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showPromptCodeSheet = true },
                color = StudioEmerald.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioEmerald)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(StudioEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "دروستکردنی پرۆمۆ کۆدی وێنە",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تەنها نمونەی وێنە + ناوی پرۆجێکت دابنێ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = StudioEmerald
                    )
                }
            }
        }
    }

    // Logo Creation Sheet
    if (activeLogoCategory != null) {
        LogoCreationSheet(
            category = activeLogoCategory!!,
            viewModel = viewModel,
            userLoggedIn = viewModel.isUserLoggedIn(),
            isUnlimited = userProfile?.isUnlimited == true,
            hasUnlimitedAiLogos = userProfile?.hasUnlimitedAiLogos == true,
            defaultUsername = userProfile?.username ?: "بەکار‌هێنەر",
            isGenerating = isGeneratingLogo,
            generatedResult = generatedLogoResult,
            retryCount = logoRetryCount,
            onDismiss = { activeLogoCategory = null }
        )
    }

    // Video Creation Sheet
    if (activeVideoCategory != null) {
        VideoCreationSheet(
            category = activeVideoCategory!!,
            viewModel = viewModel,
            defaultUsername = userProfile?.username ?: "بەکار‌هێنەر",
            onDismiss = { activeVideoCategory = null }
        )
    }

    // Image Creation Sheet
    if (activeImageCategory != null) {
        ImageCreationSheet(
            category = activeImageCategory!!,
            viewModel = viewModel,
            defaultUsername = userProfile?.username ?: "بەکار‌هێنەر",
            onDismiss = { activeImageCategory = null }
        )
    }

    // Prompt Code Sheet
    if (showPromptCodeSheet) {
        PromptCodeSheet(
            viewModel = viewModel,
            defaultUsername = userProfile?.username ?: "بەکار‌هێنەر",
            isGenerating = isGeneratingPromo,
            generatedCode = generatedPromoCode,
            onDismiss = { showPromptCodeSheet = false }
        )
    }
}
