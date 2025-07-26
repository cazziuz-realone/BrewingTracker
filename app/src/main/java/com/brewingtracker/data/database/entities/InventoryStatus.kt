package com.brewingtracker.data.database.entities

/**
 * Enum representing the inventory status of an ingredient
 * Used in recipe builders to indicate if sufficient stock is available
 */
enum class InventoryStatus {
    SUFFICIENT,    // Enough stock available for the recipe
    INSUFFICIENT,  // Some stock available but not enough
    UNKNOWN        // Stock status cannot be determined
}
