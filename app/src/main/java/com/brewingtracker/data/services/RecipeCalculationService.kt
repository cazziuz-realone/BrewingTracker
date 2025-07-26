package com.brewingtracker.data.services

import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType
import com.brewingtracker.data.models.BatchSize
import com.brewingtracker.data.models.InventoryStatus
import com.brewingtracker.data.models.LiveRecipeCalculations
import com.brewingtracker.data.models.RecipeIngredientWithDetails
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for calculating recipe parameters and inventory status
 */
@Singleton
class RecipeCalculationService @Inject constructor() {
    
    /**
     * Calculate estimated OG, FG, ABV for a recipe at given batch size
     */
    fun calculateRecipeParameters(
        recipeIngredients: List<RecipeIngredientWithDetails>,
        batchSize: BatchSize
    ): LiveRecipeCalculations {
        if (recipeIngredients.isEmpty()) {
            return LiveRecipeCalculations.empty(batchSize)
        }
        
        try {
            var totalGravityPoints = 0.0
            val totalVolume = batchSize.ozValue / 128.0 // Convert to gallons
            
            recipeIngredients.forEach { ingredientWithDetails ->
                val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
                val ingredient = ingredientWithDetails.ingredient
                
                // Calculate gravity contribution based on ingredient type
                when (ingredient.type) {
                    IngredientType.GRAIN -> {
                        // Use PPG (Points Per Gallon) for grains
                        ingredient.ppgExtract?.let { ppg ->
                            val gravityPoints = (scaledQuantity * ppg * 0.75) / totalVolume // 75% efficiency assumed
                            totalGravityPoints += gravityPoints
                        }
                    }
                    IngredientType.FRUIT -> {
                        // Estimate sugar content for fruits (varies by fruit type)
                        val estimatedSugarContent = when (ingredient.name.lowercase()) {
                            "honey" -> 0.80 // 80% fermentable sugars
                            "apple" -> 0.15
                            "grape" -> 0.20
                            "cherry" -> 0.16
                            else -> 0.12 // Default fruit sugar content
                        }
                        val gravityPoints = (scaledQuantity * estimatedSugarContent * 46) / totalVolume
                        totalGravityPoints += gravityPoints
                    }
                    // Add other ingredient types as needed
                    else -> {
                        // Non-fermentable ingredients don't contribute to gravity
                    }
                }
            }
            
            val estimatedOG = 1.0 + (totalGravityPoints / 1000.0)
            val estimatedFG = calculateFinalGravity(estimatedOG)
            val estimatedABV = calculateABV(estimatedOG, estimatedFG)
            val estimatedCost = calculateCost(recipeIngredients, batchSize)
            
            return LiveRecipeCalculations(
                batchSize = batchSize,
                estimatedOG = estimatedOG,
                estimatedFG = estimatedFG,
                estimatedABV = estimatedABV,
                estimatedCost = estimatedCost,
                totalVolume = totalVolume,
                isCalculating = false
            )
        } catch (e: Exception) {
            return LiveRecipeCalculations.error(batchSize, "Calculation error: ${e.message}")
        }
    }
    
    /**
     * Check inventory status for all ingredients in a recipe
     */
    fun checkInventoryStatus(
        recipeIngredients: List<RecipeIngredientWithDetails>,
        batchSize: BatchSize
    ): Map<Int, InventoryStatus> {
        return recipeIngredients.associate { ingredientWithDetails ->
            val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
            val availableStock = ingredientWithDetails.ingredient.currentStock
            
            val status = InventoryStatus.fromStockComparison(scaledQuantity, availableStock)
            
            ingredientWithDetails.ingredient.id to status
        }
    }
    
    /**
     * Scale recipe ingredients to different batch size
     */
    fun scaleRecipe(
        recipeIngredients: List<RecipeIngredientWithDetails>,
        fromBatchSize: BatchSize,
        toBatchSize: BatchSize
    ): List<RecipeIngredientWithDetails> {
        val scaleFactor = toBatchSize.scaleFactor / fromBatchSize.scaleFactor
        
        return recipeIngredients.map { ingredientWithDetails ->
            val scaledRecipeIngredient = ingredientWithDetails.recipeIngredient.copy(
                baseQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * scaleFactor
            )
            ingredientWithDetails.copy(recipeIngredient = scaledRecipeIngredient)
        }
    }
    
    /**
     * Calculate estimated cost for a recipe
     */
    private fun calculateCost(
        recipeIngredients: List<RecipeIngredientWithDetails>,
        batchSize: BatchSize
    ): Double {
        return recipeIngredients.sumOf { ingredientWithDetails ->
            val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
            val costPerUnit = ingredientWithDetails.ingredient.costPerUnit ?: 0.0
            scaledQuantity * costPerUnit
        }
    }
    
    /**
     * Calculate final gravity based on original gravity
     */
    private fun calculateFinalGravity(og: Double): Double {
        // Simplified attenuation calculation (75% for typical mead yeast)
        val attenuation = 0.75
        return og - ((og - 1.0) * attenuation)
    }
    
    /**
     * Calculate ABV from original and final gravity
     */
    private fun calculateABV(og: Double, fg: Double): Double {
        return (og - fg) * 131.25 // Standard ABV calculation
    }
    
    /**
     * Get required quantity for an ingredient at specific batch size
     */
    fun getRequiredQuantity(
        baseQuantity: Double,
        batchSize: BatchSize
    ): Double {
        return baseQuantity * batchSize.scaleFactor
    }
    
    /**
     * Check if sufficient inventory exists for a single ingredient
     */
    fun checkIngredientStock(
        ingredient: Ingredient,
        requiredQuantity: Double
    ): InventoryStatus {
        return InventoryStatus.fromStockComparison(requiredQuantity, ingredient.currentStock)
    }
}
