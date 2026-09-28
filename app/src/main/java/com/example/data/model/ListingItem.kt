package com.example.data.model

data class ListingItem(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val whatsappLink: String = "",
    val category: String = "",
    val type: ListingType = ListingType.GROUP,
    val status: ListingStatus = ListingStatus.APPROVED,
    val views: Long = 0,
    val isPromoted: Boolean = false,
    val promotionStart: Long = 0L,
    val promotionEnd: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks if promotion is currently valid and not expired.
     */
    fun isPromotionCurrentlyActive(): Boolean {
        if (!isPromoted) return false
        val now = System.currentTimeMillis()
        return promotionEnd == 0L || now <= promotionEnd
    }

    /**
     * Converts to Map for Firestore persistence.
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "ownerId" to ownerId,
            "name" to name,
            "description" to description,
            "imageUrl" to imageUrl,
            "whatsappLink" to whatsappLink,
            "category" to category,
            "type" to type.name,
            "status" to status.name,
            "views" to views,
            "isPromoted" to isPromoted,
            "promotionStart" to promotionStart,
            "promotionEnd" to promotionEnd,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): ListingItem {
            return ListingItem(
                id = id,
                ownerId = map["ownerId"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                whatsappLink = map["whatsappLink"] as? String ?: "",
                category = map["category"] as? String ?: "",
                type = try {
                    ListingType.valueOf(map["type"] as? String ?: ListingType.GROUP.name)
                } catch (_: Exception) {
                    ListingType.GROUP
                },
                status = try {
                    ListingStatus.valueOf(map["status"] as? String ?: ListingStatus.APPROVED.name)
                } catch (_: Exception) {
                    ListingStatus.APPROVED
                },
                views = (map["views"] as? Number)?.toLong() ?: 0L,
                isPromoted = map["isPromoted"] as? Boolean ?: false,
                promotionStart = (map["promotionStart"] as? Number)?.toLong() ?: 0L,
                promotionEnd = (map["promotionEnd"] as? Number)?.toLong() ?: 0L,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
