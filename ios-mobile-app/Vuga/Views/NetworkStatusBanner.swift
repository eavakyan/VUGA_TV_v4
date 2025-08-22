//
//  NetworkStatusBanner.swift
//  Vuga
//
//  Visual network status indicator
//

import SwiftUI

struct NetworkStatusBanner: View {
    @StateObject private var monitor = ConnectionMonitor.shared
    @State private var showBanner = false
    @State private var message = ""
    @State private var bannerColor = Color.red
    
    var body: some View {
        VStack {
            if showBanner {
                HStack {
                    Image(systemName: monitor.isConnected ? "wifi.exclamationmark" : "wifi.slash")
                        .foregroundColor(.white)
                    
                    Text(message)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    if !monitor.isConnected {
                        Button(action: {
                            // Navigate to downloads or dismiss
                        }) {
                            Text("View Downloads")
                                .font(.system(size: 12))
                                .foregroundColor(.white)
                                .underline()
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 10)
                .background(bannerColor)
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .onReceive(monitor.$isConnected) { isConnected in
            updateBanner()
        }
        .onReceive(monitor.$connectionQuality) { quality in
            updateBanner()
        }
        .onAppear {
            updateBanner()
        }
    }
    
    private func updateBanner() {
        withAnimation(.easeInOut(duration: 0.3)) {
            if !monitor.isConnected {
                showBanner = true
                message = "No Internet Connection"
                bannerColor = Color.red
            } else if monitor.connectionQuality == .poor {
                showBanner = true
                message = "Slow Connection Detected"
                bannerColor = Color.orange
            } else {
                showBanner = false
            }
        }
    }
}

// Extension to add network banner to any view
extension View {
    func withNetworkStatusBanner() -> some View {
        VStack(spacing: 0) {
            NetworkStatusBanner()
            self
        }
    }
}