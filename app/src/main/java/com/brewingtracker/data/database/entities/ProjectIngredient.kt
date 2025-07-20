package com.brewingtracker.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "project_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["ingredientId"])
    ]
)
data class ProjectIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val projectId: String,
    val ingredientId: String, // Changed from Int to String to match Ingredient.id
    val quantity: Double,
    val unit: String, // oz, lbs, grams, etc.
    val additionTime: String? = null, // "boil", "5 min", "dry hop", etc.
    val notes: String? = null
)