//
//  TVCategoriesModel.swift
//  Vuga
//
//

import Foundation

// MARK: - TVCategoriesModel
struct TVCategoriesModel: Codable {
    let status: Bool?
    let message: String?
    let data: [TVCategory]?
}

// MARK: - TVCategoryModel
struct TVCategoryModel: Codable {
    let status: Bool?
    let message: String?
    let data: TVCategory?
}

// MARK: - TVCategory
struct TVCategory: Codable {
    let id: Int?
    let title: String?
    let image: String?
    let createdAt: String?
    let updatedAt: String?
    let channels: [Channel]?

    enum CodingKeys: String, CodingKey {
        case id, title, image
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case channels
    }
}

// MARK: - Channel
struct Channel: Codable, Hashable, Identifiable {
    let id: Int?
    let title, thumbnail: String?
    let accessType: AccessType?
    let categoryID: String?
    let type: SourceType?
    let source: String?
//    let totalView, totalShare: Int?
    let createdAt, updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id, title, thumbnail
        case accessType = "access_type"
        case categoryID = "category_ids"
        case type, source
//        case totalView = "total_view"
//        case totalShare = "total_share"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}
