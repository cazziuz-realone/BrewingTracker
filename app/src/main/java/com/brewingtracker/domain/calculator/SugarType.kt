package com.brewingtracker.domain.calculator

/**
 * Enum representing different types of priming sugars used for carbonation
 * Each sugar type has a different factor used in priming calculations
 * 
 * Factor represents grams of CO2 produced per ounce of sugar
 * Values based on professional brewing standards
 */
enum class SugarType(
    val displayName: String,
    val factor: Double
) {
    CORN_SUGAR("Corn Sugar (Dextrose)", 4.0),
    TABLE_SUGAR("Table Sugar (Sucrose)", 3.7),
    DRY_MALT_EXTRACT("Dry Malt Extract", 4.6),
    HONEY("Honey", 3.5);
    
    companion object {
        /**
         * Get all sugar types as a list for UI display
         */
        fun getAllTypes(): List<SugarType> = values().toList()
        
        /**
         * Get sugar type by display name
         */
        fun fromDisplayName(name: String): SugarType? {
            return values().find { it.displayName == name }
        }
        
        /**
         * Get the most common sugar type (corn sugar/dextrose)
         */
        fun getDefault(): SugarType = CORN_SUGAR
    }
}
