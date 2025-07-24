package com.brewingtracker.data.database.dao

import androidx.room.*
import com.brewingtracker.data.database.entities.RecipeIngredient
import com.brewingtracker.data.database.entities.RecipeIngredientWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY additionTiming, createdAt")
    suspend fun getRecipeIngredients(recipeId: String): List<RecipeIngredient>
    
    @Query("""
        SELECT ri.*, i.* FROM recipe_ingredients ri
        INNER JOIN ingredients i ON ri.ingredientId = i.id
        WHERE ri.recipeId = :recipeId
        ORDER BY ri.additionTiming, ri.createdAt
    """)
    fun getRecipeIngredientsWithDetails(recipeId: String): Flow<List<RecipeIngredientWithDetails>>
    
    @Query("""
        SELECT ri.*, i.* FROM recipe_ingredients ri
        INNER JOIN ingredients i ON ri.ingredientId = i.id
        WHERE ri.recipeId = :recipeId AND ri.additionTiming = :timing
        ORDER BY ri.createdAt
    """)
    fun getRecipeIngredientsByTiming(recipeId: String, timing: String): Flow<List<RecipeIngredientWithDetails>>
    
    @Query("SELECT * FROM recipe_ingredients WHERE id = :ingredientId")
    suspend fun getRecipeIngredientById(ingredientId: Int): RecipeIngredient?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(recipeIngredients: List<RecipeIngredient>)
    
    @Update
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient)
    
    @Delete
    suspend fun deleteRecipeIngredient(recipeIngredient: RecipeIngredient)
    
    @Query("DELETE FROM recipe_ingredients WHERE id = :ingredientId")
    suspend fun deleteRecipeIngredientById(ingredientId: Int)
    
    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteAllRecipeIngredients(recipeId: String)
    
    @Query("SELECT COUNT(*) FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getRecipeIngredientCount(recipeId: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM recipe_ingredients ri
        INNER JOIN ingredients i ON ri.ingredientId = i.id
        WHERE ri.recipeId = :recipeId 
        AND (ri.baseQuantity * :scaleFactor) > i.currentStock
    """)
    suspend fun getInsufficientStockCount(recipeId: String, scaleFactor: Double): Int
    
    // Check if recipe ingredients have sufficient inventory for a specific batch size
    @Query("""
        SELECT ri.ingredientId, ri.baseQuantity, i.currentStock, i.unit
        FROM recipe_ingredients ri
        INNER JOIN ingredients i ON ri.ingredientId = i.id
        WHERE ri.recipeId = :recipeId
    """)
    suspend fun getInventoryCheckData(recipeId: String): List<InventoryCheckResult>
    
    // Copy ingredients from one recipe to another (for duplication)
    @Query("""
        INSERT INTO recipe_ingredients (recipeId, ingredientId, baseQuantity, baseUnit, additionTiming, additionDay, notes, isOptional, substitutable)
        SELECT :newRecipeId, ingredientId, baseQuantity, baseUnit, additionTiming, additionDay, notes, isOptional, substitutable
        FROM recipe_ingredients 
        WHERE recipeId = :sourceRecipeId
    """)
    suspend fun copyIngredientsToNewRecipe(sourceRecipeId: String, newRecipeId: String)
}

// Data class for inventory checking
data class InventoryCheckResult(
    val ingredientId: Int,
    val baseQuantity: Double,
    val currentStock: Double,
    val unit: String
)
