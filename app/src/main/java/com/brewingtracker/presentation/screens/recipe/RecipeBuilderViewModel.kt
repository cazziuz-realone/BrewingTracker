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
            // Use the proper Flow-based method for getting ingredients with details
            recipeIngredientDao.getRecipeIngredientsWithDetails(recipeId)
                .collect { ingredientsWithDetails ->
                    _uiState.value = _uiState.value.copy(
                        recipeIngredients = ingredientsWithDetails
                    )
                    calculateRecipe()
                    checkInventoryStatus()
                }
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
    
    // FIXED: Completely rewritten to prevent foreign key constraint errors
    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                val currentRecipe = _uiState.value.recipe
                
                // Validate recipe name first
                if (currentRecipe.name.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        validation = listOf("Please enter a recipe name before adding ingredients")
                    )
                    return@launch
                }
                
                // CRITICAL FIX: Always ensure recipe exists in database first
                val savedRecipeId = if (_uiState.value.isEditing) {
                    // Recipe already exists, just update it
                    recipeDao.updateRecipe(currentRecipe)
                    currentRecipe.id
                } else {
                    // NEW RECIPE: Must insert recipe first before adding ingredients
                    try {
                        recipeDao.insertRecipe(currentRecipe)
                        // Mark as editing now that it's saved
                        _uiState.value = _uiState.value.copy(isEditing = true)
                        currentRecipe.id
                    } catch (e: Exception) {
                        // If insert fails, the recipe might already exist, try to get it
                        val existingRecipe = recipeDao.getRecipeById(currentRecipe.id)
                        if (existingRecipe != null) {
                            _uiState.value = _uiState.value.copy(isEditing = true)
                            existingRecipe.id
                        } else {
                            throw e
                        }
                    }
                }
                
                // Now safely add the ingredient to the saved recipe
                val recipeIngredient = RecipeIngredient(
                    recipeId = savedRecipeId,
                    ingredientId = ingredient.id,
                    baseQuantity = 1.0, // Default quantity
                    baseUnit = ingredient.unit,
                    additionTiming = "primary" // Default timing
                )
                
                // Insert the recipe ingredient
                recipeIngredientDao.insertRecipeIngredient(recipeIngredient)
                
                // Reload recipe ingredients to update UI
                loadRecipeIngredients(savedRecipeId)
                
                // Clear validation errors on success
                _uiState.value = _uiState.value.copy(validation = emptyList())
                
            } catch (e: Exception) {
                // Better error handling with more specific messages
                val errorMessage = when {
                    e.message?.contains("FOREIGN KEY") == true -> 
                        "Database error: Recipe must be saved first. Please try again."
                    e.message?.contains("UNIQUE constraint") == true ->
                        "This ingredient is already in the recipe."
                    else -> "Error adding ingredient: ${e.message}"
                }
                
                _uiState.value = _uiState.value.copy(
                    validation = listOf(errorMessage)
                )
                
                // Log the full error for debugging
                println("RecipeBuilderViewModel: Error adding ingredient ${ingredient.name}: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun removeIngredient(ingredientId: Int) {
        viewModelScope.launch {
            try {
                recipeIngredientDao.deleteRecipeIngredientById(ingredientId)
                // Reload recipe ingredients
                if (_uiState.value.isEditing) {
                    loadRecipeIngredients(_uiState.value.recipe.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    validation = _uiState.value.validation + "Error removing ingredient: ${e.message}"
                )
            }
        }
    }
    
    fun updateIngredient(updatedIngredient: RecipeIngredient) {
        viewModelScope.launch {
            try {
                recipeIngredientDao.updateRecipeIngredient(updatedIngredient)
                // Reload recipe ingredients
                if (_uiState.value.isEditing) {
                    loadRecipeIngredients(_uiState.value.recipe.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    validation = _uiState.value.validation + "Error updating ingredient: ${e.message}"
                )
            }
        }
    }
    
    private fun calculateRecipe() {
        // Only calculate if we have ingredients
        val ingredients = _uiState.value.recipeIngredients
        if (ingredients.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                calculations = null,
                isCalculating = false
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true)
            
            try {
                // Basic calculation (you'll want to make this more sophisticated)
                // Calculate based on ingredient types and quantities
                var totalGravityPoints = 0.0
                val batchSize = _uiState.value.selectedBatchSize
                
                ingredients.forEach { ingredientWithDetails ->
                    val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
                    val ingredient = ingredientWithDetails.ingredient
                    
                    when (ingredient.type) {
                        IngredientType.GRAIN -> {
                            // Use PPG if available, otherwise estimate based on color
                            val ppg = ingredient.ppgExtract ?: (35.0 - (ingredient.colorLovibond ?: 0.0) * 0.1)
                            totalGravityPoints += (scaledQuantity * ppg * 0.75) // 75% brewhouse efficiency
                        }
                        IngredientType.FRUIT -> {
                            // Honey and fruit sugars - rough estimate
                            val sugarContent = when {
                                ingredient.name.contains("honey", ignoreCase = true) -> 0.75
                                ingredient.name.contains("juice", ignoreCase = true) -> 0.15
                                else -> 0.12 // Default fruit sugar content
                            }
                            totalGravityPoints += scaledQuantity * sugarContent * 46.0
                        }
                        IngredientType.ADJUNCT -> {
                            // Sugars and adjuncts
                            val ppg = ingredient.ppgExtract ?: 46.0 // Default for sugars
                            totalGravityPoints += scaledQuantity * ppg
                        }
                        else -> {
                            // Other ingredients don't contribute significantly to gravity
                        }
                    }
                }
                
                // Calculate gravity for 1 gallon base
                val estimatedOG = 1.000 + (totalGravityPoints / 1000.0)
                val estimatedFG = estimatedOG - ((estimatedOG - 1.0) * 0.75) // Assuming 75% attenuation
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
                println("RecipeBuilderViewModel: Inventory check error: ${e.message}")
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
                
                if (_uiState.value.isEditing) {
                    recipeDao.updateRecipe(recipe)
                } else {
                    recipeDao.insertRecipe(recipe)
                    _uiState.value = _uiState.value.copy(isEditing = true)
                }
                
                _uiState.value = _uiState.value.copy(
                    saveResult = Result.success(Unit),
                    validation = emptyList()
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
            try {
                // Ensure recipe is saved first
                if (!_uiState.value.isEditing) {
                    saveRecipe()
                }
                
                // TODO: Implement project creation from recipe
                // This would create a new Project entity based on the current recipe
                _uiState.value = _uiState.value.copy(
                    validation = listOf("Project creation from recipe - feature coming soon!")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    validation = _uiState.value.validation + "Error creating project: ${e.message}"
                )
            }
        }
    }
    
    fun clearSaveResult() {
        _uiState.value = _uiState.value.copy(saveResult = null)
    }
    
    // Clear validation errors
    fun clearValidation() {
        _uiState.value = _uiState.value.copy(validation = emptyList())
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
