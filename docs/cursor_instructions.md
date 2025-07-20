# TV Streaming Platform Multi-Platform Development Project

## 🎬 **Project Overview**

You are working on a **multi-platform TV streaming application** with four independent applications that all communicate through a shared Node.js API backend. This is a **monorepo architecture** where each platform has its own directory but shares common business logic through the API. If these instructions conflict with what you see in the starting codebase, assume that the codebase is correct and working and that you need to start with it as it now stands. Always ask if you are unsure about conflicting instructions or situations.

## 📁 **Project Structure**

```
tv-streaming-platform/
├── ios-mobile-app/           # iOS mobile application (Swift/SwiftUI)
├── android-mobile-app/       # Android mobile application (Kotlin/Jetpack Compose)
├── android-tv-app/          # Android TV application (Kotlin/Leanback)
├── api-backend/             # Node.js API backend (Express/MongoDB)
├── shared/                  # Shared types, assets, documentation
├── docs/                    # Project documentation
├── scripts/                 # Build and deployment scripts
├── docker-compose.yml       # Local development orchestration
├── .github/                 # CI/CD workflows
├── .gitignore              # Root gitignore
└── README.md               # Project overview
```

## 🏗️ **Architecture Principles**

### **API-First Design**
- **Single source of truth**: All business logic resides in the API backend
- **Platform-agnostic**: API serves all three client applications
- **Consistent data model**: Same user accounts, content, orders across all platforms
- **RESTful endpoints**: Standard HTTP APIs for all client interactions

### **Platform Independence**
- **Separate codebases**: Each platform has its own directory and build system
- **Platform-specific UI**: Each app follows its platform's design guidelines
- **Independent deployment**: Apps can be updated separately
- **Technology flexibility**: Can rewrite one platform without affecting others

## 📱 **Platform-Specific Details**

### **iOS Mobile App** (`ios-mobile-app/`)
- **Technology**: Swift, SwiftUI, Combine
- **UI Framework**: SwiftUI with MVVM pattern
- **Key Features**: Touch-optimized, Face ID, Apple Pay, iOS design guidelines
- **Navigation**: UINavigationController, SwiftUI NavigationView
- **Deployment**: App Store, TestFlight

### **Android Mobile App** (`android-mobile-app/`)
- **Technology**: Kotlin, Jetpack Compose, ViewModel
- **UI Framework**: Jetpack Compose with Material Design
- **Key Features**: Touch-optimized, Google Pay, fingerprint auth, Material Design
- **Navigation**: Navigation Component, Jetpack Compose navigation
- **Deployment**: Google Play Store

### **Android TV App** (`android-tv-app/`)
- **Technology**: Kotlin, Leanback library, Android TV Extensions
- **UI Framework**: Leanback with focus-based navigation
- **Key Features**: Remote control navigation, 10-foot UI, voice search
- **Navigation**: D-pad focus management, TV-safe areas
- **Deployment**: Google Play (TV), side-loading

### **API Backend** (`api-backend/`)
- **Technology**: Node.js, Express, MongoDB, Mongoose
- **Architecture**: RESTful API with middleware
- **Database**: MongoDB with Mongoose ODM
- **Security**: JWT authentication, rate limiting, CORS
- **Deployment**: DigitalOcean App Platform

## 🔌 **API Endpoints Structure**

The API backend provides these core endpoints that all platforms consume:

### **Users API** (`/api/users`)
- `GET /api/users` - Retrieve all users with filtering
- `GET /api/users/analytics` - User statistics and demographics

### **Products API** (`/api/products`)
- `GET /api/products` - Content catalog with filtering (type, genre, subscription tier)
- `GET /api/products/trending` - Trending content with statistics

### **Orders API** (`/api/orders`)
- `GET /api/orders` - Order management with filtering
- `GET /api/orders/revenue` - Revenue analytics and financial metrics

### **Health Check**
- `GET /health` - Server health status
- `GET /` - API welcome and endpoint documentation

## 🎨 **Platform-Specific UI Considerations**

### **iOS Mobile**
- **Touch gestures**: Swipe, tap, long press
- **iOS design**: Human Interface Guidelines
- **Navigation**: Tab bars, navigation stacks
- **Features**: Face ID, Apple Pay, Push notifications

### **Android Mobile**
- **Touch gestures**: Material Design touch targets
- **Android design**: Material Design 3 guidelines
- **Navigation**: Bottom navigation, navigation drawer
- **Features**: Google Pay, fingerprint, FCM notifications

### **Android TV**
- **Remote control**: D-pad navigation, focus management
- **TV design**: 10-foot interface, large text, TV-safe areas
- **Navigation**: Leanback browse fragments, focus-based UI
- **Features**: Voice search, remote control input

## 🛠️ **Development Workflow**

### **Cross-Platform Feature Development**
1. **API-First**: Always start with API endpoints
2. **Platform Implementation**: Implement in each platform following their conventions
3. **Consistency Check**: Ensure features work similarly across platforms
4. **Testing**: Test API integration in each platform

### **Platform-Specific Development**
- **iOS**: Focus on SwiftUI patterns, iOS-specific features
- **Android**: Focus on Jetpack Compose, Material Design
- **Android TV**: Focus on Leanback, focus management, TV UX
- **API**: Focus on RESTful design, database optimization

## 🛠️ **Technology Stack**

| Platform | Languages | Frameworks | IDE |
|----------|-----------|------------|-----|
| iOS Mobile | Swift | SwiftUI, UIKit | Xcode |
| Android Mobile | Kotlin | Jetpack Compose, Views | Android Studio |
| Android TV | Kotlin | Leanback, TV Extensions | Android Studio |
| API Backend | JavaScript | Node.js, Express | VSCode |

## 📊 **Data Models**

### **User Model**
```javascript
{
  username: String (required, unique),
  email: String (required, unique),
  firstName: String (required),
  lastName: String (required),
  age: Number (13-120),
  subscription: Enum ['basic', 'premium', 'enterprise'],
  watchHistory: Array of viewing records,
  preferences: {
    genres: Array of strings,
    language: String,
    quality: Enum ['720p', '1080p', '4K']
  },
  isActive: Boolean,
  timestamps: true
}
```

### **Product Model**
```javascript
{
  title: String (required),
  description: String (required, max 1000 chars),
  type: Enum ['movie', 'series', 'documentary', 'special'],
  genre: Array of strings,
  releaseYear: Number (1900 - current year + 5),
  duration: Number (minutes),
  rating: {
    imdb: Number (0-10),
    userRating: Number (0-5),
    totalRatings: Number
  },
  cast: Array of {name, role},
  director: String (required),
  language: String,
  subtitles: Array of language codes,
  quality: Array of ['720p', '1080p', '4K'],
  subscriptionTier: Enum ['basic', 'premium', 'enterprise'],
  viewCount: Number,
  timestamps: true
}
```

### **Order Model**
```javascript
{
  userId: String (required),
  orderNumber: String (required, unique),
  type: Enum ['subscription', 'rental', 'purchase'],
  items: Array of product items,
  subscriptionDetails: Object with plan details,
  pricing: {
    subtotal: Number,
    tax: Number,
    discount: Number,
    total: Number,
    currency: String
  },
  payment: {
    method: Enum ['credit_card', 'debit_card', 'paypal', 'apple_pay', 'google_pay'],
    status: Enum ['pending', 'completed', 'failed', 'refunded']
  },
  status: Enum ['pending', 'confirmed', 'active', 'expired', 'cancelled'],
  billingAddress: Object,
  metadata: Object with tracking info,
  timestamps: true
}
```

## 🚀 **Deployment Strategy**

### **API Backend**
- **Platform**: Hostinger php hosting via ftp - manually updated by admin
- **Database**: MySQL on Hostinger
- **Environment**: Production with environment variables


### **Mobile Apps**
- **iOS**: App Store, TestFlight for beta testing
- **Android Mobile**: Google Play Store, Firebase App Distribution
- **Android TV**: Google Play (TV), side-loading for development

## 📋 **Development Guidelines**

### **When Working on Specific Platforms**
- **iOS Commands**: "Add login screen to iOS app", "Update iOS video player"
- **Android Commands**: "Implement Material Design in Android app", "Add Google Pay to Android"
- **Android TV Commands**: "Design TV home screen", "Add remote navigation"
- **API Commands**: "Add new endpoint", "Update authentication", "Fix database query"

### **When Working Across Platforms**
- **Cross-Platform**: "Add feature to all apps", "Sync data across platforms"
- **API Integration**: "Update all apps to use new API", "Fix authentication across platforms"

### **Best Practices**
1. **API-First**: Always design API endpoints before implementing in clients
2. **Platform Conventions**: Follow each platform's design and coding guidelines
3. **Consistency**: Ensure features work similarly across all platforms
4. **Testing**: Test API integration in each platform
5. **Documentation**: Keep API documentation updated for all platforms

## 🎯 **Current Status**

The project is in **initial setup phase** with:
- ✅ **API Backend**: Node.js API with MongoDB, complete with sample data
- ✅ **Project Structure**: Multi-platform directory structure defined
- ✅ **Deployment**: DigitalOcean configuration ready
- 🔄 **Next Steps**: Implement platform-specific applications

## 💡 **Key Success Factors**

1. **Maintain API as single source of truth**
2. **Follow platform-specific best practices**
3. **Ensure consistent user experience across platforms**
4. **Implement proper error handling and offline support**
5. **Maintain security and authentication across all platforms**
6. **Optimize for each platform's unique capabilities**

## 🔧 **Command Examples for Development**

### **Platform-Specific Commands**
```
"Add a login screen to the iOS app with biometric authentication"
"Update the iOS video player to support 4K quality selection"
"Fix the iOS navigation bar styling in the home screen"
"Add pull-to-refresh functionality to the iOS content list"

"Implement Material Design bottom navigation in the Android app"
"Add Google Pay integration to the Android checkout flow"
"Create a settings screen for the Android app with dark mode toggle"
"Fix the Android video playback issues on older devices"

"Design the Android TV home screen with focus management"
"Add voice search functionality to the Android TV app"
"Implement remote control navigation for the content browser"
"Create TV-optimized video controls with D-pad support"

"Add a new endpoint for user watchlist management"
"Implement JWT authentication in the API backend"
"Add video streaming URLs to the products API response"
"Create analytics endpoints for tracking user behavior"
```

### **Cross-Platform Commands**
```
"Add 'Continue Watching' feature across all apps and API"
"Implement user profiles that sync between iOS, Android, and TV"
"Add push notifications for new content releases to all platforms"
"Create a unified search experience across mobile and TV apps"
```

## 📁 **Individual Directory Structures**

### **iOS Mobile App Structure**
analyze existing code in /ios-mobile-app

### **Android Mobile App Structure**
analyze existing code in /android-mobile-app

### **Android TV App Structure**
analyze existing code in /android-tv-app

### **API Backend Structure**
analyze api endpoints in /api-backend

### **Shared Directory Structure**
analyze existing structure in /shared

# ******** STOPPED HERE - ALSO CHECK THE JSON EXAMPLES ABOVE, THEY ARE SUSPECT *********

### **Example 2: Cross-Platform Feature**
**Command**: *"Add user ratings system across all platforms"*

**Expected Response**:
1. **API Backend**: Add rating endpoints
2. **iOS App**: SwiftUI rating component  
3. **Android App**: Jetpack Compose rating UI
4. **Android TV**: TV-optimized rating interface

### **Example 3: API-First Feature**
**Command**: *"Add content recommendations API that all apps can use"*

**Expected Response**:
```javascript
// api-backend/routes/recommendations.js
router.get('/api/users/:id/recommendations', async (req, res) => {
    const recommendations = await getPersonalizedContent(req.params.id);
    res.json({
        mobile: formatForMobile(recommendations),
        tv: formatForTV(recommendations)
    });
});
```

## 🎯 **Navigation Between Platforms**

When working on this project, you should seamlessly switch between directories:

```bash
# Clear navigation between platforms
cd ios-mobile-app/         # Work on iOS features
cd android-mobile-app/     # Work on Android features  
cd android-tv-app/         # Work on TV interface
cd api-backend/            # Work on backend logic
cd shared/                 # Update shared types
```

## 💡 **Platform-Specific Considerations**

### **iOS Development**
- Use SwiftUI for modern UI development
- Follow Human Interface Guidelines
- Implement MVVM pattern with Combine
- Use iOS-specific features (Face ID, Apple Pay)
- Handle different device sizes and orientations

### **Android Development**
- Use Jetpack Compose for modern UI
- Follow Material Design guidelines
- Implement MVVM with ViewModel and LiveData
- Use Android-specific features (Google Pay, fingerprint)
- Handle different screen densities and configurations

### **Android TV Development**
- Use Leanback library for TV UI
- Implement focus-based navigation
- Design for 10-foot interface
- Handle remote control input
- Optimize for TV viewing distance

### **API Development**
- Follow RESTful principles
- Implement proper error handling
- Use middleware for authentication and validation
- Optimize database queries
- Implement rate limiting and security

## 🔄 **Cross-Platform Coordination**

### **Shared API Contract**
All platforms consume the same API endpoints but may receive platform-specific data formatting:

```javascript
// API handles platform differences
app.post('/api/users/login', (req, res) => {
  const { platform } = req.headers; // 'ios', 'android', 'android-tv'
  
  switch(platform) {
    case 'ios':
      // Apple-specific auth handling
      break;
    case 'android':
    case 'android-tv':
      // Google-specific auth handling
      break;
  }
});
```

### **Feature Synchronization**
- **Watchlist**: Syncs between mobile and TV apps
- **User preferences**: Consistent across all platforms
- **Viewing progress**: Continues across devices
- **Authentication**: Single sign-on across platforms

## 📊 **Monitoring and Analytics**

### **API Monitoring**
- Health check endpoints for each service
- Performance metrics and response times
- Error tracking and logging
- User behavior analytics

### **Platform-Specific Analytics**
- **iOS**: App Store Connect analytics
- **Android**: Google Play Console analytics
- **Android TV**: TV-specific usage metrics
- **Cross-platform**: Unified analytics dashboard

## 🛡️ **Security Considerations**

### **API Security**
- JWT authentication
- Rate limiting
- Input validation
- CORS configuration
- HTTPS enforcement

### **Platform Security**
- **iOS**: Keychain storage, Face ID integration
- **Android**: Keystore, fingerprint integration
- **Android TV**: Secure storage for credentials
- **Cross-platform**: Secure token management

## 🚀 **Deployment and CI/CD**

### **API Deployment**
- DigitalOcean App Platform
- Environment-specific configurations
- Database migrations
- Health check monitoring

### **Mobile App Deployment**
- **iOS**: App Store Connect, TestFlight
- **Android**: Google Play Console, Firebase App Distribution
- **Android TV**: Google Play (TV), side-loading

### **CI/CD Pipeline**
- Automated testing across platforms
- Build and deployment automation
- Environment management
- Rollback capabilities

---

**This project represents a modern, scalable approach to multi-platform development with a shared backend API serving iOS mobile, Android mobile, and Android TV applications for a TV streaming service. The architecture ensures consistency, maintainability, and platform-specific optimization while providing a unified user experience across all devices.** 