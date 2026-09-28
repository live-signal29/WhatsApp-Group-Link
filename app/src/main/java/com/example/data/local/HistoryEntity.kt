package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "view_history",
    indices = [Index(value = ["listingId"], unique = true)]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listingId: String,
    val name: String,
    val category: String,
    val type: String,
    val imageUrl: String,
    val whatsappLink: String,
    val timestamp: Long = System.currentTimeMillis()
)
