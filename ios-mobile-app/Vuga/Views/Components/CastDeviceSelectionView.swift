//
//  CastDeviceSelectionView.swift
//  Vuga
//
//

import SwiftUI
import GoogleCast

struct CastDeviceSelectionView: View {
    let content: VugaContent?
    let episode: Episode
    @Binding var isPresented: Bool
    
    @StateObject private var castManager = UniversalCastManager.shared
    @State private var isCasting = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Header
                VStack(spacing: 12) {
                    Text("Cast to Device")
                        .font(.title2)
                        .fontWeight(.semibold)
                        .foregroundColor(.white)
                    
                    if let episodeTitle = episode.title {
                        Text(episodeTitle)
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.7))
                            .multilineTextAlignment(.center)
                    }
                }
                .padding(.top, 20)
                .padding(.horizontal, 20)
                
                Divider()
                    .background(Color.white.opacity(0.2))
                    .padding(.horizontal, 20)
                    .padding(.vertical, 20)
                
                // Device List
                if castManager.availableDevices.isEmpty && !castManager.isScanning {
                    VStack(spacing: 16) {
                        Image(systemName: "tv.slash")
                            .font(.system(size: 48))
                            .foregroundColor(.white.opacity(0.5))
                        
                        Text("No devices found")
                            .font(.headline)
                            .foregroundColor(.white.opacity(0.7))
                        
                        Text("Make sure your casting devices are on the same Wi-Fi network")
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.5))
                            .multilineTextAlignment(.center)
                        
                        Button("Scan Again") {
                            castManager.startDeviceDiscovery()
                        }
                        .foregroundColor(.blue)
                        .padding(.top, 8)
                    }
                    .padding(.horizontal, 40)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(castManager.availableDevices, id: \.id) { device in
                                DeviceRow(
                                    device: device,
                                    isCasting: isCasting,
                                    onTap: {
                                        castToDevice(device)
                                    }
                                )
                            }
                            
                            if castManager.isScanning {
                                HStack {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .scaleEffect(0.8)
                                    
                                    Text("Scanning for devices...")
                                        .font(.caption)
                                        .foregroundColor(.white.opacity(0.7))
                                }
                                .padding(.vertical, 20)
                            }
                        }
                        .padding(.horizontal, 20)
                    }
                }
                
                Spacer()
                
                // Connected Device Info
                if let connectedDevice = castManager.connectedDevice {
                    VStack(spacing: 8) {
                        Divider()
                            .background(Color.white.opacity(0.2))
                        
                        HStack {
                            Image(systemName: "wifi")
                                .foregroundColor(.green)
                            
                            Text("Connected to \\(connectedDevice.name)")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.8))
                            
                            Spacer()
                            
                            Button("Disconnect") {
                                castManager.disconnect()
                            }
                            .font(.caption)
                            .foregroundColor(.red)
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 12)
                    }
                    .background(Color.black.opacity(0.3))
                }
            }
            .background(Color.black)
            .navigationBarItems(
                trailing: Button("Close") {
                    isPresented = false
                }
                .foregroundColor(.white)
            )
        }
        .preferredColorScheme(.dark)
        .onAppear {
            if castManager.availableDevices.isEmpty {
                castManager.startDeviceDiscovery()
            }
        }
    }
    
    private func castToDevice(_ device: CastDevice) {
        guard let content = content,
              let firstSource = episode.sources?.first else {
            return
        }
        
        isCasting = true
        
        // Create a Source object for casting
        let source = Source()
        source.sourceUrl = firstSource.sourceURL.absoluteString
        source.type = firstSource.type
        
        castManager.cast(to: device, content: content, source: source, episode: episode)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            isCasting = false
            isPresented = false
        }
    }
}

struct DeviceRow: View {
    let device: CastDevice
    let isCasting: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 16) {
                // Device icon
                Image(systemName: deviceIcon)
                    .font(.system(size: 24))
                    .foregroundColor(device.isConnected ? .green : .white)
                    .frame(width: 32)
                
                // Device info
                VStack(alignment: .leading, spacing: 4) {
                    Text(device.name)
                        .font(.headline)
                        .foregroundColor(.white)
                        .multilineTextAlignment(.leading)
                    
                    Text(deviceTypeDescription)
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.6))
                }
                
                Spacer()
                
                // Status indicator
                if device.isConnected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                } else if isCasting {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .scaleEffect(0.8)
                } else {
                    Image(systemName: "chevron.right")
                        .foregroundColor(.white.opacity(0.3))
                }
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 16)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(device.isConnected ? Color.green.opacity(0.1) : Color.white.opacity(0.05))
                    .stroke(device.isConnected ? Color.green.opacity(0.3) : Color.clear, lineWidth: 1)
            )
        }
        .disabled(isCasting)
    }
    
    private var deviceIcon: String {
        switch device.type {
        case .airplay:
            return "airplayaudio"
        case .googleCast:
            return "tv"
        case .dlna:
            return "tv.and.mediabox"
        case .unknown:
            return "questionmark.circle"
        }
    }
    
    private var deviceTypeDescription: String {
        switch device.type {
        case .airplay:
            return "AirPlay"
        case .googleCast:
            return "Google Cast"
        case .dlna:
            return "DLNA"
        case .unknown:
            return "Unknown"
        }
    }
}

#Preview {
    CastDeviceSelectionView(
        content: nil,
        episode: Episode(),
        isPresented: .constant(true)
    )
}