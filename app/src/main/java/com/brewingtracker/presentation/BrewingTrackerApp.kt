package com.brewingtracker.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.brewingtracker.presentation.navigation.BrewingNavigation
import com.brewingtracker.presentation.navigation.Screen
import com.brewingtracker.presentation.navigation.bottomNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewingTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.screen.route
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { 
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        },
                        selected = isSelected,
                        onClick = {
                            // FIXED: Simplified navigation logic to ensure home button works
                            if (!isSelected) {
                                try {
                                    if (item.screen.route == Screen.Dashboard.route) {
                                        // Special handling for Dashboard/Home screen
                                        navController.navigate(Screen.Dashboard.route) {
                                            // Clear the entire back stack and make Dashboard the only destination
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        // Standard navigation for other screens
                                        navController.navigate(item.screen.route) {
                                            // Pop up to Dashboard to avoid deep back stacks
                                            popUpTo(Screen.Dashboard.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Fallback: simple navigation without options
                                    navController.navigate(item.screen.route)
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        BrewingNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}