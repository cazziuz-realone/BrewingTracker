package com.brewingtracker.data.services

import com.brewingtracker.data.database.entities.*
import com.brewingtracker.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for calculating recipe parameters and validating recipes
 */
@Singleton
class RecipeCalculationService @Inject constructor() {

    /**
     * Calculate estimated OG, FG, ABV for a recipe at given batch size
     */
    fun calculateRecipeParameters(
        recipeIngredients: List<RecipeIngredientWithDetails>,
        batchSize: BatchSize
    ): RecipeCalculations {
        var totalGravityPoints = 0.0
        val totalVolume = batchSize.toGallons()
        
        recipeIngredients.forEach { ingredientWithDetails ->
            val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
            val ingredient = ingredientWithDetails.ingredient
            
            // Calculate gravity contribution based on ingredient type
            when (ingredient.type) {
                IngredientType.GRAIN -> {
                    // Use PPG (Points Per Gallon) for grains if available
                    ingredient.ppgExtract?.let { ppg ->
                        val efficiency = 0.75 // 75% efficiency assumed
                        val gravityPoints = (scaledQuantity * ppg * efficiency) / totalVolume
                        totalGravityPoints += gravityPoints
                    }
                }
                IngredientType.FRUIT -> {
                    // Estimate sugar content for fruits
                    val estimatedSugarContent = estimateSugarContent(ingredient.name)
                    val gravityPoints = (scaledQuantity * estimatedSugarContent * 46) / totalVolume
                    totalGravityPoints += gravityPoints
                }
                IngredientType.SPICE,
                IngredientType.ADDITIVE,
                IngredientType.CLARIFIER -> {
                    // Non-fermentable ingredients don't contribute to gravity
                }
                else -> {
                    // Handle other ingredient types as needed
                }
            }
        }
        
        val estimatedOG = 1.0 + (totalGravityPoints / 1000.0)
        val estimatedFG = calculateFinalGravity(estimatedOG)
        val estimatedABV = calculateABV(estimatedOG, estimatedFG)
        
        return RecipeCalculations(
            estimatedOG = estimatedOG,
            estimatedFG = estimatedFG,
            estimatedABV = estimatedABV,
            batchSize = batchSize
        )
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
            
            val status = when {
                availableStock >= scaledQuantity -> InventoryStatus.SUFFICIENT
                availableStock > 0 -> InventoryStatus.INSUFFICIENT
                else -> InventoryStatus.UNKNOWN
            }
            
            ingredientWithDetails.ingredient.id to status
        }
    }
    
    /**
     * Scale recipe ingredients to different batch size
     */
    fun scaleRecipe(
        recipeIngredients: List<RecipeIngredient>,
        fromBatchSize: BatchSize,
        toBatchSize: BatchSize
    ): List<RecipeIngredient> {
        val scaleFactor = toBatchSize.scaleFactor / fromBatchSize.scaleFactor
        
        return recipeIngredients.map { ingredient ->
            ingredient.copy(
                baseQuantity = ingredient.baseQuantity * scaleFactor
            )
        }
    }
    
    /**
     * Generate default process steps for a beverage type
     */
    fun generateDefaultSteps(beverageType: BeverageType): List<RecipeStep> {
        return when (beverageType) {
            BeverageType.MEAD -> generateMeadSteps()
            BeverageType.BEER -> generateBeerSteps()
            BeverageType.WINE -> generateWineSteps()
            BeverageType.CIDER -> generateCiderSteps()
            BeverageType.KOMBUCHA -> generateKombuchaSteps()
        }
    }
    
    /**
     * Validate a recipe for completeness and correctness
     */
    fun validateRecipe(
        recipe: Recipe,
        recipeIngredients: List<RecipeIngredientWithDetails>
    ): Map<String, String> {
        val validationResults = mutableMapOf<String, String>()

        // Validate recipe name
        if (recipe.name.isBlank()) {
            validationResults["Recipe Name"] = "Recipe name is required"
        }

        // Validate ingredients
        if (recipeIngredients.isEmpty()) {
            validationResults["Ingredients"] = "At least one ingredient is required"
        }

        // Check for appropriate base ingredients by beverage type
        when (recipe.beverageType) {
            BeverageType.MEAD -> {
                val hasHoney = recipeIngredients.any { 
                    it.ingredient.name.lowercase().contains("honey")
                }
                if (!hasHoney) {
                    validationResults["Mead Base"] = "Mead recipes require honey"
                }
            }
            BeverageType.BEER -> {
                val hasGrain = recipeIngredients.any { 
                    it.ingredient.type == IngredientType.GRAIN
                }
                if (!hasGrain) {
                    validationResults["Beer Base"] = "Beer recipes require grain"
                }
            }
            BeverageType.WINE -> {
                val hasFruit = recipeIngredients.any { 
                    it.ingredient.type == IngredientType.FRUIT
                }
                if (!hasFruit) {
                    validationResults["Wine Base"] = "Wine recipes require fruit"
                }
            }
            else -> { /* Other types are more flexible */ }
        }

        return validationResults
    }
    
    private fun estimateSugarContent(ingredientName: String): Double {
        return when (ingredientName.lowercase()) {
            "honey" -> 0.80 // 80% fermentable sugars
            "apple" -> 0.15
            "grape" -> 0.20
            "cherry" -> 0.16
            "blueberry" -> 0.14
            "strawberry" -> 0.08
            "orange" -> 0.12
            "maple syrup" -> 0.67
            "corn sugar", "dextrose" -> 1.00
            "table sugar", "sucrose" -> 1.00
            "brown sugar" -> 0.97
            else -> 0.12 // Default fruit sugar content
        }
    }
    
    private fun calculateFinalGravity(og: Double): Double {
        // Simplified attenuation calculation (75% for typical yeast)
        val attenuation = 0.75
        return og - ((og - 1.0) * attenuation)
    }
    
    private fun calculateABV(og: Double, fg: Double): Double {
        return (og - fg) * 131.25 // Standard ABV calculation
    }
    
    private fun generateMeadSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                stepNumber = 1,
                phase = "preparation",
                title = "Sanitize Equipment",
                description = "Sanitize all equipment that will come in contact with the must",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                stepNumber = 2,
                phase = "preparation", 
                title = "Heat Water",
                description = "Heat water to dissolve honey. Do not boil honey to preserve flavor",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                stepNumber = 3,
                phase = "primary",
                title = "Mix Must",
                description = "Combine honey, water, and any acids. Cool to room temperature",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                stepNumber = 4,
                phase = "primary",
                title = "Pitch Yeast",
                description = "Add rehydrated yeast to the must and transfer to fermenter",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                stepNumber = 5,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment at 65-75°F. Fermentation should begin within 24-48 hours",
                estimatedDuration = "2-4 weeks"
            ),
            RecipeStep(
                stepNumber = 6,
                phase = "secondary",
                title = "Rack to Secondary",
                description = "Transfer mead off the lees to secondary fermenter",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                stepNumber = 7,
                phase = "aging",
                title = "Aging",
                description = "Age mead for 3-6 months, racking as needed for clarity",
                estimatedDuration = "3-6 months"
            ),
            RecipeStep(
                stepNumber = 8,
                phase = "bottling",
                title = "Bottle",
                description = "Rack to bottling bucket, add priming sugar if desired, and bottle",
                estimatedDuration = "2 hours"
            )
        )
    }
    
    private fun generateBeerSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                stepNumber = 1,
                phase = "preparation",
                title = "Mash",
                description = "Mash grains at appropriate temperature for 60 minutes",
                estimatedDuration = "90 minutes"
            ),
            RecipeStep(
                stepNumber = 2,
                phase = "preparation",
                title = "Lauter",
                description = "Separate wort from grain bed",
                estimatedDuration = "45 minutes"
            ),
            RecipeStep(
                stepNumber = 3,
                phase = "preparation",
                title = "Boil",
                description = "Boil wort for 60 minutes, adding hops as scheduled",
                estimatedDuration = "75 minutes"
            ),
            RecipeStep(
                stepNumber = 4,
                phase = "primary",
                title = "Cool and Pitch",
                description = "Cool wort to fermentation temperature and pitch yeast",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                stepNumber = 5,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment for 1-2 weeks at appropriate temperature",
                estimatedDuration = "1-2 weeks"
            ),
            RecipeStep(
                stepNumber = 6,
                phase = "bottling",
                title = "Package",
                description = "Bottle or keg the finished beer",
                estimatedDuration = "2 hours"
            )
        )
    }
    
    private fun generateWineSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                stepNumber = 1,
                phase = "preparation",
                title = "Crush Fruit",
                description = "Crush or press fruit to extract juice",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                stepNumber = 2,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Begin fermentation on skins (for reds) or clear juice (for whites)",
                estimatedDuration = "5-7 days"
            ),
            RecipeStep(
                stepNumber = 3,
                phase = "secondary",
                title = "Press and Secondary",
                description = "Press and transfer to secondary fermenter",
                estimatedDuration = "4-8 weeks"
            ),
            RecipeStep(
                stepNumber = 4,
                phase = "aging",
                title = "Aging",
                description = "Age wine for 6-12 months with periodic racking",
                estimatedDuration = "6-12 months"
            ),
            RecipeStep(
                stepNumber = 5,
                phase = "bottling",
                title = "Bottle",
                description = "Clarify, stabilize, and bottle wine",
                estimatedDuration = "3 hours"
            )
        )
    }
    
    private fun generateCiderSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                stepNumber = 1,
                phase = "preparation",
                title = "Press Apples",
                description = "Press fresh apples or prepare apple juice",
                estimatedDuration = "2 hours"
            ),
            RecipeStep(
                stepNumber = 2,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment apple juice for 1-2 weeks",
                estimatedDuration = "1-2 weeks"
            ),
            RecipeStep(
                stepNumber = 3,
                phase = "secondary",
                title = "Secondary Fermentation",
                description = "Rack to secondary, add any flavor additions",
                estimatedDuration = "2-4 weeks"
            ),
            RecipeStep(
                stepNumber = 4,
                phase = "bottling",
                title = "Package",
                description = "Bottle or keg the finished cider",
                estimatedDuration = "2 hours"
            )
        )
    }
    
    private fun generateKombuchaSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                stepNumber = 1,
                phase = "preparation",
                title = "Brew Tea",
                description = "Brew strong tea and dissolve sugar",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                stepNumber = 2,
                phase = "primary",
                title = "First Fermentation",
                description = "Add SCOBY and starter tea, ferment for 7-10 days",
                estimatedDuration = "7-10 days"
            ),
            RecipeStep(
                stepNumber = 3,
                phase = "secondary",
                title = "Second Fermentation",
                description = "Bottle with flavorings for carbonation",
                estimatedDuration = "2-4 days"
            ),
            RecipeStep(
                stepNumber = 4,
                phase = "bottling",
                title = "Refrigerate",
                description = "Refrigerate to stop fermentation",
                estimatedDuration = "Immediate"
            )
        )
    }
}

/**
 * Data class for recipe calculation results
 */
data class RecipeCalculations(
    val estimatedOG: Double,
    val estimatedFG: Double,
    val estimatedABV: Double,
    val batchSize: BatchSize
)
