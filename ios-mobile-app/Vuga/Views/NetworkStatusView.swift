import SwiftUI

struct NetworkStatusBanner: View {
    @ObservedObject var monitor = ConnectionMonitor.shared
    @State private var showDetails = false
    
    var body: some View {
        if monitor.showConnectionAlert {
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    Image(systemName: monitor.connectionQuality.iconName)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white)
                    
                    Text(monitor.connectionQuality.displayText)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    if monitor.connectionQuality != .offline {
                        Button(action: {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                showDetails.toggle()
                            }
                        }) {
                            Image(systemName: showDetails ? "chevron.up" : "chevron.down")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(.white.opacity(0.8))
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(backgroundGradient)
                
                if showDetails && monitor.connectionQuality != .offline {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Text("Connection Type:")
                                .font(.system(size: 12))
                                .foregroundColor(.white.opacity(0.7))
                            Text(connectionTypeText)
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(.white)
                        }
                        
                        if monitor.latency > 0 {
                            HStack {
                                Text("Response Time:")
                                    .font(.system(size: 12))
                                    .foregroundColor(.white.opacity(0.7))
                                Text(String(format: "%.1f seconds", monitor.latency))
                                    .font(.system(size: 12, weight: .medium))
                                    .foregroundColor(.white)
                            }
                        }
                        
                        if monitor.connectionQuality == .poor || monitor.connectionQuality == .fair {
                            Text("Some features may be slow or unavailable")
                                .font(.system(size: 11))
                                .foregroundColor(.white.opacity(0.7))
                                .padding(.top, 4)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)
                    .background(backgroundGradient.opacity(0.95))
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
            .padding(.horizontal, 16)
            .transition(.move(edge: .top).combined(with: .opacity))
            .gesture(
                DragGesture()
                    .onEnded { value in
                        if value.translation.height < -20 {
                            withAnimation {
                                monitor.showConnectionAlert = false
                            }
                        }
                    }
            )
        }
    }
    
    var backgroundGradient: LinearGradient {
        let color = monitor.connectionQuality.color
        return LinearGradient(
            gradient: Gradient(colors: [
                color.opacity(0.9),
                color.opacity(0.8)
            ]),
            startPoint: .leading,
            endPoint: .trailing
        )
    }
    
    var connectionTypeText: String {
        guard let type = monitor.connectionType else {
            return "Unknown"
        }
        
        switch type {
        case .wifi:
            return "Wi-Fi"
        case .cellular:
            return "Cellular"
        default:
            return "Other"
        }
    }
}

// Floating connection indicator for persistent display
struct NetworkStatusIndicator: View {
    @ObservedObject var monitor = ConnectionMonitor.shared
    @State private var isAnimating = false
    
    var body: some View {
        if monitor.connectionQuality == .poor || 
           monitor.connectionQuality == .fair || 
           monitor.connectionQuality == .offline {
            HStack(spacing: 6) {
                Image(systemName: monitor.connectionQuality.iconName)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(indicatorColor)
                    .scaleEffect(isAnimating ? 1.1 : 1.0)
                
                if monitor.connectionQuality == .offline {
                    Text("Offline")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(indicatorColor)
                }
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(indicatorColor.opacity(0.15))
                    .overlay(
                        Capsule()
                            .stroke(indicatorColor.opacity(0.3), lineWidth: 1)
                    )
            )
            .onAppear {
                if monitor.connectionQuality == .offline {
                    withAnimation(.easeInOut(duration: 1).repeatForever(autoreverses: true)) {
                        isAnimating = true
                    }
                }
            }
            .onDisappear {
                isAnimating = false
            }
            .onTapGesture {
                monitor.showConnectionAlert = true
            }
        }
    }
    
    var indicatorColor: Color {
        switch monitor.connectionQuality {
        case .offline:
            return .red
        case .poor:
            return .orange
        case .fair:
            return .yellow
        default:
            return .green
        }
    }
}

// View modifier to add network monitoring to any view
struct NetworkAwareModifier: ViewModifier {
    @ObservedObject var monitor = ConnectionMonitor.shared
    
    func body(content: Content) -> some View {
        ZStack(alignment: .top) {
            content
            
            NetworkStatusBanner()
                .padding(.top, 50) // Adjust based on your navigation bar
                .animation(.spring(response: 0.3, dampingFraction: 0.8), value: monitor.showConnectionAlert)
        }
    }
}

extension View {
    func networkAware() -> some View {
        self.modifier(NetworkAwareModifier())
    }
}