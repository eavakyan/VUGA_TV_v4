//
//  OfflineDataManager.swift
//  Vuga
//
//  Enhanced offline data management with intelligent caching and sync
//

import Foundation
import Combine

enum CachePolicy {
    case networkFirst      // Try network first, fallback to cache
    case cacheFirst       // Use cache first, update in background
    case networkOnly      // Network only, fail if no connection
    case cacheOnly        // Cache only, never make network calls
}

enum CacheType {
    case user
    case profiles
    case homeContent
    case contentDetail(Int)
    case watchlist
    case recentlyWatched
    case downloadedContent
    case settings
    case languages
    case genres
}

struct CacheEntry<T: Codable>: Codable {
    let data: T
    let timestamp: TimeInterval
    let expiryDate: Date
    
    init(data: T, ttl: TimeInterval = 3600) { // 1 hour default TTL
        self.data = data
        self.timestamp = Date().timeIntervalSince1970
        self.expiryDate = Date().addingTimeInterval(ttl)
    }
    
    var isExpired: Bool {
        return Date() > expiryDate
    }
    
    var age: TimeInterval {
        return Date().timeIntervalSince1970 - timestamp
    }
}

class OfflineDataManager: ObservableObject {
    static let shared = OfflineDataManager()
    
    private let fileManager = FileManager.default
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()
    private let connectionMonitor = ConnectionMonitor.shared
    
    @Published var isOfflineMode: Bool = false
    @Published var offlineMessage: String = ""
    @Published var cachedDataAvailable: Set<CacheType> = []
    
    private var cancellables = Set<AnyCancellable>()
    
    // Cache TTL settings (in seconds)
    private let cacheTTL: [CacheType: TimeInterval] = [
        .user: 86400,              // 24 hours
        .profiles: 86400,          // 24 hours
        .homeContent: 3600,        // 1 hour
        .watchlist: 1800,          // 30 minutes
        .recentlyWatched: 1800,    // 30 minutes
        .downloadedContent: 604800, // 7 days
        .settings: 86400,          // 24 hours
        .languages: 604800,        // 7 days
        .genres: 604800            // 7 days
    ]
    
    private init() {
        setupCacheDirectory()
        monitorNetworkStatus()
        updateCachedDataAvailability()
    }
    
    private func setupCacheDirectory() {
        let cacheDir = getCacheDirectory()
        if !fileManager.fileExists(atPath: cacheDir.path) {
            try? fileManager.createDirectory(at: cacheDir, withIntermediateDirectories: true)
        }
    }
    
    private func getCacheDirectory() -> URL {
        return fileManager.urls(for: .cachesDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("VugaOfflineData")
    }
    
    private func monitorNetworkStatus() {
        connectionMonitor.$isConnected
            .combineLatest(connectionMonitor.$connectionQuality)
            .sink { [weak self] isConnected, quality in
                self?.updateOfflineStatus(isConnected: isConnected, quality: quality)
            }
            .store(in: &cancellables)
    }
    
    private func updateOfflineStatus(isConnected: Bool, quality: ConnectionQuality) {
        DispatchQueue.main.async {
            if !isConnected {
                self.isOfflineMode = true
                self.offlineMessage = "You're offline. Using cached data."
            } else if quality == .poor {
                self.isOfflineMode = false
                self.offlineMessage = "Slow connection. Some features may be limited."
            } else {
                self.isOfflineMode = false
                self.offlineMessage = ""
            }
        }
    }
    
    // MARK: - Generic Cache Operations
    
    func cache<T: Codable>(_ data: T, for type: CacheType, ttl: TimeInterval? = nil) {
        let effectiveTTL = ttl ?? cacheTTL[type] ?? 3600
        let entry = CacheEntry(data: data, ttl: effectiveTTL)
        
        do {
            let encodedData = try encoder.encode(entry)
            let url = getCacheURL(for: type)
            try encodedData.write(to: url)
            
            DispatchQueue.main.async {
                self.cachedDataAvailable.insert(type)
            }
            
            print("OfflineDataManager: Cached \(type) successfully")
        } catch {
            print("OfflineDataManager: Failed to cache \(type): \(error)")
        }
    }
    
    func getCachedData<T: Codable>(for type: CacheType, as dataType: T.Type) -> T? {
        let url = getCacheURL(for: type)
        
        guard fileManager.fileExists(atPath: url.path) else {
            return nil
        }
        
        do {
            let data = try Data(contentsOf: url)
            let entry = try decoder.decode(CacheEntry<T>.self, from: data)
            
            if entry.isExpired {
                clearCache(for: type)
                return nil
            }
            
            return entry.data
        } catch {
            print("OfflineDataManager: Failed to load cached \(type): \(error)")
            return nil
        }
    }
    
    func clearCache(for type: CacheType) {
        let url = getCacheURL(for: type)
        try? fileManager.removeItem(at: url)
        
        DispatchQueue.main.async {
            self.cachedDataAvailable.remove(type)
        }
    }
    
    func clearAllCache() {
        let cacheDir = getCacheDirectory()
        try? fileManager.removeItem(at: cacheDir)
        setupCacheDirectory()
        
        DispatchQueue.main.async {
            self.cachedDataAvailable.removeAll()
        }
    }
    
    private func getCacheURL(for type: CacheType) -> URL {
        let filename: String
        switch type {
        case .user:
            filename = "user.json"
        case .profiles:
            filename = "profiles.json"
        case .homeContent:
            filename = "homeContent.json"
        case .contentDetail(let id):
            filename = "content_\(id).json"
        case .watchlist:
            filename = "watchlist.json"
        case .recentlyWatched:
            filename = "recentlyWatched.json"
        case .downloadedContent:
            filename = "downloadedContent.json"
        case .settings:
            filename = "settings.json"
        case .languages:
            filename = "languages.json"
        case .genres:
            filename = "genres.json"
        }
        return getCacheDirectory().appendingPathComponent(filename)
    }
    
    // MARK: - Specific Data Caching Methods
    
    func cacheUser(_ user: User) {
        cache(user, for: .user)
    }
    
    func getCachedUser() -> User? {
        return getCachedData(for: .user, as: User.self)
    }
    
    func cacheProfiles(_ profiles: [Profile]) {
        cache(profiles, for: .profiles)
    }
    
    func getCachedProfiles() -> [Profile]? {
        return getCachedData(for: .profiles, as: [Profile].self)
    }
    
    func cacheHomeContent(_ homeData: HomeModel) {
        cache(homeData, for: .homeContent)
    }
    
    func getCachedHomeContent() -> HomeModel? {
        return getCachedData(for: .homeContent, as: HomeModel.self)
    }
    
    func cacheContentDetail(_ content: VugaContent, id: Int) {
        cache(content, for: .contentDetail(id))
    }
    
    func getCachedContentDetail(id: Int) -> VugaContent? {
        return getCachedData(for: .contentDetail(id), as: VugaContent.self)
    }
    
    func cacheWatchlist(_ watchlist: [VugaContent]) {
        cache(watchlist, for: .watchlist)
    }
    
    func getCachedWatchlist() -> [VugaContent]? {
        return getCachedData(for: .watchlist, as: [VugaContent].self)
    }
    
    func cacheRecentlyWatched(_ recentlyWatched: [RecentlyWatchedContent]) {
        cache(recentlyWatched, for: .recentlyWatched)
    }
    
    func getCachedRecentlyWatched() -> [RecentlyWatchedContent]? {
        return getCachedData(for: .recentlyWatched, as: [RecentlyWatchedContent].self)
    }
    
    func cacheLanguages(_ languages: [ContentLanguage]) {
        cache(languages, for: .languages)
    }
    
    func getCachedLanguages() -> [ContentLanguage]? {
        return getCachedData(for: .languages, as: [ContentLanguage].self)
    }
    
    func cacheGenres(_ genres: [Genre]) {
        cache(genres, for: .genres)
    }
    
    func getCachedGenres() -> [Genre]? {
        return getCachedData(for: .genres, as: [Genre].self)
    }
    
    // MARK: - Cache Statistics and Management
    
    private func updateCachedDataAvailability() {
        let allTypes: [CacheType] = [.user, .profiles, .homeContent, .watchlist, .recentlyWatched, .downloadedContent, .settings, .languages, .genres]
        
        var available = Set<CacheType>()
        for type in allTypes {
            let url = getCacheURL(for: type)
            if fileManager.fileExists(atPath: url.path) {
                // Check if cache is not expired
                if let data = try? Data(contentsOf: url),
                   let entry = try? decoder.decode(CacheEntry<Data>.self, from: data),
                   !entry.isExpired {
                    available.insert(type)
                }
            }
        }
        
        DispatchQueue.main.async {
            self.cachedDataAvailable = available
        }
    }
    
    func getCacheSize() -> Int64 {
        let cacheDir = getCacheDirectory()
        guard let enumerator = fileManager.enumerator(at: cacheDir, includingPropertiesForKeys: [.fileSizeKey]) else {
            return 0
        }
        
        var totalSize: Int64 = 0
        for case let fileURL as URL in enumerator {
            guard let resourceValues = try? fileURL.resourceValues(forKeys: [.fileSizeKey]),
                  let fileSize = resourceValues.fileSize else {
                continue
            }
            totalSize += Int64(fileSize)
        }
        
        return totalSize
    }
    
    func getCacheSizeFormatted() -> String {
        return StorageManager.formatBytes(getCacheSize())
    }
    
    // MARK: - Offline Content Suggestions
    
    func getOfflineContentSuggestions() -> [String] {
        var suggestions: [String] = []
        
        if cachedDataAvailable.contains(.downloadedContent) {
            suggestions.append("View your downloaded movies and shows")
        }
        
        if cachedDataAvailable.contains(.recentlyWatched) {
            suggestions.append("Resume watching from your history")
        }
        
        if cachedDataAvailable.contains(.watchlist) {
            suggestions.append("Browse your watchlist")
        }
        
        if cachedDataAvailable.contains(.profiles) {
            suggestions.append("Switch between user profiles")
        }
        
        if suggestions.isEmpty {
            suggestions.append("Download content for offline viewing")
        }
        
        return suggestions
    }
}

// MARK: - Extensions for easier usage

extension OfflineDataManager {
    func isDataCached(for type: CacheType) -> Bool {
        return cachedDataAvailable.contains(type)
    }
    
    func getCacheAge(for type: CacheType) -> TimeInterval? {
        let url = getCacheURL(for: type)
        
        guard fileManager.fileExists(atPath: url.path) else {
            return nil
        }
        
        do {
            let data = try Data(contentsOf: url)
            let entry = try decoder.decode(CacheEntry<Data>.self, from: data)
            return entry.age
        } catch {
            return nil
        }
    }
    
    func shouldRefreshCache(for type: CacheType, maxAge: TimeInterval = 3600) -> Bool {
        guard let age = getCacheAge(for: type) else {
            return true // No cache, should refresh
        }
        return age > maxAge
    }
}