//
//  UnifiedWatchlistModel.swift
//  Vuga
//
//  Unified watchlist model for movies, TV shows, and episodes
//

import Foundation

// Unified watchlist item that can be either content or episode
struct UnifiedWatchlistItem: Codable, Identifiable {
    let itemType: String
    let contentId: Int?
    let episodeId: Int?
    let title: String?
    let type: Int?
    let poster: String?
    let ratings: String?
    let seriesTitle: String?
    let seasonNumber: Int?
    let episodeNumber: Int?
    let addedAt: String?
    
    var id: String {
        if itemType == "episode" {
            return "episode_\(episodeId ?? 0)"
        } else {
            return "content_\(contentId ?? 0)"
        }
    }
    
    var displayTitle: String {
        if itemType == "episode" {
            if let seriesTitle = seriesTitle,
               let seasonNum = seasonNumber,
               let episodeNum = episodeNumber {
                return "\(seriesTitle) S\(seasonNum) E\(episodeNum)"
            }
            return title ?? "Episode"
        }
        return title ?? ""
    }
    
    var isEpisode: Bool {
        return itemType == "episode"
    }
    
    var thumbnailURL: URL? {
        if let poster = poster {
            return poster.addBaseURL()
        }
        return nil
    }
    
    private enum CodingKeys: String, CodingKey {
        case itemType = "item_type"
        case contentId = "content_id"
        case episodeId = "episode_id"
        case title
        case type
        case poster
        case ratings
        case seriesTitle = "series_title"
        case seasonNumber = "season_number"
        case episodeNumber = "episode_number"
        case addedAt = "added_at"
    }
    
    // Custom decoder to handle ratings as either String or Number
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        itemType = try container.decode(String.self, forKey: .itemType)
        contentId = try container.decodeIfPresent(Int.self, forKey: .contentId)
        episodeId = try container.decodeIfPresent(Int.self, forKey: .episodeId)
        title = try container.decodeIfPresent(String.self, forKey: .title)
        type = try container.decodeIfPresent(Int.self, forKey: .type)
        poster = try container.decodeIfPresent(String.self, forKey: .poster)
        seriesTitle = try container.decodeIfPresent(String.self, forKey: .seriesTitle)
        seasonNumber = try container.decodeIfPresent(Int.self, forKey: .seasonNumber)
        episodeNumber = try container.decodeIfPresent(Int.self, forKey: .episodeNumber)
        addedAt = try container.decodeIfPresent(String.self, forKey: .addedAt)
        
        // Handle ratings as either String or Number
        if let ratingsString = try? container.decodeIfPresent(String.self, forKey: .ratings) {
            ratings = ratingsString
        } else if let ratingsDouble = try? container.decodeIfPresent(Double.self, forKey: .ratings) {
            ratings = String(ratingsDouble)
        } else if let ratingsInt = try? container.decodeIfPresent(Int.self, forKey: .ratings) {
            ratings = String(ratingsInt)
        } else {
            ratings = nil
        }
    }
}

// Response model for unified watchlist
struct UnifiedWatchlistResponse: Codable {
    let status: Bool?
    let message: String?
    let data: [UnifiedWatchlistItem]?
    let total: Int?
}