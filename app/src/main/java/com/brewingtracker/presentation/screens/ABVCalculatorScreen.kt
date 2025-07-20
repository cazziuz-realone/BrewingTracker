package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewingtracker.domain.calculator.BrewCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ABVCalculatorScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedMethod by remember { mutableStateOf(0) }

    // Gravity method inputs
    var originalGravity by remember { mutableStateOf("") }
    var finalGravity by remember { mutableStateOf("") }

    // Brix method inputs
    var originalBrix by remember { mutableStateOf("") }
    var finalBrix by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Calculators"
                )
            }
            Text(
                text = "ABV Calculator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Method Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Calculation Method",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedMethod = 0 },
                        label = { Text("Specific Gravity") },
                        selected = selectedMethod == 0,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { selectedMethod = 1 },
                        label = { Text("Brix") },
                        selected = selectedMethod == 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Input Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (selectedMethod == 0) {
                    // Specific Gravity Inputs
                    Text(
                        text = "Specific Gravity Method",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = originalGravity,
                        onValueChange = { originalGravity = it },
                        label = { Text("Original Gravity (e.g., 1.050)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = finalGravity,
                        onValueChange = { finalGravity = it },
                        label = { Text("Final Gravity (e.g., 1.010)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Brix Inputs
                    Text(
                        text = "Brix Method",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = originalBrix,
                        onValueChange = { originalBrix = it },
                        label = { Text("Original Brix (°Bx)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = finalBrix,
                        onValueChange = { finalBrix = it },
                        label = { Text("Final Brix (°Bx)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Results Section
        val abv = remember(selectedMethod, originalGravity, finalGravity, originalBrix, finalBrix) {
            try {
                if (selectedMethod == 0) {
                    if (originalGravity.isNotEmpty() && finalGravity.isNotEmpty()) {
                        BrewCalculator.calculateABVFromGravity(
                            originalGravity.toDouble(),
                            finalGravity.toDouble()
                        )
                    } else null
                } else {
                    if (originalBrix.isNotEmpty() && finalBrix.isNotEmpty()) {
                        BrewCalculator.calculateABVFromBrix(
                            originalBrix.toDouble(),
                            finalBrix.toDouble()
                        )
                    } else null
                }
            } catch (e: Exception) { null }
        }

        if (abv != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Alcohol by Volume",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${String.format("%.2f", abv)}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    val category = when {
                        abv < 3.5 -> "Low Alcohol"
                        abv < 5.5 -> "Standard Strength"
                        abv < 8.0 -> "Strong"
                        abv < 12.0 -> "Very Strong"
                        else -> "Extreme"
                    }

                    Text(
                        text = category,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}