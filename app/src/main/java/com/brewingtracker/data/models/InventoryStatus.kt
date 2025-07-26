package com.brewingtracker.data.models

/**
 * Enum representing the inventory status of ingredients for recipes
 */
enum class InventoryStatus {
    /**
     * Sufficient stock available for the recipe
     */
    SUFFICIENT,
    
    /**
     * Insufficient stock - some available but not enough
     */
    INSUFFICIENT,
    
    /**
     * Unknown stock status - not tracked or not checked
     */
    UNKNOWN;

    /**
     * Check if the status indicates the ingredient is available
     */
    fun isAvailable(): Boolean = this == SUFFICIENT
    
    /**
     * Check if the status indicates a shortage
     */
    fun hasShortage(): Boolean = this == INSUFFICIENT
    
    /**
     * Get a user-friendly display name
     */
    fun getDisplayName(): String = when (this) {
        SUFFICIENT -> "In Stock"
        INSUFFICIENT -> "Low Stock"
        UNKNOWN -> "Unknown"
    }
}
