//
//  SimpleOfflineManager.swift
//  Vuga
//
//  Handles offline caching of profile and user data
//

import Foundation

class SimpleOfflineManager {
    static let shared = SimpleOfflineManager()
    private let userDefaults = UserDefaults.standard
    
    // Cache keys
    private let profilesCacheKey = "cached_profiles"
    private let userDataCacheKey = "cached_user_data"
    private let lastSyncKey = "last_sync_time"
    
    // Cache validity duration (24 hours)
    private let cacheValidityDuration: TimeInterval = 24 * 60 * 60
    
    private init() {}
    
    // MARK: - Profile Caching
    
    func cacheProfiles(_ profiles: [ProfileModel]) {
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(profiles)
            userDefaults.set(data, forKey: profilesCacheKey)
            updateLastSyncTime()
        } catch {
            print("Failed to cache profiles: \(error)")
        }
    }
    
    func getCachedProfiles() -> [ProfileModel]? {
        guard isCacheValid() else { return nil }
        
        guard let data = userDefaults.data(forKey: profilesCacheKey) else {
            return nil
        }
        
        do {
            let decoder = JSONDecoder()
            return try decoder.decode([ProfileModel].self, from: data)
        } catch {
            print("Failed to decode cached profiles: \(error)")
            return nil
        }
    }
    
    // MARK: - User Data Caching
    
    func cacheUserData(_ userData: User) {
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(userData)
            userDefaults.set(data, forKey: userDataCacheKey)
            updateLastSyncTime()
        } catch {
            print("Failed to cache user data: \(error)")
        }
    }
    
    func getCachedUserData() -> User? {
        guard isCacheValid() else { return nil }
        
        guard let data = userDefaults.data(forKey: userDataCacheKey) else {
            return nil
        }
        
        do {
            let decoder = JSONDecoder()
            return try decoder.decode(User.self, from: data)
        } catch {
            print("Failed to decode cached user data: \(error)")
            return nil
        }
    }
    
    // MARK: - Cache Management
    
    func isCacheValid() -> Bool {
        guard let lastSync = userDefaults.object(forKey: lastSyncKey) as? Date else {
            return false
        }
        
        let timeSinceSync = Date().timeIntervalSince(lastSync)
        return timeSinceSync < cacheValidityDuration
    }
    
    func clearCache() {
        userDefaults.removeObject(forKey: profilesCacheKey)
        userDefaults.removeObject(forKey: userDataCacheKey)
        userDefaults.removeObject(forKey: lastSyncKey)
    }
    
    private func updateLastSyncTime() {
        userDefaults.set(Date(), forKey: lastSyncKey)
    }
    
    // MARK: - Network Status Helper
    
    func shouldUseCachedData() -> Bool {
        // Use cached data if offline or cache is still valid
        if !ConnectionMonitor.shared.isConnected {
            return true
        }
        
        // Use cache if connection is poor and cache is valid
        if ConnectionMonitor.shared.connectionQuality == .poor && isCacheValid() {
            return true
        }
        
        return false
    }
}