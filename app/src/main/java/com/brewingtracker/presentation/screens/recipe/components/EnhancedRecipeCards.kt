package com.brewingtracker.presentation.screens.recipe.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brewingtracker.data.database.entities.*
import com.brewingtracker.data.models.*
import com.brewingtracker.utils.formatQuantity

@Composable
fun BatchSizeCard(
    currentSize: BatchSize,
    onSizeChange: (BatchSize) -> Unit,
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
                    text = "Batch Size",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Batch settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BatchSize.values().forEach { size ->
                    FilterChip(
                        onClick = { onSizeChange(size) },
                        label = { 
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = size.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${size.ozValue} oz",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        selected = currentSize == size,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Scaling indicator
            if (currentSize != BatchSize.GALLON) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Scaling info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Scaling from 1 gallon base × ${currentSize.scaleFactor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationsCard(
    calculations: LiveRecipeCalculations,
    isCalculating: Boolean,
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
                    text = "Recipe Calculations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Calculations",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (calculations.hasError) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = calculations.errorMessage ?: "Calculation error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                // Calculation Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculationMetric(
                        label = "OG",
                        value = String.format("%.3f", calculations.estimatedOG),
                        modifier = Modifier.weight(1f)
                    )
                    CalculationMetric(
                        label = "FG",
                        value = String.format("%.3f", calculations.estimatedFG),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculationMetric(
                        label = "ABV",
                        value = String.format("%.1f%%", calculations.estimatedABV),
                        modifier = Modifier.weight(1f),
                        highlight = calculations.estimatedABV > 0
                    )
                    CalculationMetric(
                        label = "SRM",
                        value = String.format("%.1f", calculations.estimatedSRM),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (calculations.estimatedCost > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalculationMetric(
                            label = "Cost",
                            value = "$${String.format("%.2f", calculations.estimatedCost)}",
                            modifier = Modifier.weight(1f),
                            highlight = calculations.estimatedCost > 0
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (highlight) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlight) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun IngredientCategoriesCard(
    selectedCategory: IngredientType?,
    onCategorySelect: (IngredientType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ingredient Categories",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Categories for recipe building
            val recipeCategories = listOf(
                IngredientType.FRUIT to "🍯 Honey & Fruits",
                IngredientType.SPICE to "🌿 Spices & Herbs", 
                IngredientType.YEAST_NUTRIENT to "⚗️ Nutrients",
                IngredientType.ACID to "🍋 Acids",
                IngredientType.CLARIFIER to "💎 Clarifiers",
                IngredientType.OTHER to "🔬 Other"
            )
            
            recipeCategories.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (type, label) ->
                        FilterChip(
                            onClick = { onCategorySelect(type) },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            },
                            selected = selectedCategory == type,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Fill remaining space if odd number
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ValidationCard(
    validationResults: Map<String, String>,
    onSave: () -> Unit,
    onCreateProject: (BatchSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recipe Actions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Validation Messages
            if (validationResults.isNotEmpty()) {
                validationResults.forEach { (field, message) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$field: $message",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = validationResults.isEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Recipe")
                }
                
                Button(
                    onClick = { onCreateProject(BatchSize.GALLON) },
                    modifier = Modifier.weight(1f),
                    enabled = validationResults.isEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Brewing",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Brewing")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Additional Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { /* Export recipe */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
                
                TextButton(
                    onClick = { /* Duplicate recipe */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicate")
                }
            }
        }
    }
}

@Composable
fun ProcessStepsCard(
    steps: List<RecipeStep>,
    onStepUpdate: (RecipeStep) -> Unit,
    onStepAdd: () -> Unit,
    onStepRemove: (Int) -> Unit,
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
                    text = "Brewing Process",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = onStepAdd
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Step"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (steps.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "No steps",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No brewing steps yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add steps to guide your brewing process",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                steps.forEachIndexed { index, step ->
                    ProcessStepItem(
                        step = step,
                        onUpdate = onStepUpdate,
                        onRemove = { onStepRemove(step.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (index < steps.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessStepItem(
    step: RecipeStep,
    onUpdate: (RecipeStep) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${step.stepNumber}.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = step.phase.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    step.estimatedDuration?.let { duration ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Duration",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = duration,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Row {
                    IconButton(
                        onClick = { /* Edit step */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit step",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onRemove
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove step",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
