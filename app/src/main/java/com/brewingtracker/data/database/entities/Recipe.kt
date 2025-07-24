package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Entity(tableName = "recipes")
@Parcelize
data class Recipe(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val beverageType: BeverageType,
    val style: String? = null, // "Traditional Mead", "Melomel", etc.
    val difficulty: RecipeDifficulty = RecipeDifficulty.BEGINNER,
    
    // Batch scaling info - always store for 1 gallon base
    val baseBatchSize: Double = 1.0,
    val baseBatchUnit: String = "gallons",
    
    // Target parameters for base batch
    val targetOG: Double? = null,
    val targetFG: Double? = null,
    val targetABV: Double? = null,
    val targetSRM: Double? = null,
    val estimatedTimeWeeks: Int? = null,
    
    // Recipe metadata
    val tags: String? = null, // JSON array: ["sweet", "spiced", "traditional"]
    val isPublic: Boolean = false,
    val isFavorite: Boolean = false,
    val timesUsed: Int = 0,
    
    // Process information
    val processSteps: String? = null, // JSON array of process steps
    val fermentationNotes: String? = null,
    val agingNotes: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class RecipeDifficulty {
    BEGINNER, INTERMEDIATE, ADVANCED
}

// Data class for calculations (moved here to avoid duplication)
data class RecipeCalculations(
    val estimatedOG: Double,
    val estimatedFG: Double,
    val estimatedABV: Double
)
