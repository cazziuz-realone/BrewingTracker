package com.brewingtracker.data.database.entities

/**
 * Enum representing different types of yeast for brewing
 */
enum class YeastType(val displayName: String) {
    /**
     * Ale yeast - top fermenting, works at warmer temperatures
     */
    ALE("Ale"),
    
    /**
     * Lager yeast - bottom fermenting, works at cooler temperatures
     */
    LAGER("Lager"),
    
    /**
     * Wild yeast strains including Brettanomyces
     */
    WILD("Wild"),
    
    /**
     * Champagne yeast - high alcohol tolerance
     */
    CHAMPAGNE("Champagne"),
    
    /**
     * Wine yeast - for fruit wines and meads
     */
    WINE("Wine"),
    
    /**
     * Mead-specific yeast strains
     */
    MEAD("Mead"),
    
    /**
     * Distilling yeast for high alcohol production
     */
    DISTILLING("Distilling"),
    
    /**
     * Specialty yeasts for specific applications
     */
    SPECIALTY("Specialty");

    /**
     * Get a description of the yeast type
     */
    fun getDescription(): String = when (this) {
        ALE -> "Top-fermenting yeast for ales, IPAs, stouts, and porters"
        LAGER -> "Bottom-fermenting yeast for clean, crisp lagers"
        WILD -> "Wild yeast strains for sour beers and farmhouse ales"
        CHAMPAGNE -> "High alcohol tolerance yeast for sparkling wines and strong meads"
        WINE -> "Wine yeast for fruit wines and grape wines"
        MEAD -> "Specialized yeast strains for honey-based meads"
        DISTILLING -> "High-performance yeast for distilling applications"
        SPECIALTY -> "Unique yeast strains for experimental brewing"
    }

    /**
     * Get typical fermentation temperature range
     */
    fun getTemperatureRange(): String = when (this) {
        ALE -> "60-75°F (15-24°C)"
        LAGER -> "45-55°F (7-13°C)"
        WILD -> "65-85°F (18-29°C)"
        CHAMPAGNE -> "55-75°F (13-24°C)"
        WINE -> "60-80°F (15-27°C)"
        MEAD -> "60-75°F (15-24°C)"
        DISTILLING -> "75-85°F (24-29°C)"
        SPECIALTY -> "Varies by strain"
    }
}
