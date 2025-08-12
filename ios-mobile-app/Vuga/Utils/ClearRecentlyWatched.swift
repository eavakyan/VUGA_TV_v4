//
//  ClearRecentlyWatched.swift
//  Vuga
//
//  Utility to clear Recently Watched data
//

import Foundation
import CoreData

extension DataController {
    func clearAllRecentlyWatched() {
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = RecentlyWatched.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        
        do {
            try context.execute(deleteRequest)
            saveData()
            print("Successfully cleared all Recently Watched data")
        } catch {
            print("Failed to clear Recently Watched data: \(error)")
        }
    }
    
    func clearRecentlyWatchedForProfile(profileId: Int32) {
        // If you want to clear for a specific profile only
        let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
        // Add profile filtering if the entity has profileId
        
        do {
            let items = try context.fetch(fetchRequest)
            for item in items {
                context.delete(item)
            }
            saveData()
            print("Successfully cleared Recently Watched data for profile")
        } catch {
            print("Failed to clear Recently Watched data: \(error)")
        }
    }
}