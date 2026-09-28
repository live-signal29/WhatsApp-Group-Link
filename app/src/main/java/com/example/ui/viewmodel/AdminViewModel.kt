package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.GroupLinksApplication
import com.example.data.model.AppSettings
import com.example.data.model.CategoryItem
import com.example.data.model.ListingItem
import com.example.data.model.ListingStatus
import com.example.data.model.PromotionRecord
import com.example.data.model.PurchaseRecord
import com.example.data.model.ReportRecord
import com.example.data.repository.AuthRepository
import com.example.data.repository.ListingRepository
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

enum class AdminTab {
    DASHBOARD,
    PENDING_SUBMISSIONS,
    ALL_LISTINGS,
    CATEGORIES,
    PURCHASES,
    REPORTS,
    SETTINGS
}

class AdminViewModel : ViewModel() {

    private val repository: ListingRepository = GroupLinksApplication.instance.listingRepository
    val authRepository: AuthRepository = GroupLinksApplication.instance.authRepository

    private val _selectedTab = MutableStateFlow(AdminTab.DASHBOARD)
    val selectedTab: StateFlow<AdminTab> = _selectedTab.asStateFlow()

    private val _searchFilter = MutableStateFlow("")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val allListings: StateFlow<List<ListingItem>> = repository.listings
    val categories: StateFlow<List<CategoryItem>> = repository.categories
    val reports: StateFlow<List<ReportRecord>> = repository.reports
    val purchases: StateFlow<List<PurchaseRecord>> = repository.purchases
    val appSettings: StateFlow<AppSettings> = repository.appSettings

    val pendingListings: StateFlow<List<ListingItem>> = repository.listings.combine(_searchFilter) { list, search ->
        list.filter { it.status == ListingStatus.PENDING }
            .filter { search.isBlank() || it.name.contains(search, ignoreCase = true) || it.category.contains(search, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAllListings: StateFlow<List<ListingItem>> = repository.listings.combine(_searchFilter) { list, search ->
        if (search.isBlank()) list
        else list.filter { it.name.contains(search, ignoreCase = true) || it.category.contains(search, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: AdminTab) {
        _selectedTab.value = tab
    }

    fun setSearchFilter(query: String) {
        _searchFilter.value = query
    }

    fun approveListing(id: String) {
        repository.adminApproveListing(id)
        viewModelScope.launch { _toastEvent.emit("Listing approved and published.") }
    }

    fun rejectListing(id: String) {
        repository.adminRejectListing(id)
        viewModelScope.launch { _toastEvent.emit("Listing rejected.") }
    }

    fun deleteListing(id: String) {
        repository.adminDeleteListing(id)
        viewModelScope.launch { _toastEvent.emit("Listing deleted permanently.") }
    }

    fun togglePromotion(id: String) {
        repository.adminTogglePromote(id)
        viewModelScope.launch { _toastEvent.emit("Promotion status updated.") }
    }

    fun addCategory(name: String) {
        if (name.isNotBlank()) {
            repository.adminAddCategory(name.trim())
            viewModelScope.launch { _toastEvent.emit("Category '$name' added.") }
        }
    }

    fun deleteCategory(id: String) {
        repository.adminDeleteCategory(id)
        viewModelScope.launch { _toastEvent.emit("Category removed.") }
    }

    fun updateSettings(settings: AppSettings) {
        repository.updateAppSettings(settings)
        viewModelScope.launch { _toastEvent.emit("App settings saved successfully.") }
    }
}
