package com.brewingtracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.brewingtracker.presentation.screens.*

@Composable
fun BrewingNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // Dashboard/Home Screen - FIXED: Enhanced error handling
        composable(Screen.Dashboard.route) {
            try {
                DashboardScreen(
                    onNavigateToProjects = {
                        navController.navigate(Screen.Projects.route)
                    },
                    onNavigateToCalculators = {
                        navController.navigate(Screen.Calculators.route)
                    },
                    onNavigateToIngredients = {
                        navController.navigate(Screen.Ingredients.route)
                    },
                    onNavigateToProjectDetail = { projectId ->
                        navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                    }
                )
            } catch (e: Exception) {
                // Fallback: Simple text screen if DashboardScreen fails
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "Welcome to Brewing Tracker\nHome Screen Loading...",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Projects Screen
        composable(Screen.Projects.route) {
            ProjectsScreen(
                onCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                }
            )
        }

        // Create Project Screen
        composable(Screen.CreateProject.route) {
            CreateProjectScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onProjectCreated = {
                    navController.popBackStack()
                }
            )
        }

        // Project Detail Screen
        composable(
            route = Screen.ProjectDetail.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailScreen(
                projectId = projectId,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = {
                    navController.navigate(Screen.EditProject.createRoute(projectId))
                },
                onAddIngredientsClick = {
                    navController.navigate(Screen.AddIngredients.createRoute(projectId))
                },
                onDeleteProject = { deletedProjectId ->
                    navController.popBackStack()
                }
            )
        }

        // Add Ingredients Screen
        composable(
            route = Screen.AddIngredients.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            AddIngredientsScreen(
                projectId = projectId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onIngredientsAdded = {
                    // Optional: Navigate back to project detail or show success message
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
                onCalculatorClick = { calculatorType ->
                    navController.navigate(Screen.Calculator.createRoute(calculatorType))
                }
            )
        }

        // Individual Calculator Screens
        composable(
            route = Screen.Calculator.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val calculatorType = backStackEntry.arguments?.getString("type") ?: ""
            
            when (calculatorType) {
                "abv" -> ABVCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
                "ibu" -> IBUCalculatorScreen(
                    onBackClick = { navController.popBackStack() }
                )
                "color" -> ColorCalculatorScreen(
                    onBackClick = { navController.popBackStack() }
                )
                "priming" -> PrimingSugarCalculatorScreen(
                    onBackClick = { navController.popBackStack() }
                )
                "brix" -> BrixConverterScreen(
                    onBackClick = { navController.popBackStack() }
                )
                "water" -> WaterCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
                else -> {
                    // Fallback to calculators list
                    CalculatorsScreen(
                        onCalculatorClick = { type ->
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