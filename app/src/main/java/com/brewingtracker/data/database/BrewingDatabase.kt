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
    version = 3,  // Incremented to 3 for expanded ingredient database
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
                .fallbackToDestructiveMigration()  // Added for development - removes this for production
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

// Function to populate database with comprehensive ingredient data
private suspend fun populateDatabase(database: BrewingDatabase) {
    val projectDao = database.projectDao()
    val ingredientDao = database.ingredientDao()
    val yeastDao = database.yeastDao()
    
    // EXPANDED INGREDIENT DATABASE - 50+ Professional Brewing Ingredients
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
        Ingredient(
            id = 9,
            name = "Roasted Barley",
            type = IngredientType.GRAIN,
            category = "Specialty Malt",
            beverageTypes = "beer",
            colorLovibond = 500.0,
            ppgExtract = 25.0,
            description = "Essential for stouts, adds dry roasted flavor",
            currentStock = 0.5,
            unit = "lbs"
        ),
        Ingredient(
            id = 10,
            name = "Wheat Malt",
            type = IngredientType.GRAIN,
            category = "Base Malt",
            beverageTypes = "beer",
            colorLovibond = 2.0,
            ppgExtract = 36.0,
            description = "For wheat beers and head retention",
            currentStock = 3.0,
            unit = "lbs"
        ),
        
        // HOPS - AMERICAN
        Ingredient(
            id = 11,
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
            id = 12,
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
            id = 13,
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
            id = 14,
            name = "Mosaic",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 12.5,
            description = "Complex tropical and stone fruit profile",
            currentStock = 2.0,
            unit = "oz"
        ),
        Ingredient(
            id = 15,
            name = "Simcoe",
            type = IngredientType.HOP,
            category = "Dual Purpose",
            beverageTypes = "beer",
            alphaAcidPercentage = 13.0,
            description = "Passion fruit and pine character",
            currentStock = 1.5,
            unit = "oz"
        ),
        
        // HOPS - NOBLE & EUROPEAN
        Ingredient(
            id = 16,
            name = "Hallertau",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 4.0,
            description = "Classic German noble hop",
            currentStock = 2.0,
            unit = "oz"
        ),
        Ingredient(
            id = 17,
            name = "Saaz",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 3.5,
            description = "Traditional Czech pilsner hop",
            currentStock = 2.5,
            unit = "oz"
        ),
        Ingredient(
            id = 18,
            name = "East Kent Goldings",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 5.0,
            description = "Traditional English ale hop",
            currentStock = 1.0,
            unit = "oz"
        ),
        
        // MEAD & HONEY INGREDIENTS
        Ingredient(
            id = 19,
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
            id = 20,
            name = "Orange Blossom Honey",
            type = IngredientType.ADJUNCT,
            category = "Fermentable",
            beverageTypes = "mead",
            ppgExtract = 35.0,
            description = "Delicate floral honey with citrus notes",
            currentStock = 6.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 21,
            name = "Buckwheat Honey",
            type = IngredientType.ADJUNCT,
            category = "Fermentable",
            beverageTypes = "mead",
            ppgExtract = 35.0,
            description = "Dark, robust honey with earthy flavors",
            currentStock = 3.0,
            unit = "lbs"
        ),
        
        // WINE & FRUIT INGREDIENTS
        Ingredient(
            id = 22,
            name = "Cabernet Sauvignon Grapes",
            type = IngredientType.FRUIT,
            category = "Fresh Fruit",
            beverageTypes = "wine",
            description = "Premium wine grapes for red wine",
            currentStock = 0.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 23,
            name = "Chardonnay Grapes",
            type = IngredientType.FRUIT,
            category = "Fresh Fruit",
            beverageTypes = "wine",
            description = "Premium white wine grapes",
            currentStock = 0.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 24,
            name = "Strawberries",
            type = IngredientType.FRUIT,
            category = "Fresh Fruit",
            beverageTypes = "mead,wine,beer",
            description = "Fresh strawberries for fruit wines and meads",
            currentStock = 0.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 25,
            name = "Blueberries",
            type = IngredientType.FRUIT,
            category = "Fresh Fruit",
            beverageTypes = "mead,wine,beer",
            description = "Fresh blueberries for antioxidant-rich beverages",
            currentStock = 2.0,
            unit = "lbs"
        ),
        
        // CIDER INGREDIENTS
        Ingredient(
            id = 26,
            name = "Apple Juice (Fresh)",
            type = IngredientType.FRUIT,
            category = "Juice",
            beverageTypes = "cider",
            description = "Fresh-pressed apple juice for cider",
            currentStock = 5.0,
            unit = "gallons"
        ),
        Ingredient(
            id = 27,
            name = "Pear Juice",
            type = IngredientType.FRUIT,
            category = "Juice",
            beverageTypes = "cider",
            description = "Fresh pear juice for perry",
            currentStock = 1.0,
            unit = "gallons"
        ),
        
        // KOMBUCHA INGREDIENTS
        Ingredient(
            id = 28,
            name = "Black Tea",
            type = IngredientType.OTHER,
            category = "Tea",
            beverageTypes = "kombucha",
            description = "Organic black tea for kombucha base",
            currentStock = 1.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 29,
            name = "Green Tea",
            type = IngredientType.OTHER,
            category = "Tea",
            beverageTypes = "kombucha",
            description = "Organic green tea for lighter kombucha",
            currentStock = 0.5,
            unit = "lbs"
        ),
        Ingredient(
            id = 30,
            name = "SCOBY",
            type = IngredientType.OTHER,
            category = "Culture",
            beverageTypes = "kombucha",
            description = "Symbiotic culture of bacteria and yeast",
            currentStock = 2.0,
            unit = "pieces"
        ),
        
        // SUGARS & ADJUNCTS
        Ingredient(
            id = 31,
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
            id = 32,
            name = "Brown Sugar",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            beverageTypes = "beer,mead",
            ppgExtract = 43.0,
            description = "Adds molasses notes and color",
            currentStock = 2.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 33,
            name = "Lactose",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            beverageTypes = "beer",
            ppgExtract = 0.0,
            description = "Unfermentable sugar for sweetness and body",
            currentStock = 1.0,
            unit = "lbs"
        ),
        
        // SPICES & HERBS
        Ingredient(
            id = 34,
            name = "Coriander Seeds",
            type = IngredientType.OTHER,
            category = "Spice",
            beverageTypes = "beer",
            description = "Traditional wheat beer spice",
            currentStock = 0.25,
            unit = "lbs"
        ),
        Ingredient(
            id = 35,
            name = "Orange Peel (Bitter)",
            type = IngredientType.OTHER,
            category = "Spice",
            beverageTypes = "beer",
            description = "Dried bitter orange peel for Belgian styles",
            currentStock = 0.1,
            unit = "lbs"
        ),
        Ingredient(
            id = 36,
            name = "Cinnamon Sticks",
            type = IngredientType.OTHER,
            category = "Spice",
            beverageTypes = "mead,beer,cider",
            description = "Whole cinnamon sticks for warming spice",
            currentStock = 0.2,
            unit = "lbs"
        ),
        Ingredient(
            id = 37,
            name = "Vanilla Beans",
            type = IngredientType.OTHER,
            category = "Spice",
            beverageTypes = "beer,mead",
            description = "Madagascar vanilla beans for rich flavor",
            currentStock = 5.0,
            unit = "pieces"
        ),
        
        // WOOD & AGING
        Ingredient(
            id = 38,
            name = "American Oak Chips",
            type = IngredientType.OTHER,
            category = "Wood",
            beverageTypes = "beer,wine,mead",
            description = "Medium toast oak chips for aging",
            currentStock = 1.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 39,
            name = "French Oak Cubes",
            type = IngredientType.OTHER,
            category = "Wood",
            beverageTypes = "wine,mead",
            description = "Heavy toast French oak for complex flavors",
            currentStock = 0.5,
            unit = "lbs"
        ),
        
        // ACIDS & NUTRIENTS
        Ingredient(
            id = 40,
            name = "Tartaric Acid",
            type = IngredientType.OTHER,
            category = "Acid",
            beverageTypes = "wine,mead",
            description = "Primary acid for wine and mead pH adjustment",
            currentStock = 0.5,
            unit = "lbs"
        ),
        Ingredient(
            id = 41,
            name = "Yeast Nutrient",
            type = IngredientType.OTHER,
            category = "Nutrient",
            beverageTypes = "mead,wine,cider",
            description = "Complete yeast nutrient blend",
            currentStock = 0.25,
            unit = "lbs"
        ),
        Ingredient(
            id = 42,
            name = "Pectic Enzyme",
            type = IngredientType.OTHER,
            category = "Enzyme",
            beverageTypes = "wine,cider,mead",
            description = "Breaks down pectin for clarity",
            currentStock = 2.0,
            unit = "oz"
        ),
        
        // SPECIALTY BEER INGREDIENTS
        Ingredient(
            id = 43,
            name = "Flaked Oats",
            type = IngredientType.GRAIN,
            category = "Adjunct",
            beverageTypes = "beer",
            colorLovibond = 1.0,
            ppgExtract = 33.0,
            description = "Adds body and silky mouthfeel to beer",
            currentStock = 2.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 44,
            name = "Rice Hulls",
            type = IngredientType.GRAIN,
            category = "Adjunct",
            beverageTypes = "beer",
            ppgExtract = 0.0,
            description = "Improves lautering in stuck mashes",
            currentStock = 1.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 45,
            name = "Coconut (Shredded)",
            type = IngredientType.OTHER,
            category = "Flavoring",
            beverageTypes = "beer,mead",
            description = "Unsweetened coconut for tropical flavors",
            currentStock = 1.0,
            unit = "lbs"
        ),
        
        // COFFEE & CHOCOLATE
        Ingredient(
            id = 46,
            name = "Coffee Beans (Whole)",
            type = IngredientType.OTHER,
            category = "Flavoring",
            beverageTypes = "beer,mead",
            description = "Fresh roasted coffee beans for coffee stouts",
            currentStock = 2.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 47,
            name = "Cocoa Nibs",
            type = IngredientType.OTHER,
            category = "Flavoring",
            beverageTypes = "beer,mead",
            description = "Raw cacao nibs for chocolate character",
            currentStock = 1.0,
            unit = "lbs"
        ),
        
        // WATER TREATMENT
        Ingredient(
            id = 48,
            name = "Gypsum (CaSO4)",
            type = IngredientType.OTHER,
            category = "Water Treatment",
            beverageTypes = "beer",
            description = "Increases calcium and sulfate for hop character",
            currentStock = 1.0,
            unit = "lbs"
        ),
        Ingredient(
            id = 49,
            name = "Calcium Chloride",
            type = IngredientType.OTHER,
            category = "Water Treatment",
            beverageTypes = "beer",
            description = "Increases calcium and chloride for malt character",
            currentStock = 0.5,
            unit = "lbs"
        ),
        
        // ADDITIONAL FERMENTABLES
        Ingredient(
            id = 50,
            name = "Maple Syrup",
            type = IngredientType.ADJUNCT,
            category = "Fermentable",
            beverageTypes = "beer,mead",
            ppgExtract = 30.0,
            description = "Pure maple syrup for unique flavor and fermentables",
            currentStock = 1.0,
            unit = "gallons"
        )
    )
    
    ingredientDao.insertIngredients(sampleIngredients)
    
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
        ),
        Yeast(
            id = 3,
            name = "Wyeast 1056",
            brand = "Wyeast",
            type = YeastType.ALE,
            beverageTypes = "beer",
            temperatureRangeMin = 60,
            temperatureRangeMax = 72,
            alcoholTolerance = 11.0,
            attenuationMin = 73,
            attenuationMax = 77,
            flocculation = FlocculationType.LOW_MEDIUM,
            description = "American ale yeast, clean and versatile"
        ),
        Yeast(
            id = 4,
            name = "SafLager W-34/70",
            brand = "Fermentis",
            type = YeastType.LAGER,
            beverageTypes = "beer",
            temperatureRangeMin = 46,
            temperatureRangeMax = 59,
            alcoholTolerance = 10.0,
            attenuationMin = 80,
            attenuationMax = 84,
            flocculation = FlocculationType.HIGH,
            description = "Classic German lager yeast"
        )
    )
    yeastDao.insertYeasts(sampleYeasts)
}