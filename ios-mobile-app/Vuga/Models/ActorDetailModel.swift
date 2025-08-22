//
//  ActorDetailModel.swift
//  Vuga
//
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
    let actorContent: [VugaContent]?

    enum CodingKeys: String, CodingKey {
        case id = "actor_id"
        case fullname, dob, bio
        case profileImage = "profile_image"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case actorContent
    }
}
