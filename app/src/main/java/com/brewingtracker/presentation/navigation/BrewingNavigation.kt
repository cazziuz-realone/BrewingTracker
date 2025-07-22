package com.brewingtracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        // Dashboard/Home Screen
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToProjects = {
                    navController.navigate(Screen.Projects.route)
                },
                onNavigateToCalculators = {
                    navController.navigate(Screen.Calculators.route)
                }
            )
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
                androidx.navigation.navArgument("projectId") {
                    type = androidx.navigation.NavType.StringType
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
                }
            )
        }

        // Add Ingredients Screen - NEWLY ADDED TO FIX CRASH
        composable(
            route = Screen.AddIngredients.route,
            arguments = listOf(
                androidx.navigation.navArgument("projectId") {
                    type = androidx.navigation.NavType.StringType
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