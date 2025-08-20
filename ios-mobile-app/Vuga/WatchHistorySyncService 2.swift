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
        
        guard let user = sessionManager.user,
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
        
        guard let user = sessionManager.user,
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
        
        guard let user = sessionManager.user,
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
        
        // Prepare request
        let params: [Params: Any] = [
            .profileId: profileId,
            .watchHistory: syncItems
        ]
        
        // Make API call
        NetworkManager.callWebService(
            url: .syncWatchHistory,
            httpMethod: .post,
            params: params,
            timeout: timeout,
            callbackSuccess: { (response: BasicResponse) in
                print("WatchHistorySyncService: Sync successful")
                
                // Save last sync time
                UserDefaults.standard.set(Date(), forKey: "last_watch_history_sync")
            },
            callbackFailure: { error in
                print("WatchHistorySyncService: Sync failed - \(error)")
            }
        )
    }
    
    private func getLocalWatchHistory() -> [RecentlyWatched] {
        let context = DataController.shared.context
        let request: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
        request.sortDescriptors = [NSSortDescriptor(key: "date", ascending: false)]
        request.fetchLimit = 100 // Limit to most recent 100 items
        
        do {
            return try context.fetch(request)
        } catch {
            print("WatchHistorySyncService: Error fetching local history - \(error)")
            return []
        }
    }
    
    private func convertToSyncFormat(_ localHistory: [RecentlyWatched]) -> [[String: Any]] {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        return localHistory.compactMap { item in
            let contentId = Int(item.contentID)
            guard contentId > 0,
                  let lastPosition = item.position,
                  Int(lastPosition) > 0 else {
                return nil
            }
            
            var syncItem: [String: Any] = [
                "content_id": contentId,
                "last_watched_position": Int(lastPosition),
                "device_type": 1 // 1 = mobile
            ]
            
            if let duration = item.duration, Int(duration) > 0 {
                syncItem["total_duration"] = Int(duration)
                // Mark as completed if 90% watched
                let percentage = Double(lastPosition) / Double(duration) * 100
                syncItem["completed"] = percentage >= 90
            }
            
            if let watchedAt = item.date {
                syncItem["watched_at"] = dateFormatter.string(from: watchedAt)
            } else {
                syncItem["watched_at"] = dateFormatter.string(from: Date())
            }
            
            return syncItem
        }
    }
}

// MARK: - Basic Response Model

struct BasicResponse: Codable {
    let status: Bool
    let message: String
}