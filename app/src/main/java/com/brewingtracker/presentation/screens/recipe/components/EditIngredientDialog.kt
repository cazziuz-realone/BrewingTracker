package com.brewingtracker.presentation.screens.recipe.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.brewingtracker.data.database.entities.*

@Composable
fun EditIngredientDialog(
    ingredientWithDetails: RecipeIngredientWithDetails,
    batchSize: BatchSize,
    onDismiss: () -> Unit,
    onSave: (RecipeIngredient) -> Unit
) {
    var quantity by remember { 
        mutableStateOf(ingredientWithDetails.recipeIngredient.baseQuantity.toString()) 
    }
    var selectedUnit by remember { 
        mutableStateOf(ingredientWithDetails.recipeIngredient.baseUnit) 
    }
    var selectedTiming by remember { 
        mutableStateOf(ingredientWithDetails.recipeIngredient.additionTiming) 
    }
    var notes by remember { 
        mutableStateOf(ingredientWithDetails.recipeIngredient.notes ?: "") 
    }
    var additionDay by remember { 
        mutableStateOf(ingredientWithDetails.recipeIngredient.additionDay?.toString() ?: "") 
    }

    // Common units for different ingredient types
    val commonUnits = when (ingredientWithDetails.ingredient.type) {
        IngredientType.GRAIN -> listOf("lbs", "oz", "kg", "g")
        IngredientType.FRUIT -> listOf("lbs", "oz", "kg", "g", "cups", "quarts", "gallons")
        IngredientType.SPICE -> listOf("oz", "g", "tsp", "tbsp", "each")
        IngredientType.HOP -> listOf("oz", "g")
        IngredientType.YEAST_NUTRIENT -> listOf("tsp", "oz", "g")
        IngredientType.ACID -> listOf("tsp", "oz", "g")
        IngredientType.WATER_TREATMENT -> listOf("tsp", "oz", "g")
        IngredientType.CLARIFIER -> listOf("tsp", "oz", "packets")
        IngredientType.ADJUNCT -> listOf("lbs", "oz", "kg", "g", "cups")
        IngredientType.OTHER -> listOf("each", "oz", "lbs", "cups")
    }

    val timingOptions = listOf(
        "primary",
        "secondary", 
        "aging",
        "bottling",
        "boil",
        "flameout",
        "dry hop"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Ingredient",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                // Ingredient name (read-only)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = ingredientWithDetails.ingredient.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = ingredientWithDetails.ingredient.type.name.lowercase()
                                    .replaceFirstChar { it.uppercase() }
                                    .replace("_", " "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quantity input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                quantity = newValue
                            }
                        },
                        label = { Text("Quantity (Base: 1 gallon)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Icon(Icons.Default.Scale, contentDescription = null)
                        },
                        singleLine = true
                    )
                }

                // Unit selection
                Text(
                    text = "Unit",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        commonUnits.chunked(3).forEach { rowUnits ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowUnits.forEach { unit ->
                                    FilterChip(
                                        onClick = { selectedUnit = unit },
                                        label = { Text(unit) },
                                        selected = selectedUnit == unit,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                // Fill remaining space if less than 3 units in row
                                repeat(3 - rowUnits.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Addition timing
                Text(
                    text = "Addition Timing",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        timingOptions.chunked(2).forEach { rowTimings ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowTimings.forEach { timing ->
                                    FilterChip(
                                        onClick = { selectedTiming = timing },
                                        label = { 
                                            Text(timing.replaceFirstChar { it.uppercase() }) 
                                        },
                                        selected = selectedTiming == timing,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                // Fill remaining space if odd number
                                if (rowTimings.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Addition day (optional)
                OutlinedTextField(
                    value = additionDay,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                            additionDay = newValue
                        }
                    },
                    label = { Text("Addition Day (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    },
                    placeholder = { Text("e.g., 7 for day 7 of fermentation") },
                    singleLine = true
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null)
                    },
                    placeholder = { Text("Special instructions or notes...") }
                )

                // Scaled quantity preview
                val scaledQuantity = (quantity.toDoubleOrNull() ?: 0.0) * batchSize.scaleFactor
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Preview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "For ${batchSize.displayName}: ${scaledQuantity.formatQuantity()} $selectedUnit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Divider()

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val quantityDouble = quantity.toDoubleOrNull()
                            if (quantityDouble != null && quantityDouble > 0) {
                                val updatedIngredient = ingredientWithDetails.recipeIngredient.copy(
                                    baseQuantity = quantityDouble,
                                    baseUnit = selectedUnit,
                                    additionTiming = selectedTiming,
                                    additionDay = additionDay.toIntOrNull(),
                                    notes = notes.ifBlank { null }
                                )
                                onSave(updatedIngredient)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = quantity.toDoubleOrNull()?.let { it > 0 } == true
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}