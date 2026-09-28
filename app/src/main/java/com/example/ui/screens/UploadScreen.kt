package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerView
import com.example.data.model.ListingType
import com.example.ui.components.HeaderBar
import com.example.ui.components.ListingCard
import com.example.ui.theme.BorderGray
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightGray
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PromotedYellow
import com.example.ui.theme.PromotedYellowContainer
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White
import com.example.ui.viewmodel.UploadViewModel

@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onNavigateToPremium: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenDrawer: () -> Unit,
    onSuccessNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val selectedTab by viewModel.selectedTab.collectAsState()
    val link by viewModel.link.collectAsState()
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val livePreview by viewModel.livePreview.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isDetecting by viewModel.isDetecting.collectAsState()
    val isDuplicate by viewModel.isDuplicate.collectAsState()
    val autoDetectSuccessMessage by viewModel.autoDetectSuccessMessage.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    val formattedPrice = viewModel.getFormattedPrice()
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.submissionSuccessEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onSuccessNavigateHome()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Green Header with Crown and History icons
        HeaderBar(
            title = "Upload",
            showCrown = true,
            showHistory = true,
            showSearch = false,
            showMenu = true,
            onCrownClick = onNavigateToPremium,
            onHistoryClick = onNavigateToHistory,
            onMenuClick = onOpenDrawer
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Tabs: [ Group ] [ Channel ] matching reference screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(LightGray, RoundedCornerShape(22.dp))
                    .padding(3.dp)
            ) {
                UploadTabButton(
                    label = "Group",
                    isSelected = selectedTab == ListingType.GROUP,
                    onClick = { viewModel.selectTab(ListingType.GROUP) },
                    modifier = Modifier.weight(1f)
                )

                UploadTabButton(
                    label = "Channel",
                    isSelected = selectedTab == ListingType.CHANNEL,
                    onClick = { viewModel.selectTab(ListingType.CHANNEL) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Link Input Field with Auto-Detection & Trailing Icon
            val linkHint = if (selectedTab == ListingType.GROUP) "Paste Group link..." else "Paste Channel link..."

            OutlinedTextField(
                value = link,
                onValueChange = viewModel::setLink,
                placeholder = { Text(linkHint, fontSize = 15.sp, color = Color.Gray) },
                singleLine = true,
                isError = isDuplicate,
                trailingIcon = {
                    if (isDetecting) {
                        CircularProgressIndicator(
                            color = PrimaryGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (link.isNotEmpty()) {
                        IconButton(onClick = { viewModel.retryAutoDetect() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Re-detect",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDuplicate) ErrorRed else PrimaryGreen,
                    unfocusedBorderColor = if (isDuplicate) ErrorRed else BorderGray,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_whatsapp_link")
            )

            // Auto-detect loading banner
            AnimatedVisibility(visible = isDetecting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto-detecting group name, image and info...",
                        fontSize = 12.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Auto-detect success notice
            AnimatedVisibility(visible = autoDetectSuccessMessage != null && !isDetecting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = autoDetectSuccessMessage ?: "",
                        fontSize = 12.sp,
                        color = DarkGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Duplicate link warning
            AnimatedVisibility(visible = isDuplicate) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorRed.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Duplicate Warning",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "This link has already been submitted and cannot be added again.",
                        fontSize = 12.sp,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select a category", color = Color.Gray) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.Gray,
                            modifier = Modifier.clickable { categoryDropdownExpanded = true }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderGray,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryDropdownExpanded = true }
                )

                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                viewModel.setCategory(cat.name)
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Name field (Auto-filled by link detection, editable by user)
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::setName,
                label = { Text("Group / Channel Name") },
                placeholder = { Text("Auto-detected or enter name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderGray,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_title")
            )

            // Error Banner if non-duplicate error
            if (errorMessage != null && !isDuplicate) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorRed.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRed,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // PROMOTION CARD (Matching Reference Screenshot 2)
            // ==========================================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PromotedYellowContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, PromotedYellow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PromotedYellow)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Crown",
                                tint = DarkText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Get More Members Faster.",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1,000+ users will see your group daily for 3 days in $formattedPrice",
                        fontSize = 13.sp,
                        color = SecondaryGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Live Promote Preview:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Promote Preview Item
                    ListingCard(
                        item = livePreview,
                        onJoinOrFollowClick = { /* Preview only */ }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Promote Now Button (Triggers Google Play Billing)
                    Button(
                        onClick = {
                            if (activity != null) {
                                viewModel.promoteNow(activity)
                            }
                        },
                        enabled = !isSubmitting && !isDuplicate,
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonGreen,
                            contentColor = White,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("button_promote_now")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Promote Now - $formattedPrice",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Policy notice
                    Text(
                        text = "Note: 18+ and spam links violate our terms and will be rejected without refund.",
                        fontSize = 11.sp,
                        color = SecondaryGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Publish Free (Slow) Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { viewModel.publishFree() },
                    enabled = !isSubmitting && !isDuplicate,
                    modifier = Modifier.testTag("button_publish_free")
                ) {
                    Text(
                        text = "Publish Free (Slow)",
                        color = if (isDuplicate) Color.Gray else PrimaryGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ad banner above bottom
            AdBannerView()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UploadTabButton(
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
