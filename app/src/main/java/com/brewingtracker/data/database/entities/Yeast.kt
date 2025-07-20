package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "yeasts")
data class Yeast(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val brand: String,
    val type: YeastType,
    val form: YeastForm,
    val attenuationRange: String? = null,
    val temperatureRange: String? = null,
    val alcoholTolerance: Double? = null,
    val flocculation: FlocculationLevel? = null,
    val description: String? = null,
    val currentStock: Int = 0,
    val unit: String = "packets",
    val costPerUnit: Double? = null,
    val supplier: String? = null,
    val expirationDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class YeastType {
    ALE, LAGER, WILD, WINE, MEAD, CHAMPAGNE, DISTILLERS, OTHER
}

enum class YeastForm {
    DRY, LIQUID, SLANT, PLATE, OTHER
}

enum class FlocculationLevel {
    LOW, MEDIUM, HIGH, VERY_HIGH
}