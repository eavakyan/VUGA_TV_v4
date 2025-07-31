#!/bin/bash

echo "Testing iOS build..."
cd /Users/gene/Documents/dev/VUGA_TV_v4/ios-mobile-app

# Clean build folder
xcodebuild clean -scheme Vuga -destination 'generic/platform=iOS' 2>&1 | grep -E "(error:|warning:|Error:|Warning:)" | head -20

# Try to build
echo "Building..."
xcodebuild -scheme Vuga -destination 'generic/platform=iOS' build 2>&1 | grep -E "(error:|Error:)" | head -20