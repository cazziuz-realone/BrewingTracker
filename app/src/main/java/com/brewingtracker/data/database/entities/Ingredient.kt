package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "ingredients")
@Parcelize
data class Ingredient(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: IngredientType,
    val category: String? = null,
    val description: String? = null,
    
    // Beverage compatibility
    val beverageTypes: String, // "beer,mead,wine,cider"
    
    // Brewing characteristics
    val colorLovibond: Double? = null,
    val alphaAcidPercentage: Double? = null,
    val ppgExtract: Double? = null, // Points per gallon extract
    val maxUsagePercentage: Double? = null,
    
    // Inventory management
    val currentStock: Double = 0.0,
    val unit: String = "lbs",
    val costPerUnit: Double? = null,
    val supplier: String? = null,
    val expirationDate: Long? = null,
    val lastRestocked: Long? = null,
    
    // Advanced data
    val origin: String? = null,
    val harvestYear: Int? = null,
    val substituteIngredients: String? = null, // JSON array of substitute IDs
    val flavorProfile: String? = null, // JSON with descriptors
    val barcode: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class IngredientType {
    GRAIN,
    HOP,
    FRUIT,
    ADJUNCT,
    SPICE,
    YEAST_NUTRIENT,
    ACID,
    WATER_TREATMENT,
    CLARIFIER,
    OTHER
}