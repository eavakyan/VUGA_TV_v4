import Foundation
import CoreData
import UIKit

/**
 * Service to sync watch history between local Core Data and server
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
        guard let user = SessionManager.shared.getUser(),
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
        guard let user = SessionManager.shared.getUser(),
              let profileId = user.lastActiveProfileId,
              profileId > 0 else {
            return
        }
        
        // Perform sync synchronously with shorter timeout
        performSync(timeout: 10.0)
    }
    
    /**
     * Sync from server to local - used on app startup for cross-device sync
     */
    func syncFromServer(completion: @escaping (Bool) -> Void) {
        guard let user = SessionManager.shared.getUser(),
              let profileId = user.lastActiveProfileId,
              profileId > 0 else {
            completion(false)
            return
        }
        
        // Get server history and merge with local
        fetchServerHistory(profileId: profileId) { [weak self] serverHistory in
            if let serverHistory = serverHistory {
                self?.mergeServerHistory(serverHistory)
                completion(true)
            } else {
                completion(false)
            }
        }
    }
    
    // MARK: - Private Methods
    
    private func performSync(timeout: TimeInterval = 30.0) {
        guard !isSyncing else {
            print("WatchHistorySyncService: Sync already in progress")
            return
        }
        
        isSyncing = true
        defer { isSyncing = false }
        
        guard let user = SessionManager.shared.getUser(),
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
        
        // Prepare request
        let requestData: [String: Any] = [
            "profile_id": profileId,
            "watch_history": syncItems
        ]
        
        // Make API call
        NetworkManager.callWebService(
            url: .syncWatchHistory,
            httpMethod: .POST,
            params: requestData,
            timeout: timeout,
            callbackSuccess: { (response: SyncResponse) in
                print("WatchHistorySyncService: Sync successful - \(response.data?.syncedNew ?? 0) new, \(response.data?.updatedExisting ?? 0) updated")
                
                // Save last sync time
                UserDefaults.standard.set(Date(), forKey: "last_watch_history_sync")
            },
            callbackFailure: { error in
                print("WatchHistorySyncService: Sync failed - \(error)")
            }
        )
    }
    
    private func getLocalWatchHistory() -> [RecentlyWatchedData] {
        let context = PersistentContainer.context
        let request: NSFetchRequest<RecentlyWatchedData> = RecentlyWatchedData.fetchRequest()
        request.sortDescriptors = [NSSortDescriptor(key: "lastWatchedAt", ascending: false)]
        request.fetchLimit = 100 // Limit to most recent 100 items
        
        do {
            return try context.fetch(request)
        } catch {
            print("WatchHistorySyncService: Error fetching local history - \(error)")
            return []
        }
    }
    
    private func convertToSyncFormat(_ localHistory: [RecentlyWatchedData]) -> [[String: Any]] {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        return localHistory.compactMap { item in
            guard let contentId = item.contentId,
                  let lastPosition = item.lastWatchedPosition,
                  lastPosition > 0 else {
                return nil
            }
            
            var syncItem: [String: Any] = [
                "content_id": contentId,
                "last_watched_position": lastPosition,
                "device_type": 1 // 1 = mobile
            ]
            
            if let episodeId = item.episodeId, episodeId > 0 {
                syncItem["episode_id"] = episodeId
            }
            
            if let duration = item.totalDuration, duration > 0 {
                syncItem["total_duration"] = duration
                // Mark as completed if 90% watched
                let percentage = Double(lastPosition) / Double(duration) * 100
                syncItem["completed"] = percentage >= 90
            }
            
            if let watchedAt = item.lastWatchedAt {
                syncItem["watched_at"] = dateFormatter.string(from: watchedAt)
            } else {
                syncItem["watched_at"] = dateFormatter.string(from: Date())
            }
            
            return syncItem
        }
    }
    
    private func fetchServerHistory(profileId: Int, completion: @escaping ([[String: Any]]?) -> Void) {
        let requestData: [String: Any] = [
            "profile_id": profileId,
            "limit": 100
        ]
        
        // Add last sync time if available
        if let lastSync = UserDefaults.standard.object(forKey: "last_watch_history_sync") as? Date {
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
            requestData["updated_since"] = dateFormatter.string(from: lastSync)
        }
        
        NetworkManager.callWebService(
            url: .getWatchHistoryForSync,
            httpMethod: .POST,
            params: requestData,
            timeout: 15.0,
            callbackSuccess: { (response: ServerHistoryResponse) in
                completion(response.data)
            },
            callbackFailure: { error in
                print("WatchHistorySyncService: Failed to fetch server history - \(error)")
                completion(nil)
            }
        )
    }
    
    private func mergeServerHistory(_ serverHistory: [[String: Any]]) {
        let context = PersistentContainer.context
        
        for serverItem in serverHistory {
            guard let contentId = serverItem["content_id"] as? Int,
                  let lastPosition = serverItem["last_watched_position"] as? Int,
                  lastPosition > 0 else {
                continue
            }
            
            let episodeId = serverItem["episode_id"] as? Int
            
            // Check if we have this item locally
            let request: NSFetchRequest<RecentlyWatchedData> = RecentlyWatchedData.fetchRequest()
            request.predicate = NSPredicate(format: "contentId == %d AND episodeId == %d", 
                                          contentId, episodeId ?? 0)
            
            do {
                let existingItems = try context.fetch(request)
                let existingItem = existingItems.first
                
                // Only update if server has more recent data
                if let existing = existingItem {
                    if lastPosition > existing.lastWatchedPosition?.intValue ?? 0 {
                        existing.lastWatchedPosition = NSNumber(value: lastPosition)
                        if let duration = serverItem["total_duration"] as? Int {
                            existing.totalDuration = NSNumber(value: duration)
                        }
                        if let completed = serverItem["completed"] as? Bool {
                            existing.isCompleted = completed
                        }
                        existing.lastWatchedAt = Date()
                    }
                } else {
                    // Create new item from server data
                    let newItem = RecentlyWatchedData(context: context)
                    newItem.contentId = NSNumber(value: contentId)
                    newItem.episodeId = episodeId != nil ? NSNumber(value: episodeId!) : nil
                    newItem.lastWatchedPosition = NSNumber(value: lastPosition)
                    newItem.lastWatchedAt = Date()
                    
                    if let duration = serverItem["total_duration"] as? Int {
                        newItem.totalDuration = NSNumber(value: duration)
                    }
                    if let completed = serverItem["completed"] as? Bool {
                        newItem.isCompleted = completed
                    }
                }
                
                try context.save()
            } catch {
                print("WatchHistorySyncService: Error merging server history - \(error)")
            }
        }
    }
}

// MARK: - Response Models

struct SyncResponse: Codable {
    let status: Bool
    let message: String
    let data: SyncData?
    
    struct SyncData: Codable {
        let syncedNew: Int
        let updatedExisting: Int
        let totalProcessed: Int
        
        enum CodingKeys: String, CodingKey {
            case syncedNew = "synced_new"
            case updatedExisting = "updated_existing"
            case totalProcessed = "total_processed"
        }
    }
}

struct ServerHistoryResponse: Codable {
    let status: Bool
    let message: String
    let data: [[String: Any]]?
    let count: Int?
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        status = try container.decode(Bool.self, forKey: .status)
        message = try container.decode(String.self, forKey: .message)
        count = try container.decodeIfPresent(Int.self, forKey: .count)
        
        // Handle the dynamic data array
        if let dataArray = try container.decodeIfPresent([String: Any].self, forKey: .data) as? [[String: Any]] {
            data = dataArray
        } else {
            data = nil
        }
    }
    
    enum CodingKeys: String, CodingKey {
        case status, message, data, count
    }
}