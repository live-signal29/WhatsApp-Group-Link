package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ListingItem
import com.example.data.model.ListingType
import com.example.data.repository.ListingRepository
import com.example.ui.theme.BorderGray
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PromotedYellow
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White

@Composable
fun ListingCard(
    item: ListingItem,
    onJoinOrFollowClick: () -> Unit,
    onReportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isPromoted = item.isPromotionCurrentlyActive()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("listing_item_${item.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            val avatarColors = remember {
                listOf(
                    Color(0xFF00A884), // Emerald
                    Color(0xFF1E88E5), // Blue
                    Color(0xFF8E24AA), // Purple
                    Color(0xFFE53935), // Crimson
                    Color(0xFFFB8C00), // Orange
                    Color(0xFF00ACC1), // Cyan
                    Color(0xFF43A047), // Green
                    Color(0xFFD81B60), // Magenta
                    Color(0xFF3949AB)  // Indigo
                )
            }
            val avatarBg = avatarColors[(item.name.hashCode() and 0x7FFFFFFF) % avatarColors.size]

            // Circular Group/Channel Image
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(avatarBg.copy(alpha = 0.18f))
                    .border(1.dp, avatarBg.copy(alpha = 0.35f), CircleShape)
            ) {
                // Initial letter / icon behind image
                val initial = item.name.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
                    ?: if (item.type == ListingType.CHANNEL) "C" else "G"
                Text(
                    text = initial,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = avatarBg
                )

                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${item.name} image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center Column: Title, Category, Promoted & Views row
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = item.name,
                    color = DarkText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Category line
                val categoryText = if (item.type == ListingType.CHANNEL) {
                    "${item.category} • Channel"
                } else {
                    item.category
                }
                Text(
                    text = categoryText,
                    color = SecondaryGray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Promoted badge & Views row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isPromoted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PromotedYellow)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Promoted",
                                color = DarkText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Views counter with eye icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = SecondaryGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${ListingRepository.formatViews(item.views)} views",
                            color = SecondaryGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Right side Action Button & Menu
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Action Button: "Join" / "Follow" for promoted, "View" for non-promoted
                val buttonLabel = if (isPromoted) {
                    if (item.type == ListingType.CHANNEL) "Follow" else "Join"
                } else {
                    "View"
                }
                val buttonBg = if (isPromoted) ButtonGreen else Color(0xFFEDF7F2)
                val buttonTextColor = if (isPromoted) White else DarkGreen

                Button(
                    onClick = onJoinOrFollowClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBg,
                        contentColor = buttonTextColor
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 18.dp,
                        vertical = 8.dp
                    ),
                    modifier = Modifier.testTag("action_button_${item.id}")
                ) {
                    Text(
                        text = buttonLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Small overflow menu for Report
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Report Link") },
                            onClick = {
                                menuExpanded = false
                                onReportClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
