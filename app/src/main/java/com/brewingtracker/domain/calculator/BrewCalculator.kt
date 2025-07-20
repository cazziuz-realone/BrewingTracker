package com.brewingtracker.domain.calculator

import kotlin.math.exp
import kotlin.math.pow

object BrewCalculator {

    // ABV Calculations
    fun calculateABVFromGravity(og: Double, fg: Double): Double {
        return (og - fg) * 131.25
    }

    fun calculateABVFromBrix(originalBrix: Double, finalBrix: Double): Double {
        val og = brixToSpecificGravity(originalBrix)
        val fg = brixToSpecificGravity(finalBrix)
        return calculateABVFromGravity(og, fg)
    }

    // Brix and Gravity Conversions
    fun brixToSpecificGravity(brix: Double): Double {
        return 1 + (brix / (258.6 - ((brix / 258.2) * 227.1)))
    }

    fun specificGravityToBrix(sg: Double): Double {
        return ((182.4601 * sg - 775.6821) * sg + 1262.7794) * sg - 669.5622
    }

    fun platoToSpecificGravity(plato: Double): Double {
        return 1 + (plato / (258.6 - ((plato / 258.2) * 227.1)))
    }

    // Temperature Correction for Hydrometer
    fun temperatureCorrection(reading: Double, measuredTemp: Double, calibrationTemp: Double = 68.0): Double {
        val correction = 1.00130346 - (0.000134722124 * measuredTemp) +
                (0.00000204052596 * measuredTemp.pow(2)) -
                (0.00000000232820948 * measuredTemp.pow(3))
        return reading * correction
    }

    // SRM Color Calculation
    fun calculateSRM(grains: List<GrainAddition>, batchSize: Double): Double {
        val mcu = grains.sumOf { (it.colorLovibond * it.amountLbs) / batchSize }
        return 1.4922 * mcu.pow(0.6859)
    }

    fun srmToEBC(srm: Double): Double = srm * 1.97

    fun getSRMDescription(srm: Double): String {
        return when {
            srm < 2 -> "Very Light"
            srm < 4 -> "Light"
            srm < 6 -> "Gold"
            srm < 9 -> "Amber"
            srm < 18 -> "Brown"
            srm < 35 -> "Dark Brown"
            else -> "Black"
        }
    }

    // IBU Calculation (Tinseth Method)
    fun calculateIBU(hops: List<HopAddition>, batchSize: Double, og: Double): Double {
        return hops.sumOf { hop ->
            val utilization = getHopUtilization(hop.boilTime, og)
            val aau = hop.alphaAcid * hop.amountOz
            (aau * utilization * 74.89) / batchSize
        }
    }

    private fun getHopUtilization(boilTime: Int, originalGravity: Double): Double {
        val bignessFactor = 1.65 * 0.000125.pow(originalGravity - 1)
        val timeFactor = (1 - exp(-0.04 * boilTime)) / 4.15
        return bignessFactor * timeFactor
    }

    fun getIBUCategory(ibu: Double): String {
        return when {
            ibu < 20 -> "Low Bitterness"
            ibu < 40 -> "Moderate Bitterness"
            ibu < 60 -> "High Bitterness"
            else -> "Very High Bitterness"
        }
    }

    // Priming Sugar Calculation
    fun calculatePrimingSugar(
        co2Volumes: Double,
        temperature: Double,
        sugarType: SugarType = SugarType.CORN_SUGAR,
        batchSize: Double = 5.0
    ): Double {
        val tempCorrection = 3.0378 - (0.050062 * temperature) + (0.00026555 * temperature.pow(2))
        val requiredCO2 = (co2Volumes - tempCorrection) * batchSize
        return requiredCO2 * sugarType.factor
    }

    // Mead Nutrient Calculation
    fun calculateMeadNutrients(honeyWeight: Double, meadType: MeadType): NutrientResult {
        val baseYAN = meadType.yanRequirement * honeyWeight
        val fermaidK = baseYAN * 0.4
        val dap = baseYAN * 0.6

        return NutrientResult(
            fermaidK = fermaidK,
            dap = dap,
            totalYAN = baseYAN
        )
    }

    // Wine Acid Addition
    fun calculateAcidAddition(
        currentTA: Double,
        targetTA: Double,
        volume: Double,
        acidType: AcidType = AcidType.TARTARIC
    ): Double {
        val difference = targetTA - currentTA
        val gallonsToLiters = volume * 3.78541
        return maxOf(0.0, (difference * gallonsToLiters) / acidType.strength)
    }

    // Recipe Scaling
    fun scaleRecipe(originalVolume: Double, targetVolume: Double, amount: Double): Double {
        return amount * (targetVolume / originalVolume)
    }

    // Attenuation
    fun calculateAttenuation(og: Double, fg: Double): Double {
        return ((og - fg) / (og - 1.0)) * 100
    }
}

// Data Classes
data class GrainAddition(
    val name: String,
    val amountLbs: Double,
    val colorLovibond: Double
)

data class HopAddition(
    val name: String,
    val amountOz: Double,
    val alphaAcid: Double,
    val boilTime: Int
)

data class NutrientResult(
    val fermaidK: Double,
    val dap: Double,
    val totalYAN: Double
)

enum class SugarType(val factor: Double, val displayName: String) {
    CORN_SUGAR(4.0, "Corn Sugar (Dextrose)"),
    TABLE_SUGAR(3.7, "Table Sugar (Sucrose)"),
    DME(4.6, "Dry Malt Extract"),
    HONEY(3.5, "Honey")
}

enum class MeadType(val yanRequirement: Double, val displayName: String) {
    TRADITIONAL(50.0, "Traditional Mead"),
    FRUIT(100.0, "Fruit Mead (Melomel)"),
    HIGH_GRAVITY(150.0, "High Gravity Mead")
}

enum class AcidType(val strength: Double, val displayName: String) {
    TARTARIC(1.0, "Tartaric Acid"),
    MALIC(0.67, "Malic Acid"),
    CITRIC(0.7, "Citric Acid")
}