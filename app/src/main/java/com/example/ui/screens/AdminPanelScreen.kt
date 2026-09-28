package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.ListingItem
import com.example.data.model.ListingStatus
import com.example.ui.theme.BorderGray
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightGray
import com.example.ui.theme.LightGreenContainer
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PromotedYellow
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authSession by viewModel.authRepository.currentUserSession.collectAsState()

    var adminEmailInput by remember { mutableStateOf("admin@grouplinks.app") }
    var adminPasswordInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        SimpleTopHeader("Admin Moderation Console", onBack)

        if (!authSession.isAdmin) {
            // Admin Authentication Gate
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin Lock",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Admin Access Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter authorized administrator credentials to manage listings, categories, and settings.",
                            fontSize = 12.sp,
                            color = SecondaryGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = adminEmailInput,
                            onValueChange = { adminEmailInput = it },
                            label = { Text("Admin Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = { adminPasswordInput = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (loginError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = loginError ?: "",
                                color = ErrorRed,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    val result = viewModel.authRepository.signInWithEmail(
                                        adminEmailInput.trim(),
                                        adminPasswordInput.trim()
                                    )
                                    result.onSuccess {
                                        loginError = null
                                    }.onFailure {
                                        loginError = "Authentication failed: invalid credentials"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text("Sign In As Administrator")
                        }
                    }
                }
            }
        } else {
            // Authorized Admin Dashboard
            AuthorizedAdminContent(viewModel = viewModel)
        }
    }
}

@Composable
private fun AuthorizedAdminContent(viewModel: AdminViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val allListings by viewModel.allListings.collectAsState()
    val pendingListings by viewModel.pendingListings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Navigation Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightGray)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminTabChip("Dashboard", selectedTab == AdminTab.DASHBOARD) { viewModel.selectTab(AdminTab.DASHBOARD) }
            AdminTabChip("Pending (${pendingListings.size})", selectedTab == AdminTab.PENDING_SUBMISSIONS) { viewModel.selectTab(AdminTab.PENDING_SUBMISSIONS) }
            AdminTabChip("Listings (${allListings.size})", selectedTab == AdminTab.ALL_LISTINGS) { viewModel.selectTab(AdminTab.ALL_LISTINGS) }
            AdminTabChip("Categories", selectedTab == AdminTab.CATEGORIES) { viewModel.selectTab(AdminTab.CATEGORIES) }
            AdminTabChip("Reports (${reports.size})", selectedTab == AdminTab.REPORTS) { viewModel.selectTab(AdminTab.REPORTS) }
            AdminTabChip("Purchases", selectedTab == AdminTab.PURCHASES) { viewModel.selectTab(AdminTab.PURCHASES) }
            AdminTabChip("Settings", selectedTab == AdminTab.SETTINGS) { viewModel.selectTab(AdminTab.SETTINGS) }
        }

        // Active Tab Screen
        when (selectedTab) {
            AdminTab.DASHBOARD -> {
                AdminDashboardOverview(
                    totalListings = allListings.size,
                    pendingCount = pendingListings.size,
                    activePromotions = allListings.count { it.isPromotionCurrentlyActive() },
                    totalViews = allListings.sumOf { it.views },
                    pendingListings = pendingListings,
                    onApprove = viewModel::approveListing,
                    onReject = viewModel::rejectListing
                )
            }
            AdminTab.PENDING_SUBMISSIONS -> {
                AdminListingsManagement(
                    listings = pendingListings,
                    searchFilter = searchFilter,
                    onSearchChange = viewModel::setSearchFilter,
                    onApprove = viewModel::approveListing,
                    onReject = viewModel::rejectListing,
                    onDelete = viewModel::deleteListing,
                    onTogglePromote = viewModel::togglePromotion
                )
            }
            AdminTab.ALL_LISTINGS -> {
                AdminListingsManagement(
                    listings = allListings,
                    searchFilter = searchFilter,
                    onSearchChange = viewModel::setSearchFilter,
                    onApprove = viewModel::approveListing,
                    onReject = viewModel::rejectListing,
                    onDelete = viewModel::deleteListing,
                    onTogglePromote = viewModel::togglePromotion
                )
            }
            AdminTab.CATEGORIES -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Category")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories, key = { it.id }) { cat ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Text(
                                        text = cat.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ErrorRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AdminTab.REPORTS -> {
                if (reports.isEmpty()) {
                    EmptyListPlaceholder("No reports submitted", "Community reports will be listed here for moderation.", Icons.Default.Check)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(reports, key = { it.id }) { rep ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Listing: ${rep.listingName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Reason: ${rep.reason}", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    if (rep.details.isNotBlank()) {
                                        Text("Details: ${rep.details}", color = SecondaryGray, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { viewModel.deleteListing(rep.listingId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Remove Listing")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AdminTab.PURCHASES -> {
                if (purchases.isEmpty()) {
                    EmptyListPlaceholder("No purchases", "All Google Play Billing receipts appear here.", Icons.Default.Check)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(purchases, key = { it.purchaseId }) { pur ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pur.listingName, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                        Text(pur.priceFormatted, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                    }
                                    Text("Order ID: ${pur.orderId}", fontSize = 12.sp, color = SecondaryGray)
                                    Text("Masked Token: ${pur.maskedToken}", fontSize = 11.sp, color = SecondaryGray)
                                }
                            }
                        }
                    }
                }
            }
            AdminTab.SETTINGS -> {
                AdminSettingsForm(settings = appSettings, onSave = viewModel::updateSettings)
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text("e.g. Technology") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCategory(newCategoryName)
                        newCategoryName = ""
                        showAddCategoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminDashboardOverview(
    totalListings: Int,
    pendingCount: Int,
    activePromotions: Int,
    totalViews: Long,
    pendingListings: List<ListingItem>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Overview Metrics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Total Listings", totalListings.toString(), PrimaryGreen, Modifier.weight(1f))
            MetricCard("Pending Review", pendingCount.toString(), PendingOrange, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Active Promotions", activePromotions.toString(), DarkGreen, Modifier.weight(1f))
            MetricCard("Total Views", totalViews.toString(), DarkText, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Quick Moderation (Pending)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(10.dp))

        if (pendingListings.isEmpty()) {
            Text("No pending submissions right now.", fontSize = 13.sp, color = SecondaryGray)
        } else {
            pendingListings.take(5).forEach { item ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.whatsappLink, fontSize = 11.sp, color = PrimaryGreen, maxLines = 1)
                        }
                        IconButton(onClick = { onApprove(item.id) }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Approve", tint = DarkGreen)
                        }
                        IconButton(onClick = { onReject(item.id) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp, color = SecondaryGray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
private fun AdminListingsManagement(
    listings: List<ListingItem>,
    searchFilter: String,
    onSearchChange: (String) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onDelete: (String) -> Unit,
    onTogglePromote: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchFilter,
            onValueChange = onSearchChange,
            placeholder = { Text("Search listings...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(listings, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            StatusBadge(item.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Category: ${item.category} • ${item.type.name}", fontSize = 12.sp, color = SecondaryGray)
                        Text("Link: ${item.whatsappLink}", fontSize = 11.sp, color = PrimaryGreen, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (item.status != ListingStatus.APPROVED) {
                                Button(
                                    onClick = { onApprove(item.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Approve", fontSize = 12.sp)
                                }
                            }
                            if (item.status != ListingStatus.REJECTED) {
                                Button(
                                    onClick = { onReject(item.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PendingOrange),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Reject", fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = { onTogglePromote(item.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (item.isPromoted) PromotedYellow else Color.LightGray
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(if (item.isPromoted) "Unpromote" else "Promote", fontSize = 12.sp, color = DarkText)
                            }
                            IconButton(onClick = { onDelete(item.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsForm(settings: AppSettings, onSave: (AppSettings) -> Unit) {
    var reachText by remember { mutableStateOf(settings.advertisedReachText) }
    var priceText by remember { mutableStateOf(settings.promotionPriceRs) }
    var supportEmail by remember { mutableStateOf(settings.supportEmail) }
    var freeSubmission by remember { mutableStateOf(settings.freeSubmissionEnabled) }
    var adsEnabled by remember { mutableStateOf(settings.adsEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("App Configuration", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = reachText,
            onValueChange = { reachText = it },
            label = { Text("Advertised Reach Text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it },
            label = { Text("Promotion Price Display (Fallback)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = supportEmail,
            onValueChange = { supportEmail = it },
            label = { Text("Support Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Allow Free Submissions", modifier = Modifier.weight(1f), fontSize = 14.sp)
            Switch(
                checked = freeSubmission,
                onCheckedChange = { freeSubmission = it },
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = LightGreenContainer)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Enable AdMob Ads", modifier = Modifier.weight(1f), fontSize = 14.sp)
            Switch(
                checked = adsEnabled,
                onCheckedChange = { adsEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = LightGreenContainer)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(
                    settings.copy(
                        advertisedReachText = reachText,
                        promotionPriceRs = priceText,
                        supportEmail = supportEmail,
                        freeSubmissionEnabled = freeSubmission,
                        adsEnabled = adsEnabled
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Save Settings")
        }
    }
}

@Composable
private fun AdminTabChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryGreen else White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) White else DarkText,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
