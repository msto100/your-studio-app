package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creation_orders")
data class CreationOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String = "",
    val type: String, // "LOGO", "VIDEO", "IMAGE", "PROMO_CODE"
    val category: String, // e.g. "لۆگۆی براند", "ڤیدیۆی گەیمینگ"
    val projectName: String,
    val userName: String,
    val details: String,
    val sampleFileName: String = "",
    val primaryFileName: String = "",
    val isAiFast: Boolean = false,
    val status: String = "PENDING_CREATOR", // "PENDING_CREATOR", "COMPLETED"
    val resultContent: String = "", // Prompt code text, SVG/graphic description, download identifier
    val ratingStatus: String = "NONE", // "NONE", "SATISFIED", "NEEDS_CHANGE"
    val retryCount: Int = 0,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String = ""
)
