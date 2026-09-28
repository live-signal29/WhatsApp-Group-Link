package com.example.data.model

data class CategoryItem(
    val id: String = "",
    val name: String = "",
    val iconName: String = "category",
    val groupCount: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "iconName" to iconName,
            "groupCount" to groupCount
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): CategoryItem {
            return CategoryItem(
                id = id,
                name = map["name"] as? String ?: "",
                iconName = map["iconName"] as? String ?: "category",
                groupCount = (map["groupCount"] as? Number)?.toInt() ?: 0
            )
        }
    }
}

data class PromotionRecord(
    val id: String = "",
    val ownerId: String = "",
    val listingId: String = "",
    val listingName: String = "",
    val purchaseToken: String = "",
    val productId: String = "promotion_3_days",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val status: PromotionStatus = PromotionStatus.ACTIVE,
    val amountFormatted: String = "Rs 239",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "ownerId" to ownerId,
            "listingId" to listingId,
            "listingName" to listingName,
            "purchaseToken" to purchaseToken,
            "productId" to productId,
            "startTime" to startTime,
            "endTime" to endTime,
            "status" to status.name,
            "amountFormatted" to amountFormatted,
            "createdAt" to createdAt
        )
    }
}

data class PurchaseRecord(
    val purchaseId: String = "",
    val orderId: String = "",
    val userId: String = "",
    val listingId: String = "",
    val listingName: String = "",
    val productId: String = "promotion_3_days",
    val priceFormatted: String = "Rs 239",
    val purchaseState: String = "PURCHASED",
    val verificationState: String = "VERIFIED",
    val createdAt: Long = System.currentTimeMillis(),
    val maskedToken: String = ""
)

data class ReportRecord(
    val id: String = "",
    val listingId: String = "",
    val listingName: String = "",
    val userId: String = "",
    val reason: String = "",
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class AppSettings(
    val promotionDurationDays: Int = 3,
    val promotionPriceRs: String = "Rs 239",
    val advertisedReachText: String = "1,000+ users will see your group daily for 3 days",
    val freeSubmissionEnabled: Boolean = true,
    val paidPromotionEnabled: Boolean = true,
    val adsEnabled: Boolean = true,
    val bannerEnabled: Boolean = true,
    val interstitialEnabled: Boolean = true,
    val maintenanceMode: Boolean = false,
    val blockedKeywords: List<String> = listOf("spam", "hack", "scam", "adult", "xxx"),
    val supportEmail: String = "support@grouplinks.app",
    val supportWhatsApp: String = ""
)
