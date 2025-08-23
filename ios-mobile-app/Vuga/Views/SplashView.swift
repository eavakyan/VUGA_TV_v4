//
//  SplashView.swift
//  Vuga
//
//

import SwiftUI
import Alamofire

struct SplashView: View {
    @StateObject var vm : SplashViewModel
    @AppStorage(SessionKeys.isLoggedIn) var isLoggedIn = false
    @AppStorage(SessionKeys.myUser) var myUser : User? = nil
    @State private var showProfileSelection = true  // Default to true to show profile selection
    @State private var forceLoadTimeout: Timer?
    
    var body: some View {
        if !vm.isSettingDataLoaded {
            Image.logoHorizontal
                .resizeFitTo(width: Device.width / 3, height: 40)
                .addBackground()
                .onAppear {
                    print("SplashView: onAppear - checking connection status")
                    
                    let connectionMonitor = ConnectionMonitor.shared
                    
                    // Only clear profile if we're online (to force profile selection)
                    // Keep cached profile if offline for better UX
                    if connectionMonitor.isConnected && connectionMonitor.connectionQuality != .poor {
                        print("SplashView: Online - clearing profile for fresh selection")
                        SessionManager.shared.clearProfile()
                    } else {
                        print("SplashView: Offline/Poor connection - keeping cached profile if available")
                    }
                    
                    // Start fetching settings
                    self.vm.fetchSettings()
                    
                    // Only fetch profile if user is logged in
                    if isLoggedIn {
                        self.vm.fetchProfile()
                    }
                    
                    // Force continue after 3 seconds for offline mode (faster startup)
                    let timeout = connectionMonitor.isConnected ? 5.0 : 3.0
                    forceLoadTimeout = Timer.scheduledTimer(withTimeInterval: timeout, repeats: false) { _ in
                        print("SplashView: Force timeout after \(timeout)s - continuing without settings")
                        if !vm.isSettingDataLoaded {
                            DispatchQueue.main.async {
                                vm.isSettingDataLoaded = true
                            }
                        }
                    }
                }
                .onChange(of: vm.isConnectedToInternet, perform: { connected in
                    if connected {
                        print("SplashView: Internet connection restored - refetching")
                        self.vm.fetchSettings()
                    }
                })
                .onChange(of: vm.isSettingDataLoaded) { loaded in
                    print("SplashView: Settings loaded changed to: \(loaded)")
                    if loaded {
                        // Cancel the force timeout since settings loaded
                        forceLoadTimeout?.invalidate()
                        forceLoadTimeout = nil
                        checkProfileSelection()
                    }
                }
                .onChange(of: isLoggedIn) { loggedIn in
                    print("SplashView: isLoggedIn changed to: \(loggedIn), settings loaded: \(vm.isSettingDataLoaded)")
                    if loggedIn && vm.isSettingDataLoaded {
                        checkProfileSelection()
                    }
                }
        } else if isLoggedIn {
            if showProfileSelection {
                ProfileSelectionView(onProfileSelected: {
                    // Profile was selected, transition to main app
                    showProfileSelection = false
                })
            } else {
                TabBarView()
            }
        } else {
            LoginView()
                .onAppear {
                    // Reset to true for next login
                    showProfileSelection = true
                }
        }
    }
    
    func checkProfileSelection() {
        print("SplashView: checkProfileSelection - isLoggedIn: \(isLoggedIn)")
        if isLoggedIn {
            let connectionMonitor = ConnectionMonitor.shared
            
            // If offline and we have a cached profile, skip profile selection
            if !connectionMonitor.isConnected || connectionMonitor.connectionQuality == .poor {
                if let cachedProfile = SessionManager.shared.currentProfile {
                    print("SplashView: Offline mode - using cached profile: \(cachedProfile.name)")
                    // Don't show profile selection, go straight to app
                    DispatchQueue.main.async {
                        self.showProfileSelection = false
                    }
                    return
                }
            }
            
            // Online mode or no cached profile - show profile selection
            if connectionMonitor.isConnected && connectionMonitor.connectionQuality != .poor {
                SessionManager.shared.clearProfile()
            }
            DispatchQueue.main.async {
                print("SplashView: Setting showProfileSelection to true")
                self.showProfileSelection = true
            }
        }
    }
}
