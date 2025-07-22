package com.brewingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType
import com.brewingtracker.data.database.entities.ProjectIngredient
import com.brewingtracker.data.repository.BrewingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IngredientsViewModel @Inject constructor(
    private val repository: BrewingRepository
) : ViewModel() {

    private val _selectedIngredientType = MutableStateFlow<IngredientType?>(null)
    val selectedIngredientType = _selectedIngredientType.asStateFlow()

    private val _selectedBeverageType = MutableStateFlow<String?>(null)
    val selectedBeverageType = _selectedBeverageType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _showInStockOnly = MutableStateFlow(false)
    val showInStockOnly = _showInStockOnly.asStateFlow()

    val allIngredients = repository.getAllIngredients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredIngredients = combine(
        allIngredients,
        selectedIngredientType,
        selectedBeverageType,
        searchQuery,
        showInStockOnly
    ) { ingredients, type, beverageType, query, inStockOnly ->
        ingredients.filter { ingredient ->
            val matchesType = type == null || ingredient.type == type
            val matchesBeverage = beverageType == null || 
                ingredient.beverageTypes.contains(beverageType, ignoreCase = true)
            val matchesQuery = query.isBlank() || 
                ingredient.name.contains(query, ignoreCase = true)
            val matchesStock = !inStockOnly || ingredient.currentStock > 0
            
            matchesType && matchesBeverage && matchesQuery && matchesStock
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val inStockIngredients = repository.getInStockIngredients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun filterByType(type: IngredientType?) {
        _selectedIngredientType.value = type
    }

    fun filterByBeverageType(beverageType: String?) {
        _selectedBeverageType.value = beverageType
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleShowInStockOnly() {
        _showInStockOnly.value = !_showInStockOnly.value
    }

    fun updateIngredientStock(ingredientId: Int, newStock: Double) {
        viewModelScope.launch {
            repository.updateIngredientStock(ingredientId, newStock)
        }
    }

    suspend fun getIngredientById(id: Int): Ingredient? {
        return repository.getIngredientById(id)
    }

    fun clearFilters() {
        _selectedIngredientType.value = null
        _selectedBeverageType.value = null
        _searchQuery.value = ""
        _showInStockOnly.value = false
    }

    // ==========================================
    // PROJECT INGREDIENT OPERATIONS - NEW
    // ==========================================
    
    /**
     * Add selected ingredients to a project
     * @param projectId The ID of the project to add ingredients to
     * @param ingredientIds Set of ingredient IDs to add
     * @param defaultQuantity Default quantity for each ingredient (can be edited later)
     * @param defaultUnit Default unit (lbs, oz, etc.)
     */
    fun addIngredientsToProject(
        projectId: String,
        ingredientIds: Set<Int>,
        defaultQuantity: Double = 0.0,
        defaultUnit: String = "lbs"
    ) {
        viewModelScope.launch {
            ingredientIds.forEach { ingredientId ->
                val projectIngredient = ProjectIngredient(
                    projectId = projectId,
                    ingredientId = ingredientId,
                    quantity = defaultQuantity,
                    unit = defaultUnit,
                    additionTime = null, // Can be set later in project detail
                    notes = null,
                    createdAt = System.currentTimeMillis()
                )
                repository.addIngredientToProject(projectIngredient)
            }
        }
    }

    /**
     * Add a single ingredient to a project
     */
    fun addIngredientToProject(projectIngredient: ProjectIngredient) {
        viewModelScope.launch {
            repository.addIngredientToProject(projectIngredient)
        }
    }

    // Helper function to get ingredient types for filtering
    fun getIngredientTypes(): List<IngredientType> {
        return IngredientType.values().toList()
    }

    // Helper function to get beverage types
    fun getBeverageTypes(): List<String> {
        return listOf("beer", "mead", "wine", "cider", "kombucha")
    }
}