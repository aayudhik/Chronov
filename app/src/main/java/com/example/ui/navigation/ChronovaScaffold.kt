package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun ChronovaScaffold(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "onboarding1"
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    
    val hideBottomBarRoutes = listOf("onboarding1", "onboarding2", "capture", "auth_landing", "auth_email", "auth_phone", "review_drafts")
    val showBottomBar = !hideBottomBarRoutes.contains(currentRoute) && !currentRoute.startsWith("details/")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val tabs = listOf(
                        Triple("home", Icons.Default.Timeline, "Timeline"),
                        Triple("memories", Icons.Default.PhotoLibrary, "Memories"),
                        Triple("search", Icons.Default.AutoAwesome, "AI Search"),
                        Triple("stories", Icons.Default.Book, "Stories"),
                        Triple("insights", Icons.Default.Insights, "Insights"),
                        Triple("intelligence", Icons.Default.Psychology, "Intelligence"),
                        Triple("profile", Icons.Default.Person, "Profile"),
                        Triple("privacy", Icons.Default.Security, "Privacy")
                    )
                    
                    tabs.forEach { (route, icon, label) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
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
        }
    ) { innerPadding ->
        ChronovaNavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
