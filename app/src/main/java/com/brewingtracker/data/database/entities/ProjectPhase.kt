package com.brewingtracker.data.database.entities

enum class ProjectPhase(val displayName: String) {
    PLANNING("Planning"),
    BREWING("Brewing"),
    PRIMARY_FERMENTATION("Primary Fermentation"),
    SECONDARY_FERMENTATION("Secondary Fermentation"),
    FERMENTATION("Fermentation"),  // Keep for backward compatibility
    CONDITIONING("Conditioning"),
    CARBONATING("Carbonating"),
    COMPLETED("Completed"),
    ARCHIVED("Archived")
}