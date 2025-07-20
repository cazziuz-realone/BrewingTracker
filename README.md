# BrewingTracker 🍺

An Android app for tracking homebrew brewing processes, built with Jetpack Compose and modern Android architecture.

## 🚀 Features

- Track brewing recipes and processes
- Modern UI with Jetpack Compose
- Clean Architecture with MVVM pattern
- Room database for local storage
- Hilt dependency injection
- Material 3 design

## 🔧 Recent Fixes Applied

The following critical build issues were identified and fixed:

### ✅ **Root Build Configuration Fixed**
- **Issue**: Missing repositories configuration causing dependency resolution failures
- **Fix**: Added proper `repositories` blocks and build script configuration
- **Files**: `build.gradle.kts` (root)

### ✅ **Annotation Processing Fixed** 
- **Issue**: `kapt` configuration block incorrectly placed inside `defaultConfig`
- **Fix**: Moved kapt configuration to proper android-level scope
- **Files**: `app/build.gradle.kts`

### ✅ **Dependency Management Improved**
- **Issue**: Potential version compatibility issues
- **Fix**: Verified and organized dependency versions
- **Files**: `app/build.gradle.kts`

## 🏗️ Project Structure

```
app/
├── src/main/java/com/brewingtracker/
│   ├── data/                 # Data layer (Room, repositories)
│   ├── domain/               # Domain layer (entities, use cases)
│   ├── presentation/         # Presentation layer (screens, viewmodels)
│   ├── ui/theme/            # UI theming and styling
│   ├── di/                  # Dependency injection modules
│   ├── MainActivity.kt      # Main activity
│   └── BrewingTrackerApplication.kt  # Application class
└── src/main/res/            # Resources (layouts, strings, etc.)
```

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room
- **DI**: Hilt/Dagger
- **Navigation**: Navigation Compose
- **Image Loading**: Coil
- **Background Tasks**: WorkManager
- **Permissions**: Accompanist Permissions
- **Data Storage**: DataStore Preferences

## 🔨 Building the Project

### Prerequisites
- Android Studio Hedgehog+ (2023.1.1+)
- JDK 8 or higher
- Android SDK 24+ (minimum)
- Android SDK 34 (target)

### Build Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/cazziuz-realone/BrewingTracker.git
   cd BrewingTracker
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned repository

3. **Sync the project**
   - Android Studio should automatically trigger a Gradle sync
   - If not, click "Sync Now" when prompted or use `File > Sync Project with Gradle Files`

4. **Build and run**
   - Click the green "Run" button or press `Ctrl+F10` (Windows/Linux) or `Cmd+R` (Mac)

## 🐛 Troubleshooting

### Common Build Issues

**Issue**: "Could not resolve dependencies"
- **Solution**: Ensure you have internet connection and try `Build > Clean Project`, then `Build > Rebuild Project`

**Issue**: "Annotation processor error" 
- **Solution**: The kapt configuration has been fixed. If issues persist, try:
  ```bash
  ./gradlew clean
  ./gradlew build
  ```

**Issue**: "SDK not found"
- **Solution**: Update Android SDK in `Tools > SDK Manager` and ensure SDK 34 is installed

**Issue**: "Build tools version not supported"
- **Solution**: Update Android Studio to the latest stable version

### Cache Issues
If you encounter persistent build issues:
```bash
# Clean Gradle cache
./gradlew clean

# Clear Gradle daemon
./gradlew --stop

# Restart Android Studio and rebuild
```

## 📱 Running the App

1. **Physical Device**: Enable Developer Options and USB Debugging
2. **Emulator**: Create an AVD with API level 24+ in AVD Manager
3. **Click Run**: The app should build and install successfully

## 🚨 Important Notes

- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Compile SDK**: API 34
- **Kotlin Version**: 1.9.22
- **Compose Version**: 2024.02.00

## 📋 Development Guidelines

1. **Follow Clean Architecture principles**
2. **Use Compose for all UI components**
3. **Implement proper error handling**
4. **Write unit tests for business logic**
5. **Follow Material 3 design guidelines**

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Status**: ✅ **Build Issues Fixed** - The project should now compile and run successfully in Android Studio!