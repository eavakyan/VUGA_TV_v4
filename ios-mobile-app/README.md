# VUGA iOS App

A comprehensive iOS streaming application built with SwiftUI, featuring a modern video streaming platform with support for movies, TV series, live TV, and user management. This app is designed to provide a seamless entertainment experience across iPhone and iPad devices.

## 📱 App Overview

VUGA is a feature-rich streaming platform that offers:
- **Multi-platform Content**: Movies, TV series, documentaries, and live TV
- **User Management**: Authentication, profiles, and personalized experiences
- **Cross-Device Support**: Optimized for both iPhone and iPad
- **Offline Viewing**: Download content for offline consumption
- **Multi-language Support**: Internationalization with 20+ languages
- **Premium Features**: Subscription-based content access

## 🚀 Key Features

### Core Functionality
- **Content Streaming**: High-quality video playback with VLC integration
- **Live TV**: Real-time television streaming with channel management
- **User Authentication**: Secure login with Google Sign-In integration
- **Content Discovery**: Advanced search and genre-based browsing
- **Watchlist Management**: Save and organize favorite content
- **Download System**: Offline content access with download management
- **Multi-language Support**: Localized interface in 20+ languages

### Technical Features
- **SwiftUI Architecture**: Modern declarative UI framework
- **Core Data Integration**: Local data persistence and management
- **Firebase Integration**: Analytics, notifications, and backend services
- **RevenueCat**: Subscription management and in-app purchases
- **Branch.io**: Deep linking and user acquisition
- **Google Mobile Ads**: Monetization through banner and interstitial ads
- **VLC Player**: Professional video playback capabilities
- **YouTube Integration**: External video content support

## 📋 Prerequisites

Before running this application, ensure you have the following:

- **Xcode** (v15.0 or higher)
- **iOS Deployment Target**: iOS 16.1+
- **CocoaPods**: For dependency management
- **Apple Developer Account**: For device testing and App Store deployment
- **Firebase Project**: For backend services
- **RevenueCat Account**: For subscription management

## 🛠️ Installation & Setup

### 1. Clone and Install Dependencies

```bash
# Clone the repository
git clone <repository-url>
cd vuga_ios_app_july_2025

# Install CocoaPods dependencies
pod install

# Open the workspace (not the project file)
open Flixy.xcworkspace
```

### 2. Configuration Setup

#### Firebase Configuration
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an iOS app to your Firebase project
3. Download `GoogleService-Info.plist` and add it to the project root
4. Enable Authentication, Analytics, and Cloud Messaging

#### RevenueCat Configuration
1. Create a RevenueCat account at [RevenueCat](https://www.revenuecat.com/)
2. Add your app and configure subscription products
3. Update the API key in `Const.swift`

#### Branch.io Configuration
1. Create a Branch.io account at [Branch.io](https://branch.io/)
2. Configure your app for deep linking
3. Update the Branch key in the project settings

### 3. Environment Configuration

Update the following constants in `Flixy/Utilities/Const.swift`:

```swift
let APP_NAME = "VUGA"
let APP_ID = "6746174928"
let RevenueCatApiKey = "your-revenuecat-api-key"
let PRIVACY_URL = "https://yourdomain.com/privacy"
let TERMS_URL = "https://yourdomain.com/terms"
```

### 4. Build and Run

```bash
# Select your target device or simulator
# Build and run the project
# The app will launch with sample content
```

## 📁 Project Structure

```
vuga_ios_app_july_2025/
├── Flixy/
│   ├── App/
│   │   ├── AppDelegate.swift          # App lifecycle and configuration
│   │   ├── ContentView.swift          # Main app container
│   │   └── FlixyApp.swift             # SwiftUI app entry point
│   ├── Views/
│   │   ├── SplashView.swift           # App launch screen
│   │   ├── LoginView.swift            # User authentication
│   │   ├── TabBarView.swift           # Main navigation
│   │   ├── HomeView.swift             # Content discovery
│   │   ├── SearchView.swift           # Content search
│   │   ├── LiveTVsView.swift          # Live TV streaming
│   │   ├── WatchlistView.swift        # Saved content
│   │   ├── ContentDetailView.swift    # Content details and playback
│   │   ├── ProfileView.swift          # User profile management
│   │   ├── VideoPlayerView.swift      # Video playback interface
│   │   └── ...                        # Additional view components
│   ├── ViewModels/
│   │   ├── BaseViewModel.swift        # Base view model class
│   │   ├── SplashViewModel.swift      # Splash screen logic
│   │   ├── LoginViewModel.swift       # Authentication logic
│   │   ├── HomeViewModel.swift        # Content management
│   │   ├── ContentDetailViewModel.swift # Content details logic
│   │   └── ...                        # Additional view models
│   ├── Models/
│   │   ├── ContentModel.swift         # Content data structure
│   │   ├── UserModel.swift            # User data structure
│   │   ├── SettingModel.swift         # App settings
│   │   └── ...                        # Additional data models
│   ├── Utilities/
│   │   ├── Const.swift                # App constants and configuration
│   │   ├── APIs.swift                 # API endpoint definitions
│   │   ├── Extention/                 # Swift extensions
│   │   └── ...                        # Utility classes
│   ├── CoreData/
│   │   ├── DataController.swift       # Core Data management
│   │   └── Flixy.xcdatamodeld         # Data model
│   ├── Localization/
│   │   ├── en.lproj/                  # English localization
│   │   ├── ar.lproj/                  # Arabic localization
│   │   ├── es.lproj/                  # Spanish localization
│   │   └── ...                        # 20+ language support
│   ├── Assets/
│   │   ├── Assets.xcassets/           # App icons and images
│   │   ├── Fonts/                     # Custom fonts
│   │   └── Lottie/                    # Animation files
│   ├── Ad/
│   │   ├── BannerAd.swift             # Banner advertisement
│   │   ├── InterstitialAd.swift       # Interstitial ads
│   │   └── BannerAdViewModel.swift    # Ad management
│   └── Info.plist                     # App configuration
├── liveActivity/                      # Live Activity extension
├── Flixy.xcodeproj/                   # Xcode project file
├── Flixy.xcworkspace/                 # CocoaPods workspace
├── Podfile                            # CocoaPods dependencies
└── GoogleService-Info.plist           # Firebase configuration
```

## 🔌 Core Components

### App Architecture
- **SwiftUI**: Modern declarative UI framework
- **MVVM Pattern**: Model-View-ViewModel architecture
- **Combine**: Reactive programming for data binding
- **Core Data**: Local data persistence
- **Firebase**: Backend services and analytics

### Key Dependencies
```ruby
# Podfile dependencies
pod 'Firebase/Analytics'
pod 'Firebase/Auth'
pod 'Firebase/Messaging'
pod 'GoogleSignIn'
pod 'Alamofire'
pod 'Kingfisher'
pod 'Lottie'
pod 'MobileVLCKit'
pod 'RevenueCat'
pod 'BranchSDK'
pod 'Google-Mobile-Ads-SDK'
```

### Data Models

#### Content Model
```swift
struct Content {
    let id: Int
    let title: String
    let description: String
    let type: ContentType // movie, series, documentary
    let genre: [String]
    let releaseYear: Int
    let duration: Int
    let rating: Double
    let posterURL: String?
    let videoURL: String?
    let isPremium: Bool
}
```

#### User Model
```swift
struct User {
    let id: Int
    let username: String
    let email: String
    let profileImage: String?
    let subscription: SubscriptionType
    let watchHistory: [Int]
    let preferences: UserPreferences
}
```

## 🌐 API Integration

### Backend Services
- **Content API**: Fetch movies, series, and live TV content
- **User API**: Authentication and profile management
- **Analytics API**: User behavior and content analytics
- **Payment API**: Subscription and purchase processing

### API Endpoints
```swift
// Base URL configuration
static let base = "https://iosdev.gossip-stone.com/"
static let apiBase = WebService.base + "api/"

// Key endpoints
static let fetchSettings = "fetchSettings"
static let fetchProfile = "fetchProfile"
static let fetchContent = "fetchContent"
static let fetchLiveTV = "fetchLiveTV"
```

## 📱 User Interface

### Design System
- **Color Scheme**: Dark theme with accent colors
- **Typography**: Custom Outfit font family
- **Icons**: Custom icon set for consistent branding
- **Animations**: Lottie animations for enhanced UX

### Navigation Structure
```
SplashView
├── LoginView (if not authenticated)
└── TabBarView (if authenticated)
    ├── HomeView
    ├── SearchView
    ├── LiveTVsView
    └── WatchlistView
```

### Key Screens
- **Splash Screen**: App initialization and loading
- **Authentication**: Login and registration flow
- **Home**: Content discovery and recommendations
- **Search**: Advanced content search and filtering
- **Live TV**: Real-time television streaming
- **Content Detail**: Movie/series information and playback
- **Video Player**: Full-featured video playback
- **Profile**: User settings and preferences

## 🔒 Security Features

### Authentication
- **Google Sign-In**: OAuth 2.0 authentication
- **Secure Storage**: Keychain integration for sensitive data
- **Token Management**: Automatic token refresh
- **Session Management**: Secure user session handling

### Data Protection
- **HTTPS**: All API communications encrypted
- **Input Validation**: Comprehensive data validation
- **Error Handling**: Secure error responses
- **Privacy Compliance**: GDPR and privacy regulation compliance

## 📊 Analytics & Monitoring

### Firebase Analytics
- **User Engagement**: Screen views and user interactions
- **Content Analytics**: Popular content and viewing patterns
- **Performance Monitoring**: App performance and crash reporting
- **User Acquisition**: Install attribution and user journey tracking

### Custom Analytics
- **Viewing Behavior**: Watch time and content preferences
- **Feature Usage**: App feature adoption rates
- **Revenue Tracking**: Subscription and purchase analytics
- **Error Tracking**: App error and crash monitoring

## 💰 Monetization

### Revenue Streams
- **Subscription Plans**: Basic, Premium, and Enterprise tiers
- **In-App Purchases**: Individual content purchases
- **Advertisements**: Banner and interstitial ads
- **Premium Features**: Exclusive content access

### RevenueCat Integration
- **Subscription Management**: Automated billing and renewal
- **Purchase Validation**: Secure receipt validation
- **Analytics**: Revenue and subscription analytics
- **Cross-Platform**: Unified subscription across platforms

## 🌍 Internationalization

### Supported Languages
- **English** (en)
- **Arabic** (ar) - RTL support
- **Spanish** (es)
- **French** (fr)
- **German** (de)
- **Italian** (it)
- **Portuguese** (pt-PT)
- **Russian** (ru)
- **Chinese** (zh-Hans)
- **Japanese** (ja)
- **Korean** (ko)
- **Hindi** (hi)
- **Turkish** (tr)
- **Dutch** (nl)
- **Swedish** (sv)
- **Norwegian** (nb)
- **Danish** (da)
- **Greek** (el)
- **Indonesian** (id)
- **Thai** (th)
- **Vietnamese** (vi)

### Localization Features
- **Dynamic Language Switching**: Runtime language changes
- **RTL Support**: Right-to-left layout for Arabic
- **Cultural Adaptation**: Region-specific content and formatting
- **Localized Content**: Region-specific movies and TV shows

## 📱 Device Support

### iPhone Support
- **iPhone 8 and newer**: Full feature support
- **iOS 16.1+**: Minimum iOS version
- **Portrait Orientation**: Primary orientation
- **Touch Interface**: Optimized touch interactions

### iPad Support
- **iPad Air and newer**: Full feature support
- **Universal App**: Single app for iPhone and iPad
- **Adaptive Layout**: Responsive design for different screen sizes
- **Full Screen**: Optimized for iPad's larger display
- **Multi-orientation**: Portrait and landscape support

## 🚀 Performance Optimization

### App Performance
- **Lazy Loading**: Efficient content loading
- **Image Caching**: Kingfisher for optimized image loading
- **Memory Management**: Efficient memory usage
- **Background Processing**: Optimized background tasks

### Video Performance
- **VLC Integration**: Professional video playback
- **Adaptive Streaming**: Quality adjustment based on connection
- **Offline Playback**: Downloaded content viewing
- **Background Audio**: Audio playback in background

## 🧪 Testing

### Testing Strategy
- **Unit Tests**: Core functionality testing
- **UI Tests**: User interface testing
- **Integration Tests**: API and service testing
- **Performance Tests**: App performance validation

### Test Coverage
- **View Models**: Business logic testing
- **API Integration**: Network layer testing
- **User Flows**: End-to-end user journey testing
- **Device Compatibility**: Multi-device testing

## 📦 Deployment

### App Store Deployment
1. **Code Signing**: Configure certificates and provisioning profiles
2. **Build Configuration**: Set up release build settings
3. **App Store Connect**: Configure app metadata and screenshots
4. **Submission**: Upload and submit for review

### Release Management
- **Version Control**: Semantic versioning
- **Release Notes**: Comprehensive change documentation
- **Rollback Strategy**: Emergency rollback procedures
- **Monitoring**: Post-release performance monitoring

## 🔧 Development Guidelines

### Code Standards
- **Swift Style Guide**: Follow Apple's Swift API Design Guidelines
- **Documentation**: Comprehensive code documentation
- **Error Handling**: Robust error handling throughout
- **Memory Management**: Proper memory management practices

### Git Workflow
- **Feature Branches**: Develop features in separate branches
- **Pull Requests**: Code review process
- **Commit Messages**: Clear and descriptive commit messages
- **Version Tags**: Tagged releases for version management

## 🐛 Troubleshooting

### Common Issues

**Build Errors**
```bash
# Clean build folder
Product → Clean Build Folder

# Reset derived data
Window → Projects → Clean
```

**Pod Installation Issues**
```bash
# Update CocoaPods
sudo gem install cocoapods

# Clean and reinstall
pod deintegrate
pod install
```

**Simulator Issues**
```bash
# Reset simulator
Device → Erase All Content and Settings

# Clean simulator
xcrun simctl erase all
```

### Debug Mode
```swift
// Enable debug logging
#if DEBUG
print("Debug: \(message)")
#endif
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes following the coding standards
4. Add tests for new functionality
5. Commit changes: `git commit -am 'Add feature'`
6. Push to branch: `git push origin feature-name`
7. Submit a pull request with detailed description

### Development Guidelines
- Follow existing code structure and patterns
- Add appropriate error handling
- Include tests for new features
- Update documentation for API changes
- Ensure backward compatibility

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Check the troubleshooting section above
- Review the code documentation
- Test with the provided sample content
- Contact the development team

---

**Ready to stream with VUGA!** 🎬📱

*This iOS app provides a comprehensive streaming experience with modern architecture, extensive feature set, and cross-device compatibility for iPhone and iPad users worldwide.*

