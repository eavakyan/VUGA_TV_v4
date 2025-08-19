//
//  SessionManager.swift
//  thumbsUp
//
//  Created by Aniket Vaddoriya on 11/04/22.
//

import Foundation

extension Notification.Name {
    static let profileChanged = Notification.Name("profileChanged")
}

struct DownloadData: Hashable,Codable {
    var data: Data?
    var sourceId: String
    var sourceURL: URL
    var contentId: Int
    var episodeId: Int
    var destinationName: String
    var profileId: Int
//    var downloadStatus: DownloadStatus
}

class SessionManager: ObservableObject {
    static var shared = SessionManager()
    
    @Published var currentUser: User? {
        didSet {
            if let user = currentUser {
                saveUser(user)
            }
        }
    }
    
    @Published var currentProfile: Profile? {
        didSet {
            if let profile = currentProfile {
                saveProfile(profile)
                NotificationCenter.default.post(name: .profileChanged, object: nil)
            }
        }
    }
    
    init() {
        loadUser()
        loadProfile()
        // Also sync with @AppStorage user if available
        syncWithAppStorage()
    }
    
    func syncWithAppStorage() {
        // Try to load user from @AppStorage if SessionManager doesn't have it
        if currentUser == nil {
            let userDefaults = UserDefaults.standard
            if let appStorageUserData = userDefaults.string(forKey: SessionKeys.myUser),
               let user = Optional<User>(rawValue: appStorageUserData) {
                print("SessionManager: Syncing user from @AppStorage")
                currentUser = user
            }
        }
    }
    
    func setSetting(data: Setting){
        do {
            let data = try JSONEncoder().encode(data)
            let dataString = String(decoding: data, as: UTF8.self)
            setStringValue(value: dataString, key: "setting")
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    func getSetting() -> Setting? {
        let dataString = getStringValueForKey(key: "setting")
        let data = Data(dataString.utf8)
        if let loaded = try? JSONDecoder().decode(Setting.self, from: data) {
            return loaded
        }
        return nil
    }
    
    func setAds(data: [Admob]){
        do {
            let data = try JSONEncoder().encode(data)
            let dataString = String(decoding: data, as: UTF8.self)
            setStringValue(value: dataString, key: "ads")
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    func getAds() -> [Admob] {
        let dataString = getStringValueForKey(key: "ads")
        let data = Data(dataString.utf8)
        if let loaded = try? JSONDecoder().decode([Admob].self, from: data) {
            return loaded
        }
        return []
    }
    
    func setLanguages(data: [ContentLanguage]){
        do {
            print("SessionManager: Storing \(data.count) languages")
            let encodedData = try JSONEncoder().encode(data)
            let dataString = String(decoding: encodedData, as: UTF8.self)
            setStringValue(value: dataString, key: "languages")
            print("SessionManager: Languages stored successfully")
        } catch let err {
            print("SessionManager: Error storing languages: \(err.localizedDescription)")
        }
    }
    
    func getLanguages() -> [ContentLanguage] {
        let dataString = getStringValueForKey(key: "languages")
        print("SessionManager: Retrieved languages string length: \(dataString.count)")
        
        if dataString.isEmpty {
            print("SessionManager: No languages stored in UserDefaults")
            return []
        }
        
        let data = Data(dataString.utf8)
        do {
            let loaded = try JSONDecoder().decode([ContentLanguage].self, from: data)
            print("SessionManager: Successfully decoded \(loaded.count) languages")
            return loaded
        } catch {
            print("SessionManager: Error decoding languages: \(error)")
            return []
        }
    }
    
    func setCustomAds(datum: [CustomAd]){
        do {
            let data = try JSONEncoder().encode(datum)
            UserDefaults.standard.set(data, forKey: "custom_ads")
        } catch let error {
            print(error.localizedDescription)
        }
    }
    
    func getCustomAds() -> [CustomAd] {
        if let data = UserDefaults.standard.data(forKey: "custom_ads"){
            if let loaded = try? JSONDecoder().decode([CustomAd].self, from: data){
                return loaded
            }
        }
        return []
    }
    
    
    func setDownloadData(datum: [DownloadData]){
        do {
            let data = try JSONEncoder().encode(datum)
            UserDefaults.standard.set(data, forKey: "download_data")
        } catch let error {
            print(error.localizedDescription)
        }
    }
    
    func getDownloadData() -> [DownloadData] {
        if let data = UserDefaults.standard.data(forKey: "download_data"){
            if let loaded = try? JSONDecoder().decode([DownloadData].self, from: data){
                return loaded
            }
        }
        return []
    }
    
    func getDownloadDataForCurrentProfile() -> [DownloadData] {
        guard let currentProfileId = currentProfile?.profileId else { return [] }
        return getDownloadData().filter { $0.profileId == currentProfileId }
    }
    
    func getDownloadDataForProfile(profileId: Int) -> [DownloadData] {
        return getDownloadData().filter { $0.profileId == profileId }
    }
    
    func setGenres(data: [Genre]){
        do {
            let data = try JSONEncoder().encode(data)
            let dataString = String(decoding: data, as: UTF8.self)
            setStringValue(value: dataString, key: "genres")
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    func getGenres() -> [Genre]{
        let dataString = getStringValueForKey(key: "genres")
        let data = Data(dataString.utf8)
        if let loaded = try? JSONDecoder().decode([Genre].self, from: data) {
            return loaded
        }
        return []
    }
    
    //MARK: Bool
    func getBooleanValueForKey(key: String) -> Bool {
        return UserDefaults.standard.bool(forKey: key)
    }
    func setBooleanValue(value: Bool, key: String) {
        UserDefaults.standard.set(value, forKey: key)
        // synchronize() is deprecated and unnecessary in iOS 12+
    }
    
    //MARK:  String
    func getStringValueForKey(key: String) -> String {
        if let value = UserDefaults.standard.string(forKey: key) {
            return value
        }
        
        return ""
    }
    func setStringValue(value: String, key: String) {
        UserDefaults.standard.set(value, forKey: key)
        // synchronize() is deprecated and unnecessary in iOS 12+
    }
    
    //MARK: Int
    func getIntegerValueForKey(key: String) -> Int {
        return UserDefaults.standard.integer(forKey: key)
    }
    func setIntegerValue(value: Int, key: String) {
        
        UserDefaults.standard.set(value, forKey: key)
        // synchronize() is deprecated and unnecessary in iOS 12+
    }
    
    //MARK: Float
    func getFloatValueForKey(key: String) -> Float {
        return UserDefaults.standard.float(forKey: key)
    }
    func setFloatValue(value: Float, key: String) {
        UserDefaults.standard.set(value, forKey: key)
        // synchronize() is deprecated and unnecessary in iOS 12+
    }
    
    
    // Force clear languages (for debugging)
    func clearLanguages() {
        print("SessionManager: Clearing stored languages")
        UserDefaults.standard.removeObject(forKey: "languages")
        UserDefaults.standard.synchronize()
    }
    
    //MARK: Clear
    func clear() {
        // Clear current profile first
        clearProfile()
        
        // Clear all UserDefaults
        let domain = Bundle.main.bundleIdentifier!
        UserDefaults.standard.removePersistentDomain(forName: domain)
        // synchronize() is deprecated and unnecessary in iOS 12+
        
        // Reset published properties to nil
        currentUser = nil
        currentProfile = nil
    }
    
    // MARK: - User Management
    func saveUser(_ user: User) {
        do {
            let data = try JSONEncoder().encode(user)
            UserDefaults.standard.set(data, forKey: "currentUser")
        } catch {
            print("Failed to save user: \(error)")
        }
    }
    
    func loadUser() {
        if let data = UserDefaults.standard.data(forKey: "currentUser") {
            do {
                currentUser = try JSONDecoder().decode(User.self, from: data)
            } catch {
                print("Failed to load user: \(error)")
            }
        }
    }
    
    // MARK: - Profile Management
    func saveProfile(_ profile: Profile) {
        do {
            let data = try JSONEncoder().encode(profile)
            UserDefaults.standard.set(data, forKey: "currentProfile")
        } catch {
            print("Failed to save profile: \(error)")
        }
    }
    
    func loadProfile() {
        if let data = UserDefaults.standard.data(forKey: "currentProfile") {
            do {
                currentProfile = try JSONDecoder().decode(Profile.self, from: data)
            } catch {
                print("Failed to load profile: \(error)")
            }
        }
    }
    
    func clearProfile() {
        currentProfile = nil
        UserDefaults.standard.removeObject(forKey: "currentProfile")
    }
    
    func getCurrentProfile() -> Profile? {
        return currentProfile
    }
    
    func setCurrentProfile(_ profile: Profile) {
        currentProfile = profile
    }
    
    // MARK: - Profile Caching
    func cacheProfiles(_ profiles: [Profile]) {
        do {
            let data = try JSONEncoder().encode(profiles)
            UserDefaults.standard.set(data, forKey: "cachedProfiles")
        } catch {
            print("Failed to cache profiles: \(error)")
        }
    }
    
    func getCachedProfiles() -> [Profile]? {
        if let data = UserDefaults.standard.data(forKey: "cachedProfiles") {
            do {
                return try JSONDecoder().decode([Profile].self, from: data)
            } catch {
                print("Failed to load cached profiles: \(error)")
            }
        }
        return nil
    }
    
    func clearCachedProfiles() {
        UserDefaults.standard.removeObject(forKey: "cachedProfiles")
    }
}
