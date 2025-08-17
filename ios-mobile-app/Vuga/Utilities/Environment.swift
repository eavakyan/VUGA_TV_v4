//
//  Environment.swift
//  Vuga
//
//  Centralized environment configuration
//

import Foundation

enum Environment {
    case development
    case staging
    case production
    
    // Current environment - change this when switching environments
    #if DEBUG
    static let current: Environment = .staging  // Use staging even in debug for now
    #else
    static let current: Environment = .staging  // Will become .production when ready
    #endif
    
    var baseURL: String {
        switch self {
        case .development:
            return "http://localhost:8000/"  // Local development
        case .staging:
            return "https://iosdev.gossip-stone.com/"  // Current staging/test
        case .production:
            return "https://api.vugatv.com/"  // Future production (example)
        }
    }
    
    var apiBase: String {
        return baseURL + "api/v2/"
    }
    
    var storageURL: String {
        return baseURL + "public/storage/"
    }
    
    var apiKey: String {
        // Could vary by environment if needed
        return "jpwc3pny"
    }
    
    var name: String {
        switch self {
        case .development:
            return "Development"
        case .staging:
            return "Staging"
        case .production:
            return "Production"
        }
    }
}

// Update WebService to use Environment
extension WebService {
    static var environment: Environment {
        return Environment.current
    }
    
    // Override existing properties to use Environment
    static var environmentBase: String {
        return environment.baseURL
    }
    
    static var environmentApiBase: String {
        return environment.apiBase
    }
    
    static var environmentStorageURL: String {
        return environment.storageURL
    }
}