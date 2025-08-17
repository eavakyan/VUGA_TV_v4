//
//  SplashView.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
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
                    print("SplashView: onAppear - fetching settings")
                    // Clear the current profile to ensure profile selection is shown
                    SessionManager.shared.clearProfile()
                    
                    // Start fetching settings
                    self.vm.fetchSettings()
                    
                    // Only fetch profile if user is logged in
                    if isLoggedIn {
                        self.vm.fetchProfile()
                    }
                    
                    // Force continue after 2 seconds regardless of settings load status
                    forceLoadTimeout = Timer.scheduledTimer(withTimeInterval: 2.0, repeats: false) { _ in
                        print("SplashView: Force timeout after 2s - continuing without settings")
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
        // Always show profile selection on app start if logged in
        print("SplashView: checkProfileSelection - isLoggedIn: \(isLoggedIn)")
        if isLoggedIn {
            // Clear any saved profile to ensure selection screen is shown
            SessionManager.shared.clearProfile()
            DispatchQueue.main.async {
                print("SplashView: Setting showProfileSelection to true")
                self.showProfileSelection = true
            }
        }
    }
}
