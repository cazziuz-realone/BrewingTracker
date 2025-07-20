package com.brewingtracker.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.brewingtracker.presentation.screens.CreateProjectScreen

@Composable
fun BrewingNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        // Dashboard/Home Screen
        composable(Screen.Dashboard.route) {
            Text("Brewing Tracker Home - Dashboard")
        }

        // Create Project Screen - THE IMPORTANT ONE
        composable(Screen.CreateProject.route) {
            CreateProjectScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProjectCreated = { projectId ->
                    navController.popBackStack()
                }
            )
        }

        // You can add other screens here as needed using your existing Screen objects
        // composable(Screen.Projects.route) { ... }
        // composable(Screen.Calculators.route) { ... }
        // etc.
    }
}