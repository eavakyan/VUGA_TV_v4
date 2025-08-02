//
//  BaseViewModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import SwiftUI
// import RevenueCat - Disabled temporarily

class BaseViewModel : NSObject, ObservableObject {
    @AppStorage(SessionKeys.myUser) var myUser : User? = nil
    @AppStorage(SessionKeys.isPro) var isPro = false
    @Published var isLoading = false
    @Published var dummy = false
    static var shared = BaseViewModel()
    
    func startLoading(){
        DispatchQueue.main.async {
            self.isLoading = true
        }
    }
    
    func stopLoading(){
        DispatchQueue.main.async {
            self.isLoading = false
        }
    }
    
    func reset(){
        DispatchQueue.main.async {
            self.dummy.toggle()
        }
    }
    
    func commonProfileEdit(params: [Params: Any], completion: @escaping ((_ user: User) -> ()) = { _ in}) {
        var params = params
        params[.userId] = myUser?.id ?? 0
        params[.appUserId] = myUser?.id ?? 0  // For V2 API compatibility
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        NetworkManager.callWebService(url: .updateProfile, params: params) { (obj: UserModel) in
            if let data = obj.data {
                self.myUser = data
                completion(data)
            }
        }
    }
    
    func hideTabbar(){
        NotificationCenter.default.post(name: .hideTabbar, object: nil)
    }
    
    func showTabbar(){
        NotificationCenter.default.post(name: .showTabbar, object: nil)
    }
    
    func checkUserIsPro(customerInfo: Any? = nil) {
        // RevenueCat disabled - always return free user for now
        // To enable premium features without RevenueCat, set isPro = true
        isPro = false
    }
    
    func toggleFavorite(contentId: Int, completion: @escaping ((_ success: Bool, _ message: String?) -> ())) {
        guard let user = myUser else {
            completion(false, "Please login to add favorites")
            return
        }
        
        var params: [Params: Any] = [
            .appUserId: user.id,
            .contentId: contentId
        ]
        
        if let profileId = user.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .toggleFavorite, params: params, callbackSuccess: { [weak self] (obj: UserModel) in
            if let data = obj.data {
                self?.myUser = data
                completion(true, obj.message)
                // Post notification to update UI
                NotificationCenter.default.post(name: Notification.Name("FavoriteUpdated"), object: nil, userInfo: ["contentId": contentId])
            } else {
                completion(false, obj.message)
            }
        }, callbackFailure: { error in
            completion(false, error.localizedDescription)
        })
    }
    
    func rateContent(contentId: Int, rating: Float, completion: @escaping ((_ success: Bool, _ message: String?) -> ())) {
        guard let user = myUser else {
            completion(false, "Please login to rate content")
            return
        }
        
        var params: [Params: Any] = [
            .appUserId: user.id,
            .contentId: contentId,
            .rating: rating
        ]
        
        if let profileId = user.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .rateContent, params: params, callbackSuccess: { (obj: StatusAndMessageModel) in
            completion(obj.status ?? false, obj.message)
            // Post notification to update UI
            NotificationCenter.default.post(name: Notification.Name("RatingUpdated"), object: nil, userInfo: ["contentId": contentId, "rating": rating])
        }, callbackFailure: { error in
            completion(false, error.localizedDescription)
        })
    }
    
    func updateWatchProgress(contentId: Int? = nil, episodeId: Int? = nil, position: Int, duration: Int, completion: @escaping ((_ success: Bool) -> ()) = { _ in }) {
        guard let user = myUser else {
            completion(false)
            return
        }
        
        var params: [Params: Any] = [
            .appUserId: user.id,
            .lastWatchedPosition: position,
            .totalDuration: duration,
            .deviceType: 1 // iOS
        ]
        
        if let profileId = user.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        if let contentId = contentId {
            params[.contentId] = contentId
        }
        
        if let episodeId = episodeId {
            params[.episodeId] = episodeId
        }
        
        NetworkManager.callWebService(url: .updateWatchProgress, params: params, callbackSuccess: { (obj: StatusAndMessageModel) in
            completion(obj.status ?? false)
        }, callbackFailure: { _ in
            completion(false)
        })
    }
}

