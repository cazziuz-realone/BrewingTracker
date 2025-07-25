package com.brewingtracker.data.models

import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.RecipeIngredient

/**
 * Batch size options for recipe scaling
 */
enum class BatchSize(
    val displayName: String,
    val ozValue: Int,
    val scaleFactor: Double
) {
    QUART("Quart", 32, 0.25),
    HALF_GALLON("Half Gallon", 64, 0.5),
    GALLON("Gallon", 128, 1.0),
    FIVE_GALLON("5 Gallons", 640, 5.0)
}

/**
 * Inventory status for recipe ingredients
 */
enum class InventoryStatus {
    SUFFICIENT,
    INSUFFICIENT,
    UNKNOWN
}

/**
 * Recipe ingredient with its associated ingredient details
 */
data class RecipeIngredientWithDetails(
    val recipeIngredient: RecipeIngredient,
    val ingredient: Ingredient
)

/**
 * Live recipe calculations data class
 */
data class LiveRecipeCalculations(
    val estimatedOG: Double = 1.000,
    val estimatedFG: Double = 1.000,
    val estimatedABV: Double = 0.0,
    val estimatedSRM: Double = 0.0,
    val estimatedCost: Double = 0.0,
    val batchSize: BatchSize = BatchSize.GALLON,
    val isCalculating: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Recipe calculations result
 */
data class RecipeCalculations(
    val estimatedOG: Double,
    val estimatedFG: Double,
    val estimatedABV: Double,
    val batchSize: BatchSize
)

/**
 * Project ingredient with details for UI display
 */
data class ProjectIngredientWithDetails(
    val projectIngredient: com.brewingtracker.data.database.entities.ProjectIngredient,
    val ingredient: Ingredient
)
