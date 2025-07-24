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
    version = 10,  // INCREMENTED to force database recreation with comprehensive ingredients
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
        
        // FIXED: Always populate on open to ensure comprehensive ingredients exist
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val ingredientDao = database.ingredientDao()
                    val count = ingredientDao.getIngredientCount()
                    // FIXED: Changed to 200+ for comprehensive mead/wine ingredients
                    if (count < 200) {
                        println("BrewingDatabase: Found only $count ingredients, repopulating with 200+ comprehensive ingredients...")
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

// COMPREHENSIVE MEAD & WINE INGREDIENT DATABASE - GUARANTEED 200+ ingredients
private suspend fun populateDatabase(database: BrewingDatabase) {
    val ingredientDao = database.ingredientDao()
    val yeastDao = database.yeastDao()
    
    println("BrewingDatabase: Starting comprehensive ingredient population...")
    
    // COMPREHENSIVE MEAD & WINE INGREDIENT DATABASE - 200+ Brewing Ingredients
    val ingredients = listOf(
        // === PREMIUM HONEY VARIETIES (EXPANDED) ===
        Ingredient(1, "Wildflower Honey", IngredientType.FRUIT, "Honey", "Pure wildflower honey for mead making", "mead,beer", null, null, 35.0, null, 12.0, "lbs"),
        Ingredient(2, "Orange Blossom Honey", IngredientType.FRUIT, "Honey", "Delicate floral honey with citrus notes", "mead", null, null, 35.0, null, 6.0, "lbs"),
        Ingredient(3, "Clover Honey", IngredientType.FRUIT, "Honey", "Light, delicate, classic honey flavor", "mead", null, null, 35.0, null, 8.0, "lbs"),
        Ingredient(4, "Buckwheat Honey", IngredientType.FRUIT, "Honey", "Dark, robust, earthy honey", "mead", null, null, 35.0, null, 3.0, "lbs"),
        Ingredient(5, "Tupelo Honey", IngredientType.FRUIT, "Honey", "Premium southern honey, won't crystallize", "mead", null, null, 35.0, null, 2.0, "lbs"),
        Ingredient(6, "Manuka Honey", IngredientType.FRUIT, "Honey", "New Zealand medicinal honey", "mead", null, null, 35.0, null, 1.0, "lbs"),
        Ingredient(7, "Sage Honey", IngredientType.FRUIT, "Honey", "Herbal, light colored honey", "mead", null, null, 35.0, null, 2.0, "lbs"),
        Ingredient(8, "Acacia Honey", IngredientType.FRUIT, "Honey", "Very light, delicate floral honey", "mead", null, null, 35.0, null, 3.0, "lbs"),
        Ingredient(9, "Basswood Honey", IngredientType.FRUIT, "Honey", "Mild, fresh, clean flavor", "mead", null, null, 35.0, null, 2.0, "lbs"),
        Ingredient(10, "Linden Honey", IngredientType.FRUIT, "Honey", "European linden honey, minty notes", "mead", null, null, 35.0, null, 1.5, "lbs"),
        Ingredient(11, "Chestnut Honey", IngredientType.FRUIT, "Honey", "Dark, bitter, tannic honey", "mead", null, null, 35.0, null, 1.0, "lbs"),
        Ingredient(12, "Blackberry Honey", IngredientType.FRUIT, "Honey", "Berry-forward honey with fruit notes", "mead", null, null, 35.0, null, 2.0, "lbs"),
        Ingredient(13, "Lavender Honey", IngredientType.FRUIT, "Honey", "Floral, aromatic honey from lavender fields", "mead", null, null, 35.0, null, 1.5, "lbs"),
        Ingredient(14, "Fireweed Honey", IngredientType.FRUIT, "Honey", "Alaskan specialty, smooth texture", "mead", null, null, 35.0, null, 1.0, "lbs"),
        Ingredient(15, "Heather Honey", IngredientType.FRUIT, "Honey", "Scottish heather honey, jelly-like texture", "mead", null, null, 35.0, null, 0.5, "lbs"),
        
        // === COMPREHENSIVE YEAST NUTRIENTS ===
        Ingredient(16, "Fermaid-O", IngredientType.YEAST_NUTRIENT, "Organic Nutrient", "Premium organic yeast nutrient, OMRI certified", "mead,wine", null, null, null, null, 1.0, "oz"),
        Ingredient(17, "Fermaid-K", IngredientType.YEAST_NUTRIENT, "Complete Nutrient", "Complete yeast nutrient blend with amino acids", "mead,wine", null, null, null, null, 1.0, "oz"),
        Ingredient(18, "Go-Ferm", IngredientType.YEAST_NUTRIENT, "Rehydration Nutrient", "Yeast rehydration nutrient for strong starts", "mead,wine", null, null, null, null, 0.5, "oz"),
        Ingredient(19, "DAP (Diammonium Phosphate)", IngredientType.YEAST_NUTRIENT, "Nitrogen Source", "Primary nitrogen source for yeast health", "mead,wine,cider", null, null, null, null, 2.0, "oz"),
        Ingredient(20, "Yeast Hulls", IngredientType.YEAST_NUTRIENT, "Autolyzed Yeast", "Dead yeast cells, natural nutrient source", "mead,wine", null, null, null, null, 1.0, "oz"),
        Ingredient(21, "Yeast Energizer", IngredientType.YEAST_NUTRIENT, "General Energizer", "Complete yeast energizer with vitamins", "mead,wine,cider", null, null, null, null, 1.0, "oz"),
        Ingredient(22, "Booster Blanc", IngredientType.YEAST_NUTRIENT, "Complex Nutrient", "Complex nutrient for white wines and meads", "mead,wine", null, null, null, null, 0.5, "oz"),
        Ingredient(23, "Booster Rouge", IngredientType.YEAST_NUTRIENT, "Complex Nutrient", "Complex nutrient for red wines", "wine", null, null, null, null, 0.5, "oz"),
        Ingredient(24, "Opti-White", IngredientType.YEAST_NUTRIENT, "White Wine Nutrient", "Specialized nutrient for white wines", "wine", null, null, null, null, 0.5, "oz"),
        Ingredient(25, "Opti-Red", IngredientType.YEAST_NUTRIENT, "Red Wine Nutrient", "Specialized nutrient for red wines", "wine", null, null, null, null, 0.5, "oz"),
        
        // === FRUITS FOR MELOMEL (COMPREHENSIVE) ===
        Ingredient(26, "Strawberries", IngredientType.FRUIT, "Berry", "Fresh strawberries for fruit wines and meads", "mead,wine,beer", null, null, null, null, 2.0, "lbs"),
        Ingredient(27, "Blackberries", IngredientType.FRUIT, "Berry", "Rich, complex berry character for melomel", "mead,wine,beer", null, null, null, null, 1.5, "lbs"),
        Ingredient(28, "Blueberries", IngredientType.FRUIT, "Berry", "Sweet-tart berries, purple color", "mead,wine,beer", null, null, null, null, 3.0, "lbs"),
        Ingredient(29, "Raspberries", IngredientType.FRUIT, "Berry", "Intense berry flavor and aroma", "mead,wine,beer", null, null, null, null, 1.0, "lbs"),
        Ingredient(30, "Sweet Cherries", IngredientType.FRUIT, "Stone Fruit", "Rich, sweet fruit character for classic melomel", "mead,wine,beer", null, null, null, null, 2.0, "lbs"),
        Ingredient(31, "Tart Cherries", IngredientType.FRUIT, "Stone Fruit", "Sour cherry complexity", "mead,wine,beer", null, null, null, null, 2.0, "lbs"),
        Ingredient(32, "Elderberries", IngredientType.FRUIT, "Berry", "Dark, wine-like berry character", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(33, "Currants (Red)", IngredientType.FRUIT, "Berry", "Tart, wine-like small berries", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(34, "Currants (Black)", IngredientType.FRUIT, "Berry", "Intense, dark berry character", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(35, "Gooseberries", IngredientType.FRUIT, "Berry", "Tart, wine-like character", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(36, "Huckleberries", IngredientType.FRUIT, "Berry", "Intense, wild flavor", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(37, "Aronia Berries", IngredientType.FRUIT, "Berry", "Chokeberries, extremely high antioxidants", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(38, "Sea Buckthorn", IngredientType.FRUIT, "Berry", "Vitamin C powerhouse, tangy", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(39, "Lingonberries", IngredientType.FRUIT, "Berry", "Scandinavian superfruit", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(40, "Cloudberries", IngredientType.FRUIT, "Berry", "Rare Nordic delicacy", "mead", null, null, null, null, 0.5, "lbs"),
        
        // === STONE FRUITS & TREE FRUITS ===
        Ingredient(41, "Peaches", IngredientType.FRUIT, "Stone Fruit", "Delicate stone fruit character", "mead,wine", null, null, null, null, 1.5, "lbs"),
        Ingredient(42, "Apricots", IngredientType.FRUIT, "Stone Fruit", "Sweet, floral stone fruit", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(43, "Plums", IngredientType.FRUIT, "Stone Fruit", "Rich, complex stone fruit", "mead,wine", null, null, null, null, 1.5, "lbs"),
        Ingredient(44, "Nectarines", IngredientType.FRUIT, "Stone Fruit", "Smooth-skinned peach variety", "mead,wine", null, null, null, null, 1.5, "lbs"),
        Ingredient(45, "Pears", IngredientType.FRUIT, "Tree Fruit", "Delicate, sweet tree fruit", "mead,wine,cider", null, null, null, null, 2.0, "lbs"),
        Ingredient(46, "Apples", IngredientType.FRUIT, "Tree Fruit", "Classic fermentation fruit", "mead,cider", null, null, null, null, 3.0, "lbs"),
        Ingredient(47, "Quince", IngredientType.FRUIT, "Tree Fruit", "Aromatic fall fruit", "mead,wine", null, null, null, null, 1.0, "lbs"),
        Ingredient(48, "Persimmons", IngredientType.FRUIT, "Tree Fruit", "Sweet fall fruit", "mead,wine", null, null, null, null, 1.5, "lbs"),
        Ingredient(49, "Figs", IngredientType.FRUIT, "Mediterranean", "Sweet, complex fruit", "mead,wine", null, null, null, null, 1.5, "lbs"),
        Ingredient(50, "Pomegranate", IngredientType.FRUIT, "Ancient Fruit", "Antioxidant-rich, complex", "mead,wine", null, null, null, null, 1.0, "lbs"),
        
        // === TROPICAL & EXOTIC FRUITS ===
        Ingredient(51, "Mango", IngredientType.FRUIT, "Tropical", "Tropical fruit character", "mead,beer", null, null, null, null, 2.0, "lbs"),
        Ingredient(52, "Pineapple", IngredientType.FRUIT, "Tropical", "Sweet tropical fruit", "mead,beer,cider", null, null, null, null, 2.0, "lbs"),
        Ingredient(53, "Passion Fruit", IngredientType.FRUIT, "Tropical", "Intense tropical character", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(54, "Dragon Fruit", IngredientType.FRUIT, "Tropical", "Mild, exotic fruit", "mead", null, null, null, null, 1.5, "lbs"),
        Ingredient(55, "Lychee", IngredientType.FRUIT, "Tropical", "Floral tropical fruit", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(56, "Rambutan", IngredientType.FRUIT, "Tropical", "Lychee-like, floral", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(57, "Longan", IngredientType.FRUIT, "Tropical", "Sweet, subtle tropical fruit", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(58, "Star Fruit (Carambola)", IngredientType.FRUIT, "Tropical", "Mild, citrusy", "mead", null, null, null, null, 1.5, "lbs"),
        Ingredient(59, "Kiwi", IngredientType.FRUIT, "Tropical", "Tart tropical fruit", "mead,wine", null, null, null, null, 2.0, "lbs"),
        Ingredient(60, "Feijoa", IngredientType.FRUIT, "Tropical", "Pineapple-guava hybrid", "mead", null, null, null, null, 1.5, "lbs"),
        Ingredient(61, "Jackfruit", IngredientType.FRUIT, "Tropical", "Unique tropical sweetness", "mead", null, null, null, null, 2.0, "lbs"),
        Ingredient(62, "Durian", IngredientType.FRUIT, "Tropical", "Adventurous, acquired taste", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(63, "Mangosteen", IngredientType.FRUIT, "Tropical", "Queen of fruits, complex flavor", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(64, "Acai Berries", IngredientType.FRUIT, "Superfruit", "Superfruit, earthy flavor", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(65, "Goji Berries", IngredientType.FRUIT, "Superfruit", "Chinese superfruit, sweet-tart", "mead", null, null, null, null, 0.5, "lbs"),
        
        // === COMPREHENSIVE SPICES & BOTANICALS ===
        Ingredient(66, "Ceylon Cinnamon", IngredientType.SPICE, "Warming", "True cinnamon, delicate and sweet", "mead,beer,wine", null, null, null, null, 0.5, "oz"),
        Ingredient(67, "Madagascar Vanilla Beans", IngredientType.SPICE, "Sweet", "Premium vanilla, split and scrape", "mead,beer", null, null, null, null, 2.0, "each"),
        Ingredient(68, "Cardamom Pods", IngredientType.SPICE, "Warming", "Aromatic, slightly citrusy spice", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(69, "Star Anise", IngredientType.SPICE, "Warming", "Licorice-like spice for winter meads", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(70, "Cloves", IngredientType.SPICE, "Warming", "Intense warming spice, use sparingly", "mead,beer", null, null, null, null, 0.1, "oz"),
        Ingredient(71, "Allspice Berries", IngredientType.SPICE, "Warming", "Complex warming spice", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(72, "Nutmeg", IngredientType.SPICE, "Warming", "Sweet, warm spice for holiday meads", "mead,beer", null, null, null, null, 0.1, "oz"),
        Ingredient(73, "Ginger Root", IngredientType.SPICE, "Warming", "Fresh ginger for heat and complexity", "mead,beer", null, null, null, null, 1.0, "oz"),
        Ingredient(74, "Black Peppercorns", IngredientType.SPICE, "Heat", "Adds heat and complexity", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(75, "Long Pepper", IngredientType.SPICE, "Heat", "Ancient spice, different from black pepper", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(76, "Grains of Paradise", IngredientType.SPICE, "Heat", "African pepper, complex heat", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(77, "Cubeb Pepper", IngredientType.SPICE, "Heat", "Floral, pine-like pepper", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(78, "Pink Peppercorns", IngredientType.SPICE, "Heat", "Fruity, mild heat", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(79, "Szechuan Peppercorns", IngredientType.SPICE, "Heat", "Numbing, citrusy", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(80, "Coriander Seeds", IngredientType.SPICE, "Citrus", "Citrusy, slightly spicy seeds", "mead,beer", null, null, null, null, 0.5, "oz"),
        
        // === HERBS & BOTANICAL ADDITIONS ===
        Ingredient(81, "Lavender Buds", IngredientType.SPICE, "Floral", "Delicate floral character", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(82, "Rose Petals", IngredientType.SPICE, "Floral", "Romantic floral addition", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(83, "Pink Rose Buds", IngredientType.SPICE, "Floral", "More intense than petals", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(84, "Chamomile Flowers", IngredientType.SPICE, "Floral", "Apple-like, soothing herb", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(85, "Elderflowers", IngredientType.SPICE, "Floral", "Classic European addition", "mead,wine", null, null, null, null, 0.3, "oz"),
        Ingredient(86, "Jasmine Flowers", IngredientType.SPICE, "Floral", "Intensely floral, perfumed", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(87, "Hibiscus Flowers", IngredientType.SPICE, "Floral", "Tart, cranberry-like flowers", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(88, "Violets", IngredientType.SPICE, "Floral", "Sweet, perfumed flavor", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(89, "Cornflowers", IngredientType.SPICE, "Floral", "Mild, cucumber-like", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(90, "Calendula Petals", IngredientType.SPICE, "Floral", "Saffron-like color and flavor", "mead", null, null, null, null, 0.3, "oz"),
        
        // === TRADITIONAL HERBS ===
        Ingredient(91, "Angelica Root", IngredientType.SPICE, "Botanical", "Traditional European herb", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(92, "Orris Root", IngredientType.SPICE, "Botanical", "Violet-scented, fixative properties", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(93, "Gentian Root", IngredientType.SPICE, "Botanical", "Bitter, complex medicinal herb", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(94, "Juniper Berries", IngredientType.SPICE, "Botanical", "Gin-like, piney flavor", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(95, "Lemon Verbena", IngredientType.SPICE, "Botanical", "Intense lemon without acid", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(96, "Bee Balm", IngredientType.SPICE, "Botanical", "Minty, citrusy wild herb", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(97, "Meadowsweet", IngredientType.SPICE, "Botanical", "Honey-almond flavor, natural aspirin", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(98, "Rosemary", IngredientType.SPICE, "Herb", "Piney, aromatic herb", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(99, "Thyme", IngredientType.SPICE, "Herb", "Earthy, aromatic herb", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(100, "Sage", IngredientType.SPICE, "Herb", "Earthy, slightly bitter herb", "mead,beer", null, null, null, null, 0.2, "oz"),
        
        // === SPECIALTY BOTANICALS ===
        Ingredient(101, "Sumac", IngredientType.SPICE, "Botanical", "Tart, lemony Middle Eastern spice", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(102, "Za'atar Blend", IngredientType.SPICE, "Botanical", "Middle Eastern herb blend", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(103, "Ras el Hanout", IngredientType.SPICE, "Botanical", "North African spice blend", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(104, "Chinese Five Spice", IngredientType.SPICE, "Botanical", "Complex Asian blend", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(105, "Baharat", IngredientType.SPICE, "Botanical", "Middle Eastern all-spice blend", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(106, "Lemongrass", IngredientType.SPICE, "Botanical", "Citrusy grass", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(107, "Bay Leaves", IngredientType.SPICE, "Botanical", "Aromatic leaves", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(108, "Fennel Seeds", IngredientType.SPICE, "Botanical", "Licorice-like seeds", "mead,beer", null, null, null, null, 0.3, "oz"),
        
        // === NUTS & SEEDS ===
        Ingredient(109, "Almonds (Raw)", IngredientType.OTHER, "Nut", "Classic metheglin addition", "mead", null, null, null, null, 1.0, "lbs"),
        Ingredient(110, "Hazelnuts", IngredientType.OTHER, "Nut", "Buttery nut character", "mead,beer", null, null, null, null, 0.5, "lbs"),
        Ingredient(111, "Walnuts", IngredientType.OTHER, "Nut", "Rich, earthy nut flavor", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(112, "Pecans", IngredientType.OTHER, "Nut", "Southern comfort nut", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(113, "Pistachios", IngredientType.OTHER, "Nut", "Unique nutty-sweet profile", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(114, "Pine Nuts", IngredientType.OTHER, "Nut", "Delicate, resinous notes", "mead", null, null, null, null, 0.3, "lbs"),
        Ingredient(115, "Macadamia Nuts", IngredientType.OTHER, "Nut", "Rich, tropical character", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(116, "Brazil Nuts", IngredientType.OTHER, "Nut", "Intense, earthy flavor", "mead", null, null, null, null, 0.3, "lbs"),
        Ingredient(117, "Chestnuts", IngredientType.OTHER, "Nut", "Sweet, starchy addition", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(118, "Pumpkin Seeds", IngredientType.OTHER, "Seed", "Earthy, autumn character", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(119, "Sunflower Seeds", IngredientType.OTHER, "Seed", "Mild nutty flavor", "mead", null, null, null, null, 0.5, "lbs"),
        Ingredient(120, "Sesame Seeds", IngredientType.OTHER, "Seed", "Toasted for depth", "mead", null, null, null, null, 0.3, "lbs"),
        
        // === CITRUS (EXPANDED) ===
        Ingredient(121, "Orange Zest", IngredientType.FRUIT, "Citrus", "Fresh orange peel for citrus character", "mead,beer", null, null, null, null, 0.5, "oz"),
        Ingredient(122, "Lemon Zest", IngredientType.FRUIT, "Citrus", "Bright lemon peel for acidity", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(123, "Lime Zest", IngredientType.FRUIT, "Citrus", "Zesty lime character", "mead,beer", null, null, null, null, 0.2, "oz"),
        Ingredient(124, "Grapefruit Zest", IngredientType.FRUIT, "Citrus", "Bitter citrus complexity", "mead,beer", null, null, null, null, 0.3, "oz"),
        Ingredient(125, "Yuzu Zest", IngredientType.FRUIT, "Citrus", "Japanese citrus, complex", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(126, "Bergamot Zest", IngredientType.FRUIT, "Citrus", "Earl Grey flavor", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(127, "Key Lime Zest", IngredientType.FRUIT, "Citrus", "Intense, tart lime", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(128, "Blood Orange Zest", IngredientType.FRUIT, "Citrus", "Berry-citrus notes", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(129, "Meyer Lemon Zest", IngredientType.FRUIT, "Citrus", "Sweet-tart lemon", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(130, "Buddha's Hand", IngredientType.FRUIT, "Citrus", "All zest, no juice", "mead", null, null, null, null, 0.2, "oz"),
        
        // === WINE ACIDS & ADDITIVES ===
        Ingredient(131, "Tartaric Acid", IngredientType.ACID, "Wine Acid", "Primary wine acid for pH adjustment", "wine,mead,cider", null, null, null, null, 0.5, "oz"),
        Ingredient(132, "Malic Acid", IngredientType.ACID, "Wine Acid", "Apple-like acid for wine", "wine,cider", null, null, null, null, 0.3, "oz"),
        Ingredient(133, "Citric Acid", IngredientType.ACID, "Wine Acid", "Citrus acid for brightness", "wine,mead,cider", null, null, null, null, 0.5, "oz"),
        Ingredient(134, "Acid Blend", IngredientType.ACID, "Wine Acid", "Blend of tartaric, malic, and citric", "wine,mead,cider", null, null, null, null, 1.0, "oz"),
        
        // === CLARIFIERS & STABILIZERS ===
        Ingredient(135, "Bentonite", IngredientType.CLARIFIER, "Clay", "Clay-based protein clarifier", "wine,mead,cider", null, null, null, null, 2.0, "oz"),
        Ingredient(136, "Sparkolloid", IngredientType.CLARIFIER, "Blend", "Hot-mix clarifier", "wine,mead", null, null, null, null, 1.0, "oz"),
        Ingredient(137, "Super-Kleer", IngredientType.CLARIFIER, "Two-part", "Two-part clarifying system", "wine,mead,cider", null, null, null, null, 5.0, "packets"),
        Ingredient(138, "Potassium Sorbate", IngredientType.CLARIFIER, "Stabilizer", "Prevents refermentation", "wine,mead,cider", null, null, null, null, 1.0, "oz"),
        Ingredient(139, "Potassium Metabisulfite", IngredientType.CLARIFIER, "Stabilizer", "Antioxidant and preservative", "wine,mead,cider", null, null, null, null, 2.0, "oz"),
        
        // === SPECIALTY ADDITIONS ===
        Ingredient(140, "Oak Chips (American)", IngredientType.OTHER, "Oak", "American oak character", "wine,mead", null, null, null, null, 2.0, "oz"),
        Ingredient(141, "Oak Chips (French)", IngredientType.OTHER, "Oak", "French oak elegance", "wine,mead", null, null, null, null, 2.0, "oz"),
        Ingredient(142, "Oak Chips (Hungarian)", IngredientType.OTHER, "Oak", "Hungarian oak complexity", "wine,mead", null, null, null, null, 2.0, "oz"),
        Ingredient(143, "Oak Spirals", IngredientType.OTHER, "Oak", "More surface area than chips", "wine,mead", null, null, null, null, 1.0, "each"),
        Ingredient(144, "Grape Tannin Powder", IngredientType.OTHER, "Tannin", "Adds structure and mouthfeel", "wine,mead", null, null, null, null, 0.5, "oz"),
        Ingredient(145, "Pectic Enzyme", IngredientType.OTHER, "Enzyme", "Breaks down fruit pectin", "wine,mead,cider", null, null, null, null, 1.0, "tsp"),
        
        // === SPECIALTY SUGARS ===
        Ingredient(146, "Coconut Sugar", IngredientType.ADJUNCT, "Sugar", "Tropical, caramel notes", "mead", null, null, 35.0, null, 2.0, "lbs"),
        Ingredient(147, "Date Sugar", IngredientType.ADJUNCT, "Sugar", "Concentrated date sweetness", "mead", null, null, 40.0, null, 1.5, "lbs"),
        Ingredient(148, "Maple Sugar", IngredientType.ADJUNCT, "Sugar", "Concentrated maple flavor", "mead,beer", null, null, 42.0, null, 1.0, "lbs"),
        Ingredient(149, "Muscovado Sugar", IngredientType.ADJUNCT, "Sugar", "Unrefined cane sugar", "mead,beer", null, null, 43.0, null, 2.0, "lbs"),
        Ingredient(150, "Jaggery", IngredientType.ADJUNCT, "Sugar", "Indian palm sugar", "mead", null, null, 40.0, null, 1.5, "lbs"),
        Ingredient(151, "Piloncillo", IngredientType.ADJUNCT, "Sugar", "Mexican brown sugar cones", "mead", null, null, 42.0, null, 1.0, "lbs"),
        Ingredient(152, "Rock Sugar", IngredientType.ADJUNCT, "Sugar", "Large crystal sugar", "mead", null, null, 46.0, null, 2.0, "lbs"),
        Ingredient(153, "Black Strap Molasses", IngredientType.ADJUNCT, "Sugar", "Intense, mineral-rich", "mead,beer", null, null, 36.0, null, 1.0, "lbs"),
        
        // === TEA & COFFEE ===
        Ingredient(154, "Earl Grey Tea", IngredientType.OTHER, "Tea", "Bergamot-scented black tea", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(155, "Green Tea", IngredientType.OTHER, "Tea", "Light, grassy tea", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(156, "Oolong Tea", IngredientType.OTHER, "Tea", "Complex, partially fermented", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(157, "White Tea", IngredientType.OTHER, "Tea", "Delicate, subtle flavor", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(158, "Rooibos Tea", IngredientType.OTHER, "Tea", "Caffeine-free, vanilla notes", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(159, "Yerba Mate", IngredientType.OTHER, "Tea", "South American energy tea", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(160, "Coffee Beans (Green)", IngredientType.OTHER, "Coffee", "Pre-roast coffee character", "mead,beer", null, null, null, null, 1.0, "lbs"),
        Ingredient(161, "Coffee Beans (Roasted)", IngredientType.OTHER, "Coffee", "Roasted coffee flavor", "mead,beer", null, null, null, null, 1.0, "lbs"),
        Ingredient(162, "Cold Brew Coffee", IngredientType.OTHER, "Coffee", "Smooth, less acidic", "mead,beer", null, null, null, null, 1.0, "cups"),
        Ingredient(163, "Espresso Beans", IngredientType.OTHER, "Coffee", "Intense coffee flavor", "mead,beer", null, null, null, null, 0.5, "lbs"),
        
        // === CHOCOLATE & COCOA ===
        Ingredient(164, "Cocoa Nibs", IngredientType.OTHER, "Chocolate", "Roasted cocoa beans", "mead,beer", null, null, null, null, 0.5, "lbs"),
        Ingredient(165, "Dark Chocolate", IngredientType.OTHER, "Chocolate", "High-quality dark chocolate", "mead,beer", null, null, null, null, 0.3, "lbs"),
        Ingredient(166, "Cocoa Powder", IngredientType.OTHER, "Chocolate", "Unsweetened cocoa powder", "mead,beer", null, null, null, null, 0.25, "lbs"),
        Ingredient(167, "Cacao Powder", IngredientType.OTHER, "Chocolate", "Raw cacao powder", "mead", null, null, null, null, 0.25, "lbs"),
        
        // === BEE PRODUCTS (EXPANDED) ===
        Ingredient(168, "Bee Pollen", IngredientType.OTHER, "Bee Product", "Adds complexity to mead", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(169, "Propolis", IngredientType.OTHER, "Bee Product", "Bee glue, adds complexity", "mead", null, null, null, null, 0.1, "oz"),
        Ingredient(170, "Royal Jelly", IngredientType.OTHER, "Bee Product", "Premium bee product", "mead", null, null, null, null, 0.1, "oz"),
        
        // === MUSHROOMS & ADAPTOGENS ===
        Ingredient(171, "Reishi Mushroom", IngredientType.OTHER, "Mushroom", "Earthy, medicinal properties", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(172, "Chaga Mushroom", IngredientType.OTHER, "Mushroom", "Birch-like, antioxidant rich", "mead", null, null, null, null, 0.5, "oz"),
        Ingredient(173, "Lion's Mane", IngredientType.OTHER, "Mushroom", "Seafood-like umami", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(174, "Cordyceps", IngredientType.OTHER, "Mushroom", "Energy-boosting properties", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(175, "Turkey Tail", IngredientType.OTHER, "Mushroom", "Immune-supporting", "mead", null, null, null, null, 0.3, "oz"),
        Ingredient(176, "Ashwagandha", IngredientType.OTHER, "Adaptogen", "Adaptogenic herb", "mead", null, null, null, null, 0.2, "oz"),
        Ingredient(177, "Rhodiola", IngredientType.OTHER, "Adaptogen", "Stress-reducing botanical", "mead", null, null, null, null, 0.2, "oz"),
        
        // === WINE GRAPES & JUICES ===
        Ingredient(178, "Cabernet Sauvignon Juice", IngredientType.FRUIT, "Wine Grape", "Full-bodied red wine grape", "wine", null, null, null, null, 2.0, "gallons"),
        Ingredient(179, "Chardonnay Juice", IngredientType.FRUIT, "Wine Grape", "Classic white wine grape", "wine", null, null, null, null, 2.0, "gallons"),
        Ingredient(180, "Pinot Noir Juice", IngredientType.FRUIT, "Wine Grape", "Light, elegant red grape", "wine", null, null, null, null, 1.5, "gallons"),
        Ingredient(181, "Riesling Juice", IngredientType.FRUIT, "Wine Grape", "Aromatic, floral white", "wine", null, null, null, null, 1.0, "gallons"),
        Ingredient(182, "Merlot Juice", IngredientType.FRUIT, "Wine Grape", "Smooth red wine grape", "wine", null, null, null, null, 1.5, "gallons"),
        Ingredient(183, "Pinot Grigio Juice", IngredientType.FRUIT, "Wine Grape", "Clean, crisp white", "wine", null, null, null, null, 1.5, "gallons"),
        Ingredient(184, "Sangiovese Juice", IngredientType.FRUIT, "Wine Grape", "Italian red wine grape", "wine", null, null, null, null, 1.5, "gallons"),
        Ingredient(185, "Tempranillo Juice", IngredientType.FRUIT, "Wine Grape", "Spanish red variety", "wine", null, null, null, null, 1.5, "gallons"),
        Ingredient(186, "Gewürztraminer Juice", IngredientType.FRUIT, "Wine Grape", "Spicy, aromatic white", "wine", null, null, null, null, 1.0, "gallons"),
        
        // === WINE CONCENTRATES ===
        Ingredient(187, "Red Wine Concentrate", IngredientType.FRUIT, "Wine Concentrate", "Instant wine character", "wine,mead", null, null, null, null, 1.0, "cans"),
        Ingredient(188, "White Wine Concentrate", IngredientType.FRUIT, "Wine Concentrate", "Clean wine base", "wine,mead", null, null, null, null, 1.0, "cans"),
        Ingredient(189, "Port Wine Concentrate", IngredientType.FRUIT, "Wine Concentrate", "Fortified wine character", "wine,mead", null, null, null, null, 0.5, "cans"),
        Ingredient(190, "Sherry Concentrate", IngredientType.FRUIT, "Wine Concentrate", "Nutty, oxidized notes", "wine,mead", null, null, null, null, 0.5, "cans"),
        
        // === FINAL SPECIALTIES ===
        Ingredient(191, "Corn Sugar (Dextrose)", IngredientType.ADJUNCT, "Sugar", "Pure dextrose for priming and fermentation", "beer,mead,wine,cider", null, null, 46.0, null, 5.0, "lbs"),
        Ingredient(192, "Lactose", IngredientType.ADJUNCT, "Sugar", "Unfermentable milk sugar for sweetness", "beer", null, null, 0.0, null, 1.0, "lbs"),
        Ingredient(193, "Maple Syrup", IngredientType.ADJUNCT, "Sugar", "Pure maple syrup for unique character", "beer,mead", null, null, 30.0, null, 1.0, "quarts"),
        Ingredient(194, "Apple Juice (Fresh)", IngredientType.FRUIT, "Juice", "Fresh-pressed apple juice for cider", "cider", null, null, null, null, 5.0, "gallons"),
        Ingredient(195, "Pear Juice", IngredientType.FRUIT, "Juice", "Fresh pear juice for perry", "cider", null, null, null, null, 1.0, "gallons"),
        Ingredient(196, "Pomegranate Juice", IngredientType.FRUIT, "Juice", "Antioxidant-rich fruit juice", "mead,wine", null, null, null, null, 1.0, "quarts"),
        
        // === WATER TREATMENT ===
        Ingredient(197, "Gypsum (CaSO4)", IngredientType.WATER_TREATMENT, "Salt", "Adds calcium and sulfate", "beer", null, null, null, null, 1.0, "oz"),
        Ingredient(198, "Calcium Chloride", IngredientType.WATER_TREATMENT, "Salt", "Adds calcium and chloride", "beer", null, null, null, null, 1.0, "oz"),
        Ingredient(199, "Epsom Salt (MgSO4)", IngredientType.WATER_TREATMENT, "Salt", "Adds magnesium and sulfate", "beer", null, null, null, null, 0.5, "oz"),
        Ingredient(200, "Baking Soda", IngredientType.WATER_TREATMENT, "Alkaline", "Raises pH and adds sodium", "beer", null, null, null, null, 0.5, "oz")
    )
    
    // Insert all ingredients with enhanced error handling
    try {
        println("BrewingDatabase: Attempting to insert ${ingredients.size} comprehensive ingredients...")
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
    
    // COMPREHENSIVE YEAST DATABASE - 40+ STRAINS
    val yeasts = listOf(
        // ALE YEASTS
        Yeast(1, "SafAle US-05", "Fermentis", YeastType.ALE, "beer", 59, 75, 12.0, 78, 82, FlocculationType.MEDIUM, description = "American ale yeast strain"),
        Yeast(2, "SafAle S-04", "Fermentis", YeastType.ALE, "beer", 59, 75, 12.0, 72, 78, FlocculationType.HIGH, description = "English ale yeast"),
        Yeast(3, "SafAle BE-256", "Fermentis", YeastType.ALE, "beer", 64, 78, 11.0, 78, 83, FlocculationType.MEDIUM, description = "Abbey/Belgian ale yeast"),
        Yeast(4, "SafAle K-97", "Fermentis", YeastType.ALE, "beer", 59, 75, 12.0, 75, 82, FlocculationType.HIGH, description = "German ale yeast"),
        
        // LAGER YEASTS  
        Yeast(5, "SafLager W-34/70", "Fermentis", YeastType.LAGER, "beer", 46, 59, 11.0, 80, 84, FlocculationType.HIGH, description = "German lager yeast"),
        Yeast(6, "SafLager S-23", "Fermentis", YeastType.LAGER, "beer", 48, 59, 11.0, 82, 86, FlocculationType.HIGH, description = "Bottom-fermenting lager yeast"),
        
        // WYEAST STRAINS
        Yeast(7, "Wyeast 1084 Irish Ale", "Wyeast", YeastType.ALE, "beer", 62, 72, 12.0, 71, 75, FlocculationType.MEDIUM, description = "Clean Irish ale yeast"),
        Yeast(8, "Wyeast 1056 American Ale", "Wyeast", YeastType.ALE, "beer", 60, 72, 11.0, 73, 77, FlocculationType.MEDIUM, description = "Classic American ale yeast"),
        Yeast(9, "Wyeast 3787 Trappist High Gravity", "Wyeast", YeastType.ALE, "beer", 64, 78, 13.0, 75, 85, FlocculationType.MEDIUM, description = "Belgian strong ale yeast"),
        Yeast(10, "Wyeast 2124 Bohemian Lager", "Wyeast", YeastType.LAGER, "beer", 48, 58, 10.0, 69, 73, FlocculationType.MEDIUM, description = "Czech lager yeast"),
        
        // WHITE LABS STRAINS
        Yeast(11, "White Labs WLP001 California Ale", "White Labs", YeastType.ALE, "beer", 68, 73, 11.0, 73, 80, FlocculationType.MEDIUM, description = "Clean American ale yeast"),
        Yeast(12, "White Labs WLP300 Hefeweizen Ale", "White Labs", YeastType.ALE, "beer", 68, 72, 10.0, 72, 76, FlocculationType.LOW, description = "German wheat beer yeast"),
        Yeast(13, "White Labs WLP500 Trappist Ale", "White Labs", YeastType.ALE, "beer", 65, 72, 12.0, 75, 80, FlocculationType.MEDIUM, description = "Belgian monastery ale yeast"),
        Yeast(14, "White Labs WLP830 German Lager", "White Labs", YeastType.LAGER, "beer", 50, 55, 11.0, 74, 79, FlocculationType.MEDIUM, description = "German lager yeast"),
        
        // MEAD SPECIALIST YEASTS
        Yeast(15, "Lallemand DistilaMax MW", "Lallemand", YeastType.MEAD, "mead,wine", 68, 86, 18.0, description = "Specialized mead and wine yeast"),
        Yeast(16, "White Labs WLP720 Sweet Mead", "White Labs", YeastType.MEAD, "mead", 70, 75, 15.0, description = "Sweet mead specialist yeast"),
        Yeast(17, "Wyeast 4767 White Labs Sweet Mead", "Wyeast", YeastType.MEAD, "mead", 65, 75, 15.0, description = "Different strain for sweet meads"),
        Yeast(18, "Red Star Premier Cuvée", "Red Star", YeastType.MEAD, "mead,wine", 59, 86, 18.0, description = "Premium mead and wine yeast"),
        Yeast(19, "Lallemand K1-V1116", "Lallemand", YeastType.MEAD, "mead,wine", 59, 95, 18.0, description = "Reliable, high alcohol tolerance, cold fermenter"),
        
        // WINE YEASTS (EXCELLENT FOR MEAD)
        Yeast(20, "Lallemand ICV-D254", "Lallemand", YeastType.WINE, "wine,mead", 68, 86, 16.0, description = "Enhances color extraction, great for fruit meads"),
        Yeast(21, "Red Star Montrachet", "Red Star", YeastType.WINE, "wine,mead", 59, 86, 13.0, description = "Classic all-purpose wine yeast"),
        Yeast(22, "Red Star Pasteur Red", "Red Star", YeastType.WINE, "wine,mead", 68, 86, 16.0, description = "For red wines and dark fruit meads"),
        Yeast(23, "Lallemand QA23", "Lallemand", YeastType.WINE, "wine,mead", 59, 68, 14.0, description = "White wine specialist, preserves delicate flavors"),
        Yeast(24, "Lallemand RC212", "Lallemand", YeastType.WINE, "wine,mead", 68, 86, 16.0, description = "Burgundy strain for complex fruit meads"),
        Yeast(25, "Red Star Pasteur Champagne", "Red Star", YeastType.WINE, "wine,mead", 59, 86, 18.0, description = "High alcohol, clean fermentation"),
        Yeast(26, "Lallemand ICV-GRE", "Lallemand", YeastType.WINE, "wine,mead", 68, 86, 15.0, description = "Enhances tropical fruit character"),
        Yeast(27, "Red Star Flor Sherry", "Red Star", YeastType.WINE, "wine,mead", 59, 86, 17.0, description = "Creates flor film, unique for traditional meads"),
        Yeast(28, "Lallemand Cross Evolution", "Lallemand", YeastType.WINE, "wine,mead", 68, 86, 16.0, description = "Hybrid vigor, fruit forward"),
        Yeast(29, "Lallemand Rhône 2323", "Lallemand", YeastType.WINE, "wine,mead", 68, 86, 15.0, description = "Spicy, complex character for metheglin"),
        
        // CHAMPAGNE/SPARKLING YEASTS
        Yeast(30, "White Labs WLP715 Champagne", "White Labs", YeastType.WINE, "wine,mead", 68, 83, 15.0, description = "High alcohol, clean profile"),
        Yeast(31, "Lallemand Prelude", "Lallemand", YeastType.WINE, "wine,mead", 59, 68, 9.0, description = "Low alcohol for session meads"),
        
        // WILD & SPECIALTY FERMENTATION
        Yeast(32, "White Labs Brettanomyces Bruxellensis", "White Labs", YeastType.WILD, "beer,mead", 68, 85, 12.0, description = "Farmhouse funk for experimental meads"),
        Yeast(33, "Wyeast Lambicus Blend", "Wyeast", YeastType.WILD, "beer,mead", 68, 78, 11.0, description = "Wild fermentation blend"),
        Yeast(34, "Lallemand Anchor Blend", "Lallemand", YeastType.WILD, "beer,mead", 68, 78, 12.0, description = "Complex wild fermentation culture"),
        
        // KVEIK STRAINS
        Yeast(35, "Lallemand Voss Kveik", "Lallemand", YeastType.KVEIK, "beer,mead", 68, 98, 12.0, description = "Norwegian farmhouse yeast, high temperature"),
        Yeast(36, "Omega Hothead Kveik", "Omega", YeastType.KVEIK, "beer,mead", 68, 98, 12.0, description = "Hot-fermenting Norwegian kveik"),
        Yeast(37, "Imperial Lutra Kveik", "Imperial", YeastType.KVEIK, "beer,mead", 68, 95, 12.0, description = "Clean kveik strain"),
        
        // CIDER YEASTS
        Yeast(38, "Lallemand Cider", "Lallemand", YeastType.CIDER, "cider", 59, 86, 18.0, description = "Specialized cider yeast"),
        Yeast(39, "Red Star Côte des Blancs", "Red Star", YeastType.CIDER, "cider,mead", 59, 86, 14.0, description = "Excellent for ciders and fruit meads"),
        Yeast(40, "Mangrove Jack M02 Cider", "Mangrove Jack", YeastType.CIDER, "cider", 64, 75, 12.0, description = "Premium cider yeast")
    )
    
    try {
        yeastDao.insertYeasts(yeasts)
        println("BrewingDatabase: ✅ Successfully inserted ${yeasts.size} comprehensive yeasts")
    } catch (e: Exception) {
        println("BrewingDatabase: ❌ Error inserting yeasts: ${e.message}")
        e.printStackTrace()
    }
    
    println("BrewingDatabase: Comprehensive database population complete!")
}