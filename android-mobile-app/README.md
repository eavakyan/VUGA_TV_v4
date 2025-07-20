# Flixy - Android TV & Mobile Streaming Application

## 📱 Application Overview

**Flixy** is a comprehensive Android streaming application designed for both mobile devices and Android TV. It provides users with access to movies, TV shows, live TV channels, and downloadable content with a modern, responsive interface that adapts to different screen sizes and orientations.

### 🎯 Key Features

- **Multi-Platform Support**: Optimized for both mobile phones and Android TV
- **Responsive Design**: Adaptive layouts for portrait, landscape, and tablet orientations
- **Content Streaming**: Movies, TV shows, and live TV channels
- **Download Management**: Offline content download and management
- **Auto-Scrolling Featured Content**: Dynamic slider with smooth transitions
- **Multi-Language Support**: 20+ language localizations
- **User Authentication**: Google Sign-In integration
- **Push Notifications**: Firebase Cloud Messaging integration
- **Subtitle Support**: SRT subtitle parsing and display
- **Recently Watched**: Content history tracking
- **Watchlist Management**: Personal content curation
- **Search Functionality**: Content and live TV search
- **Genre-based Browsing**: Categorized content discovery

## 🏗️ Architecture

### Technology Stack
- **Language**: Java (Android)
- **UI Framework**: Android Data Binding
- **Image Loading**: Glide
- **Networking**: Retrofit + RxJava
- **Architecture**: MVVM with ViewModel
- **Dependency Injection**: Manual DI with ViewModelFactory
- **Blur Effects**: BlurView library
- **Video Player**: ExoPlayer
- **Analytics**: Firebase Analytics
- **Crash Reporting**: Firebase Crashlytics

### Project Structure
```
app/src/main/
├── java/com/retry/vuga/
│   ├── activities/          # Main UI activities
│   ├── adapters/           # RecyclerView adapters
│   ├── fragments/          # UI fragments
│   ├── model/              # Data models
│   ├── retrofit/           # API client
│   ├── utils/              # Utility classes
│   └── viewmodel/          # ViewModels
├── res/
│   ├── layout/             # UI layouts
│   ├── layout-land/        # Landscape layouts
│   ├── layout-sw600dp/     # Tablet layouts
│   ├── values/             # Resources
│   └── drawable/           # Graphics
```

## 🔧 Recent Fixes & Improvements

### Date: July 14, 2025

#### Critical Bug Fixes
1. **Recycled Bitmap Crash Resolution**
   - **Issue**: App crashed during orientation changes due to recycled bitmap usage in Glide placeholders
   - **Solution**: Replaced dynamic placeholder with static drawable resource
   - **Files Modified**: `HomeFragment.java`

2. **Auto-Scrolling Stability Improvements**
   - **Issue**: NullPointerExceptions during fragment lifecycle changes
   - **Solution**: Comprehensive null checks and proper lifecycle management
   - **Files Modified**: `HomeFragment.java`

3. **Data Binding Collision Resolution**
   - **Issue**: View ID collisions between mobile and tablet layouts
   - **Solution**: Renamed placeholder RecyclerView IDs in tablet layouts
   - **Files Modified**: Multiple layout XML files

4. **MainActivity Crash Prevention**
   - **Issue**: NullPointerExceptions due to missing views in some layouts
   - **Solution**: Added null checks around all binding references
   - **Files Modified**: `MainActivity.java`

#### Layout & UI Improvements
- **Tablet Landscape Support**: Added missing views and proper navigation
- **Orientation Handling**: Improved rotation stability
- **Memory Management**: Better resource cleanup during lifecycle changes

### Previous Updates

#### Date: May 28, 2025
- Updated SDK version to 36
- Fixed push notification crashes
- Increased episode view & download count tracking

#### Date: February 26, 2025
- Added embedded video link support
- Updated dependencies

#### Date: January 22, 2025
- **Subtitle System Implementation**
  - Added SRT subtitle downloader
  - Implemented subtitle parser and display
  - Enhanced video player with subtitle support

#### Date: January 13, 2025
- Fixed home page data duplication issues

#### Date: December 31, 2024
- **Recently Watched Feature**
  - Added movie history tracking
  - Implemented pull-to-refresh functionality
  - Enhanced notification deep linking

#### Date: October 15, 2024
- Fixed dialog blur issues
- Android 14 compatibility improvements

#### Date: August 30, 2024
- Implemented .m3u8 player support
- Fixed home page crashes

## 🚀 Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 36
- Java 8 or higher
- Google Play Services (for authentication)

### Build Instructions
1. Clone the repository
2. Open project in Android Studio
3. Sync Gradle dependencies
4. Configure Firebase (if using push notifications)
5. Build and run on device/emulator

### Configuration
- Update API endpoints in `RetrofitClient.java`
- Configure Firebase in `google-services.json`
- Set up Google Sign-In credentials
- Configure CDN URLs in `Const.java`

## 📱 Device Support

### Mobile Devices
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 36 (Android 15)
- **Orientations**: Portrait and Landscape
- **Screen Sizes**: Phone, Large Phone

### Android TV
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 36 (Android 15)
- **Orientations**: Landscape only
- **Screen Sizes**: TV (sw600dp and larger)

### Tablet Support
- **Layouts**: Dedicated tablet layouts in `layout-sw600dp/`
- **Navigation**: Side navigation for landscape tablets
- **Responsive Design**: Adaptive UI elements

## 🔒 Security Features

- **Secure API Communication**: HTTPS endpoints
- **User Authentication**: Google Sign-In
- **Session Management**: Secure token storage
- **Content Protection**: DRM support for premium content

## 📊 Performance Optimizations

- **Image Caching**: Glide with memory and disk caching
- **Lazy Loading**: RecyclerView with efficient adapters
- **Memory Management**: Proper lifecycle handling
- **Network Optimization**: Retrofit with connection pooling

## 🌐 Internationalization

The app supports 20+ languages:
- Arabic, Danish, German, Greek, English, Spanish
- French, Hindi, Indonesian, Italian, Japanese
- Korean, Norwegian, Dutch, Polish, Portuguese
- Russian, Swedish, Thai, Turkish, Vietnamese, Chinese

## 🐛 Known Issues & Limitations

- Google Play Billing API unavailable on emulator (normal behavior)
- Some features require Google Play Services
- Live TV functionality depends on backend service availability

## 📈 Future Enhancements

- **Offline Mode**: Enhanced offline content management
- **Social Features**: User reviews and ratings
- **Advanced Search**: Filters and recommendations
- **Parental Controls**: Content filtering
- **Chromecast Support**: Cast to TV functionality

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly on multiple devices/orientations
5. Submit a pull request

## 📄 License

This project is proprietary software. All rights reserved.

---

**Last Updated**: July 14, 2025  
**Version**: 1.0.0  
**Maintainer**: Vuga Enterprises

