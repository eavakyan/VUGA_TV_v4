import Foundation
import CoreData

/**
 * Simple service to sync watch history between local Core Data and server
 * Provides hybrid local-first storage with periodic API sync
 */
class WatchHistorySyncService {
    static let shared = WatchHistorySyncService()
    
    private let debounceInterval: TimeInterval = 300 // 5 minutes
    private var lastSyncAttempt: Date?
    private var isSyncing = false
    
    private init() {}
    
    // MARK: - Public Methods
    
    /**
     * Trigger a debounced sync - used when watch history is updated
     */
    func triggerSync() {
        // Only sync if user is logged in and has active profile
        let sessionManager = SessionManager.shared
        sessionManager.loadUser()
        
        guard let user = sessionManager.currentUser,
              let profileId = user.lastActiveProfileId,
              profileId > 0 else {
            print("WatchHistorySyncService: No user or profile, skipping sync")
            return
        }
        
        // Debounce mechanism - don't sync too frequently
        let now = Date()
        if let lastAttempt = lastSyncAttempt,
           now.timeIntervalSince(lastAttempt) < debounceInterval {
            print("WatchHistorySyncService: Debouncing sync, last attempt too recent")
            return
        }
        
        lastSyncAttempt = now
        
        // Perform sync in background
        DispatchQueue.global(qos: .utility).async {
            self.performSync()
        }
    }
    
    /**
     * Force sync - used when app is closing or backgrounding
     */
    func forceSync() {
        let sessionManager = SessionManager.shared
        sessionManager.loadUser()
        
        guard let user = sessionManager.currentUser,
              let profileId = user.lastActiveProfileId,
              profileId > 0 else {
            return
        }
        
        // Perform sync with shorter timeout
        performSync(timeout: 10.0)
    }
    
    // MARK: - Private Methods
    
    private func performSync(timeout: TimeInterval = 30.0) {
        guard !isSyncing else {
            print("WatchHistorySyncService: Sync already in progress")
            return
        }
        
        isSyncing = true
        defer { isSyncing = false }
        
        let sessionManager = SessionManager.shared
        sessionManager.loadUser()
        
        guard let user = sessionManager.currentUser,
              let profileId = user.lastActiveProfileId else {
            print("WatchHistorySyncService: No user or profile for sync")
            return
        }
        
        print("WatchHistorySyncService: Starting sync for profile \(profileId)")
        
        // Get local watch history
        let localHistory = getLocalWatchHistory()
        guard !localHistory.isEmpty else {
            print("WatchHistorySyncService: No local history to sync")
            return
        }
        
        // Convert to sync format
        let syncItems = convertToSyncFormat(localHistory)
        guard !syncItems.isEmpty else {
            print("WatchHistorySyncService: No valid items to sync")
            return
        }
        
        // Prepare request body as JSON with replace mode
        let requestBody: [String: Any] = [
            "profile_id": profileId,
            "watch_history": syncItems,
            "sync_mode": "replace" // Replace all server items with current local state
        ]
        
        // Debug: Log what we're sending
        print("WatchHistorySyncService: Preparing to sync \(syncItems.count) items")
        if let jsonString = try? JSONSerialization.data(withJSONObject: requestBody, options: .prettyPrinted),
           let debugString = String(data: jsonString, encoding: .utf8) {
            print("WatchHistorySyncService: Request body: \(debugString)")
        }
        
        // Use JSON encoding for proper array handling
        guard let jsonData = try? JSONSerialization.data(withJSONObject: requestBody, options: []) else {
            print("WatchHistorySyncService: Failed to encode JSON")
            return
        }
        
        // Create URL request - WebService.apiBase already includes /api/v2/
        guard let url = URL(string: "\(WebService.apiBase)watch/sync") else {
            print("WatchHistorySyncService: Invalid URL")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(WebService.headerValue, forHTTPHeaderField: WebService.headerKey)
        request.httpBody = jsonData
        request.timeoutInterval = timeout
        
        // Make API call
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("WatchHistorySyncService: Network error - \(error)")
                return
            }
            
            guard let data = data else {
                print("WatchHistorySyncService: No response data")
                return
            }
            
            // Debug: Log raw response
            if let responseString = String(data: data, encoding: .utf8) {
                print("WatchHistorySyncService: Raw response: \(responseString)")
            }
            
            do {
                if let json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
                    print("WatchHistorySyncService: Parsed response: \(json)")
                    
                    if let status = json["status"] as? Bool {
                        if status {
                            print("WatchHistorySyncService: Sync successful")
                            if let responseData = json["data"] as? [String: Any] {
                                print("WatchHistorySyncService: Sync result - deleted: \(responseData["deleted"] ?? 0), synced: \(responseData["synced_new"] ?? 0), updated: \(responseData["updated_existing"] ?? 0)")
                            }
                            UserDefaults.standard.set(Date(), forKey: "last_watch_history_sync")
                        } else {
                            let message = json["message"] as? String ?? "Unknown error"
                            print("WatchHistorySyncService: Sync failed - \(message)")
                            if let errors = json["errors"] {
                                print("WatchHistorySyncService: Errors: \(errors)")
                            }
                        }
                    }
                } else {
                    print("WatchHistorySyncService: Invalid response format")
                }
            } catch {
                print("WatchHistorySyncService: Failed to parse response - \(error)")
            }
        }.resume()
    }
    
    private func getLocalWatchHistory() -> [RecentlyWatched] {
        let context = DataController.shared.context
        let request: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
        request.sortDescriptors = [NSSortDescriptor(key: "date", ascending: false)]
        request.fetchLimit = 100 // Limit to most recent 100 items
        
        do {
            let results = try context.fetch(request)
            print("WatchHistorySyncService: Fetched \(results.count) local watch history items")
            return results
        } catch {
            print("WatchHistorySyncService: Error fetching local history - \(error)")
            return []
        }
    }
    
    private func convertToSyncFormat(_ localHistory: [RecentlyWatched]) -> [[String: Any]] {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        var converted = 0
        var skipped = 0
        
        let results: [[String: Any]] = localHistory.compactMap { item -> [String: Any]? in
            let contentId = Int(item.contentID)
            guard contentId > 0 else {
                skipped += 1
                print("WatchHistorySyncService: Skipping item - invalid contentID: \(item.contentID)")
                return nil
            }
            
            let lastPosition = item.progress
            guard lastPosition > 0 else {
                skipped += 1
                print("WatchHistorySyncService: Skipping item - zero progress for content \(contentId)")
                return nil
            }
            
            var syncItem: [String: Any] = [
                "content_id": contentId,
                "last_watched_position": Int(lastPosition),
                "device_type": 1 // 1 = mobile
            ]
            
            if item.totalDuration > 0 {
                syncItem["total_duration"] = Int(item.totalDuration)
                // Mark as completed if 90% watched
                let percentage = (lastPosition / item.totalDuration) * 100
                syncItem["completed"] = percentage >= 90
            }
            
            if let watchedAt = item.date {
                syncItem["watched_at"] = dateFormatter.string(from: watchedAt)
            } else {
                syncItem["watched_at"] = dateFormatter.string(from: Date())
            }
            
            converted += 1
            return syncItem
        }
        
        print("WatchHistorySyncService: Converted \(converted) items, skipped \(skipped) items")
        return results
    }
}

// MARK: - Basic Response Model

struct BasicResponse: Codable {
    let status: Bool
    let message: String
}