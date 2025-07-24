package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brewingtracker.data.database.entities.Project
import com.brewingtracker.data.database.entities.BeverageType
import com.brewingtracker.presentation.viewmodel.ProjectsViewModel
import com.brewingtracker.presentation.viewmodel.IngredientsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToProjects: () -> Unit,
    onNavigateToCalculators: () -> Unit,
    onNavigateToIngredients: () -> Unit = {},
    onNavigateToProjectDetail: (String) -> Unit = {},
    onNavigateToRecipeBuilder: () -> Unit = {}, // NEW: Recipe Builder navigation
    projectsViewModel: ProjectsViewModel = hiltViewModel(),
    ingredientsViewModel: IngredientsViewModel = hiltViewModel()
) {
    val activeProjects by projectsViewModel.allProjects.collectAsStateWithLifecycle()
    val favoriteProjects by projectsViewModel.favoriteProjects.collectAsStateWithLifecycle()
    val inStockIngredients by ingredientsViewModel.inStockIngredients.collectAsStateWithLifecycle()

    // FIXED: Use LazyColumn with proper padding to fix scrolling issues
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Brewing Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Overview Stats
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StatCard(
                        title = "Active Projects",
                        value = activeProjects.size.toString(),
                        icon = Icons.Default.Assignment,
                        onClick = onNavigateToProjects
                    )
                }
                item {
                    StatCard(
                        title = "Favorite Projects",
                        value = favoriteProjects.size.toString(),
                        icon = Icons.Default.Favorite,
                        onClick = onNavigateToProjects
                    )
                }
                item {
                    StatCard(
                        title = "In Stock",
                        value = inStockIngredients.size.toString(),
                        icon = Icons.Default.Inventory,
                        onClick = onNavigateToIngredients
                    )
                }
            }
        }

        // Recent Projects Section
        if (activeProjects.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Projects",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeProjects.take(3)) { project ->
                        RecentProjectCard(
                            project = project,
                            onClick = { onNavigateToProjectDetail(project.id) }
                        )
                    }
                }
            }
        }

        // Quick Actions Section
        item {
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // First row of quick actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "New Project",
                    subtitle = "Start brewing",
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProjects
                )

                // NEW: Recipe Builder Quick Action
                QuickActionCard(
                    title = "Recipe Builder",
                    subtitle = "Create recipes",
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToRecipeBuilder
                )
            }
        }

        // Second row of quick actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Calculators",
                    subtitle = "ABV, IBU, SRM",
                    icon = Icons.Default.Calculate,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCalculators
                )

                QuickActionCard(
                    title = "Ingredients",
                    subtitle = "Manage stock",
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToIngredients
                )
            }
        }

        // Third row - moved reminders here for better layout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Reminders",
                    subtitle = "Set alerts", 
                    icon = Icons.Default.Notifications,
                    modifier = Modifier.weight(1f),
                    onClick = { /* Navigate to reminders */ }
                )

                // Placeholder for future feature or empty space
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Project Type Distribution (if there are projects)
        if (activeProjects.isNotEmpty()) {
            item {
                Text(
                    text = "Project Types",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                val projectsByType = activeProjects.groupBy { it.type }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projectsByType.entries.toList()) { (type, projects) ->
                        AssistChip(
                            onClick = { /* Filter by type */ },
                            label = { 
                                Text("${type.displayName}: ${projects.size}")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = getBeverageTypeIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Add bottom padding to ensure content is not cut off
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(180.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getBeverageTypeIcon(project.type),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = project.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = project.currentPhase.displayName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Started ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(project.startDate))}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getBeverageTypeIcon(type: BeverageType): ImageVector {
    return when (type) {
        BeverageType.BEER -> Icons.Default.LocalBar
        BeverageType.MEAD -> Icons.Default.LocalFlorist  
        BeverageType.WINE -> Icons.Default.LocalDining   
        BeverageType.CIDER -> Icons.Default.Eco          
        BeverageType.KOMBUCHA -> Icons.Default.Science
        BeverageType.WATER_KEFIR -> Icons.Default.WaterDrop
        BeverageType.OTHER -> Icons.Default.Science
    }
}
