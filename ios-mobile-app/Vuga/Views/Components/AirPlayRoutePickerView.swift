//
//  AirPlayRoutePickerView.swift
//  Vuga
//
//  Created for AirPlay support
//

import SwiftUI
import AVKit

struct AirPlayRoutePickerView: UIViewRepresentable {
    var isConnected: Bool
    
    func makeUIView(context: Context) -> AVRoutePickerView {
        let routePickerView = AVRoutePickerView()
        routePickerView.backgroundColor = UIColor.clear
        routePickerView.tintColor = isConnected ? UIColor.systemBlue : UIColor.white
        routePickerView.activeTintColor = UIColor.systemBlue
        routePickerView.prioritizesVideoDevices = true
        return routePickerView
    }
    
    func updateUIView(_ uiView: AVRoutePickerView, context: Context) {
        // Update tint color based on connection status
        uiView.tintColor = isConnected ? UIColor.systemBlue : UIColor.white
    }
}