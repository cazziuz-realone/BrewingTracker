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
    private val recipeIngredientDao: RecipeIngredientDao
) {
    
    // === PROJECT OPERATIONS ===
    suspend fun createProject(project: Project): String {
        projectDao.insertProject(project)
        return project.id
    }
    
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    
    suspend fun deleteProject(projectId: String) = projectDao.deleteProject(projectId)
    
    // FIXED: Method name now matches ProjectDao
    fun getProjects(): Flow<List<Project>> = projectDao.getProjects()
    
    suspend fun getProjectById(projectId: String): Project? = projectDao.getProjectById(projectId)
    
    // === INGREDIENT OPERATIONS ===
    suspend fun addIngredient(ingredient: Ingredient) = ingredientDao.insertIngredient(ingredient)
    
    suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.updateIngredient(ingredient)
    
    suspend fun deleteIngredient(ingredientId: Int) = ingredientDao.deleteIngredient(ingredientId)
    
    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    
    suspend fun getIngredientById(ingredientId: Int): Ingredient? = ingredientDao.getIngredientById(ingredientId)
    
    fun getIngredientsByType(type: IngredientType): Flow<List<Ingredient>> = ingredientDao.getIngredientsByType(type)
    
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
    
    fun getProjectIngredients(projectId: String): Flow<List<ProjectIngredientWithDetails>> = 
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