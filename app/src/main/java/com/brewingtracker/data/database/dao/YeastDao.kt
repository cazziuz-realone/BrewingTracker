package com.brewingtracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.brewingtracker.data.database.entities.Yeast
import com.brewingtracker.data.database.entities.YeastType

@Dao
interface YeastDao {
    @Query("SELECT * FROM yeasts ORDER BY name ASC")
    fun getAllYeasts(): Flow<List<Yeast>>

    @Query("SELECT * FROM yeasts WHERE type = :type ORDER BY name ASC")
    fun getYeastsByType(type: YeastType): Flow<List<Yeast>>

    @Query("SELECT * FROM yeasts WHERE name LIKE '%' || :searchTerm || '%' ORDER BY name ASC")
    fun searchYeasts(searchTerm: String): Flow<List<Yeast>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYeast(yeast: Yeast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYeasts(yeasts: List<Yeast>)

    @Update
    suspend fun updateYeast(yeast: Yeast)

    @Delete
    suspend fun deleteYeast(yeast: Yeast)
}