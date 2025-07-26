package com.brewingtracker.data.database.entities

/**
 * Enum representing different flocculation characteristics of yeast
 */
enum class FlocculationType(val displayName: String) {
    /**
     * Low flocculation - yeast stays in suspension longer
     */
    LOW("Low"),
    
    /**
     * Medium flocculation - moderate settling behavior
     */
    MEDIUM("Medium"),
    
    /**
     * High flocculation - yeast settles quickly and compactly
     */
    HIGH("High"),
    
    /**
     * Very high flocculation - rapid and very compact settling
     */
    VERY_HIGH("Very High");

    /**
     * Get a description of the flocculation behavior
     */
    fun getDescription(): String = when (this) {
        LOW -> "Stays in suspension longer, may require cold conditioning or filtering"
        MEDIUM -> "Moderate settling, good for most beer styles"
        HIGH -> "Settles quickly, leaves clear beer with compact cake"
        VERY_HIGH -> "Very rapid settling, excellent for clarity"
    }

    /**
     * Get typical applications for this flocculation type
     */
    fun getTypicalUse(): String = when (this) {
        LOW -> "Hefeweizens, some Belgian styles, cask ales"
        MEDIUM -> "Most ales, IPAs, general purpose brewing"
        HIGH -> "English ales, stouts, porters"
        VERY_HIGH -> "Lagers, English bitters, clarification critical styles"
    }
}
