package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.local.HistoryDao
import com.example.data.local.HistoryEntity
import com.example.data.model.AppSettings
import com.example.data.model.CategoryItem
import com.example.data.model.ListingItem
import com.example.data.model.ListingStatus
import com.example.data.model.ListingType
import com.example.data.model.PromotionRecord
import com.example.data.model.PromotionStatus
import com.example.data.model.PurchaseRecord
import com.example.data.model.ReportRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class ListingRepository(
    private val context: Context,
    private val historyDao: HistoryDao,
    private val authRepository: AuthRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var firestore: FirebaseFirestore? = null

    // In-memory reactive state
    private val _listings = MutableStateFlow<List<ListingItem>>(emptyList())
    val listings: StateFlow<List<ListingItem>> = _listings.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _promotions = MutableStateFlow<List<PromotionRecord>>(emptyList())
    val promotions: StateFlow<List<PromotionRecord>> = _promotions.asStateFlow()

    private val _reports = MutableStateFlow<List<ReportRecord>>(emptyList())
    val reports: StateFlow<List<ReportRecord>> = _reports.asStateFlow()

    private val _purchases = MutableStateFlow<List<PurchaseRecord>>(emptyList())
    val purchases: StateFlow<List<PurchaseRecord>> = _purchases.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    // Track listings viewed in this session to prevent spam incrementing
    private val sessionViewedListings = mutableSetOf<String>()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                listenToFirestore()
            } else {
                Log.d("ListingRepo", "Firebase not yet initialized. Loading seeded local data.")
                loadInitialData()
            }
        } catch (e: Exception) {
            Log.w("ListingRepo", "Firestore init note: ${e.message}. Using offline catalog.")
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        val initialCategories = listOf(
            CategoryItem("cat_1", "News", "newspaper", 128),
            CategoryItem("cat_2", "Entertainment", "movie", 254),
            CategoryItem("cat_3", "Funny", "sentiment_very_satisfied", 312),
            CategoryItem("cat_4", "Poetry", "edit_note", 85),
            CategoryItem("cat_5", "Videos", "play_circle", 192),
            CategoryItem("cat_6", "Education", "school", 143),
            CategoryItem("cat_7", "Sports", "sports_soccer", 167),
            CategoryItem("cat_8", "Science", "science", 94),
            CategoryItem("cat_9", "Friendship", "diversity_3", 420),
            CategoryItem("cat_10", "Food", "restaurant", 115),
            CategoryItem("cat_11", "Crypto", "currency_bitcoin", 230),
            CategoryItem("cat_12", "Business", "business_center", 188)
        )
        _categories.value = initialCategories

        val now = System.currentTimeMillis()
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L

        val initialListings = listOf(
            // Promoted Groups
            ListingItem(
                id = "list_1",
                ownerId = "owner_1",
                name = "Crypto Signal & Trading 🚀",
                description = "Daily profitable crypto calls, Bitcoin analysis, and spot gem signals.",
                imageUrl = "https://images.unsplash.com/photo-1622979135225-d2ba269bc1df?w=150",
                whatsappLink = "https://chat.whatsapp.com/sampleCryptoVipGroup",
                category = "Crypto",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 4290,
                isPromoted = true,
                promotionStart = now - 3600000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 7200000L
            ),
            ListingItem(
                id = "list_2",
                ownerId = "owner_2",
                name = "Funny Memes & Viral Reels 🎭",
                description = "Laugh out loud every hour! Top hilarious comedy memes and status clips.",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                whatsappLink = "https://chat.whatsapp.com/sampleMemeLoversClub",
                category = "Funny",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 2090,
                isPromoted = true,
                promotionStart = now - 1800000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 3600000L
            ),
            ListingItem(
                id = "list_3",
                ownerId = "owner_3",
                name = "Global Breaking News 24/7 📰",
                description = "Real-time international headlines, politics, weather, and breaking updates.",
                imageUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=150",
                whatsappLink = "https://chat.whatsapp.com/sampleWorldNewsAlerts",
                category = "News",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 6450,
                isPromoted = false,
                createdAt = now - 86400000L
            ),
            ListingItem(
                id = "list_4",
                ownerId = "owner_4",
                name = "E-Commerce & Startup Founders 💼",
                description = "Networking club for entrepreneurs, dropshippers, and small business owners.",
                imageUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=150",
                whatsappLink = "https://chat.whatsapp.com/sampleBusinessMastery",
                category = "Business",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 1240,
                isPromoted = false,
                createdAt = now - 172800000L
            ),
            // Promoted Channels
            ListingItem(
                id = "list_chan_1",
                ownerId = "owner_5",
                name = "Tech Gadgets & AI Daily ⚡",
                description = "Official channel for latest mobile reviews, artificial intelligence, and software tools.",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=150",
                whatsappLink = "https://whatsapp.com/channel/sampleTechUpdates001",
                category = "Science",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 10450,
                isPromoted = true,
                promotionStart = now - 5000000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 10000000L
            ),
            ListingItem(
                id = "list_chan_2",
                ownerId = "owner_6",
                name = "Movie Trailers & Cinema Hub 🎬",
                description = "Hollywood and Bollywood HD trailers, Netflix releases, and cinema gossip.",
                imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=150",
                whatsappLink = "https://whatsapp.com/channel/sampleCinemaTrailers",
                category = "Entertainment",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 8900,
                isPromoted = true,
                promotionStart = now - 2500000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 4000000L
            ),
            ListingItem(
                id = "list_chan_3",
                ownerId = "owner_7",
                name = "Football & Premier League Live ⚽",
                description = "Match scores, live commentary, transfer rumors, and matchday highlights.",
                imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=150",
                whatsappLink = "https://whatsapp.com/channel/sampleFootballScoreUpdates",
                category = "Sports",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 3120,
                isPromoted = false,
                createdAt = now - 250000000L
            )
        )
        _listings.value = initialListings
    }

    private fun listenToFirestore() {
        val db = firestore ?: return
        db.collection("listings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ListingRepo", "Firestore error: ${error.message}")
                    if (_listings.value.isEmpty()) loadInitialData()
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { ListingItem.fromMap(doc.id, it) }
                    }
                    _listings.value = items
                } else if (_listings.value.isEmpty()) {
                    loadInitialData()
                }
            }

        db.collection("categories").addSnapshotListener { snapshot, error ->
            if (snapshot != null && !snapshot.isEmpty) {
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { CategoryItem.fromMap(doc.id, it) }
                }
                _categories.value = items
            }
        }
    }

    // ==========================================
    // FILTERED & SORTED LISTINGS
    // ==========================================

    fun getFilteredListings(type: ListingType, category: String? = null, searchQuery: String = ""): List<ListingItem> {
        val now = System.currentTimeMillis()
        val allApproved = _listings.value.filter { it.status == ListingStatus.APPROVED }

        return allApproved
            .filter { it.type == type }
            .filter { category == null || it.category.equals(category, ignoreCase = true) }
            .filter {
                searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<ListingItem> { it.isPromoted && (it.promotionEnd == 0L || now <= it.promotionEnd) }
                    .thenByDescending { it.createdAt }
            )
    }

    // ==========================================
    // WHATSAPP LINK VALIDATION
    // ==========================================

    fun validateWhatsAppLink(url: String, type: ListingType): ValidationResult {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Please enter a link.")
        }

        // Security check for malicious schemes
        val lower = trimmed.lowercase(Locale.ROOT)
        if (lower.startsWith("javascript:") || lower.startsWith("file:") || lower.startsWith("content:") || lower.startsWith("data:")) {
            return ValidationResult.Error("Invalid or unsafe URL protocol.")
        }

        if (!lower.startsWith("https://")) {
            return ValidationResult.Error("Link must start with https://")
        }

        return when (type) {
            ListingType.GROUP -> {
                if (lower.startsWith("https://chat.whatsapp.com/") && trimmed.length > 25) {
                    ValidationResult.Success(trimmed)
                } else {
                    ValidationResult.Error("Must be a valid WhatsApp Group link (e.g. https://chat.whatsapp.com/...)")
                }
            }
            ListingType.CHANNEL -> {
                if ((lower.startsWith("https://whatsapp.com/channel/") || lower.startsWith("https://www.whatsapp.com/channel/")) && trimmed.length > 29) {
                    ValidationResult.Success(trimmed)
                } else {
                    ValidationResult.Error("Must be a valid WhatsApp Channel link (e.g. https://whatsapp.com/channel/...)")
                }
            }
        }
    }

    // Check duplicate link
    fun isDuplicateLink(link: String): Boolean {
        val normalized = link.trim().lowercase(Locale.ROOT).trimEnd('/')
        return _listings.value.any {
            it.status != ListingStatus.DELETED &&
                    it.whatsappLink.trim().lowercase(Locale.ROOT).trimEnd('/') == normalized
        }
    }

    // Check blocked keywords
    fun containsBlockedKeywords(text: String): Boolean {
        val blocked = _appSettings.value.blockedKeywords
        val lower = text.lowercase(Locale.ROOT)
        return blocked.any { lower.contains(it.lowercase(Locale.ROOT)) }
    }

    // ==========================================
    // JOIN / FOLLOW & HISTORY & VIEWS
    // ==========================================

    fun openWhatsAppLink(item: ListingItem) {
        val link = item.whatsappLink
        if (link.isBlank()) return

        // 1. Increment view count safely if not viewed in this session
        if (!sessionViewedListings.contains(item.id)) {
            sessionViewedListings.add(item.id)
            incrementViews(item.id)
        }

        // 2. Record to local History (Room DB)
        scope.launch {
            try {
                historyDao.insertOrUpdate(
                    HistoryEntity(
                        listingId = item.id,
                        name = item.name,
                        category = item.category,
                        type = item.type.name,
                        imageUrl = item.imageUrl,
                        whatsappLink = item.whatsappLink,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.w("ListingRepo", "Error recording history", e)
            }
        }

        // 3. Launch WhatsApp or browser
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                intent.setPackage("com.whatsapp.w4b") // WhatsApp Business
                context.startActivity(intent)
            } catch (_: Exception) {
                // Fallback to browser
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            }
        }
    }

    private fun incrementViews(listingId: String) {
        val updated = _listings.value.map {
            if (it.id == listingId) it.copy(views = it.views + 1) else it
        }
        _listings.value = updated

        firestore?.collection("listings")?.document(listingId)?.update("views", com.google.firebase.firestore.FieldValue.increment(1))
    }

    fun getHistoryFlow(): Flow<List<HistoryEntity>> = historyDao.getRecentHistory()

    fun clearHistory() {
        scope.launch { historyDao.clearAll() }
    }

    // ==========================================
    // SUBMISSIONS & PROMOTIONS
    // ==========================================

    fun submitFreeListing(
        name: String,
        description: String,
        category: String,
        type: ListingType,
        whatsappLink: String,
        imageUrl: String
    ): Result<ListingItem> {
        if (isDuplicateLink(whatsappLink)) {
            return Result.failure(Exception("This link has already been submitted."))
        }
        if (containsBlockedKeywords(name) || containsBlockedKeywords(description)) {
            return Result.failure(Exception("Content violates policy terms and contains restricted keywords."))
        }

        val id = "list_" + UUID.randomUUID().toString().take(10)
        val ownerId = authRepository.getCurrentUserId()
        val newListing = ListingItem(
            id = id,
            ownerId = ownerId,
            name = name.trim(),
            description = description.trim(),
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150" },
            whatsappLink = whatsappLink.trim(),
            category = category,
            type = type,
            status = ListingStatus.PENDING,
            views = 0,
            isPromoted = false,
            createdAt = System.currentTimeMillis()
        )

        _listings.value = listOf(newListing) + _listings.value

        firestore?.collection("listings")?.document(id)?.set(newListing.toMap())
            ?.addOnFailureListener { Log.w("ListingRepo", "Failed to sync to Firestore: ${it.message}") }

        return Result.success(newListing)
    }

    fun activatePaidPromotion(
        listingId: String,
        purchaseToken: String,
        orderId: String,
        priceFormatted: String = "Rs 239"
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val durationDays = _appSettings.value.promotionDurationDays
        val durationMs = durationDays * 24 * 60 * 60 * 1000L
        val endTime = now + durationMs
        val ownerId = authRepository.getCurrentUserId()

        // 1. Update listing
        val existing = _listings.value.find { it.id == listingId }
        val listingName = existing?.name ?: "Promotion Campaign"

        val updatedList = _listings.value.map {
            if (it.id == listingId) {
                it.copy(
                    isPromoted = true,
                    promotionStart = now,
                    promotionEnd = endTime,
                    status = ListingStatus.APPROVED // Promoted listing automatically approved
                )
            } else it
        }
        _listings.value = updatedList

        // 2. Record promotion
        val promoRecord = PromotionRecord(
            id = "promo_" + UUID.randomUUID().toString().take(8),
            ownerId = ownerId,
            listingId = listingId,
            listingName = listingName,
            purchaseToken = purchaseToken,
            startTime = now,
            endTime = endTime,
            status = PromotionStatus.ACTIVE,
            amountFormatted = priceFormatted,
            createdAt = now
        )
        _promotions.value = listOf(promoRecord) + _promotions.value

        // 3. Record purchase
        val purchaseRec = PurchaseRecord(
            purchaseId = "pur_" + UUID.randomUUID().toString().take(8),
            orderId = orderId,
            userId = ownerId,
            listingId = listingId,
            listingName = listingName,
            productId = "promotion_3_days",
            priceFormatted = priceFormatted,
            purchaseState = "PURCHASED",
            verificationState = "VERIFIED",
            createdAt = now,
            maskedToken = if (purchaseToken.length > 8) purchaseToken.take(4) + "..." + purchaseToken.takeLast(4) else "token_valid"
        )
        _purchases.value = listOf(purchaseRec) + _purchases.value

        // Sync to Firestore
        firestore?.collection("listings")?.document(listingId)?.update(
            mapOf(
                "isPromoted" to true,
                "promotionStart" to now,
                "promotionEnd" to endTime,
                "status" to ListingStatus.APPROVED.name
            )
        )
        firestore?.collection("promotions")?.document(promoRecord.id)?.set(promoRecord.toMap())

        return Result.success(Unit)
    }

    // ==========================================
    // REPORTING SYSTEM
    // ==========================================

    fun submitReport(listingId: String, reason: String, details: String = ""): Result<Unit> {
        val item = _listings.value.find { it.id == listingId }
        val report = ReportRecord(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            listingId = listingId,
            listingName = item?.name ?: "Unknown Listing",
            userId = authRepository.getCurrentUserId(),
            reason = reason,
            details = details,
            createdAt = System.currentTimeMillis()
        )
        _reports.value = listOf(report) + _reports.value
        firestore?.collection("reports")?.document(report.id)?.set(
            mapOf(
                "id" to report.id,
                "listingId" to report.listingId,
                "listingName" to report.listingName,
                "userId" to report.userId,
                "reason" to report.reason,
                "details" to report.details,
                "createdAt" to report.createdAt
            )
        )
        return Result.success(Unit)
    }

    // ==========================================
    // ADMIN ACTIONS
    // ==========================================

    fun adminApproveListing(id: String) {
        _listings.value = _listings.value.map {
            if (it.id == id) it.copy(status = ListingStatus.APPROVED) else it
        }
        firestore?.collection("listings")?.document(id)?.update("status", ListingStatus.APPROVED.name)
    }

    fun adminRejectListing(id: String) {
        _listings.value = _listings.value.map {
            if (it.id == id) it.copy(status = ListingStatus.REJECTED, isPromoted = false) else it
        }
        firestore?.collection("listings")?.document(id)?.update(
            mapOf("status" to ListingStatus.REJECTED.name, "isPromoted" to false)
        )
    }

    fun adminDeleteListing(id: String) {
        _listings.value = _listings.value.filter { it.id != id }
        firestore?.collection("listings")?.document(id)?.delete()
    }

    fun adminTogglePromote(id: String) {
        val now = System.currentTimeMillis()
        _listings.value = _listings.value.map {
            if (it.id == id) {
                val newPromoted = !it.isPromoted
                it.copy(
                    isPromoted = newPromoted,
                    promotionStart = if (newPromoted) now else 0L,
                    promotionEnd = if (newPromoted) now + 3 * 86400000L else 0L
                )
            } else it
        }
    }

    fun adminAddCategory(name: String, iconName: String = "category") {
        val cat = CategoryItem("cat_" + UUID.randomUUID().toString().take(6), name.trim(), iconName, 0)
        _categories.value = _categories.value + cat
        firestore?.collection("categories")?.document(cat.id)?.set(cat.toMap())
    }

    fun adminDeleteCategory(id: String) {
        _categories.value = _categories.value.filter { it.id != id }
        firestore?.collection("categories")?.document(id)?.delete()
    }

    fun updateAppSettings(settings: AppSettings) {
        _appSettings.value = settings
    }

    companion object {
        /**
         * Formats view counts intelligently:
         * 999 -> 999
         * 1200 -> 1.2k
         * 2090 -> 2.09k
         * 4290 -> 4.29k
         * 10400 -> 10.4k
         * 100000 -> 100k
         */
        fun formatViews(count: Long): String {
            return when {
                count < 1000 -> count.toString()
                count < 10000 -> {
                    val k = count / 1000.0
                    val formatted = String.format(Locale.US, "%.2f", k)
                    val trimmed = formatted.trimEnd('0').trimEnd('.')
                    "${trimmed}k"
                }
                count < 1000000 -> {
                    val k = count / 1000.0
                    val formatted = String.format(Locale.US, "%.1f", k)
                    val trimmed = formatted.trimEnd('0').trimEnd('.')
                    "${trimmed}k"
                }
                else -> {
                    val m = count / 1000000.0
                    val formatted = String.format(Locale.US, "%.1f", m)
                    val trimmed = formatted.trimEnd('0').trimEnd('.')
                    "${trimmed}M"
                }
            }
        }
    }
}

sealed class ValidationResult {
    data class Success(val normalizedUrl: String) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
