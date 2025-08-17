# TestFlight Deployment Guide for VUGA TV

## Current App Status
- **App Version**: 1.2
- **Build Number**: 2
- **Bundle ID**: Check in Xcode (likely com.vuga.tv or similar)

## Pre-Deployment Checklist

### 1. Update Version and Build Number
```bash
# Increment build number (required for each TestFlight upload)
cd /Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app
xcrun agvtool next-version -all

# Or manually in Xcode:
# - Select project → TARGETS → Vuga
# - General tab → Identity section
# - Update Version (e.g., 1.2.1) and Build (e.g., 3)
```

### 2. Clean and Prepare

#### Remove Development Files
```bash
# Remove any test/development specific files
rm -rf Vuga/Assets/Managers/ConnectionMonitor.swift  # If not added to project
rm -rf Vuga/Views/NetworkStatusView.swift  # If not added to project
rm -rf Vuga/Views/VideoNetworkAlert.swift  # If not added to project
```

#### Clean Build Folder
In Xcode:
- Product → Clean Build Folder (⇧⌘K)

### 3. Configure Signing & Capabilities

#### In Xcode:
1. Select project → TARGETS → Vuga
2. Signing & Capabilities tab
3. Ensure:
   - ✅ Automatically manage signing is checked
   - Team: Your Apple Developer Team
   - Bundle Identifier: Verify it's correct
   - Provisioning Profile: Should say "Xcode Managed Profile"

### 4. Set Build Configuration

#### Switch to Release Mode:
1. Product → Scheme → Edit Scheme (⌘<)
2. Run → Build Configuration → Release
3. Archive → Build Configuration → Release

### 5. Required App Store Connect Setup

#### Before First Upload:
1. Log in to [App Store Connect](https://appstoreconnect.apple.com)
2. My Apps → "+" → New App
3. Fill in:
   - Platform: iOS
   - App Name: VUGA TV
   - Primary Language: English
   - Bundle ID: Select from dropdown
   - SKU: vuga-tv-2024 (or similar unique ID)

### 6. Archive and Upload

#### In Xcode:
1. Select Generic iOS Device or "Any iOS Device" as build target
2. Product → Archive (⌘B then ⌘⌥⇧K)
3. Wait for archive to complete
4. Organizer window opens automatically

#### In Organizer:
1. Select your archive
2. Click "Distribute App"
3. Select "App Store Connect"
4. Select "Upload"
5. Options:
   - ✅ Upload your app's symbols
   - ✅ Manage Version and Build Number (optional)
6. Review and Upload

### 7. TestFlight Configuration

#### In App Store Connect:
1. My Apps → VUGA TV
2. TestFlight tab
3. iOS builds will appear (may take 5-30 minutes)

#### Add Test Information:
1. Test Information → Add:
   - What to Test: "New features and bug fixes in version 1.2"
   - App Description: Brief description
   - Email: Your contact email
   - Privacy Policy URL: Your privacy policy
   - License Agreement: Standard or custom

#### Export Compliance:
1. Provide Export Compliance Information
2. Usually select "No" if app doesn't use encryption
3. If using HTTPS only, select appropriate option

### 8. Add Testers

#### Internal Testing (up to 100 testers):
1. TestFlight → Internal Testing
2. Create New Group or use existing
3. Add testers by email (must be App Store Connect users)

#### External Testing (up to 10,000 testers):
1. TestFlight → External Testing
2. Create New Group
3. Add testers by email
4. Submit for Beta App Review (first time only)

### 9. Common Issues and Solutions

#### Issue: Archive option grayed out
- Solution: Select Generic iOS Device as target

#### Issue: "No suitable application records were found"
- Solution: Create app in App Store Connect first

#### Issue: Build not appearing in TestFlight
- Wait 5-30 minutes
- Check email for processing issues
- Verify upload completed successfully

#### Issue: Missing compliance
- Add export compliance in TestFlight
- Add ITSAppUsesNonExemptEncryption key to Info.plist

### 10. Post-Upload Tasks

1. **Monitor Processing**:
   - Check email for any issues
   - Watch App Store Connect for build status

2. **Test on TestFlight**:
   - Download TestFlight app on your device
   - Accept invitation
   - Install and test the build

3. **Gather Feedback**:
   - Monitor crash reports in App Store Connect
   - Review tester feedback
   - Check TestFlight analytics

## Build Script (Optional Automation)

Create `deploy_testflight.sh`:
```bash
#!/bin/bash

# Configuration
SCHEME="Vuga"
CONFIGURATION="Release"
ARCHIVE_PATH="./build/Vuga.xcarchive"
EXPORT_PATH="./build"

# Clean
echo "Cleaning..."
xcodebuild clean -workspace Vuga.xcworkspace -scheme "$SCHEME" -configuration "$CONFIGURATION"

# Archive
echo "Archiving..."
xcodebuild archive \
    -workspace Vuga.xcworkspace \
    -scheme "$SCHEME" \
    -configuration "$CONFIGURATION" \
    -archivePath "$ARCHIVE_PATH" \
    -destination "generic/platform=iOS"

# Export
echo "Exporting..."
xcodebuild -exportArchive \
    -archivePath "$ARCHIVE_PATH" \
    -exportPath "$EXPORT_PATH" \
    -exportOptionsPlist ExportOptions.plist

# Upload to TestFlight
echo "Uploading to TestFlight..."
xcrun altool --upload-app \
    -f "$EXPORT_PATH/Vuga.ipa" \
    -t ios \
    -u YOUR_APPLE_ID \
    -p YOUR_APP_SPECIFIC_PASSWORD
```

## Important Notes

1. **Build Number**: Must increment for each upload
2. **Version Number**: Update for significant releases
3. **Processing Time**: Builds take 5-30 minutes to process
4. **Beta Review**: First external test requires review (24-48 hours)
5. **Certificates**: Ensure certificates are valid (check in Keychain)

## Next Steps After TestFlight

1. Collect feedback from testers
2. Fix any reported issues
3. Update build and re-upload
4. Prepare for App Store submission
5. Create App Store listing (screenshots, description, etc.)

---

Last Updated: 2025-01-15
Current Status: Ready for TestFlight deployment