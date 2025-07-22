package com.brewingtracker.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector

data class CalculatorItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val type: String
)

@Composable
fun CalculatorsScreen(
    onCalculatorClick: (String) -> Unit
) {
    val calculators = listOf(
        CalculatorItem(
            name = "ABV Calculator",
            description = "Calculate alcohol by volume",
            icon = Icons.Default.Percent,
            type = "abv"
        ),
        CalculatorItem(
            name = "IBU Calculator",
            description = "Calculate bitterness units",
            icon = Icons.Default.LocalDrink,
            type = "ibu"
        ),
        CalculatorItem(
            name = "SRM Calculator",
            description = "Calculate beer color",
            icon = Icons.Default.Palette,
            type = "srm"
        ),
        CalculatorItem(
            name = "Water Calculator",
            description = "Mash & sparge water",
            icon = Icons.Default.Water,
            type = "water"
        ),
        CalculatorItem(
            name = "Priming Sugar",
            description = "Calculate carbonation",
            icon = Icons.Default.BubbleChart,
            type = "priming"
        ),
        CalculatorItem(
            name = "Brix to SG",
            description = "Convert measurements",
            icon = Icons.Default.SwapHoriz,
            type = "brix"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Brewing Calculators",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(calculators) { calculator ->
                CalculatorCard(
                    calculator = calculator,
                    onClick = { onCalculatorClick(calculator.type) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorCard(
    calculator: CalculatorItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = calculator.icon,
                contentDescription = calculator.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = calculator.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Text(
                text = calculator.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}