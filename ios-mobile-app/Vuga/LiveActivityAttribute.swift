//
//  LiveActivityAttribute.swift
//  Vuga
//
//

import Foundation
import ActivityKit

struct VugaLiveActivityAttributes: ActivityAttributes, Identifiable, Equatable {
    public typealias LiveDeliveryData = ContentState

    public struct ContentState: Codable, Hashable {
        var status: String
        var progress: Double
    }
    var imageUrl: String
    var downloadId: String
    var id = UUID()
    var contentName: String
    var contentId: Int
    var contentResolution: String
    var contentSize: String
    var contentType: String
    var seasonEpisodeName: String
}


