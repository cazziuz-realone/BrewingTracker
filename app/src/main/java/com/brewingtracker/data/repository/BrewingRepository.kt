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
    // INGREDIENT OPERATIONS
    // ==========================================
    
    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    
    fun getIngredientsByType(type: String): Flow<List<Ingredient>> = 
        ingredientDao.getIngredientsByType(type)
    
    fun getIngredientsByBeverageType(beverageType: String): Flow<List<Ingredient>> = 
        ingredientDao.getIngredientsByBeverageType(beverageType)
    
    fun searchIngredients(query: String): Flow<List<Ingredient>> = 
        ingredientDao.searchIngredients(query)
    
    suspend fun getIngredientById(id: Int): Ingredient? = ingredientDao.getIngredientById(id)
    
    fun getInStockIngredients(): Flow<List<Ingredient>> = ingredientDao.getInStockIngredients()
    
    suspend fun insertIngredients(ingredients: List<Ingredient>) = 
        ingredientDao.insertIngredients(ingredients)
    
    suspend fun updateIngredientStock(ingredientId: Int, stock: Double) = 
        ingredientDao.updateStock(ingredientId, stock)
    
    suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.updateIngredient(ingredient)
    
    // ==========================================
    // YEAST OPERATIONS
    // ==========================================
    
    fun getAllYeasts(): Flow<List<Yeast>> = yeastDao.getAllYeasts()
    
    fun getYeastsByType(type: String): Flow<List<Yeast>> = yeastDao.getYeastsByType(type)
    
    fun getYeastsByBeverageType(beverageType: String): Flow<List<Yeast>> = 
        yeastDao.getYeastsByBeverageType(beverageType)
    
    fun searchYeasts(query: String): Flow<List<Yeast>> = yeastDao.searchYeasts(query)
    
    suspend fun getYeastById(id: Int): Yeast? = yeastDao.getYeastById(id)
    
    fun getInStockYeasts(): Flow<List<Yeast>> = yeastDao.getInStockYeasts()
    
    fun getKveikYeasts(): Flow<List<Yeast>> = yeastDao.getKveikYeasts()
    
    suspend fun insertYeasts(yeasts: List<Yeast>) = yeastDao.insertYeasts(yeasts)
    
    suspend fun updateYeastStock(yeastId: Int, stock: Int) = yeastDao.updateStock(yeastId, stock)
    
    suspend fun updateYeast(yeast: Yeast) = yeastDao.updateYeast(yeast)
    
    // ==========================================
    // PROJECT OPERATIONS
    // ==========================================
    
    fun getAllActiveProjects(): Flow<List<Project>> = projectDao.getAllActiveProjects()
    
    fun getProjectsByType(type: ProjectType): Flow<List<Project>> = 
        projectDao.getProjectsByType(type)
    
    fun getActiveIncompleteProjects(): Flow<List<Project>> = 
        projectDao.getActiveIncompleteProjects()
    
    fun getFavoriteProjects(): Flow<List<Project>> = projectDao.getFavoriteProjects()
    
    suspend fun getProjectById(id: String): Project? = projectDao.getProjectById(id)
    
    suspend fun insertProject(project: Project) = projectDao.insertProject(project)
    
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)
    
    suspend fun updateProjectPhase(projectId: String, phase: ProjectPhase) = 
        projectDao.updateProjectPhase(projectId, phase)
    
    suspend fun updateProjectCompletion(projectId: String, isCompleted: Boolean) = 
        projectDao.updateProjectCompletion(projectId, isCompleted)
    
    suspend fun updateProjectFavorite(projectId: String, isFavorite: Boolean) = 
        projectDao.updateProjectFavorite(projectId, isFavorite)
    
    // ==========================================
    // PROJECT INGREDIENT OPERATIONS
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
}