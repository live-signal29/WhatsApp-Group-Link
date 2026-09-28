package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGray
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White

enum class BottomNavDestination {
    HOME,
    CATEGORIES,
    UPLOAD
}

@Composable
fun BottomNavBar(
    currentDestination: BottomNavDestination,
    onDestinationSelected: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = White,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(color = BorderGray, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentDestination == BottomNavDestination.HOME,
                    onClick = { onDestinationSelected(BottomNavDestination.HOME) },
                    testTag = "nav_home"
                )

                BottomNavItem(
                    label = "Categories",
                    icon = Icons.Default.GridView,
                    isSelected = currentDestination == BottomNavDestination.CATEGORIES,
                    onClick = { onDestinationSelected(BottomNavDestination.CATEGORIES) },
                    testTag = "nav_categories"
                )

                BottomNavItem(
                    label = "Upload",
                    icon = Icons.Default.CloudUpload,
                    isSelected = currentDestination == BottomNavDestination.UPLOAD,
                    onClick = { onDestinationSelected(BottomNavDestination.UPLOAD) },
                    testTag = "nav_upload"
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val tint = if (isSelected) PrimaryGreen else SecondaryGray
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            fontWeight = fontWeight,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
