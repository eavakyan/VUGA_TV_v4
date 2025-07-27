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
    var body: some View {
        if !vm.isSettingDataLoaded {
            Image.logoHorizontal
                .resizeFitTo(width: Device.width / 3, height: 40)
                .addBackground()
                .onAppear {
                    vm.fetchSettings()
                    vm.fetchProfile()
                }
                .onChange(of: vm.isConnectedToInternet, perform: { _ in
                    vm.fetchSettings()
                    vm.fetchProfile()
                })
        } else if isLoggedIn {
                TabBarView()
        } else {
            LoginView()
        }
    }
}
