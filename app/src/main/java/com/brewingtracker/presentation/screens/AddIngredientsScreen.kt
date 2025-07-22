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
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }
    
    LaunchedEffect(projectId) {
        viewModel.loadIngredients()
    }

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
                                // TODO: Add selected ingredients to project
                                // This will be implemented when we add the ProjectIngredient logic
                                onIngredientsAdded()
                                onNavigateBack()
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Add Selected")
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
                    Text(
                        text = "${selectedIngredients.size} ingredient(s) selected",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
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
                                        onSelectionChanged = { isSelected ->
                                            selectedIngredients = if (isSelected) {
                                                selectedIngredients + ingredient.id
                                            } else {
                                                selectedIngredients - ingredient.id
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
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
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
                if (ingredient.description.isNotEmpty()) {
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
                    if (ingredient.type == IngredientType.GRAIN && ingredient.potential > 0) {
                        Text(
                            text = "Potential: ${String.format("%.3f", ingredient.potential)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ingredient.type == IngredientType.HOP && ingredient.alphaAcid > 0) {
                        Text(
                            text = "AA: ${String.format("%.1f", ingredient.alphaAcid)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ingredient.type == IngredientType.GRAIN && ingredient.lovibond > 0) {
                        Text(
                            text = "${String.format("%.1f", ingredient.lovibond)}°L",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged
            )
        }
    }
}
