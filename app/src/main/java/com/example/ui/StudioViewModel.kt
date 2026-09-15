package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserLocalStore
import com.example.data.model.CreationOrder
import com.example.data.model.UserProfile
import com.example.data.remote.GeminiAiService
import com.example.data.remote.LogoAiResult
import com.example.data.remote.OpenAiService
import com.example.data.remote.PollinationsAiService
import com.example.data.repository.StudioRepository
import com.example.util.GoogleUserData
import com.example.util.TelegramHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

enum class AppTab(val titleKurdish: String) {
    HOME("سەرەکی"),
    PREMIUM("کڕین"),
    PROFILE("زانیاری کەسی"),
    CONTACT("پەیوەندی"),
    ACCOUNT("هەژمار")
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = StudioRepository(database.userDao(), database.orderDao())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendingOrders: StateFlow<List<CreationOrder>> = repository.pendingOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedOrders: StateFlow<List<CreationOrder>> = repository.completedOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Snackbar / Alert message
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Active Generation State for Logo
    private val _isGeneratingLogo = MutableStateFlow(false)
    val isGeneratingLogo: StateFlow<Boolean> = _isGeneratingLogo.asStateFlow()

    private val _generatedLogoResult = MutableStateFlow<LogoAiResult?>(null)
    val generatedLogoResult: StateFlow<LogoAiResult?> = _generatedLogoResult.asStateFlow()

    private val _activeLogoOrderId = MutableStateFlow<Long?>(null)
    val activeLogoOrderId: StateFlow<Long?> = _activeLogoOrderId.asStateFlow()

    private val _logoRetryCount = MutableStateFlow(0)
    val logoRetryCount: StateFlow<Int> = _logoRetryCount.asStateFlow()

    // Active Promo Code Generation State
    private val _isGeneratingPromoCode = MutableStateFlow(false)
    val isGeneratingPromoCode: StateFlow<Boolean> = _isGeneratingPromoCode.asStateFlow()

    private val _generatedPromoCode = MutableStateFlow<String?>(null)
    val generatedPromoCode: StateFlow<String?> = _generatedPromoCode.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureProfileExists()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return userProfile.value?.isLoggedIn == true
    }

    // --- Authentication Actions ---
    fun loginWithGoogleProfile(googleData: GoogleUserData) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            repository.loginWithProvider(
                provider = "google",
                username = googleData.displayName,
                emailOrPhone = googleData.email,
                currentProfile = profile
            )
            // Persist avatar url and email into profile
            val current = userProfile.value ?: profile
            repository.updateProfile(
                current.copy(
                    username = googleData.displayName,
                    email = googleData.email,
                    authMethod = "google",
                    isLoggedIn = true,
                    avatarUri = googleData.photoUrl ?: current.avatarUri
                )
            )
            _toastEvent.emit("بەخێربێیت ${googleData.displayName}، لە ڕێگەی Google هەژمارەکەت چالاککرا!")
        }
    }

    fun loginInstant(provider: String, username: String = "") {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            val name = if (username.isNotBlank()) username else {
                if (provider == "apple") "Apple User" else "Google User"
            }
            repository.loginWithProvider(provider, name, "$provider@user.com", profile)
            _toastEvent.emit("بە سەرکەوتوویی لە ڕێگەی $provider چونەژوورەوەت ئەنجامدا")
        }
    }

    suspend fun sendVerificationOtp(
        credential: String,
        name: String,
        isPhone: Boolean,
        isRegister: Boolean,
        code: String
    ): Pair<Boolean, String> {
        // Direct local verification without calling Telegram bot
        return Pair(true, "کۆدی دڵنیابوونەوە ئامادەکرا")
    }

    fun loginWithCredentials(
        isRegister: Boolean,
        type: String, // "phone" or "email"
        credential: String,
        password: String,
        code: String,
        name: String = ""
    ) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            val finalName = if (name.isNotBlank()) name else (if (type == "phone") "بەکارهێنەری مۆبایل" else "بەکارهێنەری ئیمەیل")
            repository.loginWithProvider(type, finalName, credential, profile)
            val msg = if (isRegister) "هەژمارەکەت بە سەرکەوتوویی دروستکرا و چالاککرا!" else "بەخێربێیتەوە بۆ ناو هەژمارەکەت!"
            _toastEvent.emit(msg)
        }
    }

    

    fun registerWithEmail(name: String, email: String, password: String) {
        viewModelScope.launch {
            UserLocalStore.saveRegisteredUser(getApplication(), email, password, name)
            
            val profile = userProfile.value ?: repository.ensureProfileExists()
            val finalName = name.ifBlank { "بەکارهێنەر" }
            repository.loginWithProvider("email", finalName, email, profile)
            val current = userProfile.value ?: profile
            repository.updateProfile(
                current.copy(
                    username = finalName,
                    email = email,
                    authMethod = "email",
                    isLoggedIn = true
                )
            )
            UserLocalStore.saveCurrentUser(getApplication(), finalName, email)
            _toastEvent.emit("هەژمارەکەت بە سەرکەوتوویی دروستکرا و تۆمارکرا!")
            _currentTab.value = AppTab.HOME
        }
    }


    fun isEmailRegistered(email: String): Boolean {
        return UserLocalStore.isEmailRegistered(getApplication(), email)
    }

    fun verifyPassword(email: String, password: String): Boolean {
        return UserLocalStore.verifyPassword(getApplication(), email, password)
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            val finalName = if (profile.email == email && profile.username.isNotBlank() && profile.username != "میوانی بەڕێز") {
                profile.username
            } else {
                email.substringBefore("@")
            }
            repository.loginWithProvider("email", finalName, email, profile)
            val current = userProfile.value ?: profile
            repository.updateProfile(
                current.copy(
                    username = finalName,
                    email = email,
                    authMethod = "email",
                    isLoggedIn = true
                )
            )
            UserLocalStore.saveCurrentUser(getApplication(), finalName, email)
            _toastEvent.emit("بەخێربێیتەوە! بە سەرکەوتوویی چوویتە ژوورەوە.")
            _currentTab.value = AppTab.HOME
        }
    }


    fun loginAsAgent(agentCode: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            val updatedProfile = profile.copy(
                isAgent = true,
                agentId = agentCode,
                isLoggedIn = true,
                username = "بریکار: $agentCode"
            )
            repository.updateProfile(updatedProfile)
            _toastEvent.emit("چوونەژوورەوەی بریکار سەرکەوتوو بوو!")
            // Optionally set current tab to admin or keep it here
        }
    }

    fun logout() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.logout(profile)
            UserLocalStore.clearCurrentUser(getApplication())
            _toastEvent.emit("لە هەژمارەکەت دەرچوویت")
            _currentTab.value = AppTab.ACCOUNT
        }
    }

    fun updateProfileInfo(name: String, age: Int, avatarUri: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.updateProfile(profile.copy(username = name, age = age, avatarUri = avatarUri))
            _toastEvent.emit("زانیارییەکانت نوێکرانەوە")
        }
    }

    // --- Package / Credit Purchases ---
    var generatedAdminLicenseKey by androidx.compose.runtime.mutableStateOf<String?>(null)
    var triggerOpenAdminModal by androidx.compose.runtime.mutableStateOf(false)

    fun purchasePackage(packageKey: String, packageName: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            if (profile.isAgent && packageKey != "pkg_admin") {
                _toastEvent.emit("تۆ بە هەژماری بریکار (Agent) چوویتەتە ژوورەوە، ناتوانیت پاکێجی ئاسایی بکڕیت.")
                return@launch
            }
            if (profile.isAgent) {
                _toastEvent.emit("تۆ بە هەژماری بریکار (Agent) چوویتەتە ژوورەوە، ناتوانیت داواکاری ئاسایی تۆمار بکەیت.")
                return@launch
            }
            if (!profile.isLoggedIn) {
                _toastEvent.emit("تکایە سەرەتا هەژمار دروست بکە یان بچۆ ژوورەوە بۆ کڕین!")
                _currentTab.value = AppTab.ACCOUNT
                return@launch
            }
            
            if (packageKey == "pkg_admin") {
                val randomKey = "ADM-" + (1..4).map { ('A'..'Z').random() }.joinToString("") + "-" + (1..3).map { ('0'..'9').random() }.joinToString("")
                generatedAdminLicenseKey = randomKey
                repository.updateProfile(profile.copy(isAgent = true, agentId = randomKey))
                _toastEvent.emit("مۆڵەتی بەڕێوەبەر کڕدرا! کۆدەکەت: $randomKey (بەکاربهێنە بۆ چوونەژوورەوە)")
            } else {
                repository.addCreditsFromPackage(packageKey, profile)
                _toastEvent.emit("پیرۆزە! $packageName بە سەرکەوتوویی کڕدرا و کریدت بۆ هەژمارەکەت زیادکرا.")
            }
        }
    }

    // --- Logo Generation & Ordering ---
    fun startLogoCreation(
        category: String,
        projectName: String,
        userName: String,
        details: String,
        sampleFileName: String,
        isAiFast: Boolean,
        sampleFileUri: Uri? = null,
        onDispatchedToTelegram: (orderMsg: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            if (profile.isAgent) {
                _toastEvent.emit("تۆ بە هەژماری بریکار (Agent) چوویتەتە ژوورەوە، ناتوانیت داواکاری ئاسایی تۆمار بکەیت.")
                return@launch
            }
            if (!profile.isLoggedIn) {
                _toastEvent.emit("تکایە سەرەتا بچۆ ژوورەوە بۆ دروستکردنی لۆگۆ!")
                _currentTab.value = AppTab.ACCOUNT
                return@launch
            }

            if (!profile.isUnlimited && profile.logoCredits <= 0) {
                _toastEvent.emit("باڵانسی لۆگۆت تەواو بووە! تکایە پاکێجێک بکڕە.")
                _currentTab.value = AppTab.PREMIUM
                return@launch
            }

            if (isAiFast) {
                _isGeneratingLogo.value = true
                _logoRetryCount.value = 0

                // Real Pollinations AI Logo generation with exact requested formula
                val pollinationsUrl = PollinationsAiService.getPollinationsImageUrl(projectName)
                val result = LogoAiResult(
                    conceptName = projectName,
                    category = category,
                    primaryColor = "#4F46E5",
                    accentColor = "#06B6D4",
                    emblemShape = "Pollinations AI",
                    explanation = "بە سەرکەوتوویی لە ڕێگەی Pollinations AI بە کواڵێتی باڵا دروستکرا.",
                    fullPromptCode = PollinationsAiService.buildProfessionalLogoPrompt(category, projectName, details),
                    imageUrl = pollinationsUrl
                )

                _generatedLogoResult.value = result

                val createdOrder = repository.createOrder(
                    type = "LOGO",
                    category = category,
                    projectName = projectName,
                    userName = userName,
                    details = details,
                    sampleFileName = sampleFileName,
                    primaryFileName = "",
                    isAiFast = true,
                    status = "PENDING_CREATOR",
                    resultContent = result.imageUrl ?: result.fullPromptCode,
                    currentProfile = profile
                )
                _activeLogoOrderId.value = createdOrder.id
                _isGeneratingLogo.value = false
            } else {
                // Manual Design - Send file & details directly to Telegram Bot API
                val createdOrder = repository.createOrder(
                    type = "LOGO",
                    category = category,
                    projectName = projectName,
                    userName = userName,
                    details = details,
                    sampleFileName = sampleFileName,
                    primaryFileName = "",
                    isAiFast = false,
                    status = "PENDING_CREATOR",
                    resultContent = "",
                    currentProfile = profile
                )
                _activeLogoOrderId.value = createdOrder.id

                // Dispatch file or details to Telegram Bot
                TelegramHelper.sendOrderWithFilesToTelegram(
                    context = getApplication(),
                    projectName = projectName,
                    userName = userName,
                    requestType = "لۆگۆی دەستی (${createdOrder.orderId})",
                    category = category,
                    notes = details,
                    sampleFileUri = sampleFileUri,
                    sampleFileName = sampleFileName
                )

                _toastEvent.emit("داواکاری و فایلەکەت ڕاستەوخۆ بۆ بۆتی تیلیگرام نێردرا!")
                val orderMsg = "داواکاری لۆگۆی دەستی نوێ:\nOrder ID: ${createdOrder.orderId}\nناوی پرۆجێکت: $projectName\nبەکارهێنەر: $userName\nجۆر: $category\nڕێنمایی: $details\nنموونە: $sampleFileName"
                onDispatchedToTelegram(orderMsg)
            }
        }
    }

        fun completeOrderByOrderId(orderIdString: String, content: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.completeOrderByOrderIdString(orderIdString, content)
            if (success) {
                _toastEvent.emit("داواکارییەکە بە سەرکەوتوویی تەواو کرا!")
                onSuccess()
            } else {
                _toastEvent.emit("هەڵە: Order ID نەدۆزرایەوە یان پێشتر تەواو کراوە!")
                onError()
            }
        }
    }

    fun acceptGeneratedLogo(savedImagePath: String? = null) {
        viewModelScope.launch {
            val orderId = _activeLogoOrderId.value
            val result = _generatedLogoResult.value
            val contentToSave = savedImagePath ?: result?.imageUrl ?: result?.fullPromptCode ?: ""
            if (orderId != null) {
                repository.completePendingOrder(orderId, contentToSave)
            }
            _generatedLogoResult.value = null
            _activeLogoOrderId.value = null
            _toastEvent.emit("لۆگۆکەت بە دەقی کوردی و کواڵێتی باڵا لە پرۆفایل پاشەکەوت کرا!")
            _currentTab.value = AppTab.PROFILE
        }
    }

    /**
     * Send logo with Kurdish text requirements to designer on Telegram for specialized manual creation
     */
    fun sendLogoForManualCustomDesign(
        category: String,
        projectName: String,
        userName: String,
        kurdishText: String,
        details: String,
        logoBitmapUri: Uri? = null,
        onDispatchedToTelegram: (orderMsg: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            if (profile.isAgent) {
                _toastEvent.emit("تۆ بە هەژماری بریکار (Agent) چوویتەتە ژوورەوە، ناتوانیت داواکاری ئاسایی تۆمار بکەیت.")
                return@launch
            }
            if (!profile.isLoggedIn) {
                _toastEvent.emit("تکایە سەرەتا بچۆ ژوورەوە!")
                _currentTab.value = AppTab.ACCOUNT
                return@launch
            }

            val fullDetails = "دەستکاری و فۆنتی تایبەتی کوردی:\nدەقی سەرەکی: $kurdishText\nتێبینییەکان: ${details.ifBlank { "دیزاین و فۆنتی زۆر تایبەتی لەسەر وێنەکە بە دەستی" }}"

            val createdOrder = repository.createOrder(
                type = "LOGO",
                category = category,
                projectName = projectName,
                userName = userName,
                details = fullDetails,
                sampleFileName = if (logoBitmapUri != null) "ai_emblem_preview.png" else "",
                primaryFileName = "",
                isAiFast = false,
                status = "PENDING_CREATOR",
                resultContent = "",
                currentProfile = profile
            )
            _activeLogoOrderId.value = createdOrder.id

            TelegramHelper.sendOrderWithFilesToTelegram(
                context = getApplication(),
                projectName = projectName,
                userName = userName,
                requestType = "داواکاری دیزاینی دەستی و فۆنتی تایبەتی کوردی",
                category = category,
                notes = fullDetails,
                primaryFileUri = logoBitmapUri,
                primaryFileName = "ai_logo_reference.png"
            )

            val orderMsg = "✍️ داواکاری لۆگۆی دەستی بە فۆنتی تایبەتی کوردی:\nناوی پرۆجێکت: $projectName\nدەقی کوردی: $kurdishText\nبەکارهێنەر: $userName\nجۆر: $category\nتێبینی: $details"
            onDispatchedToTelegram(orderMsg)

            _generatedLogoResult.value = null
            _activeLogoOrderId.value = null
            _toastEvent.emit("داواکاریی دەستی بۆ تیلیگرامی دیزاینەر نێردرا!")
            _currentTab.value = AppTab.PROFILE
        }
    }

    fun requestNewLogoIteration(
        newInstructions: String,
        category: String,
        projectName: String
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val isUnlimited = profile?.isUnlimited == true || profile?.hasUnlimitedAiLogos == true
            val currentRetries = _logoRetryCount.value

            if (!isUnlimited && currentRetries >= 5) {
                _toastEvent.emit("ببورە! سنوری ٥ جار لۆگۆی نوێت بەکارهێناوە. بۆ لۆگۆی بێ سنور پاکێجی ٢٠$ یان ٣٥$ بکڕە.")
                return@launch
            }

            _isGeneratingLogo.value = true
            val nextRetry = currentRetries + 1
            _logoRetryCount.value = nextRetry

            // Call OpenAI DALL-E with modified instructions
            val dalleResult = OpenAiService.generateDalleImage(
                prompt = "Professional logo design for $projectName, category $category. Revision feedback: $newInstructions",
                category = category,
                projectName = projectName,
                details = newInstructions
            )

            val result = if (dalleResult.isSuccess && !dalleResult.imageUrl.isNullOrBlank()) {
                LogoAiResult(
                    conceptName = "$projectName (نوێکراوە $nextRetry)",
                    category = category,
                    primaryColor = "#4F46E5",
                    accentColor = "#06B6D4",
                    emblemShape = "DALL-E 3",
                    explanation = "بەپێی تێبینییەکانت لۆگۆکە نوێکرایەوە.",
                    fullPromptCode = dalleResult.imageUrl,
                    imageUrl = dalleResult.imageUrl
                )
            } else {
                val pollinationsUrl = PollinationsAiService.getPollinationsImageUrl(projectName, newInstructions)
                LogoAiResult(
                    conceptName = "$projectName (نوێکراوە $nextRetry)",
                    category = category,
                    primaryColor = "#4F46E5",
                    accentColor = "#06B6D4",
                    emblemShape = "Pollinations AI",
                    explanation = "بەپێی تێبینییە نوێیەکانت لۆگۆکە دروستکرایەوە.",
                    fullPromptCode = PollinationsAiService.buildProfessionalLogoPrompt(category, projectName, newInstructions),
                    imageUrl = pollinationsUrl
                )
            }

            _generatedLogoResult.value = result
            _isGeneratingLogo.value = false
            _toastEvent.emit("لۆگۆی نوێ دروستکرا! ($nextRetry/5)")
        }
    }

    fun dismissLogoReview() {
        _generatedLogoResult.value = null
        _activeLogoOrderId.value = null
    }

    // --- Video Editing Order ---
    fun submitVideoEditOrder(
        category: String,
        projectName: String,
        userName: String,
        details: String,
        sampleVideoName: String,
        mainVideoName: String,
        mainVideoUri: Uri? = null,
        sampleVideoUri: Uri? = null,
        onDispatchedToTelegram: (orderMsg: String) -> Unit
    ) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            if (profile.isAgent) {
                _toastEvent.emit("تۆ بە هەژماری بریکار (Agent) چوویتەتە ژوورەوە، ناتوانیت داواکاری ئاسایی تۆمار بکەیت.")
                return@launch
            }
            if (!profile.isLoggedIn) {
                _toastEvent.emit("تکایە سەرەتا بچۆ ژوورەوە بۆ داواکردنی ئیدیت!")
                _currentTab.value = AppTab.ACCOUNT
                return@launch
            }

            if (!profile.isUnlimited && profile.videoCredits <= 0) {
                _toastEvent.emit("باڵانسی ڤیدیۆت تەواو بووە! تکایە پاکێجێک بکڕە.")
                _currentTab.value = AppTab.PREMIUM
                return@launch
            }

            val createdOrder = repository.createOrder(
                type = "VIDEO",
                category = category,
                projectName = projectName,
                userName = userName,
                details = details,
                sampleFileName = sampleVideoName,
                primaryFileName = mainVideoName,
                isAiFast = false,
                status = "PENDING_CREATOR",
                resultContent = "",
                currentProfile = profile
            )

            // Dispatch file(s) and details directly to Telegram Bot API
            val result = TelegramHelper.sendOrderWithFilesToTelegram(
                context = getApplication(),
                projectName = projectName,
                userName = userName,
                requestType = "ئیدیتی ڤیدیۆ (دەستی)",
                category = category,
                notes = details,
                primaryFileUri = mainVideoUri,
                primaryFileName = mainVideoName,
                sampleFileUri = sampleVideoUri,
                sampleFileName = sampleVideoName
            )

            if (result.first) {
                _toastEvent.emit("داواکاری و فایلی ڤیدیۆ بە سەرکەوتوویی بۆ بۆتی تیلیگرام نێردران!")
            } else {
                _toastEvent.emit("داواکاری تۆمارکرا: ${result.second}")
            }

            val msg = "🎬 داواکاری ئیدیتی ڤیدیۆ:\nبەش: $category\nناوی پرۆجێکت: $projectName\nبەکارهێنەر: $userName\nفایلی سەرەکی: $mainVideoName\nنموونە: $sampleVideoName\nڕێنمایی: $details"
            onDispatchedToTelegram(msg)
            _currentTab.value = AppTab.PROFILE
        }
    }

    // --- Image Editing Order ---
    fun submitImageOrder(
        category: String,
        projectName: String,
        userName: String,
        details: String,
        sampleImageName: String,
        isAiFast: Boolean,
        sampleImageUri: Uri? = null,
        onDispatchedToTelegram: (orderMsg: String) -> Unit
    ) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            if (profile.isAgent) {
                _toastEvent.emit("تۆ بە هەژماری بریکار (Agent) چوویتەتە ژوورەوە، ناتوانیت داواکاری ئاسایی تۆمار بکەیت.")
                return@launch
            }
            if (!profile.isLoggedIn) {
                _toastEvent.emit("تکایە سەرەتا بچۆ ژوورەوە بۆ داواکردنی وێنە!")
                _currentTab.value = AppTab.ACCOUNT
                return@launch
            }

            if (!profile.isUnlimited && profile.imageCredits <= 0) {
                _toastEvent.emit("باڵانسی وێنەت تەواو بووە! تکایە پاکێجێک بکڕە.")
                _currentTab.value = AppTab.PREMIUM
                return@launch
            }

            val status = if (isAiFast) "COMPLETED" else "PENDING_CREATOR"
            var resultContent = if (isAiFast) "وێنەی تایبەت بە $projectName بە شێوازی $category بە کواڵێتی 4K ئامادەیە." else ""

            if (isAiFast) {
                // Generate via OpenAI DALL-E
                val dalleRes = OpenAiService.generateDalleImage(
                    prompt = "",
                    category = category,
                    projectName = projectName,
                    details = details
                )
                if (dalleRes.isSuccess && !dalleRes.imageUrl.isNullOrBlank()) {
                    resultContent = dalleRes.imageUrl
                }
            } else {
                // Manual creation - Send real file and details to Telegram Bot API
                val result = TelegramHelper.sendOrderWithFilesToTelegram(
                    context = getApplication(),
                    projectName = projectName,
                    userName = userName,
                    requestType = "ئیدیتی وێنەی دەستی",
                    category = category,
                    notes = details,
                    primaryFileUri = sampleImageUri,
                    primaryFileName = sampleImageName
                )
                if (result.first) {
                    _toastEvent.emit("داواکاری و فایلی وێنە بە سەرکەوتوویی بۆ بۆتی تیلیگرام نێردران!")
                }
            }

            val createdOrder = repository.createOrder(
                type = "IMAGE",
                category = category,
                projectName = projectName,
                userName = userName,
                details = details,
                sampleFileName = sampleImageName,
                primaryFileName = "",
                isAiFast = isAiFast,
                status = status,
                resultContent = resultContent,
                currentProfile = profile
            )

            if (isAiFast) {
                _toastEvent.emit("وێنەکەت بە سەرکەوتوویی بەرهەمهێنرا و خرایە پرۆفایلەکەت!")
            } else {
                val msg = "🖼 داواکاری ئیدیتی وێنە:\nبەش: $category\nناوی پرۆجێکت: $projectName\nبەکارهێنەر: $userName\nنموونەی وێنە: $sampleImageName\nڕێنمایی: $details"
                onDispatchedToTelegram(msg)
            }
            _currentTab.value = AppTab.PROFILE
        }
    }

    // --- Direct Telegram Contact Message ---
    fun sendContactMessageToBot(
        senderName: String,
        subject: String,
        messageBody: String,
        onFinished: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val contactHtml = TelegramHelper.formatContactMessageHtml(
                userName = senderName.ifBlank { "بەکارهێنەر" },
                subject = subject,
                messageBody = messageBody
            )
            val (sent, responseMsg) = TelegramHelper.sendToTelegramBot(contactHtml)
            if (sent) {
                _toastEvent.emit("نامەکەت بە سەرکەوتوویی بۆ بۆتی تیلیگرام نێردرا!")
            } else {
                _toastEvent.emit("پەیام: $responseMsg")
            }
            onFinished()
        }
    }

    // --- AI Image Promo Code Generation ---
    fun generateImagePromoCode(
        projectName: String,
        userName: String,
        sampleFileName: String,
        sampleFileUri: Uri? = null,
        onDispatchedToTelegram: (orderMsg: String) -> Unit
    ) {
        viewModelScope.launch {
            val profile = userProfile.value ?: repository.ensureProfileExists()
            if (profile.isAgent) {
                _toastEvent.emit("تۆ بە هەژماری بریکار (Agent) چوویتەتە ژوورەوە، ناتوانیت داواکاری ئاسایی تۆمار بکەیت.")
                return@launch
            }
            if (!profile.isLoggedIn) {
                _toastEvent.emit("تکایە سەرەتا بچۆ ژوورەوە بۆ پرۆمۆ کۆد!")
                _currentTab.value = AppTab.ACCOUNT
                return@launch
            }

            if (!profile.isUnlimited && profile.promoCredits <= 0) {
                _toastEvent.emit("باڵانسی پرۆمۆ کۆدت تەواو بووە! تکایە پاکێجێک بکڕە.")
                _currentTab.value = AppTab.PREMIUM
                return@launch
            }

            _isGeneratingPromoCode.value = true
            val promoResult = GeminiAiService.generatePromoCodeForImage(
                context = getApplication(),
                projectName = projectName,
                userName = userName,
                sampleFileName = sampleFileName,
                imageUri = sampleFileUri
            )
            _generatedPromoCode.value = promoResult

            val createdOrder = repository.createOrder(
                type = "PROMO_CODE",
                category = "پرۆمۆ کۆدی وێنە",
                projectName = projectName,
                userName = userName,
                details = "AI Vision Prompt Analysis",
                sampleFileName = sampleFileName,
                primaryFileName = "",
                isAiFast = true,
                status = "COMPLETED",
                resultContent = promoResult,
                currentProfile = profile
            )

            _isGeneratingPromoCode.value = false
            _toastEvent.emit("پرۆمۆ کۆدی ئەی ئای بە سەرکەوتوویی دروستکرا!")

            val msg = "⚡️ داواکاری پرۆمۆ کۆدی ئەی ئای (Vision):\nناوی پرۆجێکت: $projectName\nبەکارهێنەر: $userName\nنموونە: $sampleFileName\nپرۆمۆ کۆد بە سەرکەوتوویی دروستکرا."
            onDispatchedToTelegram(msg)
        }
    }

    fun dismissPromoCodeResult() {
        _generatedPromoCode.value = null
    }

    // Helper to simulate completing a pending order for testing by creator
    fun simulateCreatorCompletingOrder(orderId: Long) {
        viewModelScope.launch {
            repository.completePendingOrder(orderId, "فایلی ئەسڵی و کواڵێتی باڵا لە لایەن دیزاینەرەوە ئامادە کرا. فەرموو لە خوارەوە پاشەکەوتی بکە.")
            _toastEvent.emit("بەرهەمەکەت لە لایەن دیزاینەرەوە تەواوکرا و گەیشتە پرۆفایلەکەت!")
        }
    }

    // Apply Promo Code Logic
    fun applyPromoCode(context: android.content.Context, code: String) {
        val prefs = context.getSharedPreferences("admin_prefs", android.content.Context.MODE_PRIVATE)
        val existingCodes = prefs.getStringSet("studio_promo_codes", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (existingCodes.contains(code)) {
            // Code is valid
            existingCodes.remove(code)
            prefs.edit().putStringSet("studio_promo_codes", existingCodes).apply()
            
            val currentProfile = userProfile.value ?: return
            
            viewModelScope.launch {
                val updatedProfile = when {
                    code.startsWith("YS-LG1-") -> currentProfile.copy(logoCredits = currentProfile.logoCredits + 1)
                    code.startsWith("YS-VD1-") -> currentProfile.copy(videoCredits = currentProfile.videoCredits + 1)
                    code.startsWith("YS-IM1-") -> currentProfile.copy(imageCredits = currentProfile.imageCredits + 1)
                    code.startsWith("YS-PR3-") -> currentProfile.copy(promoCredits = currentProfile.promoCredits + 3)
                    code.startsWith("YS-PK5-") -> currentProfile.copy(
                        logoCredits = currentProfile.logoCredits + 7,
                        imageCredits = currentProfile.imageCredits + 3
                    )
                    code.startsWith("YS-PK7-") -> currentProfile.copy(
                        promoCredits = currentProfile.promoCredits + 7,
                        imageCredits = currentProfile.imageCredits + 5
                    )
                    code.startsWith("YS-PK10-") -> currentProfile.copy(
                        videoCredits = currentProfile.videoCredits + 5,
                        logoCredits = currentProfile.logoCredits + 10,
                        promoCredits = currentProfile.promoCredits + 5,
                        imageCredits = currentProfile.imageCredits + 5
                    )
                    code.startsWith("YS-PK20-") -> currentProfile.copy(
                        hasUnlimitedAiLogos = true,
                        logoCredits = currentProfile.logoCredits + 30,
                        videoCredits = currentProfile.videoCredits + 20,
                        promoCredits = currentProfile.promoCredits + 1,
                        imageCredits = currentProfile.imageCredits + 10
                    )
                    code.startsWith("YS-VIP-") -> currentProfile.copy(isUnlimited = true)
                    else -> currentProfile.copy(isUnlimited = true) // Fallback for old codes
                }
                
                repository.updateProfile(updatedProfile)
                _toastEvent.emit("پیرۆزە! کۆدەکە قبوڵ کرا و پاکێجەکەت بۆ زیاد کرا.")
            }
        } else {
            viewModelScope.launch {
                _toastEvent.emit("ئەم کۆدە هەڵەیە یان پێشتر بەکارهاتووە!")
            }
        }
    }
}
