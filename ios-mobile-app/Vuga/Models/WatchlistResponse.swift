//
//  WatchlistResponse.swift
//  Vuga
//
//

import Foundation

// MARK: - WatchlistResponse
struct WatchlistResponse: Codable {
    let status: Bool?
    let message: String?
    let isInWatchlist: Bool?
    
    enum CodingKeys: String, CodingKey {
        case status, message
        case isInWatchlist = "is_in_watchlist"
    }
    
    // Custom decoder to handle status as both Bool and Int
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        message = try container.decodeIfPresent(String.self, forKey: .message)
        isInWatchlist = try container.decodeIfPresent(Bool.self, forKey: .isInWatchlist)
        
        // Handle status field that can be either Bool or Int
        if let statusBool = try? container.decodeIfPresent(Bool.self, forKey: .status) {
            status = statusBool
        } else if let statusInt = try? container.decodeIfPresent(Int.self, forKey: .status) {
            status = statusInt == 1
        } else {
            status = false
        }
    }
}