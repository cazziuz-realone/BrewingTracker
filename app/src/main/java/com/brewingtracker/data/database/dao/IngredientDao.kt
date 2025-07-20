package com.brewingtracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE type = :type ORDER BY name ASC")
    fun getIngredientsByType(type: IngredientType): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE type = :type AND isActive = 1 ORDER BY name ASC")
    fun getActiveIngredientsByType(type: IngredientType): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :searchTerm || '%' ORDER BY name ASC")
    fun searchIngredients(searchTerm: String): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :searchTerm || '%' AND isActive = 1 ORDER BY name ASC")
    fun searchActiveIngredients(searchTerm: String): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("UPDATE ingredients SET isActive = 0 WHERE id = :id")
    suspend fun deactivateIngredient(id: String)

    @Query("UPDATE ingredients SET isActive = 1 WHERE id = :id")
    suspend fun activateIngredient(id: String)
}