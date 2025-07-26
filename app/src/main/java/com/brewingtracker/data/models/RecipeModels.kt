package com.brewingtracker.data.models

import com.brewingtracker.data.database.entities.RecipeIngredientWithDetails as RoomRecipeIngredientWithDetails
import com.brewingtracker.data.database.entities.ProjectIngredientWithDetails as RoomProjectIngredientWithDetails

/**
 * Type aliases for Room relation entities to avoid KAPT conflicts
 * These provide clean references to the Room entities in the models package
 */

typealias RecipeIngredientWithDetails = RoomRecipeIngredientWithDetails
typealias ProjectIngredientWithDetails = RoomProjectIngredientWithDetails
