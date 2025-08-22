//
//  IncreaseViewsModel.swift
//  Vuga
//
//

import Foundation

struct IncreaseContentViewModel: Codable {
    let status: Bool?
    let message: String?
    let data: VugaContent?
}

struct IncreaseEpisodeViewsModel : Codable {
    let status: Bool?
    let message: String?
    let data: Episode?
}
