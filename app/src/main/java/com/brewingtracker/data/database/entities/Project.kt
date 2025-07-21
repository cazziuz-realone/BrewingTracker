package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "projects")
@Parcelize
data class Project(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: BeverageType,  // Changed from ProjectType to BeverageType
    val description: String? = null,
    val startDate: Long,
    val targetFinishDate: Long? = null,
    val currentPhase: ProjectPhase,
    
    // Batch info
    val batchSize: Double? = null,
    val batchUnit: String = "gallons",
    
    // Target parameters
    val targetOG: Double? = null,
    val targetFG: Double? = null,
    val targetABV: Double? = null,
    val targetIBU: Double? = null,
    val targetSRM: Double? = null,
    
    // Actual measurements
    val actualOG: Double? = null,
    val actualFG: Double? = null,
    val actualABV: Double? = null,
    val actualIBU: Double? = null,
    val actualSRM: Double? = null,
    
    // Status
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Notes and photos
    val notes: String? = null,
    val photoPath: String? = null,
    val recipeNotes: String? = null,
    val processNotes: String? = null,
    val tastingNotes: String? = null
) : Parcelable

// REMOVED: Duplicate enum definitions that conflicted with separate enum files
// ProjectPhase is now defined in ProjectPhase.kt
// BeverageType is now defined in BeverageType.kt