package com.brewingtracker.data.repository

import com.brewingtracker.data.database.dao.*
import com.brewingtracker.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrewingRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val ingredientDao: IngredientDao,
    private val yeastDao: YeastDao,
    private val projectIngredientDao: ProjectIngredientDao,
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val recipeStepDao: RecipeStepDao,                    // NEW: Recipe step management
    private val recipeCalculationDao: RecipeCalculationDao       // NEW: Recipe calculation caching
) {
    
    // === PROJECT OPERATIONS ===
    suspend fun createProject(project: Project): String {
        projectDao.insertProject(project)
        return project.id
    }
    
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    
    suspend fun deleteProject(projectId: String) = projectDao.deleteProject(projectId)
    
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()
    
    suspend fun getProjectById(projectId: String): Project? = projectDao.getProjectById(projectId)
    
    // === INGREDIENT OPERATIONS ===
    suspend fun addIngredient(ingredient: Ingredient) = ingredientDao.insertIngredient(ingredient)
    
    suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.updateIngredient(ingredient)
    
    suspend fun deleteIngredient(ingredientId: Int) = ingredientDao.deleteIngredient(ingredientId)
    
    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    
    suspend fun getIngredientById(ingredientId: Int): Ingredient? = ingredientDao.getIngredientById(ingredientId)
    
    fun getIngredientsByType(type: IngredientType): Flow<List<Ingredient>> = ingredientDao.getIngredientsByType(type)
    
    suspend fun searchIngredients(query: String, type: IngredientType? = null): List<Ingredient> {
        return if (type != null) {
            ingredientDao.searchIngredientsByTypeSync(query, type)
        } else {
            ingredientDao.searchIngredientsSync(query)
        }
    }
    
    suspend fun updateIngredientStock(ingredientId: Int, newStock: Double) = 
        ingredientDao.updateStock(ingredientId, newStock)
    
    // === YEAST OPERATIONS ===
    suspend fun addYeast(yeast: Yeast) = yeastDao.insertYeast(yeast)
    
    suspend fun updateYeast(yeast: Yeast) = yeastDao.updateYeast(yeast)
    
    suspend fun deleteYeast(yeastId: Int) = yeastDao.deleteYeast(yeastId)
    
    fun getAllYeasts(): Flow<List<Yeast>> = yeastDao.getAllYeasts()
    
    suspend fun getYeastById(yeastId: Int): Yeast? = yeastDao.getYeastById(yeastId)
    
    fun getYeastsByType(type: YeastType): Flow<List<Yeast>> = yeastDao.getYeastsByType(type)
    
    suspend fun searchYeasts(query: String): List<Yeast> = yeastDao.searchYeastsSync(query)
    
    // === PROJECT INGREDIENT OPERATIONS ===
    suspend fun addIngredientToProject(projectIngredient: ProjectIngredient) = 
        projectIngredientDao.insertProjectIngredient(projectIngredient)
    
    suspend fun updateProjectIngredient(projectIngredient: ProjectIngredient) = 
        projectIngredientDao.updateProjectIngredient(projectIngredient)
    
    suspend fun removeIngredientFromProject(projectIngredientId: Int) = 
        projectIngredientDao.deleteProjectIngredientById(projectIngredientId)
    
    fun getProjectIngredients(projectId: String): Flow<List<ProjectIngredientWithDetails>> = 
        projectIngredientDao.getProjectIngredientsWithDetails(projectId)
    
    suspend fun getProjectIngredientsSync(projectId: String): List<ProjectIngredientWithDetails> = 
        projectIngredientDao.getProjectIngredientsWithDetailsSync(projectId)
    
    // === RECIPE OPERATIONS ===
    suspend fun createRecipe(recipe: Recipe): String {
        recipeDao.insertRecipe(recipe)
        return recipe.id
    }
    
    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)
    
    suspend fun deleteRecipe(recipeId: String) = recipeDao.deleteRecipe(recipeId)
    
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
    
    suspend fun getRecipeById(recipeId: String): Recipe? = recipeDao.getRecipeById(recipeId)
    
    fun getRecipesByType(type: BeverageType): Flow<List<Recipe>> = recipeDao.getRecipesByType(type)
    
    suspend fun searchRecipes(query: String): List<Recipe> = recipeDao.searchRecipesSync(query)
    
    suspend fun getFavoriteRecipes(): List<Recipe> = recipeDao.getFavoriteRecipes()
    
    suspend fun updateRecipeUsage(recipeId: String) = recipeDao.incrementTimesUsed(recipeId)
    
    // === RECIPE INGREDIENT OPERATIONS ===
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long = 
        recipeIngredientDao.insertRecipeIngredient(recipeIngredient)
    
    suspend fun insertRecipeIngredients(recipeIngredients: List<RecipeIngredient>) = 
        recipeIngredientDao.insertRecipeIngredients(recipeIngredients)
    
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient) = 
        recipeIngredientDao.updateRecipeIngredient(recipeIngredient)
    
    suspend fun deleteRecipeIngredient(recipeIngredientId: Int) = 
        recipeIngredientDao.deleteRecipeIngredientById(recipeIngredientId)
    
    fun getRecipeIngredients(recipeId: String): Flow<List<RecipeIngredient>> = 
        recipeIngredientDao.getRecipeIngredients(recipeId)
    
    fun getRecipeIngredientsWithDetails(recipeId: String): Flow<List<RecipeIngredientWithDetails>> = 
        recipeIngredientDao.getRecipeIngredientsWithDetails(recipeId)
    
    suspend fun getRecipeIngredientsWithDetailsSync(recipeId: String): List<RecipeIngredientWithDetails> = 
        recipeIngredientDao.getRecipeIngredientsWithDetailsSync(recipeId)
    
    suspend fun scaleRecipeIngredients(recipeId: String, scaleFactor: Double): List<RecipeIngredient> = 
        recipeIngredientDao.getScaledRecipeIngredients(recipeId, scaleFactor)
    
    // === RECIPE STEP OPERATIONS (NEW) ===
    suspend fun insertRecipeStep(step: RecipeStep): Long = 
        recipeStepDao.insertStep(step)
    
    suspend fun insertRecipeSteps(steps: List<RecipeStep>) = 
        recipeStepDao.insertSteps(steps)
    
    suspend fun updateRecipeStep(step: RecipeStep) = 
        recipeStepDao.updateStep(step)
    
    suspend fun deleteRecipeStep(stepId: Int) = 
        recipeStepDao.deleteStepById(stepId)
    
    fun getRecipeSteps(recipeId: String): Flow<List<RecipeStep>> = 
        recipeStepDao.getStepsForRecipe(recipeId)
    
    suspend fun getRecipeStepsSync(recipeId: String): List<RecipeStep> = 
        recipeStepDao.getStepsForRecipeSync(recipeId)
    
    suspend fun getStepsByPhase(recipeId: String, phase: String): List<RecipeStep> = 
        recipeStepDao.getStepsByPhase(recipeId, phase)
    
    suspend fun reorderStepsAfterDeletion(recipeId: String, deletedStepNumber: Int) = 
        recipeStepDao.reorderStepsAfterDeletion(recipeId, deletedStepNumber)
    
    // === RECIPE CALCULATION OPERATIONS (NEW) ===
    suspend fun saveRecipeCalculation(calculation: RecipeCalculation): Long = 
        recipeCalculationDao.insertCalculation(calculation)
    
    suspend fun updateRecipeCalculation(calculation: RecipeCalculation) = 
        recipeCalculationDao.updateCalculation(calculation)
    
    suspend fun getRecipeCalculation(recipeId: String, batchSizeOz: Int): RecipeCalculation? = 
        recipeCalculationDao.getCalculationForBatchSize(recipeId, batchSizeOz)
    
    fun getRecipeCalculations(recipeId: String): Flow<List<RecipeCalculation>> = 
        recipeCalculationDao.getCalculationsForRecipe(recipeId)
    
    suspend fun deleteOldCalculations(cutoffDays: Int = 30) {
        val cutoffTimestamp = System.currentTimeMillis() - (cutoffDays * 24 * 60 * 60 * 1000L)
        recipeCalculationDao.cleanupOldCalculations(cutoffTimestamp)
    }
    
    suspend fun updateCalculationValues(
        recipeId: String, 
        batchSizeOz: Int, 
        og: Double?, 
        fg: Double?, 
        abv: Double?
    ) = recipeCalculationDao.updateCalculationValues(
        recipeId, batchSizeOz, og, fg, abv, System.currentTimeMillis()
    )
    
    suspend fun updateCalculationCost(recipeId: String, batchSizeOz: Int, cost: Double) = 
        recipeCalculationDao.updateCalculationCost(recipeId, batchSizeOz, cost, System.currentTimeMillis())
    
    // === RECIPE DUPLICATION & EXPORT ===
    suspend fun duplicateRecipe(sourceRecipeId: String, newName: String): String {
        val sourceRecipe = getRecipeById(sourceRecipeId)
            ?: throw IllegalArgumentException("Source recipe not found")
        
        val newRecipe = sourceRecipe.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = newName,
            timesUsed = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // Create new recipe
        createRecipe(newRecipe)
        
        // Copy ingredients
        val ingredients = getRecipeIngredientsWithDetailsSync(sourceRecipeId)
        val newIngredients = ingredients.map { ingredientWithDetails ->
            ingredientWithDetails.recipeIngredient.copy(
                id = 0, // Auto-generate new ID
                recipeId = newRecipe.id
            )
        }
        insertRecipeIngredients(newIngredients)
        
        // Copy steps
        val steps = getRecipeStepsSync(sourceRecipeId)
        val newSteps = steps.map { step ->
            step.copy(
                id = 0, // Auto-generate new ID
                recipeId = newRecipe.id
            )
        }
        insertRecipeSteps(newSteps)
        
        return newRecipe.id
    }
    
    suspend fun createProjectFromRecipe(
        recipeId: String, 
        batchSize: BatchSize, 
        projectName: String? = null
    ): String {
        val recipe = getRecipeById(recipeId)
            ?: throw IllegalArgumentException("Recipe not found")
        
        val project = Project(
            name = projectName ?: "${recipe.name} - ${batchSize.displayName}",
            description = "Brewing project from recipe: ${recipe.name}",
            beverageType = recipe.beverageType,
            targetOG = recipe.targetOG,
            targetFG = recipe.targetFG,
            targetABV = recipe.targetABV,
            batchSizeOz = batchSize.ozValue,
            recipeId = recipeId
        )
        
        // Create project
        createProject(project)
        
        // Scale and add ingredients
        val recipeIngredients = getRecipeIngredientsWithDetailsSync(recipeId)
        recipeIngredients.forEach { ingredientWithDetails ->
            val scaledQuantity = ingredientWithDetails.recipeIngredient.baseQuantity * batchSize.scaleFactor
            
            val projectIngredient = ProjectIngredient(
                projectId = project.id,
                ingredientId = ingredientWithDetails.ingredient.id,
                plannedQuantity = scaledQuantity,
                unit = ingredientWithDetails.recipeIngredient.baseUnit,
                additionTiming = ingredientWithDetails.recipeIngredient.additionTiming
            )
            
            addIngredientToProject(projectIngredient)
        }
        
        // Update recipe usage
        updateRecipeUsage(recipeId)
        
        return project.id
    }
    
    // === STATISTICS & ANALYTICS ===
    suspend fun getRecipeStats(): RecipeStats {
        val totalRecipes = recipeDao.getRecipeCount()
        val recipesByType = BeverageType.values().associateWith { type ->
            recipeDao.getRecipeCountByType(type)
        }
        val favoriteCount = recipeDao.getFavoriteRecipes().size
        val mostUsedRecipes = recipeDao.getMostUsedRecipes(5)
        
        return RecipeStats(
            totalRecipes = totalRecipes,
            recipesByType = recipesByType,
            favoriteCount = favoriteCount,
            mostUsedRecipes = mostUsedRecipes
        )
    }
    
    suspend fun getIngredientUsageStats(): Map<Ingredient, Int> {
        return recipeIngredientDao.getIngredientUsageStats()
    }
    
    // === BULK OPERATIONS ===
    suspend fun importRecipes(recipes: List<Recipe>): List<String> {
        return recipes.map { recipe ->
            createRecipe(recipe)
        }
    }
    
    suspend fun exportRecipeData(recipeId: String): RecipeExportData {
        val recipe = getRecipeById(recipeId)
            ?: throw IllegalArgumentException("Recipe not found")
        
        val ingredients = getRecipeIngredientsWithDetailsSync(recipeId)
        val steps = getRecipeStepsSync(recipeId)
        val calculations = recipeCalculationDao.getCalculationsForRecipeSync(recipeId)
        
        return RecipeExportData(
            recipe = recipe,
            ingredients = ingredients,
            steps = steps,
            calculations = calculations
        )
    }
}

// Data classes for statistics and export
data class RecipeStats(
    val totalRecipes: Int,
    val recipesByType: Map<BeverageType, Int>,
    val favoriteCount: Int,
    val mostUsedRecipes: List<Recipe>
)

data class RecipeExportData(
    val recipe: Recipe,
    val ingredients: List<RecipeIngredientWithDetails>,
    val steps: List<RecipeStep>,
    val calculations: List<RecipeCalculation>
)
