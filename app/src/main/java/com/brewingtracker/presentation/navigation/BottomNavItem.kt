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
        label = "Home"
    ),
    BottomNavItem(
        screen = Screen.Projects,
        icon = Icons.Default.List,
        label = "Brew"
    ),
    BottomNavItem(
        screen = Screen.RecipeLibrary, // ADDED: Recipe Library to bottom nav
        icon = Icons.Default.MenuBook,
        label = "Recipes"
    ),
    BottomNavItem(
        screen = Screen.Calculators,
        icon = Icons.Default.Calculate,
        label = "Calc"
    ),
    BottomNavItem(
        screen = Screen.Ingredients,
        icon = Icons.Default.Inventory,
        label = "Ingredients"
    )
)