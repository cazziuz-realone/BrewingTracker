package com.brewingtracker.data.database

import androidx.room.TypeConverter
import com.brewingtracker.data.database.entities.*

class Converters {
    
    @TypeConverter
    fun fromProjectType(type: ProjectType): String = type.name
    
    @TypeConverter
    fun toProjectType(type: String): ProjectType = ProjectType.valueOf(type)
    
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
}