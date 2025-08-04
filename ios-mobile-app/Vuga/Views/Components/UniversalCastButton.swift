//
//  UniversalCastButton.swift
//  Vuga
//
//  Created by AI Assistant on 2025-08-03.
//

import SwiftUI
import GoogleCast

struct UniversalCastButton: View {
    @StateObject private var castManager = UniversalCastManager.shared
    @State private var showDevicePicker = false
    
    var body: some View {
        Menu {
            if castManager.availableDevices.isEmpty {
                Label("No devices found", systemImage: "tv.slash")
                    .foregroundColor(.gray)
            } else {
                ForEach(castManager.availableDevices, id: \.id) { device in
                    Button(action: {
                        if device.isConnected {
                            castManager.disconnect()
                        } else {
                            // Cast logic will be handled by parent view
                            NotificationCenter.default.post(
                                name: Notification.Name("CastToDevice"),
                                object: nil,
                                userInfo: ["device": device]
                            )
                        }
                    }) {
                        HStack {
                            Text(device.name)
                            Spacer()
                            if device.isConnected {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.green)
                            }
                        }
                    }
                }
                
                Divider()
                
                if castManager.connectedDevice != nil {
                    Button(action: {
                        castManager.disconnect()
                    }) {
                        Label("Disconnect", systemImage: "xmark.circle")
                            .foregroundColor(.red)
                    }
                }
            }
            
            Divider()
            
            Button(action: {
                castManager.startDeviceDiscovery()
            }) {
                Label("Refresh Devices", systemImage: "arrow.clockwise")
            }
        } label: {
            ZStack {
                // Base cast icon
                Image(systemName: castManager.connectedDevice != nil ? "tv.inset.filled" : "tv")
                    .resizable()
                    .frame(width: 24, height: 24)
                    .foregroundColor(castManager.connectedDevice != nil ? .primary : .white)
                
                // Connection indicator
                if castManager.connectedDevice != nil {
                    Image(systemName: "wifi")
                        .resizable()
                        .frame(width: 10, height: 10)
                        .foregroundColor(.green)
                        .offset(x: 8, y: -8)
                }
                
                // Scanning indicator
                if castManager.isScanning {
                    ProgressView()
                        .scaleEffect(0.5)
                        .offset(x: 8, y: -8)
                }
            }
            .frame(width: 44, height: 44)
            .contentShape(Rectangle())
        }
        .onAppear {
            // Start passive discovery when view appears
            castManager.startDeviceDiscovery()
        }
    }
}

// MARK: - Google Cast Button Wrapper
struct GoogleCastButton: UIViewRepresentable {
    func makeUIView(context: Context) -> GCKUICastButton {
        let castButton = GCKUICastButton()
        castButton.tintColor = .white
        return castButton
    }
    
    func updateUIView(_ uiView: GCKUICastButton, context: Context) {
        // Update button if needed
    }
}

// MARK: - Combined Cast View
struct CombinedCastView: View {
    @StateObject private var castManager = UniversalCastManager.shared
    @State private var showAirPlayPicker = false
    
    var body: some View {
        HStack(spacing: 8) {
            // Google Cast button (native)
            if castManager.availableDevices.contains(where: { $0.type == .googleCast }) {
                GoogleCastButton()
                    .frame(width: 44, height: 44)
            }
            
            // AirPlay button (native)
            AirPlayRoutePickerView(isConnected: castManager.connectedDevice?.type == .airplay)
                .frame(width: 44, height: 44)
            
            // Universal button for all devices
            UniversalCastButton()
        }
    }
}