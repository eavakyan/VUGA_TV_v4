//
//  ProfileViewModel.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 31/05/24.
//

import Foundation
import SwiftUI
// import RevenueCat - Disabled temporarily
import UIKit

class ProfileViewModel : BaseViewModel {
    
    @Published var isLogoutDialogShow = false
    @Published var isDeleteDialogShow = false
    @Published var isTermsURLSheet = false
    @Published var isRatingAppSheet = false
    @Published var isPrivacyURLSheet = false
    @Published var showProfileSelection = false
    @Published var showAgeSettings = false
    @Published var ageRatings: [AgeRating] = []
    @Published var selectedAge: Int?
    @Published var isKidsProfile: Bool = false
    @AppStorage(SessionKeys.isLoggedIn) var isLoggedIn = false

    
    func deleteMyAc() {
        startLoading()
        let param : [Params : Any] = [.userId: myUser?.id ?? 0]
        NetworkManager.callWebService(url: .deleteMyAccount,params: param) {(obj: DeleteAccountModel) in
            if obj.status == true {
                self.isDeleteDialogShow = false
                SessionManager.shared.clear()
                // Force app restart by dispatching to main queue
                DispatchQueue.main.async {
                    // RevenueCat disabled - directly restart app flow
                    // Force navigation to root and restart app flow
                    if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                       let window = windowScene.windows.first {
                        window.rootViewController = UIHostingController(rootView: ContentView())
                        window.makeKeyAndVisible()
                    }
                }
            }
            self.stopLoading()
        }
    }

    func logOutMyAc() {
        print("ProfileViewModel logOutMyAc - userId: \(myUser?.id ?? 0)")
        
        // If user is not logged in (userId = 0), just clear session locally
        if myUser?.id == nil || myUser?.id == 0 {
            print("User not logged in, clearing session locally")
            self.isLogoutDialogShow = false
            self.isLoggedIn = false
            SessionManager.shared.clear()
            // Force app restart by dispatching to main queue
            DispatchQueue.main.async {
                // Force navigation to root and restart app flow
                if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                   let window = windowScene.windows.first {
                    window.rootViewController = UIHostingController(rootView: ContentView())
                    window.makeKeyAndVisible()
                }
            }
            return
        }
        
        startLoading()
        let params : [Params: Any] = [.userId : myUser?.id ?? 0]
        NetworkManager.callWebService(url: .logOut, params: params, callbackSuccess: { [weak self] (obj: DeleteAccountModel) in
            print("Logout Response:")
            print("Status: \(obj.status ?? false)")
            print("Message: \(obj.message ?? "")")
            
            if obj.status == true {
                self?.isLogoutDialogShow = false
                self?.isLoggedIn = false
                SessionManager.shared.clear()
                // Force app restart by dispatching to main queue
                DispatchQueue.main.async {
                    // RevenueCat disabled - directly restart app flow
                    // Force navigation to root and restart app flow
                    if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                       let window = windowScene.windows.first {
                        window.rootViewController = UIHostingController(rootView: ContentView())
                        window.makeKeyAndVisible()
                    }
                }
            }
            self?.stopLoading()
        }, callbackFailure: { error in
            self.stopLoading()
            print("ProfileViewModel logOutMyAc error: \(error)")
        })
    }
    
    // MARK: - Age Restrictions
    
    func fetchAgeRatings() {
        startLoading()
        NetworkManager.callWebService(url: .getAgeRatings) { (response: AgeRatingResponse) in
            DispatchQueue.main.async {
                if response.status {
                    self.ageRatings = response.ageRatings ?? []
                } else {
                    print("Failed to fetch age ratings: \(response.message)")
                }
                self.stopLoading()
            }
        } callbackFailure: { error in
            DispatchQueue.main.async {
                self.stopLoading()
                print("Error fetching age ratings: \(error)")
            }
        }
    }
    
    func updateAgeSettings(profileId: Int, age: Int?, isKidsProfile: Bool?) {
        startLoading()
        
        var params: [Params: Any] = [
            .profileId: profileId,
            .userId: myUser?.id ?? 0
        ]
        
        if let age = age {
            params[.age] = age
        }
        
        if let isKidsProfile = isKidsProfile {
            params[.isKidsProfile] = isKidsProfile ? 1 : 0
        }
        
        NetworkManager.callWebService(url: .updateAgeSettings, params: params) { (response: ProfileAgeUpdateResponse) in
            DispatchQueue.main.async {
                if response.status {
                    // Update current profile if it matches
                    if let updatedProfile = response.profile,
                       let currentProfile = SessionManager.shared.getCurrentProfile(),
                       currentProfile.profileId == updatedProfile.profileId {
                        SessionManager.shared.setCurrentProfile(updatedProfile)
                    }
                    self.showAgeSettings = false
                    print("Age settings updated successfully")
                } else {
                    print("Failed to update age settings: \(response.message)")
                }
                self.stopLoading()
            }
        } callbackFailure: { error in
            DispatchQueue.main.async {
                self.stopLoading()
                print("Error updating age settings: \(error)")
            }
        }
    }
    
    func loadCurrentProfileSettings() {
        if let currentProfile = SessionManager.shared.getCurrentProfile() {
            selectedAge = currentProfile.age
            isKidsProfile = currentProfile.effectiveKidsProfile
        }
    }
}
