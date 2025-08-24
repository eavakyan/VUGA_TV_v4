//
//  LiveTVModels.swift
//  Vuga
//
//  Live TV models that match Android implementation for API compatibility
//

import Foundation

// MARK: - Live TV Response Model (matches Android LiveTvResponse)
struct LiveTVResponse: Codable {
    let status: Bool?
    let message: String?
    let channels: [LiveTVChannel]?
    let categories: [LiveTVCategory]?
    let data: [LiveTVCategory]? // For old API format compatibility
    let total: Int?
    let currentPage: Int?
    let lastPage: Int?
    
    enum CodingKeys: String, CodingKey {
        case status, message, channels, categories, data, total
        case currentPage = "current_page"
        case lastPage = "last_page"
    }
}

// MARK: - Live TV Channel Model (matches Android LiveTvChannel)
struct LiveTVChannel: Codable, Identifiable, Hashable {
    let id: Int?
    let tvChannelId: Int? // Old API compatibility
    let title: String?
    let streamUrl: String?
    let source: String? // Old API uses "source" instead of "stream_url"
    let logoUrl: String?
    let thumbnail: String?
    let categoryIds: String?
    let channelNumber: Int?
    let accessType: Int? // Old API: 1=Free, 2=Premium, 3=Ads
    let isPremium: Bool?
    let isFree: Bool?
    let requiresAds: Bool?
    let currentProgramTitle: String?
    let currentProgramDescription: String?
    let currentProgramStart: String?
    let currentProgramEnd: String?
    let nextProgramTitle: String?
    let nextProgramStart: String?
    let isActive: Bool?
    let quality: String?
    let description: String?
    
    enum CodingKeys: String, CodingKey {
        case id, title, thumbnail, description, quality
        case tvChannelId = "tv_channel_id"
        case streamUrl = "stream_url"
        case source
        case logoUrl = "logo_url"
        case categoryIds = "category_ids"
        case channelNumber = "channel_number"
        case accessType = "access_type"
        case isPremium = "is_premium"
        case isFree = "is_free"
        case requiresAds = "requires_ads"
        case currentProgramTitle = "current_program_title"
        case currentProgramDescription = "current_program_description"
        case currentProgramStart = "current_program_start"
        case currentProgramEnd = "current_program_end"
        case nextProgramTitle = "next_program_title"
        case nextProgramStart = "next_program_start"
        case isActive = "is_active"
    }
    
    // Computed properties for API compatibility
    var channelId: Int {
        return (id ?? 0) > 0 ? (id ?? 0) : (tvChannelId ?? 0)
    }
    
    var displayTitle: String {
        return title ?? ""
    }
    
    var playbackUrl: String {
        // Use source if streamUrl is null (for old API compatibility)
        return streamUrl ?? source ?? ""
    }
    
    var channelLogo: String? {
        return logoUrl ?? thumbnail
    }
    
    var isChannelFree: Bool {
        // Check accessType if boolean fields are not set (old API)
        if let accessType = accessType, accessType > 0 {
            return accessType == 1
        }
        return isFree ?? false
    }
    
    var isChannelPremium: Bool {
        // Check accessType if boolean fields are not set (old API)
        if let accessType = accessType, accessType > 0 {
            return accessType == 2
        }
        return isPremium ?? false
    }
    
    var channelRequiresAds: Bool {
        // Check accessType if boolean fields are not set (old API)
        if let accessType = accessType, accessType > 0 {
            return accessType == 3
        }
        return requiresAds ?? false
    }
    
    var displayCurrentProgram: String {
        return currentProgramTitle ?? "No program info"
    }
    
    var displayQuality: String {
        return quality ?? "HD"
    }
}

// MARK: - Live TV Category Model (matches Android LiveTvCategory)
struct LiveTVCategory: Codable, Identifiable, Hashable {
    let id: Int?
    let tvCategoryId: Int? // Old API uses this field name
    let name: String?
    let title: String? // Old API uses "title" instead of "name"
    let slug: String?
    let icon: String?
    let order: Int?
    let isActive: Bool?
    let channelCount: Int?
    let channels: [LiveTVChannel]? // For old API format
    
    enum CodingKeys: String, CodingKey {
        case id, name, title, slug, icon, order, channels
        case tvCategoryId = "tv_category_id"
        case isActive = "is_active"
        case channelCount = "channel_count"
    }
    
    // Computed properties for API compatibility
    var categoryId: Int {
        // Return tvCategoryId if id is 0 (for old API compatibility)
        return (id ?? 0) > 0 ? (id ?? 0) : (tvCategoryId ?? 0)
    }
    
    var displayName: String {
        // Use title if name is null (for old API compatibility)
        let displayName = name ?? title
        return displayName ?? ""
    }
    
    // Predefined categories for UI
    static let allCategory = LiveTVCategory(
        id: 0, 
        tvCategoryId: nil, 
        name: "All", 
        title: nil, 
        slug: nil, 
        icon: nil, 
        order: nil, 
        isActive: true, 
        channelCount: nil, 
        channels: nil
    )
    
    static let featuredCategory = LiveTVCategory(
        id: -1, 
        tvCategoryId: nil, 
        name: "Featured", 
        title: nil, 
        slug: nil, 
        icon: nil, 
        order: nil, 
        isActive: true, 
        channelCount: nil, 
        channels: nil
    )
}

// MARK: - Schedule Grid Response
struct ScheduleGridResponse: Codable {
    let status: Bool?
    let message: String?
    let schedule: ScheduleData?
}

struct ScheduleData: Codable {
    // Can be customized based on actual API response structure
    let programs: [ProgramInfo]?
    let timeSlots: [String]?
}

struct ProgramInfo: Codable {
    let id: Int?
    let title: String?
    let description: String?
    let startTime: String?
    let endTime: String?
    let channelId: Int?
    
    enum CodingKeys: String, CodingKey {
        case id, title, description
        case startTime = "start_time"
        case endTime = "end_time"
        case channelId = "channel_id"
    }
}

// MARK: - View Tracking Response
struct ViewTrackingResponse: Codable {
    let status: Bool?
    let message: String?
    let viewCount: Int?
    
    enum CodingKeys: String, CodingKey {
        case status, message
        case viewCount = "view_count"
    }
}

// MARK: - API Request/Response Models for iOS compatibility with existing infrastructure
struct LiveTVPageData: Codable {
    let status: Bool?
    let message: String?
    let data: [TVCategory]?
}

// Extension to convert between new and old models for backward compatibility
extension LiveTVChannel {
    // Convert to existing Channel model for UI compatibility
    var toChannel: Channel {
        return Channel(
            id: self.channelId,
            title: self.displayTitle,
            thumbnail: self.channelLogo,
            accessType: self.isChannelFree ? .free : (self.isChannelPremium ? .premium : .locked),
            categoryID: self.categoryIds,
            type: self.playbackUrl.lowercased().contains("youtube") ? .youtube : .server,
            source: self.playbackUrl,
            createdAt: nil,
            updatedAt: nil
        )
    }
}

extension LiveTVCategory {
    // Convert to existing TVCategory model for UI compatibility
    var toTVCategory: TVCategory {
        return TVCategory(
            id: self.categoryId,
            title: self.displayName,
            image: self.icon,
            createdAt: nil,
            updatedAt: nil,
            channels: self.channels?.map { $0.toChannel }
        )
    }
}

// MARK: - Live TV API Parameters
struct LiveTVAPIParams {
    static func channelsWithPrograms(userId: Int, page: Int = 1, perPage: Int = 20) -> [String: Any] {
        return [
            "userId": userId,
            "page": page,
            "per_page": perPage
        ]
    }
    
    static func trackChannelView(channelId: Int, userId: Int) -> [String: Any] {
        return [
            "channel_id": channelId,
            "userId": userId
        ]
    }
    
    static func scheduleGrid(date: String? = nil) -> [String: Any] {
        var params: [String: Any] = [:]
        if let date = date {
            params["date"] = date
        }
        return params
    }
    
    static func searchChannels(query: String, userId: Int, page: Int = 1) -> [String: Any] {
        return [
            "query": query,
            "userId": userId,
            "page": page
        ]
    }
}