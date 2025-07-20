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
import com.brewingtracker.domain.calculator.SugarType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimingSugarCalculatorScreen(
    onBackClick: () -> Unit = {}
) {
    var co2Volumes by remember { mutableStateOf("2.4") }
    var temperature by remember { mutableStateOf("68") }
    var batchSize by remember { mutableStateOf("5.0") }
    var selectedSugar by remember { mutableStateOf(SugarType.CORN_SUGAR) }
    var expanded by remember { mutableStateOf(false) }

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
                text = "Priming Sugar Calculator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Input Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Carbonation Parameters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = co2Volumes,
                    onValueChange = { co2Volumes = it },
                    label = { Text("Target CO₂ Volumes") },
                    supportingText = {
                        Text("Beer: 2.2-2.6, Lager: 2.4-2.8, Wheat: 3.0-4.0")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = temperature,
                        onValueChange = { temperature = it },
                        label = { Text("Beverage Temp (°F)") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = batchSize,
                        onValueChange = { batchSize = it },
                        label = { Text("Batch Size (gal)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sugar Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedSugar.displayName,
                        onValueChange = { },
                        label = { Text("Sugar Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SugarType.values().forEach { sugar ->
                            DropdownMenuItem(
                                text = { Text(sugar.displayName) },
                                onClick = {
                                    selectedSugar = sugar
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Results Section
        val sugarAmount = remember(co2Volumes, temperature, batchSize, selectedSugar) {
            try {
                if (co2Volumes.isNotEmpty() && temperature.isNotEmpty() && batchSize.isNotEmpty()) {
                    BrewCalculator.calculatePrimingSugar(
                        co2Volumes.toDouble(),
                        temperature.toDouble(),
                        selectedSugar,
                        batchSize.toDouble()
                    )
                } else null
            } catch (e: Exception) { null }
        }

        if (sugarAmount != null) {
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
                        text = "Priming Sugar Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "${String.format("%.1f", sugarAmount)} grams",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "(${String.format("%.2f", sugarAmount / 28.35)} oz)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Text(
                            text = "Instructions:",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "1. Dissolve sugar in 1-2 cups of boiling water",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "2. Cool the solution to room temperature",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "3. Add to bottling bucket before transferring beer",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "4. Mix gently and bottle immediately",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // CO2 Style Guide
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CO₂ Style Guide",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val styleGuide = listOf(
                    "British Ales" to "1.8 - 2.3 volumes",
                    "American Ales" to "2.2 - 2.6 volumes",
                    "German Lagers" to "2.4 - 2.8 volumes",
                    "Belgian Ales" to "2.8 - 3.5 volumes",
                    "Wheat Beers" to "3.0 - 4.0 volumes",
                    "Barleywines" to "1.8 - 2.3 volumes"
                )

                styleGuide.forEach { (style, volumes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = style,
                            fontSize = 14.sp
                        )
                        Text(
                            text = volumes,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}