package com.brewingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewingtracker.data.database.entities.Project
import com.brewingtracker.data.database.entities.ProjectPhase
import com.brewingtracker.data.database.entities.BeverageType
import com.brewingtracker.data.repository.BrewingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: BrewingRepository
) : ViewModel() {

    private val _selectedProjectType = MutableStateFlow<BeverageType?>(null)
    val selectedProjectType = _selectedProjectType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val allProjects = repository.getAllActiveProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredProjects = combine(
        allProjects,
        selectedProjectType,
        searchQuery
    ) { projects, type, query ->
        projects.filter { project ->
            val matchesType = type == null || project.type == type
            val matchesQuery = query.isBlank() || 
                project.name.contains(query, ignoreCase = true)
            matchesType && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteProjects = repository.getFavoriteProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun filterByType(type: BeverageType?) {
        _selectedProjectType.value = type
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createProject(
        name: String,
        type: BeverageType,
        description: String? = null,
        batchSize: Double? = null,
        targetOG: Double? = null,
        targetFG: Double? = null,
        targetABV: Double? = null
    ) {
        viewModelScope.launch {
            val project = Project(
                id = UUID.randomUUID().toString(),
                name = name,
                type = type,
                description = description,
                startDate = System.currentTimeMillis(),
                currentPhase = ProjectPhase.PLANNING,
                batchSize = batchSize,
                targetOG = targetOG,
                targetFG = targetFG,
                targetABV = targetABV
            )
            repository.insertProject(project)
        }
    }

    fun updateProjectPhase(projectId: String, phase: ProjectPhase) {
        viewModelScope.launch {
            repository.updateProjectPhase(projectId, phase)
        }
    }

    fun toggleProjectFavorite(projectId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateProjectFavorite(projectId, !isFavorite)
        }
    }

    fun completeProject(projectId: String) {
        viewModelScope.launch {
            repository.updateProjectPhase(projectId, ProjectPhase.COMPLETED)
            repository.updateProjectCompletion(projectId, true)
        }
    }
}