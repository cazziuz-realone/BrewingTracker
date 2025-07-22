package com.brewingtracker.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brewingtracker.data.database.entities.Ingredient
import com.brewingtracker.data.database.entities.IngredientType
import com.brewingtracker.presentation.viewmodel.IngredientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(
    viewModel: IngredientsViewModel = hiltViewModel()
) {
    val ingredients by viewModel.filteredIngredients.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedIngredientType.collectAsStateWithLifecycle()
    val selectedBeverageType by viewModel.selectedBeverageType.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingredients",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { viewModel.clearFilters() }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear filters"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("Search ingredients") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ingredient Type Filter
        Text(
            text = "Filter by Type",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { viewModel.filterByType(null) },
                    label = { Text("All") }
                )
            }
            items(viewModel.getIngredientTypes()) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { viewModel.filterByType(type) },
                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Beverage Type Filter
        Text(
            text = "Filter by Beverage",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedBeverageType == null,
                    onClick = { viewModel.filterByBeverageType(null) },
                    label = { Text("All") }
                )
            }
            items(viewModel.getBeverageTypes()) { beverageType ->
                FilterChip(
                    selected = selectedBeverageType == beverageType,
                    onClick = { viewModel.filterByBeverageType(beverageType) },
                    label = { Text(beverageType.replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Results count
        Text(
            text = "${ingredients.size} ingredients found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Ingredients list
        if (ingredients.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No ingredients found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Try adjusting your search or filters",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ingredients) { ingredient ->
                    ExpandableIngredientCard(
                        ingredient = ingredient,
                        onStockUpdate = { newStock ->
                            viewModel.updateIngredientStock(ingredient.id, newStock)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandableIngredientCard(
    ingredient: Ingredient,
    onStockUpdate: (Double) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }

    // Get ingredient type icon - FIXED: Use correct enum values
    val typeIcon = when (ingredient.type) {
        IngredientType.GRAIN -> "🌾"
        IngredientType.HOP -> "🍃"
        IngredientType.FRUIT -> "🍎"
        IngredientType.ADJUNCT -> "🍯"
        IngredientType.SPICE -> "🌶️"
        IngredientType.YEAST_NUTRIENT -> "🧪"
        IngredientType.ACID -> "⚗️"
        IngredientType.WATER_TREATMENT -> "💧"
        IngredientType.CLARIFIER -> "🔍"
        IngredientType.OTHER -> "📦"
    }

    // Get ingredient type color - FIXED: Use correct enum values
    val typeColor = when (ingredient.type) {
        IngredientType.GRAIN -> MaterialTheme.colorScheme.primaryContainer
        IngredientType.HOP -> MaterialTheme.colorScheme.secondaryContainer
        IngredientType.FRUIT -> MaterialTheme.colorScheme.tertiaryContainer
        IngredientType.ADJUNCT -> MaterialTheme.colorScheme.errorContainer
        IngredientType.SPICE -> MaterialTheme.colorScheme.surfaceVariant
        IngredientType.YEAST_NUTRIENT -> MaterialTheme.colorScheme.primaryContainer
        IngredientType.ACID -> MaterialTheme.colorScheme.errorContainer
        IngredientType.WATER_TREATMENT -> MaterialTheme.colorScheme.secondaryContainer
        IngredientType.CLARIFIER -> MaterialTheme.colorScheme.tertiaryContainer
        IngredientType.OTHER -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Collapsed view - Always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = typeIcon,
                            fontSize = 20.sp
                        )
                        Text(
                            text = ingredient.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = ingredient.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = typeColor
                            )
                        )
                        
                        Text(
                            text = ingredient.beverageTypes,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    // Basic brewing characteristics in collapsed view
                    val basicCharacteristics = buildList {
                        ingredient.colorLovibond?.let { color ->
                            add("${String.format("%.1f", color)}°L")
                        }
                        ingredient.alphaAcidPercentage?.let { alpha ->
                            add("${String.format("%.1f", alpha)}% AA")
                        }
                        ingredient.ppgExtract?.let { ppg ->
                            add("${String.format("%.0f", ppg)} PPG")
                        }
                    }

                    if (basicCharacteristics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = basicCharacteristics.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Expand/Collapse icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded view - Only visible when expanded
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HorizontalDivider()
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    ingredient.description?.let { description ->
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Detailed brewing characteristics
                    Text(
                        text = "Brewing Characteristics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val detailedCharacteristics = buildList {
                        ingredient.colorLovibond?.let { color ->
                            add("Color" to "${String.format("%.1f", color)}°L")
                        }
                        ingredient.alphaAcidPercentage?.let { alpha ->
                            add("Alpha Acids" to "${String.format("%.1f", alpha)}%")
                        }
                        ingredient.ppgExtract?.let { ppg ->
                            add("Extract Potential" to "${String.format("%.0f", ppg)} PPG")
                        }
                        ingredient.category?.let { category ->
                            add("Category" to category)
                        }
                    }

                    if (detailedCharacteristics.isNotEmpty()) {
                        detailedCharacteristics.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                row.forEach { (label, value) ->
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = value,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                // Fill remaining space if odd number of items
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    HorizontalDivider()
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stock Management Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Stock",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${String.format("%.1f", ingredient.currentStock)} ${ingredient.unit}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { showStockDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update Stock")
                        }
                    }
                }
            }
        }
    }

    // Stock update dialog
    if (showStockDialog) {
        StockUpdateDialog(
            ingredient = ingredient,
            onDismiss = { showStockDialog = false },
            onStockUpdate = { newStock ->
                onStockUpdate(newStock)
                showStockDialog = false
            }
        )
    }
}

@Composable
private fun StockUpdateDialog(
    ingredient: Ingredient,
    onDismiss: () -> Unit,
    onStockUpdate: (Double) -> Unit
) {
    var stockValue by remember { mutableStateOf(ingredient.currentStock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Stock: ${ingredient.name}") },
        text = {
            Column {
                Text(
                    text = "Current stock: ${String.format("%.1f", ingredient.currentStock)} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = stockValue,
                    onValueChange = { stockValue = it },
                    label = { Text("New stock amount") },
                    suffix = { Text(ingredient.unit) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    stockValue.toDoubleOrNull()?.let { newStock ->
                        if (newStock >= 0) {
                            onStockUpdate(newStock)
                        }
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}