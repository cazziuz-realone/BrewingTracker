package com.brewingtracker.data.database.entities

/**
 * Enum representing different batch sizes for brewing recipes
 * All calculations are based on a 1 gallon base, then scaled
 */
enum class BatchSize(
    val displayName: String,
    val ozValue: Int,
    val scaleFactor: Double
) {
    QUART("Quart", 32, 0.25),
    HALF_GALLON("Half Gallon", 64, 0.5),
    GALLON("Gallon", 128, 1.0),
    FIVE_GALLON("5 Gallons", 640, 5.0)
}
