package com.brewingtracker.data.database

import androidx.room.TypeConverter
import com.brewingtracker.data.database.entities.BeverageType
import com.brewingtracker.data.database.entities.ProjectPhase

class Converters {
    @TypeConverter
    fun fromBeverageType(value: BeverageType): String {
        return value.name
    }

    @TypeConverter
    fun toBeverageType(value: String): BeverageType {
        return BeverageType.valueOf(value)
    }

    @TypeConverter
    fun fromProjectPhase(value: ProjectPhase): String {
        return value.name
    }

    @TypeConverter
    fun toProjectPhase(value: String): ProjectPhase {
        return ProjectPhase.valueOf(value)
    }
}