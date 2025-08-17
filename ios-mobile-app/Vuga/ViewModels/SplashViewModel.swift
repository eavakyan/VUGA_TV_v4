//
//  SplashViewModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import Foundation

class SplashViewModel : BaseViewModel {
    @Published var isSettingDataLoaded = false
    @Published var isConnectedToInternet = false

    func fetchSettings(){
        print("SplashViewModel: Starting fetchSettings with 10s timeout")
        NetworkManager.callWebService(url: .fetchSettings,
            timeout: 10, // 10 second timeout for faster startup
            callbackSuccess: { (obj: SettingModel) in
                print("SplashViewModel: Settings fetched successfully")
                if let data = obj.setting {
                    SessionManager.shared.setSetting(data: data)
                }
                SessionManager.shared.setGenres(data: obj.genres ?? [])
                SessionManager.shared.setLanguages(data: obj.languages ?? [])
                SessionManager.shared.setAds(data: obj.admob ?? [])
                // Temporarily disabled - configure valid AdMob IDs
                // RewardedAdManager.shared.loadRewardAd()
                
                // Mark as loaded even if some data is missing
                DispatchQueue.main.async {
                    self.isSettingDataLoaded = true
                }
            },
            callbackFailure: { error in
                print("SplashViewModel: Failed to fetch settings (timeout or error): \(error)")
                // Still mark as loaded to allow app to continue
                DispatchQueue.main.async {
                    self.isSettingDataLoaded = true
                }
            }
        )
        // Fetch ads in parallel, not sequentially
        fetchAds()
    }
    
    func fetchAds(){
        let params : [Params: Any] = [.isIos : 1]
        NetworkManager.callWebService(url: .fetchCustomAds, params: params, timeout: 5, callbackSuccess: {(obj: CustomAdModel) in
            self.stopLoading()
            if let data = obj.data {
                SessionManager.shared.setCustomAds(datum: data)
            }
        }, callbackFailure: { error in
            print("SplashViewModel: Failed to fetch ads: \(error)")
            self.stopLoading()
            // Continue without ads
        })
    }
    
    func fetchProfile() {
        // Only fetch profile if user is logged in
        guard let userId = myUser?.id, userId > 0 else {
            return
        }
        
        let params : [Params: Any] = [.userId : userId]
        NetworkManager.callWebService(url: .fetchProfile, params: params, timeout: 10, callbackSuccess: {(obj: UserModel) in
            if let user = obj.data {
                DispatchQueue.main.async {
                    self.myUser = user
                    print("SplashViewModel: Profile fetched for user \(userId)")
                }
            }
        }, callbackFailure: { error in
            print("SplashViewModel: Failed to fetch profile: \(error)")
            // Continue without updated profile
        })
    }
}

