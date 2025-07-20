package com.brewingtracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Dashboard,
        icon = Icons.Default.Home,
        label = "Dashboard"
    ),
    BottomNavItem(
        screen = Screen.Projects,
        icon = Icons.Default.List,
        label = "Projects"
    ),
    BottomNavItem(
        screen = Screen.Calculators,
        icon = Icons.Default.Calculate,
        label = "Calculators"
    ),
    BottomNavItem(
        screen = Screen.Ingredients,
        icon = Icons.Default.Inventory,
        label = "Ingredients"
    ),
    BottomNavItem(
        screen = Screen.Settings,
        icon = Icons.Default.Settings,
        label = "Settings"
    )
)