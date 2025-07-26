package com.brewingtracker.data.models

/**
 * Enum representing the inventory status of ingredients
 * for recipe building and project planning
 */
enum class InventoryStatus {
    /**
     * There is sufficient stock to fulfill the recipe requirement
     */
    SUFFICIENT,
    
    /**
     * There is some stock but not enough to fulfill the recipe requirement
     */
    INSUFFICIENT,
    
    /**
     * Stock status is unknown (ingredient not in inventory or stock not tracked)
     */
    UNKNOWN;
    
    companion object {
        /**
         * Determine inventory status based on required quantity and available stock
         */
        fun fromStockComparison(requiredQuantity: Double, availableStock: Double): InventoryStatus {
            return when {
                availableStock >= requiredQuantity -> SUFFICIENT
                availableStock > 0 -> INSUFFICIENT
                else -> UNKNOWN
            }
        }
    }
}
