package com.brewingtracker.presentation.screens.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewingtracker.data.database.entities.*
import com.brewingtracker.data.models.*
import com.brewingtracker.data.repository.BrewingRepository
import com.brewingtracker.data.services.RecipeCalculationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnhancedRecipeBuilderViewModel @Inject constructor(
    private val repository: BrewingRepository,
    private val calculationService: RecipeCalculationService
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(RecipeBuilderUiState())
    val uiState: StateFlow<RecipeBuilderUiState> = _uiState.asStateFlow()

    // Recipe ingredients with details
    private val _recipeIngredients = MutableStateFlow<List<RecipeIngredientWithDetails>>(emptyList())
    val recipeIngredients: StateFlow<List<RecipeIngredientWithDetails>> = _recipeIngredients.asStateFlow()

    // Search results
    private val _searchResults = MutableStateFlow<List<Ingredient>>(emptyList())
    val searchResults: StateFlow<List<Ingredient>> = _searchResults.asStateFlow()

    // Initialize with default recipe
    init {
        createNewRecipe(BeverageType.MEAD)
    }

    fun createNewRecipe(beverageType: BeverageType) {
        val newRecipe = Recipe(
            name = "New ${beverageType.displayName} Recipe",
            beverageType = beverageType,
            description = "A delicious ${beverageType.displayName.lowercase()} recipe"
        )
        
        _uiState.value = _uiState.value.copy(
            recipe = newRecipe,
            processSteps = calculationService.generateDefaultSteps(beverageType),
            isLoading = false
        )
        
        // Trigger initial calculations
        calculateRecipeParameters()
    }

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val recipe = repository.getRecipeById(recipeId)
                if (recipe != null) {
                    _uiState.value = _uiState.value.copy(
                        recipe = recipe,
                        isLoading = false
                    )
                    
                    // Load recipe ingredients
                    repository.getRecipeIngredientsWithDetails(recipeId).collect { ingredients ->
                        _recipeIngredients.value = ingredients
                        calculateRecipeParameters()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Recipe not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun updateRecipe(recipe: Recipe) {
        _uiState.value = _uiState.value.copy(recipe = recipe)
        calculateRecipeParameters()
    }

    fun updateBatchSize(batchSize: BatchSize) {
        _uiState.value = _uiState.value.copy(selectedBatchSize = batchSize)
        calculateRecipeParameters()
    }

    fun selectCategory(category: IngredientType?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun searchIngredients(query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchIngredients(query, _uiState.value.selectedCategory)
                _searchResults.value = results
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to search ingredients: ${e.message}"
                )
            }
        }
    }

    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                val recipeIngredient = RecipeIngredient(
                    recipeId = _uiState.value.recipe.id,
                    ingredientId = ingredient.id,
                    baseQuantity = 1.0,
                    baseUnit = ingredient.unit,
                    additionTiming = "primary"
                )
                
                repository.insertRecipeIngredient(recipeIngredient)
                
                // Refresh ingredients list
                loadRecipeIngredients()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add ingredient: ${e.message}"
                )
            }
        }
    }

    fun updateIngredient(recipeIngredient: RecipeIngredient) {
        viewModelScope.launch {
            try {
                repository.updateRecipeIngredient(recipeIngredient)
                loadRecipeIngredients()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update ingredient: ${e.message}"
                )
            }
        }
    }

    fun removeIngredient(recipeIngredientId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteRecipeIngredient(recipeIngredientId)
                loadRecipeIngredients()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove ingredient: ${e.message}"
                )
            }
        }
    }

    fun updateStep(step: RecipeStep) {
        viewModelScope.launch {
            try {
                repository.updateRecipeStep(step)
                loadRecipeSteps()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update step: ${e.message}"
                )
            }
        }
    }

    fun addStep(step: RecipeStep) {
        viewModelScope.launch {
            try {
                repository.insertRecipeStep(step)
                loadRecipeSteps()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add step: ${e.message}"
                )
            }
        }
    }

    fun removeStep(stepId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteRecipeStep(stepId)
                loadRecipeSteps()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove step: ${e.message}"
                )
            }
        }
    }

    fun saveRecipe() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val recipe = _uiState.value.recipe.copy(
                    updatedAt = System.currentTimeMillis()
                )
                
                repository.updateRecipe(recipe)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to save recipe: ${e.message}"
                )
            }
        }
    }

    fun createProjectFromRecipe(projectName: String) {
        viewModelScope.launch {
            try {
                val project = Project(
                    name = projectName,
                    description = _uiState.value.recipe.description,
                    beverageType = _uiState.value.recipe.beverageType,
                    targetOG = _uiState.value.calculations.estimatedOG,
                    targetFG = _uiState.value.calculations.estimatedFG,
                    targetABV = _uiState.value.calculations.estimatedABV
                )
                
                val projectId = repository.createProject(project)
                
                // Copy recipe ingredients to project
                _recipeIngredients.value.forEach { ingredientWithDetails ->
                    val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * 
                                        _uiState.value.selectedBatchSize.scaleFactor
                    
                    val projectIngredient = ProjectIngredient(
                        projectId = projectId,
                        ingredientId = ingredientWithDetails.ingredient.id,
                        quantity = scaledQuantity,
                        unit = ingredientWithDetails.recipeIngredient.baseUnit,
                        additionTime = ingredientWithDetails.recipeIngredient.additionTiming
                    )
                    
                    repository.addIngredientToProject(projectIngredient)
                }
                
                _uiState.value = _uiState.value.copy(
                    projectCreated = true,
                    createdProjectId = projectId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create project: ${e.message}"
                )
            }
        }
    }

    private fun calculateRecipeParameters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true)
            
            try {
                val calculations = calculationService.calculateRecipeParameters(
                    _recipeIngredients.value,
                    _uiState.value.selectedBatchSize
                )
                
                val inventoryStatus = calculationService.checkInventoryStatus(
                    _recipeIngredients.value,
                    _uiState.value.selectedBatchSize
                )
                
                _uiState.value = _uiState.value.copy(
                    calculations = calculations,
                    inventoryStatus = inventoryStatus,
                    isCalculating = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCalculating = false,
                    error = "Calculation failed: ${e.message}"
                )
            }
        }
    }

    private fun loadRecipeIngredients() {
        viewModelScope.launch {
            repository.getRecipeIngredientsWithDetails(_uiState.value.recipe.id).collect { ingredients ->
                _recipeIngredients.value = ingredients
                calculateRecipeParameters()
            }
        }
    }

    private fun loadRecipeSteps() {
        viewModelScope.launch {
            repository.getRecipeSteps(_uiState.value.recipe.id).collect { steps ->
                _uiState.value = _uiState.value.copy(processSteps = steps)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

// UI State data class
data class RecipeBuilderUiState(
    val recipe: Recipe = Recipe(
        name = "New Recipe",
        beverageType = BeverageType.MEAD,
        description = ""
    ),
    val selectedBatchSize: BatchSize = BatchSize.GALLON,
    val selectedCategory: IngredientType? = null,
    val calculations: LiveRecipeCalculations = LiveRecipeCalculations(),
    val inventoryStatus: Map<Int, InventoryStatus> = emptyMap(),
    val processSteps: List<RecipeStep> = emptyList(),
    val validation: RecipeValidation = RecipeValidation(),
    val isLoading: Boolean = false,
    val isCalculating: Boolean = false,
    val isSaved: Boolean = false,
    val projectCreated: Boolean = false,
    val createdProjectId: String? = null,
    val error: String? = null
)

// Recipe validation data class
data class RecipeValidation(
    val isValid: Boolean = true,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)
