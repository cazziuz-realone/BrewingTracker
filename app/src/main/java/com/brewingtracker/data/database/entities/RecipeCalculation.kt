package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_calculations",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["recipeId", "batchSizeOz"], unique = true)
    ]
)
data class RecipeCalculation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: String,
    val batchSizeOz: Int, // 32, 64, 128, 640 (quart, half-gal, gallon, 5-gal)
    
    // Calculated values for this batch size
    val estimatedOG: Double? = null,
    val estimatedFG: Double? = null,
    val estimatedABV: Double? = null,
    val estimatedSRM: Double? = null,
    val estimatedIBU: Double? = null,
    val estimatedCost: Double? = null,
    
    // Nutritional info
    val totalCalories: Double? = null,
    val alcoholCalories: Double? = null,
    val residualSugar: Double? = null,
    
    // Process estimates
    val estimatedFermentationTime: Int? = null, // days
    val estimatedAgingTime: Int? = null, // days
    val estimatedTotalTime: Int? = null, // days
    
    val calculatedAt: Long = System.currentTimeMillis()
)

// Data class for real-time calculations (not stored in DB)
data class LiveRecipeCalculations(
    val estimatedOG: Double = 1.000,
    val estimatedFG: Double = 1.000,
    val estimatedABV: Double = 0.0,
    val estimatedSRM: Double = 0.0,
    val estimatedIBU: Double = 0.0,
    val batchSize: BatchSize = BatchSize.GALLON,
    val totalCost: Double = 0.0,
    val isCalculating: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

// Calculation status for UI feedback
enum class CalculationStatus {
    IDLE,
    CALCULATING,
    SUCCESS,
    ERROR
}
