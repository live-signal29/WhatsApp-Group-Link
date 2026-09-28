package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerView
import com.example.data.model.CategoryItem
import com.example.ui.components.HeaderBar
import com.example.ui.theme.BorderGray
import com.example.ui.theme.DarkText
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGray
import com.example.ui.theme.White
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun CategoriesScreen(
    viewModel: HomeViewModel,
    onCategorySelected: (String) -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Green Header
        HeaderBar(
            title = "Categories",
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Subtitle: ALL CATEGORIES
            Text(
                text = "ALL CATEGORIES",
                color = SecondaryGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 2-column grid of rounded category cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories, key = { it.id }) { cat ->
                    CategoryCard(
                        category = cat,
                        onClick = { onCategorySelected(cat.name) }
                    )
                }
            }

            // Small bottom banner ad
            AdBannerView(modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("category_card_${category.name}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 12.dp)
        ) {
            Text(
                text = category.name,
                color = DarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun getCategoryIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "news" -> Icons.Default.Newspaper
        "entertainment" -> Icons.Default.Movie
        "funny" -> Icons.Default.SentimentVerySatisfied
        "poetry" -> Icons.Default.EditNote
        "videos" -> Icons.Default.PlayCircle
        "education" -> Icons.Default.School
        "sports" -> Icons.Default.SportsSoccer
        "science" -> Icons.Default.Science
        "friendship" -> Icons.Default.Diversity3
        "food" -> Icons.Default.Restaurant
        "crypto" -> Icons.Default.CurrencyBitcoin
        "business" -> Icons.Default.BusinessCenter
        else -> Icons.Default.Category
    }
}
