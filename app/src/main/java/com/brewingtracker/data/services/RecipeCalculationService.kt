package com.brewingtracker.data.services

import com.brewingtracker.data.database.entities.*
import com.brewingtracker.data.models.LiveRecipeCalculations
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class RecipeCalculationService @Inject constructor() {
    
    /**
     * Calculate estimated OG, FG, ABV for a recipe at given batch size
     */
    fun calculateRecipeParameters(
        recipeIngredients: List<RecipeIngredientWithDetails>,
        batchSize: BatchSize
    ): LiveRecipeCalculations {
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
                    val estimatedSugarContent = estimateFruitSugarContent(ingredient.name)
                    val gravityPoints = (scaledQuantity * estimatedSugarContent * 46) / totalVolume
                    totalGravityPoints += gravityPoints
                }
                IngredientType.HONEY -> {
                    // Honey is approximately 80% fermentable sugars
                    val gravityPoints = (scaledQuantity * 0.80 * 46) / totalVolume
                    totalGravityPoints += gravityPoints
                }
                IngredientType.SUGAR -> {
                    // Sugars are 100% fermentable
                    val gravityPoints = (scaledQuantity * 1.0 * 46) / totalVolume
                    totalGravityPoints += gravityPoints
                }
                // Non-fermentable ingredients don't contribute to gravity
                else -> {
                    // SPICE, HERB, HOPS, NUTRIENT, ACID, CLARIFIER, OTHER
                }
            }
        }
        
        val estimatedOG = 1.0 + (totalGravityPoints / 1000.0)
        val estimatedFG = calculateFinalGravity(estimatedOG)
        val estimatedABV = calculateABV(estimatedOG, estimatedFG)
        val estimatedSRM = calculateSRM(recipeIngredients, totalVolume)
        
        return LiveRecipeCalculations(
            estimatedOG = estimatedOG,
            estimatedFG = estimatedFG,
            estimatedABV = estimatedABV,
            estimatedSRM = estimatedSRM,
            estimatedCost = calculateCost(recipeIngredients, batchSize),
            batchSize = batchSize,
            totalGravityPoints = totalGravityPoints,
            totalVolume = totalVolume
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
     * Generate default process steps based on beverage type
     * Note: These are templates - the actual RecipeStep entities need a recipeId
     */
    fun generateDefaultSteps(beverageType: BeverageType): List<RecipeStep> {
        // We'll use a placeholder recipeId that should be replaced when saving
        val placeholderRecipeId = "PLACEHOLDER"
        
        return when (beverageType) {
            BeverageType.MEAD -> generateMeadSteps(placeholderRecipeId)
            BeverageType.BEER -> generateBeerSteps(placeholderRecipeId)
            BeverageType.WINE -> generateWineSteps(placeholderRecipeId)
            BeverageType.CIDER -> generateCiderSteps(placeholderRecipeId)
            BeverageType.KOMBUCHA -> generateKombuchaSteps(placeholderRecipeId)
            else -> generateGenericSteps(placeholderRecipeId)
        }
    }
    
    private fun estimateFruitSugarContent(fruitName: String): Double {
        // Estimate sugar content for common fruits
        return when (fruitName.lowercase()) {
            "apple", "apples" -> 0.15
            "grape", "grapes" -> 0.20
            "cherry", "cherries" -> 0.16
            "peach", "peaches" -> 0.12
            "pear", "pears" -> 0.15
            "strawberry", "strawberries" -> 0.08
            "blackberry", "blackberries" -> 0.10
            "raspberry", "raspberries" -> 0.08
            "blueberry", "blueberries" -> 0.10
            "mango", "mangos" -> 0.18
            "pineapple" -> 0.16
            else -> 0.12 // Default fruit sugar content
        }
    }
    
    private fun calculateFinalGravity(og: Double): Double {
        // Simplified attenuation calculation (75% for typical yeast)
        val attenuation = 0.75
        return og - ((og - 1.0) * attenuation)
    }
    
    private fun calculateABV(og: Double, fg: Double): Double {
        // Standard ABV calculation
        return (og - fg) * 131.25
    }
    
    private fun calculateSRM(ingredients: List<RecipeIngredientWithDetails>, volumeGallons: Double): Double {
        var totalColorUnits = 0.0
        
        ingredients.forEach { ingredientWithDetails ->
            val ingredient = ingredientWithDetails.ingredient
            ingredient.colorSRM?.let { srm ->
                val quantity = ingredientWithDetails.recipeIngredient.baseQuantity
                // Morey equation for SRM calculation
                totalColorUnits += (srm * quantity) / volumeGallons
            }
        }
        
        // Morey equation: SRM = 1.4922 * (MCU ^ 0.6859)
        return 1.4922 * totalColorUnits.pow(0.6859)
    }
    
    private fun calculateCost(ingredients: List<RecipeIngredientWithDetails>, batchSize: BatchSize): Double {
        return ingredients.sumOf { ingredientWithDetails ->
            val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
            val costPerUnit = ingredientWithDetails.ingredient.costPerUnit
            scaledQuantity * costPerUnit
        }
    }
    
    private fun generateMeadSteps(recipeId: String): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 1,
                phase = "preparation",
                title = "Sanitize Equipment",
                description = "Clean and sanitize all equipment that will come in contact with the must",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 2,
                phase = "preparation",
                title = "Prepare Honey Must",
                description = "Mix honey with water to reach target volume. Heat gently if needed to dissolve.",
                estimatedDuration = "45 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 3,
                phase = "primary",
                title = "Pitch Yeast",
                description = "Cool must to 65-75°F and add rehydrated yeast",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 4,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment at 60-70°F until vigorous fermentation subsides",
                estimatedDuration = "2-4 weeks"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 5,
                phase = "secondary",
                title = "Rack to Secondary",
                description = "Transfer mead off sediment to secondary vessel",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 6,
                phase = "aging",
                title = "Age and Clear",
                description = "Allow mead to age and clear naturally",
                estimatedDuration = "2-6 months"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 7,
                phase = "bottling",
                title = "Bottle",
                description = "Rack clear mead to bottles, optionally add priming sugar for carbonation",
                estimatedDuration = "1-2 hours"
            )
        )
    }
    
    private fun generateBeerSteps(recipeId: String): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 1,
                phase = "preparation",
                title = "Mash Grains",
                description = "Steep grains at target temperature for conversion",
                estimatedDuration = "60 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 2,
                phase = "preparation",
                title = "Boil Wort",
                description = "Boil wort and add hops according to schedule",
                estimatedDuration = "60 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 3,
                phase = "primary",
                title = "Cool and Pitch",
                description = "Cool wort rapidly and pitch yeast",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 4,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment at appropriate temperature for yeast strain",
                estimatedDuration = "1-2 weeks"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 5,
                phase = "bottling",
                title = "Package",
                description = "Bottle or keg with appropriate carbonation",
                estimatedDuration = "2 hours"
            )
        )
    }
    
    private fun generateWineSteps(recipeId: String): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 1,
                phase = "preparation",
                title = "Prepare Must",
                description = "Crush fruit and prepare must",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 2,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment on skins if red wine",
                estimatedDuration = "1-2 weeks"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 3,
                phase = "secondary",
                title = "Press and Secondary",
                description = "Press fruit and transfer to secondary",
                estimatedDuration = "2 hours"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 4,
                phase = "aging",
                title = "Age",
                description = "Age wine with periodic racking",
                estimatedDuration = "3-12 months"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 5,
                phase = "bottling",
                title = "Bottle",
                description = "Bottle finished wine",
                estimatedDuration = "2 hours"
            )
        )
    }
    
    private fun generateCiderSteps(recipeId: String): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 1,
                phase = "preparation",
                title = "Prepare Juice",
                description = "Press apples or prepare juice",
                estimatedDuration = "1-2 hours"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 2,
                phase = "primary",
                title = "Pitch Yeast",
                description = "Add yeast to juice",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 3,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment at cool temperature",
                estimatedDuration = "2-4 weeks"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 4,
                phase = "secondary",
                title = "Secondary",
                description = "Rack to secondary for clearing",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 5,
                phase = "bottling",
                title = "Package",
                description = "Bottle with optional carbonation",
                estimatedDuration = "2 hours"
            )
        )
    }
    
    private fun generateKombuchaSteps(recipeId: String): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 1,
                phase = "preparation",
                title = "Brew Tea",
                description = "Brew strong tea and add sugar",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 2,
                phase = "primary",
                title = "Add SCOBY",
                description = "Cool tea and add SCOBY with starter liquid",
                estimatedDuration = "15 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 3,
                phase = "primary",
                title = "First Fermentation",
                description = "Ferment covered at room temperature",
                estimatedDuration = "7-14 days"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 4,
                phase = "secondary",
                title = "Second Fermentation",
                description = "Bottle with flavorings for carbonation",
                estimatedDuration = "2-3 days"
            )
        )
    }
    
    private fun generateGenericSteps(recipeId: String): List<RecipeStep> {
        return listOf(
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 1,
                phase = "preparation",
                title = "Prepare Ingredients",
                description = "Prepare all ingredients according to recipe",
                estimatedDuration = "1 hour"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 2,
                phase = "primary",
                title = "Primary Fermentation",
                description = "Ferment at appropriate temperature",
                estimatedDuration = "1-4 weeks"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 3,
                phase = "secondary",
                title = "Secondary",
                description = "Transfer to secondary if needed",
                estimatedDuration = "30 minutes"
            ),
            RecipeStep(
                recipeId = recipeId,
                stepNumber = 4,
                phase = "bottling",
                title = "Package",
                description = "Package finished beverage",
                estimatedDuration = "2 hours"
            )
        )
    }
}
