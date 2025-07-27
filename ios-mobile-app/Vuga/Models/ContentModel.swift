//
//  ContentModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 15/05/24.
//

import Foundation

// MARK: - ContentModel
struct ContentModel: Codable {
    let status: Bool?
    let message: String?
    let data: FlixyContent?
}

// MARK: - Content
struct FlixyContent: Codable {
    let id: Int?
    let title, description: String?
    let type: ContentType?
    let duration : String?
    let releaseYear: Int?
    let ratings: Double?
    let languageID: Int?
    let downloadLink: String?
    let trailerURL: String?
    let verticalPoster, horizontalPoster, genreIDS: String?
    var isFeatured, totalView, totalDownload, totalShare: Int?
    let actorIDS: String?
    let createdAt, updatedAt: String?
    let isWatchlist: Bool?
    let contentCast: [Cast]?
    let contentSources: [Source]?
    let contentSubtitles: [Subtitle]?
    let seasons: [Season]?
    let moreLikeThis: [FlixyContent]?
    
    enum CodingKeys: String, CodingKey {
        case id, title, description, type, duration
        case releaseYear = "release_year"
        case ratings
        case languageID = "language_id"
        case downloadLink = "download_link"
        case trailerURL = "trailer_url"
        case verticalPoster = "vertical_poster"
        case horizontalPoster = "horizontal_poster"
        case genreIDS = "genre_ids"
        case isFeatured = "is_featured"
        case totalView = "total_view"
        case totalDownload = "total_download"
        case totalShare = "total_share"
        case actorIDS = "actor_ids"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case isWatchlist = "is_watchlist"
        case contentCast
        case contentSources = "content_sources"
        case contentSubtitles = "content_subtitles"
        case seasons
        case moreLikeThis = "more_like_this"
    }
    
    var ratingString : String {
        String(format: "%.1f", ratings ?? 0)
    }
    
    var genres : [Genre] {
        let genreIds = genreIDS?.components(separatedBy: ",").map({ Int($0) ?? 0 }) ?? []
        let genres = SessionManager.shared.getGenres().filter({ genreIds.contains($0.id ?? 0) })
        return genres
    }
    
    var genreString : String {
        genres.map({ $0.title ?? "" }).joined(separator: ", ")
    }
    
}


struct Subtitle: Codable, Identifiable {
    let id: Int?
    let contentID: String?
    let languageID: Int?
    let file, createdAt, updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id
        case contentID = "content_id"
        case languageID = "language_id"
        case file
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
    var language : ContentLanguage {
        SessionManager.shared.getLanguages().first(where: {$0.id == languageID})!
    }
}


struct Source: Codable,Identifiable {
    let id, contentID: Int?
    let title, quality, size: String?
    let type: SourceType?
    let accessType: AccessType?
    let isDownload: Int?
    let source, createdAt, updatedAt: String?
    let media: Media?

    enum CodingKeys: String, CodingKey {
        case id
        case contentID = "content_id"
        case title, quality, size
        case isDownload = "is_download"
        case accessType = "access_type"
        case type, source
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case media
    }
    func sourceDownloadId(contentType: ContentType) -> String {
        return "\(contentType.title)_\(id ?? 0)"
    }
    
//    var sourceURL: URL {
//        if type == .file {
//            return media?.file?.addBaseURL()
//        } else {
//            return URL(string: source ?? "https://www.google.com")
//        }
//    }
    var sourceURL: URL {
        if type == .file, let fileURL = media?.file?.addBaseURL() {
            return fileURL
        }
        return URL(string: source ?? "https://www.google.com") ?? URL(string: "https://www.google.com")!
    }
    
}

struct Media: Codable {
    let id: Int?
    let title, file, createdAt, updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id, title, file
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}

// MARK: - Source
//struct Source: Codable,Identifiable,Hashable {
//    let id, contentID: Int?
//    let title, quality, size: String?
//    let isDownload: Int?
//    let type: SourceType?
//    let accessType: AccessType?
//    let source: String?
//    let createdAt: String?
//    let updatedAt: String?
//    let episodeID: Int?
//    
//    enum CodingKeys: String, CodingKey {
//        case id
//        case contentID = "content_id"
//        case title, quality, size
//        case isDownload = "is_download"
//        case accessType = "access_type"
//        case type, source
//        case createdAt = "created_at"
//        case updatedAt = "updated_at"
//        case episodeID = "episode_id"
//    }
//    
//    
//}

// MARK: - Cast
struct Cast: Codable {
    let id, contentID, actorID: Int?
    let charactorName, createdAt: String?
    let updatedAt: String?
    let actor: Actor?
    
    enum CodingKeys: String, CodingKey {
        case id
        case contentID = "content_id"
        case actorID = "actor_id"
        case charactorName = "character_name"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case actor
    }
}

// MARK: - Actor
struct Actor: Codable {
    let id: Int?
    let fullname, profile_image, createdAt: String?
    let updatedAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, fullname, profile_image
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}

// MARK: - Season
struct Season: Codable,Equatable {
    static func == (lhs: Season, rhs: Season) -> Bool {
        true
    }
    
    let id, contentID: Int?
    let title: String?
    let trailerURL: String?
    let createdAt: String?
    let updatedAt: String?
    let episodes: [Episode]?
    
    enum CodingKeys: String, CodingKey {
        case id
        case contentID = "content_id"
        case title
        case trailerURL = "trailer_url"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case episodes
    }
}

// MARK: - Episode
struct Episode: Codable {
    let id, seasonID, number: Int?
    let thumbnail, title, description, duration: String?
    var accessType, totalView, totalDownload: Int?
    let createdAt: String?
    let updatedAt: String?
    let sources: [Source]?
    let episodeSubtitle: [Subtitle]?

    
    enum CodingKeys: String, CodingKey {
        case id
        case seasonID = "season_id"
        case number, thumbnail, title, description, duration
        case accessType = "access_type"
        case totalView = "total_view"
        case totalDownload = "total_download"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case episodeSubtitle = "subtitles"
        case sources
    }
}

enum AccessType : Int, Codable {
    case free = 1
    case premium = 2
    case locked = 3
}

enum SourceType : Int, Codable {
    case youtubeURL = 1
    case m3u8 = 2
    case mov = 3
    case mp4 = 4
    case mkv = 5
    case webm = 6
    case file = 7
    case embeddedURL = 8
}

// MARK: - Subtitle
//struct Subtitle: Codable {
//    let subtitleFile, languageName: String?
//    
//    enum CodingKeys: String, CodingKey {
//        case subtitleFile = "subtitle_file"
//        case languageName = "language_name"
//    }
//}

enum ContentType : Int, CaseIterable, Codable {
    case all = 0
    case movie = 1
    case series = 2
    
    var title: String {
        switch self {
        case .all: .all
        case .movie: .movie
        case .series: .series
        }
    }
}



extension DownloadContent {
    var type: ContentType {
        get {
            return ContentType(rawValue: Int(self.contentType)) ?? .movie
        }
        set {
            self.contentType = Int16(newValue.rawValue)
        }
    }
    var status: DownloadStatus {
        get {
            return DownloadStatus(rawValue: Int(self.downloadStatus)) ?? .notStarted
        }
        set {
            self.downloadStatus = Int16(newValue.rawValue)
        }
    }
}

extension RecentlyWatched {
    var type: ContentType {
        get {
            return ContentType(rawValue: Int(self.contentType)) ?? .movie
        }
        set {
            self.contentType = Int16(newValue.rawValue)
        }
    }
}

