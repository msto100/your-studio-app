package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val isLoggedIn: Boolean = false,
    val authMethod: String = "guest", // "apple", "google", "phone", "email", "guest"
    val username: String = "بەکار‌هێنەر",
    val email: String = "",
    val phoneNumber: String = "",
    val age: Int = 22,
    val avatarUri: String = "",
    val logoCredits: Int = 2,
    val videoCredits: Int = 1,
    val imageCredits: Int = 2,
    val promoCredits: Int = 3,
    val isUnlimited: Boolean = false, // from $35 package
    val hasUnlimitedAiLogos: Boolean = false, // from $20 or $35 package
    val totalVideosPurchasedOrCreated: Int = 0,
    val totalLogosPurchasedOrCreated: Int = 0,
    val totalImagesPurchasedOrCreated: Int = 0,
    val totalPromoCodesCreated: Int = 0,
    val isAgent: Boolean = false,
    val agentId: String = ""
)
