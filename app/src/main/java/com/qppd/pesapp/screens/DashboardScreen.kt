package com.qppd.pesapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qppd.pesapp.models.User
import com.qppd.pesapp.models.UserRole
import com.qppd.pesapp.navigation.BottomNavItem
import com.qppd.pesapp.auth.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUserRole: UserRole?,
    onLogout: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val navigationItems = when (currentUserRole) {
        UserRole.ADMIN -> listOf(
            BottomNavItem("Home", Icons.Default.Home, "home"),
            BottomNavItem("Announcements", Icons.Default.Campaign, "announcements"),
            BottomNavItem("Financial Reports", Icons.Default.Assessment, "reports"),
            BottomNavItem("Admin Panel", Icons.Default.AdminPanelSettings, "admin"),
            BottomNavItem("Profile", Icons.Default.Person, "profile")
        )
        UserRole.TEACHER -> listOf(
            BottomNavItem("Home", Icons.Default.Home, "home"),
            BottomNavItem("Announcements", Icons.Default.Campaign, "announcements"),
            BottomNavItem("Financial Reports", Icons.Default.Assessment, "reports"),
            BottomNavItem("Profile", Icons.Default.Person, "profile")
        )
        else -> listOf(
            BottomNavItem("Home", Icons.Default.Home, "home"),
            BottomNavItem("Announcements", Icons.Default.Campaign, "announcements"),
            BottomNavItem("Financial Reports", Icons.Default.Assessment, "reports"),
            BottomNavItem("Profile", Icons.Default.Person, "profile")
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = if (selectedIndex == 0) "Home" 
                    else if (selectedIndex == 1) "Announcements"
                    else if (selectedIndex == 2) "Financial Reports"
                    else if (selectedIndex == 3 && currentUserRole == UserRole.ADMIN) "Admin Panel"
                    else "Profile") },
                actions = {
                    if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.TEACHER) {
                        IconButton(onClick = { 
                            when (selectedIndex) {
                                1 -> { /* Handle add announcement */ }
                                2 -> { /* Handle add financial report */ }
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index: Int, item: BottomNavItem ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedIndex) {
                0 -> HomeScreen(currentUserRole = currentUserRole)
                1 -> AnnouncementListScreen(currentUserRole = currentUserRole ?: UserRole.GUEST)
                2 -> FinancialReportListScreen(currentUserRole = currentUserRole ?: UserRole.GUEST)
                3 -> when (currentUserRole) {
                    UserRole.ADMIN -> AdminPanelScreen()
                    else -> ParentProfileScreen(onLogout = onLogout)
                }
                4 -> ParentProfileScreen(onLogout = onLogout)
            }
        }
    }
}
