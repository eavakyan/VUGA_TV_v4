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
        NetworkManager.callWebService(url: .fetchSettings, 
            callbackSuccess: { (obj: SettingModel) in
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
                print("Failed to fetch settings: \(error)")
                // Still mark as loaded to allow app to continue
                DispatchQueue.main.async {
                    self.isSettingDataLoaded = true
                }
            }
        )
        fetchAds()
    }
    
    func fetchAds(){
        let params : [Params: Any] = [.isIos : 1]
        NetworkManager.callWebService(url: .fetchCustomAds, params: params, callbackSuccess: {(obj: CustomAdModel) in
            self.stopLoading()
            if let data = obj.data {
                SessionManager.shared.setCustomAds(datum: data)
            }
        })
    }
    
    func fetchProfile() {
        // Only fetch profile if user is logged in
        guard let userId = myUser?.id, userId > 0 else {
            return
        }
        
        let params : [Params: Any] = [.userId : userId]
        NetworkManager.callWebService(url: .fetchProfile,params: params){(obj: UserModel) in
            if let user = obj.data {
                DispatchQueue.main.async {
                    self.myUser = user
                    print("=======", user)
                }
            }
        }
    }
}

