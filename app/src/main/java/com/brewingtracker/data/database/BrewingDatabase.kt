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