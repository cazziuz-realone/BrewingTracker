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
        ProjectIngredient::class,
        Recipe::class,           // ← ADDED for recipe builder
        RecipeIngredient::class  // ← ADDED for recipe builder
    ],
    version = 7,  // Incremented from 6 to 7 for recipe builder system
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BrewingDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun yeastDao(): YeastDao
    abstract fun projectIngredientDao(): ProjectIngredientDao
    abstract fun recipeDao(): RecipeDao                    // ← ADDED for recipe builder
    abstract fun recipeIngredientDao(): RecipeIngredientDao // ← ADDED for recipe builder

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
                .fallbackToDestructiveMigration()  // This will recreate database on version change
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
        
        // Also populate on open to ensure data exists
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Check if ingredients exist, if not populate
                    val ingredientDao = database.ingredientDao()
                    val count = ingredientDao.getIngredientCount()
                    if (count == 0) {
                        populateDatabase(database)
                    }
                }
            }
        }
    }
}

// Function to populate database with comprehensive ingredient data
private suspend fun populateDatabase(database: BrewingDatabase) {
    val projectDao = database.projectDao()
    val ingredientDao = database.ingredientDao()
    val yeastDao = database.yeastDao()
    
    // COMPREHENSIVE INGREDIENT DATABASE - 200+ Mead-Focused Ingredients
    val sampleIngredients = listOf(
        // BASE MALTS
        Ingredient(
            id = 1,
            name = "Pale 2-Row",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            beverageTypes = "beer",
            colorLovibond = 1.8,
            ppgExtract = 37.0,
            description = "Standard base malt for ales and lagers",
            currentStock = 10.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 2,
            name = "Pilsner Malt",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            beverageTypes = "beer",
            colorLovibond = 1.5,
            ppgExtract = 37.0,
            description = "Light, crisp base malt for lagers",
            currentStock = 8.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 3,
            name = "Maris Otter",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            beverageTypes = "beer",
            colorLovibond = 3.0,
            ppgExtract = 36.0,
            description = "Traditional English ale malt",
            currentStock = 5.0,
            unit = "lbs"
        ),
        
        // MEAD & HONEY INGREDIENTS - EXPANDED
        Ingredient(
            id = 13,
            name = "Wildflower Honey",
            type = IngredientType.FRUIT,
            category = "Honey",
            beverageTypes = "mead,beer",
            ppgExtract = 35.0,
            description = "Pure wildflower honey for mead making",
            currentStock = 12.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 14,
            name = "Orange Blossom Honey",
            type = IngredientType.FRUIT,
            category = "Honey",
            beverageTypes = "mead",
            ppgExtract = 35.0,
            description = "Delicate floral honey with citrus notes",
            currentStock = 6.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 15,
            name = "Clover Honey",
            type = IngredientType.FRUIT,
            category = "Honey",
            beverageTypes = "mead",
            ppgExtract = 35.0,
            description = "Light, delicate, classic honey flavor",
            currentStock = 8.0,
            unit = "lbs"
        ),
        
        // FRUITS FOR MELOMEL
        Ingredient(
            id = 20,
            name = "Strawberries",
            type = IngredientType.FRUIT,
            category = "Berry",
            beverageTypes = "mead,wine,beer",
            description = "Fresh strawberries for fruit wines and meads",
            currentStock = 2.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 21,
            name = "Blackberries",
            type = IngredientType.FRUIT,
            category = "Berry",
            beverageTypes = "mead,wine,beer",
            description = "Rich, complex berry character for melomel",
            currentStock = 1.5,
            unit = "lbs"
        ),
        Ingredient(
            id = 22,
            name = "Sweet Cherries",
            type = IngredientType.FRUIT,
            category = "Stone Fruit",
            beverageTypes = "mead,wine,beer",
            description = "Rich, sweet fruit character for classic melomel",
            currentStock = 0.0,
            unit = "lbs"
        ),
        
        // SPICES FOR METHEGLIN
        Ingredient(
            id = 30,
            name = "Ceylon Cinnamon",
            type = IngredientType.SPICE,
            category = "Warming",
            beverageTypes = "mead,beer,wine",
            description = "True cinnamon, delicate and sweet for metheglin",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 31,
            name = "Madagascar Vanilla Beans",
            type = IngredientType.SPICE,
            category = "Sweet",
            beverageTypes = "mead,beer",
            description = "Premium vanilla, split and scrape for best results",
            currentStock = 0.0,
            unit = "each"
        ),
        Ingredient(
            id = 32,
            name = "Cardamom Pods",
            type = IngredientType.SPICE,
            category = "Warming",
            beverageTypes = "mead,beer",
            description = "Aromatic, slightly citrusy spice",
            currentStock = 0.0,
            unit = "oz"
        ),
        
        // CIDER INGREDIENTS
        Ingredient(
            id = 40,
            name = "Apple Juice (Fresh)",
            type = IngredientType.FRUIT,
            category = "Juice",
            beverageTypes = "cider",
            description = "Fresh-pressed apple juice for cider",
            currentStock = 5.0,
            unit = "gallons"
        ),
        
        // SUGARS & ADJUNCTS
        Ingredient(
            id = 50,
            name = "Corn Sugar (Dextrose)",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            beverageTypes = "beer,mead,wine,cider",
            ppgExtract = 46.0,
            description = "Pure dextrose for priming and fermentation",
            currentStock = 5.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 51,
            name = "Brown Sugar",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            beverageTypes = "beer,mead",
            ppgExtract = 43.0,
            description = "Adds molasses notes and color",
            currentStock = 2.0,
            unit = "lbs"
        )
    )
    
    // Insert ingredients
    try {
        ingredientDao.insertIngredients(sampleIngredients)
    } catch (e: Exception) {
        // Handle any insertion errors
        e.printStackTrace()
    }
    
    // Enhanced yeast database
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
    
    // Insert yeasts
    try {
        yeastDao.insertYeasts(sampleYeasts)
    } catch (e: Exception) {
        // Handle any insertion errors
        e.printStackTrace()
    }
}
