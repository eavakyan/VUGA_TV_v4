import Foundation
import UIKit

struct Profile: Codable, Equatable {
    let profileId: Int
    let appUserId: Int?
    let name: String
    let avatarType: String
    let avatarUrl: String?
    let avatarColor: String
    let avatarId: Int?
    let isKids: Bool
    let isActive: Bool?
    let createdAt: String?
    let updatedAt: String?
    
    enum CodingKeys: String, CodingKey {
        case profileId = "profile_id"
        case appUserId = "app_user_id"
        case name
        case avatarType = "avatar_type"
        case avatarUrl = "avatar_url"
        case avatarColor = "avatar_color"
        case avatarId = "avatar_id"
        case isKids = "is_kids"
        case isActive = "is_active"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
    
    // Custom decoder to handle both Int and Bool for isKids
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        profileId = try container.decode(Int.self, forKey: .profileId)
        appUserId = try container.decodeIfPresent(Int.self, forKey: .appUserId)
        name = try container.decode(String.self, forKey: .name)
        avatarType = try container.decode(String.self, forKey: .avatarType)
        avatarUrl = try container.decodeIfPresent(String.self, forKey: .avatarUrl)
        avatarColor = try container.decode(String.self, forKey: .avatarColor)
        avatarId = try container.decodeIfPresent(Int.self, forKey: .avatarId)
        
        // Handle isKids as either Bool or Int
        if let isKidsBool = try? container.decode(Bool.self, forKey: .isKids) {
            isKids = isKidsBool
        } else if let isKidsInt = try? container.decode(Int.self, forKey: .isKids) {
            isKids = isKidsInt == 1
        } else {
            isKids = false
        }
        
        isActive = try container.decodeIfPresent(Bool.self, forKey: .isActive)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
        updatedAt = try container.decodeIfPresent(String.self, forKey: .updatedAt)
    }
    
    // Custom encoder to encode isKids as Int
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        
        try container.encode(profileId, forKey: .profileId)
        try container.encodeIfPresent(appUserId, forKey: .appUserId)
        try container.encode(name, forKey: .name)
        try container.encode(avatarType, forKey: .avatarType)
        try container.encodeIfPresent(avatarUrl, forKey: .avatarUrl)
        try container.encode(avatarColor, forKey: .avatarColor)
        try container.encodeIfPresent(avatarId, forKey: .avatarId)
        try container.encode(isKids ? 1 : 0, forKey: .isKids) // Encode as Int
        try container.encodeIfPresent(isActive, forKey: .isActive)
        try container.encodeIfPresent(createdAt, forKey: .createdAt)
        try container.encodeIfPresent(updatedAt, forKey: .updatedAt)
    }
    
    // Helper to check if it's a kids profile
    var isKidsProfile: Bool {
        return isKids
    }
    
    // Helper to get the display initial
    var initial: String {
        return String(name.prefix(1)).uppercased()
    }
    
    // Helper to get UIColor from hex string
    var color: UIColor {
        return UIColor(hex: avatarColor) ?? UIColor.systemBlue
    }
}

struct ProfileResponse: Codable {
    let status: Bool
    let message: String
    let profiles: [Profile]?
    let profile: Profile?
}

// Extension to convert hex string to UIColor
extension UIColor {
    convenience init?(hex: String) {
        let r, g, b: CGFloat
        var hexColor = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        hexColor = hexColor.replacingOccurrences(of: "#", with: "")
        
        if hexColor.count == 6 {
            let scanner = Scanner(string: hexColor)
            var hexNumber: UInt64 = 0
            
            if scanner.scanHexInt64(&hexNumber) {
                r = CGFloat((hexNumber & 0xff0000) >> 16) / 255
                g = CGFloat((hexNumber & 0x00ff00) >> 8) / 255
                b = CGFloat((hexNumber & 0x0000ff)) / 255
                
                self.init(red: r, green: g, blue: b, alpha: 1.0)
                return
            }
        }
        
        return nil
    }
    
    func toHexString() -> String {
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var a: CGFloat = 0
        
        getRed(&r, green: &g, blue: &b, alpha: &a)
        
        let rgb: Int = (Int)(r*255)<<16 | (Int)(g*255)<<8 | (Int)(b*255)<<0
        
        return String(format: "#%06x", rgb)
    }
}