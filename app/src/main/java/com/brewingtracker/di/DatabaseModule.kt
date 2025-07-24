package com.brewingtracker.di

import android.content.Context
import androidx.room.Room
import com.brewingtracker.data.database.BrewingDatabase
import com.brewingtracker.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBrewingDatabase(@ApplicationContext context: Context): BrewingDatabase {
        return BrewingDatabase.getDatabase(context)
    }

    @Provides
    fun provideProjectDao(database: BrewingDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    fun provideIngredientDao(database: BrewingDatabase): IngredientDao {
        return database.ingredientDao()
    }

    @Provides
    fun provideYeastDao(database: BrewingDatabase): YeastDao {
        return database.yeastDao()
    }

    @Provides
    fun provideProjectIngredientDao(database: BrewingDatabase): ProjectIngredientDao {
        return database.projectIngredientDao()
    }

    // NEW: Recipe Builder DAOs - FIXED MISSING DEPENDENCY INJECTION
    @Provides
    fun provideRecipeDao(database: BrewingDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    fun provideRecipeIngredientDao(database: BrewingDatabase): RecipeIngredientDao {
        return database.recipeIngredientDao()
    }
}
