//
//  UserModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import Foundation

// MARK: - UserModel
struct UserModel: Codable {
    let status: Bool?
    let message: String?
    let data: User?
}

// MARK: - User
struct User: Codable {
    let id: Int?
    let fullname, email: String?
    let loginType: LoginType?
    let identity, profileImage, watchlistContentIDS: String?
    let deviceType: DeviceType?
    let deviceToken: String?
    let status, isPremium: Int?
    let timezone, createdAt, updatedAt: String?
    let profiles: [Profile]?
    let lastActiveProfileId: Int?
    let lastActiveProfile: Profile?

    enum CodingKeys: String, CodingKey {
        case id = "app_user_id"
        case fullname, email
        case loginType = "login_type"
        case identity
        case profileImage = "profile_image"
        case watchlistContentIDS = "watchlist_content_ids"
        case deviceType = "device_type"
        case deviceToken = "device_token"
        case status
        case isPremium = "is_premium"
        case timezone
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case profiles
        case lastActiveProfileId = "last_active_profile_id"
        case lastActiveProfile = "last_active_profile"
    }
    
    var watchlistIds : [Int] {
        (watchlistContentIDS?.components(separatedBy: ",") ?? []).map({ Int($0) ?? 0 }) 
    }
    
    func checkIsAddedToWatchList(contentId: Int) -> Bool {
        (watchlistContentIDS?.components(separatedBy: ",") ?? []).map({ Int($0) ?? 0 }).contains(contentId)
    }
}


enum LoginType : Int, Codable {
    case gmail = 1
    case facebook = 2
    case apple = 3
    case email = 4
}

enum DeviceType : Int, Codable {
    case android = 1
    case iOS = 2
}
