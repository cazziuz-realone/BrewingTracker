package com.brewingtracker.data.models

/**
 * Data class representing live calculations for a recipe
 * Used for real-time display in the recipe builder UI
 */
data class LiveRecipeCalculations(
    val batchSize: BatchSize,
    val estimatedOG: Double? = null,
    val estimatedFG: Double? = null,
    val estimatedABV: Double? = null,
    val estimatedSRM: Double? = null,
    val estimatedCost: Double? = null,
    val totalVolume: Double = batchSize.ozValue / 128.0, // Volume in gallons
    val isCalculating: Boolean = false,
    val errorMessage: String? = null
) {
    // ADDED: Computed property for error checking
    val hasError: Boolean
        get() = errorMessage != null
    
    companion object {
        fun empty(batchSize: BatchSize): LiveRecipeCalculations {
            return LiveRecipeCalculations(
                batchSize = batchSize,
                isCalculating = false
            )
        }
        
        fun calculating(batchSize: BatchSize): LiveRecipeCalculations {
            return LiveRecipeCalculations(
                batchSize = batchSize,
                isCalculating = true
            )
        }
        
        fun error(batchSize: BatchSize, errorMessage: String): LiveRecipeCalculations {
            return LiveRecipeCalculations(
                batchSize = batchSize,
                isCalculating = false,
                errorMessage = errorMessage
            )
        }
    }
}
