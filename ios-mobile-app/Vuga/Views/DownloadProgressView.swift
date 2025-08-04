//
//  DownloadProgressView.swift
//  Vuga
//
//  Created by Assistant on 02/08/25.
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
                    // Background download button
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
                    
                    // Cancel button
                    Button(action: {
                        cancelDownload()
                    }) {
                        Text(cancelButtonText)
                            .outfitRegular(16)
                            .foregroundColor(downloadStatus == .downloaded ? .bg : .bg)
                            .frame(maxWidth: .infinity)
                            .frame(height: 45)
                            .background(downloadStatus == .downloaded ? Color.text : Color.base)
                            .cornerRadius(10)
                    }
                    .disabled(isCancelling || downloadStatus == .downloaded)
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
        let totalSize = Float(source.size ?? "0") ?? 0
        let downloadedSize = totalSize * progress
        return String(format: "%.1f MB / %.0f MB", downloadedSize, totalSize)
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