//
//  SplashViewModel.swift
//  Vuga
//
//

import Foundation

class SplashViewModel : BaseViewModel {
    @Published var isSettingDataLoaded = false
    @Published var isConnectedToInternet = false

    func fetchSettings(){
        print("SplashViewModel: Starting fetchSettings with 10s timeout")
        
        // Check if languages are empty and clear them to force re-fetch
        let currentLanguages = SessionManager.shared.getLanguages()
        if currentLanguages.isEmpty {
            print("SplashViewModel: Detected empty languages, clearing storage to force re-fetch")
            SessionManager.shared.clearLanguages()
        }
        
        NetworkManager.callWebService(url: .fetchSettings,
            timeout: 10, // 10 second timeout for faster startup
            callbackSuccess: { (obj: SettingModel) in
                print("SplashViewModel: Settings fetched successfully")
                if let data = obj.setting {
                    SessionManager.shared.setSetting(data: data)
                }
                SessionManager.shared.setGenres(data: obj.genres ?? [])
                
                // Debug logging for languages
                if let languages = obj.languages {
                    print("SplashViewModel: Received \(languages.count) languages from API")
                    if languages.isEmpty {
                        print("SplashViewModel: WARNING - Languages array is empty from API!")
                    } else {
                        for lang in languages {
                            print("  - Language: \(lang.title ?? "nil") (id: \(lang.id ?? 0))")
                        }
                    }
                    SessionManager.shared.setLanguages(data: languages)
                } else {
                    print("SplashViewModel: ERROR - languages field is nil in API response!")
                    print("SplashViewModel: Full response object keys: status=\(obj.status ?? false), message=\(obj.message ?? "nil")")
                    // Don't overwrite with empty array if languages field is missing
                    // SessionManager.shared.setLanguages(data: [])
                }
                
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

