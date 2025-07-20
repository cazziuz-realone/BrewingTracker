package com.brewingtracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE type = :type ORDER BY name ASC")
    fun getIngredientsByType(type: String): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE beverageTypes LIKE '%' || :beverageType || '%' ORDER BY name ASC")
    fun getIngredientsByBeverageType(beverageType: String): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :searchTerm || '%' ORDER BY name ASC")
    fun searchIngredients(searchTerm: String): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Int): Ingredient?

    @Query("SELECT * FROM ingredients WHERE currentStock > 0 ORDER BY name ASC")
    fun getInStockIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("UPDATE ingredients SET currentStock = :stock, updatedAt = :timestamp WHERE id = :ingredientId")
    suspend fun updateStock(ingredientId: Int, stock: Double, timestamp: Long = System.currentTimeMillis())
}