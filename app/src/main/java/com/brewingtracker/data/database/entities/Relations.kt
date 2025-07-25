package com.brewingtracker.data.database.entities

import androidx.room.*
import com.brewingtracker.data.models.*

/**
 * Room relation entities for complex queries
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
