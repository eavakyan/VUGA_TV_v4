//
//  VideoCacheManager.swift
//  Vuga
//
//  Manages video caching for improved playback on poor network connections
//  Uses AVAssetDownloadURLSession for HLS and URLCache for progressive downloads
//

import Foundation
import AVFoundation
import Network

/// Manages video caching and adaptive buffering for improved playback on poor networks
class VideoCacheManager: NSObject, ObservableObject {
    static let shared = VideoCacheManager()
    
    // MARK: - Properties
    private let cacheDirectory: URL
    private let maxCacheSize: Int64 = 200 * 1024 * 1024 // 200MB
    private var downloadSession: AVAssetDownloadURLSession?
    private var activeDownloads: [URL: AVAssetDownloadTask] = [:]
    private let urlCache: URLCache
    private let monitor = NWPathMonitor()
    private let monitorQueue = DispatchQueue(label: "network.monitor")
    
    @Published var currentNetworkType: NetworkType = .unknown
    
    enum NetworkType {
        case wifi
        case cellular
        case offline
        case unknown
    }
    
    // MARK: - Initialization
    
    override init() {
        // Setup cache directory
        let documentsPath = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!
        self.cacheDirectory = documentsPath.appendingPathComponent("VideoCache")
        
        // Create cache directory if needed
        try? FileManager.default.createDirectory(at: cacheDirectory, withIntermediateDirectories: true)
        
        // Setup URL cache for progressive downloads
        self.urlCache = URLCache(
            memoryCapacity: 50 * 1024 * 1024, // 50MB memory cache
            diskCapacity: Int(maxCacheSize),
            directory: cacheDirectory
        )
        
        super.init()
        
        // Setup download session for HLS
        setupDownloadSession()
        
        // Start network monitoring
        startNetworkMonitoring()
        
        // Clean old cache on init
        cleanOldCache()
    }
    
    private func setupDownloadSession() {
        let config = URLSessionConfiguration.background(withIdentifier: "com.vuga.videocache")
        config.isDiscretionary = false
        config.sessionSendsLaunchEvents = true
        config.urlCache = urlCache
        config.requestCachePolicy = .returnCacheDataElseLoad
        
        downloadSession = AVAssetDownloadURLSession(
            configuration: config,
            assetDownloadDelegate: self,
            delegateQueue: OperationQueue.main
        )
    }
    
    // MARK: - Network Monitoring
    
    private func startNetworkMonitoring() {
        monitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                self?.updateNetworkType(path)
            }
        }
        monitor.start(queue: monitorQueue)
    }
    
    private func updateNetworkType(_ path: NWPath) {
        if path.status == .satisfied {
            if path.usesInterfaceType(.wifi) {
                currentNetworkType = .wifi
            } else if path.usesInterfaceType(.cellular) {
                currentNetworkType = .cellular
            } else {
                currentNetworkType = .unknown
            }
        } else {
            currentNetworkType = .offline
        }
        
        print("VideoCacheManager: Network type changed to \(currentNetworkType)")
    }
    
    // MARK: - Adaptive Buffer Configuration
    
    /// Returns optimal buffer duration based on network conditions
    func getAdaptiveBufferDuration() -> TimeInterval {
        switch currentNetworkType {
        case .wifi:
            return 60.0 // 60 seconds buffer on WiFi
        case .cellular:
            return 30.0 // 30 seconds on cellular
        case .offline, .unknown:
            return 15.0 // 15 seconds for poor/unknown
        }
    }
    
    /// Configures AVPlayer with adaptive settings
    func configurePlayer(_ player: AVPlayer) {
        player.automaticallyWaitsToMinimizeStalling = true
        
        // Set preferred forward buffer duration based on network
        if let currentItem = player.currentItem {
            currentItem.preferredForwardBufferDuration = getAdaptiveBufferDuration()
            
            // Configure for better performance on poor networks
            if currentNetworkType == .offline || currentNetworkType == .unknown {
                currentItem.canUseNetworkResourcesForLiveStreamingWhilePaused = false
            }
        }
        
        print("VideoCacheManager: Player configured with buffer duration: \(getAdaptiveBufferDuration())s")
    }
    
    // MARK: - Cache Management
    
    /// Creates a cached AVPlayerItem for the given URL
    func getCachedPlayerItem(for url: URL) -> AVPlayerItem {
        let asset: AVURLAsset
        
        // Check if it's HLS
        if url.pathExtension.lowercased() == "m3u8" {
            // For HLS, create asset with cache configuration
            asset = AVURLAsset(url: url, options: [
                AVURLAssetPreferPreciseDurationAndTimingKey: true,
                AVURLAssetHTTPCookiesKey: HTTPCookieStorage.shared.cookies ?? []
            ])
            
            // Start background caching if on WiFi
            if currentNetworkType == .wifi {
                startHLSCaching(for: asset)
            }
        } else {
            // For progressive downloads, use URL cache
            var request = URLRequest(url: url)
            request.cachePolicy = .returnCacheDataElseLoad
            
            asset = AVURLAsset(url: url, options: [
                AVURLAssetPreferPreciseDurationAndTimingKey: true,
                "AVURLAssetHTTPHeaderFieldsKey": request.allHTTPHeaderFields ?? [:]
            ])
        }
        
        let playerItem = AVPlayerItem(asset: asset)
        
        // Configure buffer settings
        playerItem.preferredForwardBufferDuration = getAdaptiveBufferDuration()
        
        // Add quality selection based on network
        if currentNetworkType == .cellular || currentNetworkType == .unknown {
            // Prefer lower quality on cellular/poor networks
            playerItem.preferredPeakBitRate = 1_000_000 // 1 Mbps max
        }
        
        return playerItem
    }
    
    /// Starts background caching for HLS content
    private func startHLSCaching(for asset: AVURLAsset) {
        guard let downloadSession = downloadSession else { return }
        
        // Check if already downloading
        let assetURL = asset.url
        if activeDownloads[assetURL] != nil {
            return
        }
        
        // Configure download options
        let downloadOptions: [String: Any] = [
            AVAssetDownloadTaskMinimumRequiredMediaBitrateKey: 265_000, // Minimum bitrate
            AVAssetDownloadTaskMinimumRequiredPresentationSizeKey: CGSize(width: 480, height: 270) // Minimum resolution
        ]
        
        // Create download task
        if let downloadTask = downloadSession.makeAssetDownloadTask(
            asset: asset,
            assetTitle: "Video Cache",
            assetArtworkData: nil,
            options: downloadOptions
        ) {
            activeDownloads[assetURL] = downloadTask
            downloadTask.resume()
            print("VideoCacheManager: Started HLS caching for \(assetURL)")
        }
    }
    
    // MARK: - Cache Cleanup
    
    /// Cleans cache files older than 30 days
    private func cleanOldCache() {
        let fileManager = FileManager.default
        let expirationDate = Date().addingTimeInterval(-30 * 24 * 60 * 60) // 30 days
        
        do {
            let cacheFiles = try fileManager.contentsOfDirectory(
                at: cacheDirectory,
                includingPropertiesForKeys: [.creationDateKey],
                options: .skipsHiddenFiles
            )
            
            for file in cacheFiles {
                if let attributes = try? fileManager.attributesOfItem(atPath: file.path),
                   let creationDate = attributes[.creationDate] as? Date,
                   creationDate < expirationDate {
                    try? fileManager.removeItem(at: file)
                    print("VideoCacheManager: Removed old cache file: \(file.lastPathComponent)")
                }
            }
        } catch {
            print("VideoCacheManager: Error cleaning cache: \(error)")
        }
    }
    
    /// Gets current cache size in bytes
    func getCacheSize() -> Int64 {
        let fileManager = FileManager.default
        var size: Int64 = 0
        
        do {
            let files = try fileManager.contentsOfDirectory(
                at: cacheDirectory,
                includingPropertiesForKeys: [.fileSizeKey]
            )
            
            for file in files {
                if let fileSize = try? file.resourceValues(forKeys: [.fileSizeKey]).fileSize {
                    size += Int64(fileSize)
                }
            }
        } catch {
            print("VideoCacheManager: Error calculating cache size: \(error)")
        }
        
        return size
    }
    
    /// Clears all video cache
    func clearCache() {
        // Cancel all downloads
        for (_, task) in activeDownloads {
            task.cancel()
        }
        activeDownloads.removeAll()
        
        // Clear URL cache
        urlCache.removeAllCachedResponses()
        
        // Remove cache directory
        try? FileManager.default.removeItem(at: cacheDirectory)
        try? FileManager.default.createDirectory(at: cacheDirectory, withIntermediateDirectories: true)
        
        print("VideoCacheManager: Cache cleared")
    }
}

// MARK: - AVAssetDownloadDelegate

extension VideoCacheManager: AVAssetDownloadDelegate {
    func urlSession(_ session: URLSession, assetDownloadTask: AVAssetDownloadTask, didFinishDownloadingTo location: URL) {
        print("VideoCacheManager: HLS download completed to \(location)")
        
        // Remove from active downloads
        let url = assetDownloadTask.urlAsset.url
        activeDownloads.removeValue(forKey: url)
    }
    
    func urlSession(_ session: URLSession, assetDownloadTask: AVAssetDownloadTask, didLoad timeRange: CMTimeRange, totalTimeRangesLoaded loadedTimeRanges: [NSValue], timeRangeExpectedToLoad: CMTimeRange) {
        // Calculate progress
        var percentComplete = 0.0
        for value in loadedTimeRanges {
            let loadedTimeRange = value.timeRangeValue
            percentComplete += loadedTimeRange.duration.seconds / timeRangeExpectedToLoad.duration.seconds
        }
        
        print("VideoCacheManager: HLS cache progress: \(Int(percentComplete * 100))%")
    }
    
    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        if let error = error {
            print("VideoCacheManager: Download error: \(error)")
        }
        
        // Clean up active download
        if let downloadTask = task as? AVAssetDownloadTask {
            let url = downloadTask.urlAsset.url
            activeDownloads.removeValue(forKey: url)
        }
    }
}