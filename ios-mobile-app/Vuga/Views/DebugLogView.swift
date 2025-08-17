//
//  DebugLogView.swift
//  Vuga
//
//  View to display logs in-app for debugging
//

import SwiftUI

struct DebugLogView: View {
    @State private var logs = AppLogger.getRecentLogs()
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    var body: some View {
        NavigationView {
            ScrollView {
                Text(logs.isEmpty ? "No logs available" : logs)
                    .font(.system(size: 12, design: .monospaced))
                    .foregroundColor(.white)
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .background(Color.black)
            .navigationTitle("Debug Logs")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Close") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Clear") {
                        AppLogger.clearLogs()
                        logs = ""
                    }
                }
            }
        }
    }
}

// Add this to any view to show logs with a shake gesture
struct ShakeToShowLogs: ViewModifier {
    @State private var showingLogs = false
    
    func body(content: Content) -> some View {
        content
            .onShake {
                #if DEBUG
                showingLogs = true
                #endif
            }
            .sheet(isPresented: $showingLogs) {
                DebugLogView()
            }
    }
}

extension View {
    func shakeToShowLogs() -> some View {
        modifier(ShakeToShowLogs())
    }
}

// Shake detection
extension UIDevice {
    static let deviceDidShakeNotification = Notification.Name(rawValue: "deviceDidShakeNotification")
}

extension UIWindow {
    open override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        if motion == .motionShake {
            NotificationCenter.default.post(name: UIDevice.deviceDidShakeNotification, object: nil)
        }
    }
}

struct DeviceShakeViewModifier: ViewModifier {
    let action: () -> Void
    
    func body(content: Content) -> some View {
        content
            .onReceive(NotificationCenter.default.publisher(for: UIDevice.deviceDidShakeNotification)) { _ in
                action()
            }
    }
}

extension View {
    func onShake(perform action: @escaping () -> Void) -> some View {
        self.modifier(DeviceShakeViewModifier(action: action))
    }
}