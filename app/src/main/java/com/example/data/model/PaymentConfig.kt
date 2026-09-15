package com.example.data.model

// =========================================================================
// 💳 LOCAL PAYMENT GATEWAYS & QR CODE CONFIGURATION
// شوێنی دانانی لینکی وێنەی بارکۆدی (QR Code) و زانیاری حیسابەکان لێرەیە:
// =========================================================================
object PaymentConfig {
    // 1. FastPay (فاست پەی)
    var FASTPAY_QR_URL = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=FastPay:07501234567"
    var FASTPAY_ACCOUNT = "0750 123 4567"
    var FASTPAY_HOLDER = "یۆر ستۆدیۆ (YourStudio)"

    // 2. FIB - First Iraqi Bank (بانکی یەکەمی عێراقی)
    var FIB_QR_URL = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=FIB:IQ99FIB0000000000000001"
    var FIB_ACCOUNT = "IQ99FIB0000000000000001"
    var FIB_HOLDER = "YourStudio LLC"

    // 3. ZainCash (زەین کاش)
    var ZAINCASH_QR_URL = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=ZainCash:07801234567"
    var ZAINCASH_ACCOUNT = "0780 123 4567"
    var ZAINCASH_HOLDER = "YourStudio"

    // 4. Qi Card (قی کارت)
    var QICARD_QR_URL = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=QiCard:9876543210987654"
    var QICARD_ACCOUNT = "9876 5432 1098 7654"
    var QICARD_HOLDER = "YourStudio Iraq"
}

enum class PaymentGatewayType(
    val titleKurdish: String,
    val brandName: String,
    val accentHex: String
) {
    FASTPAY("فاست پەی", "FastPay", "#E11D48"),
    FIB("بانکی یەکەمی عێراقی", "FIB", "#0284C7"),
    ZAINCASH("زەین کاش", "ZainCash", "#9333EA"),
    QICARD("قی کارت", "Qi Card", "#059669")
}
