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
    @State private var showProfileSelection = false
    
    var body: some View {
        if !vm.isSettingDataLoaded {
            Image.logoHorizontal
                .resizeFitTo(width: Device.width / 3, height: 40)
                .addBackground()
                .onAppear {
                    print("SplashView: onAppear - fetching settings and profile")
                    vm.fetchSettings()
                    vm.fetchProfile()
                }
                .onChange(of: vm.isConnectedToInternet, perform: { _ in
                    print("SplashView: Internet connection changed - refetching")
                    vm.fetchSettings()
                    vm.fetchProfile()
                })
                .onChange(of: vm.isSettingDataLoaded) { loaded in
                    print("SplashView: Settings loaded changed to: \(loaded)")
                    if loaded {
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
                ProfileSelectionView()
                    .onDisappear {
                        showProfileSelection = false
                    }
            } else {
                TabBarView()
            }
        } else {
            LoginView()
                .onAppear {
                    showProfileSelection = false
                }
        }
    }
    
    func checkProfileSelection() {
        // Always show profile selection on app start if logged in
        print("SplashView: checkProfileSelection - isLoggedIn: \(isLoggedIn)")
        if isLoggedIn {
            DispatchQueue.main.async {
                print("SplashView: Setting showProfileSelection to true")
                self.showProfileSelection = true
            }
        }
    }
}
