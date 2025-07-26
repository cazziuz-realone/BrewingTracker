package com.brewingtracker.data.models

/**
 * Data class representing live calculations for a recipe
 */
data class LiveRecipeCalculations(
    /**
     * Estimated Original Gravity (OG)
     */
    val estimatedOG: Double = 1.000,
    
    /**
     * Estimated Final Gravity (FG) 
     */
    val estimatedFG: Double = 1.000,
    
    /**
     * Estimated Alcohol By Volume (ABV) percentage
     */
    val estimatedABV: Double = 0.0,
    
    /**
     * Estimated Standard Reference Method (SRM) color
     */
    val estimatedSRM: Double? = null,
    
    /**
     * Estimated cost for the recipe
     */
    val estimatedCost: Double? = null,
    
    /**
     * Batch size these calculations are for
     */
    val batchSize: BatchSize = BatchSize.GALLON,
    
    /**
     * Whether calculations are currently running
     */
    val isCalculating: Boolean = false,
    
    /**
     * Whether there was an error in calculations
     */
    val hasError: Boolean = false,
    
    /**
     * Error message if calculation failed
     */
    val errorMessage: String? = null,
    
    /**
     * Timestamp when calculations were last updated
     */
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Check if the calculations are valid
     */
    fun isValid(): Boolean = !hasError && estimatedOG >= 1.000
    
    /**
     * Get a formatted ABV string
     */
    fun getFormattedABV(): String = "${String.format("%.1f", estimatedABV)}%"
    
    /**
     * Get a formatted OG string
     */
    fun getFormattedOG(): String = String.format("%.3f", estimatedOG)
    
    /**
     * Get a formatted FG string
     */
    fun getFormattedFG(): String = String.format("%.3f", estimatedFG)
    
    /**
     * Calculate apparent attenuation
     */
    fun getApparentAttenuation(): Double {
        return if (estimatedOG > 1.000) {
            ((estimatedOG - estimatedFG) / (estimatedOG - 1.000)) * 100.0
        } else {
            0.0
        }
    }
}
