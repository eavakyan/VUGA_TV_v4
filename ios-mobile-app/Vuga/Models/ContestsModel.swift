//
//  ContestsModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 22/05/24.
//

import Foundation

// MARK: - ContentsModel
struct ContentsModel: Codable {
    let status: Bool?
    let message: String?
    let data: [VugaContent]?
}
