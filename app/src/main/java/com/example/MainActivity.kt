package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppNavigationDrawer
import com.example.ui.components.BottomNavBar
import com.example.ui.components.BottomNavDestination
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.ContactSupportScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyPromotionsScreen
import com.example.ui.screens.MySubmissionsScreen
import com.example.ui.screens.PremiumInfoScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.PurchaseHistoryScreen
import com.example.ui.screens.TermsOfServiceScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.theme.GroupLinksTheme
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.UploadViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GroupLinksTheme {
                GroupLinksApp()
            }
        }
    }
}

@Composable
fun GroupLinksApp() {
    val homeViewModel: HomeViewModel = viewModel()
    val uploadViewModel: UploadViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()

    val authSession by adminViewModel.authRepository.currentUserSession.collectAsState()

    var currentScreen by remember { mutableStateOf("home") }
    var currentBottomTab by remember { mutableStateOf(BottomNavDestination.HOME) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Handle system back button for sub-screens
    BackHandler(enabled = currentScreen != "home") {
        currentScreen = "home"
        currentBottomTab = BottomNavDestination.HOME
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawer(
                userEmail = authSession.email,
                isAdmin = authSession.isAdmin,
                onNavigate = { route ->
                    when (route) {
                        "home" -> {
                            currentScreen = "home"
                            currentBottomTab = BottomNavDestination.HOME
                        }
                        "categories" -> {
                            currentScreen = "categories"
                            currentBottomTab = BottomNavDestination.CATEGORIES
                        }
                        "upload" -> {
                            currentScreen = "upload"
                            currentBottomTab = BottomNavDestination.UPLOAD
                        }
                        else -> {
                            currentScreen = route
                        }
                    }
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        val showBottomBar = currentScreen in listOf("home", "categories", "upload")

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        currentDestination = currentBottomTab,
                        onDestinationSelected = { destination ->
                            currentBottomTab = destination
                            currentScreen = when (destination) {
                                BottomNavDestination.HOME -> "home"
                                BottomNavDestination.CATEGORIES -> "categories"
                                BottomNavDestination.UPLOAD -> "upload"
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "home" -> {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToCategories = {
                                currentBottomTab = BottomNavDestination.CATEGORIES
                                currentScreen = "categories"
                            },
                            onNavigateToUpload = {
                                currentBottomTab = BottomNavDestination.UPLOAD
                                currentScreen = "upload"
                            },
                            onNavigateToHistory = { currentScreen = "history" },
                            onNavigateToPremium = { currentScreen = "premium" },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    "categories" -> {
                        CategoriesScreen(
                            viewModel = homeViewModel,
                            onCategorySelected = { categoryName ->
                                homeViewModel.selectCategory(categoryName)
                                currentBottomTab = BottomNavDestination.HOME
                                currentScreen = "home"
                            },
                            onNavigateToPremium = { currentScreen = "premium" },
                            onNavigateToHistory = { currentScreen = "history" },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    "upload" -> {
                        UploadScreen(
                            viewModel = uploadViewModel,
                            onNavigateToPremium = { currentScreen = "premium" },
                            onNavigateToHistory = { currentScreen = "history" },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onSuccessNavigateHome = {
                                homeViewModel.selectCategory(null)
                                homeViewModel.setSearchQuery("")
                                currentBottomTab = BottomNavDestination.HOME
                                currentScreen = "home"
                            }
                        )
                    }
                    "history" -> {
                        HistoryScreen(
                            viewModel = historyViewModel,
                            onBack = { currentScreen = "home" }
                        )
                    }
                    "premium" -> {
                        PremiumInfoScreen(
                            onBack = { currentScreen = "home" },
                            onNavigateToUpload = {
                                currentBottomTab = BottomNavDestination.UPLOAD
                                currentScreen = "upload"
                            }
                        )
                    }
                    "my_submissions" -> {
                        MySubmissionsScreen(onBack = { currentScreen = "home" })
                    }
                    "my_promotions" -> {
                        MyPromotionsScreen(onBack = { currentScreen = "home" })
                    }
                    "purchase_history" -> {
                        PurchaseHistoryScreen(onBack = { currentScreen = "home" })
                    }
                    "privacy_policy" -> {
                        PrivacyPolicyScreen(onBack = { currentScreen = "home" })
                    }
                    "terms_of_service" -> {
                        TermsOfServiceScreen(onBack = { currentScreen = "home" })
                    }
                    "contact" -> {
                        ContactSupportScreen(onBack = { currentScreen = "home" })
                    }
                    "about" -> {
                        AboutScreen(onBack = { currentScreen = "home" })
                    }
                    "admin" -> {
                        AdminPanelScreen(
                            viewModel = adminViewModel,
                            onBack = { currentScreen = "home" }
                        )
                    }
                }
            }
        }
    }
}
