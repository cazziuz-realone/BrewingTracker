package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.brewingtracker.data.database.entities.Project
import com.brewingtracker.data.database.entities.ProjectPhase
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
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val project by viewModel.getProjectById(projectId).collectAsState(initial = null)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        if (project != null) {
            val proj = project!!

            // Header
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

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Project"
                    )
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

            // Quick Actions
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
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onAddIngredientsClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ingredients")
                        }

                        OutlinedButton(
                            onClick = { /* TODO: Add gravity reading */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reading")
                        }

                        OutlinedButton(
                            onClick = { /* TODO: Add photo */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Camera, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Photo")
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
                progress = (currentIndex + 1).toFloat() / phases.size,
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