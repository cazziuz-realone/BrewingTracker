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
fun BrixConverterScreen(
    onBackClick: () -> Unit = {}
) {
    var brixInput by remember { mutableStateOf("") }
    var sgInput by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("68") }
    var showTempCorrection by remember { mutableStateOf(false) }

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
                text = "Brix Converter",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Conversion Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Brix to Specific Gravity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = brixInput,
                    onValueChange = { brixInput = it },
                    label = { Text("Brix (°Bx)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                val sgFromBrix = remember(brixInput) {
                    try {
                        if (brixInput.isNotEmpty()) {
                            BrewCalculator.brixToSpecificGravity(brixInput.toDouble())
                        } else null
                    } catch (e: Exception) { null }
                }

                if (sgFromBrix != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Specific Gravity: ${String.format("%.3f", sgFromBrix)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Reverse Conversion
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Specific Gravity to Brix",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = sgInput,
                    onValueChange = { sgInput = it },
                    label = { Text("Specific Gravity (e.g., 1.050)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                val brixFromSG = remember(sgInput) {
                    try {
                        if (sgInput.isNotEmpty()) {
                            BrewCalculator.specificGravityToBrix(sgInput.toDouble())
                        } else null
                    } catch (e: Exception) { null }
                }

                if (brixFromSG != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Brix: ${String.format("%.1f", brixFromSG)}°Bx",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Temperature Correction
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Temperature Correction",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = showTempCorrection,
                        onCheckedChange = { showTempCorrection = it }
                    )
                }

                if (showTempCorrection) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = temperature,
                        onValueChange = { temperature = it },
                        label = { Text("Sample Temperature (°F)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    val correctedSG = remember(sgInput, temperature) {
                        try {
                            if (sgInput.isNotEmpty() && temperature.isNotEmpty()) {
                                BrewCalculator.temperatureCorrection(
                                    sgInput.toDouble(),
                                    temperature.toDouble()
                                )
                            } else null
                        } catch (e: Exception) { null }
                    }

                    if (correctedSG != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Temperature Corrected SG:",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = String.format("%.3f", correctedSG),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}