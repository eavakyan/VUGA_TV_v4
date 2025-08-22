//
//  DownloadProgressView.swift
//  Vuga
//
//

import SwiftUI
import Kingfisher

struct DownloadProgressView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @EnvironmentObject var downloadViewModel: DownloadViewModel
    @Binding var isShowing: Bool
    
    let content: VugaContent
    let source: Source
    let downloadId: String
    
    var downloadingContent: DownloadingContent? {
        downloadViewModel.downloadingContents[downloadId]
    }
    
    var progress: Float {
        downloadingContent?.progress ?? 0
    }
    
    var downloadStatus: DownloadStatus {
        downloadingContent?.downloadStatus ?? .notStarted
    }
    
    @State private var isCancelling = false
    
    var body: some View {
        ZStack {
            // Background overlay
            Color.black.opacity(0.5)
                .ignoresSafeArea()
                .onTapGesture {
                    // Prevent dismissing by tapping outside
                }
            
            VStack(spacing: 0) {
                // Content info with thumbnail
                HStack(spacing: 15) {
                    // Thumbnail
                    KFImage(URL(string: content.horizontalPoster ?? ""))
                        .resizable()
                        .aspectRatio(16/9, contentMode: .fill)
                        .frame(width: 100, height: 56)
                        .cornerRadius(8)
                        .clipped()
                    
                    VStack(alignment: .leading, spacing: 5) {
                        Text(content.title ?? "")
                            .outfitMedium(18)
                            .foregroundColor(.text)
                            .lineLimit(2)
                        
                        Text(statusText)
                            .outfitRegular(14)
                            .foregroundColor(.textLight)
                    }
                    
                    Spacer()
                }
                .padding()
                
                // Progress section
                VStack(spacing: 10) {
                    // Progress bar
                    HStack {
                        Text("\(Int(progress * 100))%")
                            .outfitRegular(16)
                            .foregroundColor(.base)
                        
                        Spacer()
                        
                        Text(downloadSizeText)
                            .outfitRegular(14)
                            .foregroundColor(.textLight)
                    }
                    
                    ProgressView(value: progress)
                        .progressViewStyle(LinearProgressViewStyle(tint: .base))
                        .frame(height: 8)
                        .background(Color.text.opacity(0.2))
                        .cornerRadius(4)
                }
                .padding(.horizontal)
                .padding(.bottom, 20)
                
                // Action buttons
                HStack(spacing: 15) {
                    // Background download button (only show if still downloading)
                    if downloadStatus != .downloaded {
                        Button(action: {
                            // Continue download in background
                            isShowing = false
                        }) {
                            Text(String.downloadInBackground.localized(language))
                                .outfitRegular(16)
                                .foregroundColor(.text)
                                .frame(maxWidth: .infinity)
                                .frame(height: 45)
                                .background(Color.bg)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(Color.text.opacity(0.3), lineWidth: 1)
                                )
                                .cornerRadius(10)
                        }
                    }
                    
                    // Cancel/Done button
                    Button(action: {
                        if downloadStatus == .downloaded {
                            // Close the dialog for completed downloads
                            isShowing = false
                        } else {
                            cancelDownload()
                        }
                    }) {
                        Text(cancelButtonText)
                            .outfitRegular(16)
                            .foregroundColor(downloadStatus == .downloaded ? .bg : .bg)
                            .frame(maxWidth: .infinity)
                            .frame(height: 45)
                            .background(downloadStatus == .downloaded ? Color.text : Color.base)
                            .cornerRadius(10)
                    }
                    .disabled(isCancelling)
                }
                .padding(.horizontal)
                .padding(.bottom, 20)
            }
            .background(Color.bg)
            .cornerRadius(15)
            .shadow(radius: 10)
            .padding(.horizontal, 20)
        }
    }
    
    private var statusText: String {
        switch downloadStatus {
        case .notStarted:
            return String.preparingDownload.localized(language)
        case .downloading:
            return String.downloading.localized(language)
        case .paused:
            return "Download paused"
        case .downloaded:
            return "Download completed!"
        case .queued:
            return "Waiting in queue..."
        }
    }
    
    private var cancelButtonText: String {
        downloadStatus == .downloaded ? String.done.localized(language) : String.cancel.localized(language)
    }
    
    private var downloadSizeText: String {
        // Use actual byte counts if available
        if let downloadingContent = downloadingContent,
           downloadingContent.totalBytesExpectedToWrite > 0 {
            
            let totalBytes = downloadingContent.totalBytesExpectedToWrite
            let downloadedBytes = downloadingContent.totalBytesWritten
            
            // Convert to appropriate units
            if totalBytes >= 1024 * 1024 * 1024 { // GB
                let totalGB = Double(totalBytes) / (1024 * 1024 * 1024)
                let downloadedGB = Double(downloadedBytes) / (1024 * 1024 * 1024)
                return String(format: "%.1f GB / %.1f GB", downloadedGB, totalGB)
            } else { // MB
                let totalMB = Double(totalBytes) / (1024 * 1024)
                let downloadedMB = Double(downloadedBytes) / (1024 * 1024)
                return String(format: "%.1f MB / %.0f MB", downloadedMB, totalMB)
            }
        } else {
            // Fallback to old method if byte counts not available
            let totalSizeMB = parseSizeToMB(source.size ?? "0")
            let downloadedSizeMB = totalSizeMB * progress
            
            if totalSizeMB >= 1024 {
                let totalSizeGB = totalSizeMB / 1024
                let downloadedSizeGB = downloadedSizeMB / 1024
                return String(format: "%.1f GB / %.1f GB", downloadedSizeGB, totalSizeGB)
            } else {
                return String(format: "%.1f MB / %.0f MB", downloadedSizeMB, totalSizeMB)
            }
        }
    }
    
    private func parseSizeToMB(_ sizeString: String) -> Float {
        let cleanSize = sizeString.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        
        // Extract the numeric part
        let components = cleanSize.components(separatedBy: .whitespaces)
        guard let numericString = components.first,
              let numericValue = Float(numericString) else {
            return 0
        }
        
        // Check for units and convert to MB
        if cleanSize.contains("gb") {
            return numericValue * 1024 // GB to MB
        } else if cleanSize.contains("kb") {
            return numericValue / 1024 // KB to MB
        } else if cleanSize.contains("mb") || cleanSize.contains("m") {
            return numericValue // Already in MB
        } else {
            // If no unit specified, assume MB
            return numericValue
        }
    }
    
    private func cancelDownload() {
        isCancelling = true
        
        // Cancel the download
        downloadViewModel.pauseDownload(sourceId: downloadId, isForDelete: true)
        
        // Remove from download queue
        downloadViewModel.downloadQueue.removeAll { $0.id == downloadId }
        
        // Remove from downloading contents
        downloadViewModel.downloadingContents.removeValue(forKey: downloadId)
        
        // Remove from Core Data
        if let downloadContent = downloadViewModel.downloadContents.first(where: { $0.downloadId == downloadId }) {
            DataController.shared.context.delete(downloadContent)
            DataController.shared.saveData()
            downloadViewModel.downloadContents.removeAll { $0.downloadId == downloadId }
        }
        
        // Close the dialog
        isShowing = false
    }
}