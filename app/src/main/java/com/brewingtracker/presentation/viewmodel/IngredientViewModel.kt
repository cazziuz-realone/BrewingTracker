package com.brewingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType
import com.brewingtracker.data.database.entities.Yeast
import com.brewingtracker.data.database.entities.YeastType
import com.brewingtracker.data.database.dao.IngredientDao
import com.brewingtracker.data.database.dao.YeastDao
import javax.inject.Inject

@HiltViewModel
class IngredientViewModel @Inject constructor(
    private val ingredientDao: IngredientDao,
    private val yeastDao: YeastDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<UniversalCategory?>(null)
    val selectedCategory: StateFlow<UniversalCategory?> = _selectedCategory.asStateFlow()

    private val _selectedYeastType = MutableStateFlow<YeastType?>(null)
    val selectedYeastType: StateFlow<YeastType?> = _selectedYeastType.asStateFlow()

    private val _uiState = MutableStateFlow(IngredientUiState())
    val uiState: StateFlow<IngredientUiState> = _uiState.asStateFlow()

    // All ingredients and yeasts
    val allIngredients = ingredientDao.getAllIngredients()
    val allYeasts = yeastDao.getAllYeasts()

    // Filtered ingredients based on search and category
    val filteredIngredients = combine(
        allIngredients,
        searchQuery,
        selectedCategory
    ) { ingredients, query, category ->
        ingredients.filter { ingredient ->
            val matchesSearch = if (query.isBlank()) true
            else ingredient.name.contains(query, ignoreCase = true) ||
                    ingredient.category?.contains(query, ignoreCase = true) == true ||
                    ingredient.flavorProfile?.contains(query, ignoreCase = true) == true

            val matchesCategory = category == null ||
                    category.ingredientTypes.contains(ingredient.type)

            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered yeasts
    val filteredYeasts = combine(
        allYeasts,
        searchQuery,
        selectedYeastType
    ) { yeasts, query, type ->
        yeasts.filter { yeast ->
            val matchesSearch = if (query.isBlank()) true
            else yeast.name.contains(query, ignoreCase = true) ||
                    yeast.brand.contains(query, ignoreCase = true) ||
                    yeast.description?.contains(query, ignoreCase = true) == true

            val matchesType = type == null || yeast.type == type

            matchesSearch && matchesType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: UniversalCategory?) {
        _selectedCategory.value = category
    }

    fun updateSelectedYeastType(type: YeastType?) {
        _selectedYeastType.value = type
    }

    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                ingredientDao.insertIngredient(ingredient)
                _uiState.value = _uiState.value.copy(
                    showSuccess = true,
                    successMessage = "Ingredient '${ingredient.name}' added successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showError = true,
                    errorMessage = "Failed to add ingredient: ${e.message}"
                )
            }
        }
    }

    fun updateIngredientStock(ingredientId: String, newStock: Double) {
        viewModelScope.launch {
            try {
                val ingredients = allIngredients.first()
                val ingredient = ingredients.find { it.id == ingredientId }
                ingredient?.let {
                    val updated = it.copy(
                        currentStock = newStock,
                        updatedAt = System.currentTimeMillis()
                    )
                    ingredientDao.updateIngredient(updated)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showError = true,
                    errorMessage = "Failed to update stock: ${e.message}"
                )
            }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            try {
                // Add sample ingredients
                val sampleIngredients = getSampleIngredients()
                ingredientDao.insertIngredients(sampleIngredients)

                // Add sample yeasts
                val sampleYeasts = getSampleYeasts()
                sampleYeasts.forEach { yeast ->
                    yeastDao.insertYeast(yeast)
                }

                _uiState.value = _uiState.value.copy(
                    showSuccess = true,
                    successMessage = "Sample ingredients and yeasts added!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showError = true,
                    errorMessage = "Failed to seed data: ${e.message}"
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

    private fun getSampleIngredients() = listOf(
        // Grains & Malts (for beer makers)
        Ingredient(
            name = "Pilsner Malt",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            description = "Light, clean base malt perfect for lagers and light ales",
            colorLovibond = 1.7,
            ppgExtract = 37.0,
            currentStock = 10.0,
            unit = "lbs",
            costPerUnit = 1.50
        ),
        Ingredient(
            name = "Munich Malt",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            description = "Rich, malty flavor with amber color",
            colorLovibond = 9.0,
            ppgExtract = 37.0,
            currentStock = 5.0,
            unit = "lbs",
            costPerUnit = 1.60
        ),
        Ingredient(
            name = "Crystal 60L",
            type = IngredientType.GRAIN,
            category = "Crystal Malt",
            description = "Medium crystal malt adding sweetness and amber color",
            colorLovibond = 60.0,
            ppgExtract = 34.0,
            currentStock = 3.0,
            unit = "lbs",
            costPerUnit = 1.80
        ),

        // Hops (for beer makers)
        Ingredient(
            name = "Cascade",
            type = IngredientType.HOP,
            category = "Aroma Hop",
            description = "Classic American hop with citrus and floral notes",
            alphaAcidPercentage = 5.5,
            currentStock = 4.0,
            unit = "oz",
            costPerUnit = 2.50,
            flavorProfile = "Citrus, Floral, Grapefruit"
        ),
        Ingredient(
            name = "Centennial",
            type = IngredientType.HOP,
            category = "Dual Purpose",
            description = "Super Cascade with intense citrus character",
            alphaAcidPercentage = 10.0,
            currentStock = 3.0,
            unit = "oz",
            costPerUnit = 2.75,
            flavorProfile = "Citrus, Pine, Lemon"
        ),

        // Fruits & Juices (for wine, cider, mead makers)
        Ingredient(
            name = "Sweet Cherries",
            type = IngredientType.FRUIT,
            category = "Stone Fruit",
            description = "Fresh sweet cherries perfect for meads and fruit wines",
            currentStock = 5.0,
            unit = "lbs",
            costPerUnit = 4.50,
            flavorProfile = "Sweet, Tart, Rich"
        ),
        Ingredient(
            name = "Blueberries",
            type = IngredientType.FRUIT,
            category = "Berry",
            description = "Fresh blueberries for complex fruit character",
            currentStock = 3.0,
            unit = "lbs",
            costPerUnit = 5.00,
            flavorProfile = "Sweet, Tart, Antioxidant"
        ),
        Ingredient(
            name = "Granny Smith Apples",
            type = IngredientType.FRUIT,
            category = "Apple",
            description = "Tart apples perfect for traditional ciders",
            currentStock = 10.0,
            unit = "lbs",
            costPerUnit = 2.50,
            flavorProfile = "Tart, Crisp, Fresh"
        ),
        Ingredient(
            name = "Concord Grapes",
            type = IngredientType.FRUIT,
            category = "Grape",
            description = "Classic American grapes for deep, rich wines",
            currentStock = 8.0,
            unit = "lbs",
            costPerUnit = 3.00,
            flavorProfile = "Sweet, Intense, Fruity"
        ),

        // Honey & Sugars (for mead makers, sugar additions)
        Ingredient(
            name = "Wildflower Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            description = "Light, floral honey perfect for traditional meads",
            ppgExtract = 35.0,
            currentStock = 6.0,
            unit = "lbs",
            costPerUnit = 8.00,
            flavorProfile = "Floral, Light, Sweet"
        ),
        Ingredient(
            name = "Orange Blossom Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            description = "Delicate citrus notes from orange blossoms",
            ppgExtract = 35.0,
            currentStock = 4.0,
            unit = "lbs",
            costPerUnit = 9.50,
            flavorProfile = "Citrus, Floral, Delicate"
        ),
        Ingredient(
            name = "Clover Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            description = "Mild, versatile honey great for beginners",
            ppgExtract = 35.0,
            currentStock = 5.0,
            unit = "lbs",
            costPerUnit = 7.50,
            flavorProfile = "Mild, Clean, Sweet"
        ),
        Ingredient(
            name = "Corn Sugar (Dextrose)",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            description = "Pure dextrose for priming and boosting alcohol",
            ppgExtract = 40.0,
            currentStock = 2.0,
            unit = "lbs",
            costPerUnit = 3.00,
            flavorProfile = "Neutral, Clean"
        ),

        // Spices & Flavorings (for everyone)
        Ingredient(
            name = "Vanilla Beans",
            type = IngredientType.SPICE,
            category = "Spice",
            description = "Madagascar vanilla beans for rich, complex flavor",
            currentStock = 10.0,
            unit = "beans",
            costPerUnit = 1.50,
            flavorProfile = "Sweet, Rich, Aromatic"
        ),
        Ingredient(
            name = "Cinnamon Sticks",
            type = IngredientType.SPICE,
            category = "Spice",
            description = "Ceylon cinnamon for warm, sweet spicing",
            currentStock = 20.0,
            unit = "sticks",
            costPerUnit = 0.25,
            flavorProfile = "Warm, Sweet, Aromatic"
        ),
        Ingredient(
            name = "Fresh Ginger",
            type = IngredientType.SPICE,
            category = "Spice",
            description = "Fresh ginger root for heat and complexity",
            currentStock = 0.5,
            unit = "lbs",
            costPerUnit = 4.00,
            flavorProfile = "Spicy, Warming, Pungent"
        ),

        // Other (acids, chemicals, etc.)
        Ingredient(
            name = "Acid Blend",
            type = IngredientType.OTHER,
            category = "Acid",
            description = "Tartaric, malic, and citric acid blend for pH adjustment",
            currentStock = 1.0,
            unit = "lbs",
            costPerUnit = 12.00,
            flavorProfile = "Tart, Sharp"
        ),
        Ingredient(
            name = "Potassium Metabisulfite",
            type = IngredientType.OTHER,
            category = "Preservative",
            description = "Wine and mead preservative and antioxidant",
            currentStock = 0.5,
            unit = "lbs",
            costPerUnit = 8.00,
            flavorProfile = "None"
        )
    )

    private fun getSampleYeasts() = listOf(
        // Ale Yeasts
        Yeast(
            name = "SafAle US-05",
            brand = "Fermentis",
            type = YeastType.ALE,
            form = com.brewingtracker.data.database.entities.YeastForm.DRY,
            attenuationRange = "78-82%",
            temperatureRange = "59-75°F",
            alcoholTolerance = 12.0,
            description = "American ale yeast producing well balanced beers with low diacetyl",
            currentStock = 5,
            unit = "packets",
            costPerUnit = 3.99
        ),

        // Lager Yeasts
        Yeast(
            name = "SafLager W-34/70",
            brand = "Fermentis",
            type = YeastType.LAGER,
            form = com.brewingtracker.data.database.entities.YeastForm.DRY,
            attenuationRange = "81-85%",
            temperatureRange = "46-59°F",
            alcoholTolerance = 10.0,
            description = "German lager yeast strain used to produce well-balanced beers",
            currentStock = 4,
            unit = "packets",
            costPerUnit = 4.49
        ),

        // Wine/Mead Yeasts
        Yeast(
            name = "EC-1118",
            brand = "Lallemand",
            type = YeastType.CHAMPAGNE,
            form = com.brewingtracker.data.database.entities.YeastForm.DRY,
            attenuationRange = "95-100%",
            temperatureRange = "50-86°F",
            alcoholTolerance = 18.0,
            description = "Champagne yeast with excellent alcohol tolerance, ideal for high gravity wines and meads",
            currentStock = 6,
            unit = "packets",
            costPerUnit = 2.99
        ),

        Yeast(
            name = "D47",
            brand = "Lallemand",
            type = YeastType.WINE,
            form = com.brewingtracker.data.database.entities.YeastForm.DRY,
            attenuationRange = "85-95%",
            temperatureRange = "59-68°F",
            alcoholTolerance = 15.0,
            description = "Low foaming with complex aroma, perfect for meads and white wines",
            currentStock = 4,
            unit = "packets",
            costPerUnit = 3.49
        ),

        Yeast(
            name = "71B-1122",
            brand = "Lallemand",
            type = YeastType.WINE,
            form = com.brewingtracker.data.database.entities.YeastForm.DRY,
            attenuationRange = "80-90%",
            temperatureRange = "59-86°F",
            alcoholTolerance = 14.0,
            description = "Ideal for young wines, reduces malic acid and enhances fruit flavors",
            currentStock = 3,
            unit = "packets",
            costPerUnit = 3.49
        )
    )
}

// Universal categories that work for all beverage types
enum class UniversalCategory(
    val displayName: String,
    val description: String,
    val ingredientTypes: List<IngredientType>,
    val primaryBeverages: List<String>
) {
    GRAINS_MALTS(
        displayName = "Grains & Malts",
        description = "Base malts, specialty grains for beer brewing",
        ingredientTypes = listOf(IngredientType.GRAIN),
        primaryBeverages = listOf("Beer", "Sake")
    ),
    HOPS(
        displayName = "Hops",
        description = "Bittering and aroma hops for beer brewing",
        ingredientTypes = listOf(IngredientType.HOP),
        primaryBeverages = listOf("Beer")
    ),
    FRUITS_JUICES(
        displayName = "Fruits & Juices",
        description = "Fresh fruits, juices for wine, cider, and fruit meads",
        ingredientTypes = listOf(IngredientType.FRUIT),
        primaryBeverages = listOf("Wine", "Cider", "Mead", "Fruit Beer")
    ),
    HONEY_SUGARS(
        displayName = "Honey & Sugars",
        description = "Honey varieties, sugars for mead and boosting alcohol",
        ingredientTypes = listOf(IngredientType.ADJUNCT),
        primaryBeverages = listOf("Mead", "Wine", "Cider", "Beer")
    ),
    SPICES_FLAVORINGS(
        displayName = "Spices & Flavorings",
        description = "Spices, herbs, flavorings for all beverage types",
        ingredientTypes = listOf(IngredientType.SPICE),
        primaryBeverages = listOf("All Beverages")
    ),
    OTHER(
        displayName = "Other",
        description = "Acids, chemicals, preservatives, misc ingredients",
        ingredientTypes = listOf(IngredientType.OTHER),
        primaryBeverages = listOf("All Beverages")
    )
}

data class IngredientUiState(
    val isLoading: Boolean = false,
    val showSuccess: Boolean = false,
    val showError: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)