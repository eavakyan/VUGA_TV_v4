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
        NetworkManager.callWebService(url: .fetchSettings) { (obj: SettingModel) in
            if let data = obj.setting {
                SessionManager.shared.setSetting(data: data)
                DispatchQueue.main.async {
                    self.isSettingDataLoaded = true
                }
            }
            SessionManager.shared.setGenres(data: obj.genres ?? [])
            SessionManager.shared.setLanguages(data: obj.languages ?? [])
            SessionManager.shared.setAds(data: obj.admob ?? [])
            RewardedAdManager.shared.loadRewardAd()
        }
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
        let params : [Params: Any] = [.userId : myUser?.id ?? 0]
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

