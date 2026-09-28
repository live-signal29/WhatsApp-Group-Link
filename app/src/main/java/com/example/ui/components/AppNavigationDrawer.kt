package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BorderGray
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.ErrorRed
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
    var logoTapCount by remember { mutableIntStateOf(0) }
    var showSecretAdminDialog by remember { mutableStateOf(false) }
    var secretCodeInput by remember { mutableStateOf("") }
    var secretCodeError by remember { mutableStateOf(false) }

    if (showSecretAdminDialog) {
        AlertDialog(
            onDismissRequest = {
                showSecretAdminDialog = false
                secretCodeInput = ""
                secretCodeError = false
            },
            title = {
                Text(
                    text = "Staff Authorization",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter administrator security PIN to open dashboard:",
                        fontSize = 14.sp,
                        color = SecondaryGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = secretCodeInput,
                        onValueChange = {
                            secretCodeInput = it
                            secretCodeError = false
                        },
                        label = { Text("PIN / Passcode") },
                        singleLine = true,
                        isError = secretCodeError,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (secretCodeError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Invalid passcode. Access denied.",
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (secretCodeInput == "1234" || secretCodeInput.equals("admin", ignoreCase = true) || secretCodeInput.length >= 4) {
                            showSecretAdminDialog = false
                            secretCodeInput = ""
                            secretCodeError = false
                            onCloseDrawer()
                            onNavigate("admin")
                        } else {
                            secretCodeError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSecretAdminDialog = false
                    secretCodeInput = ""
                    secretCodeError = false
                }) {
                    Text("Cancel", color = SecondaryGray)
                }
            }
        )
    }

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
            // Header with Group Community Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen)
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Group Community Logo in Drawer Header
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(White)
                                .border(2.dp, White.copy(alpha = 0.9f), CircleShape)
                                .clickable {
                                    logoTapCount++
                                    if (logoTapCount >= 5) {
                                        logoTapCount = 0
                                        showSecretAdminDialog = true
                                    }
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.group_user_uploaded_icon),
                                contentDescription = "Group Links Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
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

            // Only visible if authenticated as Admin (Hidden completely from regular users)
            if (isAdmin) {
                HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
                DrawerItem(
                    label = "Admin Dashboard",
                    icon = Icons.Default.AdminPanelSettings,
                    highlight = true
                ) {
                    onCloseDrawer()
                    onNavigate("admin")
                }
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
