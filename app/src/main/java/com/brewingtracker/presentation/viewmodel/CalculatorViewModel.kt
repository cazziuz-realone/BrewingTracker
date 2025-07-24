package com.brewingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()
    
    fun calculateAlcoholByVolume(originalGravity: Double, finalGravity: Double) {
        viewModelScope.launch {
            try {
                if (originalGravity <= finalGravity) {
                    _uiState.value = _uiState.value.copy(
                        error = "Original gravity must be higher than final gravity",
                        abvResult = null
                    )
                    return@launch
                }
                
                // Standard ABV formula: (OG - FG) * 131.25
                val abv = (originalGravity - finalGravity) * 131.25
                
                _uiState.value = _uiState.value.copy(
                    abvResult = ABVResult(
                        alcoholByVolume = abv,
                        originalGravity = originalGravity,
                        finalGravity = finalGravity,
                        attenuation = ((originalGravity - finalGravity) / (originalGravity - 1.0)) * 100
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error calculating ABV: ${e.message}",
                    abvResult = null
                )
            }
        }
    }
    
    fun calculateGravityFromBrix(brix: Double) {
        viewModelScope.launch {
            try {
                // Convert Brix to Specific Gravity using: SG = (Brix / (258.6-((Brix / 258.2)*227.1))) + 1
                val specificGravity = (brix / (258.6 - ((brix / 258.2) * 227.1))) + 1
                
                _uiState.value = _uiState.value.copy(
                    brixResult = BrixResult(
                        brix = brix,
                        specificGravity = specificGravity,
                        plato = brix // Brix ≈ Plato for practical purposes
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error converting Brix: ${e.message}",
                    brixResult = null
                )
            }
        }
    }
    
    fun calculatePotentialAlcohol(originalGravity: Double) {
        viewModelScope.launch {
            try {
                // Assuming complete fermentation to 1.000
                val potentialABV = (originalGravity - 1.000) * 131.25
                
                _uiState.value = _uiState.value.copy(
                    potentialAlcoholResult = PotentialAlcoholResult(
                        originalGravity = originalGravity,
                        potentialABV = potentialABV,
                        sugarContent = (originalGravity - 1.000) * 1000 // gravity points
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error calculating potential alcohol: ${e.message}",
                    potentialAlcoholResult = null
                )
            }
        }
    }
    
    fun calculateHydrometer(temperature: Double, reading: Double, calibrationTemp: Double = 20.0) {
        viewModelScope.launch {
            try {
                // Temperature correction formula for hydrometers
                // Corrected SG = Reading + (0.000013 * (Temperature - Calibration Temperature) * Reading)
                val temperatureDifference = temperature - calibrationTemp
                val correctedGravity = reading + (0.000013 * temperatureDifference * reading)
                
                _uiState.value = _uiState.value.copy(
                    hydrometerResult = HydrometerResult(
                        measuredGravity = reading,
                        temperature = temperature,
                        calibrationTemperature = calibrationTemp,
                        correctedGravity = correctedGravity,
                        temperatureCorrection = correctedGravity - reading
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error calculating hydrometer correction: ${e.message}",
                    hydrometerResult = null
                )
            }
        }
    }
    
    fun calculateDilution(currentVolume: Double, currentABV: Double, targetABV: Double) {
        viewModelScope.launch {
            try {
                if (targetABV >= currentABV) {
                    _uiState.value = _uiState.value.copy(
                        error = "Target ABV must be lower than current ABV for dilution",
                        dilutionResult = null
                    )
                    return@launch
                }
                
                // Calculate water needed: Water = (Current Volume * Current ABV / Target ABV) - Current Volume
                val finalVolume = (currentVolume * currentABV) / targetABV
                val waterToAdd = finalVolume - currentVolume
                
                _uiState.value = _uiState.value.copy(
                    dilutionResult = DilutionResult(
                        currentVolume = currentVolume,
                        currentABV = currentABV,
                        targetABV = targetABV,
                        waterToAdd = waterToAdd,
                        finalVolume = finalVolume
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error calculating dilution: ${e.message}",
                    dilutionResult = null
                )
            }
        }
    }
    
    fun calculateCarbonation(temperature: Double, desiredVolumes: Double) {
        viewModelScope.launch {
            try {
                // Calculate CO2 pressure needed
                // Using formula: Pressure = (Desired CO2 - Residual CO2) / Solubility
                
                // Residual CO2 at given temperature (approximate)
                val residualCO2 = 0.5 + (0.01 * temperature)
                
                // CO2 solubility factor (temperature dependent)
                val solubilityFactor = 0.5 + (0.02 * temperature)
                
                val pressureNeeded = (desiredVolumes - residualCO2) / solubilityFactor
                
                // Calculate priming sugar needed (corn sugar)
                // Approximately 0.5 oz per gallon per volume of CO2
                val primingSugar = desiredVolumes * 0.5
                
                _uiState.value = _uiState.value.copy(
                    carbonationResult = CarbonationResult(
                        temperature = temperature,
                        desiredVolumes = desiredVolumes,
                        pressureNeeded = max(0.0, pressureNeeded),
                        primingSugarOzPerGallon = primingSugar,
                        residualCO2 = residualCO2
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error calculating carbonation: ${e.message}",
                    carbonationResult = null
                )
            }
        }
    }
    
    fun calculateYeastStarter(targetCells: Double, viability: Double, packagedCells: Double) {
        viewModelScope.launch {
            try {
                // Calculate how many packages needed
                val viableCellsPerPackage = packagedCells * (viability / 100.0)
                val packagesNeeded = ceil(targetCells / viableCellsPerPackage)
                
                // Calculate starter size if needed
                val totalViableCells = packagesNeeded * viableCellsPerPackage
                val needsStarter = totalViableCells < targetCells
                
                val starterSize = if (needsStarter) {
                    // Approximate starter size calculation
                    val cellGrowthNeeded = targetCells - totalViableCells
                    cellGrowthNeeded * 0.1 // Rough calculation: 0.1L per billion cells
                } else {
                    0.0
                }
                
                _uiState.value = _uiState.value.copy(
                    yeastResult = YeastResult(
                        targetCells = targetCells,
                        viability = viability,
                        packagedCells = packagedCells,
                        packagesNeeded = packagesNeeded.toInt(),
                        needsStarter = needsStarter,
                        starterSizeLiters = starterSize
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error calculating yeast requirements: ${e.message}",
                    yeastResult = null
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearResults() {
        _uiState.value = CalculatorUiState()
    }
}

data class CalculatorUiState(
    val abvResult: ABVResult? = null,
    val brixResult: BrixResult? = null,
    val potentialAlcoholResult: PotentialAlcoholResult? = null,
    val hydrometerResult: HydrometerResult? = null,
    val dilutionResult: DilutionResult? = null,
    val carbonationResult: CarbonationResult? = null,
    val yeastResult: YeastResult? = null,
    val error: String? = null
)

data class ABVResult(
    val alcoholByVolume: Double,
    val originalGravity: Double,
    val finalGravity: Double,
    val attenuation: Double
)

data class BrixResult(
    val brix: Double,
    val specificGravity: Double,
    val plato: Double
)

data class PotentialAlcoholResult(
    val originalGravity: Double,
    val potentialABV: Double,
    val sugarContent: Double
)

data class HydrometerResult(
    val measuredGravity: Double,
    val temperature: Double,
    val calibrationTemperature: Double,
    val correctedGravity: Double,
    val temperatureCorrection: Double
)

data class DilutionResult(
    val currentVolume: Double,
    val currentABV: Double,
    val targetABV: Double,
    val waterToAdd: Double,
    val finalVolume: Double
)

data class CarbonationResult(
    val temperature: Double,
    val desiredVolumes: Double,
    val pressureNeeded: Double,
    val primingSugarOzPerGallon: Double,
    val residualCO2: Double
)

data class YeastResult(
    val targetCells: Double,
    val viability: Double,
    val packagedCells: Double,
    val packagesNeeded: Int,
    val needsStarter: Boolean,
    val starterSizeLiters: Double
)
