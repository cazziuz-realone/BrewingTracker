package com.brewingtracker.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.brewingtracker.presentation.screens.*
import com.brewingtracker.presentation.screens.recipe.RecipeBuilderScreen
import com.brewingtracker.presentation.screens.recipe.RecipeLibraryScreen

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
                },
                onNavigateToIngredients = {
                    navController.navigate(Screen.Ingredients.route)
                },
                onNavigateToProjectDetail = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                },
                // NEW: Recipe Builder navigation
                onNavigateToRecipeBuilder = {
                    navController.navigate(Screen.RecipeBuilder.route)
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

        // RECIPE SCREENS - FULLY IMPLEMENTED
        
        // Recipe Library Screen - FIXED: Now properly implemented
        composable(Screen.RecipeLibrary.route) {
            RecipeLibraryScreen(navController = navController)
        }

        // Recipe Builder Screen - New recipe
        composable(Screen.RecipeBuilder.route) {
            RecipeBuilderScreen(
                recipeId = null, // New recipe
                navController = navController
            )
        }

        // Recipe Builder Screen - Edit existing recipe
        composable(
            route = Screen.RecipeBuilderEdit.route,
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            RecipeBuilderScreen(
                recipeId = recipeId,
                navController = navController
            )
        }

        // Recipe Detail Screen - PLACEHOLDER (can be implemented later)
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            // Placeholder for Recipe Detail - can be implemented later
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Recipe Detail\nRecipe ID: $recipeId\n(Coming Soon)",
                    textAlign = TextAlign.Center
                )
            }
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}