    // Duplicate recipe ingredients for recipe copying
    suspend fun duplicateRecipeIngredients(
        sourceRecipeId: String, 
        newRecipeId: String
    ) {
        val sourceIngredients = getRecipeIngredients(sourceRecipeId)
        val newIngredients = sourceIngredients.map { ingredient ->
            ingredient.copy(
                id = 0, // Auto-generate new ID
                recipeId = newRecipeId,
                createdAt = System.currentTimeMillis()
            )
        }
        insertRecipeIngredients(newIngredients)
    }