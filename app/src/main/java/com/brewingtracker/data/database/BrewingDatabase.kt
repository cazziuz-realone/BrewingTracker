package com.brewingtracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brewingtracker.data.database.dao.*
import com.brewingtracker.data.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Project::class,
        Ingredient::class,
        Yeast::class,
        ProjectIngredient::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BrewingDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun yeastDao(): YeastDao
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
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Seed the database with initial data
                    populateDatabase(database)
                }
            }
        }
    }
}

// Function to populate database with sample data
private suspend fun populateDatabase(database: BrewingDatabase) {
    val projectDao = database.projectDao()
    val ingredientDao = database.ingredientDao()
    val yeastDao = database.yeastDao()
    
    // Add sample ingredients
    val sampleIngredients = listOf(
        Ingredient(
            id = 1,
            name = "Pale 2-Row",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            beverageTypes = "beer",
            colorLovibond = 1.8,
            ppgExtract = 37.0,
            description = "Standard base malt for ales and lagers"
        ),
        Ingredient(
            id = 2,
            name = "Cascade",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 5.5,
            description = "Classic American citrus hop"
        ),
        Ingredient(
            id = 3,
            name = "Wildflower Honey",
            type = IngredientType.ADJUNCT,
            category = "Fermentable",
            beverageTypes = "mead,beer",
            ppgExtract = 35.0,
            description = "Pure wildflower honey for mead making"
        )
    )
    ingredientDao.insertIngredients(sampleIngredients)
    
    // Add sample yeasts
    val sampleYeasts = listOf(
        Yeast(
            id = 1,
            name = "SafAle US-05",
            brand = "Fermentis",
            type = YeastType.ALE,
            beverageTypes = "beer",
            temperatureRangeMin = 59,
            temperatureRangeMax = 75,
            alcoholTolerance = 12.0,
            attenuationMin = 78,
            attenuationMax = 82,
            flocculation = FlocculationType.MEDIUM,
            description = "American ale yeast strain"
        ),
        Yeast(
            id = 2,
            name = "Lallemand DistilaMax MW",
            brand = "Lallemand",
            type = YeastType.MEAD,
            beverageTypes = "mead,wine",
            temperatureRangeMin = 68,
            temperatureRangeMax = 86,
            alcoholTolerance = 18.0,
            description = "Specialized mead and wine yeast"
        )
    )
    yeastDao.insertYeasts(sampleYeasts)
}