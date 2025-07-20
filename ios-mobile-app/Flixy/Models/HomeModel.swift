//
//  HomeModel.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 15/05/24.
//

import Foundation



// MARK: - Genre
struct Genre: Codable {
    let id: Int?
    let title, createdAt: String?
    let updatedAt: String?
    let contents: [FlixyContent]?

    enum CodingKeys: String, CodingKey {
        case id, title
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case contents
    }
}

struct HomeModel: Codable {
    let status: Bool?
    let message: String?
    let featured, watchlist: [FlixyContent]?
    let topContents: [TopContent]?
    let genreContents: [Genre]?
}



// MARK: - TopContent
struct TopContent: Codable {
    let id, contentIndex, contentID: Int?
    let createdAt: String?
    let updatedAt: String?
    let content: FlixyContent?

    enum CodingKeys: String, CodingKey {
        case id
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
