import SwiftUI
import AVKit

struct VideoNetworkAlert: ViewModifier {
    @ObservedObject var monitor = ConnectionMonitor.shared
    @State private var showBufferingWarning = false
    @State private var bufferingStartTime: Date?
    
    func body(content: Content) -> some View {
        ZStack {
            content
            
            // Show warning overlay for poor connection during video playback
            if monitor.connectionQuality == .poor || monitor.connectionQuality == .fair {
                VStack {
                    Spacer()
                    HStack {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.yellow)
                            .font(.system(size: 18))
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Slow Connection Detected")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(.white)
                            
                            Text("Video quality may be reduced")
                                .font(.system(size: 12))
                                .foregroundColor(.white.opacity(0.8))
                        }
                        
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(
                        RoundedRectangle(cornerRadius: 10)
                            .fill(Color.black.opacity(0.8))
                    )
                    .padding(.horizontal, 20)
                    .padding(.bottom, 100)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .animation(.spring(response: 0.3), value: monitor.connectionQuality)
            }
            
            // Buffering indicator with connection info
            if showBufferingWarning {
                VStack(spacing: 12) {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .scaleEffect(1.5)
                    
                    Text("Buffering...")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white)
                    
                    if monitor.connectionQuality == .poor || monitor.connectionQuality == .fair {
                        Text("Slow connection detected")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.7))
                    }
                }
                .padding(24)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color.black.opacity(0.85))
                )
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: .AVPlayerItemPlaybackStalled)) { _ in
            showBufferingWarning = true
            bufferingStartTime = Date()
            
            // Auto-hide after detecting playback resumed or timeout
            DispatchQueue.main.asyncAfter(deadline: .now() + 10) {
                showBufferingWarning = false
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: .AVPlayerItemDidPlayToEndTime)) { _ in
            showBufferingWarning = false
        }
    }
}

extension View {
    func videoNetworkAlert() -> some View {
        self.modifier(VideoNetworkAlert())
    }
}

// Enhanced video player with automatic quality adjustment
struct NetworkAwareVideoPlayer: View {
    let url: URL
    @StateObject private var monitor = ConnectionMonitor.shared
    @State private var player: AVPlayer
    @State private var preferredQuality: String = "auto"
    
    init(url: URL) {
        self.url = url
        self._player = State(initialValue: AVPlayer(url: url))
    }
    
    var body: some View {
        VideoPlayer(player: player)
            .videoNetworkAlert()
            .onAppear {
                setupPlayer()
            }
            .onChange(of: monitor.connectionQuality) { quality in
                adjustVideoQuality(for: quality)
            }
    }
    
    private func setupPlayer() {
        // Configure for network conditions
        player.automaticallyWaitsToMinimizeStalling = true
        
        if let currentItem = player.currentItem {
            // Set buffer preferences based on connection
            switch monitor.connectionQuality {
            case .excellent, .good:
                currentItem.preferredForwardBufferDuration = 5
            case .fair:
                currentItem.preferredForwardBufferDuration = 10
            case .poor:
                currentItem.preferredForwardBufferDuration = 15
            case .offline:
                break
            }
        }
        
        player.play()
    }
    
    private func adjustVideoQuality(for quality: ConnectionQuality) {
        // This would integrate with your HLS or adaptive streaming setup
        // to automatically select appropriate quality variant
        switch quality {
        case .excellent:
            preferredQuality = "1080p"
        case .good:
            preferredQuality = "720p"
        case .fair:
            preferredQuality = "480p"
        case .poor:
            preferredQuality = "360p"
        case .offline:
            player.pause()
        }
        
        print("Adjusting video quality to: \(preferredQuality)")
    }
}