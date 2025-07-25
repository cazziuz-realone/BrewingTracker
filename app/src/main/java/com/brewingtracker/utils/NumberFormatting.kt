package com.brewingtracker.utils

import java.text.DecimalFormat
import kotlin.math.roundToInt

/**
 * Utility functions for formatting numbers in the brewing context
 */

/**
 * Format a double quantity with appropriate decimal places
 */
fun Double.formatQuantity(): String {
    return when {
        this >= 100 -> DecimalFormat("#").format(this)
        this >= 10 -> DecimalFormat("#.#").format(this)
        this >= 1 -> DecimalFormat("#.##").format(this)
        this >= 0.1 -> DecimalFormat("0.##").format(this)
        else -> DecimalFormat("0.###").format(this)
    }
}

/**
 * Format a double as a percentage
 */
fun Double.formatPercentage(): String {
    return DecimalFormat("#.##").format(this)
}

/**
 * Format specific gravity (OG/FG) values
 */
fun Double.formatGravity(): String {
    return DecimalFormat("1.000").format(this)
}

/**
 * Format SRM color values
 */
fun Double.formatSRM(): String {
    return "${roundToInt()} SRM"
}

/**
 * Format ABV values
 */
fun Double.formatABV(): String {
    return "${DecimalFormat("#.#").format(this)}%"
}

/**
 * Format cost values
 */
fun Double.formatCost(): String {
    return "$${DecimalFormat("#.##").format(this)}"
}

/**
 * Round to specific decimal places
 */
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (this * multiplier).roundToInt() / multiplier
}

/**
 * Safe conversion of string to double with default value
 */
fun String.toDoubleOrDefault(default: Double = 0.0): Double {
    return try {
        this.toDouble()
    } catch (e: NumberFormatException) {
        default
    }
}

/**
 * Safe conversion of string to int with default value  
 */
fun String.toIntOrDefault(default: Int = 0): Int {
    return try {
        this.toInt()
    } catch (e: NumberFormatException) {
        default
    }
}

/**
 * Format duration in human-readable format
 */
fun Int.formatDuration(): String {
    return when {
        this < 60 -> "${this}min"
        this < 1440 -> "${this / 60}h ${this % 60}min"
        this < 10080 -> "${this / 1440}d ${(this % 1440) / 60}h"
        else -> "${this / 10080}w ${(this % 10080) / 1440}d"
    }
}
