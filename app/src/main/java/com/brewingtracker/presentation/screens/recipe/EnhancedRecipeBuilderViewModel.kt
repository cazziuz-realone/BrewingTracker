package com.brewingtracker.presentation.screens.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewingtracker.data.database.entities.*
import com.brewingtracker.data.repository.BrewingRepository
import com.brewingtracker.data.services.RecipeCalculationService
import com.brewingtracker.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnhancedRecipeBuilderViewModel @Inject constructor(
    private val repository: BrewingRepository,
    private val calculationService: RecipeCalculationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeBuilderUiState())
    val uiState = _uiState.asStateFlow()

    private var calculationJob: Job? = null
    private var currentRecipeId: String? = null

    init {
        // Load initial data
        loadIngredients()
        loadYeasts()
    }

    fun loadRecipe(recipeId: String) {
        currentRecipeId = recipeId
        viewModelScope.launch {
            try {
                val recipe = repository.getRecipeById(recipeId)
                recipe?.let {
                    _uiState.value = _uiState.value.copy(
                        recipe = it,
                        isLoading = false
                    )
                    loadRecipeIngredients(recipeId)
                    loadRecipeSteps(recipeId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load recipe: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun createNewRecipe(beverageType: BeverageType) {
        val newRecipe = Recipe(
            name = "New ${beverageType.name.lowercase().replaceFirstChar { it.uppercase() }} Recipe",
            beverageType = beverageType,
            description = "A delicious ${beverageType.name.lowercase()} recipe"
        )
        
        _uiState.value = _uiState.value.copy(
            recipe = newRecipe,
            processSteps = calculationService.generateDefaultSteps(beverageType),
            isLoading = false
        )
        
        // Trigger initial calculations
        calculateRecipeParameters()
    }

    fun updateRecipe(updatedRecipe: Recipe) {
        _uiState.value = _uiState.value.copy(recipe = updatedRecipe)
        validateRecipe()
    }

    fun updateBatchSize(batchSize: BatchSize) {
        _uiState.value = _uiState.value.copy(selectedBatchSize = batchSize)
        calculateRecipeParameters()
        checkInventoryStatus()
    }

    fun selectCategory(category: IngredientType) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        searchIngredients("")
    }

    fun searchIngredients(query: String) {
        val currentState = _uiState.value
        viewModelScope.launch {
            try {
                val filteredIngredients = if (query.isBlank()) {
                    currentState.selectedCategory?.let { category ->
                        repository.getIngredientsByType(category).first()
                    } ?: repository.getAllIngredients().first()
                } else {
                    repository.searchIngredients(query, currentState.selectedCategory)
                }
                
                _uiState.value = _uiState.value.copy(
                    searchResults = filteredIngredients,
                    searchQuery = query
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }

    fun addIngredient(ingredient: Ingredient, quantity: Double, unit: String, timing: String) {
        val currentState = _uiState.value
        val newRecipeIngredient = RecipeIngredient(
            recipeId = currentState.recipe.id,
            ingredientId = ingredient.id,
            baseQuantity = quantity,
            baseUnit = unit,
            additionTiming = timing
        )

        val updatedIngredients = currentState.recipeIngredients + 
            RecipeIngredientWithDetails(newRecipeIngredient, ingredient)

        _uiState.value = _uiState.value.copy(
            recipeIngredients = updatedIngredients
        )

        calculateRecipeParameters()
        checkInventoryStatus()
        validateRecipe()
    }

    fun updateIngredient(updatedRecipeIngredient: RecipeIngredient) {
        val currentState = _uiState.value
        val updatedList = currentState.recipeIngredients.map { item ->
            if (item.recipeIngredient.id == updatedRecipeIngredient.id) {
                item.copy(recipeIngredient = updatedRecipeIngredient)
            } else {
                item
            }
        }

        _uiState.value = _uiState.value.copy(
            recipeIngredients = updatedList
        )

        calculateRecipeParameters()
        checkInventoryStatus()
        validateRecipe()
    }

    fun removeIngredient(recipeIngredientId: Int) {
        val currentState = _uiState.value
        val updatedList = currentState.recipeIngredients.filterNot { 
            it.recipeIngredient.id == recipeIngredientId 
        }

        _uiState.value = _uiState.value.copy(
            recipeIngredients = updatedList
        )

        calculateRecipeParameters()
        checkInventoryStatus()
        validateRecipe()
    }

    fun addStep() {
        val currentState = _uiState.value
        val nextStepNumber = currentState.processSteps.maxOfOrNull { it.stepNumber }?.plus(1) ?: 1
        
        val newStep = RecipeStep(
            recipeId = currentState.recipe.id,
            stepNumber = nextStepNumber,
            phase = "preparation",
            title = "New Step",
            description = "Step description"
        )

        _uiState.value = _uiState.value.copy(
            processSteps = currentState.processSteps + newStep
        )
    }

    fun updateStep(updatedStep: RecipeStep) {
        val currentState = _uiState.value
        val updatedSteps = currentState.processSteps.map { step ->
            if (step.id == updatedStep.id) updatedStep else step
        }

        _uiState.value = _uiState.value.copy(
            processSteps = updatedSteps
        )
    }

    fun removeStep(stepId: Int) {
        val currentState = _uiState.value
        val updatedSteps = currentState.processSteps
            .filterNot { it.id == stepId }
            .mapIndexed { index, step ->
                step.copy(stepNumber = index + 1)
            }

        _uiState.value = _uiState.value.copy(
            processSteps = updatedSteps
        )
    }

    fun saveRecipe() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val recipe = currentState.recipe.copy(
                    updatedAt = System.currentTimeMillis()
                )

                // Save recipe
                val savedRecipeId = if (currentRecipeId == null) {
                    repository.createRecipe(recipe)
                } else {
                    repository.updateRecipe(recipe)
                    recipe.id
                }

                // Save ingredients
                currentState.recipeIngredients.forEach { ingredientWithDetails ->
                    val recipeIngredient = ingredientWithDetails.recipeIngredient.copy(
                        recipeId = savedRecipeId
                    )
                    repository.insertRecipeIngredient(recipeIngredient)
                }

                // Save steps
                currentState.processSteps.forEach { step ->
                    val updatedStep = step.copy(recipeId = savedRecipeId)
                    repository.insertRecipeStep(updatedStep)
                }

                _uiState.value = _uiState.value.copy(
                    recipe = recipe.copy(id = savedRecipeId),
                    isSaved = true,
                    successMessage = "Recipe saved successfully!"
                )

                currentRecipeId = savedRecipeId

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save recipe: ${e.message}"
                )
            }
        }
    }

    fun createProjectFromRecipe(batchSize: BatchSize) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                
                // First save the recipe if it's not saved
                if (!currentState.isSaved) {
                    saveRecipe()
                }

                // Create project from recipe
                val project = Project(
                    name = "${currentState.recipe.name} - ${batchSize.displayName}",
                    description = "Brewing project from recipe: ${currentState.recipe.name}",
                    beverageType = currentState.recipe.beverageType,
                    targetOG = currentState.calculations.estimatedOG,
                    targetFG = currentState.calculations.estimatedFG,
                    targetABV = currentState.calculations.estimatedABV,
                    batchSizeOz = batchSize.ozValue,
                    recipeId = currentState.recipe.id
                )

                val projectId = repository.createProject(project)

                // Scale and add ingredients to project
                currentState.recipeIngredients.forEach { ingredientWithDetails ->
                    val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
                    
                    val projectIngredient = ProjectIngredient(
                        projectId = projectId,
                        ingredientId = ingredientWithDetails.ingredient.id,
                        plannedQuantity = scaledQuantity,
                        unit = ingredientWithDetails.recipeIngredient.baseUnit,
                        additionTiming = ingredientWithDetails.recipeIngredient.additionTiming
                    )
                    
                    repository.addIngredientToProject(projectIngredient)
                }

                _uiState.value = _uiState.value.copy(
                    projectCreated = true,
                    createdProjectId = projectId,
                    successMessage = "Project created successfully! Ready to start brewing."
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create project: ${e.message}"
                )
            }
        }
    }

    private fun calculateRecipeParameters() {
        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    calculations = _uiState.value.calculations.copy(isCalculating = true)
                )

                // Small delay to debounce rapid changes
                delay(500)

                val currentState = _uiState.value
                val calculations = calculationService.calculateRecipeParameters(
                    currentState.recipeIngredients,
                    currentState.selectedBatchSize
                )

                _uiState.value = _uiState.value.copy(
                    calculations = LiveRecipeCalculations(
                        estimatedOG = calculations.estimatedOG,
                        estimatedFG = calculations.estimatedFG,
                        estimatedABV = calculations.estimatedABV,
                        batchSize = calculations.batchSize,
                        isCalculating = false
                    )
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    calculations = _uiState.value.calculations.copy(
                        isCalculating = false,
                        hasError = true,
                        errorMessage = e.message
                    )
                )
            }
        }
    }

    private fun checkInventoryStatus() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val inventoryStatus = calculationService.checkInventoryStatus(
                    currentState.recipeIngredients,
                    currentState.selectedBatchSize
                )

                _uiState.value = _uiState.value.copy(
                    inventoryStatus = inventoryStatus
                )

            } catch (e: Exception) {
                // Inventory check failure shouldn't block the UI
                _uiState.value = _uiState.value.copy(
                    inventoryStatus = emptyMap()
                )
            }
        }
    }

    private fun validateRecipe() {
        val currentState = _uiState.value
        val validationResults = mutableMapOf<String, String>()

        // Validate recipe name
        if (currentState.recipe.name.isBlank()) {
            validationResults["Recipe Name"] = "Recipe name is required"
        }

        // Validate ingredients
        if (currentState.recipeIngredients.isEmpty()) {
            validationResults["Ingredients"] = "At least one ingredient is required"
        }

        // Check for honey/sugar in mead recipes
        if (currentState.recipe.beverageType == BeverageType.MEAD) {
            val hasHoney = currentState.recipeIngredients.any { 
                it.ingredient.name.lowercase().contains("honey") ||
                it.ingredient.type == IngredientType.FRUIT
            }
            if (!hasHoney) {
                validationResults["Mead Base"] = "Mead recipes require honey or fermentable sugars"
            }
        }

        // Validate calculations
        if (currentState.calculations.estimatedOG < 1.000) {
            validationResults["Original Gravity"] = "Invalid OG calculation - check ingredients"
        }

        _uiState.value = _uiState.value.copy(
            validation = validationResults
        )
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            try {
                repository.getAllIngredients().collect { ingredients ->
                    _uiState.value = _uiState.value.copy(
                        availableIngredients = ingredients
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load ingredients: ${e.message}"
                )
            }
        }
    }

    private fun loadYeasts() {
        viewModelScope.launch {
            try {
                repository.getAllYeasts().collect { yeasts ->
                    _uiState.value = _uiState.value.copy(
                        availableYeasts = yeasts
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load yeasts: ${e.message}"
                )
            }
        }
    }

    private fun loadRecipeIngredients(recipeId: String) {
        viewModelScope.launch {
            try {
                repository.getRecipeIngredientsWithDetails(recipeId).collect { ingredients ->
                    _uiState.value = _uiState.value.copy(
                        recipeIngredients = ingredients
                    )
                    calculateRecipeParameters()
                    checkInventoryStatus()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load recipe ingredients: ${e.message}"
                )
            }
        }
    }

    private fun loadRecipeSteps(recipeId: String) {
        viewModelScope.launch {
            try {
                repository.getRecipeSteps(recipeId).collect { steps ->
                    _uiState.value = _uiState.value.copy(
                        processSteps = steps
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load recipe steps: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class RecipeBuilderUiState(
    val recipe: Recipe = Recipe(
        name = "",
        beverageType = BeverageType.MEAD,
        description = ""
    ),
    val selectedBatchSize: BatchSize = BatchSize.GALLON,
    val selectedCategory: IngredientType? = null,
    val searchQuery: String = "",
    val searchResults: List<Ingredient> = emptyList(),
    val availableIngredients: List<Ingredient> = emptyList(),
    val availableYeasts: List<Yeast> = emptyList(),
    val recipeIngredients: List<RecipeIngredientWithDetails> = emptyList(),
    val processSteps: List<RecipeStep> = emptyList(),
    val calculations: LiveRecipeCalculations = LiveRecipeCalculations(),
    val inventoryStatus: Map<Int, InventoryStatus> = emptyMap(),
    val validation: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val projectCreated: Boolean = false,
    val createdProjectId: String? = null,
    val error: String? = null,
    val successMessage: String? = null
)
