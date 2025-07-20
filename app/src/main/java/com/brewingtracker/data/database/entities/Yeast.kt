package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "yeasts")
@Parcelize
data class Yeast(
    @PrimaryKey
    val id: Int,
    val name: String,
    val brand: String,
    val type: YeastType,
    val beverageTypes: String, // "beer,mead,wine"
    
    // Performance characteristics
    val temperatureRangeMin: Int? = null,
    val temperatureRangeMax: Int? = null,
    val alcoholTolerance: Double? = null,
    val attenuationMin: Int? = null,
    val attenuationMax: Int? = null,
    val flocculation: FlocculationType? = null,
    
    // Kveik-specific
    val isKveik: Boolean = false,
    val kveikStrainOrigin: String? = null,
    val temperatureRangeKveik: String? = null, // often much higher
    
    // Inventory
    val currentStock: Int = 0, // packets/vials
    val costPerUnit: Double? = null,
    val expirationDate: Long? = null,
    
    val description: String? = null,
    val usageNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class YeastType {
    ALE,
    LAGER,
    WINE,
    MEAD,
    KVEIK,
    WILD,
    CHAMPAGNE,
    CIDER,
    SAKE,
    KOMBUCHA
}

enum class FlocculationType {
    LOW,
    MEDIUM,
    HIGH,
    VARIABLE
}