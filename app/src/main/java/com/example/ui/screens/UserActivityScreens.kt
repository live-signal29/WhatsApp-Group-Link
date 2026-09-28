package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GroupLinksApplication
import com.example.data.model.ListingStatus
import com.example.ui.theme.BorderGray
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PromotedYellow
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MySubmissionsScreen(onBack: () -> Unit) {
    val repository = GroupLinksApplication.instance.listingRepository
    val authRepo = GroupLinksApplication.instance.authRepository
    val userId = authRepo.getCurrentUserId()
    val allListings by repository.listings.collectAsState()

    val mySubmissions = allListings.filter { it.ownerId == userId || it.id.startsWith("list_") }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        SimpleTopHeader("My Submissions", onBack)

        if (mySubmissions.isEmpty()) {
            EmptyListPlaceholder("No submissions yet", "Submit your group or channel using the Upload tab.", Icons.Default.PostAdd)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mySubmissions, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusBadge(item.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Category: ${item.category} • ${item.type.name}",
                                fontSize = 12.sp,
                                color = SecondaryGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Link: ${item.whatsappLink}",
                                fontSize = 11.sp,
                                color = PrimaryGreen,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyPromotionsScreen(onBack: () -> Unit) {
    val repository = GroupLinksApplication.instance.listingRepository
    val promotions by repository.promotions.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        SimpleTopHeader("My Promotions", onBack)

        if (promotions.isEmpty()) {
            EmptyListPlaceholder("No active promotions", "Promote your group or channel to feature it at the top of the feed.", Icons.Default.Campaign)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(promotions, key = { it.id }) { promo ->
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    val isActive = System.currentTimeMillis() <= promo.endTime

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = promo.listingName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isActive) DarkGreen else SecondaryGray)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isActive) "ACTIVE" else "EXPIRED",
                                        color = White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Amount: ${promo.amountFormatted} • 3 Days Campaign",
                                fontSize = 12.sp,
                                color = SecondaryGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ends: ${dateFormat.format(Date(promo.endTime))}",
                                fontSize = 12.sp,
                                color = if (isActive) DarkGreen else SecondaryGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseHistoryScreen(onBack: () -> Unit) {
    val repository = GroupLinksApplication.instance.listingRepository
    val purchases by repository.purchases.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        SimpleTopHeader("Purchase History", onBack)

        if (purchases.isEmpty()) {
            EmptyListPlaceholder("No purchases found", "Your Google Play in-app purchase receipts will appear here.", Icons.Default.Payment)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(purchases, key = { it.purchaseId }) { pur ->
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "3-Day Promotion",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = pur.priceFormatted,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Target: ${pur.listingName}",
                                fontSize = 13.sp,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Order ID: ${pur.orderId}",
                                fontSize = 11.sp,
                                color = SecondaryGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Date: ${dateFormat.format(Date(pur.createdAt))}",
                                fontSize = 11.sp,
                                color = SecondaryGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = pur.verificationState,
                                        color = DarkGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleTopHeader(title: String, onBack: () -> Unit) {
    Surface(
        color = PrimaryGreen,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }
            Text(
                text = title,
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(status: ListingStatus) {
    val (bgColor, textColor, label) = when (status) {
        ListingStatus.APPROVED -> Triple(DarkGreen.copy(alpha = 0.15f), DarkGreen, "APPROVED")
        ListingStatus.PENDING -> Triple(PendingOrange.copy(alpha = 0.15f), PendingOrange, "PENDING")
        ListingStatus.REJECTED -> Triple(ErrorRed.copy(alpha = 0.15f), ErrorRed, "REJECTED")
        ListingStatus.DELETED -> Triple(Color.LightGray, DarkText, "DELETED")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyListPlaceholder(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryGray,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = SecondaryGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
