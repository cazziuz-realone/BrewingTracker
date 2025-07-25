package com.brewingtracker.data.database.dao

import androidx.room.*
import com.brewingtracker.data.database.entities.RecipeCalculation
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeCalculationDao {
    
    @Query("SELECT * FROM recipe_calculations WHERE recipeId = :recipeId ORDER BY batchSizeOz ASC")
    fun getCalculationsForRecipe(recipeId: String): Flow<List<RecipeCalculation>>
    
    @Query("SELECT * FROM recipe_calculations WHERE recipeId = :recipeId ORDER BY batchSizeOz ASC")
    suspend fun getCalculationsForRecipeSync(recipeId: String): List<RecipeCalculation>
    
    @Query("SELECT * FROM recipe_calculations WHERE recipeId = :recipeId AND batchSizeOz = :batchSizeOz")
    suspend fun getCalculationForBatchSize(recipeId: String, batchSizeOz: Int): RecipeCalculation?
    
    @Query("SELECT * FROM recipe_calculations WHERE id = :calculationId")
    suspend fun getCalculationById(calculationId: Int): RecipeCalculation?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: RecipeCalculation): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculations(calculations: List<RecipeCalculation>)
    
    @Update
    suspend fun updateCalculation(calculation: RecipeCalculation)
    
    @Delete
    suspend fun deleteCalculation(calculation: RecipeCalculation)
    
    @Query("DELETE FROM recipe_calculations WHERE id = :calculationId")
    suspend fun deleteCalculationById(calculationId: Int)
    
    @Query("DELETE FROM recipe_calculations WHERE recipeId = :recipeId")
    suspend fun deleteCalculationsForRecipe(recipeId: String)
    
    @Query("DELETE FROM recipe_calculations WHERE recipeId = :recipeId AND batchSizeOz = :batchSizeOz")
    suspend fun deleteCalculationForBatchSize(recipeId: String, batchSizeOz: Int)
    
    // Cache management queries
    @Query("SELECT COUNT(*) FROM recipe_calculations WHERE recipeId = :recipeId")
    suspend fun getCalculationCountForRecipe(recipeId: String): Int
    
    @Query("DELETE FROM recipe_calculations WHERE calculatedAt < :timestamp")
    suspend fun deleteOldCalculations(timestamp: Long)
    
    // Get all calculations older than specified days
    @Query("DELETE FROM recipe_calculations WHERE calculatedAt < :cutoffTimestamp")
    suspend fun cleanupOldCalculations(cutoffTimestamp: Long)
    
    // Update specific calculation values
    @Query("UPDATE recipe_calculations SET estimatedOG = :og, estimatedFG = :fg, estimatedABV = :abv, calculatedAt = :timestamp WHERE recipeId = :recipeId AND batchSizeOz = :batchSizeOz")
    suspend fun updateCalculationValues(
        recipeId: String, 
        batchSizeOz: Int, 
        og: Double?, 
        fg: Double?, 
        abv: Double?, 
        timestamp: Long
    )
    
    // Batch size specific queries
    @Query("SELECT * FROM recipe_calculations WHERE batchSizeOz = :batchSizeOz ORDER BY calculatedAt DESC")
    suspend fun getCalculationsForBatchSize(batchSizeOz: Int): List<RecipeCalculation>
    
    // Cost calculation queries
    @Query("SELECT * FROM recipe_calculations WHERE recipeId = :recipeId AND estimatedCost IS NOT NULL ORDER BY batchSizeOz ASC")
    suspend fun getCalculationsWithCost(recipeId: String): List<RecipeCalculation>
    
    @Query("UPDATE recipe_calculations SET estimatedCost = :cost, calculatedAt = :timestamp WHERE recipeId = :recipeId AND batchSizeOz = :batchSizeOz")
    suspend fun updateCalculationCost(recipeId: String, batchSizeOz: Int, cost: Double, timestamp: Long)
}
