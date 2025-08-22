//
//  CustomAdModel.swift
//  Vuga
//
//

import Foundation

struct CustomAdModel: Codable {
    let status: Bool?
    let message: String?
    let data: [CustomAd]?
}

// MARK: - Datum
struct CustomAd: Codable {
    let id: Int?
    let title, brandName, brandLogo, buttonText: String?
    let isAndroid, isIos: Int?
    let androidLink, iosLink: String?
    let startDate, endDate: String?
    let status, views, clicks: Int?
    let createdAt, updatedAt: String?
    let sources: [AdSource]?

    enum CodingKeys: String, CodingKey {
        case id = "custom_ad_id"
        case title
        case brandName = "brand_name"
        case brandLogo = "brand_logo"
        case buttonText = "button_text"
        case isAndroid = "is_android"
        case androidLink = "android_link"
        case isIos = "is_ios"
        case iosLink = "ios_link"
        case startDate = "start_date"
        case endDate = "end_date"
        case status, views, clicks
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case sources
    }
}

// MARK: - Source
struct AdSource: Codable {
    let id, customAdID: Int?
    let type: AdSourceType?
    let content, headline, description: String?
    let showTime, isSkippable: Int?
    let createdAt, updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id = "custom_ad_source_id"
        case customAdID = "custom_ad_id"
        case type, content, headline, description
        case showTime = "show_time"
        case isSkippable = "is_skippable"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}

enum AdSourceType : Int, CaseIterable, Codable {
    case image = 0
    case video = 1
}
