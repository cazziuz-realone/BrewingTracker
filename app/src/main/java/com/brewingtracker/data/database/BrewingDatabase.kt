package com.brewingtracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.brewingtracker.data.database.dao.*
import com.brewingtracker.data.database.entities.*

@Database(
    entities = [
        Ingredient::class,
        Yeast::class,
        Project::class,
        ProjectIngredient::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BrewingDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun yeastDao(): YeastDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectIngredientDao(): ProjectIngredientDao

    companion object {
        @Volatile
        private var INSTANCE: BrewingDatabase? = null

        fun getDatabase(context: Context): BrewingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrewingDatabase::class.java,
                    "brewing_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}