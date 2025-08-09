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
    let data: VugaContent?
}

// MARK: - Content
struct VugaContent: Codable, Identifiable {
    let id: Int?
    let title, description: String?
    let type: ContentType?
    let duration : String?
    let releaseYear: Int?
    let ratings: Double?
    let languageID: Int?
    let downloadLink: String?
    let trailerURL: String?
    let trailerYoutubeId: String?
    let trailers: [TrailerModel]?
    let verticalPoster, horizontalPoster, genreIDS: String?
    var isFeatured, totalView, totalDownload, totalShare: Int?
    let actorIDS: String?
    let createdAt, updatedAt: String?
    let isWatchlist: Bool?
    let contentCast: [Cast]?
    let contentSources: [Source]?
    let contentSubtitles: [Subtitle]?
    let seasons: [Season]?
    let moreLikeThis: [VugaContent]?
    let ageRating: String?
    let minAge: Int?
    let ageLimits: [AgeRating]?
    let userRating: Double?
    
    enum CodingKeys: String, CodingKey {
        case id = "content_id"
        case title, description, type, duration
        case releaseYear = "release_year"
        case ratings
        case languageID = "language_id"
        case downloadLink = "download_link"
        case trailerURL = "trailer_url"
        case trailerYoutubeId = "trailer_youtube_id"
        case trailers
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
        case ageRating = "age_rating"
        case minAge = "min_age"
        case ageLimits = "age_limits"
        case userRating = "user_rating"
    }
    
    // Custom Codable implementation
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        id = try container.decodeIfPresent(Int.self, forKey: .id)
        title = try container.decodeIfPresent(String.self, forKey: .title)
        description = try container.decodeIfPresent(String.self, forKey: .description)
        type = try container.decodeIfPresent(ContentType.self, forKey: .type)
        duration = try container.decodeIfPresent(String.self, forKey: .duration)
        releaseYear = try container.decodeIfPresent(Int.self, forKey: .releaseYear)
        ratings = try container.decodeIfPresent(Double.self, forKey: .ratings)
        languageID = try container.decodeIfPresent(Int.self, forKey: .languageID)
        downloadLink = try container.decodeIfPresent(String.self, forKey: .downloadLink)
        trailerURL = try container.decodeIfPresent(String.self, forKey: .trailerURL)
        trailerYoutubeId = try container.decodeIfPresent(String.self, forKey: .trailerYoutubeId)
        trailers = try container.decodeIfPresent([TrailerModel].self, forKey: .trailers)
        verticalPoster = try container.decodeIfPresent(String.self, forKey: .verticalPoster)
        horizontalPoster = try container.decodeIfPresent(String.self, forKey: .horizontalPoster)
        genreIDS = try container.decodeIfPresent(String.self, forKey: .genreIDS)
        isFeatured = try container.decodeIfPresent(Int.self, forKey: .isFeatured)
        totalView = try container.decodeIfPresent(Int.self, forKey: .totalView)
        totalDownload = try container.decodeIfPresent(Int.self, forKey: .totalDownload)
        totalShare = try container.decodeIfPresent(Int.self, forKey: .totalShare)
        actorIDS = try container.decodeIfPresent(String.self, forKey: .actorIDS)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
        updatedAt = try container.decodeIfPresent(String.self, forKey: .updatedAt)
        isWatchlist = try container.decodeIfPresent(Bool.self, forKey: .isWatchlist)
        contentCast = try container.decodeIfPresent([Cast].self, forKey: .contentCast)
        contentSources = try container.decodeIfPresent([Source].self, forKey: .contentSources)
        contentSubtitles = try container.decodeIfPresent([Subtitle].self, forKey: .contentSubtitles)
        seasons = try container.decodeIfPresent([Season].self, forKey: .seasons)
        moreLikeThis = try container.decodeIfPresent([VugaContent].self, forKey: .moreLikeThis)
        ageRating = try container.decodeIfPresent(String.self, forKey: .ageRating)
        minAge = try container.decodeIfPresent(Int.self, forKey: .minAge)
        ageLimits = try container.decodeIfPresent([AgeRating].self, forKey: .ageLimits)
        userRating = try container.decodeIfPresent(Double.self, forKey: .userRating)
    }
    
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        
        try container.encodeIfPresent(id, forKey: .id)
        try container.encodeIfPresent(title, forKey: .title)
        try container.encodeIfPresent(description, forKey: .description)
        try container.encodeIfPresent(type, forKey: .type)
        try container.encodeIfPresent(duration, forKey: .duration)
        try container.encodeIfPresent(releaseYear, forKey: .releaseYear)
        try container.encodeIfPresent(ratings, forKey: .ratings)
        try container.encodeIfPresent(languageID, forKey: .languageID)
        try container.encodeIfPresent(downloadLink, forKey: .downloadLink)
        try container.encodeIfPresent(trailerURL, forKey: .trailerURL)
        try container.encodeIfPresent(trailerYoutubeId, forKey: .trailerYoutubeId)
        try container.encodeIfPresent(trailers, forKey: .trailers)
        try container.encodeIfPresent(verticalPoster, forKey: .verticalPoster)
        try container.encodeIfPresent(horizontalPoster, forKey: .horizontalPoster)
        try container.encodeIfPresent(genreIDS, forKey: .genreIDS)
        try container.encodeIfPresent(isFeatured, forKey: .isFeatured)
        try container.encodeIfPresent(totalView, forKey: .totalView)
        try container.encodeIfPresent(totalDownload, forKey: .totalDownload)
        try container.encodeIfPresent(totalShare, forKey: .totalShare)
        try container.encodeIfPresent(actorIDS, forKey: .actorIDS)
        try container.encodeIfPresent(createdAt, forKey: .createdAt)
        try container.encodeIfPresent(updatedAt, forKey: .updatedAt)
        try container.encodeIfPresent(isWatchlist, forKey: .isWatchlist)
        try container.encodeIfPresent(contentCast, forKey: .contentCast)
        try container.encodeIfPresent(contentSources, forKey: .contentSources)
        try container.encodeIfPresent(contentSubtitles, forKey: .contentSubtitles)
        try container.encodeIfPresent(seasons, forKey: .seasons)
        try container.encodeIfPresent(moreLikeThis, forKey: .moreLikeThis)
        try container.encodeIfPresent(ageRating, forKey: .ageRating)
        try container.encodeIfPresent(minAge, forKey: .minAge)
        try container.encodeIfPresent(ageLimits, forKey: .ageLimits)
        try container.encodeIfPresent(userRating, forKey: .userRating)
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
    
    // Age restriction helpers
    var ageRatingCode: String {
        return ageRating ?? "NR" // Not Rated
    }
    
    var minimumAge: Int {
        return minAge ?? 0
    }
    
    // Check if content is appropriate for a given profile
    func isAppropriateFor(profile: Profile?) -> Bool {
        guard let profile = profile else { return true }
        
        // Kids profiles can only see G and PG content
        if profile.effectiveKidsProfile {
            return ageRatingCode == "G" || ageRatingCode == "PG"
        }
        
        // Check age restriction
        if let profileAge = profile.age {
            return profileAge >= minimumAge
        }
        
        return true // No restrictions if age not set
    }
    
    // MARK: - Trailer Helpers
    
    // Get primary trailer from trailers list
    var primaryTrailer: TrailerModel? {
        return trailers?.primaryTrailer
    }
    
    // Get all trailers sorted properly
    var sortedTrailers: [TrailerModel] {
        return trailers?.sortedTrailers ?? []
    }
    
    // Get additional (non-primary) trailers
    var additionalTrailers: [TrailerModel] {
        return trailers?.additionalTrailers ?? []
    }
    
    // Backward compatibility: Get effective trailer URL
    var effectiveTrailerURL: String? {
        // Use the existing trailerURL if available
        if let trailerURL = trailerURL, !trailerURL.isEmpty {
            return trailerURL
        }
        // Otherwise get from primary trailer
        return primaryTrailer?.effectiveTrailerUrl
    }
    
    // Backward compatibility: Get effective trailer YouTube ID
    var effectiveTrailerYoutubeId: String? {
        // Use the existing trailerYoutubeId if available
        if let trailerYoutubeId = trailerYoutubeId, !trailerYoutubeId.isEmpty {
            return trailerYoutubeId
        }
        // Otherwise get from primary trailer
        return primaryTrailer?.effectiveYoutubeId
    }
    
    // Get trailer thumbnail URL (for UI)
    var trailerThumbnailURL: String? {
        return primaryTrailer?.effectiveThumbnailUrl
    }
    
    // Check if content has trailers
    var hasTrailers: Bool {
        return !(trailers?.isEmpty ?? true) || !(trailerURL?.isEmpty ?? true)
    }
    
    // Get age rating display color
    var ageRatingColor: String {
        switch ageRatingCode {
        case "G":
            return "#4CAF50" // Green
        case "PG":
            return "#8BC34A" // Light Green
        case "PG-13":
            return "#FF9800" // Orange
        case "R":
            return "#F44336" // Red
        case "NC-17":
            return "#9C27B0" // Purple
        default:
            return "#757575" // Gray
        }
    }
    
}


struct Subtitle: Codable, Identifiable {
    let id: Int?
    let contentID: String?
    let languageID: Int?
    let file, createdAt, updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id = "subtitle_id"
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
        case id = "content_source_id"
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
        case id = "media_gallery_id"
        case title, file
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
        case id = "content_cast_id"
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
        case id = "actor_id"
        case fullname, profile_image
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
        case id = "season_id"
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
    let thumbnail, title, description: String?
    var accessType, totalView, totalDownload: Int?
    let ratings: Double?
    let createdAt: String?
    let updatedAt: String?
    let sources: [Source]?
    let episodeSubtitle: [Subtitle]?
    let userRating: Double?
    
    // Handle duration as either String or Int
    private let durationString: String?
    private let durationInt: Int?
    
    var duration: String? {
        if let durationString = durationString {
            return durationString
        } else if let durationInt = durationInt {
            return String(durationInt)
        }
        return nil
    }

    
    enum CodingKeys: String, CodingKey {
        case id = "episode_id"
        case seasonID = "season_id"
        case number, thumbnail, title, description
        case accessType = "access_type"
        case totalView = "total_view"
        case totalDownload = "total_download"
        case ratings
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case episodeSubtitle = "subtitles"
        case sources
        case userRating = "user_rating"
        case duration
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decodeIfPresent(Int.self, forKey: .id)
        seasonID = try container.decodeIfPresent(Int.self, forKey: .seasonID)
        number = try container.decodeIfPresent(Int.self, forKey: .number)
        thumbnail = try container.decodeIfPresent(String.self, forKey: .thumbnail)
        title = try container.decodeIfPresent(String.self, forKey: .title)
        description = try container.decodeIfPresent(String.self, forKey: .description)
        accessType = try container.decodeIfPresent(Int.self, forKey: .accessType)
        totalView = try container.decodeIfPresent(Int.self, forKey: .totalView)
        totalDownload = try container.decodeIfPresent(Int.self, forKey: .totalDownload)
        ratings = try container.decodeIfPresent(Double.self, forKey: .ratings)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
        updatedAt = try container.decodeIfPresent(String.self, forKey: .updatedAt)
        sources = try container.decodeIfPresent([Source].self, forKey: .sources)
        episodeSubtitle = try container.decodeIfPresent([Subtitle].self, forKey: .episodeSubtitle)
        userRating = try container.decodeIfPresent(Double.self, forKey: .userRating)
        
        // Try to decode duration as String first, then as Int
        if let durationStr = try? container.decode(String.self, forKey: .duration) {
            durationString = durationStr
            durationInt = nil
        } else if let durationNum = try? container.decode(Int.self, forKey: .duration) {
            durationInt = durationNum
            durationString = nil
        } else {
            durationString = nil
            durationInt = nil
        }
    }
    
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encodeIfPresent(id, forKey: .id)
        try container.encodeIfPresent(seasonID, forKey: .seasonID)
        try container.encodeIfPresent(number, forKey: .number)
        try container.encodeIfPresent(thumbnail, forKey: .thumbnail)
        try container.encodeIfPresent(title, forKey: .title)
        try container.encodeIfPresent(description, forKey: .description)
        try container.encodeIfPresent(accessType, forKey: .accessType)
        try container.encodeIfPresent(totalView, forKey: .totalView)
        try container.encodeIfPresent(totalDownload, forKey: .totalDownload)
        try container.encodeIfPresent(ratings, forKey: .ratings)
        try container.encodeIfPresent(createdAt, forKey: .createdAt)
        try container.encodeIfPresent(updatedAt, forKey: .updatedAt)
        try container.encodeIfPresent(sources, forKey: .sources)
        try container.encodeIfPresent(episodeSubtitle, forKey: .episodeSubtitle)
        try container.encodeIfPresent(userRating, forKey: .userRating)
        
        // Encode duration as string
        try container.encodeIfPresent(duration, forKey: .duration)
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

