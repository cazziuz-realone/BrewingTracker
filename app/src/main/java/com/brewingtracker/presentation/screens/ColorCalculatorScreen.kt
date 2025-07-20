package com.brewingtracker.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewingtracker.domain.calculator.BrewCalculator
import com.brewingtracker.domain.calculator.GrainAddition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorCalculatorScreen(
    onBackClick: () -> Unit = {}
) {
    var batchSize by remember { mutableStateOf("5.0") }
    var grains by remember { mutableStateOf(listOf<GrainAddition>()) }

    // New grain inputs
    var newGrainName by remember { mutableStateOf("") }
    var newGrainAmount by remember { mutableStateOf("") }
    var newGrainColor by remember { mutableStateOf("") }

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
                text = "Color Calculator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Batch Size Input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Batch Size",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = batchSize,
                    onValueChange = { batchSize = it },
                    label = { Text("Batch Size (gallons)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Add Grain Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Grain",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = newGrainName,
                    onValueChange = { newGrainName = it },
                    label = { Text("Grain Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newGrainAmount,
                        onValueChange = { newGrainAmount = it },
                        label = { Text("Amount (lbs)") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = newGrainColor,
                        onValueChange = { newGrainColor = it },
                        label = { Text("Color (°L)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try {
                            if (newGrainName.isNotEmpty() && newGrainAmount.isNotEmpty() && newGrainColor.isNotEmpty()) {
                                grains = grains + GrainAddition(
                                    name = newGrainName,
                                    amountLbs = newGrainAmount.toDouble(),
                                    colorLovibond = newGrainColor.toDouble()
                                )
                                newGrainName = ""
                                newGrainAmount = ""
                                newGrainColor = ""
                            }
                        } catch (e: Exception) {
                            // Handle invalid input
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Grain")
                }
            }
        }

        // Grain List
        if (grains.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Grain Bill",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    grains.forEachIndexed { index, grain ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = grain.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${grain.amountLbs} lbs • ${grain.colorLovibond}°L",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        grains = grains.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Results Section
        val results = remember(grains, batchSize) {
            try {
                if (grains.isNotEmpty() && batchSize.isNotEmpty()) {
                    val srm = BrewCalculator.calculateSRM(grains, batchSize.toDouble())
                    val ebc = BrewCalculator.srmToEBC(srm)
                    val description = BrewCalculator.getSRMDescription(srm)
                    val totalGrain = grains.sumOf { it.amountLbs }

                    mapOf(
                        "SRM" to String.format("%.1f", srm),
                        "EBC" to String.format("%.1f", ebc),
                        "Color" to description,
                        "Total Grain" to "${String.format("%.1f", totalGrain)} lbs"
                    )
                } else null
            } catch (e: Exception) { null }
        }

        if (results != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Color Results",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    results.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$key:",
                                fontWeight = FontWeight.Medium
                            )
                            Text(text = value)
                        }
                    }

                    // Color preview
                    val srmValue = results["SRM"]?.toDoubleOrNull() ?: 0.0
                    val colorPreview = getSRMColor(srmValue)

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                color = colorPreview,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        }
    }
}

// Helper function to get approximate SRM color
private fun getSRMColor(srm: Double): Color {
    return when {
        srm < 2 -> Color(0xFFFFF4C4)
        srm < 4 -> Color(0xFFFFE699)
        srm < 6 -> Color(0xFFFFD700)
        srm < 9 -> Color(0xFFFFBF00)
        srm < 18 -> Color(0xFFB8860B)
        srm < 35 -> Color(0xFF8B4513)
        else -> Color(0xFF2F1B14)
    }
}