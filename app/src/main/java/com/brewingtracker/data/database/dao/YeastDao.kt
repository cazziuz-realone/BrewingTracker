package com.brewingtracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.brewingtracker.data.database.entities.Yeast
import com.brewingtracker.data.database.entities.YeastType

@Dao
interface YeastDao {
    @Query("SELECT * FROM yeasts ORDER BY name ASC")
    fun getAllYeasts(): Flow<List<Yeast>>

    // FIXED: Parameter type changed from String to YeastType to match repository calls
    @Query("SELECT * FROM yeasts WHERE type = :type ORDER BY name ASC")
    fun getYeastsByType(type: YeastType): Flow<List<Yeast>>

    @Query("SELECT * FROM yeasts WHERE beverageTypes LIKE '%' || :beverageType || '%' ORDER BY name ASC")
    fun getYeastsByBeverageType(beverageType: String): Flow<List<Yeast>>

    @Query("SELECT * FROM yeasts WHERE name LIKE '%' || :searchTerm || '%' ORDER BY name ASC")
    fun searchYeasts(searchTerm: String): Flow<List<Yeast>>

    @Query("SELECT * FROM yeasts WHERE id = :id")
    suspend fun getYeastById(id: Int): Yeast?

    @Query("SELECT * FROM yeasts WHERE currentStock > 0 ORDER BY name ASC")
    fun getInStockYeasts(): Flow<List<Yeast>>

    @Query("SELECT * FROM yeasts WHERE isKveik = 1 ORDER BY name ASC")
    fun getKveikYeasts(): Flow<List<Yeast>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYeast(yeast: Yeast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYeasts(yeasts: List<Yeast>)

    @Update
    suspend fun updateYeast(yeast: Yeast)

    @Delete
    suspend fun deleteYeast(yeast: Yeast)

    // ADDED: Missing method that repository expects
    @Query("DELETE FROM yeasts WHERE id = :yeastId")
    suspend fun deleteYeast(yeastId: Int)

    @Query("UPDATE yeasts SET currentStock = :stock, updatedAt = :timestamp WHERE id = :yeastId")
    suspend fun updateStock(yeastId: Int, stock: Int, timestamp: Long = System.currentTimeMillis())
}
