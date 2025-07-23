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
    version = 6,  // Incremented to 6 for major mead-focused ingredient expansion
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
            description = "Citrus and floral, super Cascade. Classic American hop",
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
            type = IngredientType.SPICE,
            category = "Spice",
            beverageTypes = "beer",
            description = "Traditional wheat beer spice",
            currentStock = 0.25,
            unit = "lbs"
        ),

        // ===== EXPANDED INGREDIENT DATABASE - YEASTS & NUTRIENTS =====
        
        // ALE YEASTS - American
        Ingredient(
            id = 100,
            name = "Wyeast 1056 American Ale",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Clean, crisp, neutral. Temp: 60-72°F. Attenuation: 73-77%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 101,
            name = "White Labs WLP001 California Ale",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Clean American strain. Temp: 68-73°F. Attenuation: 73-80%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 102,
            name = "Safale US-05",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Dry American ale yeast. Temp: 59-75°F. Attenuation: 78-82%",
            currentStock = 0.0,
            unit = "pack"
        ),

        // ALE YEASTS - English
        Ingredient(
            id = 103,
            name = "Wyeast 1968 London ESB",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Malty, balanced English character. Temp: 64-72°F. Attenuation: 67-71%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 104,
            name = "White Labs WLP002 English Ale",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Classic British strain. Temp: 65-68°F. Attenuation: 63-70%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 105,
            name = "Safale S-04",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Dry English ale yeast. Temp: 59-75°F. Attenuation: 69-82%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 106,
            name = "Wyeast 1318 London Ale III",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Dry, tart, crisp character. Temp: 66-71°F. Attenuation: 71-75%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 107,
            name = "White Labs WLP007 Dry English Ale",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "High attenuation, clean finish. Temp: 65-70°F. Attenuation: 70-80%",
            currentStock = 0.0,
            unit = "pack"
        ),

        // ALE YEASTS - Belgian
        Ingredient(
            id = 108,
            name = "Wyeast 3787 Trappist High Gravity",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Spicy phenolics, fruity esters. Temp: 64-78°F. Attenuation: 75-85%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 109,
            name = "White Labs WLP530 Abbey Ale",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Complex Belgian character. Temp: 66-72°F. Attenuation: 75-80%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 110,
            name = "Lallemand Abbaye",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Ale",
            beverageTypes = "beer",
            description = "Dry Belgian abbey yeast. Temp: 59-77°F. Attenuation: 78-84%",
            currentStock = 0.0,
            unit = "pack"
        ),

        // LAGER YEASTS
        Ingredient(
            id = 111,
            name = "Wyeast 2124 Bohemian Lager",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Lager",
            beverageTypes = "beer",
            description = "Rich, full malty character. Temp: 48-58°F. Attenuation: 69-73%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 112,
            name = "White Labs WLP830 German Lager",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Lager",
            beverageTypes = "beer",
            description = "Malty, clean German character. Temp: 50-55°F. Attenuation: 74-79%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 113,
            name = "Saflager W-34/70",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Lager",
            beverageTypes = "beer",
            description = "Dry German lager yeast. Temp: 48-59°F. Attenuation: 80-84%",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 114,
            name = "Wyeast 2007 Pilsen Lager",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Lager",
            beverageTypes = "beer",
            description = "Classic Pilsner character. Temp: 46-56°F. Attenuation: 71-75%",
            currentStock = 0.0,
            unit = "pack"
        ),

        // MEAD YEASTS - PREMIUM SELECTION
        Ingredient(
            id = 115,
            name = "Lallemand DistilaMax MW",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Mead",
            beverageTypes = "mead",
            description = "High alcohol tolerance mead specialist. Clean fermentation. 20% ABV potential",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 116,
            name = "Red Star Premier Blanc",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Mead",
            beverageTypes = "wine,mead",
            description = "Neutral character, high alcohol. Temp: 59-86°F. 18% ABV",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 117,
            name = "Wyeast 4184 Sweet Mead",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Mead",
            beverageTypes = "mead",
            description = "Low attenuation for sweet meads. Temp: 65-75°F. Leaves residual honey",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 118,
            name = "White Labs WLP720 Sweet Mead",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Mead",
            beverageTypes = "mead",
            description = "Low attenuation, honey character. Temp: 70-75°F. Sweet finish",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 119,
            name = "Lallemand 71B-1122",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Mead",
            beverageTypes = "wine,mead",
            description = "Reduces malic acid, softens harsh edges. Perfect for fruit meads",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 120,
            name = "Red Star Cuvée",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Mead",
            beverageTypes = "wine,mead",
            description = "Enhances varietal character. Great for traditional meads",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 121,
            name = "Wyeast 4632 Dry Mead",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Mead",
            beverageTypes = "mead",
            description = "High attenuation for dry meads. Temp: 65-75°F. Clean finish",
            currentStock = 0.0,
            unit = "pack"
        ),

        // WINE YEASTS (Also excellent for mead)
        Ingredient(
            id = 122,
            name = "Lallemand EC-1118",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "Champagne yeast. High alcohol tolerance 18%. Temp: 50-86°F",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 123,
            name = "Lallemand D47",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "White wine, low foaming. Temp: 59-68°F. 15% ABV",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 124,
            name = "Red Star Côte des Blancs",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "White wine, fruit wines. Temp: 64-86°F. 14% ABV",
            currentStock = 0.0,
            unit = "pack"
        ),

        // SPECIALTY YEASTS - KVEIK (ORIGINAL + NEW)
        Ingredient(
            id = 125,
            name = "Omega Voss Kveik",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Fast fermenting, citrus character. Temp: 68-98°F! Great for mead",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 126,
            name = "White Labs WLP644 Sacch Trois",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wild",
            beverageTypes = "beer",
            description = "Tropical fruit character. Temp: 70-85°F",
            currentStock = 0.0,
            unit = "pack"
        ),

        // YEAST NUTRIENTS - ESSENTIAL FOR MEAD MAKING
        Ingredient(
            id = 127,
            name = "Fermaid O",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Nutrient",
            beverageTypes = "wine,mead",
            description = "Organic yeast nutrient. Use 1g/gallon for healthy fermentation",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 128,
            name = "Go-Ferm",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Nutrient",
            beverageTypes = "wine,mead",
            description = "Yeast rehydration nutrient. Use before pitching yeast",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 129,
            name = "DAP (Diammonium Phosphate)",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Nutrient",
            beverageTypes = "wine,mead",
            description = "Nitrogen source for yeast health. Use sparingly - can create off flavors",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 130,
            name = "Yeast Hulls",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Nutrient",
            beverageTypes = "wine,mead",
            description = "Dead yeast cells for nutrition. Prevents H2S production",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 131,
            name = "Fermaid K",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Nutrient",
            beverageTypes = "wine,mead",
            description = "Balanced yeast nutrient blend. Use throughout fermentation",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 132,
            name = "Yeast Energizer",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Nutrient",
            beverageTypes = "wine,mead",
            description = "Comprehensive nutrient blend with vitamins and minerals",
            currentStock = 0.0,
            unit = "oz"
        ),

        // ===== MAJOR MEAD-FOCUSED EXPANSION STARTS HERE =====

        // ADDITIONAL KVEIK STRAINS - PERFECT FOR MEAD
        Ingredient(
            id = 133,
            name = "Omega Hornindal Kveik",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Tropical fruit esters, works fantastic in fruit meads. Temp: 70-95°F",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 134,
            name = "Omega Hothead Kveik",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Clean fermentation, extreme temperature tolerance. Temp: 68-98°F",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 135,
            name = "Omega Lutra Kveik",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Pseudolager strain, clean at high temps. Perfect for session meads",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 136,
            name = "Imperial Kveiking",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Citrus and tropical fruit character. Fast mead fermentation",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 137,
            name = "Lallemand Voss Kveik",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Alternative Voss source. Orange citrus esters, mead-friendly",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 138,
            name = "Bootleg Biology Oslo Kveik",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Clean, neutral fermentation. Excellent base for spiced meads",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 139,
            name = "Imperial Ragnar Kveik",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Orange citrus character. Complements honey beautifully",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 140,
            name = "Omega Ebbegarden Kveik",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Kveik",
            beverageTypes = "beer,mead",
            description = "Dried fruit and spice notes. Perfect for traditional meads",
            currentStock = 0.0,
            unit = "pack"
        ),

        // ADDITIONAL WINE YEASTS - EXCELLENT FOR MEAD
        Ingredient(
            id = 141,
            name = "Lallemand ICV-D254",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "Enhances color extraction, perfect for dark fruit meads. 16% ABV",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 142,
            name = "Red Star Montrachet",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "Classic all-purpose wine yeast. Clean fermentation, 13% ABV",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 143,
            name = "Lallemand K1-V1116",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "Reliable, high alcohol tolerance (18%). Cold fermenter, crisp meads",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 144,
            name = "Red Star Pasteur Red",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "For red wines and dark fruit meads. Rich, full-bodied character",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 145,
            name = "Lallemand QA23",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "White wine specialist, preserves delicate honey flavors",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 146,
            name = "Lallemand RC212",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "Burgundy strain for complex fruit meads. Elegant character",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 147,
            name = "Red Star Pasteur Champagne",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "High alcohol, clean fermentation. Traditional mead favorite",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 148,
            name = "Lallemand ICV-GRE",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "Enhances tropical fruit character. Great for exotic fruit meads",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 149,
            name = "White Labs WLP715 Champagne",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "High alcohol (15%), clean profile. Sparkling mead specialist",
            currentStock = 0.0,
            unit = "pack"
        ),
        Ingredient(
            id = 150,
            name = "Lallemand Rhône 2323",
            type = IngredientType.YEAST_NUTRIENT,
            category = "Wine",
            beverageTypes = "wine,mead",
            description = "Spicy, complex character perfect for metheglin",
            currentStock = 0.0,
            unit = "pack"
        ),

        // HONEY VARIETIES - PREMIUM MEAD SELECTION (EXPANDED)
        Ingredient(
            id = 200,
            name = "Wildflower Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Complex, varied flavor from mixed wildflowers. Most common mead honey",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 201,
            name = "Clover Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Light, delicate, classic honey flavor. Excellent for traditional meads",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 202,
            name = "Orange Blossom Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Light citrus notes, aromatic. Beautiful in metheglin and traditional",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 203,
            name = "Tupelo Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Premium Southern honey, buttery smooth, rarely crystallizes",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 204,
            name = "Sage Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Light color, mild flavor, slow to granulate. Ideal for delicate meads",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 205,
            name = "Buckwheat Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Dark, robust, malty flavors. High in antioxidants, complex character",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 206,
            name = "Lavender Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Delicate floral notes, aromatic. Perfect for metheglin",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 207,
            name = "Basswood Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Fresh, almost minty flavor. Light color, distinctive character",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 208,
            name = "Sourwood Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Spicy, buttery flavor. Prized Appalachian honey",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 209,
            name = "Acacia Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Very light, mild flavor, stays liquid. Perfect base honey",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 210,
            name = "Raw Unfiltered Wildflower",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Unpasteurized with natural enzymes and pollen. More complex fermentation",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 211,
            name = "Manuka Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Medicinal properties, earthy flavor. Premium New Zealand honey",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 212,
            name = "Heather Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Thick, jelly-like texture. Unique floral, wine-like character",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 213,
            name = "Chestnut Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Dark amber, slightly bitter, woody notes. European specialty",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 214,
            name = "Mesquite Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Southwestern honey with warm, earthy character",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 215,
            name = "Star Thistle Honey",
            type = IngredientType.ADJUNCT,
            category = "Honey",
            beverageTypes = "mead",
            description = "Slow to crystallize, light flavor, excellent mead honey",
            currentStock = 0.0,
            unit = "lb"
        ),

        // NUTS & SEEDS - NEW CATEGORY FOR MEAD MAKING
        Ingredient(
            id = 600,
            name = "Raw Almonds",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Classic metheglin addition, subtle sweetness and nuttiness",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 601,
            name = "English Walnuts",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Rich, earthy flavor perfect for fall and winter meads",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 602,
            name = "Hazelnuts",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Buttery character, pairs excellently with chocolate flavors",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 603,
            name = "Pecans",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Southern comfort in mead form. Rich, buttery sweetness",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 604,
            name = "Pistachios",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Unique nutty-sweet profile with subtle green flavor",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 605,
            name = "Pine Nuts",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Delicate, resinous notes. Expensive but unique character",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 606,
            name = "Macadamia Nuts",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Rich, tropical character with buttery smoothness",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 607,
            name = "Brazil Nuts",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Intense, earthy flavor. High in selenium",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 608,
            name = "Chestnuts",
            type = IngredientType.ADJUNCT,
            category = "Nuts",
            beverageTypes = "mead,wine",
            description = "Sweet, starchy addition perfect for winter holiday meads",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 609,
            name = "Pumpkin Seeds",
            type = IngredientType.ADJUNCT,
            category = "Seeds",
            beverageTypes = "mead,beer",
            description = "Earthy, autumn character. Great for seasonal meads",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 610,
            name = "Sunflower Seeds",
            type = IngredientType.ADJUNCT,
            category = "Seeds",
            beverageTypes = "mead,beer",
            description = "Mild nutty flavor with earthy undertones",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 611,
            name = "Sesame Seeds",
            type = IngredientType.ADJUNCT,
            category = "Seeds",
            beverageTypes = "mead",
            description = "Toasted for depth, adds Middle Eastern character",
            currentStock = 0.0,
            unit = "oz"
        ),

        // WILD & EXOTIC BERRIES
        Ingredient(
            id = 250,
            name = "Elderberries",
            type = IngredientType.FRUIT,
            category = "Berry",
            beverageTypes = "mead,wine",
            description = "Dark, tart berries for complex melomel. High tannins",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 251,
            name = "Black Currants",
            type = IngredientType.FRUIT,
            category = "Berry",
            beverageTypes = "mead,wine",
            description = "Intense dark fruit flavor, high acidity for melomel",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 252,
            name = "Blackberries",
            type = IngredientType.FRUIT,
            category = "Berry",
            beverageTypes = "mead,wine,beer",
            description = "Rich, complex berry character. Traditional melomel fruit",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 253,
            name = "Raspberries",
            type = IngredientType.FRUIT,
            category = "Berry",
            beverageTypes = "mead,wine,beer",
            description = "Bright, tart berry flavor. Excellent in melomel",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 254,
            name = "Sweet Cherries",
            type = IngredientType.FRUIT,
            category = "Stone Fruit",
            beverageTypes = "mead,wine,beer",
            description = "Rich, sweet fruit character. Classic melomel addition",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 255,
            name = "Tart Cherries",
            type = IngredientType.FRUIT,
            category = "Stone Fruit",
            beverageTypes = "mead,wine,beer",
            description = "Bright acidity, complex flavor. Balances honey sweetness",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 256,
            name = "Peaches",
            type = IngredientType.FRUIT,
            category = "Stone Fruit",
            beverageTypes = "mead,wine,beer",
            description = "Soft, sweet, aromatic. Classic summer melomel",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 257,
            name = "Apricots",
            type = IngredientType.FRUIT,
            category = "Stone Fruit",
            beverageTypes = "mead,wine,beer",
            description = "Delicate, floral sweetness. Pairs well with light honeys",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 258,
            name = "Pomegranate",
            type = IngredientType.FRUIT,
            category = "Berry",
            beverageTypes = "mead,wine",
            description = "Tart, astringent, high antioxidants. Adds complexity",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 259,
            name = "Dragon Fruit",
            type = IngredientType.FRUIT,
            category = "Tropical",
            beverageTypes = "mead,wine",
            description = "Mild, subtle flavor with striking appearance",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 260,
            name = "Passion Fruit",
            type = IngredientType.FRUIT,
            category = "Tropical",
            beverageTypes = "mead,wine,beer",
            description = "Intense tropical aroma and flavor. Very aromatic",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 261,
            name = "Lychee",
            type = IngredientType.FRUIT,
            category = "Tropical",
            beverageTypes = "mead,wine",
            description = "Floral, grape-like flavor. Elegant in light meads",
            currentStock = 0.0,
            unit = "lb"
        ),
        
        // ADDITIONAL WILD BERRIES
        Ingredient(
            id = 262,
            name = "Huckleberries",
            type = IngredientType.FRUIT,
            category = "Wild Berry",
            beverageTypes = "mead,wine",
            description = "Intense, wild flavor similar to blueberries but more complex",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 263,
            name = "Gooseberries",
            type = IngredientType.FRUIT,
            category = "Wild Berry",
            beverageTypes = "mead,wine",
            description = "Tart, wine-like character. European traditional mead fruit",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 264,
            name = "Chokeberries (Aronia)",
            type = IngredientType.FRUIT,
            category = "Wild Berry",
            beverageTypes = "mead,wine",
            description = "Extremely high antioxidants, astringent, complex flavor",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 265,
            name = "Sea Buckthorn",
            type = IngredientType.FRUIT,
            category = "Wild Berry",
            beverageTypes = "mead,wine",
            description = "Vitamin C powerhouse, tangy orange berries. Nordic superfruit",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 266,
            name = "Lingonberries",
            type = IngredientType.FRUIT,
            category = "Wild Berry",
            beverageTypes = "mead,wine",
            description = "Scandinavian superfruit, tart and bright. Traditional Nordic mead",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 267,
            name = "Cloudberries",
            type = IngredientType.FRUIT,
            category = "Wild Berry",
            beverageTypes = "mead,wine",
            description = "Rare Nordic delicacy. Golden berries with complex flavor",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 268,
            name = "Boysenberries",
            type = IngredientType.FRUIT,
            category = "Hybrid Berry",
            beverageTypes = "mead,wine,beer",
            description = "Blackberry-raspberry hybrid with complex sweet-tart flavor",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 269,
            name = "Marionberries",
            type = IngredientType.FRUIT,
            category = "Specialty Berry",
            beverageTypes = "mead,wine,beer",
            description = "Pacific Northwest specialty. Complex blackberry character",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 270,
            name = "Acai Berries",
            type = IngredientType.FRUIT,
            category = "Superfruit",
            beverageTypes = "mead,wine",
            description = "Brazilian superfruit with earthy, wine-like flavor",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 271,
            name = "Goji Berries",
            type = IngredientType.FRUIT,
            category = "Superfruit",
            beverageTypes = "mead,wine",
            description = "Chinese superfruit, sweet-tart with herbal undertones",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 272,
            name = "Rambutan",
            type = IngredientType.FRUIT,
            category = "Tropical",
            beverageTypes = "mead,wine",
            description = "Lychee-like tropical fruit, floral and sweet",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 273,
            name = "Mangosteen",
            type = IngredientType.FRUIT,
            category = "Tropical",
            beverageTypes = "mead,wine",
            description = "Queen of fruits, complex sweet-tart tropical flavor",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 274,
            name = "Feijoa",
            type = IngredientType.FRUIT,
            category = "Tropical",
            beverageTypes = "mead,wine",
            description = "Pineapple-guava hybrid with aromatic, tropical character",
            currentStock = 0.0,
            unit = "lb"
        ),

        // SPICES & BOTANICALS - METHEGLIN FOCUSED (EXPANDED)
        Ingredient(
            id = 300,
            name = "Ceylon Cinnamon",
            type = IngredientType.SPICE,
            category = "Warming",
            beverageTypes = "mead,beer,wine",
            description = "True cinnamon, delicate and sweet. Perfect for metheglin",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 301,
            name = "Madagascar Vanilla Beans",
            type = IngredientType.SPICE,
            category = "Sweet",
            beverageTypes = "mead,beer",
            description = "Premium vanilla with complex flavor. Split and scrape for best results",
            currentStock = 0.0,
            unit = "each"
        ),
        Ingredient(
            id = 302,
            name = "Cardamom Pods",
            type = IngredientType.SPICE,
            category = "Warming",
            beverageTypes = "mead,beer",
            description = "Aromatic, slightly citrusy spice. Crush lightly before use",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 303,
            name = "Star Anise",
            type = IngredientType.SPICE,
            category = "Warming",
            beverageTypes = "mead,beer",
            description = "Licorice-like flavor. Use sparingly - very potent",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 304,
            name = "Whole Cloves",
            type = IngredientType.SPICE,
            category = "Warming",
            beverageTypes = "mead,beer,wine",
            description = "Intense, warming spice. Traditional in winter meads",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 305,
            name = "Fresh Ginger Root",
            type = IngredientType.SPICE,
            category = "Warming",
            beverageTypes = "mead,beer",
            description = "Spicy, warming heat. Peel and slice for metheglin",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 306,
            name = "Dried Lavender Buds",
            type = IngredientType.SPICE,
            category = "Floral",
            beverageTypes = "mead",
            description = "Delicate floral notes. Use culinary grade only",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 307,
            name = "Rose Petals",
            type = IngredientType.SPICE,
            category = "Floral",
            beverageTypes = "mead,wine",
            description = "Romantic floral character. Use pesticide-free petals",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 308,
            name = "Chamomile Flowers",
            type = IngredientType.SPICE,
            category = "Floral",
            beverageTypes = "mead",
            description = "Gentle, apple-like floral notes. Soothing character",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 309,
            name = "Hibiscus Flowers",
            type = IngredientType.SPICE,
            category = "Floral",
            beverageTypes = "mead,wine",
            description = "Tart, cranberry-like flavor. Beautiful red color",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 310,
            name = "Orange Zest",
            type = IngredientType.SPICE,
            category = "Citrus",
            beverageTypes = "mead,beer",
            description = "Bright citrus oils. Use organic oranges only",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 311,
            name = "Lemon Zest",
            type = IngredientType.SPICE,
            category = "Citrus",
            beverageTypes = "mead,beer",
            description = "Sharp, bright citrus character. Avoid white pith",
            currentStock = 0.0,
            unit = "oz"
        ),

        // EXOTIC SPICES - NEW ADDITIONS
        Ingredient(
            id = 312,
            name = "Grains of Paradise",
            type = IngredientType.SPICE,
            category = "Exotic",
            beverageTypes = "mead,beer",
            description = "African pepper with complex heat and floral notes",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 313,
            name = "Long Pepper",
            type = IngredientType.SPICE,
            category = "Exotic",
            beverageTypes = "mead,wine",
            description = "Ancient spice, different from black pepper. Sweet then hot",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 314,
            name = "Pink Peppercorns",
            type = IngredientType.SPICE,
            category = "Exotic",
            beverageTypes = "mead,wine",
            description = "Fruity, mild heat with sweet undertones",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 315,
            name = "Szechuan Peppercorns",
            type = IngredientType.SPICE,
            category = "Exotic",
            beverageTypes = "mead",
            description = "Numbing, citrusy sensation. Unique mouthfeel",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 316,
            name = "Sumac",
            type = IngredientType.SPICE,
            category = "Exotic",
            beverageTypes = "mead,wine",
            description = "Tart, lemony Middle Eastern spice. Natural acidity",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 317,
            name = "Juniper Berries",
            type = IngredientType.SPICE,
            category = "Botanical",
            beverageTypes = "mead,beer",
            description = "Gin-like, piney flavor. Traditional European botanical",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 318,
            name = "Angelica Root",
            type = IngredientType.SPICE,
            category = "Botanical",
            beverageTypes = "mead,wine",
            description = "Traditional European herb, gin botanical. Earthy, herbal",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 319,
            name = "Orris Root",
            type = IngredientType.SPICE,
            category = "Botanical",
            beverageTypes = "mead,wine",
            description = "Violet-scented, fixative properties. Floral complexity",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 320,
            name = "Gentian Root",
            type = IngredientType.SPICE,
            category = "Botanical",
            beverageTypes = "mead,wine",
            description = "Bitter, complex medicinal herb. Adds depth and complexity",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 321,
            name = "Elderflowers",
            type = IngredientType.SPICE,
            category = "Floral",
            beverageTypes = "mead,wine",
            description = "Classic European addition. Delicate, muscat-like flavor",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 322,
            name = "Jasmine Flowers",
            type = IngredientType.SPICE,
            category = "Floral",
            beverageTypes = "mead,wine",
            description = "Intensely floral, perfumed. Use very sparingly",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 323,
            name = "Lemon Verbena",
            type = IngredientType.SPICE,
            category = "Herbal",
            beverageTypes = "mead",
            description = "Intense lemon flavor without acidity. Aromatic herb",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 324,
            name = "Bee Balm",
            type = IngredientType.SPICE,
            category = "Herbal",
            beverageTypes = "mead",
            description = "Minty, citrusy wild herb. Traditional Native American botanical",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 325,
            name = "Meadowsweet",
            type = IngredientType.SPICE,
            category = "Herbal",
            beverageTypes = "mead",
            description = "Honey-almond flavor, natural aspirin. Traditional mead herb",
            currentStock = 0.0,
            unit = "oz"
        ),

        // ADAPTOGENS & FUNCTIONAL MUSHROOMS
        Ingredient(
            id = 326,
            name = "Reishi Mushroom",
            type = IngredientType.OTHER,
            category = "Adaptogen",
            beverageTypes = "mead",
            description = "Earthy, medicinal properties. Immune support and stress relief",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 327,
            name = "Chaga Mushroom",
            type = IngredientType.OTHER,
            category = "Adaptogen",
            beverageTypes = "mead",
            description = "Birch-like flavor, antioxidant rich. Natural energy support",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 328,
            name = "Ashwagandha",
            type = IngredientType.OTHER,
            category = "Adaptogen",
            beverageTypes = "mead",
            description = "Adaptogenic herb for stress reduction and vitality",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 329,
            name = "Rhodiola Root",
            type = IngredientType.OTHER,
            category = "Adaptogen",
            beverageTypes = "mead",
            description = "Stress-reducing botanical with earthy, rose-like notes",
            currentStock = 0.0,
            unit = "oz"
        ),

        // SPECIALTY SUGARS & SWEETENERS
        Ingredient(
            id = 330,
            name = "Coconut Sugar",
            type = IngredientType.ADJUNCT,
            category = "Specialty Sugar",
            beverageTypes = "mead,wine",
            description = "Tropical, caramel notes with lower glycemic index",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 331,
            name = "Date Sugar",
            type = IngredientType.ADJUNCT,
            category = "Specialty Sugar",
            beverageTypes = "mead,wine",
            description = "Concentrated date sweetness with complex fruit notes",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 332,
            name = "Maple Sugar",
            type = IngredientType.ADJUNCT,
            category = "Specialty Sugar",
            beverageTypes = "mead,beer",
            description = "Concentrated maple flavor, traditional North American sweetener",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 333,
            name = "Jaggery",
            type = IngredientType.ADJUNCT,
            category = "Specialty Sugar",
            beverageTypes = "mead,wine",
            description = "Indian palm sugar with complex, molasses-like flavor",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 334,
            name = "Muscovado Sugar",
            type = IngredientType.ADJUNCT,
            category = "Specialty Sugar",
            beverageTypes = "mead,wine",
            description = "Unrefined cane sugar with rich molasses character",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 335,
            name = "Black Strap Molasses",
            type = IngredientType.ADJUNCT,
            category = "Specialty Sugar",
            beverageTypes = "mead,beer",
            description = "Intense, mineral-rich sweetener with robust flavor",
            currentStock = 0.0,
            unit = "lb"
        ),

        // TEA & COFFEE ADDITIONS
        Ingredient(
            id = 336,
            name = "Earl Grey Tea",
            type = IngredientType.OTHER,
            category = "Tea",
            beverageTypes = "mead",
            description = "Bergamot-scented black tea for aromatic complexity",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 337,
            name = "Oolong Tea",
            type = IngredientType.OTHER,
            category = "Tea",
            beverageTypes = "mead,wine",
            description = "Complex, partially fermented tea with fruit and floral notes",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 338,
            name = "White Tea",
            type = IngredientType.OTHER,
            category = "Tea",
            beverageTypes = "mead",
            description = "Delicate, subtle flavor with light sweetness",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 339,
            name = "Rooibos Tea",
            type = IngredientType.OTHER,
            category = "Tea",
            beverageTypes = "mead",
            description = "Caffeine-free red bush tea with natural vanilla notes",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 340,
            name = "Cold Brew Coffee",
            type = IngredientType.OTHER,
            category = "Coffee",
            beverageTypes = "mead,beer",
            description = "Smooth, less acidic coffee character perfect for coffee meads",
            currentStock = 0.0,
            unit = "fl oz"
        ),

        // ADDITIONAL HOPS
        Ingredient(
            id = 401,
            name = "Simcoe",
            type = IngredientType.HOP,
            category = "Dual Purpose",
            beverageTypes = "beer",
            alphaAcidPercentage = 12.0,
            description = "Pine, fruit, earthy complexity. Unique character",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 402,
            name = "Mosaic",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 11.5,
            description = "Tropical fruit, berry, herbal. Complex flavor profile",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 403,
            name = "Galaxy",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 13.0,
            description = "Passion fruit, peach. Australian tropical character",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 404,
            name = "Nelson Sauvin",
            type = IngredientType.HOP,
            category = "Aroma",
            beverageTypes = "beer",
            alphaAcidPercentage = 12.0,
            description = "White wine, gooseberry. New Zealand's signature hop",
            currentStock = 0.0,
            unit = "oz"
        ),

        // SPECIALTY GRAINS
        Ingredient(
            id = 450,
            name = "Carapils (Dextrine Malt)",
            type = IngredientType.GRAIN,
            category = "Specialty",
            beverageTypes = "beer",
            colorLovibond = 1.5,
            ppgExtract = 33.0,
            description = "Body and head retention without adding flavor",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 451,
            name = "Aromatic Malt",
            type = IngredientType.GRAIN,
            category = "Specialty",
            beverageTypes = "beer",
            colorLovibond = 26.0,
            ppgExtract = 36.0,
            description = "Intense malt aroma and flavor. Use up to 10%",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 452,
            name = "Special B",
            type = IngredientType.GRAIN,
            category = "Specialty",
            beverageTypes = "beer",
            colorLovibond = 180.0,
            ppgExtract = 31.0,
            description = "Heavy caramel, raisin, plum flavors. Belgian character",
            currentStock = 0.0,
            unit = "lb"
        ),

        // WINE-SPECIFIC INGREDIENTS
        Ingredient(
            id = 700,
            name = "Pinot Noir Grapes",
            type = IngredientType.FRUIT,
            category = "Wine Grape",
            beverageTypes = "wine,mead",
            description = "Elegant red wine grape variety. Light to medium body",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 701,
            name = "Chardonnay Grapes",
            type = IngredientType.FRUIT,
            category = "Wine Grape",
            beverageTypes = "wine,mead",
            description = "Classic white wine grape. Full-bodied, versatile",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 702,
            name = "Riesling Grapes",
            type = IngredientType.FRUIT,
            category = "Wine Grape",
            beverageTypes = "wine,mead",
            description = "Aromatic white grape with floral character",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 703,
            name = "Grape Tannin Powder",
            type = IngredientType.ADJUNCT,
            category = "Wine Additive",
            beverageTypes = "wine,mead",
            description = "Adds structure and mouthfeel to wines and meads",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 704,
            name = "Pectic Enzyme",
            type = IngredientType.OTHER,
            category = "Processing Aid",
            beverageTypes = "wine,mead",
            description = "Breaks down fruit pectin for clearer wines and meads",
            currentStock = 0.0,
            unit = "oz"
        ),
        Ingredient(
            id = 705,
            name = "Acid Blend",
            type = IngredientType.ACID,
            category = "Wine Additive",
            beverageTypes = "wine,mead",
            description = "Tartaric, malic, citric acid blend for pH adjustment",
            currentStock = 0.0,
            unit = "oz"
        ),

        // FINAL ADJUNCTS & SUGARS
        Ingredient(
            id = 500,
            name = "Corn Sugar (Dextrose)",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            beverageTypes = "beer,mead,wine",
            description = "Pure fermentable sugar. Lightens body, increases ABV",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 501,
            name = "Belgian Candi Sugar (Clear)",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            beverageTypes = "beer",
            description = "Invert sugar for Belgian ales. Clean fermentation",
            currentStock = 0.0,
            unit = "lb"
        ),
        Ingredient(
            id = 502,
            name = "Turbinado Sugar",
            type = IngredientType.ADJUNCT,
            category = "Sugar",
            beverageTypes = "beer,mead",
            description = "Raw sugar with molasses character. Adds complexity",
            currentStock = 0.0,
            unit = "lb"
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