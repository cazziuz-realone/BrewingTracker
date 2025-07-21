package com.brewingtracker.domain.calculator

import kotlin.math.pow
import kotlin.math.round

/**
 * Brewing calculation utilities for homebrew tracking
 */
object BrewingCalculations {

    /**
     * Calculate ABV (Alcohol By Volume) from Original Gravity and Final Gravity
     * Formula: ABV = (OG - FG) * 131.25
     */
    fun calculateABV(originalGravity: Double, finalGravity: Double): Double {
        return (originalGravity - finalGravity) * 131.25
    }

    /**
     * Calculate ABV with temperature correction
     * More accurate formula that accounts for temperature
     */
    fun calculateABVWithCorrection(originalGravity: Double, finalGravity: Double): Double {
        val abv = (76.08 * (originalGravity - finalGravity) / (1.775 - originalGravity)) * 
                  (finalGravity / 0.794)
        return round(abv * 100) / 100
    }

    /**
     * Calculate apparent attenuation percentage
     * Formula: ((OG - FG) / (OG - 1)) * 100
     */
    fun calculateAttenuation(originalGravity: Double, finalGravity: Double): Double {
        return ((originalGravity - finalGravity) / (originalGravity - 1.0)) * 100
    }

    /**
     * Calculate IBU (International Bittering Units) using Tinseth formula
     * @param alphaAcid Alpha acid percentage of hops
     * @param hopWeight Weight of hops in ounces
     * @param batchSize Batch size in gallons
     * @param boilTime Boil time in minutes
     * @param gravity Specific gravity during boil
     */
    fun calculateIBU(
        alphaAcid: Double,
        hopWeight: Double,
        batchSize: Double,
        boilTime: Int,
        gravity: Double
    ): Double {
        // Utilization factor
        val utilizationFactor = (1.65 * (0.000125).pow(gravity - 1.0)) * 
                              ((1 - 2.718.pow(-0.04 * boilTime)) / 4.15)
        
        // IBU calculation
        val ibu = (alphaAcid * hopWeight * utilizationFactor * 7490) / batchSize
        
        return round(ibu * 10) / 10
    }

    /**
     * Calculate SRM (Standard Reference Method) color using Morey's formula
     * @param grainWeights List of grain weights in pounds
     * @param grainColors List of corresponding grain colors in Lovibond
     * @param batchSize Batch size in gallons
     */
    fun calculateSRM(
        grainWeights: List<Double>,
        grainColors: List<Double>,
        batchSize: Double
    ): Double {
        if (grainWeights.size != grainColors.size) {
            throw IllegalArgumentException("Grain weights and colors lists must be same size")
        }

        val totalMCU = grainWeights.zip(grainColors) { weight, color ->
            (weight * color) / batchSize
        }.sum()

        // Morey's formula for SRM
        val srm = 1.4922 * totalMCU.pow(0.6859)
        return round(srm * 10) / 10
    }

    /**
     * Convert SRM to EBC (European Brewery Convention)
     */
    fun srmToEBC(srm: Double): Double {
        return srm * 1.97
    }

    /**
     * Convert EBC to SRM
     */
    fun ebcToSRM(ebc: Double): Double {
        return ebc / 1.97
    }

    /**
     * Calculate priming sugar needed for carbonation
     * @param beerVolume Volume of beer in gallons
     * @param currentTemp Temperature of beer in Fahrenheit
     * @param targetCO2 Target CO2 volumes
     * @param sugarType Type of priming sugar (corn sugar, table sugar, etc.)
     */
    fun calculatePrimingSugar(
        beerVolume: Double,
        currentTemp: Double,
        targetCO2: Double,
        sugarType: SugarType = SugarType.CORN_SUGAR
    ): Double {
        // Calculate residual CO2 based on temperature
        val residualCO2 = 3.0378 - (0.050062 * currentTemp) + (0.00026555 * currentTemp.pow(2))
        
        // CO2 needed
        val co2Needed = targetCO2 - residualCO2
        
        // Sugar needed using the factor from SugarType enum
        val sugarOunces = (co2Needed * beerVolume) / sugarType.factor
        return round(sugarOunces * 100) / 100
    }

    /**
     * Convert Brix to Specific Gravity
     */
    fun brixToSpecificGravity(brix: Double): Double {
        return 1 + (brix / (258.6 - ((brix / 258.2) * 227.1)))
    }

    /**
     * Convert Specific Gravity to Brix
     */
    fun specificGravityToBrix(sg: Double): Double {
        return (((182.4601 * sg - 775.6821) * sg + 1262.7794) * sg - 669.5622)
    }

    /**
     * Calculate mash water needed
     * @param grainWeight Total grain weight in pounds
     * @param ratio Water to grain ratio (typically 1.25 to 1.5 quarts per pound)
     */
    fun calculateMashWater(grainWeight: Double, ratio: Double = 1.25): Double {
        return grainWeight * ratio
    }

    /**
     * Calculate sparge water needed
     * @param totalWater Total water needed for batch
     * @param mashWater Water used in mash
     * @param grainAbsorption Water absorbed by grain (typically 0.125 gal/lb)
     * @param boilOffRate Water lost to boil-off (typically 1-1.5 gal/hour)
     * @param boilTime Boil time in hours
     */
    fun calculateSpargeWater(
        totalWater: Double,
        mashWater: Double,
        grainWeight: Double,
        grainAbsorption: Double = 0.125,
        boilOffRate: Double = 1.25,
        boilTime: Double = 1.0
    ): Double {
        val waterLoss = (grainWeight * grainAbsorption) + (boilOffRate * boilTime)
        return totalWater - mashWater + waterLoss
    }

    /**
     * Calculate strike water temperature
     * @param grainTemp Temperature of grain in Fahrenheit
     * @param targetMashTemp Target mash temperature in Fahrenheit
     * @param ratio Water to grain ratio
     */
    fun calculateStrikeWaterTemp(
        grainTemp: Double,
        targetMashTemp: Double,
        ratio: Double
    ): Double {
        return (0.2 / ratio) * (targetMashTemp - grainTemp) + targetMashTemp
    }

    /**
     * Temperature correction for hydrometer readings
     * @param observedGravity Gravity reading from hydrometer
     * @param sampleTemp Temperature of sample in Fahrenheit
     * @param calibrationTemp Calibration temperature of hydrometer (usually 60°F)
     */
    fun temperatureCorrection(
        observedGravity: Double,
        sampleTemp: Double,
        calibrationTemp: Double = 60.0
    ): Double {
        val correction = 1.313454 - (0.132674 * sampleTemp) + 
                        (2.057793e-3 * sampleTemp.pow(2)) - 
                        (2.627634e-6 * sampleTemp.pow(3))
        return observedGravity * correction
    }
}

// REMOVED: Duplicate SugarType enum - using the one from BrewCalculator.kt with factor and displayName properties