//
//  VugaApp.swift
//  Vuga
//
//

import SwiftUI
import ActivityKit
import AVKit
import GoogleSignIn

@main
struct VugaApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @Environment(\.scenePhase) private var scenePhase
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Handle Google Sign-In callback
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
        .onChange(of: scenePhase) { phase in
            switch phase {
            case .background, .inactive:
                // App is going to background - sync watch history
                print("VugaApp: App entering background, triggering watch history sync")
                WatchHistorySyncService.shared.forceSync()
            case .active:
                // App became active - no need for cross-device sync in this simple version
                print("VugaApp: App became active")
            @unknown default:
                break
            }
        }
    }
}



