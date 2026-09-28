package com.example.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.GroupLinksApplication
import com.example.billing.BillingManager
import com.example.billing.BillingState
import com.example.data.model.CategoryItem
import com.example.data.model.ListingItem
import com.example.data.model.ListingStatus
import com.example.data.model.ListingType
import com.example.data.repository.ListingRepository
import com.example.data.repository.ValidationResult
import com.example.utils.LinkMetadataFetcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UploadViewModel : ViewModel() {

    private val repository: ListingRepository = GroupLinksApplication.instance.listingRepository
    private val billingManager: BillingManager = GroupLinksApplication.instance.billingManager

    val categories: StateFlow<List<CategoryItem>> = repository.categories
    val billingState: StateFlow<BillingState> = billingManager.billingState

    val appSettings = repository.appSettings

    private val _selectedTab = MutableStateFlow(ListingType.GROUP)
    val selectedTab: StateFlow<ListingType> = _selectedTab.asStateFlow()

    private val _link = MutableStateFlow("")
    val link: StateFlow<String> = _link.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _selectedCategory = MutableStateFlow("News")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting.asStateFlow()

    private val _isDuplicate = MutableStateFlow(false)
    val isDuplicate: StateFlow<Boolean> = _isDuplicate.asStateFlow()

    private val _autoDetectSuccessMessage = MutableStateFlow<String?>(null)
    val autoDetectSuccessMessage: StateFlow<String?> = _autoDetectSuccessMessage.asStateFlow()

    private val _submissionSuccessEvent = MutableSharedFlow<String>()
    val submissionSuccessEvent: SharedFlow<String> = _submissionSuccessEvent.asSharedFlow()

    private var detectionJob: Job? = null

    // Live preview object updated reactively
    val livePreview: StateFlow<ListingItem> = combine(
        _name,
        _description,
        _selectedCategory,
        _selectedTab,
        _link
    ) { name, desc, category, tab, link ->
        val img = _imageUrl.value
        ListingItem(
            id = "preview_id",
            name = name.ifBlank { if (tab == ListingType.GROUP) "My WhatsApp Group" else "My WhatsApp Channel" },
            description = desc.ifBlank { "Join our active community!" },
            category = category,
            type = tab,
            whatsappLink = link,
            imageUrl = img.ifBlank {
                if (tab == ListingType.GROUP)
                    "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150"
                else
                    "https://images.unsplash.com/photo-1518770660439-4636190af475?w=150"
            },
            views = 0L,
            isPromoted = true,
            status = ListingStatus.APPROVED
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListingItem())

    init {
        // Observe billing purchases
        viewModelScope.launch {
            billingManager.billingState.collect { state ->
                when (state) {
                    is BillingState.PurchaseSuccess -> {
                        // Activate promotion in repository
                        val price = getFormattedPrice()
                        repository.activatePaidPromotion(
                            listingId = state.listingId,
                            purchaseToken = state.purchaseToken,
                            orderId = state.orderId,
                            priceFormatted = price
                        )
                        _isSubmitting.value = false
                        _submissionSuccessEvent.emit("Promotion Activated! Your ${selectedTab.value.name.lowercase()} is now featured at the top.")
                        resetForm()
                        billingManager.resetState()
                    }
                    is BillingState.PendingPurchase -> {
                        _isSubmitting.value = false
                        _errorMessage.value = state.message
                    }
                    is BillingState.Error -> {
                        _isSubmitting.value = false
                        _errorMessage.value = state.message
                    }
                    else -> {}
                }
            }
        }
    }

    fun selectTab(tab: ListingType) {
        _selectedTab.value = tab
        _errorMessage.value = null
        checkDuplicateAndTriggerDetection(_link.value)
    }

    fun setLink(url: String) {
        _link.value = url
        checkDuplicateAndTriggerDetection(url)
    }

    private fun checkDuplicateAndTriggerDetection(url: String) {
        val trimmed = url.trim()
        _autoDetectSuccessMessage.value = null

        // 1. Strict Duplicate Check
        if (trimmed.length > 15 && repository.isDuplicateLink(trimmed)) {
            _isDuplicate.value = true
            _errorMessage.value = "This link has already been submitted."
            return
        } else {
            _isDuplicate.value = false
            if (_errorMessage.value == "This link has already been submitted.") {
                _errorMessage.value = null
            }
        }

        // 2. Auto-Detect Metadata when URL is a valid WhatsApp group or channel link
        val isGroupLink = trimmed.contains("chat.whatsapp.com/") && trimmed.length >= 26
        val isChannelLink = trimmed.contains("whatsapp.com/channel/") && trimmed.length >= 30

        if (isGroupLink || isChannelLink) {
            detectionJob?.cancel()
            detectionJob = viewModelScope.launch {
                delay(350) // Slight debounce for smooth typing/pasting
                _isDetecting.value = true
                try {
                    val metadata = LinkMetadataFetcher.fetchMetadata(trimmed)
                    if (metadata != null) {
                        if (!metadata.title.isNullOrBlank()) {
                            _name.value = metadata.title
                        }
                        if (!metadata.imageUrl.isNullOrBlank()) {
                            _imageUrl.value = metadata.imageUrl
                        }
                        if (!metadata.description.isNullOrBlank() && _description.value.isBlank()) {
                            _description.value = metadata.description
                        }
                        _autoDetectSuccessMessage.value = "Detected name & image from WhatsApp!"
                    }
                } catch (_: Exception) {
                    // Fail gracefully
                } finally {
                    _isDetecting.value = false
                }
            }
        }
    }

    fun retryAutoDetect() {
        if (_link.value.isNotBlank() && !_isDuplicate.value) {
            checkDuplicateAndTriggerDetection(_link.value)
        }
    }

    fun setName(text: String) {
        _name.value = text
        _errorMessage.value = null
    }

    fun setDescription(text: String) {
        _description.value = text
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setImageUrl(url: String) {
        _imageUrl.value = url
    }

    fun getFormattedPrice(): String {
        return (billingState.value as? BillingState.Ready)?.formattedPrice ?: "Rs 239"
    }

    fun publishFree() {
        val validation = validateInputs()
        if (!validation) return

        _isSubmitting.value = true
        _errorMessage.value = null

        val result = repository.submitFreeListing(
            name = _name.value,
            description = _description.value,
            category = _selectedCategory.value,
            type = _selectedTab.value,
            whatsappLink = _link.value,
            imageUrl = _imageUrl.value
        )

        _isSubmitting.value = false
        result.onSuccess {
            viewModelScope.launch {
                _submissionSuccessEvent.emit("Submitted successfully! Your link will appear once reviewed by admin.")
                resetForm()
            }
        }.onFailure { e ->
            _errorMessage.value = e.message ?: "Failed to submit link"
        }
    }

    fun promoteNow(activity: Activity) {
        val validation = validateInputs()
        if (!validation) return

        _isSubmitting.value = true
        _errorMessage.value = null

        // Submit listing record first (or find existing)
        val submitResult = repository.submitFreeListing(
            name = _name.value,
            description = _description.value,
            category = _selectedCategory.value,
            type = _selectedTab.value,
            whatsappLink = _link.value,
            imageUrl = _imageUrl.value
        )

        submitResult.onSuccess { listing ->
            // Launch Google Play Billing checkout flow
            billingManager.launchPurchaseFlow(
                activity = activity,
                listingId = listing.id,
                onFallbackPurchase = { targetListingId ->
                    // In debug sandbox when Play Console has no real product yet configured
                    viewModelScope.launch {
                        repository.activatePaidPromotion(
                            listingId = targetListingId,
                            purchaseToken = "sandbox_token_${System.currentTimeMillis()}",
                            orderId = "GPA.sandbox-${System.currentTimeMillis()}",
                            priceFormatted = getFormattedPrice()
                        )
                        _isSubmitting.value = false
                        _submissionSuccessEvent.emit("Promotion Activated! Your ${selectedTab.value.name.lowercase()} is now featured.")
                        resetForm()
                    }
                }
            )
        }.onFailure { e ->
            _isSubmitting.value = false
            _errorMessage.value = e.message ?: "Failed to initiate promotion"
        }
    }

    private fun validateInputs(): Boolean {
        if (_isDuplicate.value || repository.isDuplicateLink(_link.value)) {
            _isDuplicate.value = true
            _errorMessage.value = "This link has already been submitted."
            return false
        }

        if (_name.value.trim().isEmpty()) {
            _errorMessage.value = "Please enter a title/name for your ${selectedTab.value.name.lowercase()}."
            return false
        }

        val urlValidation = repository.validateWhatsAppLink(_link.value, _selectedTab.value)
        if (urlValidation is ValidationResult.Error) {
            _errorMessage.value = urlValidation.message
            return false
        }

        if (repository.containsBlockedKeywords(_name.value) || repository.containsBlockedKeywords(_description.value)) {
            _errorMessage.value = "Title or description contains prohibited keywords."
            return false
        }

        return true
    }

    private fun resetForm() {
        _link.value = ""
        _name.value = ""
        _description.value = ""
        _imageUrl.value = ""
        _isDuplicate.value = false
        _autoDetectSuccessMessage.value = null
        _errorMessage.value = null
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
