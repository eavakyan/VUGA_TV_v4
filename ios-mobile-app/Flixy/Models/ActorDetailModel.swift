//
//  ActorDetailModel.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 16/07/24.
//

import Foundation

struct ActorDetailModel: Codable {
    let status: Bool?
    let message: String?
    let data: ActorDetail?
}

// MARK: - DataClass
struct ActorDetail: Codable {
    let id: Int?
    let fullname, dob, bio, profileImage: String?
    let createdAt, updatedAt: String?
    let actorContent: [FlixyContent]?

    enum CodingKeys: String, CodingKey {
        case id, fullname, dob, bio
        case profileImage = "profile_image"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case actorContent
    }
}
