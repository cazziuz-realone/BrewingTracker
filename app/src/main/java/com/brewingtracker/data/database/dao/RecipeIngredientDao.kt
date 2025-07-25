package com.brewingtracker.data.database.dao

import androidx.room.*
import com.brewingtracker.data.database.entities.RecipeIngredient
import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType
import com.brewingtracker.data.database.entities.RecipeIngredientWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    
    // FIXED: Return Flow to match repository expectation
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY additionTiming, createdAt")
    fun getRecipeIngredients(recipeId: String): Flow<List<RecipeIngredient>>
    
    // ADDED: Synchronous version needed for recipe duplication
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY additionTiming, createdAt")
    suspend fun getRecipeIngredientsSync(recipeId: String): List<RecipeIngredient>
    
    // FIXED: Use proper Transaction annotation for Room relationships
    @Transaction
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY additionTiming, createdAt")
    fun getRecipeIngredientsWithDetails(recipeId: String): Flow<List<RecipeIngredientWithDetails>>
    
    @Transaction
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId AND additionTiming = :timing ORDER BY createdAt")
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
    
    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredientsByRecipeId(recipeId: String)
    
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
    
    // Duplicate recipe ingredients for recipe copying
    suspend fun duplicateRecipeIngredients(
        sourceRecipeId: String, 
        newRecipeId: String
    ) {
        val sourceIngredients = getRecipeIngredientsSync(sourceRecipeId)
        val newIngredients = sourceIngredients.map { ingredient ->
            ingredient.copy(
                id = 0, // Auto-generate new ID
                recipeId = newRecipeId,
                createdAt = System.currentTimeMillis()
            )
        }
        insertRecipeIngredients(newIngredients)
    }
}

// Data class for inventory checking
data class InventoryCheckResult(
    val ingredientId: Int,
    val baseQuantity: Double,
    val currentStock: Double,
    val unit: String
)
