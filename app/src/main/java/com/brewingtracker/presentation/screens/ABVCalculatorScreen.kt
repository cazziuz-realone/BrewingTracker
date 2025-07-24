package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brewingtracker.presentation.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ABVCalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    // Local state for input fields
    var originalGravityText by remember { mutableStateOf("") }
    var finalGravityText by remember { mutableStateOf("") }
    
    // Calculate ABV when both fields are valid
    LaunchedEffect(originalGravityText, finalGravityText) {
        val og = originalGravityText.toDoubleOrNull()
        val fg = finalGravityText.toDoubleOrNull()
        if (og != null && fg != null && og > fg && og >= 1.0 && fg >= 1.0) {
            viewModel.calculateAlcoholByVolume(og, fg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ABV Calculator") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        originalGravityText = ""
                        finalGravityText = ""
                        viewModel.clearResults()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "About ABV Calculation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Calculate Alcohol by Volume using specific gravity readings. " +
                                "Take your Original Gravity (OG) before fermentation and Final Gravity (FG) after fermentation completes.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Input Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Gravity Readings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = originalGravityText,
                        onValueChange = { originalGravityText = it },
                        label = { Text("Original Gravity (OG)") },
                        placeholder = { Text("1.050") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Gravity before fermentation") },
                        isError = originalGravityText.isNotBlank() && originalGravityText.toDoubleOrNull() == null
                    )

                    OutlinedTextField(
                        value = finalGravityText,
                        onValueChange = { finalGravityText = it },
                        label = { Text("Final Gravity (FG)") },
                        placeholder = { Text("1.010") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Gravity after fermentation") },
                        isError = finalGravityText.isNotBlank() && finalGravityText.toDoubleOrNull() == null
                    )
                }
            }

            // Results Section
            uiState.abvResult?.let { abvResult ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Alcohol by Volume",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${String.format("%.2f", abvResult.alcoholByVolume)}%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Additional calculations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Apparent Attenuation:",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.1f", abvResult.attenuation)}%",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Strength category
                        val category = when {
                            abvResult.alcoholByVolume < 3.5 -> "Low Alcohol" to "Light and sessionable"
                            abvResult.alcoholByVolume < 5.5 -> "Standard Strength" to "Most ales and lagers"
                            abvResult.alcoholByVolume < 8.0 -> "Strong" to "IPAs, strong ales"
                            abvResult.alcoholByVolume < 12.0 -> "Very Strong" to "Barleywines, Belgian strongs"
                            else -> "Extreme" to "Imperial styles, spirits"
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text(category.first) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = category.second,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Brewing Tips Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Brewing Tips",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val tips = listOf(
                        "Take gravity readings at the same temperature for accuracy",
                        "Wait for fermentation to completely finish before taking FG",
                        "Typical beer attenuation ranges from 65-85%",
                        "Higher OG usually results in higher ABV",
                        "Consider temperature correction for hot samples"
                    )
                    
                    tips.forEach { tip ->
                        Text(
                            text = "• $tip",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            // Error handling
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠️ $error",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Input validation error
            if ((originalGravityText.isNotBlank() && originalGravityText.toDoubleOrNull() == null) ||
                (finalGravityText.isNotBlank() && finalGravityText.toDoubleOrNull() == null) ||
                (originalGravityText.toDoubleOrNull() != null && finalGravityText.toDoubleOrNull() != null &&
                 originalGravityText.toDouble() <= finalGravityText.toDouble())) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠️ Please enter valid gravity values (e.g., 1.050, 1.010). " +
                                "Original gravity should be higher than final gravity.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}