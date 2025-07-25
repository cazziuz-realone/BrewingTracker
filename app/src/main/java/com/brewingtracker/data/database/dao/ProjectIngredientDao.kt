package com.brewingtracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.brewingtracker.data.database.entities.ProjectIngredient
import com.brewingtracker.data.models.ProjectIngredientWithDetails

@Dao
interface ProjectIngredientDao {
    @Query("SELECT * FROM project_ingredients WHERE projectId = :projectId")
    fun getProjectIngredients(projectId: String): Flow<List<ProjectIngredient>>

    @Transaction
    @Query("SELECT * FROM project_ingredients WHERE projectId = :projectId ORDER BY additionTiming, createdAt")
    fun getProjectIngredientsWithDetails(projectId: String): Flow<List<ProjectIngredientWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectIngredient(projectIngredient: ProjectIngredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectIngredients(projectIngredients: List<ProjectIngredient>)

    @Update
    suspend fun updateProjectIngredient(projectIngredient: ProjectIngredient)

    @Delete
    suspend fun deleteProjectIngredient(projectIngredient: ProjectIngredient)

    // FIXED: Added missing method that repository expects
    @Query("DELETE FROM project_ingredients WHERE id = :projectIngredientId")
    suspend fun deleteProjectIngredient(projectIngredientId: Int)

    @Query("DELETE FROM project_ingredients WHERE projectId = :projectId")
    suspend fun deleteAllProjectIngredients(projectId: String)

    @Query("DELETE FROM project_ingredients WHERE projectId = :projectId")
    suspend fun removeAllIngredientsFromProject(projectId: String)

    @Query("DELETE FROM project_ingredients WHERE projectId = :projectId AND ingredientId = :ingredientId")
    suspend fun removeIngredientFromProject(projectId: String, ingredientId: Int)

    @Query("""
        UPDATE project_ingredients 
        SET plannedQuantity = :quantity, 
            unit = :unit, 
            additionTiming = :additionTime 
        WHERE projectId = :projectId AND ingredientId = :ingredientId
    """)
    suspend fun updateProjectIngredientDetails(
        projectId: String,
        ingredientId: Int,
        quantity: Double,
        unit: String,
        additionTime: String? = null
    )
}
