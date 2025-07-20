package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brewingtracker.data.database.entities.ProjectType
import com.brewingtracker.presentation.viewmodel.ProjectsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    onBackClick: () -> Unit,
    onProjectCreated: () -> Unit,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    var projectName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ProjectType.BEER) }
    var description by remember { mutableStateOf("") }
    var batchSize by remember { mutableStateOf("") }
    var targetOG by remember { mutableStateOf("") }
    var targetFG by remember { mutableStateOf("") }
    var targetABV by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Project") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createProject(
                                name = projectName,
                                type = selectedType,
                                description = description.takeIf { it.isNotBlank() },
                                batchSize = batchSize.toDoubleOrNull(),
                                targetOG = targetOG.toDoubleOrNull(),
                                targetFG = targetFG.toDoubleOrNull(),
                                targetABV = targetABV.toDoubleOrNull()
                            )
                            onProjectCreated()
                        },
                        enabled = projectName.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Project Information
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Project Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = projectName,
                            onValueChange = { projectName = it },
                            label = { Text("Project Name") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., Summer IPA, Honey Mead") },
                            isError = projectName.isBlank()
                        )

                        // Project Type Dropdown
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = !typeExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedType.name.lowercase().replaceFirstChar { it.uppercase() },
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Project Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                ProjectType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            selectedType = type
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            placeholder = { Text("Brief description of your project") }
                        )

                        OutlinedTextField(
                            value = batchSize,
                            onValueChange = { batchSize = it },
                            label = { Text("Batch Size (gallons)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("5.0") }
                        )
                    }
                }
            }

            // Target Parameters
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Target Parameters (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Set your brewing targets. You can always update these later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = targetOG,
                                onValueChange = { targetOG = it },
                                label = { Text("Target OG") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                placeholder = { Text("1.050") }
                            )

                            OutlinedTextField(
                                value = targetFG,
                                onValueChange = { targetFG = it },
                                label = { Text("Target FG") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                placeholder = { Text("1.010") }
                            )
                        }

                        OutlinedTextField(
                            value = targetABV,
                            onValueChange = { targetABV = it },
                            label = { Text("Target ABV%") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("5.2") }
                        )
                    }
                }
            }

            // Project Type Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = getProjectTypeDescription(selectedType),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun getProjectTypeDescription(type: ProjectType): String {
    return when (type) {
        ProjectType.BEER -> "Traditional beer brewing with grains, hops, and yeast. Perfect for ales, lagers, and specialty beers."
        ProjectType.MEAD -> "Honey-based fermented beverage. Create traditional meads, melomels (with fruit), or experimental varieties."
        ProjectType.WINE -> "Grape or fruit wine production. Track fermentation, aging, and clarification processes."
        ProjectType.CIDER -> "Apple or pear-based alcoholic beverages. Monitor fermentation and carbonation levels."
        ProjectType.KOMBUCHA -> "Fermented tea beverage with SCOBY. Track SCOBY health and fermentation progress."
        ProjectType.WATER_KEFIR -> "Probiotic fermented beverage using water kefir grains. Monitor grain health and flavoring."
        ProjectType.OTHER -> "Custom brewing project. Use for experimental fermentations or unique beverage types."
    }
}