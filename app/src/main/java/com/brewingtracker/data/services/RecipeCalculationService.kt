package com.brewingtracker.data.services

import com.brewingtracker.data.database.entities.*
import com.brewingtracker.data.models.BatchSize
import com.brewingtracker.data.models.InventoryStatus
import com.brewingtracker.data.models.LiveRecipeCalculations
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
     * Generate default recipe steps based on beverage type
     */
    fun generateDefaultSteps(beverageType: BeverageType): List<RecipeStep> {
        return when (beverageType) {
            BeverageType.MEAD -> generateMeadSteps()
            BeverageType.BEER -> generateBeerSteps()
            BeverageType.WINE -> generateWineSteps()
            BeverageType.CIDER -> generateCiderSteps()
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
    
    // === DEFAULT STEP GENERATORS ===
    
    private fun generateMeadSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = "", // Will be set when recipe is created
                stepNumber = 1,
                phase = "preparation",
                title = "Sanitize Equipment",
                description = "Sanitize all equipment that will come into contact with the mead including carboy, airlock, racking cane, and stirring spoon.",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 2,
                phase = "preparation",
                title = "Heat Water",
                description = "Heat water to 180°F (82°C). Do not boil as this can drive off delicate honey aromatics.",
                estimatedDuration = "20 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 3,
                phase = "primary",
                title = "Mix Honey Must",
                description = "Dissolve honey in the heated water, stirring gently until fully incorporated. Cool to room temperature.",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 4,
                phase = "primary",
                title = "Add Nutrients",
                description = "Add yeast nutrients and acid blend if using. Stir gently to distribute.",
                estimatedDuration = "5 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 5,
                phase = "primary",
                title = "Pitch Yeast",
                description = "Rehydrate yeast according to package instructions, then pitch into the must when temperature is below 80°F.",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 6,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment at 65-75°F for 4-6 weeks. Monitor specific gravity every few days after day 5.",
                estimatedDuration = "4-6 weeks"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 7,
                phase = "secondary",
                title = "Rack to Secondary",
                description = "Rack mead off the lees into a clean carboy, leaving sediment behind.",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 8,
                phase = "aging",
                title = "Bulk Aging",
                description = "Age mead for 3-6 months, racking every 2-3 months to clarify.",
                estimatedDuration = "3-6 months"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 9,
                phase = "bottling",
                title = "Final Racking and Bottling",
                description = "Rack to bottling bucket, add potassium sorbate if desired, then bottle.",
                estimatedDuration = "1 hour"
            )
        )
    }
    
    private fun generateBeerSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = "",
                stepNumber = 1,
                phase = "preparation",
                title = "Mash Grains",
                description = "Heat water to mash temperature (148-158°F) and add grains. Maintain temperature for 60 minutes.",
                estimatedDuration = "90 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 2,
                phase = "preparation",
                title = "Sparge and Collect Wort",
                description = "Rinse grains with hot water to extract remaining sugars. Collect wort in kettle.",
                estimatedDuration = "45 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 3,
                phase = "preparation",
                title = "Boil and Add Hops",
                description = "Bring wort to boil and add hops according to schedule. Boil for 60-90 minutes total.",
                estimatedDuration = "90 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 4,
                phase = "primary",
                title = "Cool and Pitch Yeast",
                description = "Cool wort to pitching temperature (65-75°F) and pitch yeast.",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 5,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment at 65-75°F for 1-2 weeks until fermentation is complete.",
                estimatedDuration = "1-2 weeks"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 6,
                phase = "bottling",
                title = "Package Beer",
                description = "Add priming sugar and bottle, or transfer to keg for carbonation.",
                estimatedDuration = "2 hours"
            )
        )
    }
    
    private fun generateWineSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = "",
                stepNumber = 1,
                phase = "preparation",
                title = "Crush Grapes",
                description = "Crush grapes to release juice while avoiding breaking seeds.",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 2,
                phase = "preparation",
                title = "Press and Test",
                description = "Press grapes to extract juice. Test and adjust sugar, acid, and pH as needed.",
                estimatedDuration = "2 hours"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 3,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Add yeast and ferment at 70-75°F for 1-2 weeks.",
                estimatedDuration = "1-2 weeks"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 4,
                phase = "secondary",
                title = "Secondary Fermentation",
                description = "Rack to secondary fermenter and continue fermentation for 4-6 weeks.",
                estimatedDuration = "4-6 weeks"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 5,
                phase = "aging",
                title = "Bulk Aging",
                description = "Age wine for 3-12 months, racking every 2-3 months.",
                estimatedDuration = "3-12 months"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 6,
                phase = "bottling",
                title = "Bottling",
                description = "Final clarification, add sulfites if desired, and bottle wine.",
                estimatedDuration = "2 hours"
            )
        )
    }
    
    private fun generateCiderSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = "",
                stepNumber = 1,
                phase = "preparation",
                title = "Press Apples",
                description = "Crush and press apples to extract juice. Strain if needed.",
                estimatedDuration = "2 hours"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 2,
                phase = "preparation",
                title = "Test and Adjust",
                description = "Test specific gravity and acid levels. Adjust if necessary.",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 3,
                phase = "primary",
                title = "Add Yeast",
                description = "Add sulfites if desired, wait 24 hours, then pitch yeast.",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 4,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment at 65-75°F for 2-3 weeks until fermentation is complete.",
                estimatedDuration = "2-3 weeks"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 5,
                phase = "secondary",
                title = "Secondary Fermentation",
                description = "Rack to secondary and age for 2-4 weeks to clarify.",
                estimatedDuration = "2-4 weeks"
            ),
            RecipeStep(
                recipeId = "",
                stepNumber = 6,
                phase = "bottling",
                title = "Bottling",
                description = "Add priming sugar if desired for carbonation, then bottle.",
                estimatedDuration = "1 hour"
            )
        )
    }
}
