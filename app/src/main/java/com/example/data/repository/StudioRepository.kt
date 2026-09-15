package com.example.data.repository

import com.example.data.local.OrderDao
import com.example.data.local.UserDao
import com.example.data.model.CreationOrder
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudioRepository(
    private val userDao: UserDao,
    private val orderDao: OrderDao
) {
    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()
    val allOrders: Flow<List<CreationOrder>> = orderDao.getAllOrders()
    val pendingOrders: Flow<List<CreationOrder>> = orderDao.getPendingOrders()
    val completedOrders: Flow<List<CreationOrder>> = orderDao.getCompletedOrders()

    suspend fun ensureProfileExists(): UserProfile {
        val initial = UserProfile(
            id = 1,
            isLoggedIn = false,
            authMethod = "guest",
            username = "میوانی بەڕێز",
            email = "",
            phoneNumber = "",
            age = 22,
            logoCredits = 1,
            videoCredits = 1,
            imageCredits = 1,
            promoCredits = 1,
            isUnlimited = false,
            hasUnlimitedAiLogos = false,
            totalVideosPurchasedOrCreated = 0,
            totalLogosPurchasedOrCreated = 0,
            totalImagesPurchasedOrCreated = 0
        )
        userDao.insertOrUpdateProfile(initial)
        return initial
    }

    suspend fun updateProfile(profile: UserProfile) {
        userDao.insertOrUpdateProfile(profile)
    }

    suspend fun loginWithProvider(
        provider: String,
        username: String,
        emailOrPhone: String,
        currentProfile: UserProfile
    ) {
        val updated = currentProfile.copy(
            isLoggedIn = true,
            authMethod = provider,
            username = username.ifBlank {
                when (provider) {
                    "apple" -> "Apple User"
                    "google" -> "Google User"
                    else -> "بەکار‌هێنەر"
                }
            },
            email = if (provider == "email" || provider == "google" || provider == "apple") emailOrPhone else currentProfile.email,
            phoneNumber = if (provider == "phone") emailOrPhone else currentProfile.phoneNumber
        )
        userDao.insertOrUpdateProfile(updated)
    }

    suspend fun logout(currentProfile: UserProfile) {
        val updated = currentProfile.copy(
            isLoggedIn = false,
            authMethod = "guest"
        )
        userDao.insertOrUpdateProfile(updated)
    }

    suspend fun addCreditsFromPackage(packageType: String, currentProfile: UserProfile) {
        val updated = when (packageType) {
            "single_logo" -> currentProfile.copy(
                logoCredits = currentProfile.logoCredits + 1,
                totalLogosPurchasedOrCreated = currentProfile.totalLogosPurchasedOrCreated + 1
            )
            "single_video" -> currentProfile.copy(
                videoCredits = currentProfile.videoCredits + 1,
                totalVideosPurchasedOrCreated = currentProfile.totalVideosPurchasedOrCreated + 1
            )
            "single_image" -> currentProfile.copy(
                imageCredits = currentProfile.imageCredits + 1,
                totalImagesPurchasedOrCreated = currentProfile.totalImagesPurchasedOrCreated + 1
            )
            "promo_discount" -> currentProfile.copy(
                promoCredits = currentProfile.promoCredits + 3
            )
            "pkg_5" -> currentProfile.copy(
                logoCredits = currentProfile.logoCredits + 7,
                imageCredits = currentProfile.imageCredits + 3,
                totalLogosPurchasedOrCreated = currentProfile.totalLogosPurchasedOrCreated + 7,
                totalImagesPurchasedOrCreated = currentProfile.totalImagesPurchasedOrCreated + 3
            )
            "pkg_7" -> currentProfile.copy(
                promoCredits = currentProfile.promoCredits + 7,
                imageCredits = currentProfile.imageCredits + 5,
                totalImagesPurchasedOrCreated = currentProfile.totalImagesPurchasedOrCreated + 5
            )
            "pkg_10" -> currentProfile.copy(
                videoCredits = currentProfile.videoCredits + 5,
                logoCredits = currentProfile.logoCredits + 10,
                promoCredits = currentProfile.promoCredits + 5,
                imageCredits = currentProfile.imageCredits + 5,
                totalVideosPurchasedOrCreated = currentProfile.totalVideosPurchasedOrCreated + 5,
                totalLogosPurchasedOrCreated = currentProfile.totalLogosPurchasedOrCreated + 10,
                totalImagesPurchasedOrCreated = currentProfile.totalImagesPurchasedOrCreated + 5
            )
            "pkg_20" -> currentProfile.copy(
                logoCredits = currentProfile.logoCredits + 30,
                videoCredits = currentProfile.videoCredits + 20,
                promoCredits = currentProfile.promoCredits + 1,
                imageCredits = currentProfile.imageCredits + 10,
                hasUnlimitedAiLogos = true,
                totalVideosPurchasedOrCreated = currentProfile.totalVideosPurchasedOrCreated + 20,
                totalLogosPurchasedOrCreated = currentProfile.totalLogosPurchasedOrCreated + 30,
                totalImagesPurchasedOrCreated = currentProfile.totalImagesPurchasedOrCreated + 10
            )
            "pkg_35" -> currentProfile.copy(
                isUnlimited = true,
                hasUnlimitedAiLogos = true,
                logoCredits = 999,
                videoCredits = 999,
                imageCredits = 999,
                promoCredits = 999,
                totalVideosPurchasedOrCreated = currentProfile.totalVideosPurchasedOrCreated + 99,
                totalLogosPurchasedOrCreated = currentProfile.totalLogosPurchasedOrCreated + 99,
                totalImagesPurchasedOrCreated = currentProfile.totalImagesPurchasedOrCreated + 99
            )
            else -> currentProfile
        }
        userDao.insertOrUpdateProfile(updated)
    }

    suspend fun createOrder(
        type: String,
        category: String,
        projectName: String,
        userName: String,
        details: String,
        sampleFileName: String,
        primaryFileName: String,
        isAiFast: Boolean,
        status: String,
        resultContent: String,
        currentProfile: UserProfile
    ): CreationOrder {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())
        val generatedOrderId = "ORD-" + (10000..99999).random()

        val order = CreationOrder(
            orderId = generatedOrderId,
            type = type,
            category = category,
            projectName = projectName,
            userName = userName,
            details = details,
            sampleFileName = sampleFileName,
            primaryFileName = primaryFileName,
            isAiFast = isAiFast,
            status = status,
            resultContent = resultContent,
            dateTimestamp = System.currentTimeMillis(),
            dateFormatted = formattedDate
        )
        val orderId = orderDao.insertOrder(order)

        // Deduct 1 credit if not unlimited
        if (!currentProfile.isUnlimited) {
            val updatedProfile = when (type) {
                "LOGO" -> currentProfile.copy(
                    logoCredits = (currentProfile.logoCredits - 1).coerceAtLeast(0),
                    totalLogosPurchasedOrCreated = currentProfile.totalLogosPurchasedOrCreated + 1
                )
                "VIDEO" -> currentProfile.copy(
                    videoCredits = (currentProfile.videoCredits - 1).coerceAtLeast(0),
                    totalVideosPurchasedOrCreated = currentProfile.totalVideosPurchasedOrCreated + 1
                )
                "IMAGE" -> currentProfile.copy(
                    imageCredits = (currentProfile.imageCredits - 1).coerceAtLeast(0),
                    totalImagesPurchasedOrCreated = currentProfile.totalImagesPurchasedOrCreated + 1
                )
                "PROMO_CODE" -> currentProfile.copy(
                    promoCredits = (currentProfile.promoCredits - 1).coerceAtLeast(0),
                    totalPromoCodesCreated = currentProfile.totalPromoCodesCreated + 1
                )
                else -> currentProfile
            }
            userDao.insertOrUpdateProfile(updatedProfile)
        } else {
            val updatedProfile = when (type) {
                "LOGO" -> currentProfile.copy(totalLogosPurchasedOrCreated = currentProfile.totalLogosPurchasedOrCreated + 1)
                "VIDEO" -> currentProfile.copy(totalVideosPurchasedOrCreated = currentProfile.totalVideosPurchasedOrCreated + 1)
                "IMAGE" -> currentProfile.copy(totalImagesPurchasedOrCreated = currentProfile.totalImagesPurchasedOrCreated + 1)
                "PROMO_CODE" -> currentProfile.copy(totalPromoCodesCreated = currentProfile.totalPromoCodesCreated + 1)
                else -> currentProfile
            }
            userDao.insertOrUpdateProfile(updatedProfile)
        }

        return order.copy(id = orderId)
    }

    suspend fun updateOrder(order: CreationOrder) {
        orderDao.updateOrder(order)
    }

    suspend fun completePendingOrder(orderId: Long, finalContent: String) {
        val existing = orderDao.getOrderById(orderId)
        if (existing != null) {
            val updated = existing.copy(
                status = "COMPLETED",
                resultContent = finalContent
            )
            orderDao.updateOrder(updated)
        }
    }
    suspend fun completeOrderByOrderIdString(orderIdString: String, content: String): Boolean {
        val order = orderDao.getOrderByOrderId(orderIdString)
        if (order != null && order.status == "PENDING_CREATOR") {
            orderDao.updateOrder(order.copy(status = "COMPLETED", resultContent = content))
            return true
        }
        return false
    }
}
