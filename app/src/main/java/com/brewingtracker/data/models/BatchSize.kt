package com.brewingtracker.data.models

/**
 * Enum representing different batch sizes for recipe scaling
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

    /**
     * Convert to gallons for calculations
     */
    fun toGallons(): Double = ozValue / 128.0
    
    companion object {
        fun fromOunces(ounces: Int): BatchSize {
            return values().minByOrNull { kotlin.math.abs(it.ozValue - ounces) } ?: GALLON
        }
    }
}
