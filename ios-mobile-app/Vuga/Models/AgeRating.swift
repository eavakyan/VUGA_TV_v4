import Foundation

struct AgeRating: Codable, Identifiable, Equatable {
    let id: Int
    let name: String?  // Made optional to handle missing data
    let minAge: Int
    let maxAge: Int?
    let description: String?
    let code: String
    let createdAt: String?
    let updatedAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id = "age_limit_id"
        case name
        case minAge = "min_age"
        case maxAge = "max_age"
        case description
        case code
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
    
    // Helper to check if this rating is appropriate for a given age
    func isAppropriateFor(age: Int?) -> Bool {
        guard let age = age else { return true }
        return age >= minAge
    }
    
    // Helper to check if this rating is appropriate for kids profiles
    var isKidsFriendly: Bool {
        // Kids profiles can access content for ages 12 and under
        if let maxAge = maxAge {
            return maxAge <= 12
        }
        return false
    }
    
    // Helper to get display color based on age group
    var displayColor: String {
        switch code {
        case "AG_0_6":
            return "#4CAF50" // Green
        case "AG_7_12":
            return "#8BC34A" // Light Green
        case "AG_13_16":
            return "#FF9800" // Orange
        case "AG_17_18":
            return "#F44336" // Red
        case "AG_18_PLUS":
            return "#9C27B0" // Purple
        default:
            return "#757575" // Gray
        }
    }
}

struct AgeRatingResponse: Codable {
    let status: Bool
    let message: String
    let ageRatings: [AgeRating]?
    
    enum CodingKeys: String, CodingKey {
        case status
        case message
        case ageRatings = "age_ratings"
    }
}

// Response for updating profile age settings
struct ProfileAgeUpdateResponse: Codable {
    let status: Bool
    let message: String
    let profile: Profile?
}