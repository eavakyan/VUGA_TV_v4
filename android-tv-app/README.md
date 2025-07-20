# VUGA TV - Android TV Streaming Application

A premium Netflix-style streaming application built specifically for Android TV devices, featuring content browsing, video playback, user profiles, and cast member information with optimized D-pad navigation.

## 🎯 Overview

VUGA TV is a comprehensive streaming platform designed for the lean-back TV viewing experience. Built with modern Android development practices, it provides a seamless content discovery and playback experience optimized for Android TV devices.

## ✨ Key Features

### 🏠 Home Screen
- **Featured Content Carousel**: Auto-sliding horizontal carousel with 5-second intervals
- **Content Categories**: Multiple rows including "Trending Now", "New Releases", "Action Movies"
- **Smart Focus Management**: D-pad navigation with blue border highlighting and scaling effects
- **Immediate Interactivity**: Featured slider is clickable and navigable on app launch

### 🎬 Content Detail Screen
- **Hero Section**: Blurred background with gradient overlay and prominent poster
- **TV Show Support**: Season selector and episode grid with "S1E1" format titles
- **Cast & Crew**: Horizontal scrollable cast member cards with circular images
- **Related Content**: "More Like This" section with similar content recommendations
- **Action Buttons**: Play and More Info buttons with proper focus handling

### 🎭 Cast Detail Screen
- **Actor Profiles**: Detailed actor information with filmography
- **Related Content**: Shows other content featuring the selected actor
- **Navigation**: Seamless integration with content detail flow

### 🎥 Video Player
- **ExoPlayer Integration**: High-quality video playback with HLS and DASH support
- **TV-Optimized Controls**: D-pad navigation with auto-hiding controls
- **Resume Playback**: Remembers and resumes from last watched position
- **Background Audio**: Continues playback when app is backgrounded

### 🔍 Search Functionality
- **Real-time Search**: Instant search results with debouncing
- **Content Filtering**: Search across movies, TV shows, and other content
- **Focus Management**: Optimized keyboard and D-pad navigation

### 👤 User Profiles
- **Multiple Profiles**: Family-friendly account separation
- **Watch History**: Automatic progress tracking and resume functionality
- **Favorites**: Add/remove content to personal watchlist
- **Profile Management**: User preferences and settings

## 🏗️ Technical Architecture

### Architecture Pattern
- **MVVM (Model-View-ViewModel)** with Clean Architecture principles
- **Repository Pattern** for data management
- **Dependency Injection** using Hilt
- **Reactive Programming** with Kotlin Flows and StateFlow

### Core Technologies

#### Frontend (Android TV)
- **Language**: Kotlin 100%
- **UI Framework**: Android Views (optimized for TV focus handling)
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt (Dagger)
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Video Player**: ExoPlayer (Media3)
- **Database**: Room (SQLite)
- **Async**: Kotlin Coroutines + Flow
- **Network**: Retrofit + OkHttp
- **Data Storage**: DataStore for preferences

#### Backend Integration
- **API Base URL**: `https://iosdev.gossip-stone.com/api/`
- **Authentication**: API key-based authentication
- **Content Types**: Movies, TV Shows, Episodes, Cast Members
- **Real-time Data**: Live content updates and user interactions

## 📱 Project Structure

```
android-tv-app/
├── app/
│   ├── src/main/java/com/vugaenterprises/androidtv/
│   │   ├── data/
│   │   │   ├── api/                    # Network layer (ApiService)
│   │   │   ├── model/                  # Data models (Content, Episode, Cast)
│   │   │   ├── repository/             # Data repositories
│   │   │   ├── VideoPlayerDataStore.kt # Video player state management
│   │   │   ├── EpisodeDataStore.kt     # Episode selection state
│   │   │   └── CastDetailDataStore.kt  # Cast detail state
│   │   ├── di/                         # Dependency injection (NetworkModule)
│   │   ├── player/                     # Media player services
│   │   ├── ui/
│   │   │   ├── components/             # Reusable UI components
│   │   │   │   ├── AutoScrollingBanner.kt
│   │   │   │   ├── CastMemberAdapter.kt
│   │   │   │   ├── ContentCard.kt
│   │   │   │   ├── ContentCardAdapter.kt
│   │   │   │   ├── ContentRow.kt
│   │   │   │   ├── FeaturedContentSection.kt
│   │   │   │   └── FeaturedSliderAdapter.kt
│   │   │   ├── navigation/             # Navigation logic
│   │   │   │   ├── AppNavigation.kt
│   │   │   │   └── Screen.kt
│   │   │   ├── screens/                # Screen implementations
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── HomeScreenView.kt
│   │   │   │   ├── ContentDetailScreen.kt
│   │   │   │   ├── ContentDetailView.kt
│   │   │   │   ├── VideoPlayerScreen.kt
│   │   │   │   ├── SearchScreen.kt
│   │   │   │   ├── ProfileScreen.kt
│   │   │   │   ├── FavoritesScreen.kt
│   │   │   │   ├── HistoryScreen.kt
│   │   │   │   ├── CastDetailView.kt
│   │   │   │   └── SplashScreen.kt
│   │   │   ├── theme/                  # UI theming
│   │   │   └── viewmodels/             # ViewModels
│   │   ├── MainActivity.kt             # Main activity with splash screen
│   │   └── StreamingApplication.kt     # Application class
│   └── src/main/res/
│       ├── drawable/                   # Vector graphics & backgrounds
│       ├── layout/                     # XML layouts
│       ├── values/                     # Colors, strings, themes
│       └── xml/                        # App configuration
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android TV device or emulator (API 21+)
- JDK 17
- Android SDK 34

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Android_TV_App/android-tv-app
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the `android-tv-app` folder

3. **Sync and Build**
   ```bash
   ./gradlew clean build
   ```

4. **Deploy to Android TV**
   ```bash
   # Connect your Android TV device via ADB
   adb connect <device-ip>
   
   # Install the APK
   adb install -r ./app/build/outputs/apk/debug/app-debug.apk
   ```

### Development Setup

1. **Enable Developer Options** on your Android TV device
2. **Enable ADB Debugging** in developer options
3. **Connect via ADB** using the device's IP address
4. **Build and deploy** using the commands above

## 🎮 TV Navigation Features

### D-Pad Navigation
- **Focus Management**: Blue border highlighting with 1.1x scaling
- **Auto-sliding**: Featured content carousel with 5-second intervals
- **Boundary Handling**: LEFT/RIGHT navigation loops within sections
- **Immediate Response**: No delay in focus changes or interactions

### Key Features
- **ENTER/SELECT**: Activates focused content or buttons
- **BACK**: Navigates to previous screen
- **UP/DOWN**: Vertical navigation between sections
- **LEFT/RIGHT**: Horizontal navigation within sections
- **Auto-focus**: Smart focus management for optimal TV experience

## 📊 Data Models

### Content Structure
```kotlin
data class Content(
    val contentId: Int,
    val title: String,
    val description: String,
    val type: Int,                    // 0=Movie, 1=TV Show
    val duration: String,
    val releaseYear: Int,
    val ratings: Double,
    val verticalPoster: String,
    val horizontalPoster: String,
    val contentCast: List<CastItem>,
    val seasons: List<SeasonItem>,    // For TV shows
    val moreLikeThis: List<Content>
)
```

### Episode Structure
```kotlin
data class EpisodeItem(
    val id: Int,
    val seasonId: Int,
    val number: Int,
    val title: String,
    val description: String,
    val duration: String,
    val sources: List<SourceItem>
)
```

## 🔧 Configuration

### API Configuration
- **Base URL**: Configured in `NetworkModule.kt`
- **API Key**: Authentication handled automatically
- **Timeout**: 1-minute timeout for all requests

### Build Configuration
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Java Version**: 17

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] D-pad navigation on all screens
- [ ] Auto-sliding featured carousel
- [ ] Video playback functionality
- [ ] Search and filtering
- [ ] User profile management
- [ ] Cast member navigation
- [ ] Episode selection for TV shows

## 🚀 Deployment

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### APK Location
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## 📝 Recent Updates

### Latest Features
- ✅ **Auto-sliding featured carousel** with focus-based control
- ✅ **TV show episode support** with season/episode navigation
- ✅ **Cast & Crew integration** on content detail screens
- ✅ **Improved D-pad navigation** with boundary handling
- ✅ **Immediate interactivity** - no focus delays
- ✅ **Enhanced focus management** for optimal TV experience

### Technical Improvements
- ✅ **Android Views implementation** for reliable TV focus
- ✅ **ExoPlayer integration** with Media3
- ✅ **Hilt dependency injection** for clean architecture
- ✅ **Room database** for local data persistence
- ✅ **Retrofit networking** with proper error handling

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Check the [Android TV App Requirements Document](Android_TV_App_Requirements_Doc.md)
- Review the [Setup Guide](docs/SETUP_GUIDE.md)
- Examine the [Database Choice Documentation](docs/DATABASE_CHOICE.md)

---

**Built with ❤️ for Android TV enthusiasts** 