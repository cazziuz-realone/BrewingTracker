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
    version = 5,  // Incremented to 5 for complete ingredient database expansion
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
    
    // COMPREHENSIVE INGREDIENT DATABASE - 100+ Ingredients
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
        ),

        // ===== EXPANDED INGREDIENT DATABASE - YEATS & NUTRIENTS =====
        
        // ALE YEASTS - American
        Ingredient(100, "Wyeast 1056 American Ale", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "Clean, crisp, neutral. Temp: 60-72°F. Attenuation: 73-77%", currentStock = 0.0, unit = "pack"),
        Ingredient(101, "White Labs WLP001 California Ale", IngredientType.YEAST_NUTRIENT, "Ale", "beer", 
            description = "Clean American strain. Temp: 68-73°F. Attenuation: 73-80%", currentStock = 0.0, unit = "pack"),
        Ingredient(102, "Safale US-05", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "Dry American ale yeast. Temp: 59-75°F. Attenuation: 78-82%", currentStock = 0.0, unit = "pack"),

        // ALE YEASTS - English
        Ingredient(103, "Wyeast 1968 London ESB", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "Malty, balanced English character. Temp: 64-72°F. Attenuation: 67-71%", currentStock = 0.0, unit = "pack"),
        Ingredient(104, "White Labs WLP002 English Ale", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "Classic British strain. Temp: 65-68°F. Attenuation: 63-70%", currentStock = 0.0, unit = "pack"),
        Ingredient(105, "Safale S-04", IngredientType.YEAST_NUTRIENT, "Ale", "beer", 
            description = "Dry English ale yeast. Temp: 59-75°F. Attenuation: 69-82%", currentStock = 0.0, unit = "pack"),
        Ingredient(106, "Wyeast 1318 London Ale III", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "Dry, tart, crisp character. Temp: 66-71°F. Attenuation: 71-75%", currentStock = 0.0, unit = "pack"),
        Ingredient(107, "White Labs WLP007 Dry English Ale", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "High attenuation, clean finish. Temp: 65-70°F. Attenuation: 70-80%", currentStock = 0.0, unit = "pack"),

        // ALE YEASTS - Belgian
        Ingredient(108, "Wyeast 3787 Trappist High Gravity", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "Spicy phenolics, fruity esters. Temp: 64-78°F. Attenuation: 75-85%", currentStock = 0.0, unit = "pack"),
        Ingredient(109, "White Labs WLP530 Abbey Ale", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "Complex Belgian character. Temp: 66-72°F. Attenuation: 75-80%", currentStock = 0.0, unit = "pack"),
        Ingredient(110, "Lallemand Abbaye", IngredientType.YEAST_NUTRIENT, "Ale", "beer",
            description = "Dry Belgian abbey yeast. Temp: 59-77°F. Attenuation: 78-84%", currentStock = 0.0, unit = "pack"),

        // LAGER YEASTS
        Ingredient(111, "Wyeast 2124 Bohemian Lager", IngredientType.YEAST_NUTRIENT, "Lager", "beer",
            description = "Rich, full malty character. Temp: 48-58°F. Attenuation: 69-73%", currentStock = 0.0, unit = "pack"),
        Ingredient(112, "White Labs WLP830 German Lager", IngredientType.YEAST_NUTRIENT, "Lager", "beer", 
            description = "Malty, clean German character. Temp: 50-55°F. Attenuation: 74-79%", currentStock = 0.0, unit = "pack"),
        Ingredient(113, "Saflager W-34/70", IngredientType.YEAST_NUTRIENT, "Lager", "beer",
            description = "Dry German lager yeast. Temp: 48-59°F. Attenuation: 80-84%", currentStock = 0.0, unit = "pack"),
        Ingredient(114, "Wyeast 2007 Pilsen Lager", IngredientType.YEAST_NUTRIENT, "Lager", "beer",
            description = "Classic Pilsner character. Temp: 46-56°F. Attenuation: 71-75%", currentStock = 0.0, unit = "pack"),

        // MEAD YEASTS - PREMIUM SELECTION
        Ingredient(115, "Lallemand DistilaMax MW", IngredientType.YEAST_NUTRIENT, "Mead", "mead",
            description = "High alcohol tolerance mead specialist. Clean fermentation. 20% ABV potential", currentStock = 0.0, unit = "pack"),
        Ingredient(116, "Red Star Premier Blanc", IngredientType.YEAST_NUTRIENT, "Mead", "wine,mead",
            description = "Neutral character, high alcohol. Temp: 59-86°F. 18% ABV", currentStock = 0.0, unit = "pack"),
        Ingredient(117, "Wyeast 4184 Sweet Mead", IngredientType.YEAST_NUTRIENT, "Mead", "mead",
            description = "Low attenuation for sweet meads. Temp: 65-75°F. Leaves residual honey", currentStock = 0.0, unit = "pack"),
        Ingredient(118, "White Labs WLP720 Sweet Mead", IngredientType.YEAST_NUTRIENT, "Mead", "mead",
            description = "Low attenuation, honey character. Temp: 70-75°F. Sweet finish", currentStock = 0.0, unit = "pack"),
        Ingredient(119, "Lallemand 71B-1122", IngredientType.YEAST_NUTRIENT, "Mead", "wine,mead",
            description = "Reduces malic acid, softens harsh edges. Perfect for fruit meads", currentStock = 0.0, unit = "pack"),
        Ingredient(120, "Red Star Cuvée", IngredientType.YEAST_NUTRIENT, "Mead", "wine,mead",
            description = "Enhances varietal character. Great for traditional meads", currentStock = 0.0, unit = "pack"),
        Ingredient(121, "Wyeast 4632 Dry Mead", IngredientType.YEAST_NUTRIENT, "Mead", "mead",
            description = "High attenuation for dry meads. Temp: 65-75°F. Clean finish", currentStock = 0.0, unit = "pack"),

        // WINE YEASTS (Also excellent for mead)
        Ingredient(122, "Lallemand EC-1118", IngredientType.YEAST_NUTRIENT, "Wine", "wine,mead",
            description = "Champagne yeast. High alcohol tolerance 18%. Temp: 50-86°F", currentStock = 0.0, unit = "pack"),
        Ingredient(123, "Lallemand D47", IngredientType.YEAST_NUTRIENT, "Wine", "wine,mead",
            description = "White wine, low foaming. Temp: 59-68°F. 15% ABV", currentStock = 0.0, unit = "pack"),
        Ingredient(124, "Red Star Côte des Blancs", IngredientType.YEAST_NUTRIENT, "Wine", "wine,mead",
            description = "White wine, fruit wines. Temp: 64-86°F. 14% ABV", currentStock = 0.0, unit = "pack"),

        // SPECIALTY YEASTS
        Ingredient(125, "Omega Voss Kveik", IngredientType.YEAST_NUTRIENT, "Kveik", "beer",
            description = "Fast fermenting, citrus character. Temp: 68-98°F!", currentStock = 0.0, unit = "pack"),
        Ingredient(126, "White Labs WLP644 Sacch Trois", IngredientType.YEAST_NUTRIENT, "Wild", "beer",
            description = "Tropical fruit character. Temp: 70-85°F", currentStock = 0.0, unit = "pack"),

        // YEAST NUTRIENTS - ESSENTIAL FOR MEAD MAKING
        Ingredient(127, "Fermaid O", IngredientType.YEAST_NUTRIENT, "Nutrient", "wine,mead",
            description = "Organic yeast nutrient. Use 1g/gallon for healthy fermentation", currentStock = 0.0, unit = "oz"),
        Ingredient(128, "Go-Ferm", IngredientType.YEAST_NUTRIENT, "Nutrient", "wine,mead", 
            description = "Yeast rehydration nutrient. Use before pitching yeast", currentStock = 0.0, unit = "oz"),
        Ingredient(129, "DAP (Diammonium Phosphate)", IngredientType.YEAST_NUTRIENT, "Nutrient", "wine,mead",
            description = "Nitrogen source for yeast health. Use sparingly - can create off flavors", currentStock = 0.0, unit = "oz"),
        Ingredient(130, "Yeast Hulls", IngredientType.YEAST_NUTRIENT, "Nutrient", "wine,mead",
            description = "Dead yeast cells for nutrition. Prevents H2S production", currentStock = 0.0, unit = "oz"),
        Ingredient(131, "Fermaid K", IngredientType.YEAST_NUTRIENT, "Nutrient", "wine,mead",
            description = "Balanced yeast nutrient blend. Use throughout fermentation", currentStock = 0.0, unit = "oz"),
        Ingredient(132, "Yeast Energizer", IngredientType.YEAST_NUTRIENT, "Nutrient", "wine,mead",
            description = "Comprehensive nutrient blend with vitamins and minerals", currentStock = 0.0, unit = "oz"),

        // HONEY VARIETIES - PREMIUM MEAD SELECTION
        Ingredient(200, "Wildflower Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Complex, varied flavor from mixed wildflowers. Most common mead honey", currentStock = 0.0, unit = "lb"),
        Ingredient(201, "Clover Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Light, delicate, classic honey flavor. Excellent for traditional meads", currentStock = 0.0, unit = "lb"),
        Ingredient(202, "Orange Blossom Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Light citrus notes, aromatic. Beautiful in metheglin and traditional", currentStock = 0.0, unit = "lb"),
        Ingredient(203, "Tupelo Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Premium Southern honey, buttery smooth, rarely crystallizes", currentStock = 0.0, unit = "lb"),
        Ingredient(204, "Sage Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Light color, mild flavor, slow to granulate. Ideal for delicate meads", currentStock = 0.0, unit = "lb"),
        Ingredient(205, "Buckwheat Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Dark, robust, malty flavors. High in antioxidants, complex character", currentStock = 0.0, unit = "lb"),
        Ingredient(206, "Lavender Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Delicate floral notes, aromatic. Perfect for metheglin", currentStock = 0.0, unit = "lb"),
        Ingredient(207, "Basswood Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Fresh, almost minty flavor. Light color, distinctive character", currentStock = 0.0, unit = "lb"),
        Ingredient(208, "Sourwood Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Spicy, buttery flavor. Prized Appalachian honey", currentStock = 0.0, unit = "lb"),
        Ingredient(209, "Acacia Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Very light, mild flavor, stays liquid. Perfect base honey", currentStock = 0.0, unit = "lb"),
        Ingredient(210, "Raw Unfiltered Wildflower", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Unpasteurized with natural enzymes and pollen. More complex fermentation", currentStock = 0.0, unit = "lb"),
        Ingredient(211, "Manuka Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Medicinal properties, earthy flavor. Premium New Zealand honey", currentStock = 0.0, unit = "lb"),
        Ingredient(212, "Heather Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Thick, jelly-like texture. Unique floral, wine-like character", currentStock = 0.0, unit = "lb"),
        Ingredient(213, "Chestnut Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Dark amber, slightly bitter, woody notes. European specialty", currentStock = 0.0, unit = "lb"),
        Ingredient(214, "Mesquite Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Southwestern honey with warm, earthy character", currentStock = 0.0, unit = "lb"),
        Ingredient(215, "Star Thistle Honey", IngredientType.ADJUNCT, "Honey", "mead",
            description = "Slow to crystallize, light flavor, excellent mead honey", currentStock = 0.0, unit = "lb"),

        // FRUITS - MELOMEL FOCUSED
        Ingredient(250, "Elderberries", IngredientType.FRUIT, "Berry", "mead,wine",
            description = "Dark, tart berries for complex melomel. High tannins", currentStock = 0.0, unit = "lb"),
        Ingredient(251, "Black Currants", IngredientType.FRUIT, "Berry", "mead,wine",
            description = "Intense dark fruit flavor, high acidity for melomel", currentStock = 0.0, unit = "lb"),
        Ingredient(252, "Blackberries", IngredientType.FRUIT, "Berry", "mead,wine,beer",
            description = "Rich, complex berry character. Traditional melomel fruit", currentStock = 0.0, unit = "lb"),
        Ingredient(253, "Raspberries", IngredientType.FRUIT, "Berry", "mead,wine,beer",
            description = "Bright, tart berry flavor. Excellent in melomel", currentStock = 0.0, unit = "lb"),
        Ingredient(254, "Sweet Cherries", IngredientType.FRUIT, "Stone Fruit", "mead,wine,beer",
            description = "Rich, sweet fruit character. Classic melomel addition", currentStock = 0.0, unit = "lb"),
        Ingredient(255, "Tart Cherries", IngredientType.FRUIT, "Stone Fruit", "mead,wine,beer",
            description = "Bright acidity, complex flavor. Balances honey sweetness", currentStock = 0.0, unit = "lb"),
        Ingredient(256, "Peaches", IngredientType.FRUIT, "Stone Fruit", "mead,wine,beer",
            description = "Soft, sweet, aromatic. Classic summer melomel", currentStock = 0.0, unit = "lb"),
        Ingredient(257, "Apricots", IngredientType.FRUIT, "Stone Fruit", "mead,wine,beer",
            description = "Delicate, floral sweetness. Pairs well with light honeys", currentStock = 0.0, unit = "lb"),
        Ingredient(258, "Pomegranate", IngredientType.FRUIT, "Berry", "mead,wine",
            description = "Tart, astringent, high antioxidants. Adds complexity", currentStock = 0.0, unit = "lb"),
        Ingredient(259, "Dragon Fruit", IngredientType.FRUIT, "Tropical", "mead,wine",
            description = "Mild, subtle flavor with striking appearance", currentStock = 0.0, unit = "lb"),
        Ingredient(260, "Passion Fruit", IngredientType.FRUIT, "Tropical", "mead,wine,beer",
            description = "Intense tropical aroma and flavor. Very aromatic", currentStock = 0.0, unit = "lb"),
        Ingredient(261, "Lychee", IngredientType.FRUIT, "Tropical", "mead,wine",
            description = "Floral, grape-like flavor. Elegant in light meads", currentStock = 0.0, unit = "lb"),

        // SPICES & BOTANICALS - METHEGLIN FOCUSED
        Ingredient(300, "Ceylon Cinnamon", IngredientType.SPICE, "Warming", "mead,beer,wine",
            description = "True cinnamon, delicate and sweet. Perfect for metheglin", currentStock = 0.0, unit = "oz"),
        Ingredient(301, "Madagascar Vanilla Beans", IngredientType.SPICE, "Sweet", "mead,beer",
            description = "Premium vanilla with complex flavor. Split and scrape for best results", currentStock = 0.0, unit = "each"),
        Ingredient(302, "Cardamom Pods", IngredientType.SPICE, "Warming", "mead,beer",
            description = "Aromatic, slightly citrusy spice. Crush lightly before use", currentStock = 0.0, unit = "oz"),
        Ingredient(303, "Star Anise", IngredientType.SPICE, "Warming", "mead,beer",
            description = "Licorice-like flavor. Use sparingly - very potent", currentStock = 0.0, unit = "oz"),
        Ingredient(304, "Whole Cloves", IngredientType.SPICE, "Warming", "mead,beer,wine",
            description = "Intense, warming spice. Traditional in winter meads", currentStock = 0.0, unit = "oz"),
        Ingredient(305, "Fresh Ginger Root", IngredientType.SPICE, "Warming", "mead,beer",
            description = "Spicy, warming heat. Peel and slice for metheglin", currentStock = 0.0, unit = "oz"),
        Ingredient(306, "Dried Lavender Buds", IngredientType.SPICE, "Floral", "mead",
            description = "Delicate floral notes. Use culinary grade only", currentStock = 0.0, unit = "oz"),
        Ingredient(307, "Rose Petals", IngredientType.SPICE, "Floral", "mead,wine",
            description = "Romantic floral character. Use pesticide-free petals", currentStock = 0.0, unit = "oz"),
        Ingredient(308, "Chamomile Flowers", IngredientType.SPICE, "Floral", "mead",
            description = "Gentle, apple-like floral notes. Soothing character", currentStock = 0.0, unit = "oz"),
        Ingredient(309, "Hibiscus Flowers", IngredientType.SPICE, "Floral", "mead,wine",
            description = "Tart, cranberry-like flavor. Beautiful red color", currentStock = 0.0, unit = "oz"),
        Ingredient(310, "Orange Zest", IngredientType.SPICE, "Citrus", "mead,beer",
            description = "Bright citrus oils. Use organic oranges only", currentStock = 0.0, unit = "oz"),
        Ingredient(311, "Lemon Zest", IngredientType.SPICE, "Citrus", "mead,beer",
            description = "Sharp, bright citrus character. Avoid white pith", currentStock = 0.0, unit = "oz"),

        // ADDITIONAL HOPS
        Ingredient(400, "Centennial", IngredientType.HOP, "Dual Purpose", "beer",
            alphaAcidPercentage = 10.0, description = "Citrus and floral, super Cascade. Classic American hop", currentStock = 0.0, unit = "oz"),
        Ingredient(401, "Simcoe", IngredientType.HOP, "Dual Purpose", "beer", 
            alphaAcidPercentage = 12.0, description = "Pine, fruit, earthy complexity. Unique character", currentStock = 0.0, unit = "oz"),
        Ingredient(402, "Mosaic", IngredientType.HOP, "Aroma", "beer",
            alphaAcidPercentage = 11.5, description = "Tropical fruit, berry, herbal. Complex flavor profile", currentStock = 0.0, unit = "oz"),
        Ingredient(403, "Galaxy", IngredientType.HOP, "Aroma", "beer",
            alphaAcidPercentage = 13.0, description = "Passion fruit, peach. Australian tropical character", currentStock = 0.0, unit = "oz"),
        Ingredient(404, "Nelson Sauvin", IngredientType.HOP, "Aroma", "beer",
            alphaAcidPercentage = 12.0, description = "White wine, gooseberry. New Zealand's signature hop", currentStock = 0.0, unit = "oz"),

        // SPECIALTY GRAINS
        Ingredient(450, "Carapils (Dextrine Malt)", IngredientType.GRAIN, "Specialty", "beer",
            colorLovibond = 1.5, ppgExtract = 33.0, description = "Body and head retention without adding flavor", currentStock = 0.0, unit = "lb"),
        Ingredient(451, "Aromatic Malt", IngredientType.GRAIN, "Specialty", "beer",
            colorLovibond = 26.0, ppgExtract = 36.0, description = "Intense malt aroma and flavor. Use up to 10%", currentStock = 0.0, unit = "lb"),
        Ingredient(452, "Special B", IngredientType.GRAIN, "Specialty", "beer",
            colorLovibond = 180.0, ppgExtract = 31.0, description = "Heavy caramel, raisin, plum flavors. Belgian character", currentStock = 0.0, unit = "lb"),

        // ADJUNCTS & SUGARS
        Ingredient(500, "Corn Sugar (Dextrose)", IngredientType.ADJUNCT, "Sugar", "beer,mead,wine",
            description = "Pure fermentable sugar. Lightens body, increases ABV", currentStock = 0.0, unit = "lb"),
        Ingredient(501, "Belgian Candi Sugar (Clear)", IngredientType.ADJUNCT, "Sugar", "beer",
            description = "Invert sugar for Belgian ales. Clean fermentation", currentStock = 0.0, unit = "lb"),
        Ingredient(502, "Turbinado Sugar", IngredientType.ADJUNCT, "Sugar", "beer,mead",
            description = "Raw sugar with molasses character. Adds complexity", currentStock = 0.0, unit = "lb")
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