package com.brewingtracker.data.database.entities

enum class BeverageType(val displayName: String) {
    BEER("Beer"),
    MEAD("Mead"),
    WINE("Wine"),
    CIDER("Cider"),
    KOMBUCHA("Kombucha"),
    WATER_KEFIR("Water Kefir"),
    OTHER("Other")
}