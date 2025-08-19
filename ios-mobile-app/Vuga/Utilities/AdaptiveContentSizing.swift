//
//  AdaptiveContentSizing.swift
//  Vuga
//
//  Adaptive sizing utility for featured content on different screen sizes
//

import SwiftUI
import UIKit

struct AdaptiveContentSizing {
    
    // MARK: - Device Detection
    static var isIPad: Bool {
        UIDevice.current.userInterfaceIdiom == .pad
    }
    
    static var screenWidth: CGFloat {
        UIScreen.main.bounds.width
    }
    
    static var screenHeight: CGFloat {
        UIScreen.main.bounds.height
    }
    
    // MARK: - Featured Content Sizing Algorithm
    
    /// Calculate optimal featured content size based on device and orientation
    static func featuredContentSize(for geometry: GeometryProxy? = nil) -> (width: CGFloat, height: CGFloat) {
        let screenWidth = geometry?.size.width ?? self.screenWidth
        let screenHeight = geometry?.size.height ?? self.screenHeight
        
        if isIPad {
            return iPadFeaturedContentSize(screenWidth: screenWidth, screenHeight: screenHeight)
        } else {
            return iPhoneFeaturedContentSize(screenWidth: screenWidth)
        }
    }
    
    /// Calculate featured content height for the given width
    static func featuredContentHeight(for width: CGFloat? = nil) -> CGFloat {
        let size = featuredContentSize()
        return size.height
    }
    
    // MARK: - iPhone Sizing
    
    private static func iPhoneFeaturedContentSize(screenWidth: CGFloat) -> (width: CGFloat, height: CGFloat) {
        // iPhone: Use 95% of screen width with proper poster aspect ratio (2:3)
        let contentWidth = screenWidth * 0.95
        let contentHeight = contentWidth * 1.5 // Standard movie poster aspect ratio (2:3)
        
        return (width: contentWidth, height: contentHeight)
    }
    
    // MARK: - iPad Sizing Algorithm
    
    private static func iPadFeaturedContentSize(screenWidth: CGFloat, screenHeight: CGFloat) -> (width: CGFloat, height: CGFloat) {
        let isLandscape = screenWidth > screenHeight
        
        // Standard movie poster aspect ratio (2:3)
        let posterAspectRatio: CGFloat = 1.5
        
        // Calculate dimensions based on available screen space
        // We want to show one full poster at a time, centered
        var contentHeight: CGFloat
        var contentWidth: CGFloat
        
        if isLandscape {
            // In landscape, height is the limiting factor
            // Use 80% of screen height to leave room for controls
            contentHeight = screenHeight * 0.8
            contentWidth = contentHeight / posterAspectRatio
            
            // Make sure width doesn't exceed reasonable limits
            let maxWidth = screenWidth * 0.4
            if contentWidth > maxWidth {
                contentWidth = maxWidth
                contentHeight = contentWidth * posterAspectRatio
            }
        } else {
            // In portrait, width is usually the limiting factor
            // Use appropriate percentage based on device size
            let deviceSizeClass = getIPadSizeClass(screenWidth: screenWidth)
            
            switch deviceSizeClass {
            case .iPadMini:
                contentWidth = min(screenWidth * 0.65, 400)
            case .iPadStandard:
                contentWidth = min(screenWidth * 0.60, 450)
            case .iPadAir:
                contentWidth = min(screenWidth * 0.55, 500)
            case .iPadPro11:
                contentWidth = min(screenWidth * 0.50, 550)
            case .iPadPro13:
                contentWidth = min(screenWidth * 0.45, 600)
            }
            
            // Calculate height based on poster aspect ratio
            contentHeight = contentWidth * posterAspectRatio
            
            // Make sure height doesn't exceed screen bounds
            let maxHeight = screenHeight * 0.7
            if contentHeight > maxHeight {
                contentHeight = maxHeight
                contentWidth = contentHeight / posterAspectRatio
            }
        }
        
        return (width: contentWidth, height: contentHeight)
    }
    
    // MARK: - iPad Size Classification
    
    private enum IPadSizeClass {
        case iPadMini     // ~768pt width
        case iPadStandard // ~810-820pt width
        case iPadAir      // ~820-834pt width
        case iPadPro11    // ~834pt width
        case iPadPro13    // ~1024pt width
    }
    
    private static func getIPadSizeClass(screenWidth: CGFloat) -> IPadSizeClass {
        // Classification based on portrait width (or landscape height)
        let referenceWidth = min(screenWidth, self.screenHeight)
        
        switch referenceWidth {
        case ..<780:
            return .iPadMini
        case 780..<815:
            return .iPadStandard
        case 815..<835:
            return .iPadAir
        case 835..<900:
            return .iPadPro11
        default:
            return .iPadPro13
        }
    }
    
    // MARK: - Additional Sizing Utilities
    
    /// Calculate optimal number of columns for grid layouts
    static func gridColumns(for screenWidth: CGFloat, itemMinWidth: CGFloat = 150) -> Int {
        if isIPad {
            let availableWidth = screenWidth - 40 // Account for padding
            let columns = Int(availableWidth / itemMinWidth)
            
            // iPad specific limits
            switch getIPadSizeClass(screenWidth: screenWidth) {
            case .iPadMini:
                return min(max(columns, 3), 5)
            case .iPadStandard, .iPadAir:
                return min(max(columns, 4), 6)
            case .iPadPro11:
                return min(max(columns, 4), 7)
            case .iPadPro13:
                return min(max(columns, 5), 8)
            }
        } else {
            // iPhone
            let availableWidth = screenWidth - 32
            return max(Int(availableWidth / itemMinWidth), 2)
        }
    }
    
    /// Calculate content card size for horizontal scrolling sections
    static func contentCardSize(for screenWidth: CGFloat) -> CGSize {
        if isIPad {
            let sizeClass = getIPadSizeClass(screenWidth: screenWidth)
            switch sizeClass {
            case .iPadMini:
                return CGSize(width: 140, height: 210)
            case .iPadStandard:
                return CGSize(width: 150, height: 225)
            case .iPadAir, .iPadPro11:
                return CGSize(width: 160, height: 240)
            case .iPadPro13:
                return CGSize(width: 180, height: 270)
            }
        } else {
            // iPhone sizing
            let baseWidth: CGFloat = screenWidth < 380 ? 110 : 120
            return CGSize(width: baseWidth, height: baseWidth * 1.5)
        }
    }
    
    /// Get padding values adjusted for device
    static func contentPadding() -> EdgeInsets {
        if isIPad {
            return EdgeInsets(top: 20, leading: 24, bottom: 20, trailing: 24)
        } else {
            return EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16)
        }
    }
    
    /// Get optimal font sizes for featured content
    static func featuredContentFontSizes() -> (title: CGFloat, subtitle: CGFloat, body: CGFloat) {
        if isIPad {
            let sizeClass = getIPadSizeClass(screenWidth: screenWidth)
            switch sizeClass {
            case .iPadMini:
                return (title: 28, subtitle: 18, body: 16)
            case .iPadStandard:
                return (title: 32, subtitle: 20, body: 17)
            case .iPadAir, .iPadPro11:
                return (title: 36, subtitle: 22, body: 18)
            case .iPadPro13:
                return (title: 42, subtitle: 24, body: 20)
            }
        } else {
            // iPhone
            return (title: 24, subtitle: 16, body: 14)
        }
    }
}