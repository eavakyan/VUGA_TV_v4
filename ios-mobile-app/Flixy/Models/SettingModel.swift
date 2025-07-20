//
//  SettingModel.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import Foundation

// MARK: - SettingModel
struct SettingModel: Codable {
    let status: Bool?
    let message: String?
    let setting: Setting?
    let genres: [Genre]?
    let languages: [ContentLanguage]?
    let admob: [Admob]?
}

// MARK: - Ads
struct Admob: Codable {
    let id: Int?
    let bannerID, intersialID, rewardedID: String?
    let type: Int?
    let createdAt, updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id
        case bannerID = "banner_id"
        case intersialID = "intersial_id"
        case rewardedID = "rewarded_id"
        case type
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}

// MARK: - Setting
struct Setting: Codable {
    let id: Int?
    let privacyURL, termsURL, moreAppsURL: String?
    let googlePlayLicenceKey: String?
    let appName: String?
    let isLiveTvEnable, isAdmobAnd, isAdmobIos, isCustomAnd: Int?
    let isCustomIos, videoadSkipTime: Int?
    let createdAt, updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id
        case privacyURL = "privacy_url"
        case termsURL = "terms_url"
        case moreAppsURL = "more_apps_url"
        case googlePlayLicenceKey = "google_play_licence_key"
        case appName = "app_name"
        case isLiveTvEnable = "is_live_tv_enable"
        case isAdmobAnd = "is_admob_android"
        case isAdmobIos = "is_admob_ios"
        case isCustomAnd = "is_custom_android"
        case isCustomIos = "is_custom_ios"
        case videoadSkipTime = "videoad_skip_time"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}

// MARK: - ContentLanguage
struct ContentLanguage: Codable {
    let id: Int?
    let title, createdAt: String?
    let updatedAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, title
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}


