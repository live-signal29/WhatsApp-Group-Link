package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.GroupLinksApplication
import com.example.data.local.HistoryEntity
import com.example.data.model.ListingItem
import com.example.data.model.ListingType
import com.example.data.repository.ListingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel : ViewModel() {

    private val repository: ListingRepository = GroupLinksApplication.instance.listingRepository

    val historyItems: StateFlow<List<HistoryEntity>> = repository.getHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        repository.clearHistory()
    }

    fun openHistoryItem(item: HistoryEntity) {
        val listing = ListingItem(
            id = item.listingId,
            name = item.name,
            category = item.category,
            type = if (item.type == ListingType.CHANNEL.name) ListingType.CHANNEL else ListingType.GROUP,
            imageUrl = item.imageUrl,
            whatsappLink = item.whatsappLink
        )
        repository.openWhatsAppLink(listing)
    }
}
