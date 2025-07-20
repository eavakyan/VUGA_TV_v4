//
//  SearchLiveTvModel.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 06/06/24.
//

import Foundation

struct SearchLiveTvModel: Codable {
    let status: Bool?
    let message: String?
    let data: [Channel]?
}


