package com.brewingtracker.data.repository

import com.brewingtracker.data.database.dao.*
import com.brewingtracker.data.database.entities.*
import com.brewingtracker.data.models.ProjectIngredientWithDetails
import com.brewingtracker.data.models.RecipeIngredientWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    private val recipeStepDao: RecipeStepDao,
    private val recipeCalculationDao: RecipeCalculationDao
) {
    
    // === PROJECT OPERATIONS ===
    suspend fun createProject(project: Project): String {
        projectDao.insertProject(project)
        return project.id
    }
    
    // ADDED: Alias for ProjectsViewModel
    suspend fun insertProject(project: Project) = createProject(project)
    
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    
    suspend fun deleteProject(projectId: String) = projectDao.deleteProject(projectId)
    
    // FIXED: Method name now matches ProjectDao
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()
    
    // ADDED: Method for ProjectsViewModel  
    fun getFavoriteProjects(): Flow<List<Project>> = projectDao.getFavoriteProjects()
    
    suspend fun getProjectById(projectId: String): Project? = projectDao.getProjectById(projectId)
    
    // ADDED: Project phase update method
    suspend fun updateProjectPhase(projectId: String, phase: ProjectPhase) = 
        projectDao.updateProjectPhase(projectId, phase, System.currentTimeMillis())
    
    // ADDED: Project favorite toggle method
    suspend fun updateProjectFavorite(projectId: String, isFavorite: Boolean) = 
        projectDao.updateProjectFavorite(projectId, isFavorite, System.currentTimeMillis())
    
    // ADDED: Project completion method
    suspend fun updateProjectCompletion(projectId: String, isCompleted: Boolean) = 
        projectDao.updateProjectCompletion(projectId, isCompleted, System.currentTimeMillis())
    
    // === INGREDIENT OPERATIONS ===
    suspend fun addIngredient(ingredient: Ingredient) = ingredientDao.insertIngredient(ingredient)
    
    suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.updateIngredient(ingredient)
    
    suspend fun deleteIngredient(ingredientId: Int) = ingredientDao.deleteIngredient(ingredientId)
    
    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    
    // ADDED: Method for IngredientsViewModel
    fun getInStockIngredients(): Flow<List<Ingredient>> = 
        ingredientDao.getAllIngredients().map { ingredients ->
            ingredients.filter { it.currentStock > 0 }
        }
    
    suspend fun getIngredientById(ingredientId: Int): Ingredient? = ingredientDao.getIngredientById(ingredientId)
    
    fun getIngredientsByType(type: IngredientType): Flow<List<Ingredient>> = ingredientDao.getIngredientsByType(type)
    
    // FIXED: Search ingredients method signature
    suspend fun searchIngredients(query: String, type: IngredientType?): List<Ingredient> {
        return if (type != null) {
            ingredientDao.searchIngredientsByTypeAndName(type, query)
        } else {
            ingredientDao.searchIngredientsByName(query)
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
    
    // === PROJECT INGREDIENT OPERATIONS ===
    suspend fun addIngredientToProject(projectIngredient: ProjectIngredient) = 
        projectIngredientDao.insertProjectIngredient(projectIngredient)
    
    suspend fun updateProjectIngredient(projectIngredient: ProjectIngredient) = 
        projectIngredientDao.updateProjectIngredient(projectIngredient)
    
    // FIXED: Parameter name to match DAO method
    suspend fun removeIngredientFromProject(projectIngredientId: Int) = 
        projectIngredientDao.deleteProjectIngredient(projectIngredientId)
    
    // ADDED: Method to remove ingredient by project and ingredient IDs
    suspend fun removeIngredientFromProject(projectId: String, ingredientId: Int) = 
        projectIngredientDao.removeIngredientFromProject(projectId, ingredientId)
    
    // ADDED: Method to remove all ingredients from a project
    suspend fun removeAllIngredientsFromProject(projectId: String) = 
        projectIngredientDao.removeAllIngredientsFromProject(projectId)
    
    // ADDED: Method to update project ingredient with individual parameters
    suspend fun updateProjectIngredient(
        projectId: String,
        ingredientId: Int,
        quantity: Double,
        unit: String,
        additionTime: String? = null
    ) = projectIngredientDao.updateProjectIngredientDetails(projectId, ingredientId, quantity, unit, additionTime)
    
    fun getProjectIngredients(projectId: String): Flow<List<ProjectIngredientWithDetails>> = 
        projectIngredientDao.getProjectIngredientsWithDetails(projectId)
    
    // ADDED: Alias method to match ViewModel usage
    fun getProjectIngredientsWithDetails(projectId: String): Flow<List<ProjectIngredientWithDetails>> = 
        projectIngredientDao.getProjectIngredientsWithDetails(projectId)
    
    // === RECIPE OPERATIONS ===
    suspend fun createRecipe(recipe: Recipe): String {
        recipeDao.insertRecipe(recipe)
        return recipe.id
    }
    
    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)
    
    suspend fun deleteRecipe(recipeId: String) = recipeDao.deleteRecipeById(recipeId)
    
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
    
    suspend fun getRecipeById(recipeId: String): Recipe? = recipeDao.getRecipeById(recipeId)
    
    fun getRecipesByType(type: BeverageType): Flow<List<Recipe>> = recipeDao.getRecipesByBeverageType(type)
    
    // === RECIPE INGREDIENT OPERATIONS ===
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long = 
        recipeIngredientDao.insertRecipeIngredient(recipeIngredient)
    
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient) = 
        recipeIngredientDao.updateRecipeIngredient(recipeIngredient)
    
    suspend fun deleteRecipeIngredient(recipeIngredientId: Int) = 
        recipeIngredientDao.deleteRecipeIngredientById(recipeIngredientId)
    
    fun getRecipeIngredients(recipeId: String): Flow<List<RecipeIngredient>> = 
        recipeIngredientDao.getRecipeIngredients(recipeId)
    
    fun getRecipeIngredientsWithDetails(recipeId: String): Flow<List<RecipeIngredientWithDetails>> = 
        recipeIngredientDao.getRecipeIngredientsWithDetails(recipeId)
    
    // === RECIPE STEP OPERATIONS ===
    suspend fun insertRecipeStep(recipeStep: RecipeStep): Long = 
        recipeStepDao.insertRecipeStep(recipeStep)
    
    suspend fun updateRecipeStep(recipeStep: RecipeStep) = 
        recipeStepDao.updateRecipeStep(recipeStep)
    
    suspend fun deleteRecipeStep(stepId: Int) = 
        recipeStepDao.deleteRecipeStep(stepId)
    
    fun getRecipeSteps(recipeId: String): Flow<List<RecipeStep>> = 
        recipeStepDao.getRecipeSteps(recipeId)
    
    // === RECIPE DUPLICATION ===
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
        
        // Duplicate ingredients using the DAO method
        recipeIngredientDao.duplicateRecipeIngredients(sourceRecipeId, newRecipe.id)
        
        return newRecipe.id
    }
}
