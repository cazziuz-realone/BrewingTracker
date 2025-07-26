package com.brewingtracker.data.models

import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.RecipeIngredient

/**
 * Recipe ingredient with its associated ingredient details
 */
data class RecipeIngredientWithDetails(
    val recipeIngredient: RecipeIngredient,
    val ingredient: Ingredient
)

/**
 * Project ingredient with details for UI display
 */
data class ProjectIngredientWithDetails(
    val projectIngredient: com.brewingtracker.data.database.entities.ProjectIngredient,
    val ingredient: Ingredient
)
