package com.brewingtracker.presentation.screens

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
    val showInStockOnly by viewModel.showInStockOnly.collectAsStateWithLifecycle()

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

            Row {
                IconButton(
                    onClick = { viewModel.toggleShowInStockOnly() }
                ) {
                    Icon(
                        imageVector = if (showInStockOnly) Icons.Default.Inventory else Icons.Default.InventoryOutlined,
                        contentDescription = if (showInStockOnly) "Show all ingredients" else "Show in-stock only",
                        tint = if (showInStockOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = { viewModel.clearFilters() }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = "Clear filters"
                    )
                }
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
                        imageVector = Icons.Default.SearchOff,
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
                    IngredientCard(
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
private fun IngredientCard(
    ingredient: Ingredient,
    onStockUpdate: (Double) -> Unit
) {
    var showStockDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ingredient.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
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
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        
                        Text(
                            text = ingredient.beverageTypes,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(
                        onClick = { showStockDialog = true }
                    ) {
                        Text("Stock: ${String.format("%.1f", ingredient.currentStock)} ${ingredient.unit}")
                    }
                    
                    if (ingredient.currentStock > 0) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "In stock",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Out of stock",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Ingredient details
            ingredient.description?.let { desc ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Brewing characteristics
            val characteristics = buildList {
                ingredient.colorLovibond?.let { color ->
                    add("Color: ${String.format("%.1f", color)}°L")
                }
                ingredient.alphaAcidPercentage?.let { alpha ->
                    add("Alpha: ${String.format("%.1f", alpha)}%")
                }
                ingredient.ppgExtract?.let { ppg ->
                    add("Extract: ${String.format("%.0f", ppg)} PPG")
                }
            }

            if (characteristics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = characteristics.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                Text("Current stock: ${String.format("%.1f", ingredient.currentStock)} ${ingredient.unit}")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = stockValue,
                    onValueChange = { stockValue = it },
                    label = { Text("New stock amount") },
                    suffix = { Text(ingredient.unit) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    stockValue.toDoubleOrNull()?.let { newStock ->
                        onStockUpdate(newStock)
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