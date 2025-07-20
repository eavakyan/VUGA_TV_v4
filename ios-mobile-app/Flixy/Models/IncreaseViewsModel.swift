//
//  IncreaseViewsModel.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 12/06/24.
//

import Foundation

struct IncreaseContentViewModel: Codable {
    let status: Bool?
    let message: String?
    let data: FlixyContent?
}

struct IncreaseEpisodeViewsModel : Codable {
    let status: Bool?
    let message: String?
    let data: Episode?
}
