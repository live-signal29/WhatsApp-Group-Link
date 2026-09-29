package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerView
import com.example.data.model.ListingItem
import com.example.data.model.ListingType
import com.example.ui.components.HeaderBar
import com.example.ui.components.ListingCard
import com.example.ui.theme.BorderGray
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightGray
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCategories: () -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val listings by viewModel.listings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var reportDialogListingId by remember { mutableStateOf<String?>(null) }
    var reportReason by remember { mutableStateOf("Spam") }
    var reportDetails by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Top Header
        HeaderBar(
            title = "Group Links",
            showCrown = true,
            showHistory = true,
            showSearch = true,
            showMenu = true,
            isSearchActive = isSearchOpen,
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::setSearchQuery,
            onSearchToggle = viewModel::toggleSearch,
            onCrownClick = onNavigateToPremium,
            onHistoryClick = onNavigateToHistory,
            onMenuClick = onOpenDrawer
        )

        // Subheader: Rounded Tabs [ Groups ] [ Channels ]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(LightGray, RoundedCornerShape(22.dp))
                    .padding(3.dp)
            ) {
                // Groups Tab
                TabButton(
                    label = "Groups",
                    isSelected = selectedTab == ListingType.GROUP,
                    onClick = { viewModel.selectTab(ListingType.GROUP) },
                    modifier = Modifier.weight(1f)
                )

                // Channels Tab
                TabButton(
                    label = "Channels",
                    isSelected = selectedTab == ListingType.CHANNEL,
                    onClick = { viewModel.selectTab(ListingType.CHANNEL) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Category Filter Indicator if any
        if (selectedCategory != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Category: ${selectedCategory}",
                            color = PrimaryGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear filter",
                            tint = PrimaryGreen,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { viewModel.selectCategory(null) }
                        )
                    }
                }
            }
        }

        // Main Feed List with Pull-to-Refresh
        @OptIn(ExperimentalMaterial3Api::class)
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshListings() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(6) {
                        SkeletonItemCard()
                    }
                }
            } else if (listings.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No results",
                            tint = SecondaryGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No ${selectedTab.name.lowercase()}s found",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search keyword" else "Be the first to upload and promote your link!",
                            fontSize = 13.sp,
                            color = SecondaryGray
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onNavigateToUpload,
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Upload Now")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listings, key = { it.id }) { item ->
                        ListingCard(
                            item = item,
                            onJoinOrFollowClick = { viewModel.onJoinOrFollowClicked(item) },
                            onReportClick = { reportDialogListingId = item.id }
                        )
                    }

                    // Non-intrusive AdMob banner placement near list bottom
                    item {
                        AdBannerView(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }

    // Report Dialog
    if (reportDialogListingId != null) {
        val reasons = listOf("Spam", "Adult content", "Scam", "Broken link", "Wrong category", "Other")

        AlertDialog(
            onDismissRequest = { reportDialogListingId = null },
            title = { Text("Report Listing", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Why are you reporting this link?",
                        fontSize = 14.sp,
                        color = SecondaryGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (reportReason == r),
                                onClick = { reportReason = r }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = r, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        placeholder = { Text("Optional additional details...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reportDialogListingId?.let { id ->
                            viewModel.submitReport(id, reportReason, reportDetails)
                        }
                        reportDialogListingId = null
                        reportDetails = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportDialogListingId = null }) {
                    Text("Cancel", color = SecondaryGray)
                }
            }
        )
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) White else Color.Transparent)
            .then(
                if (isSelected) Modifier.border(1.dp, BorderGray, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) DarkText else SecondaryGray
        )
    }
}

@Composable
fun SkeletonItemCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Circular avatar placeholder
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6E8EA))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width(130.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE6E8EA))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .width(75.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE6E8EA))
                )
            }

            // Button placeholder
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .width(68.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0xFFE6E8EA))
            )
        }
    }
}
