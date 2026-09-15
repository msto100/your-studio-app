package com.example.ui.premium

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PaymentConfig
import com.example.data.model.PaymentGatewayType
import com.example.ui.StudioViewModel
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPrimary
import com.example.util.TelegramHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentModal(
    packageKey: String,
    packageTitle: String,
    viewModel: StudioViewModel,
    userName: String,
    customPriceUsd: Double? = null,
    isRedeemPurchase: Boolean = false,
    onSuccessAction: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedGateway by remember { mutableStateOf(PaymentGatewayType.FASTPAY) }
    var receiptNumber by remember { mutableStateOf("") }
    var receiptNotes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Calculate approx IQD amount based on package price
    val (priceUsd, priceIqd) = if (customPriceUsd != null) {
        val usdFmt = "$" + String.format(java.util.Locale.US, "%.2f", customPriceUsd).removeSuffix(".00")
        val iqdFmt = String.format("%,d", (customPriceUsd * 1500).toInt()) + " دینار"
        Pair(usdFmt, iqdFmt)
    } else {
        getPriceEstimates(packageKey)
    }

    val currentQrUrl = when (selectedGateway) {
        PaymentGatewayType.FASTPAY -> PaymentConfig.FASTPAY_QR_URL
        PaymentGatewayType.FIB -> PaymentConfig.FIB_QR_URL
        PaymentGatewayType.ZAINCASH -> PaymentConfig.ZAINCASH_QR_URL
        PaymentGatewayType.QICARD -> PaymentConfig.QICARD_QR_URL
    }

    val currentAccount = when (selectedGateway) {
        PaymentGatewayType.FASTPAY -> PaymentConfig.FASTPAY_ACCOUNT
        PaymentGatewayType.FIB -> PaymentConfig.FIB_ACCOUNT
        PaymentGatewayType.ZAINCASH -> PaymentConfig.ZAINCASH_ACCOUNT
        PaymentGatewayType.QICARD -> PaymentConfig.QICARD_ACCOUNT
    }

    val currentHolder = when (selectedGateway) {
        PaymentGatewayType.FASTPAY -> PaymentConfig.FASTPAY_HOLDER
        PaymentGatewayType.FIB -> PaymentConfig.FIB_HOLDER
        PaymentGatewayType.ZAINCASH -> PaymentConfig.ZAINCASH_HOLDER
        PaymentGatewayType.QICARD -> PaymentConfig.QICARD_HOLDER
    }

    val brandColor = Color(android.graphics.Color.parseColor(selectedGateway.accentHex))

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "دەروازەی پارەدانی ناوخۆیی",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "کڕینی: $packageTitle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "داخستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Payment Gateway Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentGatewayType.values().forEach { gateway ->
                    val isSelected = selectedGateway == gateway
                    val gColor = Color(android.graphics.Color.parseColor(gateway.accentHex))

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedGateway = gateway }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) gColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) gColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = gateway.brandName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) gColor else MaterialTheme.colorScheme.onSurface
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = gateway.titleKurdish,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = if (isSelected) gColor else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Amount Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = brandColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "بڕی پێویست بۆ پارەدان:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = priceUsd,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = brandColor
                                    )
                                )
                                Text(
                                    text = priceIqd,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code Display
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, brandColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = currentQrUrl,
                            contentDescription = "بارکۆدی ${selectedGateway.brandName}",
                            modifier = Modifier.size(174.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "بارکۆدی پارەدان لە ڕێگەی ئەپی ${selectedGateway.brandName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = brandColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Info Row with Copy Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ژمارەی حیساب / IBAN:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currentAccount,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "بەناوی: $currentHolder",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Account Number", currentAccount)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.showToast("ژمارەی حیساب کۆپی کرا: $currentAccount")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "کۆپیکردن",
                                    tint = brandColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions Text
            Text(
                text = "پاش ئەوەی پارەکەت لە ڕێگەی ${selectedGateway.brandName} نارد، ژمارەی وەسڵەکە لێرە بنووسە تاوەکو کریدتەکە دەستبەجێ بخرێتە سەر هەژمارەکەت و زانیارییەکان بۆ دیزاینەر بنێردرێت.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Receipt Input Fields
            OutlinedTextField(
                value = receiptNumber,
                onValueChange = { receiptNumber = it },
                label = { Text("ژمارەی وەسڵ یان ترانزاکشن (Transaction ID)") },
                placeholder = { Text("نموونە: TX-849204 یان 0750...1234") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = receiptNotes,
                onValueChange = { receiptNotes = it },
                label = { Text("تێبینی یان ناوی نێرەر (ئارەزوومەندانە)") },
                placeholder = { Text("نموونە: نێردرا لە هەژماری بەڕێز...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Button
            Button(
                onClick = {
                    if (receiptNumber.isBlank()) {
                        viewModel.showToast("تکایە ژمارەی وەسڵ یان ترانزاکشن بنووسە")
                        return@Button
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        // Format and send payment receipt to Telegram Bot
                        val receiptHtml = TelegramHelper.formatPaymentReceiptHtml(
                            userName = userName.ifBlank { "بەکارهێنەر" },
                            packageName = packageTitle,
                            amount = "$priceUsd ($priceIqd)",
                            gateway = "${selectedGateway.brandName} (${selectedGateway.titleKurdish})",
                            receiptNumber = receiptNumber,
                            notes = receiptNotes
                        )

                        // 1. Send to Telegram Bot API
                        TelegramHelper.sendToTelegramBot(receiptHtml)

                        // 2. Activate credits / package
                        viewModel.purchasePackage(packageKey, packageTitle)

                        isSubmitting = false
                        viewModel.showToast("وەسڵی پارەدان ڕەوانەی دیزاینەر کرا و پاکێجەکەت چالاککرا!")
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ناردن بۆ بۆتی تیلیگرام...")
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ناردنی وەسڵی پارەدان و چالاککردن",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun getPriceEstimates(packageKey: String): Pair<String, String> {
    return when (packageKey) {
        "single_logo" -> Pair("1$", "1,500 دینار")
        "single_video" -> Pair("3$", "4,500 دینار")
        "single_image" -> Pair("1$", "1,500 دینار")
        "single_promo" -> Pair("1$", "1,500 دینار")
        "pkg_5" -> Pair("5$", "7,500 دینار")
        "pkg_10" -> Pair("10$", "15,000 دینار")
        "pkg_20" -> Pair("20$", "30,000 دینار")
        "pkg_35" -> Pair("35$", "52,500 دینار (بێ سنور)")
        "pkg_admin" -> Pair("34.99$", "52,500 دینار (مۆڵەتی بەڕێوەبەر)")
        else -> Pair("5$", "7,500 دینار")
    }
}
