//
//  ContestsModel.swift
//  Vuga
//
//

import Foundation

// MARK: - ContentsModel
struct ContentsModel: Codable {
    let status: Bool?
    let message: String?
    let data: [VugaContent]?
}
