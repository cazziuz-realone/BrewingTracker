package com.brewingtracker.presentation.screens.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.brewingtracker.data.database.entities.*
import com.brewingtracker.data.database.dao.RecipeDao
import com.brewingtracker.data.database.dao.RecipeIngredientDao
import javax.inject.Inject

@HiltViewModel
class RecipeBuilderViewModel @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecipeBuilderUiState())
    val uiState: StateFlow<RecipeBuilderUiState> = _uiState.asStateFlow()
    
    init {
        // Set up search flow
        _uiState
            .map { it.selectedCategory to it.searchQuery }
            .distinctUntilChanged()
            .onEach { (category, query) ->
                if (category != null && query.isNotEmpty()) {
                    searchIngredients(category, query)
                } else if (category != null && query.isEmpty()) {
                    // Show all ingredients for selected category
                    loadIngredientsByCategory(category)
                } else {
                    _uiState.value = _uiState.value.copy(searchResults = emptyList())
                }
            }
            .launchIn(viewModelScope)
    }
    
    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val recipe = recipeDao.getRecipeById(recipeId)
                if (recipe != null) {
                    _uiState.value = _uiState.value.copy(
                        recipe = recipe,
                        isEditing = true
                    )
                    
                    // Load recipe ingredients
                    loadRecipeIngredients(recipeId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    validation = listOf("Error loading recipe: ${e.message}")
                )
            }
        }
    }
    
    private suspend fun loadRecipeIngredients(recipeId: String) {
        try {
            // Note: This will need to be adapted based on your actual Room relationship setup
            // For now, we'll use a simpler approach
            val recipeIngredients = recipeIngredientDao.getRecipeIngredients(recipeId)
            
            // Convert to RecipeIngredientWithDetails - you'll need to implement this join
            // This is a placeholder - implement proper join query in your DAO
            val ingredientsWithDetails = emptyList<RecipeIngredientWithDetails>()
            
            _uiState.value = _uiState.value.copy(
                recipeIngredients = ingredientsWithDetails
            )
            calculateRecipe()
            checkInventoryStatus()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                validation = _uiState.value.validation + "Error loading ingredients: ${e.message}"
            )
        }
    }
    
    fun updateRecipe(recipe: Recipe) {
        _uiState.value = _uiState.value.copy(
            recipe = recipe.copy(updatedAt = System.currentTimeMillis()),
            canSave = recipe.name.isNotBlank()
        )
    }
    
    fun updateBatchSize(batchSize: BatchSize) {
        _uiState.value = _uiState.value.copy(selectedBatchSize = batchSize)
        calculateRecipe()
        checkInventoryStatus()
    }
    
    fun selectCategory(category: IngredientType) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            searchQuery = "" // Clear search when changing category
        )
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    private fun searchIngredients(category: IngredientType, query: String) {
        viewModelScope.launch {
            try {
                val results = recipeIngredientDao.searchIngredientsByTypeAndName(category, query)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList())
            }
        }
    }
    
    private fun loadIngredientsByCategory(category: IngredientType) {
        viewModelScope.launch {
            try {
                val results = recipeIngredientDao.getIngredientsByType(category)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList())
            }
        }
    }
    
    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            // Show add ingredient dialog - for now just add with default values
            val recipeIngredient = RecipeIngredient(
                recipeId = _uiState.value.recipe.id,
                ingredientId = ingredient.id,
                baseQuantity = 1.0, // Default quantity
                baseUnit = ingredient.unit,
                additionTiming = "primary" // Default timing
            )
            
            try {
                recipeIngredientDao.insertRecipeIngredient(recipeIngredient)
                // Reload recipe ingredients
                loadRecipeIngredients(_uiState.value.recipe.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    validation = _uiState.value.validation + "Error adding ingredient: ${e.message}"
                )
            }
        }
    }
    
    fun removeIngredient(ingredientId: Int) {
        viewModelScope.launch {
            try {
                recipeIngredientDao.deleteRecipeIngredientById(ingredientId)
                // Reload recipe ingredients
                loadRecipeIngredients(_uiState.value.recipe.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    validation = _uiState.value.validation + "Error removing ingredient: ${e.message}"
                )
            }
        }
    }
    
    private fun calculateRecipe() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true)
            
            try {
                val ingredients = _uiState.value.recipeIngredients
                
                if (ingredients.isNotEmpty()) {
                    // Basic calculation (you'll want to make this more sophisticated)
                    // This is a placeholder - implement proper gravity/ABV calculations
                    val estimatedOG = 1.000 + (ingredients.size * 0.010) // Placeholder
                    val estimatedFG = estimatedOG - 0.020 // Placeholder
                    val estimatedABV = (estimatedOG - estimatedFG) * 131.25
                    
                    val calculations = RecipeCalculations(
                        estimatedOG = estimatedOG,
                        estimatedFG = estimatedFG,
                        estimatedABV = estimatedABV
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        calculations = calculations,
                        isCalculating = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        calculations = null,
                        isCalculating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    calculations = null,
                    isCalculating = false,
                    validation = _uiState.value.validation + "Calculation error: ${e.message}"
                )
            }
        }
    }
    
    private fun checkInventoryStatus() {
        viewModelScope.launch {
            try {
                val ingredients = _uiState.value.recipeIngredients
                val batchSize = _uiState.value.selectedBatchSize
                
                val inventoryStatus = ingredients.associate { ingredientWithDetails ->
                    val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
                    val availableStock = ingredientWithDetails.ingredient.currentStock
                    
                    val status = when {
                        availableStock >= scaledQuantity -> InventoryStatus.SUFFICIENT
                        availableStock > 0 -> InventoryStatus.INSUFFICIENT
                        else -> InventoryStatus.UNKNOWN
                    }
                    
                    ingredientWithDetails.ingredient.id to status
                }
                
                _uiState.value = _uiState.value.copy(inventoryStatus = inventoryStatus)
            } catch (e: Exception) {
                // Handle error silently for inventory checking
            }
        }
    }
    
    fun saveRecipe() {
        viewModelScope.launch {
            try {
                val recipe = _uiState.value.recipe
                if (recipe.name.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        validation = listOf("Recipe name is required")
                    )
                    return@launch
                }
                
                recipeDao.insertRecipe(recipe)
                
                _uiState.value = _uiState.value.copy(
                    saveResult = Result.success(Unit)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveResult = Result.failure(e)
                )
            }
        }
    }
    
    fun createProjectFromRecipe() {
        viewModelScope.launch {
            // TODO: Implement project creation from recipe
            // This would create a new Project entity based on the current recipe
        }
    }
    
    fun clearSaveResult() {
        _uiState.value = _uiState.value.copy(saveResult = null)
    }
}

data class RecipeBuilderUiState(
    val recipe: Recipe = Recipe(
        name = "",
        beverageType = BeverageType.MEAD,
        difficulty = RecipeDifficulty.BEGINNER
    ),
    val isEditing: Boolean = false,
    val selectedBatchSize: BatchSize = BatchSize.GALLON,
    val selectedCategory: IngredientType? = null,
    val searchQuery: String = "",
    val searchResults: List<Ingredient> = emptyList(),
    val recipeIngredients: List<RecipeIngredientWithDetails> = emptyList(),
    val inventoryStatus: Map<Int, InventoryStatus> = emptyMap(),
    val calculations: RecipeCalculations? = null,
    val isCalculating: Boolean = false,
    val validation: List<String> = emptyList(),
    val canSave: Boolean = false,
    val saveResult: Result<Unit>? = null
)
