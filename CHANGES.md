# BrewingTracker Changes Log

## Date: 2025-07-26

### New Files Created

#### 1. **app/.../entities/BatchSize.kt**
- Added enum for batch size scaling
- Values: QUART (0.25x), HALF_GALLON (0.5x), GALLON (1.0x), FIVE_GALLON (5.0x)
- Each size includes displayName, ozValue, and scaleFactor properties

#### 2. **app/.../entities/InventoryStatus.kt**
- Added enum for ingredient inventory status tracking
- Values: SUFFICIENT, INSUFFICIENT, UNKNOWN
- Used in recipe builder to show stock availability

#### 3. **app/.../models/LiveRecipeCalculations.kt**
- Data class for real-time recipe calculations
- Properties: estimatedOG, estimatedFG, estimatedABV, estimatedSRM, estimatedCost
- Includes batchSize, totalGravityPoints, and totalVolume

#### 4. **app/.../services/RecipeCalculationService.kt**
- Complete implementation of recipe calculation engine
- Methods:
  - `calculateRecipeParameters()` - Calculates OG, FG, ABV, SRM based on ingredients
  - `checkInventoryStatus()` - Verifies ingredient availability
  - `scaleRecipe()` - Scales recipes between batch sizes
  - `generateDefaultSteps()` - Creates beverage-specific process steps
- Includes gravity calculations for different ingredient types
- Supports MEAD, BEER, WINE, CIDER, KOMBUCHA beverage types

### Modified Files

#### 1. **app/.../entities/Ingredient.kt**
- Updated IngredientType enum:
  - Added: HONEY, SUGAR, HERB, HOPS, NUTRIENT
  - These were missing but required by RecipeCalculationService
- Added colorSRM property for color calculations
- Changed costPerUnit to non-nullable with default value 0.0

#### 2. **app/.../screens/recipe/EnhancedRecipeBuilderViewModel.kt**
- Fixed RecipeStep generation to use proper recipe IDs
- Updated createNewRecipe() to map placeholder IDs to actual recipe IDs
- Enhanced saveRecipe() to handle both create and update operations
- Added proper RecipeStep persistence for new recipes

#### 3. **app/.../services/RecipeCalculationService.kt** (Updated)
- Modified all step generation methods to accept recipeId parameter
- Changed from creating RecipeStep objects without IDs to accepting recipeId
- Fixed compilation issues with RecipeStep entity requirements

### Technical Details

#### Compilation Errors Fixed:
1. **Unresolved references**: BatchSize, InventoryStatus, LiveRecipeCalculations
2. **Missing service**: RecipeCalculationService not found by Hilt
3. **Type mismatches**: IngredientType missing required enum values
4. **Entity constraints**: RecipeStep requiring non-null recipeId

#### Architecture Improvements:
- Proper separation of concerns with dedicated calculation service
- Reactive state management with Flow and StateFlow
- Proper dependency injection with @Singleton and @Inject
- Clean entity relationships with proper foreign keys

### Database Impact
- No schema changes required
- New enum values compatible with existing type converters
- All changes backward compatible

### Testing Requirements
1. Verify recipe calculations produce accurate OG/FG/ABV values
2. Test inventory status checking with various stock levels
3. Confirm batch size scaling maintains proper ratios
4. Validate step generation for each beverage type

### Performance Considerations
- Calculations are performed asynchronously in coroutines
- Inventory checks are optimized with single-pass mapping
- Recipe scaling uses efficient collection transformations
