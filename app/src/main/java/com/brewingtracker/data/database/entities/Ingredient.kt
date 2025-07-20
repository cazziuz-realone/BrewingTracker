package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: IngredientType,
    val category: String? = null,
    val description: String? = null,
    val colorLovibond: Double? = null,
    val alphaAcidPercentage: Double? = null,
    val ppgExtract: Double? = null,
    val maxUsagePercentage: Double? = null,
    val currentStock: Double = 0.0,
    val unit: String = "lbs",
    val costPerUnit: Double? = null,
    val supplier: String? = null,
    val expirationDate: Long? = null,
    val lastRestocked: Long? = null,
    val origin: String? = null,
    val harvestYear: Int? = null,
    val substituteIngredients: String? = null,
    val flavorProfile: String? = null,
    val barcode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class IngredientType {
    GRAIN, HOP, YEAST, ADJUNCT, FRUIT, SPICE, OTHER
}