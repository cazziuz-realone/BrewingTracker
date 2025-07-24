package com.brewingtracker.data.database.dao

import androidx.room.*
import com.brewingtracker.data.database.entities.Recipe
import com.brewingtracker.data.database.entities.BeverageType
import com.brewingtracker.data.database.entities.RecipeDifficulty
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun getAllRecipes(): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): Recipe?
    
    @Query("SELECT * FROM recipes WHERE beverageType = :beverageType ORDER BY name ASC")
    fun getRecipesByType(beverageType: BeverageType): Flow<List<Recipe>>
    
    // ADDED: Method needed by RecipeLibraryViewModel
    @Query("SELECT * FROM recipes WHERE beverageType = :beverageType ORDER BY updatedAt DESC")
    fun getRecipesByBeverageType(beverageType: BeverageType): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE difficulty = :difficulty ORDER BY name ASC")
    fun getRecipesByDifficulty(difficulty: RecipeDifficulty): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteRecipes(): Flow<List<Recipe>>
    
    @Query("""
        SELECT * FROM recipes 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR description LIKE '%' || :searchQuery || '%'
        OR style LIKE '%' || :searchQuery || '%'
        ORDER BY name ASC
    """)
    fun searchRecipes(searchQuery: String): Flow<List<Recipe>>
    
    // ADDED: Method needed by RecipeLibraryViewModel
    @Query("""
        SELECT * FROM recipes 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        OR style LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchRecipesByName(query: String): Flow<List<Recipe>>
    
    @Query("""
        SELECT * FROM recipes 
        WHERE beverageType = :beverageType 
        AND (name LIKE '%' || :searchQuery || '%' 
             OR description LIKE '%' || :searchQuery || '%'
             OR style LIKE '%' || :searchQuery || '%')
        ORDER BY name ASC
    """)
    fun searchRecipesByType(searchQuery: String, beverageType: BeverageType): Flow<List<Recipe>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long
    
    @Update
    suspend fun updateRecipe(recipe: Recipe)
    
    @Delete
    suspend fun deleteRecipe(recipe: Recipe)
    
    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)
    
    @Query("UPDATE recipes SET timesUsed = timesUsed + 1 WHERE id = :recipeId")
    suspend fun incrementTimesUsed(recipeId: String)
    
    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun setFavorite(recipeId: String, isFavorite: Boolean)
    
    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int
    
    @Query("SELECT COUNT(*) FROM recipes WHERE beverageType = :beverageType")
    suspend fun getRecipeCountByType(beverageType: BeverageType): Int
}