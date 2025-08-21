package com.qppd.pesapp.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qppd.pesapp.ui.screens.announcements.AnnouncementListScreen

sealed class Screen(val route: String, val icon: ImageVector? = null, val label: String? = null) {
    object Announcements : Screen("announcements", Icons.Default.Announcement, "Announcements")
    object Events : Screen("events", Icons.Default.Event, "Events")
    object EventDetails : Screen("events/{eventId}") {
        fun createRoute(eventId: String) = "events/$eventId"
    }
    object Messages : Screen("messages", Icons.Default.Message, "Messages")
    object Financials : Screen("financials", Icons.Default.AttachMoney, "Financials")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PESAppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Screen.Announcements,
                    Screen.Events,
                    Screen.Messages,
                    Screen.Financials,
                    Screen.Profile
                ).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Announcements.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Announcements.route) {
                AnnouncementListScreen(
                    onAnnouncementClick = { id -> 
                        // Navigate to announcement details
                    },
                    onCreateAnnouncement = {
                        // Navigate to create announcement
                    }
                )
            }
            composable(Screen.Events.route) {
                EventListScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetails.createRoute(eventId))
                    },
                    onCreateEvent = {
                        // TODO: Navigate to create event screen
                    }
                )
            }
            
            composable(
                route = Screen.EventDetails.route,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType }
                )
            ) {
                EventDetailsScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }
            composable(Screen.Messages.route) {
                // TODO: Messages screen
            }
            composable(Screen.Financials.route) {
                // TODO: Financials screen
            }
            composable(Screen.Profile.route) {
                // TODO: Profile screen
            }
        }
    }
}
