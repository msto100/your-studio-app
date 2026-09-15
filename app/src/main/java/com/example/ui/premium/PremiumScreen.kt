package com.example.ui.premium

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import com.example.ui.AppTab
import com.example.ui.StudioViewModel
import com.example.ui.components.StudioHeader
import com.example.ui.theme.GoldGradientEnd
import com.example.ui.theme.GoldGradientStart
import com.example.ui.theme.StudioAccentPink
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioTertiary

val NeonBlue = Color(0xFF00E5FF)
val RoyalVioletLight = Color(0xFF8B48D1)
val Obsidian = Color(0xFF0D0D12)

@Composable
fun PremiumScreen(viewModel: StudioViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    var purchaseConfirmationDialog by remember { mutableStateOf<Pair<String, String>?>(null) } // (packageKey, title)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 85.dp)
    ) {
        StudioHeader(
            title = "کڕینی پاکێج و کریدت",
            subtitle = "بەرزترین کوالێتی و خزمەتگوزاری خێرا بۆ هەموو بەشەکان",
            badgeText = if (userProfile?.isUnlimited == true) "تۆ VIP بێ سنوریت 👑" else "${userProfile?.logoCredits ?: 0} لۆگۆ | ${userProfile?.videoCredits ?: 0} ڤیدیۆ"
        )

        // Auth warning banner if user is not logged in
        if (userProfile?.isLoggedIn != true) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.setTab(AppTab.ACCOUNT) },
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ئاگاداری: دەبێت سەرەتا هەژمار دروست بکەیت",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "هەر کەسێک ئەکاونتی نەبوو ناتوانێت پاکێج یاخود لۆگۆ و ڤیدیۆ بکڕێت. کلیک بکە بۆ چوونەژوورەوە.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Section: Single Items (کڕینی تاک)
        Text(
            text = "کڕینی تاک بە تاک",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SingleItemCard(
                title = "1 لۆگۆ",
                price = "1$",
                color = StudioPrimary,
                modifier = Modifier.weight(1f),
                onClick = { purchaseConfirmationDialog = Pair("single_logo", "1 لۆگۆ (1$)") }
            )
            SingleItemCard(
                title = "1 ڤیدیۆ",
                price = "3$",
                color = StudioSecondary,
                modifier = Modifier.weight(1f),
                onClick = { purchaseConfirmationDialog = Pair("single_video", "1 ڤیدیۆ (3$)") }
            )
            SingleItemCard(
                title = "1 وێنە",
                price = "1$",
                color = StudioTertiary,
                modifier = Modifier.weight(1f),
                onClick = { purchaseConfirmationDialog = Pair("single_image", "1 وێنە (1$)") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Promo Code Discount Box (داشکانی کاتی)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable {
                    purchaseConfirmationDialog = Pair("promo_discount", "3 پرۆمۆ کۆدی وێنە بە داشکانی کاتی (0.89$)")
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioEmerald.copy(alpha = 0.12f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioEmerald)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(StudioEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.LocalOffer, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "3 پرۆمۆ کۆدی وێنە",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = StudioAccentPink,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "داشکانی کاتی !",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "نرخی ئەسڵی 3$ -> تەنها بە 0.89$",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioEmerald
                        )
                    }
                }
                Text(
                    text = "0.89$",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = StudioEmerald
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: Main Packages (پاکێجە سەرەکییەکان)
        Text(
            text = "پاکێجە بەناوبانگەکان",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. پاکێجی 5 دۆلاری: 7 لۆگۆ + 3 وێنە
        PackageCard(
            title = "پاکێجی 5 دۆلاری",
            price = "5$",
            accentColor = StudioPrimary,
            features = listOf(
                "7 لۆگۆی تایبەت و براند",
                "3 دەستکاریکردن یان وێنەی نوێ"
            ),
            isPopular = false,
            onClick = { purchaseConfirmationDialog = Pair("pkg_5", "پاکێجی 5 دۆلاری") }
        )

        // 2. پاکێجی 7 دۆلاری: 7 پرۆمۆ کۆدی وێنە + 5 وێنە
        PackageCard(
            title = "پاکێجی 7 دۆلاری",
            price = "7$",
            accentColor = StudioSecondary,
            features = listOf(
                "7 پرۆمۆ کۆدی وێنەی زیرەکی دەستکرد",
                "5 دەستکاریکردن یان وێنەی تایبەت"
            ),
            isPopular = false,
            onClick = { purchaseConfirmationDialog = Pair("pkg_7", "پاکێجی 7 دۆلاری") }
        )

        // 3. پاکێجی 10 دۆلاری: 5 ڤیدیۆ + 10 لۆگۆ + 5 پرۆمۆ کۆد + 5 وێنە
        PackageCard(
            title = "پاکێجی 10 دۆلاری (گشتگیر)",
            price = "10$",
            accentColor = StudioTertiary,
            features = listOf(
                "5 ئیدیتی ڤیدیۆی تایبەت و گەیمینگ",
                "10 لۆگۆی پڕۆفیشناڵ",
                "5 پرۆمۆ کۆدی وێنە",
                "5 دەستکاری و بەرزکردنەوەی وێنە"
            ),
            isPopular = true,
            onClick = { purchaseConfirmationDialog = Pair("pkg_10", "پاکێجی 10 دۆلاری") }
        )

        // 4. پاکێجی 20 دۆلاری: 30 لۆگۆ + 20 ڤیدیۆ + 1 پرۆمۆ کۆد + 10 وێنە (لۆگۆی ئەی ئای بێ سنور بێت!)
        PackageCard(
            title = "پاکێجی 20 دۆلاری (لۆگۆی AI بێ سنور)",
            price = "20$",
            accentColor = StudioAccentPink,
            features = listOf(
                "👑 لۆگۆی ئەی ئای (AI) بە تەواوی بێ سنور!",
                "30 لۆگۆی دەستی و پرۆفیشناڵ",
                "20 ئیدیتی ڤیدیۆی کواڵێتی باڵا",
                "1 پرۆمۆ کۆدی تایبەت",
                "10 وێنەی ئیدیتکراو"
            ),
            isPopular = false,
            onClick = { purchaseConfirmationDialog = Pair("pkg_20", "پاکێجی 20 دۆلاری") }
        )

        // 5. پاکێجی 35 دۆلاری: لۆگۆی بێ سنور + ڤیدیۆی بێ سنور + وێنەی بێ سنور + پرۆمۆ کۆدی بێ سنور (باشترین هەڵبژاردە)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable {
                    if (userProfile?.isUnlimited != true) purchaseConfirmationDialog = Pair("pkg_35", "سەبسکرایبی مانگانە (Monthly VIP Subscription - 35$)")
                },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6),
                                Color(0xFFEC4899)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    if (userProfile?.isUnlimited == true) {
                        Surface(
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("چالاکە / Active", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFFBBF24),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "باشترین هەڵبژاردە ⭐️ VIP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "35$",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "سەبسکرایبی مانگانە (Monthly VIP Subscription - 35$)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val vipFeatures = listOf(
                        "لۆگۆی بێ سنور (AI و دەستی)",
                        "ڤیدیۆی بێ سنور بە بەرزترین کواڵێتی",
                        "وێنەی بێ سنور بۆ هەموو بوارەکان",
                        "پرۆمۆ کۆدی بێ سنوری ڕۆژانە"
                    )
                    vipFeatures.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feat,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    if (userProfile?.isUnlimited == true) {
                        Button(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                disabledContentColor = Color(0xFF00E5FF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ئەم بەشە کڕاوە (چالاکە)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (userProfile?.isUnlimited != true) purchaseConfirmationDialog = Pair("pkg_35", "سەبسکرایبی مانگانە (Monthly VIP Subscription - 35$)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "کڕینی سەبسکرایبی مانگانە (35$)",
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary
                            )
                        }
                    }
                }
            }
        }
        

        // Admin / Agent License Package
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable {
                    if (userProfile?.isAgent != true) purchaseConfirmationDialog = Pair("pkg_admin", "مۆڵەتی بەڕێوەبەر / بریکاری فەرمی (Official Agent License)")
                },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D12).copy(alpha = 0.8f))
                    .border(1.dp, Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF5A228B))), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
                    if (userProfile?.isUnlimited == true) {
                        Surface(
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("چالاکە / Active", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "مۆڵەتی بەڕێوەبەر",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E5FF)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "34.99$",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "مۆڵەتی بەڕێوەبەر / بریکاری فەرمی",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "(Official Agent License)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha=0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val adminFeatures = listOf(
                        "دەستەبەرکردنی داشکاندنی تایبەتی ٦٠٪ لەسەر کڕینی هەموو جۆرە کۆدەکانی ڕیدیم (Redeem Codes) بۆ فرۆشتنەوە.",
                        "دروستکردنی کۆدی ڕیدیم بۆ کڕیاران بە نرخی تایبەت",
                        "ئۆتۆماتیکی دروستکردنی کۆدی هەڕەمەکی (ADM-XXXX)"
                    )
                    adminFeatures.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feat,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    if (userProfile?.isAgent == true) {
                        Button(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                disabledContentColor = Color(0xFF00E5FF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ئەم بەشە کڕاوە (چالاکە)", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                viewModel.triggerOpenAdminModal = true
                                viewModel.setTab(AppTab.PROFILE)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🚀 چوونە ژووری ئادمین", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (userProfile?.isAgent != true) purchaseConfirmationDialog = Pair("pkg_admin", "مۆڵەتی بەڕێوەبەر / بریکاری فەرمی (Official Agent License)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "کڕینی مۆڵەت (34.99$)",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Enter Promo Code (کۆدی دیاری) Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "کۆدی دیاری (Promo Code) هەیە؟",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var userPromoCode by remember { mutableStateOf("") }
                    val context = LocalContext.current
                    
                    OutlinedTextField(
                        value = userPromoCode,
                        onValueChange = { userPromoCode = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("YS-XXXX-XXXX") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (userPromoCode.isNotBlank()) {
                                viewModel.applyPromoCode(context, userPromoCode.trim())
                                userPromoCode = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                    ) {
                        Text("چەسپاندن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
    }

    // Payment Modal with 4 local payment gateways (FastPay, FIB, ZainCash, Qi Card)
    if (purchaseConfirmationDialog != null) {
        val (pkgKey, pkgTitle) = purchaseConfirmationDialog!!
        PaymentModal(
            packageKey = pkgKey,
            packageTitle = pkgTitle,
            viewModel = viewModel,
            userName = userProfile?.username ?: "",
            onDismiss = { purchaseConfirmationDialog = null }
        )
    }

    // Show Generated Admin License Key
    val adminKey = viewModel.generatedAdminLicenseKey
    if (adminKey != null) {
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        var isCopied by remember { mutableStateOf(false) }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.generatedAdminLicenseKey = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF0D0D12))
                            .border(2.dp, NeonBlue, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("پیرۆزە!", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                        Text("مۆڵەتی بەڕێوەبەر چالاککرا", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.titleMedium)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Surface(
                            color = NeonBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = adminKey,
                                color = NeonBlue,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(adminKey))
                                isCopied = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            Icon(if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isCopied) "کۆپیکرا!" else "📋 کۆپیکردنی کۆد", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = { 
                                viewModel.generatedAdminLicenseKey = null
                                viewModel.setTab(com.example.ui.AppTab.PROFILE)
                                viewModel.loginAsAgent(adminKey) // ensure session is active
                                viewModel.triggerOpenAdminModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            Text("🚀 چوونە ناو ژووری بەڕێوەبەر", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SingleItemCard(
    title: String,
    price: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = price,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "کڕین",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun PackageCard(
    title: String,
    price: String,
    accentColor: Color,
    features: List<String>,
    isPopular: Boolean,
    isPurchased: Boolean = false,
    onAdminLoginClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = !isPurchased) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = if (isPopular) androidx.compose.foundation.BorderStroke(2.dp, accentColor) else if (isPurchased) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isPurchased) {
                Surface(
                    color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("چالاکە / Active", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            features.forEach { feat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feat,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (isPurchased) {
                Button(
                    onClick = { },
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFF00E5FF).copy(alpha = 0.15f),
                        disabledContentColor = Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ئەم بەشە کڕاوە (چالاکە)", fontWeight = FontWeight.Bold)
                }
                
                if (onAdminLoginClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAdminLoginClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚀 چوونە ژووری ئادمین", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "کڕینی پاکێج ($price)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
