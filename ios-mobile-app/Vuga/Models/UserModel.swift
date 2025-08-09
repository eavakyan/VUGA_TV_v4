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
    
    enum CodingKeys: String, CodingKey {
        case status, message, data
    }
    
    // Custom decoder to handle status as both Bool and Int
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        message = try container.decodeIfPresent(String.self, forKey: .message)
        data = try container.decodeIfPresent(User.self, forKey: .data)
        
        // Handle status field that can be either Bool or Int
        if let statusBool = try? container.decodeIfPresent(Bool.self, forKey: .status) {
            status = statusBool
        } else if let statusInt = try? container.decodeIfPresent(Int.self, forKey: .status) {
            status = statusInt == 1
        } else {
            status = false
        }
    }
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
    let smsConsent: Bool?
    let emailConsent: Bool?
    let smsConsentDate: String?
    let emailConsentDate: String?

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
        case smsConsent = "sms_consent"
        case emailConsent = "email_consent"
        case smsConsentDate = "sms_consent_date"
        case emailConsentDate = "email_consent_date"
    }
    
    // Custom decoder to handle consent fields as both Bool and Int
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        id = try container.decodeIfPresent(Int.self, forKey: .id)
        fullname = try container.decodeIfPresent(String.self, forKey: .fullname)
        email = try container.decodeIfPresent(String.self, forKey: .email)
        loginType = try container.decodeIfPresent(LoginType.self, forKey: .loginType)
        identity = try container.decodeIfPresent(String.self, forKey: .identity)
        profileImage = try container.decodeIfPresent(String.self, forKey: .profileImage)
        watchlistContentIDS = try container.decodeIfPresent(String.self, forKey: .watchlistContentIDS)
        deviceType = try container.decodeIfPresent(DeviceType.self, forKey: .deviceType)
        deviceToken = try container.decodeIfPresent(String.self, forKey: .deviceToken)
        status = try container.decodeIfPresent(Int.self, forKey: .status)
        isPremium = try container.decodeIfPresent(Int.self, forKey: .isPremium)
        timezone = try container.decodeIfPresent(String.self, forKey: .timezone)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
        updatedAt = try container.decodeIfPresent(String.self, forKey: .updatedAt)
        profiles = try container.decodeIfPresent([Profile].self, forKey: .profiles)
        lastActiveProfileId = try container.decodeIfPresent(Int.self, forKey: .lastActiveProfileId)
        lastActiveProfile = try container.decodeIfPresent(Profile.self, forKey: .lastActiveProfile)
        smsConsentDate = try container.decodeIfPresent(String.self, forKey: .smsConsentDate)
        emailConsentDate = try container.decodeIfPresent(String.self, forKey: .emailConsentDate)
        
        // Handle consent fields that can be either Bool or Int
        if let smsConsentBool = try? container.decodeIfPresent(Bool.self, forKey: .smsConsent) {
            smsConsent = smsConsentBool
        } else if let smsConsentInt = try? container.decodeIfPresent(Int.self, forKey: .smsConsent) {
            smsConsent = smsConsentInt == 1
        } else {
            smsConsent = false
        }
        
        if let emailConsentBool = try? container.decodeIfPresent(Bool.self, forKey: .emailConsent) {
            emailConsent = emailConsentBool
        } else if let emailConsentInt = try? container.decodeIfPresent(Int.self, forKey: .emailConsent) {
            emailConsent = emailConsentInt == 1
        } else {
            emailConsent = false
        }
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
