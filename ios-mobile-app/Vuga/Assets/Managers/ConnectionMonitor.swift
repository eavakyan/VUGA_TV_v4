import Foundation
import Network
import SwiftUI
import Combine

enum ConnectionQuality {
    case excellent
    case good
    case fair
    case poor
    case offline
    
    var displayText: String {
        switch self {
        case .excellent:
            return "Excellent Connection"
        case .good:
            return "Good Connection"
        case .fair:
            return "Slow Connection"
        case .poor:
            return "Very Slow Connection"
        case .offline:
            return "No Internet Connection"
        }
    }
    
    var color: Color {
        switch self {
        case .excellent:
            return .green
        case .good:
            return .green
        case .fair:
            return .orange
        case .poor:
            return .red
        case .offline:
            return .red
        }
    }
    
    var iconName: String {
        switch self {
        case .excellent:
            return "wifi"
        case .good:
            return "wifi"
        case .fair:
            return "wifi.exclamationmark"
        case .poor:
            return "wifi.exclamationmark"
        case .offline:
            return "wifi.slash"
        }
    }
}

class ConnectionMonitor: ObservableObject {
    static let shared = ConnectionMonitor()
    
    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "NetworkMonitor")
    private var cancellables = Set<AnyCancellable>()
    
    @Published var isConnected: Bool = true
    @Published var connectionType: NWInterface.InterfaceType?
    @Published var connectionQuality: ConnectionQuality = .good
    @Published var showConnectionAlert: Bool = false
    @Published var latency: TimeInterval = 0
    @Published var downloadSpeed: Double = 0 // Mbps
    
    // Track API response times for quality assessment
    private var recentResponseTimes: [TimeInterval] = []
    private let maxResponseTimeSamples = 10
    
    // Thresholds for connection quality (in seconds)
    private let excellentThreshold: TimeInterval = 0.5
    private let goodThreshold: TimeInterval = 1.0
    private let fairThreshold: TimeInterval = 2.0
    private let poorThreshold: TimeInterval = 4.0
    
    private init() {
        startMonitoring()
    }
    
    private func startMonitoring() {
        monitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                self?.updateConnectionStatus(path)
            }
        }
        
        monitor.start(queue: queue)
        
        // Periodically check connection quality
        Timer.publish(every: 30, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                self?.checkConnectionQuality()
            }
            .store(in: &cancellables)
    }
    
    private func updateConnectionStatus(_ path: NWPath) {
        let wasConnected = isConnected
        isConnected = path.status == .satisfied
        
        if path.usesInterfaceType(.wifi) {
            connectionType = .wifi
        } else if path.usesInterfaceType(.cellular) {
            connectionType = .cellular
        } else {
            connectionType = nil
        }
        
        // Show alert when connection status changes
        if wasConnected && !isConnected {
            connectionQuality = .offline
            showConnectionAlert = true
            sendConnectionNotification(connected: false)
        } else if !wasConnected && isConnected {
            showConnectionAlert = false
            sendConnectionNotification(connected: true)
            checkConnectionQuality()
        }
        
        // Update quality based on connection type
        if !isConnected {
            connectionQuality = .offline
        }
    }
    
    func recordResponseTime(_ time: TimeInterval) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            
            self.recentResponseTimes.append(time)
            if self.recentResponseTimes.count > self.maxResponseTimeSamples {
                self.recentResponseTimes.removeFirst()
            }
            
            self.updateConnectionQuality()
        }
    }
    
    private func updateConnectionQuality() {
        guard isConnected else {
            connectionQuality = .offline
            return
        }
        
        guard !recentResponseTimes.isEmpty else {
            // Default to good if no data yet
            connectionQuality = .good
            return
        }
        
        let averageResponseTime = recentResponseTimes.reduce(0, +) / Double(recentResponseTimes.count)
        latency = averageResponseTime
        
        // Determine quality based on average response time
        let previousQuality = connectionQuality
        
        switch averageResponseTime {
        case 0..<excellentThreshold:
            connectionQuality = .excellent
        case excellentThreshold..<goodThreshold:
            connectionQuality = .good
        case goodThreshold..<fairThreshold:
            connectionQuality = .fair
        case fairThreshold..<poorThreshold:
            connectionQuality = .poor
        default:
            connectionQuality = .poor
        }
        
        // Show alert if connection becomes poor
        if previousQuality != connectionQuality &&
           (connectionQuality == .poor || connectionQuality == .fair) {
            showConnectionAlert = true
            
            // Auto-hide after 3 seconds for non-critical alerts
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak self] in
                if self?.connectionQuality != .offline {
                    self?.showConnectionAlert = false
                }
            }
        }
    }
    
    func checkConnectionQuality() {
        guard isConnected else { return }
        
        // Ping a reliable server to test latency
        testLatency()
    }
    
    private func testLatency() {
        let url = URL(string: "https://www.google.com")!
        let startTime = Date()
        
        var request = URLRequest(url: url)
        request.httpMethod = "HEAD"
        request.timeoutInterval = 5
        
        URLSession.shared.dataTask(with: request) { [weak self] _, response, error in
            let responseTime = Date().timeIntervalSince(startTime)
            
            if error == nil && response != nil {
                self?.recordResponseTime(responseTime)
            } else if error != nil {
                // Connection is very slow or problematic
                self?.recordResponseTime(5.0)
            }
        }.resume()
    }
    
    private func sendConnectionNotification(connected: Bool) {
        let notificationName = connected ? 
            NSNotification.Name("NetworkConnected") : 
            NSNotification.Name("NetworkDisconnected")
        
        NotificationCenter.default.post(name: notificationName, object: nil)
    }
    
    func startSpeedTest(completion: @escaping (Double) -> Void) {
        // Simple speed test using a small file download
        let testURL = URL(string: "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png")!
        let startTime = Date()
        
        URLSession.shared.dataTask(with: testURL) { data, _, error in
            guard let data = data, error == nil else {
                completion(0)
                return
            }
            
            let elapsedTime = Date().timeIntervalSince(startTime)
            let bytes = Double(data.count)
            let bits = bytes * 8
            let megabits = bits / 1_000_000
            let speed = megabits / elapsedTime
            
            DispatchQueue.main.async {
                self.downloadSpeed = speed
                completion(speed)
            }
        }.resume()
    }
}