package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.brewingtracker.data.database.entities.YeastType
import com.brewingtracker.presentation.viewmodel.IngredientViewModel
import com.brewingtracker.presentation.viewmodel.UniversalCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(
    viewModel: IngredientViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedYeastType by viewModel.selectedYeastType.collectAsState()
    val filteredIngredients by viewModel.filteredIngredients.collectAsState()
    val filteredYeasts by viewModel.filteredYeasts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val tabs = listOf("Ingredients", "Yeasts")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp) // Space for bottom navigation
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
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
                        text = "Ingredient Database",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { viewModel.seedSampleData() }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sample Data")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    label = { Text("Search ${tabs[selectedTab].lowercase()}...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        // Clear search when switching tabs
                        viewModel.updateSearchQuery("")
                        // Clear filters when switching tabs
                        viewModel.updateSelectedCategory(null)
                        viewModel.updateSelectedYeastType(null)
                    },
                    text = { Text(title) }
                )
            }
        }

        // Content
        when (selectedTab) {
            0 -> IngredientsTab(
                ingredients = filteredIngredients,
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::updateSelectedCategory,
                onStockUpdate = viewModel::updateIngredientStock,
                modifier = Modifier.weight(1f)
            )
            1 -> YeastsTab(
                yeasts = filteredYeasts,
                selectedYeastType = selectedYeastType,
                onYeastTypeSelected = viewModel::updateSelectedYeastType,
                modifier = Modifier.weight(1f)
            )
        }

        // Success/Error Messages as Snackbar-style notifications
        if (uiState.successMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.successMessage!!,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            LaunchedEffect(uiState.successMessage) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearMessages()
            }
        }

        if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            LaunchedEffect(uiState.errorMessage) {
                kotlinx.coroutines.delay(5000)
                viewModel.clearMessages()
            }
        }
    }
}

@Composable
private fun IngredientsTab(
    ingredients: List<com.brewingtracker.data.database.entities.Ingredient>,
    selectedCategory: UniversalCategory?,
    onCategorySelected: (UniversalCategory?) -> Unit,
    onStockUpdate: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Universal Category Filter
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Filter by Category",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Universal categories for all beverage makers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { onCategorySelected(null) },
                            label = { Text("All") },
                            selected = selectedCategory == null
                        )
                    }
                    items(UniversalCategory.values()) { category ->
                        FilterChip(
                            onClick = { onCategorySelected(category) },
                            label = { Text(category.displayName) },
                            selected = selectedCategory == category
                        )
                    }
                }

                // Show category description
                selectedCategory?.let { category ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${category.description} • Primary uses: ${category.primaryBeverages.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Ingredients List
        if (ingredients.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No ingredients found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Try adjusting your search or filters, or add sample data",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ingredients) { ingredient ->
                    IngredientCard(
                        ingredient = ingredient,
                        onStockUpdate = onStockUpdate
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp)) // Extra bottom padding
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientCard(
    ingredient: com.brewingtracker.data.database.entities.Ingredient,
    onStockUpdate: (String, Double) -> Unit
) {
    var showStockDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: Navigate to ingredient detail */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
                    Text(
                        text = ingredient.category ?: ingredient.type.name.lowercase(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                // Stock chip
                AssistChip(
                    onClick = { showStockDialog = true },
                    label = {
                        Text(
                            text = "${ingredient.currentStock} ${ingredient.unit}",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Stock",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Description
            ingredient.description?.let { description ->
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Flavor Profile
            ingredient.flavorProfile?.let { flavor ->
                Text(
                    text = "Flavor: $flavor",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Properties in a more organized way
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Color (for grains)
                ingredient.colorLovibond?.let { color ->
                    PropertyItem("Color", "${color}°L")
                }

                // Alpha acids (for hops)
                ingredient.alphaAcidPercentage?.let { alpha ->
                    PropertyItem("Alpha Acids", "${alpha}%")
                }

                // Extract (for fermentables)
                ingredient.ppgExtract?.let { extract ->
                    PropertyItem("Extract", "${extract} PPG")
                }

                // Cost
                ingredient.costPerUnit?.let { cost ->
                    PropertyItem("Cost", "$${String.format("%.2f", cost)}/${ingredient.unit}")
                }
            }
        }
    }

    // Stock Update Dialog
    if (showStockDialog) {
        var newStock by remember { mutableStateOf(ingredient.currentStock.toString()) }

        AlertDialog(
            onDismissRequest = { showStockDialog = false },
            title = { Text("Update Stock - ${ingredient.name}") },
            text = {
                Column {
                    Text(
                        text = "Current stock: ${ingredient.currentStock} ${ingredient.unit}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newStock,
                        onValueChange = { newStock = it },
                        label = { Text("New Stock (${ingredient.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        newStock.toDoubleOrNull()?.let { stock ->
                            onStockUpdate(ingredient.id, stock)
                        }
                        showStockDialog = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PropertyItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun YeastsTab(
    yeasts: List<com.brewingtracker.data.database.entities.Yeast>,
    selectedYeastType: YeastType?,
    onYeastTypeSelected: (YeastType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Yeast Type Filter
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Filter by Yeast Type",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Choose the yeast type for your beverage",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { onYeastTypeSelected(null) },
                            label = { Text("All") },
                            selected = selectedYeastType == null
                        )
                    }
                    items(YeastType.values()) { type ->
                        FilterChip(
                            onClick = { onYeastTypeSelected(type) },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            selected = selectedYeastType == type
                        )
                    }
                }
            }
        }

        if (yeasts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No yeasts found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Try adjusting your search or add sample data",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(yeasts) { yeast ->
                    YeastCard(yeast = yeast)
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp)) // Extra bottom padding
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YeastCard(
    yeast: com.brewingtracker.data.database.entities.Yeast
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: Navigate to yeast detail */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = yeast.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${yeast.brand} - ${yeast.type.name} ${yeast.form.name}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                // Stock chip
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${yeast.currentStock} ${yeast.unit}",
                            fontSize = 12.sp
                        )
                    }
                )
            }

            yeast.description?.let { description ->
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Properties
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                yeast.attenuationRange?.let { attenuation ->
                    PropertyItem("Attenuation", attenuation)
                }

                yeast.temperatureRange?.let { temp ->
                    PropertyItem("Temperature", temp)
                }

                yeast.alcoholTolerance?.let { alcohol ->
                    PropertyItem("Alcohol Tolerance", "${alcohol}%")
                }

                yeast.costPerUnit?.let { cost ->
                    PropertyItem("Cost", "$${String.format("%.2f", cost)}")
                }
            }
        }
    }
}