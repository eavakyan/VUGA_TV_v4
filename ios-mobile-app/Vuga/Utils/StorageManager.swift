//
//  StorageManager.swift
//  Vuga
//
//  Created by Assistant on today's date.
//

import Foundation

class StorageManager {
    static let shared = StorageManager()
    
    private init() {}
    
    /// Check if device has enough storage for download
    /// - Parameter requiredBytes: Size needed for download in bytes
    /// - Returns: True if enough storage available
    func hasEnoughStorage(for requiredBytes: Int64) -> Bool {
        do {
            let fileURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
            let values = try fileURL.resourceValues(forKeys: [.volumeAvailableCapacityForImportantUsageKey, .volumeTotalCapacityKey])
            
            guard let availableBytes = values.volumeAvailableCapacityForImportantUsage,
                  let totalBytes = values.volumeTotalCapacity else {
                return true // Allow download if we can't check
            }
            
            // Industry best practice: Allow downloads up to 10% of total storage or 90% of available storage
            let maxAllowedBytes = min(Int64(totalBytes) / 10, Int64(Double(availableBytes) * 0.9))
            
            // Check if required space is available
            if requiredBytes > maxAllowedBytes {
                return false
            }
            
            // Also check if at least 500MB will remain after download
            let minRemainingSpace: Int64 = 500 * 1024 * 1024 // 500MB
            return (Int64(availableBytes) - requiredBytes) > minRemainingSpace
            
        } catch {
            print("Error checking storage: \(error)")
            return true // Allow download if we can't check
        }
    }
    
    /// Get available storage space in bytes
    func getAvailableStorage() -> Int64? {
        do {
            let fileURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
            let values = try fileURL.resourceValues(forKeys: [.volumeAvailableCapacityForImportantUsageKey])
            return values.volumeAvailableCapacityForImportantUsage.map { Int64($0) }
        } catch {
            print("Error getting available storage: \(error)")
            return nil
        }
    }
    
    /// Get total storage space in bytes
    func getTotalStorage() -> Int64? {
        do {
            let fileURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
            let values = try fileURL.resourceValues(forKeys: [.volumeTotalCapacityKey])
            return values.volumeTotalCapacity.map { Int64($0) }
        } catch {
            print("Error getting total storage: \(error)")
            return nil
        }
    }
    
    /// Format bytes to human readable string
    static func formatBytes(_ bytes: Int64) -> String {
        let formatter = ByteCountFormatter()
        formatter.countStyle = .file
        return formatter.string(fromByteCount: bytes)
    }
}