//
//  UniversalCastManager.swift
//  Vuga
//
//

import SwiftUI
import AVKit
import GoogleCast
import Network

enum CastDeviceType {
    case airplay
    case googleCast
    case dlna
    case unknown
}

struct CastDevice {
    let id: String
    let name: String
    let type: CastDeviceType
    var isConnected: Bool = false
}

class UniversalCastManager: NSObject, ObservableObject {
    static let shared = UniversalCastManager()
    
    @Published var availableDevices: [CastDevice] = []
    @Published var connectedDevice: CastDevice?
    @Published var isScanning = false
    @Published var castState: GCKCastState = .noDevicesAvailable
    
    private var castContext: GCKCastContext?
    private var sessionManager: GCKSessionManager?
    private var discoveryManager: GCKDiscoveryManager?
    
    override init() {
        super.init()
        setupGoogleCast()
        setupAirPlay()
        startDeviceDiscovery()
    }
    
    // MARK: - Google Cast Setup
    private func setupGoogleCast() {
        // Load Cast configuration from plist
        guard let path = Bundle.main.path(forResource: "GoogleCast-Info", ofType: "plist"),
              let plist = NSDictionary(contentsOfFile: path),
              let appId = plist["GCKCastApplicationID"] as? String else {
            print("Error: GoogleCast-Info.plist not found or invalid")
            return
        }
        
        let criteria = GCKDiscoveryCriteria(applicationID: appId)
        let options = GCKCastOptions(discoveryCriteria: criteria)
        options.physicalVolumeButtonsWillControlDeviceVolume = true
        
        GCKCastContext.setSharedInstanceWith(options)
        castContext = GCKCastContext.sharedInstance()
        sessionManager = castContext?.sessionManager
        discoveryManager = castContext?.discoveryManager
        
        // Add observers
        sessionManager?.add(self)
        discoveryManager?.add(self)
        
        // Start passive discovery
        discoveryManager?.passivelyStartDiscovery()
    }
    
    // MARK: - AirPlay Setup
    private func setupAirPlay() {
        // AirPlay is handled through AVPlayer, but we can monitor route changes
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(audioRouteChanged),
            name: AVAudioSession.routeChangeNotification,
            object: nil
        )
    }
    
    @objc private func audioRouteChanged(_ notification: Notification) {
        updateAvailableDevices()
    }
    
    // MARK: - Device Discovery
    func startDeviceDiscovery() {
        isScanning = true
        
        // Start active Google Cast discovery
        discoveryManager?.startDiscovery()
        
        // Check for AirPlay devices
        updateAvailableDevices()
        
        // Stop scanning after 30 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 30) { [weak self] in
            self?.stopDeviceDiscovery()
        }
    }
    
    func stopDeviceDiscovery() {
        isScanning = false
        discoveryManager?.stopDiscovery()
    }
    
    private func updateAvailableDevices() {
        var devices: [CastDevice] = []
        
        // Add Google Cast devices
        if let castDevices = discoveryManager?.devices {
            for device in castDevices {
                devices.append(CastDevice(
                    id: device.uniqueID,
                    name: device.friendlyName ?? "Unknown Cast Device",
                    type: .googleCast,
                    isConnected: sessionManager?.currentCastSession?.device == device
                ))
            }
        }
        
        // Check for AirPlay availability
        let audioSession = AVAudioSession.sharedInstance()
        let currentRoute = audioSession.currentRoute
        
        for output in currentRoute.outputs {
            if output.portType == .airPlay {
                devices.append(CastDevice(
                    id: output.uid,
                    name: output.portName,
                    type: .airplay,
                    isConnected: true
                ))
            }
        }
        
        // Check if AirPlay is available but not connected
        if AVAudioSession.sharedInstance().isOtherAudioPlaying == false {
            let routePickerView = AVRoutePickerView()
            if routePickerView.isRoutePickerButtonBordered {
                // AirPlay devices are available
                if !devices.contains(where: { $0.type == .airplay }) {
                    devices.append(CastDevice(
                        id: "airplay-available",
                        name: "AirPlay Devices",
                        type: .airplay,
                        isConnected: false
                    ))
                }
            }
        }
        
        DispatchQueue.main.async {
            self.availableDevices = devices
            self.connectedDevice = devices.first { $0.isConnected }
        }
    }
    
    // MARK: - Casting Functions
    func cast(to device: CastDevice, content: VugaContent, source: Source, episode: Episode? = nil) {
        switch device.type {
        case .googleCast:
            castToGoogleCast(device: device, content: content, source: source, episode: episode)
        case .airplay:
            // AirPlay is handled through AVPlayer in VideoPlayerModel
            var userInfo: [String: Any] = ["content": content, "source": source]
            if let episode = episode {
                userInfo["episode"] = episode
            }
            NotificationCenter.default.post(
                name: Notification.Name("StartAirPlayCasting"),
                object: nil,
                userInfo: userInfo
            )
        case .dlna:
            castToDLNA(device: device, content: content, source: source, episode: episode)
        case .unknown:
            break
        }
    }
    
    private func castToGoogleCast(device: CastDevice, content: VugaContent, source: Source, episode: Episode? = nil) {
        guard let castDevice = discoveryManager?.devices.first(where: { $0.uniqueID == device.id }) else {
            return
        }
        
        sessionManager?.startSession(with: castDevice)
        
        // Wait for session to connect
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) { [weak self] in
            self?.loadMediaOnGoogleCast(content: content, source: source, episode: episode)
        }
    }
    
    private func loadMediaOnGoogleCast(content: VugaContent, source: Source, episode: Episode? = nil) {
        guard let session = sessionManager?.currentCastSession,
              session.connectionState == .connected else {
            print("No connected Cast session")
            return
        }
        
        let metadata = GCKMediaMetadata(metadataType: content.type == .movie ? .movie : .tvShow)
        
        // Use episode title if casting an episode, otherwise use content title
        let title = episode?.title ?? content.title ?? ""
        let subtitle = episode != nil ? content.title ?? "" : content.description ?? ""
        
        metadata.setString(title, forKey: kGCKMetadataKeyTitle)
        metadata.setString(subtitle, forKey: kGCKMetadataKeySubtitle)
        
        // For episodes, add season and episode number
        if let episode = episode {
            if let seasonNum = episode.seasonID, let episodeNum = episode.number {
                metadata.setString("S\(seasonNum) E\(episodeNum)", forKey: kGCKMetadataKeyEpisodeNumber)
            }
        }
        
        if let posterURL = content.verticalPoster?.addBaseURL() {
            metadata.addImage(GCKImage(url: URL(string: posterURL)!, width: 480, height: 720))
        }
        
        if let thumbnailURL = content.thumbnail?.addBaseURL() {
            metadata.addImage(GCKImage(url: URL(string: thumbnailURL)!, width: 1920, height: 1080))
        }
        
        let mediaInfoBuilder = GCKMediaInformationBuilder(contentURL: URL(string: source.sourceUrl!)!)
        mediaInfoBuilder.streamType = .buffered
        mediaInfoBuilder.contentType = "video/mp4"
        mediaInfoBuilder.metadata = metadata
        mediaInfoBuilder.streamDuration = TimeInterval(content.durationInSeconds ?? 0)
        
        let mediaInfo = mediaInfoBuilder.build()
        
        let loadOptions = GCKMediaLoadOptions()
        loadOptions.playPosition = 0
        loadOptions.autoplay = true
        
        session.remoteMediaClient?.loadMedia(mediaInfo, with: loadOptions)
    }
    
    private func castToDLNA(device: CastDevice, content: VugaContent, source: Source, episode: Episode? = nil) {
        // DLNA implementation will be added when CocoaUPnP is integrated
        print("DLNA casting not yet implemented")
    }
    
    // MARK: - Disconnect
    func disconnect() {
        if let session = sessionManager?.currentCastSession {
            session.end(with: .disconnect)
        }
        
        // For AirPlay, post notification to stop
        NotificationCenter.default.post(
            name: Notification.Name("StopAirPlayCasting"),
            object: nil
        )
        
        connectedDevice = nil
    }
}

// MARK: - GCKSessionManagerListener
extension UniversalCastManager: GCKSessionManagerListener {
    func sessionManager(_ sessionManager: GCKSessionManager, didStart session: GCKSession) {
        print("Cast session started")
        updateAvailableDevices()
    }
    
    func sessionManager(_ sessionManager: GCKSessionManager, didEnd session: GCKSession, withError error: Error?) {
        print("Cast session ended")
        if let error = error {
            print("Session ended with error: \(error.localizedDescription)")
        }
        updateAvailableDevices()
    }
    
    func sessionManager(_ sessionManager: GCKSessionManager, didFailToStart session: GCKSession, withError error: Error) {
        print("Failed to start session: \(error.localizedDescription)")
    }
}

// MARK: - GCKDiscoveryManagerListener
extension UniversalCastManager: GCKDiscoveryManagerListener {
    func didUpdateDeviceList() {
        print("Cast device list updated")
        updateAvailableDevices()
    }
}

// MARK: - Helper Extensions
extension String {
    func addBaseURL() -> String {
        // Add your base URL logic here
        return self
    }
}

extension VugaContent {
    var durationInSeconds: Int? {
        // Parse duration string to seconds
        guard let duration = self.duration else { return nil }
        // Implement duration parsing logic
        return 0
    }
}