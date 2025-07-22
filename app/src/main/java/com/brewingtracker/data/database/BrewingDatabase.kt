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
    version = 4,  // Incremented to 4 to force database recreation
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
    
    // EXPANDED INGREDIENT DATABASE - 30+ Core Ingredients (simplified for reliability)
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
        Ingredient(
            id = 4,
            name = "Vienna Malt",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            beverageTypes = "beer",
            colorLovibond = 3.5,
            ppgExtract = 35.0,
            description = "Adds malty sweetness and amber color",
            currentStock = 3.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 5,
            name = "Munich Malt",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            beverageTypes = "beer",
            colorLovibond = 9.0,
            ppgExtract = 35.0,
            description = "Rich, malty flavor for German styles",
            currentStock = 4.0,
            unit = "lbs"
        ),
        
        // SPECIALTY MALTS
        Ingredient(
            id = 6,
            name = "Crystal 40L",
            type = IngredientType.GRAIN,
            category = "Specialty Malt",
            beverageTypes = "beer",
            colorLovibond = 40.0,
            ppgExtract = 34.0,
            description = "Medium crystal malt for body and caramel flavor",
            currentStock = 2.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 7,
            name = "Crystal 60L",
            type = IngredientType.GRAIN,
            category = "Specialty Malt",
            beverageTypes = "beer",
            colorLovibond = 60.0,
            ppgExtract = 34.0,
            description = "Dark crystal malt for rich caramel notes",
            currentStock = 1.5,
            unit = "lbs"
        ),
        Ingredient(
            id = 8,
            name = "Chocolate Malt",
            type = IngredientType.GRAIN,
            category = "Specialty Malt",
            beverageTypes = "beer",
            colorLovibond = 350.0,
            ppgExtract = 28.0,
            description = "Adds chocolate and coffee notes",
            currentStock = 1.0,
            unit = "lbs"
        ),
        
        // HOPS
        Ingredient(
            id = 9,
            name = "Cascade",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 5.5,
            description = "Classic American citrus hop with floral notes",
            currentStock = 4.0,
            unit = "oz"
        ),
        Ingredient(
            id = 10,
            name = "Centennial",
            type = IngredientType.HOP,
            category = "Dual Purpose",
            beverageTypes = "beer",
            alphaAcidPercentage = 10.0,
            description = "Intense citrus and floral character",
            currentStock = 3.0,
            unit = "oz"
        ),
        Ingredient(
            id = 11,
            name = "Citra",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 12.0,
            description = "Tropical fruit and citrus flavors",
            currentStock = 2.0,
            unit = "oz"
        ),
        Ingredient(
            id = 12,
            name = "Saaz",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 3.5,
            description = "Traditional Czech pilsner hop",
            currentStock = 2.5,
            unit = "oz"
        ),
        
        // MEAD & HONEY INGREDIENTS
        Ingredient(
            id = 13,
            name = "Wildflower Honey",
            type = IngredientType.ADJUNCT,
            category = "Fermentable",
            beverageTypes = "mead,beer",
            ppgExtract = 35.0,
            description = "Pure wildflower honey for mead making",
            currentStock = 12.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 14,
            name = "Orange Blossom Honey",
            type = IngredientType.ADJUNCT,
            category = "Fermentable",
            beverageTypes = "mead",
            ppgExtract = 35.0,
            description = "Delicate floral honey with citrus notes",
            currentStock = 6.0,
            unit = "lbs"
        ),
        
        // WINE & FRUIT INGREDIENTS
        Ingredient(
            id = 15,
            name = "Cabernet Sauvignon Grapes",
            type = IngredientType.FRUIT,
            category = "Fresh Fruit",
            beverageTypes = "wine",
            description = "Premium wine grapes for red wine",
            currentStock = 0.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 16,
            name = "Strawberries",
            type = IngredientType.FRUIT,
            category = "Fresh Fruit",
            beverageTypes = "mead,wine,beer",
            description = "Fresh strawberries for fruit wines and meads",
            currentStock = 0.0,
            unit = "lbs"
        ),
        
        // CIDER INGREDIENTS
        Ingredient(
            id = 17,
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
            id = 18,
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
            id = 19,
            name = "Brown Sugar",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            beverageTypes = "beer,mead",
            ppgExtract = 43.0,
            description = "Adds molasses notes and color",
            currentStock = 2.0,
            unit = "lbs"
        ),
        
        // SPICES
        Ingredient(
            id = 20,
            name = "Coriander Seeds",
            type = IngredientType.OTHER,
            category = "Spice",
            beverageTypes = "beer",
            description = "Traditional wheat beer spice",
            currentStock = 0.25,
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
    
    // Enhanced yeast database - FIXED: Changed LOW_MEDIUM to MEDIUM
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