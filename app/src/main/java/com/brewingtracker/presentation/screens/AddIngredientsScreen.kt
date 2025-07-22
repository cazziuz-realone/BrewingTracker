package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType
import com.brewingtracker.presentation.viewmodel.IngredientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientsScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onIngredientsAdded: () -> Unit = {},
    viewModel: IngredientsViewModel = hiltViewModel()
) {
    val ingredients by viewModel.allIngredients.collectAsStateWithLifecycle()
    var selectedIngredients by remember { mutableStateOf(setOf<Int>()) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Ingredients") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedIngredients.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                isSaving = true
                                // Save selected ingredients to project
                                viewModel.addIngredientsToProject(
                                    projectId = projectId,
                                    ingredientIds = selectedIngredients,
                                    defaultQuantity = 1.0, // Default to 1 unit - user can edit later
                                    defaultUnit = "lbs" // Default unit - user can edit later
                                )
                                
                                // Navigate back after a short delay to show completion
                                isSaving = false
                                onIngredientsAdded()
                                onNavigateBack()
                            },
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = "Add Selected")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (selectedIngredients.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedIngredients.size} ingredient(s) selected",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (!isSaving) {
                            AssistChip(
                                onClick = { selectedIngredients = emptySet() },
                                label = { Text("Clear") }
                            )
                        }
                    }
                }
            }
            
            when {
                ingredients.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No ingredients available",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Add ingredients in the Ingredients tab first",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Select ingredients to add to your project:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Group ingredients by type
                        val groupedIngredients = ingredients.groupBy { it.type }
                        
                        IngredientType.entries.forEach { type ->
                            val typeIngredients = groupedIngredients[type] ?: emptyList()
                            if (typeIngredients.isNotEmpty()) {
                                item {
                                    Text(
                                        text = type.name.replace("_", " ").lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                
                                items(typeIngredients) { ingredient ->
                                    IngredientSelectionCard(
                                        ingredient = ingredient,
                                        isSelected = selectedIngredients.contains(ingredient.id),
                                        enabled = !isSaving,
                                        onSelectionChanged = { isSelected ->
                                            if (!isSaving) {
                                                selectedIngredients = if (isSelected) {
                                                    selectedIngredients + ingredient.id
                                                } else {
                                                    selectedIngredients - ingredient.id
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientSelectionCard(
    ingredient: Ingredient,
    isSelected: Boolean,
    enabled: Boolean = true,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                enabled = enabled,
                onClick = { onSelectionChanged(!isSelected) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (!ingredient.description.isNullOrEmpty()) {
                    Text(
                        text = ingredient.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (ingredient.type == IngredientType.GRAIN && ingredient.ppgExtract != null && ingredient.ppgExtract > 0) {
                        Text(
                            text = "Extract: ${String.format("%.1f", ingredient.ppgExtract)} PPG",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ingredient.type == IngredientType.HOP && ingredient.alphaAcidPercentage != null && ingredient.alphaAcidPercentage > 0) {
                        Text(
                            text = "AA: ${String.format("%.1f", ingredient.alphaAcidPercentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ingredient.colorLovibond != null && ingredient.colorLovibond > 0) {
                        Text(
                            text = "${String.format("%.1f", ingredient.colorLovibond)}°L",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Show current stock if available
                if (ingredient.currentStock > 0) {
                    Text(
                        text = "In stock: ${String.format("%.1f", ingredient.currentStock)} ${ingredient.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            Checkbox(
                checked = isSelected,
                enabled = enabled,
                onCheckedChange = onSelectionChanged
            )
        }
    }
}
