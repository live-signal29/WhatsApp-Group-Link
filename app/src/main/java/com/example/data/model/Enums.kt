package com.example.data.model

enum class ListingType {
    GROUP,
    CHANNEL
}

enum class ListingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    DELETED
}

enum class PromotionStatus {
    ACTIVE,
    EXPIRED,
    PENDING_VERIFICATION,
    CANCELLED
}
