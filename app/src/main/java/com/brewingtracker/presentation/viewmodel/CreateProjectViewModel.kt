package com.brewingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.brewingtracker.data.database.dao.IngredientDao
import com.brewingtracker.data.database.dao.ProjectDao
import com.brewingtracker.data.database.dao.ProjectIngredientDao
import com.brewingtracker.data.database.entities.*
import javax.inject.Inject
import java.util.*

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val projectDao: ProjectDao,
    private val ingredientDao: IngredientDao,
    private val projectIngredientDao: ProjectIngredientDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    val availableIngredients = ingredientDao.getAllIngredients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateType(type: BeverageType) {
        _uiState.update { it.copy(type = type) }
    }

    fun updateBatchSize(batchSize: String) {
        _uiState.update { it.copy(batchSize = batchSize) }
    }

    fun updateTargetOG(targetOG: String) {
        _uiState.update { it.copy(targetOG = targetOG) }
    }

    fun updateTargetFG(targetFG: String) {
        _uiState.update { it.copy(targetFG = targetFG) }
    }

    fun updateTargetABV(targetABV: String) {
        _uiState.update { it.copy(targetABV = targetABV) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updateIngredientSearchQuery(query: String) {
        _uiState.update { it.copy(ingredientSearchQuery = query) }
    }

    fun addIngredient(ingredient: Ingredient) {
        val currentState = _uiState.value
        val newIngredient = ProjectIngredientUi(
            id = currentState.selectedIngredients.size + 1,
            ingredient = ingredient,
            quantity = "1.0",
            unit = getDefaultUnit(ingredient.type),
            additionTime = getDefaultAdditionTime(ingredient.type)
        )
        _uiState.update {
            it.copy(selectedIngredients = it.selectedIngredients + newIngredient)
        }
    }

    fun removeIngredient(ingredientId: Int) {
        _uiState.update {
            it.copy(selectedIngredients = it.selectedIngredients.filter { ingredient ->
                ingredient.id != ingredientId
            })
        }
    }

    fun updateIngredientQuantity(ingredientId: Int, quantity: String) {
        _uiState.update { state ->
            state.copy(
                selectedIngredients = state.selectedIngredients.map { ingredient ->
                    if (ingredient.id == ingredientId) {
                        ingredient.copy(quantity = quantity)
                    } else {
                        ingredient
                    }
                }
            )
        }
    }

    fun updateIngredientUnit(ingredientId: Int, unit: String) {
        _uiState.update { state ->
            state.copy(
                selectedIngredients = state.selectedIngredients.map { ingredient ->
                    if (ingredient.id == ingredientId) {
                        ingredient.copy(unit = unit)
                    } else {
                        ingredient
                    }
                }
            )
        }
    }

    fun updateIngredientAdditionTime(ingredientId: Int, additionTime: String) {
        _uiState.update { state ->
            state.copy(
                selectedIngredients = state.selectedIngredients.map { ingredient ->
                    if (ingredient.id == ingredientId) {
                        ingredient.copy(additionTime = additionTime)
                    } else {
                        ingredient
                    }
                }
            )
        }
    }

    fun createProject(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val projectId = UUID.randomUUID().toString()

                // Create the project with ALL required parameters
                val project = Project(
                    id = projectId,
                    name = currentState.name,
                    type = currentState.type,
                    batchSize = currentState.batchSize.toDoubleOrNull() ?: 5.0,
                    targetOG = currentState.targetOG.toDoubleOrNull(),
                    targetFG = currentState.targetFG.toDoubleOrNull(),
                    targetABV = currentState.targetABV.toDoubleOrNull(),
                    notes = currentState.notes,
                    currentPhase = ProjectPhase.PLANNING,
                    isCompleted = false,
                    isFavorite = false,
                    isActive = true,
                    startDate = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                // Insert project
                projectDao.insertProject(project)

                // Insert project ingredients (if any)
                currentState.selectedIngredients.forEach { ingredientUi ->
                    val projectIngredient = ProjectIngredient(
                        projectId = projectId,
                        ingredientId = ingredientUi.ingredient.id,
                        quantity = ingredientUi.quantity.toDoubleOrNull() ?: 0.0,
                        unit = ingredientUi.unit,
                        additionTime = ingredientUi.additionTime,
                        notes = null
                    )
                    projectIngredientDao.insertProjectIngredient(projectIngredient)
                }

                onSuccess(projectId)
            } catch (e: Exception) {
                // Handle error - you can add logging here if needed
            }
        }
    }

    private fun getDefaultUnit(ingredientType: String): String {
        return when (ingredientType.lowercase()) {
            "grain", "malt" -> "lbs"
            "hop" -> "oz"
            "fruit", "adjunct" -> "lbs"
            "yeast_nutrient", "acid" -> "tsp"
            else -> "oz"
        }
    }

    private fun getDefaultAdditionTime(ingredientType: String): String {
        return when (ingredientType.lowercase()) {
            "grain", "malt" -> "Mash"
            "hop" -> "60 min"
            "fruit" -> "Secondary"
            "adjunct" -> "Boil"
            "yeast_nutrient" -> "Primary"
            else -> "As needed"
        }
    }
}

data class CreateProjectUiState(
    val name: String = "",
    val type: BeverageType = BeverageType.BEER,
    val batchSize: String = "5.0",
    val targetOG: String = "",
    val targetFG: String = "",
    val targetABV: String = "",
    val notes: String = "",
    val selectedIngredients: List<ProjectIngredientUi> = emptyList(),
    val ingredientSearchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isValidProject: Boolean
        get() = name.isNotBlank() && batchSize.isNotBlank()
}

data class ProjectIngredientUi(
    val id: Int,
    val ingredient: Ingredient,
    val quantity: String,
    val unit: String,
    val additionTime: String
)