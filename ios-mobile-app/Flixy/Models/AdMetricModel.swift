//
//  AdMetricModel.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 19/06/24.
//

import Foundation

struct AdMetricModel: Codable {
    let status: Bool?
    let message: String?
    let data: DataClass?
}

// MARK: - DataClass
struct DataClass: Codable {
    let id: Int?
    let title, brandName, brandLogo, buttonText: String?
    let isAndroid: Int?
    let androidLink: String?
    let isIos: Int?
    let iosLink, startDate, endDate: String?
    let status, views, clicks: Int?
    let createdAt, updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id, title
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
    }
}
