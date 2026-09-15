package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.StudioViewModel
import com.example.ui.account.AccountScreen
import com.example.ui.contact.ContactScreen
import com.example.ui.home.HomeScreen
import com.example.ui.premium.PremiumScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val studioViewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    StudioMainApp(viewModel = studioViewModel)
                }
            }
        }
    }
}

@Composable
fun StudioMainApp(viewModel: StudioViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Home (سەرەکی)
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { viewModel.setTab(AppTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "سەرەکی") },
                    label = {
                        Text(
                            text = AppTab.HOME.titleKurdish,
                            fontWeight = if (currentTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = StudioPrimary
                    )
                )

                // Premium (کڕین)
                NavigationBarItem(
                    selected = currentTab == AppTab.PREMIUM,
                    onClick = { viewModel.setTab(AppTab.PREMIUM) },
                    icon = { Icon(Icons.Default.Diamond, contentDescription = "کڕین") },
                    label = {
                        Text(
                            text = AppTab.PREMIUM.titleKurdish,
                            fontWeight = if (currentTab == AppTab.PREMIUM) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = StudioSecondary
                    )
                )

                // Contact (پەیوەندی)
                NavigationBarItem(
                    selected = currentTab == AppTab.CONTACT,
                    onClick = { viewModel.setTab(AppTab.CONTACT) },
                    icon = { Icon(Icons.Default.SupportAgent, contentDescription = "پەیوەندی") },
                    label = {
                        Text(
                            text = AppTab.CONTACT.titleKurdish,
                            fontWeight = if (currentTab == AppTab.CONTACT) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = StudioSecondary
                    )
                )

                // Profile (زانیاری کەسی)
                NavigationBarItem(
                    selected = currentTab == AppTab.PROFILE,
                    onClick = { viewModel.setTab(AppTab.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "زانیاری کەسی") },
                    label = {
                        Text(
                            text = AppTab.PROFILE.titleKurdish,
                            fontWeight = if (currentTab == AppTab.PROFILE) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = StudioPrimary
                    )
                )

                // Account (هەژمار)
                NavigationBarItem(
                    selected = currentTab == AppTab.ACCOUNT,
                    onClick = { viewModel.setTab(AppTab.ACCOUNT) },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "هەژمار") },
                    label = {
                        Text(
                            text = AppTab.ACCOUNT.titleKurdish,
                            fontWeight = if (currentTab == AppTab.ACCOUNT) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = StudioPrimary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> HomeScreen(viewModel = viewModel)
                AppTab.PREMIUM -> PremiumScreen(viewModel = viewModel)
                AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                AppTab.CONTACT -> ContactScreen(viewModel = viewModel)
                AppTab.ACCOUNT -> {
                    if (userProfile?.isLoggedIn == true) {
                        ProfileScreen(viewModel = viewModel)
                    } else {
                        AccountScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

