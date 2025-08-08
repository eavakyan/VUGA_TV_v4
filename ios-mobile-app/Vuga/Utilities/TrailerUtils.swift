//
//  TrailerUtils.swift
//  Vuga
//
//  Created by Claude on 08/01/25.
//

import Foundation
import UIKit

class TrailerUtils {
    
    // MARK: - Trailer URL Handling
    
    /// Get the effective trailer URL for a content item
    /// Prioritizes new trailers array, falls back to legacy trailer_url
    static func getEffectiveTrailerUrl(for content: VugaContent) -> String? {
        // Try to get primary trailer from trailers list first
        if let primaryTrailer = content.primaryTrailer,
           !primaryTrailer.effectiveTrailerUrl.isEmpty {
            return primaryTrailer.effectiveTrailerUrl
        }
        
        // Fall back to legacy trailer_url field
        return content.trailerURL
    }
    
    /// Get the effective YouTube ID for a content item
    static func getEffectiveYouTubeId(for content: VugaContent) -> String? {
        // Try to get primary trailer from trailers list first
        if let primaryTrailer = content.primaryTrailer,
           !primaryTrailer.effectiveYoutubeId.isEmpty {
            return primaryTrailer.effectiveYoutubeId
        }
        
        // Fall back to extracting from legacy trailer_url field
        if let trailerUrl = content.trailerURL {
            return extractYouTubeId(from: trailerUrl)
        }
        
        return nil
    }
    
    /// Get trailer thumbnail URL for display in UI
    static func getTrailerThumbnailUrl(for content: VugaContent) -> String? {
        // Try to get from primary trailer
        if let primaryTrailer = content.primaryTrailer,
           !primaryTrailer.effectiveThumbnailUrl.isEmpty {
            return primaryTrailer.effectiveThumbnailUrl
        }
        
        // Generate from YouTube ID if available
        if let youtubeId = getEffectiveYouTubeId(for: content),
           !youtubeId.isEmpty {
            return "https://img.youtube.com/vi/\(youtubeId)/maxresdefault.jpg"
        }
        
        return nil
    }
    
    /// Check if content has any trailers
    static func hasTrailers(for content: VugaContent) -> Bool {
        // Check new trailers list
        if !content.sortedTrailers.isEmpty {
            return true
        }
        
        // Check legacy trailer_url
        if let trailerUrl = content.trailerURL, !trailerUrl.isEmpty {
            return true
        }
        
        return false
    }
    
    // MARK: - YouTube Handling
    
    /// Extract YouTube ID from various URL formats
    static func extractYouTubeId(from url: String) -> String? {
        guard !url.isEmpty else { return nil }
        
        // Standard YouTube URL: https://www.youtube.com/watch?v=VIDEO_ID
        if url.contains("youtube.com/watch?v=") {
            let components = url.components(separatedBy: "v=")
            if components.count > 1 {
                let id = components[1].components(separatedBy: "&")[0] // Remove additional parameters
                if id.count == 11 {
                    return id
                }
            }
        }
        
        // YouTube short URL: https://youtu.be/VIDEO_ID
        if url.contains("youtu.be/") {
            let components = url.components(separatedBy: "youtu.be/")
            if components.count > 1 {
                let id = components[1].components(separatedBy: "?")[0] // Remove parameters
                if id.count == 11 {
                    return id
                }
            }
        }
        
        // YouTube embed URL: https://www.youtube.com/embed/VIDEO_ID
        if url.contains("youtube.com/embed/") {
            let components = url.components(separatedBy: "embed/")
            if components.count > 1 {
                let id = components[1].components(separatedBy: "?")[0] // Remove parameters
                if id.count == 11 {
                    return id
                }
            }
        }
        
        // Check if it's already just the ID
        if url.count == 11 && url.range(of: "^[A-Za-z0-9_-]{11}$", options: .regularExpression) != nil {
            return url
        }
        
        return nil
    }
    
    /// Generate YouTube embed URL from ID
    static func generateEmbedUrl(from youtubeId: String) -> String? {
        guard !youtubeId.isEmpty else { return nil }
        return "https://www.youtube.com/embed/\(youtubeId)"
    }
    
    /// Generate YouTube watch URL from ID
    static func generateWatchUrl(from youtubeId: String) -> String? {
        guard !youtubeId.isEmpty else { return nil }
        return "https://www.youtube.com/watch?v=\(youtubeId)"
    }
    
    // MARK: - Trailer Playback
    
    /// Open trailer in YouTube app or web browser
    static func openTrailer(for content: VugaContent, from viewController: UIViewController) {
        guard let trailerUrl = getEffectiveTrailerUrl(for: content), !trailerUrl.isEmpty else {
            print("No trailer URL available for content: \(content.title ?? "Unknown")")
            return
        }
        
        // Try to open in YouTube app first
        if let youtubeId = getEffectiveYouTubeId(for: content),
           !youtubeId.isEmpty,
           let youtubeUrl = URL(string: "youtube://\(youtubeId)"),
           UIApplication.shared.canOpenURL(youtubeUrl) {
            
            UIApplication.shared.open(youtubeUrl, options: [:]) { success in
                if !success {
                    // Fallback to web browser
                    self.openInWebBrowser(url: trailerUrl)
                }
            }
        } else {
            // Open in web browser
            openInWebBrowser(url: trailerUrl)
        }
    }
    
    /// Open URL in web browser
    private static func openInWebBrowser(url: String) {
        guard let webUrl = URL(string: url) else {
            print("Invalid URL: \(url)")
            return
        }
        
        UIApplication.shared.open(webUrl, options: [:]) { success in
            if !success {
                print("Failed to open URL: \(url)")
            }
        }
    }
    
    // MARK: - Trailer Selection UI
    
    /// Show trailer selection alert if multiple trailers exist
    static func showTrailerSelection(for content: VugaContent, from viewController: UIViewController) {
        let trailers = content.sortedTrailers
        
        if trailers.isEmpty {
            print("No trailers available")
            return
        }
        
        if trailers.count == 1 {
            // Only one trailer, play it directly
            if let trailer = trailers.first {
                openTrailer(trailer: trailer, from: viewController)
            }
            return
        }
        
        // Multiple trailers - show selection
        let alertController = UIAlertController(
            title: "Select Trailer",
            message: "Choose which trailer to watch",
            preferredStyle: .actionSheet
        )
        
        for trailer in trailers {
            let title = trailer.effectiveTitle
            let isPrimary = trailer.isEffectivePrimary
            let displayTitle = isPrimary ? "\(title) (Primary)" : title
            
            let action = UIAlertAction(title: displayTitle, style: .default) { _ in
                openTrailer(trailer: trailer, from: viewController)
            }
            
            alertController.addAction(action)
        }
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel)
        alertController.addAction(cancelAction)
        
        // For iPad
        if let popoverController = alertController.popoverPresentationController {
            popoverController.sourceView = viewController.view
            popoverController.sourceRect = CGRect(x: viewController.view.bounds.midX,
                                                 y: viewController.view.bounds.midY,
                                                 width: 0, height: 0)
            popoverController.permittedArrowDirections = []
        }
        
        viewController.present(alertController, animated: true)
    }
    
    /// Open specific trailer
    static func openTrailer(trailer: TrailerModel, from viewController: UIViewController) {
        guard let trailerUrl = trailer.getPlayableUrl() else {
            print("No playable URL for trailer: \(trailer.effectiveTitle)")
            return
        }
        
        // Try YouTube app first if it's a YouTube video
        if !trailer.effectiveYoutubeId.isEmpty,
           let youtubeUrl = URL(string: "youtube://\(trailer.effectiveYoutubeId)"),
           UIApplication.shared.canOpenURL(youtubeUrl) {
            
            UIApplication.shared.open(youtubeUrl, options: [:]) { success in
                if !success {
                    // Fallback to web browser
                    UIApplication.shared.open(trailerUrl, options: [:])
                }
            }
        } else {
            // Open in web browser
            UIApplication.shared.open(trailerUrl, options: [:])
        }
    }
}

// MARK: - SwiftUI Integration Extensions

extension TrailerUtils {
    
    /// Get trailer button configuration for SwiftUI
    static func getTrailerButtonConfig(for content: VugaContent) -> TrailerButtonConfig? {
        guard hasTrailers(for: content) else { return nil }
        
        let trailers = content.sortedTrailers
        let hasMultiple = trailers.count > 1
        
        return TrailerButtonConfig(
            isEnabled: true,
            hasMultipleTrailers: hasMultiple,
            primaryTrailerTitle: content.primaryTrailer?.effectiveTitle ?? "Trailer"
        )
    }
}

// MARK: - Helper Structs

struct TrailerButtonConfig {
    let isEnabled: Bool
    let hasMultipleTrailers: Bool
    let primaryTrailerTitle: String
}