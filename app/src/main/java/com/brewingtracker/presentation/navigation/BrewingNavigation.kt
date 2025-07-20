package com.brewingtracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.brewingtracker.presentation.screens.*

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
            DashboardScreen(
                onNavigateToCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onNavigateToProjects = {
                    navController.navigate(Screen.Projects.route)
                }
            )
        }

        // Projects Screen
        composable(Screen.Projects.route) {
            ProjectsScreen(
                onNavigateToCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onNavigateToProjectDetail = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                }
            )
        }

        // Create Project Screen
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

        // Project Detail Screen
        composable(
            route = Screen.ProjectDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("projectId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailScreen(
                projectId = projectId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Ingredients Screen
        composable(Screen.Ingredients.route) {
            IngredientsScreen()
        }

        // Calculators Screen
        composable(Screen.Calculators.route) {
            CalculatorsScreen(
                onNavigateToCalculator = { calculatorType ->
                    navController.navigate(Screen.Calculator.createRoute(calculatorType))
                }
            )
        }

        // Individual Calculator Screens
        composable(
            route = Screen.Calculator.route,
            arguments = listOf(
                androidx.navigation.navArgument("type") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val calculatorType = backStackEntry.arguments?.getString("type") ?: ""
            
            when (calculatorType) {
                "abv" -> ABVCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
                "ibu" -> IBUCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
                "color" -> ColorCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
                "priming" -> PrimingSugarCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
                "brix" -> BrixConverterScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
                else -> {
                    // Fallback to calculators list
                    CalculatorsScreen(
                        onNavigateToCalculator = { type ->
                            navController.navigate(Screen.Calculator.createRoute(type))
                        }
                    )
                }
            }
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}