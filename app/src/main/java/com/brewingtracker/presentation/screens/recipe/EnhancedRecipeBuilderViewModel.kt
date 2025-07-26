    fun createNewRecipe(beverageType: BeverageType) {
        val newRecipe = Recipe(
            name = "New ${beverageType.displayName} Recipe",
            beverageType = beverageType,
            description = "A delicious ${beverageType.displayName.lowercase()} recipe"
        )
        
        _uiState.value = _uiState.value.copy(
            recipe = newRecipe,
            processSteps = calculationService.generateDefaultSteps(beverageType),
            isLoading = false
        )
        
        // Trigger initial calculations
        calculateRecipeParameters()
    }