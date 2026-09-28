package com.example.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.GroupLinksApplication
import com.example.billing.BillingManager
import com.example.billing.BillingState
import com.example.data.local.HistoryEntity
import com.example.data.model.CategoryItem
import com.example.data.model.ListingItem
import com.example.data.model.ListingType
import com.example.data.model.PromotionRecord
import com.example.data.model.PurchaseRecord
import com.example.data.repository.ListingRepository
import com.example.data.repository.ValidationResult
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

class HomeViewModel : ViewModel() {

    private val repository: ListingRepository = GroupLinksApplication.instance.listingRepository

    private val _selectedTab = MutableStateFlow(ListingType.GROUP)
    val selectedTab: StateFlow<ListingType> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val categories: StateFlow<List<CategoryItem>> = repository.categories

    // Reactive filtered listings combining listings, tab, category, and query
    val listings: StateFlow<List<ListingItem>> = combine(
        repository.listings,
        _selectedTab,
        _selectedCategory,
        _searchQuery
    ) { _, tab, category, query ->
        repository.getFilteredListings(type = tab, category = category, searchQuery = query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(type: ListingType) {
        _selectedTab.value = type
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchOpen.value = !_isSearchOpen.value
        if (!_isSearchOpen.value) {
            _searchQuery.value = ""
        }
    }

    fun onJoinOrFollowClicked(item: ListingItem) {
        repository.openWhatsAppLink(item)
    }

    fun submitReport(listingId: String, reason: String, details: String) {
        viewModelScope.launch {
            repository.submitReport(listingId, reason, details)
            _toastEvent.emit("Report submitted. Our moderation team will review this listing.")
        }
    }
}
