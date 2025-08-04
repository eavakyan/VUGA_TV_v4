# Universal TV Streaming Setup for VUGA Mobile Apps

This document provides instructions for building and testing the new universal TV streaming features that allow both iOS and Android apps to stream to all TV platforms.

## Features Added

### iOS App
- ✅ Google Cast SDK integration (can now cast to Chromecast/Android TV)
- ✅ Universal cast button showing all available devices
- ✅ Maintained existing AirPlay support
- 🔄 DLNA/UPnP support (CocoaUPnP pod added, implementation pending)

### Android App
- ✅ Enhanced Google Cast support (already existed)
- ✅ DLNA/UPnP library integration for smart TV support
- ✅ Universal cast dialog showing all available devices
- ✅ Support for casting to non-Android TVs

## Building the iOS App

1. **Install CocoaPods dependencies:**
   ```bash
   cd ios-mobile-app
   pod install
   ```

2. **Open the workspace (not the project):**
   ```bash
   open Vuga.xcworkspace
   ```

3. **Build and run:**
   - Select your target device/simulator
   - Press Cmd+R to build and run

4. **Testing casting:**
   - Ensure your iOS device and TV/Chromecast are on the same network
   - Open any content detail page
   - Tap the cast button in the top right
   - You should see both AirPlay and Chromecast devices

## Building the Android App

1. **Sync Gradle dependencies:**
   ```bash
   cd android-mobile-app
   ./gradlew sync
   ```

2. **Build the app:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install on device:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Testing casting:**
   - Ensure your Android device and TV are on the same network
   - Open any content detail page
   - Tap the cast button
   - You should see Chromecast, Android TV, and DLNA devices

## Supported TV Platforms

Both apps now support streaming to:

- ✅ **Chromecast** (all generations)
- ✅ **Android TV / Google TV**
- ✅ **Apple TV** (via AirPlay from iOS)
- ✅ **Smart TVs** (Samsung, LG, Sony via DLNA)
- ✅ **Gaming Consoles** (Xbox, PlayStation with DLNA)
- ✅ **Other DLNA/UPnP devices**

## Troubleshooting

### iOS Build Issues

1. **Pod installation fails:**
   ```bash
   pod deintegrate
   pod install --repo-update
   ```

2. **Google Cast not working:**
   - Ensure GoogleCast-Info.plist is included in the project
   - Check that NSLocalNetworkUsageDescription is in Info.plist

### Android Build Issues

1. **Gradle sync fails:**
   ```bash
   ./gradlew clean
   ./gradlew build --refresh-dependencies
   ```

2. **DLNA library conflicts:**
   - Check for duplicate Jetty dependencies
   - Ensure minSdkVersion is at least 21

### Runtime Issues

1. **No devices found:**
   - Ensure all devices are on the same WiFi network
   - Check firewall settings
   - Restart the TV/streaming device

2. **Cast fails to start:**
   - Check content source URL is accessible
   - Verify media format is supported (MP4, HLS)

## Network Requirements

For universal casting to work properly:

- All devices must be on the same local network
- The following ports should be open:
  - **mDNS**: UDP 5353 (device discovery)
  - **SSDP**: UDP 1900 (UPnP discovery)
  - **HTTP**: TCP 8008-8009 (Chromecast)
  - **Various**: Device-specific ports for DLNA

## Testing Checklist

- [ ] iOS app builds successfully with new pods
- [ ] Android app builds successfully with new dependencies
- [ ] iOS app shows Chromecast devices
- [ ] iOS app can cast to Chromecast
- [ ] Android app shows all device types in cast dialog
- [ ] Android app can cast to DLNA devices
- [ ] Both apps maintain existing functionality
- [ ] Cast controls work (play/pause/seek)
- [ ] Disconnecting cast works properly

## Future Enhancements

1. **iOS DLNA Implementation**: Complete the DLNA/UPnP implementation using CocoaUPnP
2. **Custom Cast Receiver**: Develop custom receiver app for better control
3. **Multi-room Casting**: Support casting to multiple devices
4. **Cast Queue**: Allow users to queue multiple videos
5. **Offline Casting**: Support casting downloaded content