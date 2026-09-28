package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGray
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PromotedYellow
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White

@Composable
fun AppNavigationDrawer(
    userEmail: String,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = White,
        drawerContentColor = DarkText,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen)
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(White)
                        ) {
                            Text(
                                text = "GL",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Group Links",
                                color = White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (userEmail.isNotEmpty()) userEmail else "Guest Explorer",
                                color = White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PromotedYellow)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ADMINISTRATOR",
                                color = DarkText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Navigation Items
            DrawerItem("Home", Icons.Default.Home) {
                onCloseDrawer()
                onNavigate("home")
            }
            DrawerItem("Categories", Icons.Default.GridView) {
                onCloseDrawer()
                onNavigate("categories")
            }
            DrawerItem("Upload", Icons.Default.CloudUpload) {
                onCloseDrawer()
                onNavigate("upload")
            }

            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))

            // User Submissions & Promotions
            DrawerItem("My Submissions", Icons.Default.Bookmark) {
                onCloseDrawer()
                onNavigate("my_submissions")
            }
            DrawerItem("My Promotions", Icons.Default.Campaign) {
                onCloseDrawer()
                onNavigate("my_promotions")
            }
            DrawerItem("Purchase History", Icons.Default.Payment) {
                onCloseDrawer()
                onNavigate("purchase_history")
            }

            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))

            // Policy & Support
            DrawerItem("Privacy Policy", Icons.Default.Policy) {
                onCloseDrawer()
                onNavigate("privacy_policy")
            }
            DrawerItem("Terms of Service", Icons.Default.Description) {
                onCloseDrawer()
                onNavigate("terms_of_service")
            }
            DrawerItem("Contact Support", Icons.Default.SupportAgent) {
                onCloseDrawer()
                onNavigate("contact")
            }
            DrawerItem("About", Icons.Default.Info) {
                onCloseDrawer()
                onNavigate("about")
            }

            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))

            // Admin Panel
            DrawerItem(
                label = if (isAdmin) "Admin Dashboard" else "Admin Access",
                icon = Icons.Default.AdminPanelSettings,
                highlight = isAdmin
            ) {
                onCloseDrawer()
                onNavigate("admin")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (highlight) PrimaryGreen else SecondaryGray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = if (highlight) PrimaryGreen else DarkText,
            fontSize = 14.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium
        )
    }
}
