package com.brewingtracker.data.database

import androidx.room.TypeConverter
import com.brewingtracker.data.database.entities.*

class Converters {
    
    @TypeConverter
    fun fromBeverageType(type: BeverageType): String = type.name
    
    @TypeConverter
    fun toBeverageType(type: String): BeverageType = BeverageType.valueOf(type)
    
    @TypeConverter
    fun fromProjectPhase(phase: ProjectPhase): String = phase.name
    
    @TypeConverter
    fun toProjectPhase(phase: String): ProjectPhase = ProjectPhase.valueOf(phase)
    
    @TypeConverter
    fun fromIngredientType(type: IngredientType): String = type.name
    
    @TypeConverter
    fun toIngredientType(type: String): IngredientType = IngredientType.valueOf(type)
    
    @TypeConverter
    fun fromYeastType(type: YeastType): String = type.name
    
    @TypeConverter
    fun toYeastType(type: String): YeastType = YeastType.valueOf(type)
    
    @TypeConverter
    fun fromFlocculationType(type: FlocculationType?): String? = type?.name
    
    @TypeConverter
    fun toFlocculationType(type: String?): FlocculationType? = 
        type?.let { FlocculationType.valueOf(it) }
    
    // NEW: Recipe-related converters
    @TypeConverter
    fun fromRecipeDifficulty(difficulty: RecipeDifficulty): String = difficulty.name
    
    @TypeConverter
    fun toRecipeDifficulty(difficulty: String): RecipeDifficulty = RecipeDifficulty.valueOf(difficulty)
    
    @TypeConverter
    fun fromBatchSize(batchSize: BatchSize): String = batchSize.name
    
    @TypeConverter
    fun toBatchSize(batchSize: String): BatchSize = BatchSize.valueOf(batchSize)
    
    @TypeConverter
    fun fromInventoryStatus(status: InventoryStatus): String = status.name
    
    @TypeConverter
    fun toInventoryStatus(status: String): InventoryStatus = InventoryStatus.valueOf(status)
}
