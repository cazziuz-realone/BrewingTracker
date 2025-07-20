package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brewingtracker.data.database.entities.*
import com.brewingtracker.presentation.viewmodel.CreateProjectViewModel
import com.brewingtracker.presentation.viewmodel.ProjectIngredientUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    onNavigateBack: () -> Unit,
    onProjectCreated: (String) -> Unit,
    viewModel: CreateProjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableIngredients by viewModel.availableIngredients.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createProject { projectId ->
                                onProjectCreated(projectId)
                            }
                        },
                        enabled = uiState.isValidProject
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
                ProjectBasicInfoSection(
                    name = uiState.name,
                    onNameChange = viewModel::updateName,
                    type = uiState.type,
                    onTypeChange = viewModel::updateType,
                    batchSize = uiState.batchSize,
                    onBatchSizeChange = viewModel::updateBatchSize
                )
            }

            // Target Parameters
            item {
                TargetParametersSection(
                    targetOG = uiState.targetOG,
                    onTargetOGChange = viewModel::updateTargetOG,
                    targetFG = uiState.targetFG,
                    onTargetFGChange = viewModel::updateTargetFG,
                    targetABV = uiState.targetABV,
                    onTargetABVChange = viewModel::updateTargetABV
                )
            }

            // Notes Section
            item {
                NotesSection(
                    notes = uiState.notes,
                    onNotesChange = viewModel::updateNotes
                )
            }
        }
    }
}

@Composable
private fun ProjectBasicInfoSection(
    name: String,
    onNameChange: (String) -> Unit,
    type: BeverageType,
    onTypeChange: (BeverageType) -> Unit,
    batchSize: String,
    onBatchSizeChange: (String) -> Unit
) {
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
                value = name,
                onValueChange = onNameChange,
                label = { Text("Project Name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Summer IPA, Honey Mead") }
            )

            // Beverage Type Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = type.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Beverage Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    BeverageType.values().forEach { beverageType ->
                        DropdownMenuItem(
                            text = { Text(beverageType.displayName) },
                            onClick = {
                                onTypeChange(beverageType)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = batchSize,
                onValueChange = onBatchSizeChange,
                label = { Text("Batch Size (gallons)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text("5.0") }
            )
        }
    }
}

@Composable
private fun TargetParametersSection(
    targetOG: String,
    onTargetOGChange: (String) -> Unit,
    targetFG: String,
    onTargetFGChange: (String) -> Unit,
    targetABV: String,
    onTargetABVChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Target Parameters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = targetOG,
                    onValueChange = onTargetOGChange,
                    label = { Text("Target OG") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("1.050") }
                )

                OutlinedTextField(
                    value = targetFG,
                    onValueChange = onTargetFGChange,
                    label = { Text("Target FG") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("1.010") }
                )

                OutlinedTextField(
                    value = targetABV,
                    onValueChange = onTargetABVChange,
                    label = { Text("Target ABV%") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("5.2") }
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Project Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Add brewing notes, recipe adjustments, etc.") }
            )
        }
    }
}