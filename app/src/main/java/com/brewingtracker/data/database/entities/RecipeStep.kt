package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_steps",
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
        Index(value = ["recipeId", "stepNumber"], unique = true)
    ]
)
data class RecipeStep(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: String,
    val stepNumber: Int,
    val phase: String, // "preparation", "primary", "secondary", "aging", "bottling"
    val title: String,
    val description: String,
    val estimatedDuration: String? = null, // "30 minutes", "2 weeks", etc.
    val temperature: String? = null, // Optional temperature guidance
    val isOptional: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// Process phases for recipes
enum class RecipePhase(val displayName: String) {
    PREPARATION("Preparation"),
    PRIMARY("Primary Fermentation"),
    SECONDARY("Secondary Fermentation"),
    AGING("Aging"),
    CLARIFICATION("Clarification"),
    BOTTLING("Bottling"),
    CARBONATION("Carbonation")
}
