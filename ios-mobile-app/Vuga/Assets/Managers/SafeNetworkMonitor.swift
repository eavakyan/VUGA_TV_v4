import Foundation
import Network
import SwiftUI
import Alamofire

// Simplified network monitor that works with existing infrastructure
class SafeNetworkMonitor: ObservableObject {
    static let shared = SafeNetworkMonitor()
    
    @Published var isSlowConnection: Bool = false
    @Published var connectionMessage: String = ""
    @Published var showAlert: Bool = false
    
    private let reachabilityManager = NetworkReachabilityManager()
    private var lastNetworkStatus: NetworkReachabilityManager.NetworkReachabilityStatus?
    private var transitionTimer: Timer?
    
    private init() {
        setupReachability()
    }
    
    private func setupReachability() {
        reachabilityManager?.startListening { [weak self] status in
            guard let self = self else { return }
            
            // Debounce network transitions to prevent freezing
            self.transitionTimer?.invalidate()
            self.transitionTimer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: false) { _ in
                DispatchQueue.main.async {
                    self.handleNetworkChange(status)
                }
            }
        }
    }
    
    private func handleNetworkChange(_ status: NetworkReachabilityManager.NetworkReachabilityStatus) {
        // Only process if status actually changed
        guard status != lastNetworkStatus else { return }
        
        let previousStatus = lastNetworkStatus
        lastNetworkStatus = status
        
        switch status {
        case .notReachable:
            connectionMessage = "No Internet Connection"
            showAlert = true
            isSlowConnection = false
            
        case .reachable(let type):
            // Only show transition message if switching between WiFi and Cellular
            if let previous = previousStatus {
                switch (previous, type) {
                case (.reachable(.ethernetOrWiFi), .cellular):
                    connectionMessage = "Switched to Cellular"
                    showAlert = true
                    // Hide after 2 seconds
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) { [weak self] in
                        self?.showAlert = false
                    }
                    
                case (.reachable(.cellular), .ethernetOrWiFi):
                    connectionMessage = "Connected to WiFi"
                    showAlert = true
                    // Hide after 2 seconds
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) { [weak self] in
                        self?.showAlert = false
                    }
                    
                default:
                    showAlert = false
                }
            } else {
                // First connection
                showAlert = false
            }
            isSlowConnection = false
            
        case .unknown:
            connectionMessage = "Checking Connection..."
            showAlert = false
            isSlowConnection = false
        }
    }
    
    // Method to report slow connection based on actual API performance
    func reportSlowConnection() {
        DispatchQueue.main.async { [weak self] in
            self?.isSlowConnection = true
            self?.connectionMessage = "Slow connection detected"
            self?.showAlert = true
            
            // Auto-hide after 3 seconds
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                self?.showAlert = false
            }
        }
    }
    
    deinit {
        reachabilityManager?.stopListening()
        transitionTimer?.invalidate()
    }
}

// Simple banner view for network status
struct SafeNetworkBanner: View {
    @ObservedObject var monitor = SafeNetworkMonitor.shared
    
    var body: some View {
        if monitor.showAlert {
            VStack {
                HStack {
                    Image(systemName: iconName)
                        .foregroundColor(.white)
                    
                    Text(monitor.connectionMessage)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white)
                    
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(bannerColor)
                .cornerRadius(10)
                .padding(.horizontal, 16)
                .padding(.top, 50)
                
                Spacer()
            }
            .transition(.move(edge: .top).combined(with: .opacity))
            .animation(.easeInOut(duration: 0.3), value: monitor.showAlert)
        }
    }
    
    var iconName: String {
        if monitor.connectionMessage.contains("No Internet") {
            return "wifi.slash"
        } else if monitor.connectionMessage.contains("Cellular") {
            return "antenna.radiowaves.left.and.right"
        } else if monitor.connectionMessage.contains("WiFi") {
            return "wifi"
        } else if monitor.isSlowConnection {
            return "exclamationmark.triangle"
        } else {
            return "network"
        }
    }
    
    var bannerColor: Color {
        if monitor.connectionMessage.contains("No Internet") {
            return .red
        } else if monitor.isSlowConnection {
            return .orange
        } else {
            return .blue
        }
    }
}

// Safe modifier that won't cause freezes
struct SafeNetworkAware: ViewModifier {
    func body(content: Content) -> some View {
        ZStack {
            content
            SafeNetworkBanner()
        }
    }
}

extension View {
    func safeNetworkAware() -> some View {
        self.modifier(SafeNetworkAware())
    }
}