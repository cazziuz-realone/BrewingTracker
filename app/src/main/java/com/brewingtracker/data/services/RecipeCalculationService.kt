package com.brewingtracker.data.services

import com.brewingtracker.data.database.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class RecipeCalculationService @Inject constructor() {
    
    /**
     * Calculate comprehensive recipe parameters for a given batch size
     */
    suspend fun calculateRecipeParameters(
        recipeIngredients: List<RecipeIngredientWithDetails>,
        batchSize: BatchSize
    ): LiveRecipeCalculations = withContext(Dispatchers.Default) {
        
        try {
            var totalGravityPoints = 0.0
            val totalVolume = batchSize.ozValue.toDouble() / 128.0 // Convert to gallons
            var totalCost = 0.0
            var totalSRM = 0.0
            var totalIBU = 0.0
            
            recipeIngredients.forEach { ingredientWithDetails ->
                val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
                val ingredient = ingredientWithDetails.ingredient
                
                // Calculate cost
                totalCost += scaledQuantity * (ingredient.costPerUnit ?: 0.0)
                
                // Calculate gravity contribution based on ingredient type
                when (ingredient.type) {
                    IngredientType.FRUIT -> {
                        // Handle honey and fruit sugars
                        val gravityPoints = calculateFruitGravityContribution(
                            ingredient.name.lowercase(),
                            scaledQuantity,
                            totalVolume
                        )
                        totalGravityPoints += gravityPoints
                    }
                    IngredientType.GRAIN -> {
                        // Use PPG (Points Per Gallon) for grains
                        ingredient.ppgExtract?.let { ppg ->
                            val efficiency = 0.75 // 75% efficiency assumed
                            val gravityPoints = (scaledQuantity * ppg * efficiency) / totalVolume
                            totalGravityPoints += gravityPoints
                            
                            // Calculate SRM contribution (using colorLovibond instead of srmColor)
                            ingredient.colorLovibond?.let { color ->
                                val srmContribution = (scaledQuantity * color) / totalVolume
                                totalSRM += srmContribution
                            }
                        }
                    }
                    IngredientType.ADJUNCT -> {
                        // Handle adjunct sugars
                        ingredient.ppgExtract?.let { ppg ->
                            val gravityPoints = (scaledQuantity * ppg) / totalVolume
                            totalGravityPoints += gravityPoints
                        }
                    }
                    // Removed HOPS case since IngredientType.HOPS doesn't exist
                    else -> {
                        // Non-fermentable ingredients don't contribute to gravity
                    }
                }
            }
            
            val estimatedOG = 1.0 + (totalGravityPoints / 1000.0)
            val estimatedFG = calculateFinalGravity(estimatedOG)
            val estimatedABV = calculateABV(estimatedOG, estimatedFG)
            
            LiveRecipeCalculations(
                estimatedOG = estimatedOG,
                estimatedFG = estimatedFG,
                estimatedABV = estimatedABV,
                estimatedSRM = totalSRM,
                estimatedIBU = totalIBU,
                batchSize = batchSize,
                totalCost = totalCost,
                isCalculating = false,
                hasError = false
            )
            
        } catch (e: Exception) {
            LiveRecipeCalculations(
                batchSize = batchSize,
                isCalculating = false,
                hasError = true,
                errorMessage = "Calculation error: ${e.message}"
            )
        }
    }
    
    /**
     * Calculate gravity contribution from fruits and honey
     */
    private fun calculateFruitGravityContribution(
        ingredientName: String,
        quantity: Double,
        volume: Double
    ): Double {
        val sugarContent = when {
            ingredientName.contains("honey") -> 0.80 // 80% fermentable sugars
            ingredientName.contains("apple") -> 0.15
            ingredientName.contains("grape") -> 0.20
            ingredientName.contains("cherry") -> 0.16
            ingredientName.contains("strawberr") -> 0.08
            ingredientName.contains("blueberr") -> 0.10
            ingredientName.contains("blackberr") -> 0.10
            ingredientName.contains("raspberr") -> 0.12
            ingredientName.contains("elderberr") -> 0.07
            ingredientName.contains("peach") -> 0.09
            ingredientName.contains("pear") -> 0.10
            ingredientName.contains("mango") -> 0.15
            ingredientName.contains("pineapple") -> 0.13
            ingredientName.contains("pomegranate") -> 0.16
            ingredientName.contains("fig") -> 0.20
            else -> 0.12 // Default fruit sugar content
        }
        
        // Calculate gravity points (46 points per pound of sugar per gallon)
        return (quantity * sugarContent * 46) / volume
    }
    
    /**
     * Calculate final gravity based on original gravity and yeast attenuation
     */
    private fun calculateFinalGravity(
        og: Double,
        attenuation: Double = 0.75 // Default 75% attenuation
    ): Double {
        return og - ((og - 1.0) * attenuation)
    }
    
    /**
     * Calculate ABV from original and final gravity
     */
    private fun calculateABV(og: Double, fg: Double): Double {
        return (og - fg) * 131.25 // Standard ABV calculation
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
     * Calculate recipe cost breakdown by ingredient category
     */
    fun calculateCostBreakdown(
        recipeIngredients: List<RecipeIngredientWithDetails>,
        batchSize: BatchSize
    ): Map<IngredientType, Double> {
        val costByType = mutableMapOf<IngredientType, Double>()
        
        recipeIngredients.forEach { ingredientWithDetails ->
            val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
            val cost = scaledQuantity * (ingredientWithDetails.ingredient.costPerUnit ?: 0.0)
            val type = ingredientWithDetails.ingredient.type
            
            costByType[type] = (costByType[type] ?: 0.0) + cost
        }
        
        return costByType
    }
    
    /**
     * Generate default recipe steps based on beverage type
     */
    fun generateDefaultSteps(beverageType: BeverageType): List<RecipeStep> {
        return when (beverageType) {
            BeverageType.MEAD -> generateMeadSteps()
            BeverageType.WINE -> generateWineSteps()
            BeverageType.CIDER -> generateCiderSteps()
            BeverageType.BEER -> generateBeerSteps()
            BeverageType.KOMBUCHA -> generateKombuchaSteps()
            BeverageType.WATER_KEFIR -> generateWaterKefirSteps()
            BeverageType.OTHER -> generateGenericSteps()
        }
    }
    
    private fun generateMeadSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 1,
                phase = "preparation",
                title = "Prepare Equipment",
                description = "Sanitize all equipment including fermenter, airlock, and stirring spoon",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 2,
                phase = "preparation",
                title = "Warm Honey",
                description = "Gently warm honey to make it easier to mix (do not boil)",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 3,
                phase = "preparation",
                title = "Mix Must",
                description = "Mix honey with water to create must. Add any nutrients.",
                estimatedDuration = "20 minutes"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 4,
                phase = "primary",
                title = "Pitch Yeast",
                description = "Rehydrate yeast if needed and pitch into must at proper temperature",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 5,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Allow primary fermentation with daily stirring for first week",
                estimatedDuration = "2-4 weeks"
            )
        )
    }
    
    private fun generateWineSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 1, phase = "preparation",
                title = "Sanitize Equipment", description = "Clean and sanitize all equipment",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 2, phase = "preparation",
                title = "Prepare Fruit", description = "Crush grapes or prepare fruit for fermentation",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 3, phase = "primary",
                title = "Primary Fermentation", description = "Begin primary fermentation with daily punch-downs",
                estimatedDuration = "5-7 days"
            )
        )
    }
    
    private fun generateCiderSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 1, phase = "preparation",
                title = "Press Apples", description = "Press fresh apples or use quality apple juice",
                estimatedDuration = "2 hours"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 2, phase = "primary",
                title = "Primary Fermentation", description = "Ferment with cider yeast",
                estimatedDuration = "2-3 weeks"
            )
        )
    }
    
    private fun generateBeerSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 1, phase = "preparation",
                title = "Mash Grains", description = "Mash grains at appropriate temperature",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 2, phase = "preparation",
                title = "Sparge and Boil", description = "Sparge, collect wort, and boil",
                estimatedDuration = "90 minutes"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 3, phase = "primary",
                title = "Primary Fermentation", description = "Ferment with ale or lager yeast",
                estimatedDuration = "1-2 weeks"
            )
        )
    }
    
    private fun generateKombuchaSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 1, phase = "preparation",
                title = "Prepare Tea", description = "Brew sweet tea for kombucha culture",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 2, phase = "primary",
                title = "First Fermentation", description = "Ferment with SCOBY",
                estimatedDuration = "7-14 days"
            )
        )
    }
    
    private fun generateWaterKefirSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 1, phase = "preparation",
                title = "Prepare Sugar Water", description = "Dissolve sugar in water",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 2, phase = "primary",
                title = "Ferment with Grains", description = "Add water kefir grains and ferment",
                estimatedDuration = "24-48 hours"
            )
        )
    }
    
    private fun generateGenericSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 1, phase = "preparation",
                title = "Prepare Ingredients", description = "Prepare all ingredients for fermentation",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 2, phase = "primary",
                title = "Primary Fermentation", description = "Begin fermentation process",
                estimatedDuration = "1-4 weeks"
            )
        )
    }
    
    /**
     * Calculate fermentation timeline based on recipe
     */
    fun calculateFermentationTimeline(
        beverageType: BeverageType,
        targetABV: Double?
    ): Map<String, Int> {
        val baseTimeline = when (beverageType) {
            BeverageType.MEAD -> mapOf(
                "primary" to 14,
                "secondary" to 42,
                "aging" to 90,
                "total" to 146
            )
            BeverageType.WINE -> mapOf(
                "primary" to 7,
                "secondary" to 35,
                "aging" to 120,
                "total" to 162
            )
            BeverageType.CIDER -> mapOf(
                "primary" to 14,
                "secondary" to 28,
                "aging" to 30,
                "total" to 72
            )
            BeverageType.BEER -> mapOf(
                "primary" to 10,
                "secondary" to 0,
                "aging" to 14,
                "total" to 24
            )
            BeverageType.KOMBUCHA -> mapOf(
                "primary" to 7,
                "secondary" to 3,
                "aging" to 0,
                "total" to 10
            )
            BeverageType.WATER_KEFIR -> mapOf(
                "primary" to 2,
                "secondary" to 1,
                "aging" to 0,
                "total" to 3
            )
            BeverageType.OTHER -> mapOf(
                "primary" to 14,
                "secondary" to 14,
                "aging" to 30,
                "total" to 58
            )
        }
        
        // Adjust timeline based on target ABV
        val abvMultiplier = when {
            targetABV == null -> 1.0
            targetABV > 14.0 -> 1.5 // High alcohol takes longer
            targetABV > 10.0 -> 1.2 // Medium high alcohol
            targetABV < 6.0 -> 0.8  // Low alcohol ferments faster
            else -> 1.0
        }
        
        return baseTimeline.mapValues { (_, days) ->
            (days * abvMultiplier).toInt()
        }
    }
}
