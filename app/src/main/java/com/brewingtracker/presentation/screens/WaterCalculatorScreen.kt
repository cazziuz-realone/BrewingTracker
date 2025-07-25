package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Thermostat
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
fun WaterCalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Local state for input fields
    var grainWeightText by remember { mutableStateOf("") }
    var mashRatioText by remember { mutableStateOf("") }
    var totalWaterText by remember { mutableStateOf("") }
    var grainAbsorptionText by remember { mutableStateOf("0.125") }
    var boilOffRateText by remember { mutableStateOf("1.25") }
    var boilTimeText by remember { mutableStateOf("1.0") }
    var grainTempText by remember { mutableStateOf("") }
    var targetMashTempText by remember { mutableStateOf("") }
    
    // Calculate water amounts when input fields are valid
    LaunchedEffect(grainWeightText, mashRatioText, totalWaterText, grainAbsorptionText, boilOffRateText, boilTimeText) {
        val grainWeight = grainWeightText.toDoubleOrNull()
        val mashRatio = mashRatioText.toDoubleOrNull()
        val totalWater = totalWaterText.toDoubleOrNull()
        val grainAbsorption = grainAbsorptionText.toDoubleOrNull()
        val boilOffRate = boilOffRateText.toDoubleOrNull()
        val boilTime = boilTimeText.toDoubleOrNull()
        
        if (grainWeight != null && grainWeight > 0 &&
            mashRatio != null && mashRatio > 0 &&
            totalWater != null && totalWater > 0 &&
            grainAbsorption != null && grainAbsorption > 0 &&
            boilOffRate != null && boilOffRate > 0 &&
            boilTime != null && boilTime > 0) {
            viewModel.calculateWaterAmounts(grainWeight, mashRatio, totalWater, grainAbsorption, boilOffRate, boilTime)
        }
    }
    
    // Calculate strike temperature when input fields are valid
    LaunchedEffect(grainTempText, targetMashTempText, mashRatioText) {
        val grainTemp = grainTempText.toDoubleOrNull()
        val targetMashTemp = targetMashTempText.toDoubleOrNull()
        val mashRatio = mashRatioText.toDoubleOrNull() ?: 1.25
        
        if (grainTemp != null && targetMashTemp != null && targetMashTemp > grainTemp) {
            viewModel.calculateStrikeTemperature(grainTemp, targetMashTemp, mashRatio)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Water Calculator") },
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
                        grainWeightText = ""
                        mashRatioText = ""
                        totalWaterText = ""
                        grainAbsorptionText = "0.125"
                        boilOffRateText = "1.25"
                        boilTimeText = "1.0"
                        grainTempText = ""
                        targetMashTempText = ""
                        viewModel.clearWaterResults() 
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
                        text = "About Water Calculations",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Calculate mash water, sparge water, and strike water temperature for all-grain brewing. " +
                                "Essential for proper mash chemistry and efficiency.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Basic Parameters
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Basic Parameters",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = grainWeightText,
                        onValueChange = { grainWeightText = it },
                        label = { Text("Grain Weight") },
                        placeholder = { Text("10.0") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Total grain weight in pounds") },
                        trailingIcon = { Text("lbs", style = MaterialTheme.typography.bodySmall) },
                        isError = grainWeightText.isNotBlank() && grainWeightText.toDoubleOrNull() == null
                    )

                    OutlinedTextField(
                        value = mashRatioText,
                        onValueChange = { mashRatioText = it },
                        label = { Text("Mash Ratio") },
                        placeholder = { Text("1.25") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Quarts of water per pound of grain (1.25-1.5)") },
                        trailingIcon = { Text("qt/lb", style = MaterialTheme.typography.bodySmall) },
                        isError = mashRatioText.isNotBlank() && mashRatioText.toDoubleOrNull() == null
                    )

                    OutlinedTextField(
                        value = totalWaterText,
                        onValueChange = { totalWaterText = it },
                        label = { Text("Total Water Needed") },
                        placeholder = { Text("7.5") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Total water for your batch size + losses") },
                        trailingIcon = { Text("gal", style = MaterialTheme.typography.bodySmall) },
                        isError = totalWaterText.isNotBlank() && totalWaterText.toDoubleOrNull() == null
                    )
                }
            }

            // Advanced Parameters
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Advanced Parameters",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = grainAbsorptionText,
                            onValueChange = { grainAbsorptionText = it },
                            label = { Text("Grain Absorption") },
                            placeholder = { Text("0.125") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            supportingText = { Text("gal/lb") },
                            isError = grainAbsorptionText.isNotBlank() && grainAbsorptionText.toDoubleOrNull() == null
                        )

                        OutlinedTextField(
                            value = boilOffRateText,
                            onValueChange = { boilOffRateText = it },
                            label = { Text("Boil-off Rate") },
                            placeholder = { Text("1.25") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            supportingText = { Text("gal/hr") },
                            isError = boilOffRateText.isNotBlank() && boilOffRateText.toDoubleOrNull() == null
                        )
                    }

                    OutlinedTextField(
                        value = boilTimeText,
                        onValueChange = { boilTimeText = it },
                        label = { Text("Boil Time") },
                        placeholder = { Text("1.0") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Boil time in hours") },
                        trailingIcon = { Text("hrs", style = MaterialTheme.typography.bodySmall) },
                        isError = boilTimeText.isNotBlank() && boilTimeText.toDoubleOrNull() == null
                    )
                }
            }

            // Water Amounts Results
            uiState.waterResult?.let { waterResult ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Water,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Water Amounts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Mash Water
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mash Water:",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.2f", waterResult.calculatedMashWater)} qt",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Sparge Water
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sparge Water:",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.2f", waterResult.calculatedSpargeWater)} gal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Total Water Summary
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Water:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.2f", waterResult.totalCalculatedWater)} gal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Strike Temperature Calculation
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Strike Water Temperature",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = grainTempText,
                            onValueChange = { grainTempText = it },
                            label = { Text("Grain Temp") },
                            placeholder = { Text("68") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            supportingText = { Text("°F") },
                            isError = grainTempText.isNotBlank() && grainTempText.toDoubleOrNull() == null
                        )

                        OutlinedTextField(
                            value = targetMashTempText,
                            onValueChange = { targetMashTempText = it },
                            label = { Text("Target Mash Temp") },
                            placeholder = { Text("152") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            supportingText = { Text("°F") },
                            isError = targetMashTempText.isNotBlank() && targetMashTempText.toDoubleOrNull() == null
                        )
                    }

                    // Strike Temperature Result
                    uiState.strikeTemperatureResult?.let { strikeResult ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Strike Water Temperature:",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "${String.format("%.1f", strikeResult.calculatedStrikeTemp)}°F",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
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
                        text = "💡 Water Brewing Tips",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val tips = listOf(
                        "Mash ratio: Thicker (1.25 qt/lb) for body, thinner (1.5 qt/lb) for efficiency",
                        "Grain absorption typically ranges from 0.1-0.15 gal/lb",
                        "Boil-off rates vary by system: electric (1-1.25 gal/hr), gas (1.5+ gal/hr)",
                        "Strike temp should be 8-15°F higher than target mash temp",
                        "Pre-heat your mash tun to avoid temperature drop",
                        "Check and adjust mash temp 10 minutes after dough-in"
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

            // Input validation errors
            val hasInputErrors = (grainWeightText.isNotBlank() && grainWeightText.toDoubleOrNull() == null) ||
                    (mashRatioText.isNotBlank() && mashRatioText.toDoubleOrNull() == null) ||
                    (totalWaterText.isNotBlank() && totalWaterText.toDoubleOrNull() == null) ||
                    (grainAbsorptionText.isNotBlank() && grainAbsorptionText.toDoubleOrNull() == null) ||
                    (boilOffRateText.isNotBlank() && boilOffRateText.toDoubleOrNull() == null) ||
                    (boilTimeText.isNotBlank() && boilTimeText.toDoubleOrNull() == null) ||
                    (grainTempText.isNotBlank() && grainTempText.toDoubleOrNull() == null) ||
                    (targetMashTempText.isNotBlank() && targetMashTempText.toDoubleOrNull() == null)

            if (hasInputErrors) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠️ Please enter valid values. All values must be numbers greater than 0, " +
                                "and mash ratio should be between 1.0-2.0 qt/lb.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Conversion Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📏 Quick Conversions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 1 gallon = 4 quarts = 8 pints = 16 cups\n" +
                                "• 1 quart = 0.25 gallons = 2 pints = 4 cups\n" +
                                "• Water weighs ~8.34 lbs per gallon\n" +
                                "• °C to °F: (°C × 9/5) + 32",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
