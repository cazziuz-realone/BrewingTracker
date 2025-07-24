package com.brewingtracker.presentation.screens.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.brewingtracker.data.database.entities.*
import com.brewingtracker.presentation.screens.recipe.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBuilderScreen(
    recipeId: String? = null, // null for new recipe, ID for editing
    navController: NavController,
    viewModel: RecipeBuilderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize recipe if editing
    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.loadRecipe(recipeId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (recipeId != null) "Edit Recipe" else "New Recipe",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save button
                    IconButton(
                        onClick = { viewModel.saveRecipe() },
                        enabled = uiState.canSave
                    ) {
                        Icon(
                            Icons.Default.Save, 
                            contentDescription = "Save Recipe",
                            tint = if (uiState.canSave) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Recipe Info Card
            item {
                RecipeInfoCard(
                    recipe = uiState.recipe,
                    onRecipeUpdate = viewModel::updateRecipe
                )
            }
            
            // Batch Size Selector Card
            item {
                BatchSizeCard(
                    currentSize = uiState.selectedBatchSize,
                    onSizeChange = viewModel::updateBatchSize
                )
            }
            
            // Calculations Card
            item {
                CalculationsCard(
                    calculations = uiState.calculations,
                    isCalculating = uiState.isCalculating,
                    batchSize = uiState.selectedBatchSize
                )
            }
            
            // Selected Ingredients Card
            item {
                SelectedIngredientsCard(
                    ingredients = uiState.recipeIngredients,
                    inventoryStatus = uiState.inventoryStatus,
                    batchSize = uiState.selectedBatchSize,
                    onIngredientEdit = { /* TODO: Show edit dialog */ },
                    onIngredientRemove = viewModel::removeIngredient
                )
            }
            
            // Ingredient Categories Card
            item {
                IngredientCategoriesCard(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelect = viewModel::selectCategory
                )
            }
            
            // Ingredient Search & Add Card
            item {
                IngredientSearchCard(
                    category = uiState.selectedCategory,
                    searchQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    onSearchChange = viewModel::updateSearchQuery,
                    onAddIngredient = viewModel::addIngredient
                )
            }
            
            // Validation Card (if there are warnings/errors)
            if (uiState.validation.isNotEmpty()) {
                item {
                    ValidationCard(
                        validationResults = uiState.validation,
                        onCreateProject = { 
                            viewModel.createProjectFromRecipe()
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
    
    // Handle save result
    LaunchedEffect(uiState.saveResult) {
        uiState.saveResult?.let { result ->
            if (result.isSuccess) {
                navController.navigateUp()
            }
            viewModel.clearSaveResult()
        }
    }
}

@Composable
fun RecipeInfoCard(
    recipe: Recipe,
    onRecipeUpdate: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recipe Information",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = recipe.name,
                onValueChange = { onRecipeUpdate(recipe.copy(name = it)) },
                label = { Text("Recipe Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Beverage Type Dropdown - TODO: Implement dropdown
                OutlinedTextField(
                    value = recipe.beverageType.name,
                    onValueChange = { },
                    label = { Text("Type") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
                
                // Difficulty Dropdown - TODO: Implement dropdown
                OutlinedTextField(
                    value = recipe.difficulty.name,
                    onValueChange = { },
                    label = { Text("Difficulty") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
            }
            
            OutlinedTextField(
                value = recipe.style ?: "",
                onValueChange = { onRecipeUpdate(recipe.copy(style = it.ifBlank { null })) },
                label = { Text("Style (e.g., Traditional Mead)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = recipe.description ?: "",
                onValueChange = { onRecipeUpdate(recipe.copy(description = it.ifBlank { null })) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}

@Composable
fun CalculationsCard(
    calculations: RecipeCalculations?,
    isCalculating: Boolean,
    batchSize: BatchSize,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (calculations != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculationChip(
                        label = "ABV",
                        value = "${String.format("%.1f", calculations.estimatedABV)}%",
                        modifier = Modifier.weight(1f)
                    )
                    
                    CalculationChip(
                        label = "OG",
                        value = String.format("%.3f", calculations.estimatedOG),
                        modifier = Modifier.weight(1f)
                    )
                    
                    CalculationChip(
                        label = "FG",
                        value = String.format("%.3f", calculations.estimatedFG),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Calculated for ${batchSize.displayName} (${batchSize.ozValue} oz)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Add ingredients to see calculations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CalculationChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ValidationCard(
    validationResults: List<String>,
    onCreateProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recipe Validation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            validationResults.forEach { result ->
                Text(
                    text = "• $result",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onCreateProject,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Project Anyway")
            }
        }
    }
}

// Data class for calculations
data class RecipeCalculations(
    val estimatedOG: Double,
    val estimatedFG: Double,
    val estimatedABV: Double
)
