# Android TV Streaming App - Setup Guide

This guide will help you set up and run the complete Android TV streaming application with its Node.js backend.

## Prerequisites

### Backend Requirements
- **Node.js** 18.0.0 or higher
- **npm** 8.0.0 or higher
- **MongoDB** 6.0.0 or higher
- **Git** for version control

### Android Development Requirements
- **Android Studio** Arctic Fox (2020.3.1) or later
- **Android SDK** API level 21 (Android 5.0) or higher
- **Android TV device** or **Android TV emulator**
- **Java Development Kit (JDK)** 17 or higher

## Backend Setup

### 1. Install MongoDB

#### Option A: Local Installation
1. Download MongoDB from [mongodb.com](https://www.mongodb.com/try/download/community)
2. Install MongoDB Community Server
3. Start MongoDB service:
   ```bash
   # On macOS/Linux
   sudo systemctl start mongod
   
   # On Windows
   net start MongoDB
   ```

#### Option B: MongoDB Atlas (Cloud)
1. Create a free account at [mongodb.com/atlas](https://www.mongodb.com/atlas)
2. Create a new cluster
3. Get your connection string

### 2. Set Up Backend

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create environment file:
   ```bash
   cp env.example .env
   ```

4. Configure environment variables in `.env`:
   ```env
   # Server Configuration
   PORT=3000
   NODE_ENV=development
   
   # MongoDB Configuration
   MONGODB_URI=mongodb://localhost:27017/android_tv_streaming
   # Or for MongoDB Atlas:
   # MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/android_tv_streaming
   
   # JWT Configuration
   JWT_SECRET=your-super-secret-jwt-key-here
   JWT_EXPIRES_IN=7d
   
   # CORS Configuration
   CORS_ORIGIN=http://localhost:3000
   ```

5. Seed the database with sample data:
   ```bash
   npm run seed
   ```

6. Start the development server:
   ```bash
   npm run dev
   ```

The backend will be running at `http://localhost:3000`

### 3. Verify Backend Setup

Test the API endpoints:

```bash
# Health check
curl http://localhost:3000/health

# Get featured content
curl http://localhost:3000/api/content/featured

# Get trending content
curl http://localhost:3000/api/content/trending
```

## Android TV App Setup

### 1. Set Up Android Studio

1. Download and install [Android Studio](https://developer.android.com/studio)
2. Open Android Studio and complete the initial setup
3. Install Android TV SDK:
   - Go to **Tools > SDK Manager**
   - Select **SDK Platforms** tab
   - Check **Android TV (API 21)** or higher
   - Click **Apply** and install

### 2. Set Up Android TV Device

#### Option A: Physical Android TV Device
1. Enable Developer Options on your Android TV:
   - Go to **Settings > About**
   - Click **Build** 7 times
   - Go back to **Settings > Developer options**
   - Enable **USB debugging**
   - Enable **Stay awake**

2. Connect your device via USB and authorize debugging

#### Option A: Android TV Emulator
1. In Android Studio, go to **Tools > AVD Manager**
2. Click **Create Virtual Device**
3. Select **TV** category
4. Choose **Android TV (1080p)** or **Android TV (4K)**
5. Select a system image (API 21 or higher)
6. Configure and create the AVD

### 3. Configure the Android App

1. Open the project in Android Studio:
   ```bash
   cd android-tv-app
   ```

2. Update the API base URL in the app:
   - Open `app/src/main/java/com/example/androidtvstreaming/di/NetworkModule.kt`
   - Update the `BASE_URL` to point to your backend:
   ```kotlin
   private const val BASE_URL = "http://YOUR_IP_ADDRESS:3000/api/"
   ```
   
   **Note**: Use your computer's IP address, not localhost, so the Android TV can reach the backend.

3. Sync the project:
   - Click **File > Sync Project with Gradle Files**

### 4. Build and Run

1. Select your Android TV device/emulator from the device dropdown
2. Click the **Run** button (green play icon)
3. The app will build and install on your device

## Testing the Application

### 1. Backend Testing

Test the API endpoints using curl or Postman:

```bash
# Register a new user
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "username": "testuser",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 2. Android TV App Testing

1. Launch the app on your Android TV
2. Navigate through the interface using the remote control or keyboard
3. Test the following features:
   - Browse featured content
   - Search for movies
   - Play videos (if you have sample video files)
   - User authentication

## Development Workflow

### Backend Development

1. Start the development server:
   ```bash
   cd backend
   npm run dev
   ```

2. The server will automatically restart when you make changes

3. View logs in the terminal

### Android App Development

1. Make changes to the Kotlin/Compose code
2. Click **Run** to deploy changes to your device
3. Use Android Studio's debugging tools for troubleshooting

## Troubleshooting

### Backend Issues

1. **MongoDB Connection Error**:
   - Ensure MongoDB is running
   - Check the connection string in `.env`
   - Verify network connectivity

2. **Port Already in Use**:
   - Change the port in `.env` file
   - Kill processes using port 3000

3. **JWT Errors**:
   - Ensure `JWT_SECRET` is set in `.env`
   - Check token expiration settings

### Android TV Issues

1. **App Won't Install**:
   - Check device compatibility
   - Ensure USB debugging is enabled
   - Try uninstalling and reinstalling

2. **Network Connection Issues**:
   - Verify the IP address in `NetworkModule.kt`
   - Check firewall settings
   - Ensure both devices are on the same network

3. **Video Playback Issues**:
   - Check video file format compatibility
   - Verify video URLs are accessible
   - Test with different video qualities

## Production Deployment

### Backend Deployment

1. **Environment Setup**:
   ```bash
   NODE_ENV=production
   MONGODB_URI_PROD=your_production_mongodb_uri
   JWT_SECRET=your_production_jwt_secret
   ```

2. **Deploy to Cloud Platform**:
   - **Heroku**: `git push heroku main`
   - **AWS**: Use Elastic Beanstalk or EC2
   - **Google Cloud**: Use App Engine or Compute Engine

3. **Set up SSL certificate** for HTTPS

### Android App Deployment

1. **Build Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

2. **Sign the APK** with your release key

3. **Upload to Google Play Console**:
   - Create a developer account
   - Upload the signed APK
   - Configure app metadata
   - Submit for review

## Additional Resources

- [Android TV Development Guide](https://developer.android.com/training/tv)
- [Jetpack Compose for TV](https://developer.android.com/jetpack/compose/tv)
- [Node.js Best Practices](https://github.com/goldbergyoni/nodebestpractices)
- [MongoDB Documentation](https://docs.mongodb.com/)

## Support

If you encounter issues:

1. Check the troubleshooting section above
2. Review the logs in both backend and Android Studio
3. Verify all prerequisites are met
4. Ensure network connectivity between devices

For additional help, refer to the project's README.md file or create an issue in the project repository. 