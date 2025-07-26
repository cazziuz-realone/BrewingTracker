package com.brewingtracker.data.models

/**
 * Enum representing different batch sizes for brewing projects
 * with scaling factors and display values
 */
enum class BatchSize(
    val displayName: String,
    val ozValue: Int,
    val scaleFactor: Double
) {
    QUART("Quart", 32, 0.25),
    HALF_GALLON("Half Gallon", 64, 0.5),
    GALLON("Gallon", 128, 1.0),
    FIVE_GALLON("5 Gallons", 640, 5.0);
    
    companion object {
        fun fromOzValue(ozValue: Int): BatchSize? {
            return values().find { it.ozValue == ozValue }
        }
        
        fun fromScaleFactor(scaleFactor: Double): BatchSize? {
            return values().find { it.scaleFactor == scaleFactor }
        }
    }
}
