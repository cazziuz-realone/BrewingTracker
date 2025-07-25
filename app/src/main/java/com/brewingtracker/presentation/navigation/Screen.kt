package com.brewingtracker.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Projects : Screen("projects")
    object CreateProject : Screen("create_project")
    object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }
    object EditProject : Screen("edit_project/{projectId}") {
        fun createRoute(projectId: String) = "edit_project/$projectId"
    }
    object AddIngredients : Screen("add_ingredients/{projectId}") {
        fun createRoute(projectId: String) = "add_ingredients/$projectId"
    }
    object Ingredients : Screen("ingredients")
    object Calculators : Screen("calculators")
    object Calculator : Screen("calculator/{type}") {
        fun createRoute(type: String) = "calculator/$type"
    }
    object Reminders : Screen("reminders")
    object Settings : Screen("settings")
    
    // NEW: Recipe Builder Navigation
    object RecipeBuilder : Screen("recipe_builder")
    object RecipeBuilderEdit : Screen("recipe_builder/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_builder/$recipeId"
    }
    object RecipeLibrary : Screen("recipe_library")
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }
}
