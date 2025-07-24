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
import java.util.UUID

@HiltViewModel
class RecipeLibraryViewModel @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecipeLibraryUiState())
    val uiState: StateFlow<RecipeLibraryUiState> = _uiState.asStateFlow()
    
    init {
        loadRecipes()
    }
    
    private fun loadRecipes() {
        viewModelScope.launch {
            try {
                recipeDao.getAllRecipes()
                    .collect { recipes ->
                        _uiState.value = _uiState.value.copy(
                            recipes = recipes,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading recipes: ${e.message}"
                )
            }
        }
    }
    
    fun searchRecipes(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    // Load all recipes if query is empty
                    loadRecipes()
                } else {
                    recipeDao.searchRecipesByName(query)
                        .collect { recipes ->
                            _uiState.value = _uiState.value.copy(
                                recipes = recipes,
                                isLoading = false
                            )
                        }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error searching recipes: ${e.message}"
                )
            }
        }
    }
    
    fun filterRecipesByType(beverageType: BeverageType?) {
        _uiState.value = _uiState.value.copy(selectedBeverageType = beverageType)
        
        viewModelScope.launch {
            try {
                if (beverageType == null) {
                    // Load all recipes if no filter
                    loadRecipes()
                } else {
                    recipeDao.getRecipesByBeverageType(beverageType)
                        .collect { recipes ->
                            _uiState.value = _uiState.value.copy(
                                recipes = recipes,
                                isLoading = false
                            )
                        }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error filtering recipes: ${e.message}"
                )
            }
        }
    }
    
    fun duplicateRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val originalRecipe = recipeDao.getRecipeById(recipeId)
                if (originalRecipe != null) {
                    // Create new recipe with duplicated data
                    val duplicatedRecipe = originalRecipe.copy(
                        id = UUID.randomUUID().toString(),
                        name = "${originalRecipe.name} (Copy)",
                        timesUsed = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    // Insert the duplicated recipe
                    recipeDao.insertRecipe(duplicatedRecipe)
                    
                    // Get original recipe ingredients
                    val originalIngredients = recipeIngredientDao.getRecipeIngredientsSync(recipeId)
                    
                    // Duplicate ingredients for new recipe
                    originalIngredients.forEach { originalIngredient ->
                        val duplicatedIngredient = originalIngredient.copy(
                            id = 0, // Auto-generate new ID
                            recipeId = duplicatedRecipe.id,
                            createdAt = System.currentTimeMillis()
                        )
                        recipeIngredientDao.insertRecipeIngredient(duplicatedIngredient)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        message = "Recipe duplicated successfully!"
                    )
                    
                    // Clear message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(message = null)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Recipe not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error duplicating recipe: ${e.message}"
                )
            }
        }
    }
    
    fun createProjectFromRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val recipe = recipeDao.getRecipeById(recipeId)
                if (recipe != null) {
                    // TODO: Implement project creation from recipe
                    // This would create a new Project entity based on the recipe
                    
                    // For now, just show a message
                    _uiState.value = _uiState.value.copy(
                        message = "Project creation from recipe - feature coming soon!"
                    )
                    
                    // Increment times used counter
                    val updatedRecipe = recipe.copy(
                        timesUsed = recipe.timesUsed + 1,
                        updatedAt = System.currentTimeMillis()
                    )
                    recipeDao.updateRecipe(updatedRecipe)
                    
                    // Clear message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(message = null)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Recipe not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error creating project: ${e.message}"
                )
            }
        }
    }
    
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                // First delete all recipe ingredients
                recipeIngredientDao.deleteRecipeIngredientsByRecipeId(recipeId)
                
                // Then delete the recipe
                recipeDao.deleteRecipeById(recipeId)
                
                _uiState.value = _uiState.value.copy(
                    message = "Recipe deleted successfully!"
                )
                
                // Clear message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(message = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error deleting recipe: ${e.message}"
                )
            }
        }
    }
    
    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            try {
                val recipe = recipeDao.getRecipeById(recipeId)
                if (recipe != null) {
                    val updatedRecipe = recipe.copy(
                        isFavorite = !recipe.isFavorite,
                        updatedAt = System.currentTimeMillis()
                    )
                    recipeDao.updateRecipe(updatedRecipe)
                    
                    _uiState.value = _uiState.value.copy(
                        message = if (updatedRecipe.isFavorite) "Added to favorites" else "Removed from favorites"
                    )
                    
                    // Clear message after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = _uiState.value.copy(message = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error toggling favorite: ${e.message}"
                )
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class RecipeLibraryUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedBeverageType: BeverageType? = null,
    val message: String? = null,
    val error: String? = null
)