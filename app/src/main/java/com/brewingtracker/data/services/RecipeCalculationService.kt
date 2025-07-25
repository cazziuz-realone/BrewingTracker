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
            var totalVolume = batchSize.ozValue / 128.0 // Convert to gallons
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
                            
                            // Calculate SRM contribution
                            ingredient.srmColor?.let { srm ->
                                val srmContribution = (scaledQuantity * srm) / totalVolume
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
                    IngredientType.HOPS -> {
                        // Calculate IBU from hops
                        ingredient.alphaAcid?.let { alphaAcid ->
                            val ibuContribution = calculateIBUContribution(
                                scaledQuantity,
                                alphaAcid,
                                totalVolume,
                                ingredientWithDetails.recipeIngredient.additionTiming
                            )
                            totalIBU += ibuContribution
                        }
                    }
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
     * Calculate IBU contribution from hops
     */
    private fun calculateIBUContribution(
        hopWeightOz: Double,
        alphaAcid: Double,
        volumeGallons: Double,
        timing: String
    ): Double {
        // Utilization based on boil time (simplified)
        val utilization = when (timing.lowercase()) {
            "boil", "60 min", "bittering" -> 0.30
            "30 min" -> 0.20
            "15 min", "flavor" -> 0.15
            "5 min", "aroma" -> 0.10
            "whirlpool", "flameout" -> 0.05
            "dry hop" -> 0.0
            else -> 0.20 // Default moderate utilization
        }
        
        // IBU = (Weight in oz * Alpha Acid % * Utilization * 7490) / (Volume in gallons * (1 + ((SG - 1.050) / 0.2)))
        // Simplified without specific gravity adjustment
        return (hopWeightOz * alphaAcid * utilization * 7490) / (volumeGallons * 1.0)
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
                estimatedDuration = "15 minutes",
                temperature = "100-110°F"
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
                estimatedDuration = "15 minutes",
                temperature = "65-75°F"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 5,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Allow primary fermentation with daily stirring for first week",
                estimatedDuration = "2-4 weeks",
                temperature = "65-75°F"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 6,
                phase = "secondary",
                title = "Rack to Secondary",
                description = "Rack off sediment to clean carboy for secondary fermentation",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 7,
                phase = "secondary",
                title = "Secondary Fermentation",
                description = "Allow slow fermentation to complete and flavors to develop",
                estimatedDuration = "4-8 weeks",
                temperature = "60-70°F"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 8,
                phase = "clarification",
                title = "Clarification",
                description = "Rack again and add clarifiers if needed. Allow to clear.",
                estimatedDuration = "2-4 weeks"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 9,
                phase = "bottling",
                title = "Bottle",
                description = "Rack to bottling bucket, add priming sugar if carbonating, and bottle",
                estimatedDuration = "2 hours"
            ),
            RecipeStep(
                id = 0,
                recipeId = "",
                stepNumber = 10,
                phase = "aging",
                title = "Age",
                description = "Allow mead to age and mature. Mead improves significantly with time.",
                estimatedDuration = "3-12 months"
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
                estimatedDuration = "5-7 days", temperature = "70-80°F"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 4, phase = "secondary",
                title = "Press and Secondary", description = "Press fruit and transfer to secondary fermentation",
                estimatedDuration = "4-6 weeks", temperature = "65-75°F"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 5, phase = "clarification",
                title = "Rack and Clear", description = "Rack off sediment and allow wine to clear",
                estimatedDuration = "2-3 months"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 6, phase = "bottling",
                title = "Bottle Wine", description = "Final racking and bottling with sulfites",
                estimatedDuration = "2 hours"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 7, phase = "aging",
                title = "Age Wine", description = "Age wine in bottles for optimal flavor development",
                estimatedDuration = "6-24 months"
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
                estimatedDuration = "2-3 weeks", temperature = "60-75°F"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 3, phase = "secondary",
                title = "Secondary Fermentation", description = "Rack to secondary for clarification",
                estimatedDuration = "4-6 weeks"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 4, phase = "bottling",
                title = "Package Cider", description = "Bottle or keg finished cider",
                estimatedDuration = "1 hour"
            )
        )
    }
    
    private fun generateBeerSteps(): List<RecipeStep> {
        return listOf(
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 1, phase = "preparation",
                title = "Mash Grains", description = "Mash grains at appropriate temperature",
                estimatedDuration = "1 hour", temperature = "148-158°F"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 2, phase = "preparation",
                title = "Sparge and Boil", description = "Sparge, collect wort, and boil with hop additions",
                estimatedDuration = "90 minutes"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 3, phase = "primary",
                title = "Primary Fermentation", description = "Ferment with ale or lager yeast",
                estimatedDuration = "1-2 weeks", temperature = "60-70°F"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 4, phase = "bottling",
                title = "Package Beer", description = "Bottle with priming sugar or keg",
                estimatedDuration = "2 hours"
            ),
            RecipeStep(
                id = 0, recipeId = "", stepNumber = 5, phase = "carbonation",
                title = "Condition", description = "Allow beer to carbonate and condition",
                estimatedDuration = "2-4 weeks"
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
