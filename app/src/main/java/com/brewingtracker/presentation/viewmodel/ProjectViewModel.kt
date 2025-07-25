package com.brewingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.brewingtracker.data.database.entities.Project
import com.brewingtracker.data.database.entities.BeverageType
import com.brewingtracker.data.database.entities.ProjectPhase
import com.brewingtracker.data.database.dao.ProjectDao
import com.brewingtracker.data.repository.BrewingRepository
import javax.inject.Inject
import java.util.*

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectDao: ProjectDao,
    private val repository: BrewingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    val allProjects = projectDao.getAllActiveProjects()

    fun createProject(
        name: String,
        type: BeverageType,
        targetOG: Double? = null,
        targetFG: Double? = null,
        targetABV: Double? = null,
        batchSize: Double? = 5.0, // FIXED: Make nullable to match entity
        notes: String? = null
    ) {
        viewModelScope.launch {
            val project = Project(
                id = UUID.randomUUID().toString(),
                name = name,
                type = type,
                batchSize = batchSize,
                targetOG = targetOG,
                targetFG = targetFG,
                targetABV = targetABV,
                notes = notes,
                currentPhase = ProjectPhase.PLANNING,
                isCompleted = false,
                isFavorite = false,
                isActive = true,
                startDate = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            projectDao.insertProject(project)
            _uiState.value = _uiState.value.copy(
                showSuccess = true,
                successMessage = "Project '$name' created successfully!"
            )
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            projectDao.updateProject(project)
        }
    }

    fun updateProjectPhase(projectId: String, phase: ProjectPhase) {
        viewModelScope.launch {
            projectDao.updateProjectPhase(projectId, phase, System.currentTimeMillis())
        }
    }

    /**
     * Delete a project
     */
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                // First, remove all project ingredients
                repository.removeAllIngredientsFromProject(projectId)
                
                // Then delete the project
                projectDao.deleteProject(projectId)
                
                _uiState.value = _uiState.value.copy(
                    showSuccess = true,
                    successMessage = "Project deleted successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showError = true,
                    errorMessage = "Failed to delete project: ${e.message}"
                )
            }
        }
    }

    // FIXED: This should return a reactive Flow
    fun getProjectById(projectId: String): Flow<Project?> {
        return allProjects.map { projectList ->
            projectList.find { it.id == projectId }
        }
    }

    // ==========================================
    // PROJECT INGREDIENT OPERATIONS - FIXED
    // ==========================================
    
    /**
     * Get project ingredients with full ingredient details
     */
    fun getProjectIngredientsWithDetails(projectId: String) = 
        repository.getProjectIngredientsWithDetails(projectId)

    /**
     * Remove an ingredient from the project
     */
    fun removeIngredientFromProject(projectId: String, ingredientId: Int) {
        viewModelScope.launch {
            repository.removeIngredientFromProject(projectId, ingredientId)
        }
    }

    /**
     * Update ingredient quantity and unit in project
     */
    fun updateProjectIngredient(
        projectId: String,
        ingredientId: Int,
        quantity: Double,
        unit: String,
        additionTime: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateProjectIngredient(projectId, ingredientId, quantity, unit, additionTime)
                
                _uiState.value = _uiState.value.copy(
                    showSuccess = true,
                    successMessage = "Ingredient updated successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showError = true,
                    errorMessage = "Failed to update ingredient: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            showSuccess = false,
            showError = false,
            successMessage = null,
            errorMessage = null
        )
    }
}

data class ProjectUiState(
    val isLoading: Boolean = false,
    val showSuccess: Boolean = false,
    val showError: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
