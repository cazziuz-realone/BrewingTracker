package com.brewingtracker.data.models

import com.brewingtracker.data.database.entities.BatchSize

/**
 * Data class representing live calculations for a recipe
 * These values update in real-time as ingredients are added/removed
 */
data class LiveRecipeCalculations(
    val estimatedOG: Double? = null,
    val estimatedFG: Double? = null,
    val estimatedABV: Double? = null,
    val estimatedSRM: Double? = null,
    val estimatedCost: Double? = null,
    val batchSize: BatchSize = BatchSize.GALLON,
    val totalGravityPoints: Double = 0.0,
    val totalVolume: Double = 1.0 // in gallons
)
