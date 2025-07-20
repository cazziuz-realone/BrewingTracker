package com.brewingtracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.brewingtracker.data.database.entities.Project
import com.brewingtracker.data.database.entities.ProjectPhase

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllActiveProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("UPDATE projects SET currentPhase = :phase, updatedAt = :timestamp WHERE id = :projectId")
    suspend fun updateProjectPhase(projectId: String, phase: ProjectPhase, timestamp: Long = System.currentTimeMillis())
}