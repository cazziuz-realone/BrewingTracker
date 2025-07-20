package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: BeverageType,
    val batchSize: Double,
    val targetOG: Double? = null,
    val targetFG: Double? = null,
    val targetABV: Double? = null,
    val notes: String? = null,
    val currentPhase: ProjectPhase,
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,
    val isActive: Boolean = true,
    val startDate: Long,
    val updatedAt: Long
)

enum class BeverageType(val displayName: String) {
    BEER("Beer"),
    MEAD("Mead"),
    WINE("Wine"),
    CIDER("Cider"),
    KOMBUCHA("Kombucha"),
    WATER_KEFIR("Water Kefir"),
    OTHER("Other")
}

enum class ProjectPhase(val displayName: String) {
    PLANNING("Planning"),
    BREWING("Brewing"),
    FERMENTATION("Fermentation"),
    CONDITIONING("Conditioning"),
    COMPLETED("Completed")
}