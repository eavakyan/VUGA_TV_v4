//
//  TrailerModel.swift
//  Vuga
//
//

import Foundation

// MARK: - Trailer Model
struct TrailerModel: Codable, Identifiable {
    let id: Int?
    let contentId: Int?
    let title: String?
    let youtubeId: String?
    let trailerUrl: String?
    let embedUrl: String?
    let watchUrl: String?
    let thumbnailUrl: String?
    let isPrimary: Bool?
    let sortOrder: Int?
    let createdAt: String?
    let updatedAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id = "content_trailer_id"
        case contentId = "content_id"
        case title
        case youtubeId = "youtube_id"
        case trailerUrl = "trailer_url"
        case embedUrl = "embed_url"
        case watchUrl = "watch_url"
        case thumbnailUrl = "thumbnail_url"
        case isPrimary = "is_primary"
        case sortOrder = "sort_order"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
    
    // Computed properties for convenience
    var effectiveTitle: String {
        return title ?? "Trailer"
    }
    
    var effectiveYoutubeId: String {
        return youtubeId ?? ""
    }
    
    var effectiveTrailerUrl: String {
        return trailerUrl ?? ""
    }
    
    var effectiveEmbedUrl: String {
        if !effectiveYoutubeId.isEmpty {
            return "https://www.youtube.com/embed/\(effectiveYoutubeId)"
        }
        return embedUrl ?? ""
    }
    
    var effectiveWatchUrl: String {
        if !effectiveYoutubeId.isEmpty {
            return "https://www.youtube.com/watch?v=\(effectiveYoutubeId)"
        }
        return watchUrl ?? ""
    }
    
    var effectiveThumbnailUrl: String {
        if !effectiveYoutubeId.isEmpty {
            return "https://img.youtube.com/vi/\(effectiveYoutubeId)/maxresdefault.jpg"
        }
        return thumbnailUrl ?? ""
    }
    
    var isEffectivePrimary: Bool {
        return isPrimary ?? false
    }
    
    var effectiveSortOrder: Int {
        return sortOrder ?? 0
    }
}

// MARK: - Trailer Response Models
struct TrailerResponse: Codable {
    let status: Bool?
    let message: String?
    let data: [TrailerModel]?
}

struct SingleTrailerResponse: Codable {
    let status: Bool?
    let message: String?
    let data: TrailerModel?
}

// MARK: - Extension for URL handling
extension TrailerModel {
    
    // Get URL for playing in YouTube app or web
    func getPlayableUrl() -> URL? {
        // Prefer the full trailer URL
        if let url = URL(string: effectiveTrailerUrl), !effectiveTrailerUrl.isEmpty {
            return url
        }
        
        // Fallback to watch URL
        if let url = URL(string: effectiveWatchUrl), !effectiveWatchUrl.isEmpty {
            return url
        }
        
        return nil
    }
    
    // Get embed URL for web view
    func getEmbedUrl() -> URL? {
        return URL(string: effectiveEmbedUrl)
    }
    
    // Get thumbnail URL for preview
    func getThumbnailUrl() -> URL? {
        return URL(string: effectiveThumbnailUrl)
    }
}

// MARK: - Extension for sorting and filtering
extension Array where Element == TrailerModel {
    
    // Get primary trailer
    var primaryTrailer: TrailerModel? {
        return first { $0.isEffectivePrimary }
    }
    
    // Get all trailers sorted by primary first, then by sort order
    var sortedTrailers: [TrailerModel] {
        return sorted { trailer1, trailer2 in
            if trailer1.isEffectivePrimary && !trailer2.isEffectivePrimary {
                return true
            } else if !trailer1.isEffectivePrimary && trailer2.isEffectivePrimary {
                return false
            } else {
                return trailer1.effectiveSortOrder < trailer2.effectiveSortOrder
            }
        }
    }
    
    // Get non-primary trailers
    var additionalTrailers: [TrailerModel] {
        return filter { !$0.isEffectivePrimary }.sorted { $0.effectiveSortOrder < $1.effectiveSortOrder }
    }
}