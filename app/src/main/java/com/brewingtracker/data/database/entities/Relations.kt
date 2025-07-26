package com.brewingtracker.data.database.entities

import androidx.room.*

/**
 * Room relation entities for complex queries
 * These are used by Room's @Transaction queries
 */

/**
 * Recipe ingredient with its associated ingredient details
 * This is used by Room's @Transaction queries
 */
data class RecipeIngredientWithDetails(
    @Embedded val recipeIngredient: RecipeIngredient,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "id"
    )
    val ingredient: Ingredient
)

/**
 * Project ingredient with its associated ingredient details  
 * This is used by Room's @Transaction queries
 */
data class ProjectIngredientWithDetails(
    @Embedded val projectIngredient: ProjectIngredient,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "id"
    )
    val ingredient: Ingredient
)
