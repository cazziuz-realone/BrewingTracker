package com.brewingtracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.brewingtracker.data.database.entities.ProjectIngredient

@Dao
interface ProjectIngredientDao {
    @Query("SELECT * FROM project_ingredients WHERE projectId = :projectId")
    fun getProjectIngredients(projectId: String): Flow<List<ProjectIngredient>>

    @Query("""
        SELECT pi.*, i.name as ingredientName, i.type as ingredientType 
        FROM project_ingredients pi 
        INNER JOIN ingredients i ON pi.ingredientId = i.id 
        WHERE pi.projectId = :projectId
        ORDER BY i.type, i.name
    """)
    fun getProjectIngredientsWithDetails(projectId: String): Flow<List<ProjectIngredientWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectIngredient(projectIngredient: ProjectIngredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectIngredients(projectIngredients: List<ProjectIngredient>)

    @Update
    suspend fun updateProjectIngredient(projectIngredient: ProjectIngredient)

    @Delete
    suspend fun deleteProjectIngredient(projectIngredient: ProjectIngredient)

    @Query("DELETE FROM project_ingredients WHERE projectId = :projectId")
    suspend fun deleteAllProjectIngredients(projectId: String)

    // NEW: Alias method for repository compatibility - ADDED
    @Query("DELETE FROM project_ingredients WHERE projectId = :projectId")
    suspend fun removeAllIngredientsFromProject(projectId: String)

    @Query("DELETE FROM project_ingredients WHERE projectId = :projectId AND ingredientId = :ingredientId")
    suspend fun removeIngredientFromProject(projectId: String, ingredientId: Int)

    // NEW: Update individual ingredient details - ADDED  
    @Query("""
        UPDATE project_ingredients 
        SET quantity = :quantity, 
            unit = :unit, 
            additionTime = :additionTime 
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

// Data class for joined query results - must include ALL columns from pi.* selection
data class ProjectIngredientWithDetails(
    val id: Int,
    val projectId: String,
    val ingredientId: Int,
    val quantity: Double,
    val unit: String,
    val additionTime: String?,
    val notes: String?,
    val createdAt: Long,  // Added missing field from project_ingredients table
    val ingredientName: String,
    val ingredientType: String
)