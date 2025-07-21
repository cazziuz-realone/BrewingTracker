package com.brewingtracker.data.repository

import com.brewingtracker.data.database.dao.*
import com.brewingtracker.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrewingRepository @Inject constructor(
    private val ingredientDao: IngredientDao,
    private val yeastDao: YeastDao,
    private val projectDao: ProjectDao,
    private val projectIngredientDao: ProjectIngredientDao
) {
    
    // ==========================================
    // INGREDIENT OPERATIONS - USED FUNCTIONS ONLY
    // ==========================================
    
    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    
    suspend fun getIngredientById(id: Int): Ingredient? = ingredientDao.getIngredientById(id)
    
    fun getInStockIngredients(): Flow<List<Ingredient>> = ingredientDao.getInStockIngredients()
    
    suspend fun updateIngredientStock(ingredientId: Int, stock: Double) = 
        ingredientDao.updateStock(ingredientId, stock)
    
    suspend fun insertIngredients(ingredients: List<Ingredient>) = 
        ingredientDao.insertIngredients(ingredients)

    // ==========================================
    // YEAST OPERATIONS - KEEP FOR FUTURE USE
    // ==========================================
    
    fun getAllYeasts(): Flow<List<Yeast>> = yeastDao.getAllYeasts()
    
    fun getInStockYeasts(): Flow<List<Yeast>> = yeastDao.getInStockYeasts()
    
    suspend fun insertYeasts(yeasts: List<Yeast>) = yeastDao.insertYeasts(yeasts)

    // ==========================================
    // PROJECT OPERATIONS - USED FUNCTIONS ONLY
    // ==========================================
    
    fun getAllActiveProjects(): Flow<List<Project>> = projectDao.getAllActiveProjects()
    
    fun getFavoriteProjects(): Flow<List<Project>> = projectDao.getFavoriteProjects()
    
    suspend fun getProjectById(id: String): Project? = projectDao.getProjectById(id)
    
    suspend fun insertProject(project: Project) = projectDao.insertProject(project)
    
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    
    suspend fun updateProjectPhase(projectId: String, phase: ProjectPhase) = 
        projectDao.updateProjectPhase(projectId, phase)
    
    suspend fun updateProjectCompletion(projectId: String, isCompleted: Boolean) = 
        projectDao.updateProjectCompletion(projectId, isCompleted)
    
    suspend fun updateProjectFavorite(projectId: String, isFavorite: Boolean) = 
        projectDao.updateProjectFavorite(projectId, isFavorite)

    // ==========================================
    // PROJECT INGREDIENT OPERATIONS - KEEP FOR FUTURE USE
    // ==========================================
    
    fun getProjectIngredients(projectId: String): Flow<List<ProjectIngredient>> = 
        projectIngredientDao.getProjectIngredients(projectId)
    
    fun getProjectIngredientsWithDetails(projectId: String): Flow<List<ProjectIngredientWithDetails>> = 
        projectIngredientDao.getProjectIngredientsWithDetails(projectId)
    
    suspend fun addIngredientToProject(projectIngredient: ProjectIngredient) = 
        projectIngredientDao.insertProjectIngredient(projectIngredient)
    
    suspend fun removeIngredientFromProject(projectId: String, ingredientId: Int) = 
        projectIngredientDao.removeIngredientFromProject(projectId, ingredientId)
    
    suspend fun updateProjectIngredient(projectIngredient: ProjectIngredient) = 
        projectIngredientDao.updateProjectIngredient(projectIngredient)

    // ==========================================
    // ADDITIONAL METHODS FOR ADVANCED FILTERING (Optional)
    // ==========================================
    
    fun getProjectsByType(type: BeverageType): Flow<List<Project>> = 
        projectDao.getProjectsByType(type)
    
    fun getActiveIncompleteProjects(): Flow<List<Project>> = 
        projectDao.getActiveIncompleteProjects()
        
    // Search methods for ingredients
    fun getIngredientsByType(type: String): Flow<List<Ingredient>> = 
        ingredientDao.getIngredientsByType(type)
    
    fun getIngredientsByBeverageType(beverageType: String): Flow<List<Ingredient>> = 
        ingredientDao.getIngredientsByBeverageType(beverageType)
    
    fun searchIngredients(query: String): Flow<List<Ingredient>> = 
        ingredientDao.searchIngredients(query)

    // Yeast search methods for advanced UI
    fun getYeastsByType(type: String): Flow<List<Yeast>> = yeastDao.getYeastsByType(type)
    
    fun getYeastsByBeverageType(beverageType: String): Flow<List<Yeast>> = 
        yeastDao.getYeastsByBeverageType(beverageType)
    
    fun searchYeasts(query: String): Flow<List<Yeast>> = yeastDao.searchYeasts(query)
    
    suspend fun getYeastById(id: Int): Yeast? = yeastDao.getYeastById(id)
    
    fun getKveikYeasts(): Flow<List<Yeast>> = yeastDao.getKveikYeasts()
}