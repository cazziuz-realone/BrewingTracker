package com.brewingtracker.data.database.dao

import androidx.room.*
import com.brewingtracker.data.database.entities.RecipeIngredient
import com.brewingtracker.data.database.entities.RecipeIngredientWithDetails
import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY additionTiming, createdAt")
    suspend fun getRecipeIngredients(recipeId: String): List<RecipeIngredient>
    
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
        val sourceIngredients = getRecipeIngredients(sourceRecipeId)
        val newIngredients = sourceIngredients.map { ingredient ->
            ingredient.copy(
                id = 0, // Auto-generate new ID
                recipeId = newRecipeId,
                createdAt = System.currentTimeMillis()
            )
        }
        insertRecipeIngredients(newIngredients)
    }
    
    // Search ingredients by type and name for the recipe builder
    @Query("""
        SELECT * FROM ingredients 
        WHERE type = :type 
        AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY name ASC
        LIMIT 50
    """)
    suspend fun searchIngredientsByTypeAndName(type: IngredientType, query: String): List<Ingredient>
    
    @Query("""
        SELECT * FROM ingredients 
        WHERE type = :type 
        ORDER BY name ASC
        LIMIT 100
    """)
    suspend fun getIngredientsByType(type: IngredientType): List<Ingredient>
}

// Data class for inventory checking
data class InventoryCheckResult(
    val ingredientId: Int,
    val baseQuantity: Double,
    val currentStock: Double,
    val unit: String
)
