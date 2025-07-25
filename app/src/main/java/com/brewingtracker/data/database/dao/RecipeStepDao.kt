package com.brewingtracker.data.database.dao

import androidx.room.*
import com.brewingtracker.data.database.entities.RecipeStep
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeStepDao {
    
    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId ORDER BY stepNumber ASC")
    fun getStepsForRecipe(recipeId: String): Flow<List<RecipeStep>>
    
    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId ORDER BY stepNumber ASC")
    suspend fun getStepsForRecipeSync(recipeId: String): List<RecipeStep>
    
    @Query("SELECT * FROM recipe_steps WHERE id = :stepId")
    suspend fun getStepById(stepId: Int): RecipeStep?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: RecipeStep): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<RecipeStep>)
    
    @Update
    suspend fun updateStep(step: RecipeStep)
    
    @Delete
    suspend fun deleteStep(step: RecipeStep)
    
    @Query("DELETE FROM recipe_steps WHERE id = :stepId")
    suspend fun deleteStepById(stepId: Int)
    
    @Query("DELETE FROM recipe_steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsForRecipe(recipeId: String)
    
    @Query("SELECT COUNT(*) FROM recipe_steps WHERE recipeId = :recipeId")
    suspend fun getStepCountForRecipe(recipeId: String): Int
    
    // Reorder steps when a step is deleted or moved
    @Query("UPDATE recipe_steps SET stepNumber = stepNumber - 1 WHERE recipeId = :recipeId AND stepNumber > :deletedStepNumber")
    suspend fun reorderStepsAfterDeletion(recipeId: String, deletedStepNumber: Int)
    
    // Get steps by phase
    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId AND phase = :phase ORDER BY stepNumber ASC")
    suspend fun getStepsByPhase(recipeId: String, phase: String): List<RecipeStep>
    
    // Get next step number for a recipe
    @Query("SELECT COALESCE(MAX(stepNumber), 0) + 1 FROM recipe_steps WHERE recipeId = :recipeId")
    suspend fun getNextStepNumber(recipeId: String): Int
}
