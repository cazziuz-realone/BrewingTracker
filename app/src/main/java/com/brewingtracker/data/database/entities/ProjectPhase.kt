package com.brewingtracker.data.database.entities

enum class ProjectPhase(val displayName: String) {
    PLANNING("Planning"),
    BREWING("Brewing"),
    FERMENTATION("Fermentation"),
    CONDITIONING("Conditioning"),
    COMPLETED("Completed")
}