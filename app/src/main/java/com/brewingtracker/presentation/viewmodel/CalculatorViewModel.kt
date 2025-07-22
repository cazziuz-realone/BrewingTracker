package com.brewingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.brewingtracker.domain.calculator.BrewingCalculations
import com.brewingtracker.domain.calculator.SugarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    // ABV Calculator State
    private val _abvState = MutableStateFlow(ABVCalculatorState())
    val abvState: StateFlow<ABVCalculatorState> = _abvState.asStateFlow()

    // IBU Calculator State
    private val _ibuState = MutableStateFlow(IBUCalculatorState())
    val ibuState: StateFlow<IBUCalculatorState> = _ibuState.asStateFlow()

    // SRM Calculator State
    private val _srmState = MutableStateFlow(SRMCalculatorState())
    val srmState: StateFlow<SRMCalculatorState> = _srmState.asStateFlow()

    // Priming Sugar Calculator State
    private val _primingState = MutableStateFlow(PrimingCalculatorState())
    val primingState: StateFlow<PrimingCalculatorState> = _primingState.asStateFlow()

    // Brix Converter State
    private val _brixState = MutableStateFlow(BrixConverterState())
    val brixState: StateFlow<BrixConverterState> = _brixState.asStateFlow()

    // Water Calculator State
    private val _waterState = MutableStateFlow(WaterCalculatorState())
    val waterState: StateFlow<WaterCalculatorState> = _waterState.asStateFlow()

    // ==========================================
    // ABV CALCULATOR FUNCTIONS
    // ==========================================

    fun updateOriginalGravity(value: String) {
        _abvState.value = _abvState.value.copy(originalGravity = value)
        calculateABV()
    }

    fun updateFinalGravity(value: String) {
        _abvState.value = _abvState.value.copy(finalGravity = value)
        calculateABV()
    }

    private fun calculateABV() {
        val state = _abvState.value
        val og = state.originalGravity.toDoubleOrNull()
        val fg = state.finalGravity.toDoubleOrNull()

        if (og != null && fg != null && og > fg && og >= 1.0 && fg >= 1.0) {
            val abv = BrewingCalculations.calculateABVWithCorrection(og, fg)
            val attenuation = BrewingCalculations.calculateAttenuation(og, fg)
            _abvState.value = state.copy(
                calculatedABV = abv,
                attenuation = attenuation,
                isValid = true
            )
        } else {
            _abvState.value = state.copy(
                calculatedABV = null,
                attenuation = null,
                isValid = false
            )
        }
    }

    // ==========================================
    // IBU CALCULATOR FUNCTIONS
    // ==========================================

    fun updateAlphaAcid(value: String) {
        _ibuState.value = _ibuState.value.copy(alphaAcid = value)
        calculateIBU()
    }

    fun updateHopWeight(value: String) {
        _ibuState.value = _ibuState.value.copy(hopWeight = value)
        calculateIBU()
    }

    fun updateBatchSize(value: String) {
        _ibuState.value = _ibuState.value.copy(batchSize = value)
        calculateIBU()
    }

    fun updateBoilTime(value: String) {
        _ibuState.value = _ibuState.value.copy(boilTime = value)
        calculateIBU()
    }

    fun updateBoilGravity(value: String) {
        _ibuState.value = _ibuState.value.copy(boilGravity = value)
        calculateIBU()
    }

    private fun calculateIBU() {
        val state = _ibuState.value
        val alpha = state.alphaAcid.toDoubleOrNull()
        val weight = state.hopWeight.toDoubleOrNull()
        val batchSize = state.batchSize.toDoubleOrNull()
        val boilTime = state.boilTime.toIntOrNull()
        val gravity = state.boilGravity.toDoubleOrNull()

        if (alpha != null && weight != null && batchSize != null && 
            boilTime != null && gravity != null &&
            alpha > 0 && weight > 0 && batchSize > 0 && boilTime >= 0 && gravity >= 1.0) {
            
            val ibu = BrewingCalculations.calculateIBU(alpha, weight, batchSize, boilTime, gravity)
            _ibuState.value = state.copy(
                calculatedIBU = ibu,
                isValid = true
            )
        } else {
            _ibuState.value = state.copy(
                calculatedIBU = null,
                isValid = false
            )
        }
    }

    // ==========================================
    // PRIMING SUGAR CALCULATOR FUNCTIONS
    // ==========================================

    fun updateBeerVolume(value: String) {
        _primingState.value = _primingState.value.copy(beerVolume = value)
        calculatePrimingSugar()
    }

    fun updateCurrentTemp(value: String) {
        _primingState.value = _primingState.value.copy(currentTemp = value)
        calculatePrimingSugar()
    }

    fun updateTargetCO2(value: String) {
        _primingState.value = _primingState.value.copy(targetCO2 = value)
        calculatePrimingSugar()
    }

    fun updateSugarType(sugarType: SugarType) {
        _primingState.value = _primingState.value.copy(sugarType = sugarType)
        calculatePrimingSugar()
    }

    private fun calculatePrimingSugar() {
        val state = _primingState.value
        val volume = state.beerVolume.toDoubleOrNull()
        val temp = state.currentTemp.toDoubleOrNull()
        val co2 = state.targetCO2.toDoubleOrNull()

        if (volume != null && temp != null && co2 != null &&
            volume > 0 && temp > 0 && co2 > 0) {
            
            val sugar = BrewingCalculations.calculatePrimingSugar(volume, temp, co2, state.sugarType)
            _primingState.value = state.copy(
                calculatedSugar = sugar,
                isValid = true
            )
        } else {
            _primingState.value = state.copy(
                calculatedSugar = null,
                isValid = false
            )
        }
    }

    // ==========================================
    // BRIX CONVERTER FUNCTIONS
    // ==========================================

    fun updateBrixValue(value: String) {
        _brixState.value = _brixState.value.copy(brixValue = value)
        convertBrixToSG()
    }

    fun updateSGValue(value: String) {
        _brixState.value = _brixState.value.copy(sgValue = value)
        convertSGToBrix()
    }

    private fun convertBrixToSG() {
        val brix = _brixState.value.brixValue.toDoubleOrNull()
        if (brix != null && brix >= 0) {
            val sg = BrewingCalculations.brixToSpecificGravity(brix)
            _brixState.value = _brixState.value.copy(
                convertedSG = String.format("%.3f", sg),
                isValid = true
            )
        } else {
            _brixState.value = _brixState.value.copy(
                convertedSG = "",
                isValid = false
            )
        }
    }

    private fun convertSGToBrix() {
        val sg = _brixState.value.sgValue.toDoubleOrNull()
        if (sg != null && sg >= 1.0) {
            val brix = BrewingCalculations.specificGravityToBrix(sg)
            _brixState.value = _brixState.value.copy(
                convertedBrix = String.format("%.1f", brix),
                isValid = true
            )
        } else {
            _brixState.value = _brixState.value.copy(
                convertedBrix = "",
                isValid = false
            )
        }
    }

    // ==========================================
    // WATER CALCULATOR FUNCTIONS
    // ==========================================

    fun updateGrainWeight(value: String) {
        _waterState.value = _waterState.value.copy(grainWeight = value)
        calculateWaterAmounts()
    }

    fun updateMashRatio(value: String) {
        _waterState.value = _waterState.value.copy(mashRatio = value)
        calculateWaterAmounts()
    }

    fun updateTotalWater(value: String) {
        _waterState.value = _waterState.value.copy(totalWater = value)
        calculateWaterAmounts()
    }

    fun updateGrainAbsorption(value: String) {
        _waterState.value = _waterState.value.copy(grainAbsorption = value)
        calculateWaterAmounts()
    }

    fun updateBoilOffRate(value: String) {
        _waterState.value = _waterState.value.copy(boilOffRate = value)
        calculateWaterAmounts()
    }

    // FIXED: Renamed from updateBoilTime to updateWaterBoilTime to avoid conflicts
    fun updateWaterBoilTime(value: String) {
        _waterState.value = _waterState.value.copy(boilTime = value)
        calculateWaterAmounts()
    }

    fun updateGrainTemp(value: String) {
        _waterState.value = _waterState.value.copy(grainTemp = value)
        calculateStrikeTemp()
    }

    fun updateTargetMashTemp(value: String) {
        _waterState.value = _waterState.value.copy(targetMashTemp = value)
        calculateStrikeTemp()
    }

    private fun calculateWaterAmounts() {
        val state = _waterState.value
        val grainWeight = state.grainWeight.toDoubleOrNull()
        val mashRatio = state.mashRatio.toDoubleOrNull()
        val totalWater = state.totalWater.toDoubleOrNull()
        val grainAbsorption = state.grainAbsorption.toDoubleOrNull()
        val boilOffRate = state.boilOffRate.toDoubleOrNull()
        val boilTime = state.boilTime.toDoubleOrNull()

        if (grainWeight != null && grainWeight > 0 && mashRatio != null && mashRatio > 0) {
            val mashWater = BrewingCalculations.calculateMashWater(grainWeight, mashRatio)

            val spargeWater = if (totalWater != null && totalWater > 0 &&
                grainAbsorption != null && grainAbsorption > 0 &&
                boilOffRate != null && boilOffRate > 0 &&
                boilTime != null && boilTime > 0) {
                BrewingCalculations.calculateSpargeWater(
                    totalWater, mashWater, grainWeight, grainAbsorption, boilOffRate, boilTime
                )
            } else null

            _waterState.value = state.copy(
                calculatedMashWater = mashWater,
                calculatedSpargeWater = spargeWater,
                isValid = true
            )
        } else {
            _waterState.value = state.copy(
                calculatedMashWater = null,
                calculatedSpargeWater = null,
                isValid = false
            )
        }
    }

    private fun calculateStrikeTemp() {
        val state = _waterState.value
        val grainTemp = state.grainTemp.toDoubleOrNull()
        val targetMashTemp = state.targetMashTemp.toDoubleOrNull()
        val mashRatio = state.mashRatio.toDoubleOrNull()

        if (grainTemp != null && targetMashTemp != null && mashRatio != null &&
            grainTemp > 0 && targetMashTemp > grainTemp && mashRatio > 0) {
            
            val strikeTemp = BrewingCalculations.calculateStrikeWaterTemp(
                grainTemp, targetMashTemp, mashRatio
            )
            
            _waterState.value = state.copy(
                calculatedStrikeTemp = strikeTemp
            )
        } else {
            _waterState.value = state.copy(
                calculatedStrikeTemp = null
            )
        }
    }

    // Reset functions
    fun resetABVCalculator() {
        _abvState.value = ABVCalculatorState()
    }

    fun resetIBUCalculator() {
        _ibuState.value = IBUCalculatorState()
    }

    fun resetPrimingCalculator() {
        _primingState.value = PrimingCalculatorState()
    }

    fun resetBrixConverter() {
        _brixState.value = BrixConverterState()
    }

    fun resetWaterCalculator() {
        _waterState.value = WaterCalculatorState()
    }
}

// ==========================================
// STATE DATA CLASSES
// ==========================================

data class ABVCalculatorState(
    val originalGravity: String = "",
    val finalGravity: String = "",
    val calculatedABV: Double? = null,
    val attenuation: Double? = null,
    val isValid: Boolean = false
)

data class IBUCalculatorState(
    val alphaAcid: String = "",
    val hopWeight: String = "",
    val batchSize: String = "",
    val boilTime: String = "",
    val boilGravity: String = "",
    val calculatedIBU: Double? = null,
    val isValid: Boolean = false
)

data class SRMCalculatorState(
    val grainEntries: List<GrainEntry> = emptyList(),
    val batchSize: String = "",
    val calculatedSRM: Double? = null,
    val calculatedEBC: Double? = null,
    val isValid: Boolean = false
)

data class GrainEntry(
    val weight: String = "",
    val colorLovibond: String = "",
    val name: String = ""
)

data class PrimingCalculatorState(
    val beerVolume: String = "",
    val currentTemp: String = "",
    val targetCO2: String = "",
    val sugarType: SugarType = SugarType.CORN_SUGAR,
    val calculatedSugar: Double? = null,
    val isValid: Boolean = false
)

data class BrixConverterState(
    val brixValue: String = "",
    val sgValue: String = "",
    val convertedSG: String = "",
    val convertedBrix: String = "",
    val isValid: Boolean = false
)

data class WaterCalculatorState(
    val grainWeight: String = "",
    val mashRatio: String = "1.25",
    val totalWater: String = "",
    val grainAbsorption: String = "0.125",
    val boilOffRate: String = "1.25", 
    val boilTime: String = "1.0",
    val grainTemp: String = "",
    val targetMashTemp: String = "",
    val calculatedMashWater: Double? = null,
    val calculatedSpargeWater: Double? = null,
    val calculatedStrikeTemp: Double? = null,
    val isValid: Boolean = false
)
