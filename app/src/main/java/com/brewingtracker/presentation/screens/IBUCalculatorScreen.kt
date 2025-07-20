package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewingtracker.domain.calculator.BrewCalculator
import com.brewingtracker.domain.calculator.HopAddition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IBUCalculatorScreen(
    onBackClick: () -> Unit = {}
) {
    var batchSize by remember { mutableStateOf("5.0") }
    var originalGravity by remember { mutableStateOf("1.050") }
    var hops by remember { mutableStateOf(listOf<HopAddition>()) }

    // New hop inputs
    var newHopName by remember { mutableStateOf("") }
    var newHopAmount by remember { mutableStateOf("") }
    var newHopAlpha by remember { mutableStateOf("") }
    var newHopTime by remember { mutableStateOf("60") }

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
                text = "IBU Calculator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Batch Parameters
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Batch Parameters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = batchSize,
                        onValueChange = { batchSize = it },
                        label = { Text("Batch Size (gal)") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = originalGravity,
                        onValueChange = { originalGravity = it },
                        label = { Text("Original Gravity") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Add Hop Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Hop Addition",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = newHopName,
                    onValueChange = { newHopName = it },
                    label = { Text("Hop Variety") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newHopAmount,
                        onValueChange = { newHopAmount = it },
                        label = { Text("Amount (oz)") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = newHopAlpha,
                        onValueChange = { newHopAlpha = it },
                        label = { Text("Alpha Acids (%)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newHopTime,
                    onValueChange = { newHopTime = it },
                    label = { Text("Boil Time (minutes)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Button(
                    onClick = {
                        try {
                            if (newHopName.isNotEmpty() && newHopAmount.isNotEmpty() &&
                                newHopAlpha.isNotEmpty() && newHopTime.isNotEmpty()) {
                                hops = hops + HopAddition(
                                    name = newHopName,
                                    amountOz = newHopAmount.toDouble(),
                                    alphaAcid = newHopAlpha.toDouble(),
                                    boilTime = newHopTime.toInt()
                                )
                                newHopName = ""
                                newHopAmount = ""
                                newHopAlpha = ""
                                newHopTime = "60"
                            }
                        } catch (e: Exception) {
                            // Handle invalid input
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Hop")
                }
            }
        }

        // Hop List
        if (hops.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hop Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    hops.forEachIndexed { index, hop ->
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
                                        text = hop.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${hop.amountOz} oz • ${hop.alphaAcid}% AA • ${hop.boilTime} min",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        hops = hops.filterIndexed { i, _ -> i != index }
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
        val results = remember(hops, batchSize, originalGravity) {
            try {
                if (hops.isNotEmpty() && batchSize.isNotEmpty() && originalGravity.isNotEmpty()) {
                    val ibu = BrewCalculator.calculateIBU(hops, batchSize.toDouble(), originalGravity.toDouble())
                    val category = BrewCalculator.getIBUCategory(ibu)

                    mapOf(
                        "Total IBU" to String.format("%.1f", ibu),
                        "Bitterness" to category,
                        "Hop Additions" to hops.size.toString()
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "IBU Results",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = results["Total IBU"] ?: "0",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "International Bitterness Units",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = results["Bitterness"] ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}