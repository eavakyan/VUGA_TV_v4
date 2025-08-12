//
//  RecentlyWatchedModel.swift
//  Vuga
//
//  Model for Recently Watched content fetched from API
//

import Foundation

struct RecentlyWatchedContent: Identifiable {
    let id = UUID()
    let contentId: Int
    let contentName: String
    let horizontalPoster: String?
    let verticalPoster: String?
    let contentType: Int
    let releaseYear: Int?
    let ratings: Double?
    let contentDuration: Int? // Duration in seconds from API
    let genres: [Genre]?
    let episodeInfo: EpisodeInfo?
    let watchedDate: Date
    let progress: Double
    let totalDuration: Double // Watch progress duration
    
    // Additional fields from local storage
    let episodeId: Int?
    let contentSourceId: Int
    let contentSourceType: Int
    let sourceUrl: String
    let isForDownload: Bool
}

struct EpisodeInfo: Codable {
    let episodeId: Int
    let episodeTitle: String
    let episodeThumbnail: String?
    let seasonNumber: Int?
    let episodeNumber: Int?
    
    enum CodingKeys: String, CodingKey {
        case episodeId = "episode_id"
        case episodeTitle = "episode_title"
        case episodeThumbnail = "episode_thumbnail"
        case seasonNumber = "season_number"
        case episodeNumber = "episode_number"
    }
}

struct RecentlyWatchedAPIResponse: Codable {
    let status: Bool
    let message: String
    let data: [RecentlyWatchedAPIContent]?
}

struct RecentlyWatchedAPIContent: Codable {
    let contentId: Int
    let contentName: String
    let horizontalPoster: String?
    let verticalPoster: String?
    let contentType: Int
    let releaseYear: Int?
    let ratings: Double?
    let duration: Int?
    let genres: [Genre]?
    let seasons: [Season]?
    
    enum CodingKeys: String, CodingKey {
        case contentId = "content_id"
        case contentName = "content_name"
        case horizontalPoster = "horizontal_poster"
        case verticalPoster = "vertical_poster"
        case contentType = "content_type"
        case releaseYear = "release_year"
        case ratings
        case duration
        case genres
        case seasons
    }
}