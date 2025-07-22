package com.brewingtracker.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brewingtracker.data.database.entities.Project
import com.brewingtracker.data.database.entities.ProjectPhase
import com.brewingtracker.data.database.dao.ProjectIngredientWithDetails
import com.brewingtracker.presentation.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onAddIngredientsClick: () -> Unit = {},
    onDeleteProject: (String) -> Unit = {},
    viewModel: ProjectViewModel = hiltViewModel()
) {
    // Fixed: Added initialValue parameters to collectAsStateWithLifecycle()
    val project by viewModel.getProjectById(projectId).collectAsStateWithLifecycle(initialValue = null)
    val projectIngredients by viewModel.getProjectIngredientsWithDetails(projectId).collectAsStateWithLifecycle(initialValue = emptyList())
    val scrollState = rememberScrollState()

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // State for ingredient editing dialog - NEW
    var editingIngredient by remember { mutableStateOf<ProjectIngredientWithDetails?>(null) }
    
    // State for reading input dialog - NEW
    var showReadingDialog by remember { mutableStateOf(false) }
    
    // State for photo selection dialog - NEW
    var showPhotoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        if (project != null) {
            val proj = project!!

            // Header with Delete Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = proj.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Project"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Project",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Project Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Project Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Phase",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = proj.currentPhase.name.replace("_", " "),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column {
                            Text(
                                text = "Project Type",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = proj.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column {
                            Text(
                                text = "Started",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(proj.startDate)),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Phase Progress
            PhaseProgressCard(
                currentPhase = proj.currentPhase,
                onPhaseUpdate = { newPhase ->
                    viewModel.updateProjectPhase(proj.id, newPhase)
                }
            )

            // Target Parameters
            if (proj.targetOG != null || proj.targetFG != null || proj.targetABV != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Parameters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            proj.targetOG?.let { og ->
                                ParameterItem("Original Gravity", String.format("%.3f", og))
                            }
                            proj.targetFG?.let { fg ->
                                ParameterItem("Final Gravity", String.format("%.3f", fg))
                            }
                            proj.targetABV?.let { abv ->
                                ParameterItem("Target ABV", "${String.format("%.1f", abv)}%")
                            }
                        }

                        proj.batchSize?.let { size ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                ParameterItem("Batch Size", "${String.format("%.1f", size)} gal")
                            }
                        }
                    }
                }
            }

            // Project Ingredients - ENHANCED SECTION WITH EXPANDABLE CARDS
            ProjectIngredientsCard(
                ingredients = projectIngredients,
                onAddIngredientsClick = onAddIngredientsClick,
                onRemoveIngredient = { ingredientId ->
                    viewModel.removeIngredientFromProject(proj.id, ingredientId)
                },
                onEditIngredient = { ingredient ->  // ENHANCED: Pass full ingredient object
                    editingIngredient = ingredient
                }
            )

            // Quick Actions - ENHANCED WITH FUNCTIONALITY
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Ingredients Action
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FloatingActionButton(
                                onClick = onAddIngredientsClick,
                                modifier = Modifier.size(56.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Ingredients",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ingredients",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Reading Action - ENHANCED WITH FUNCTIONALITY
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FloatingActionButton(
                                onClick = { showReadingDialog = true },  // ENHANCED: Added functionality
                                modifier = Modifier.size(56.dp),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = "Add Reading",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reading",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Photo Action - ENHANCED WITH FUNCTIONALITY
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FloatingActionButton(
                                onClick = { showPhotoDialog = true },  // ENHANCED: Added functionality
                                modifier = Modifier.size(56.dp),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Add Photo",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Photo",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Notes
            proj.notes?.let { projectNotes ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Notes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = projectNotes,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Project") },
                    text = { 
                        Text("Are you sure you want to delete \"${proj.name}\"? This action cannot be undone.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteProject(proj.id)  // ENHANCED: Call delete method
                                onDeleteProject(proj.id)
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            // NEW: Ingredient Edit Dialog
            editingIngredient?.let { ingredient ->
                EditIngredientDialog(
                    ingredient = ingredient,
                    onDismiss = { editingIngredient = null },
                    onUpdate = { quantity, unit, additionTime ->
                        viewModel.updateProjectIngredient(
                            projectId = proj.id,
                            ingredientId = ingredient.ingredientId,
                            quantity = quantity,
                            unit = unit,
                            additionTime = additionTime
                        )
                        editingIngredient = null
                    }
                )
            }
            
            // NEW: Reading Input Dialog
            if (showReadingDialog) {
                ReadingInputDialog(
                    onDismiss = { showReadingDialog = false },
                    onSubmit = { gravity, temperature, notes ->
                        // TODO: Save gravity reading to database
                        showReadingDialog = false
                    }
                )
            }
            
            // NEW: Photo Selection Dialog  
            if (showPhotoDialog) {
                PhotoSelectionDialog(
                    onDismiss = { showPhotoDialog = false },
                    onPhotoSelected = { photoPath ->
                        // TODO: Save photo to project
                        showPhotoDialog = false
                    }
                )
            }

        } else {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

// NEW: Edit Ingredient Dialog
@Composable
private fun EditIngredientDialog(
    ingredient: ProjectIngredientWithDetails,
    onDismiss: () -> Unit,
    onUpdate: (Double, String, String?) -> Unit
) {
    var quantity by remember { mutableStateOf(ingredient.quantity.toString()) }
    var unit by remember { mutableStateOf(ingredient.unit) }
    var additionTime by remember { mutableStateOf(ingredient.additionTime ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${ingredient.ingredientName}") },
        text = {
            Column {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = additionTime,
                    onValueChange = { additionTime = it },
                    label = { Text("Addition Time (optional)") },
                    placeholder = { Text("e.g., 60 min boil, dry hop") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    quantity.toDoubleOrNull()?.let { qty ->
                        onUpdate(qty, unit, additionTime.takeIf { it.isNotBlank() })
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

// NEW: Reading Input Dialog
@Composable
private fun ReadingInputDialog(
    onDismiss: () -> Unit,
    onSubmit: (Double, Double?, String?) -> Unit
) {
    var gravity by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Gravity Reading") },
        text = {
            Column {
                OutlinedTextField(
                    value = gravity,
                    onValueChange = { gravity = it },
                    label = { Text("Specific Gravity") },
                    placeholder = { Text("1.050") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temperature (°F)") },
                    placeholder = { Text("68") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    gravity.toDoubleOrNull()?.let { sg ->
                        val temp = temperature.toDoubleOrNull()
                        val readingNotes = notes.takeIf { it.isNotBlank() }
                        onSubmit(sg, temp, readingNotes)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// NEW: Photo Selection Dialog (placeholder)
@Composable
private fun PhotoSelectionDialog(
    onDismiss: () -> Unit,
    onPhotoSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Photo") },
        text = { 
            Text("Photo functionality will be available in a future update. For now, you can take photos manually and add them to your project notes.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun ProjectIngredientsCard(
    ingredients: List<ProjectIngredientWithDetails>,
    onAddIngredientsClick: () -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onEditIngredient: (ProjectIngredientWithDetails) -> Unit,  // ENHANCED: Pass full ingredient
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recipe Ingredients",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (ingredients.isNotEmpty()) {
                    Text(
                        text = "${ingredients.size} ingredient${if (ingredients.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                ingredients.isEmpty() -> {
                    // Enhanced Empty state
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No ingredients added yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add ingredients to start building your recipe",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddIngredientsClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Ingredients")
                        }
                    }
                }
                else -> {
                    // ENHANCED: Expandable ingredients list
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ingredients) { ingredient ->
                            ExpandableProjectIngredientItem(
                                ingredient = ingredient,
                                onRemove = { onRemoveIngredient(ingredient.ingredientId) },
                                onEdit = { onEditIngredient(ingredient) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = onAddIngredientsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add More Ingredients")
                    }
                }
            }
        }
    }
}

// NEW: Expandable Project Ingredient Item
@Composable
private fun ExpandableProjectIngredientItem(
    ingredient: ProjectIngredientWithDetails,
    onRemove: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Get ingredient type icon based on ingredient type string
    val typeIcon = when (ingredient.ingredientType.uppercase()) {
        "GRAIN" -> "🌾"
        "HOP" -> "🍃"
        "FRUIT" -> "🍎"
        "ADJUNCT" -> "🍯"
        "SPICE" -> "🌶️"
        "YEAST_NUTRIENT" -> "🧪"
        "ACID" -> "⚗️"
        "WATER_TREATMENT" -> "💧"
        "CLARIFIER" -> "🔍"
        else -> "📦"
    }

    // Get ingredient type color
    val typeColor = when (ingredient.ingredientType.uppercase()) {
        "GRAIN" -> MaterialTheme.colorScheme.primaryContainer
        "HOP" -> MaterialTheme.colorScheme.secondaryContainer
        "FRUIT" -> MaterialTheme.colorScheme.tertiaryContainer
        "ADJUNCT" -> MaterialTheme.colorScheme.errorContainer
        "SPICE" -> MaterialTheme.colorScheme.surfaceVariant
        "YEAST_NUTRIENT" -> MaterialTheme.colorScheme.primaryContainer
        "ACID" -> MaterialTheme.colorScheme.errorContainer
        "WATER_TREATMENT" -> MaterialTheme.colorScheme.secondaryContainer
        "CLARIFIER" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                            fontSize = 18.sp
                        )
                        Text(
                            text = ingredient.ingredientName,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
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
                                    text = ingredient.ingredientType.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = typeColor
                            )
                        )
                        
                        Text(
                            text = "${String.format("%.1f", ingredient.quantity)} ${ingredient.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Show addition time in collapsed view if available
                    ingredient.additionTime?.let { time ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Added at: $time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit ingredient",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove ingredient",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // Expand/Collapse icon
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
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

                    // Recipe Details
                    Text(
                        text = "Recipe Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val recipeDetails = buildList {
                        add("Quantity" to "${String.format("%.1f", ingredient.quantity)} ${ingredient.unit}")
                        ingredient.additionTime?.let { time ->
                            add("Addition Time" to time)
                        }
                        add("Type" to ingredient.ingredientType.lowercase().replaceFirstChar { it.uppercase() })
                        ingredient.notes?.let { notes ->
                            add("Notes" to notes)
                        }
                    }

                    recipeDetails.chunked(2).forEach { row ->
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

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    HorizontalDivider()
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Actions in expanded view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = onRemove,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParameterItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhaseProgressCard(
    currentPhase: ProjectPhase,
    onPhaseUpdate: (ProjectPhase) -> Unit
) {
    val phases = ProjectPhase.values().toList()
    val currentIndex = phases.indexOf(currentPhase)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Project Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Phase chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(phases) { phase ->
                    val isActive = phase == currentPhase
                    val isPast = phases.indexOf(phase) < currentIndex

                    FilterChip(
                        onClick = { onPhaseUpdate(phase) },
                        label = {
                            Text(
                                text = phase.name.replace("_", " "),
                                fontSize = 12.sp
                            )
                        },
                        selected = isActive,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isPast -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                }
            }

            // Progress indicator
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / phases.size },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Phase ${currentIndex + 1} of ${phases.size}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}