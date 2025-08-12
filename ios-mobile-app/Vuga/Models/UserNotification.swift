//
//  UserNotification.swift
//  Vuga
//
//  Model for in-app user notifications
//

import Foundation

struct UserNotificationResponse: Codable {
    let status: Bool
    let data: [UserNotification]?
    let message: String?
}

struct UserNotification: Codable, Identifiable {
    let id: Int
    let title: String
    let message: String
    let notificationType: String
    let priority: String
    let targetPlatforms: [String]?
    let isActive: Bool
    let scheduledAt: String?
    let expiresAt: String?
    let createdAt: String
    
    enum CodingKeys: String, CodingKey {
        case id = "notification_id"
        case title
        case message
        case notificationType = "notification_type"
        case priority
        case targetPlatforms = "target_platforms"
        case isActive = "is_active"
        case scheduledAt = "scheduled_at"
        case expiresAt = "expires_at"
        case createdAt = "created_at"
    }
    
    var priorityColor: String {
        switch priority {
        case "urgent": return "red"
        case "high": return "orange"
        case "medium": return "yellow"
        default: return "gray"
        }
    }
    
    var typeIcon: String {
        switch notificationType {
        case "system": return "gear"
        case "promotional": return "megaphone"
        case "update": return "arrow.up.circle"
        case "maintenance": return "wrench"
        default: return "info.circle"
        }
    }
}

// Mark notification as shown
struct MarkNotificationShownRequest: Codable {
    let profileId: Int
    let notificationId: Int
    
    enum CodingKeys: String, CodingKey {
        case profileId = "profile_id"
        case notificationId = "notification_id"
    }
}

// Dismiss notification
struct DismissNotificationRequest: Codable {
    let profileId: Int
    let notificationId: Int
    
    enum CodingKeys: String, CodingKey {
        case profileId = "profile_id"
        case notificationId = "notification_id"
    }
}