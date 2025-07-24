package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["ingredientId"]),
        Index(value = ["recipeId", "ingredientId"], unique = true)
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: String,
    val ingredientId: Int,
    
    // Amount per BASE batch size (always stored for 1 gallon base)
    val baseQuantity: Double,
    val baseUnit: String,
    
    // Timing and process
    val additionTiming: String, // "primary", "secondary", "aging", "bottling"
    val additionDay: Int? = null, // Day number in process
    val notes: String? = null,
    
    // Optional for calculation purposes
    val isOptional: Boolean = false,
    val substitutable: Boolean = true,
    
    val createdAt: Long = System.currentTimeMillis()
)

// FIXED: Proper Room relationship data class
data class RecipeIngredientWithDetails(
    @Embedded val recipeIngredient: RecipeIngredient,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "id"
    )
    val ingredient: Ingredient
)

// Batch size enum for scaling
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

// Inventory status for recipe ingredients
enum class InventoryStatus {
    SUFFICIENT,    // Green checkmark - enough stock
    INSUFFICIENT,  // Red warning - not enough stock
    UNKNOWN        // Gray question - no stock info
}
