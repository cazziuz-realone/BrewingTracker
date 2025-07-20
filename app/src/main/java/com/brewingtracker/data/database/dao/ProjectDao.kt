package com.brewingtracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.brewingtracker.data.database.entities.Project
import com.brewingtracker.data.database.entities.ProjectType
import com.brewingtracker.data.database.entities.ProjectPhase

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE isActive = 1 ORDER BY startDate DESC")
    fun getAllActiveProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE type = :type AND isActive = 1 ORDER BY startDate DESC")
    fun getProjectsByType(type: ProjectType): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): Project?

    @Query("SELECT * FROM projects WHERE isCompleted = 0 AND isActive = 1 ORDER BY startDate DESC")
    fun getActiveIncompleteProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE isFavorite = 1 AND isActive = 1 ORDER BY startDate DESC")
    fun getFavoriteProjects(): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("UPDATE projects SET currentPhase = :phase, updatedAt = :timestamp WHERE id = :projectId")
    suspend fun updateProjectPhase(projectId: String, phase: ProjectPhase, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET isCompleted = :isCompleted, updatedAt = :timestamp WHERE id = :projectId")
    suspend fun updateProjectCompletion(projectId: String, isCompleted: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :projectId")
    suspend fun updateProjectFavorite(projectId: String, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())
}