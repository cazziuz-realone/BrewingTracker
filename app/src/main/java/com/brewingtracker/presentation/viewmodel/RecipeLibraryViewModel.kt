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
        
        if (query.isBlank()) {
            loadRecipes()
            return
        }
        
        viewModelScope.launch {
            try {
                recipeDao.searchRecipes(query)
                    .collect { recipes ->
                        _uiState.value = _uiState.value.copy(
                            recipes = recipes,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error searching recipes: ${e.message}"
                )
            }
        }
    }
    
    fun filterByBeverageType(beverageType: BeverageType?) {
        _uiState.value = _uiState.value.copy(selectedBeverageType = beverageType)
        
        viewModelScope.launch {
            try {
                val recipes = if (beverageType == null) {
                    recipeDao.getAllRecipes()
                } else {
                    recipeDao.getRecipesByBeverageType(beverageType)
                }
                
                recipes.collect { recipeList ->
                    _uiState.value = _uiState.value.copy(
                        recipes = recipeList,
                        isLoading = false
                    )
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
                    // Create a new recipe with "Copy of" prefix
                    val duplicatedRecipe = originalRecipe.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Copy of ${originalRecipe.name}",
                        timesUsed = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    // Insert the duplicated recipe
                    recipeDao.insertRecipe(duplicatedRecipe)
                    
                    // Duplicate the recipe ingredients
                    recipeIngredientDao.duplicateRecipeIngredients(recipeId, duplicatedRecipe.id)
                    
                    _uiState.value = _uiState.value.copy(
                        message = "Recipe duplicated successfully!"
                    )
                    
                    // Clear message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(message = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error duplicating recipe: ${e.message}"
                )
            }
        }
    }
    
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                // Delete recipe ingredients first (cascade should handle this, but being explicit)
                recipeIngredientDao.deleteAllRecipeIngredients(recipeId)
                
                // Delete the recipe
                recipeDao.deleteRecipe(recipeId)
                
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
    
    fun markRecipeAsUsed(recipeId: String) {
        viewModelScope.launch {
            try {
                val recipe = recipeDao.getRecipeById(recipeId)
                if (recipe != null) {
                    val updatedRecipe = recipe.copy(
                        timesUsed = recipe.timesUsed + 1,
                        updatedAt = System.currentTimeMillis()
                    )
                    recipeDao.updateRecipe(updatedRecipe)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error updating recipe usage: ${e.message}"
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
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error toggling favorite: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class RecipeLibraryUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedBeverageType: BeverageType? = null,
    val error: String? = null,
    val message: String? = null
)
