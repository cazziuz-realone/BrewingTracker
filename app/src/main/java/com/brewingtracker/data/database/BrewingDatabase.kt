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
        Recipe::class,           // ← Recipe builder entities
        RecipeIngredient::class  // ← Recipe builder entities
    ],
    version = 9,  // INCREMENTED to force database recreation and fix ingredient population
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BrewingDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun yeastDao(): YeastDao
    abstract fun projectIngredientDao(): ProjectIngredientDao
    abstract fun recipeDao(): RecipeDao                    // ← Recipe builder DAOs
    abstract fun recipeIngredientDao(): RecipeIngredientDao // ← Recipe builder DAOs

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
                    // ALWAYS populate on create
                    populateDatabase(database)
                }
            }
        }
        
        // FIXED: Always populate on open to ensure 150+ ingredients exist
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val ingredientDao = database.ingredientDao()
                    val count = ingredientDao.getIngredientCount()
                    // FIXED: Changed from 100 to 150 to match our target ingredient count
                    if (count < 150) {
                        println("BrewingDatabase: Found only $count ingredients, repopulating with 150+...")
                        // Clear existing data first
                        try {
                            db.execSQL("DELETE FROM ingredients WHERE 1=1")
                            db.execSQL("DELETE FROM yeasts WHERE 1=1") 
                        } catch (e: Exception) {
                            println("Database cleanup error (expected): ${e.message}")
                        }
                        populateDatabase(database)
                    } else {
                        println("BrewingDatabase: Found $count ingredients - database properly populated")
                    }
                }
            }
        }
    }
}

// COMPREHENSIVE INGREDIENT DATABASE - GUARANTEED 150+ ingredients
private suspend fun populateDatabase(database: BrewingDatabase) {
    val ingredientDao = database.ingredientDao()
    val yeastDao = database.yeastDao()
    
    println("BrewingDatabase: Starting ingredient population...")
    
    // COMPREHENSIVE INGREDIENT DATABASE - 150+ Brewing Ingredients
    val ingredients = listOf(
        // === BASE MALTS & GRAINS ===
        Ingredient(1, "Pale 2-Row", IngredientType.GRAIN, "Base Malt", "Standard base malt for ales and lagers", "beer", 1.8, null, 37.0, null, 10.0, "lbs"),
        Ingredient(2, "Pilsner Malt", IngredientType.GRAIN, "Base Malt", "Light, crisp base malt for lagers", "beer", 1.5, null, 37.0, null, 8.0, "lbs"),
        Ingredient(3, "Maris Otter", IngredientType.GRAIN, "Base Malt", "Traditional English ale malt", "beer", 3.0, null, 36.0, null, 5.0, "lbs"),
        Ingredient(4, "Vienna Malt", IngredientType.GRAIN, "Base Malt", "Adds biscuity, toasty character", "beer", 3.5, null, 35.0, null, 4.0, "lbs"),
        Ingredient(5, "Munich Malt", IngredientType.GRAIN, "Base Malt", "Rich malty flavor, amber color", "beer", 8.0, null, 35.0, null, 3.0, "lbs"),
        Ingredient(6, "Wheat Malt", IngredientType.GRAIN, "Base Malt", "For wheat beers, adds body", "beer", 2.5, null, 36.0, null, 2.0, "lbs"),
        
        // === SPECIALTY MALTS ===
        Ingredient(7, "Crystal 40L", IngredientType.GRAIN, "Crystal", "Light caramel sweetness", "beer", 40.0, null, 34.0, null, 2.0, "lbs"),
        Ingredient(8, "Crystal 60L", IngredientType.GRAIN, "Crystal", "Medium caramel character", "beer", 60.0, null, 34.0, null, 2.0, "lbs"),
        Ingredient(9, "Crystal 120L", IngredientType.GRAIN, "Crystal", "Dark caramel, raisin notes", "beer", 120.0, null, 33.0, null, 1.0, "lbs"),
        Ingredient(10, "Chocolate Malt", IngredientType.GRAIN, "Roasted", "Chocolate, coffee notes", "beer", 350.0, null, 28.0, null, 1.0, "lbs"),
        Ingredient(11, "Black Patent", IngredientType.GRAIN, "Roasted", "Roasted, burnt flavors", "beer", 500.0, null, 25.0, null, 0.5, "lbs"),
        Ingredient(12, "Roasted Barley", IngredientType.GRAIN, "Roasted", "Coffee, dry stout character", "beer", 300.0, null, 25.0, null, 0.5, "lbs"),
        
        // === HONEY VARIETIES (EXPANDED) ===
        Ingredient(13, "Wildflower Honey", IngredientType.FRUIT, "Honey", "Pure wildflower honey for mead making", "mead,beer", null, null, 35.0, null, 12.0, "lbs"),
        Ingredient(14, "Orange Blossom Honey", IngredientType.FRUIT, "Honey", "Delicate floral honey with citrus notes", "mead", null, null, 35.0, null, 6.0, "lbs"),
        Ingredient(15, "Clover Honey", IngredientType.FRUIT, "Honey", "Light, delicate, classic honey flavor", "mead", null, null, 35.0, null, 8.0, "lbs"),
        Ingredient(16, "Buckwheat Honey", IngredientType.FRUIT, "Honey", "Dark, robust, earthy honey", "mead", null, null, 35.0, null, 3.0, "lbs"),
        Ingredient(17, "Tupelo Honey", IngredientType.FRUIT, "Honey", "Premium southern honey, won't crystallize", "mead", null, null, 35.0, null, 2.0, "lbs"),
        Ingredient(18, "Manuka Honey", IngredientType.FRUIT, "Honey", "New Zealand medicinal honey", "mead", null, null, 35.0, null, 1.0, "lbs"),
        Ingredient(19, "Sage Honey", IngredientType.FRUIT, "Honey", "Herbal, light colored honey", "mead", null, null, 35.0, null, 2.0, "lbs"),
        Ingredient(20, "Acacia Honey", IngredientType.FRUIT, "Honey", "Very light, delicate floral honey", "mead", null, null, 35.0, null, 3.0, "lbs"),
        
        // === FRUITS FOR MELOMEL (EXPANDED) ===
        Ingredient(21, "Strawberries", IngredientType.FRUIT, "Berry", "Fresh strawberries for fruit wines and meads", "mead,wine,beer", null, null, null, null, 2.0, "lbs"),
        Ingredient(22, "Blackberries", IngredientType.FRUIT, "Berry", "Rich, complex berry character for melomel", "mead,wine,beer", null, null, null, null, 1.5, "lbs"),
        Ingredient(23, "Blueberries", IngredientType.FRUIT, "Berry", "Sweet-tart berries, purple color", "mead,wine,beer", null, null, null, null, 3.0, "lbs"),
        Ingredient(24, "Raspberries", IngredientType.FRUIT, "Berry", "Intense berry flavor and aroma", "mead,wine,beer", null, null, null, null, 1.0, "lbs"),
        Ingredient(25, "Sweet Cherries", IngredientType.FRUIT, "Stone Fruit", "Rich, sweet fruit character for classic melomel", "mead,wine,beer", null, null, null, null, 2.0, "lbs"),
        Ingredient(26, "Tart Cherries", IngredientType.FRUIT, "Stone Fruit", "Sour cherry complexity", "mead,wine,beer", null, null, null, null, 2.0, "lbs"),
        Ingredient(27, "Peaches", IngredientType.FRUIT, "Stone Fruit", "Delicate stone fruit character", "mead,wine", null, null, null, null, 1.5, "lbs"),
        Ingredient(28, "Apricots", IngredientType.FRUIT, "Stone Fruit", "Sweet, floral stone fruit", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(29, "Plums", IngredientType.FRUIT, "Stone Fruit", "Rich, complex stone fruit", "mead,wine", null, null, null, null, 1.5, "lbs"),
        Ingredient(30, "Elderberries", IngredientType.FRUIT, "Berry", "Dark, wine-like berry character", "mead,wine", null, null, null, null, 1.0, "lbs"),
        
        // === CITRUS FRUITS ===
        Ingredient(31, "Orange Zest", IngredientType.FRUIT, "Citrus", "Fresh orange peel for citrus character", "mead,beer", null, null, null, null, 0.5, "oz"),
        Ingredient(32, "Lemon Zest", IngredientType.FRUIT, "Citrus", "Bright lemon peel for acidity", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(33, "Lime Zest", IngredientType.FRUIT, "Citrus", "Zesty lime character", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(34, "Grapefruit Zest", IngredientType.FRUIT, "Citrus", "Bitter citrus complexity", "mead,beer", null, null, null, null, 0.3, "oz"),
        
        // === SPICES FOR METHEGLIN (EXPANDED) ===
        Ingredient(35, "Ceylon Cinnamon", IngredientType.SPICE, "Warming", "True cinnamon, delicate and sweet for metheglin", "mead,beer,wine", null, null, null, null, 0.5, "oz"),
        Ingredient(36, "Madagascar Vanilla Beans", IngredientType.SPICE, "Sweet", "Premium vanilla, split and scrape for best results", "mead,beer", null, null, null, null, 2.0, "each"),
        Ingredient(37, "Cardamom Pods", IngredientType.SPICE, "Warming", "Aromatic, slightly citrusy spice", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(38, "Star Anise", IngredientType.SPICE, "Warming", "Licorice-like spice for winter meads", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(39, "Cloves", IngredientType.SPICE, "Warming", "Intense warming spice, use sparingly", "mead,beer", null, null, null, null, 0.1, "oz"),
        Ingredient(40, "Allspice Berries", IngredientType.SPICE, "Warming", "Complex warming spice", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(41, "Nutmeg", IngredientType.SPICE, "Warming", "Sweet, warm spice for holiday meads", "mead,beer", null, null, null, null, 0.1, "oz"),
        Ingredient(42, "Ginger Root", IngredientType.SPICE, "Warming", "Fresh ginger for heat and complexity", "mead,beer", null, null, null, null, 1.0, "oz"),
        Ingredient(43, "Black Peppercorns", IngredientType.SPICE, "Heat", "Adds heat and complexity", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(44, "Coriander Seeds", IngredientType.SPICE, "Citrus", "Citrusy, slightly spicy seeds", "mead,beer", null, null, null, null, 0.5, "oz"),
        
        // === HERBS & BOTANICALS ===
        Ingredient(45, "Lavender Buds", IngredientType.SPICE, "Floral", "Delicate floral character", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(46, "Rose Petals", IngredientType.SPICE, "Floral", "Romantic floral addition", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(47, "Chamomile Flowers", IngredientType.SPICE, "Floral", "Apple-like, soothing herb", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(48, "Rosemary", IngredientType.SPICE, "Herb", "Piney, aromatic herb", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(49, "Thyme", IngredientType.SPICE, "Herb", "Earthy, aromatic herb", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(50, "Sage", IngredientType.SPICE, "Herb", "Earthy, slightly bitter herb", "mead,beer", null, null, null, null, 0.2, "oz"),
        
        // === HOPS (EXPANDED) ===
        Ingredient(51, "Cascade", IngredientType.HOP, "Citrus", "American citrus hop", "beer", null, 5.5, null, null, 1.0, "oz"),
        Ingredient(52, "Centennial", IngredientType.HOP, "Citrus", "Floral, citrus American hop", "beer", null, 10.0, null, null, 0.5, "oz"),
        Ingredient(53, "Chinook", IngredientType.HOP, "Piney", "Grapefruit, spicy hop", "beer", null, 13.0, null, null, 0.5, "oz"),
        Ingredient(54, "Columbus", IngredientType.HOP, "Piney", "High alpha, bittering hop", "beer", null, 15.0, null, null, 0.3, "oz"),
        Ingredient(55, "Citra", IngredientType.HOP, "Citrus", "Tropical fruit, citrus hop", "beer", null, 12.0, null, null, 0.8, "oz"),
        Ingredient(56, "Mosaic", IngredientType.HOP, "Tropical", "Complex fruit, berry hop", "beer", null, 12.5, null, null, 0.6, "oz"),
        Ingredient(57, "Simcoe", IngredientType.HOP, "Piney", "Passion fruit, piney hop", "beer", null, 13.0, null, null, 0.4, "oz"),
        Ingredient(58, "Amarillo", IngredientType.HOP, "Citrus", "Orange, tropical hop", "beer", null, 9.5, null, null, 0.7, "oz"),
        Ingredient(59, "Saaz", IngredientType.HOP, "Noble", "Classic Czech noble hop", "beer", null, 3.5, null, null, 2.0, "oz"),
        Ingredient(60, "Hallertau", IngredientType.HOP, "Noble", "German noble hop", "beer", null, 4.0, null, null, 1.5, "oz"),
        
        // === CIDER INGREDIENTS ===
        Ingredient(61, "Apple Juice (Fresh)", IngredientType.FRUIT, "Juice", "Fresh-pressed apple juice for cider", "cider", null, null, null, null, 5.0, "gallons"),
        Ingredient(62, "Apple Juice Concentrate", IngredientType.FRUIT, "Juice", "Concentrated apple juice", "cider", null, null, null, null, 2.0, "cans"),
        Ingredient(63, "Pear Juice", IngredientType.FRUIT, "Juice", "Fresh pear juice for perry", "cider", null, null, null, null, 1.0, "gallons"),
        
        // === SUGARS & ADJUNCTS (EXPANDED) ===
        Ingredient(64, "Corn Sugar (Dextrose)", IngredientType.ADJUNCT, "Sugar", "Pure dextrose for priming and fermentation", "beer,mead,wine,cider", null, null, 46.0, null, 5.0, "lbs"),
        Ingredient(65, "Brown Sugar", IngredientType.ADJUNCT, "Sugar", "Adds molasses notes and color", "beer,mead", null, null, 43.0, null, 2.0, "lbs"),
        Ingredient(66, "Cane Sugar", IngredientType.ADJUNCT, "Sugar", "Pure cane sugar for fermentation", "beer,mead,wine", null, null, 46.0, null, 3.0, "lbs"),
        Ingredient(67, "Honey Malt", IngredientType.GRAIN, "Specialty", "Adds honey-like sweetness", "beer", 25.0, null, 35.0, null, 1.0, "lbs"),
        Ingredient(68, "Lactose", IngredientType.ADJUNCT, "Sugar", "Unfermentable milk sugar for sweetness", "beer", null, null, 0.0, null, 1.0, "lbs"),
        Ingredient(69, "Maple Syrup", IngredientType.ADJUNCT, "Sugar", "Pure maple syrup for unique character", "beer,mead", null, null, 30.0, null, 1.0, "quarts"),
        Ingredient(70, "Molasses", IngredientType.ADJUNCT, "Sugar", "Dark, rich sugar source", "beer,mead", null, null, 36.0, null, 0.5, "lbs"),
        
        // === WINE GRAPES & JUICES ===
        Ingredient(71, "Cabernet Sauvignon Juice", IngredientType.FRUIT, "Wine Grape", "Full-bodied red wine grape", "wine", null, null, null, null, 2.0, "gallons"),
        Ingredient(72, "Chardonnay Juice", IngredientType.FRUIT, "Wine Grape", "Classic white wine grape", "wine", null, null, null, null, 2.0, "gallons"),
        Ingredient(73, "Pinot Noir Juice", IngredientType.FRUIT, "Wine Grape", "Light, elegant red grape", "wine", null, null, null, null, 1.5, "gallons"),
        Ingredient(74, "Riesling Juice", IngredientType.FRUIT, "Wine Grape", "Aromatic white wine grape", "wine", null, null, null, null, 1.0, "gallons"),
        Ingredient(75, "Merlot Juice", IngredientType.FRUIT, "Wine Grape", "Smooth red wine grape", "wine", null, null, null, null, 1.5, "gallons"),
        
        // === ACIDS & WINE ADDITIVES ===
        Ingredient(76, "Tartaric Acid", IngredientType.ACID, "Wine Acid", "Primary wine acid for pH adjustment", "wine,mead,cider", null, null, null, null, 0.5, "oz"),
        Ingredient(77, "Malic Acid", IngredientType.ACID, "Wine Acid", "Apple-like acid for wine", "wine,cider", null, null, null, null, 0.3, "oz"),
        Ingredient(78, "Citric Acid", IngredientType.ACID, "Wine Acid", "Citrus acid for brightness", "wine,mead,cider", null, null, null, null, 0.5, "oz"),
        Ingredient(79, "Acid Blend", IngredientType.ACID, "Wine Acid", "Blend of tartaric, malic, and citric", "wine,mead,cider", null, null, null, null, 1.0, "oz"),
        
        // === YEAST NUTRIENTS (EXPANDED) ===
        Ingredient(80, "Diammonium Phosphate (DAP)", IngredientType.YEAST_NUTRIENT, "Nutrient", "Primary nitrogen source for yeast", "mead,wine,cider", null, null, null, null, 2.0, "oz"),
        Ingredient(81, "Fermaid-O", IngredientType.YEAST_NUTRIENT, "Nutrient", "Organic yeast nutrient", "mead,wine", null, null, null, null, 1.0, "oz"),
        Ingredient(82, "Fermaid-K", IngredientType.YEAST_NUTRIENT, "Nutrient", "Complete yeast nutrient blend", "mead,wine", null, null, null, null, 1.0, "oz"),
        Ingredient(83, "Go-Ferm", IngredientType.YEAST_NUTRIENT, "Nutrient", "Yeast rehydration nutrient", "mead,wine", null, null, null, null, 0.5, "oz"),
        Ingredient(84, "Yeast Energizer", IngredientType.YEAST_NUTRIENT, "Nutrient", "General yeast energizer", "mead,wine,cider", null, null, null, null, 1.0, "oz"),
        
        // === WATER TREATMENT ===
        Ingredient(85, "Gypsum (CaSO4)", IngredientType.WATER_TREATMENT, "Salt", "Adds calcium and sulfate", "beer", null, null, null, null, 1.0, "oz"),
        Ingredient(86, "Calcium Chloride", IngredientType.WATER_TREATMENT, "Salt", "Adds calcium and chloride", "beer", null, null, null, null, 1.0, "oz"),
        Ingredient(87, "Epsom Salt (MgSO4)", IngredientType.WATER_TREATMENT, "Salt", "Adds magnesium and sulfate", "beer", null, null, null, null, 0.5, "oz"),
        Ingredient(88, "Table Salt (NaCl)", IngredientType.WATER_TREATMENT, "Salt", "Adds sodium and chloride", "beer", null, null, null, null, 0.3, "oz"),
        Ingredient(89, "Baking Soda", IngredientType.WATER_TREATMENT, "Alkaline", "Raises pH and adds sodium", "beer", null, null, null, null, 0.5, "oz"),
        
        // === CLARIFIERS & STABILIZERS ===
        Ingredient(90, "Bentonite", IngredientType.CLARIFIER, "Clay", "Clay-based protein clarifier", "wine,mead,cider", null, null, null, null, 2.0, "oz"),
        Ingredient(91, "Sparkolloid", IngredientType.CLARIFIER, "Blend", "Hot-mix clarifier", "wine,mead", null, null, null, null, 1.0, "oz"),
        Ingredient(92, "Super-Kleer", IngredientType.CLARIFIER, "Two-part", "Two-part clarifying system", "wine,mead,cider", null, null, null, null, 5.0, "packets"),
        Ingredient(93, "Potassium Sorbate", IngredientType.CLARIFIER, "Stabilizer", "Prevents refermentation", "wine,mead,cider", null, null, null, null, 1.0, "oz"),
        Ingredient(94, "Potassium Metabisulfite", IngredientType.CLARIFIER, "Stabilizer", "Antioxidant and preservative", "wine,mead,cider", null, null, null, null, 2.0, "oz"),
        
        // === ADDITIONAL FRUITS & EXOTICS ===
        Ingredient(95, "Cranberries", IngredientType.FRUIT, "Berry", "Tart red berries", "mead,wine,cider", null, null, null, null, 1.0, "lbs"),
        Ingredient(96, "Pomegranate Juice", IngredientType.FRUIT, "Juice", "Antioxidant-rich fruit juice", "mead,wine", null, null, null, null, 1.0, "quarts"),
        Ingredient(97, "Mango", IngredientType.FRUIT, "Tropical", "Tropical fruit character", "mead,beer", null, null, null, null, 2.0, "lbs"),
        Ingredient(98, "Pineapple", IngredientType.FRUIT, "Tropical", "Sweet tropical fruit", "mead,beer,cider", null, null, null, null, 2.0, "lbs"),
        Ingredient(99, "Coconut Flakes", IngredientType.FRUIT, "Tropical", "Toasted coconut character", "mead,beer", null, null, null, null, 0.5, "lbs"),
        Ingredient(100, "Banana", IngredientType.FRUIT, "Tropical", "Tropical fruit character", "mead,beer", null, null, null, null, 2.0, "lbs"),
        
        // Continue adding more ingredients to reach 150+...
        // === ADDITIONAL SPICES & HERBS ===
        Ingredient(101, "Juniper Berries", IngredientType.SPICE, "Gin", "Gin-like botanical", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(102, "Angelica Root", IngredientType.SPICE, "Botanical", "Traditional European herb", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(103, "Orris Root", IngredientType.SPICE, "Botanical", "Violet-scented root", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(104, "Hibiscus Flowers", IngredientType.SPICE, "Floral", "Tart, cranberry-like flowers", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(105, "Lemongrass", IngredientType.SPICE, "Citrus", "Citrusy grass", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(106, "Bay Leaves", IngredientType.SPICE, "Herb", "Aromatic leaves", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(107, "Fennel Seeds", IngredientType.SPICE, "Licorice", "Licorice-like seeds", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(108, "Cumin Seeds", IngredientType.SPICE, "Earthy", "Earthy, warm spice", "mead,beer", null, null, null, null, 0.2, "oz"),
        
        // === NUTS & SPECIALTY INGREDIENTS ===
        Ingredient(109, "Almonds (Raw)", IngredientType.OTHER, "Nut", "Classic metheglin addition", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(110, "Hazelnuts", IngredientType.OTHER, "Nut", "Buttery nut character", "mead,beer", null, null, null, null, 0.5, "lbs"),
        Ingredient(111, "Walnuts", IngredientType.OTHER, "Nut", "Rich, earthy nut flavor", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(112, "Pecans", IngredientType.OTHER, "Nut", "Southern comfort nut", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(113, "Coffee Beans", IngredientType.OTHER, "Coffee", "Whole coffee beans", "mead,beer", null, null, null, null, 1.0, "lbs"),
        Ingredient(114, "Cocoa Nibs", IngredientType.OTHER, "Chocolate", "Roasted cocoa beans", "mead,beer", null, null, null, null, 0.5, "lbs"),
        Ingredient(115, "Dark Chocolate", IngredientType.OTHER, "Chocolate", "High-quality dark chocolate", "mead,beer", null, null, null, null, 0.3, "lbs"),
        
        // === TEA & BOTANICALS ===
        Ingredient(116, "Earl Grey Tea", IngredientType.OTHER, "Tea", "Bergamot-scented black tea", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(117, "Green Tea", IngredientType.OTHER, "Tea", "Light, grassy tea", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(118, "Oolong Tea", IngredientType.OTHER, "Tea", "Partially fermented tea", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(119, "Rooibos Tea", IngredientType.OTHER, "Tea", "Caffeine-free red tea", "mead", null, null, null, null, 0.5, "oz"),
        
        // === ADDITIONAL ADJUNCTS ===
        Ingredient(120, "Oats (Flaked)", IngredientType.ADJUNCT, "Cereal", "Adds body and mouthfeel", "beer", null, null, 33.0, null, 2.0, "lbs"),
        Ingredient(121, "Rice (Flaked)", IngredientType.ADJUNCT, "Cereal", "Lightens body, adds fermentables", "beer", null, null, 42.0, null, 1.0, "lbs"),
        Ingredient(122, "Corn (Flaked)", IngredientType.ADJUNCT, "Cereal", "Classic American adjunct", "beer", null, null, 42.0, null, 1.0, "lbs"),
        Ingredient(123, "Wheat (Flaked)", IngredientType.ADJUNCT, "Cereal", "Adds protein and body", "beer", null, null, 36.0, null, 1.0, "lbs"),
        
        // === ADDITIONAL HOPS ===
        Ingredient(124, "Fuggle", IngredientType.HOP, "English", "Traditional English hop", "beer", null, 4.5, null, null, 1.0, "oz"),
        Ingredient(125, "East Kent Goldings", IngredientType.HOP, "English", "Classic English aroma hop", "beer", null, 5.0, null, null, 0.8, "oz"),
        Ingredient(126, "Tettnang", IngredientType.HOP, "Noble", "German noble hop", "beer", null, 4.5, null, null, 1.0, "oz"),
        Ingredient(127, "Sterling", IngredientType.HOP, "Noble", "American Saaz-like hop", "beer", null, 6.0, null, null, 0.8, "oz"),
        Ingredient(128, "Willamette", IngredientType.HOP, "American", "American Fuggle-type", "beer", null, 5.5, null, null, 1.0, "oz"),
        
        // === WINE & MEAD SPECIALTY INGREDIENTS ===
        Ingredient(129, "Oak Chips (American)", IngredientType.OTHER, "Oak", "American oak character", "wine,mead", null, null, null, null, 2.0, "oz"),
        Ingredient(130, "Oak Chips (French)", IngredientType.OTHER, "Oak", "French oak elegance", "wine,mead", null, null, null, null, 2.0, "oz"),
        Ingredient(131, "Oak Spirals", IngredientType.OTHER, "Oak", "More surface area than chips", "wine,mead", null, null, null, null, 1.0, "each"),
        Ingredient(132, "Grape Tannin", IngredientType.OTHER, "Tannin", "Adds structure and mouthfeel", "wine,mead", null, null, null, null, 0.5, "oz"),
        Ingredient(133, "Pectic Enzyme", IngredientType.OTHER, "Enzyme", "Breaks down fruit pectin", "wine,mead,cider", null, null, null, null, 1.0, "tsp"),
        
        // === FINAL SPECIALTY INGREDIENTS ===
        Ingredient(134, "Elderflowers", IngredientType.SPICE, "Floral", "Classic European floral addition", "mead,wine", null, null, null, null, 0.3, "oz"),
        Ingredient(135, "Meadowsweet", IngredientType.SPICE, "Floral", "Honey-almond flavor", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(136, "Lemon Verbena", IngredientType.SPICE, "Citrus", "Intense lemon without acid", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(137, "Bee Pollen", IngredientType.OTHER, "Bee Product", "Adds complexity to mead", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(138, "Propolis", IngredientType.OTHER, "Bee Product", "Bee glue, adds complexity", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(139, "Royal Jelly", IngredientType.OTHER, "Bee Product", "Premium bee product", "mead", null, null, null, null, 0.1, "oz"),
        
        // === FINAL FRUITS & BERRIES ===
        Ingredient(140, "Blackcurrants", IngredientType.FRUIT, "Berry", "Intense berry character", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(141, "Gooseberries", IngredientType.FRUIT, "Berry", "Tart, wine-like character", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(142, "Boysenberries", IngredientType.FRUIT, "Berry", "Blackberry-raspberry hybrid", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(143, "Passion Fruit", IngredientType.FRUIT, "Tropical", "Intense tropical character", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(144, "Kiwi", IngredientType.FRUIT, "Tropical", "Tart tropical fruit", "mead,wine", null, null, null, null, 2.0, "lbs"),
        Ingredient(145, "Dragon Fruit", IngredientType.FRUIT, "Tropical", "Mild, exotic fruit", "mead", null, null, null, null, 1.5, "lbs"),
        Ingredient(146, "Lychee", IngredientType.FRUIT, "Tropical", "Floral tropical fruit", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(147, "Star Fruit", IngredientType.FRUIT, "Tropical", "Mild, citrusy tropical", "mead", null, null, null, null, 1.5, "lbs"),
        Ingredient(148, "Persimmon", IngredientType.FRUIT, "Fall Fruit", "Sweet fall fruit", "mead,wine", null, null, null, null, 1.5, "lbs"),
        Ingredient(149, "Quince", IngredientType.FRUIT, "Fall Fruit", "Aromatic fall fruit", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(150, "Fig", IngredientType.FRUIT, "Mediterranean", "Sweet, complex fruit", "mead,wine", null, null, null, null, 1.5, "lbs")
    )
    
    // Insert all ingredients with enhanced error handling
    try {
        println("BrewingDatabase: Attempting to insert ${ingredients.size} ingredients...")
        ingredientDao.insertIngredients(ingredients)
        println("BrewingDatabase: ✅ Successfully inserted ${ingredients.size} ingredients!")
        
        // Verify insertion
        val finalCount = ingredientDao.getIngredientCount()
        println("BrewingDatabase: Final ingredient count: $finalCount")
    } catch (e: Exception) {
        println("BrewingDatabase: ❌ Error inserting ingredients in batch: ${e.message}")
        e.printStackTrace()
        
        // Try inserting one by one if batch fails
        println("BrewingDatabase: Attempting individual ingredient insertion...")
        var successCount = 0
        ingredients.forEach { ingredient ->
            try {
                ingredientDao.insertIngredient(ingredient)
                successCount++
            } catch (ex: Exception) {
                println("BrewingDatabase: Failed to insert ingredient: ${ingredient.name} - ${ex.message}")
            }
        }
        println("BrewingDatabase: Successfully inserted $successCount out of ${ingredients.size} ingredients individually")
    }
    
    // Enhanced yeast database
    val yeasts = listOf(
        Yeast(1, "SafAle US-05", "Fermentis", YeastType.ALE, "beer", 59, 75, 12.0, 78, 82, FlocculationType.MEDIUM, description = "American ale yeast strain"),
        Yeast(2, "Lallemand DistilaMax MW", "Lallemand", YeastType.MEAD, "mead,wine", 68, 86, 18.0, description = "Specialized mead and wine yeast"),
        Yeast(3, "Red Star Premier Cuvée", "Red Star", YeastType.WINE, "wine,mead", 59, 86, 18.0, description = "Premium wine and mead yeast"),
        Yeast(4, "Wyeast 1084 Irish Ale", "Wyeast", YeastType.ALE, "beer", 62, 72, 12.0, 71, 75, FlocculationType.MEDIUM, description = "Clean Irish ale yeast"),
        Yeast(5, "White Labs WLP720 Sweet Mead", "White Labs", YeastType.MEAD, "mead", 70, 75, 15.0, description = "Sweet mead specialist yeast")
    )
    
    try {
        yeastDao.insertYeasts(yeasts)
        println("BrewingDatabase: ✅ Successfully inserted ${yeasts.size} yeasts")
    } catch (e: Exception) {
        println("BrewingDatabase: ❌ Error inserting yeasts: ${e.message}")
        e.printStackTrace()
    }
    
    println("BrewingDatabase: Database population complete!")
}
