# Xcode Build Fix Instructions

## Issue
The build is failing because `SubscriptionRequiredSheet.swift` is not added to the Xcode project.

## Solution

1. Open Xcode
2. In the Project Navigator (left sidebar), right-click on the "Views" folder
3. Select "Add Files to 'Vuga'..."
4. Navigate to: `/Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app/Vuga/Views/`
5. Select `SubscriptionRequiredSheet.swift`
6. Make sure "Copy items if needed" is unchecked (the file is already in the correct location)
7. Make sure your app target is selected in "Add to targets"
8. Click "Add"

## Alternative Solution

If you prefer not to use the separate SubscriptionRequiredSheet file, the code has been temporarily replaced with a simplified inline version in ContentDetailView.swift. The app should build successfully with this simplified version.

## What Changed

1. Fixed the complex expression error by breaking down the ContentDetailView into smaller computed properties
2. Removed duplicate code that was causing issues
3. Created a simplified subscription required sheet inline

The app should now build successfully once the SubscriptionRequiredSheet.swift file is added to the project, or you can continue using the simplified inline version.