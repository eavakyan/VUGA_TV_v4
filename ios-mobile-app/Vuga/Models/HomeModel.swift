//
//  HomeModel.swift
//  Vuga
//
//

import Foundation



// MARK: - Genre
struct Genre: Codable {
    let id: Int?
    let title, createdAt: String?
    let updatedAt: String?
    let contents: [VugaContent]?

    enum CodingKeys: String, CodingKey {
        case id
        case title
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case contents
    }
}

struct HomeModel: Codable {
    let status: Bool?
    let message: String?
    let featured, watchlist: [VugaContent]?
    let topContents: [TopContent]?
    let genreContents: [Genre]?
}



// MARK: - TopContent
struct TopContent: Codable {
    let id, contentIndex, contentID: Int?
    let createdAt: String?
    let updatedAt: String?
    let content: VugaContent?

    enum CodingKeys: String, CodingKey {
        case id = "top_content_id"
        case contentIndex = "content_index"
        case contentID = "content_id"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case content
    }
}

enum UpdatedAt: String, Codable {
    case the20240716T065035000000Z = "2024-07-16T06:50:35.000000Z"
}
