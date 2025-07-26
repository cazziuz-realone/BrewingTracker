package com.brewingtracker.data.database.entities

/**
 * Enum representing the difficulty level of a recipe
 */
enum class RecipeDifficulty {
    /**
     * Beginner level - simple recipes with basic ingredients and processes
     */
    BEGINNER,
    
    /**
     * Intermediate level - moderate complexity with some specialized techniques
     */
    INTERMEDIATE,
    
    /**
     * Advanced level - complex recipes requiring experience and specialized equipment
     */
    ADVANCED;

    /**
     * Get a user-friendly display name
     */
    fun getDisplayName(): String = when (this) {
        BEGINNER -> "Beginner"
        INTERMEDIATE -> "Intermediate" 
        ADVANCED -> "Advanced"
    }

    /**
     * Get a description of the difficulty level
     */
    fun getDescription(): String = when (this) {
        BEGINNER -> "Simple recipes perfect for first-time brewers"
        INTERMEDIATE -> "Moderate complexity with some specialized techniques"
        ADVANCED -> "Complex recipes requiring brewing experience"
    }

    /**
     * Get the estimated time commitment
     */
    fun getTimeCommitment(): String = when (this) {
        BEGINNER -> "2-4 hours"
        INTERMEDIATE -> "4-8 hours"
        ADVANCED -> "8+ hours"
    }
}
