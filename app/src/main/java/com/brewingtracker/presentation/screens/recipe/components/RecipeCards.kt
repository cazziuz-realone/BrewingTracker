package com.brewingtracker.presentation.screens.recipe.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Batch Size",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // FIXED: Better layout for batch size buttons with consistent sizing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BatchSize.values().forEach { size ->
                    // FIXED: Use more consistent text and better sizing
                    FilterChip(
                        onClick = { onSizeChange(size) },
                        label = { 
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (size) {
                                        BatchSize.QUART -> "Quart"
                                        BatchSize.HALF_GALLON -> "½ Gal"
                                        BatchSize.GALLON -> "1 Gal"
                                        BatchSize.FIVE_GALLON -> "5 Gal"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${size.ozValue} oz",
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = if (currentSize == size) 
                                        MaterialTheme.colorScheme.onSecondaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = currentSize == size,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp), // Fixed height for consistency
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
            
            // FIXED: Better scaling indicator with improved styling
            if (currentSize != BatchSize.GALLON) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz, // FIXED: Use existing icon instead of ScaleIcon
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scaling from 1 gallon base × ${currentSize.scaleFactor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientCard(
    ingredientWithDetails: RecipeIngredientWithDetails,
    inventoryStatus: InventoryStatus,
    batchSize: BatchSize,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (inventoryStatus) {
                InventoryStatus.SUFFICIENT -> MaterialTheme.colorScheme.surfaceVariant
                InventoryStatus.INSUFFICIENT -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                InventoryStatus.UNKNOWN -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stock Status Icon
            when (inventoryStatus) {
                InventoryStatus.SUFFICIENT -> Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Sufficient stock",
                    tint = Color(0xFF4CAF50), // Green
                    modifier = Modifier.size(24.dp)
                )
                InventoryStatus.INSUFFICIENT -> Icon(
                    Icons.Default.Warning,
                    contentDescription = "Insufficient stock", 
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                InventoryStatus.UNKNOWN -> Icon(
                    Icons.Default.HelpOutline,
                    contentDescription = "Unknown stock",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredientWithDetails.ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${scaledQuantity.formatQuantity()} ${ingredientWithDetails.recipeIngredient.baseUnit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Added during ${ingredientWithDetails.recipeIngredient.additionTiming}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Stock information for insufficient items
                if (inventoryStatus == InventoryStatus.INSUFFICIENT) {
                    Text(
                        text = "Need ${scaledQuantity.formatQuantity()} ${ingredientWithDetails.recipeIngredient.baseUnit}, " +
                               "have ${ingredientWithDetails.ingredient.currentStock.formatQuantity()} ${ingredientWithDetails.ingredient.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Notes if available
                ingredientWithDetails.recipeIngredient.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Text(
                            text = "Notes: $notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            
            // Action buttons
            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Edit ingredient",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Remove ingredient",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedIngredientsCard(
    ingredients: List<RecipeIngredientWithDetails>,
    inventoryStatus: Map<Int, InventoryStatus>,
    batchSize: BatchSize,
    onIngredientEdit: (RecipeIngredient) -> Unit,
    onIngredientRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // State for edit dialog
    var editingIngredient by remember { mutableStateOf<RecipeIngredientWithDetails?>(null) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
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
                    text = "Recipe Ingredients",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Ingredient count badge with inventory status summary
                if (ingredients.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Count of sufficient ingredients
                        val sufficientCount = inventoryStatus.values.count { it == InventoryStatus.SUFFICIENT }
                        val insufficientCount = inventoryStatus.values.count { it == InventoryStatus.INSUFFICIENT }
                        
                        if (insufficientCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "⚠️ $insufficientCount",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "${ingredients.size}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            
            if (ingredients.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No ingredients added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Start building your recipe by adding ingredients below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Group ingredients by timing
                val groupedIngredients = ingredients.groupBy { it.recipeIngredient.additionTiming }
                
                groupedIngredients.forEach { (timing, timingIngredients) ->
                    // Timing header
                    Text(
                        text = timing.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Ingredients for this timing
                    timingIngredients.forEach { ingredientWithDetails ->
                        IngredientCard(
                            ingredientWithDetails = ingredientWithDetails,
                            inventoryStatus = inventoryStatus[ingredientWithDetails.ingredient.id] 
                                ?: InventoryStatus.UNKNOWN,
                            batchSize = batchSize,
                            onEdit = { 
                                editingIngredient = ingredientWithDetails
                            },
                            onRemove = { 
                                onIngredientRemove(ingredientWithDetails.recipeIngredient.id) 
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    // Show edit dialog if ingredient is being edited
    editingIngredient?.let { ingredient ->
        EditIngredientDialog(
            ingredientWithDetails = ingredient,
            batchSize = batchSize,
            onDismiss = { editingIngredient = null },
            onSave = { updatedIngredient ->
                onIngredientEdit(updatedIngredient)
                editingIngredient = null
            }
        )
    }
}
