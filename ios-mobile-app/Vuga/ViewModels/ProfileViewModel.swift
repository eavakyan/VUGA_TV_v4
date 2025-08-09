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
    
    // Logout progress tracking
    @Published var isLoggingOut = false
    @Published var logoutProgress: Float = 0.0
    @Published var logoutStatusMessage = ""

    
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
        guard !isLoggingOut else { return }
        
        print("ProfileViewModel logOutMyAc - userId: \(myUser?.id ?? 0)")
        
        isLoggingOut = true
        logoutProgress = 0.0
        logoutStatusMessage = "Signing out..."
        
        // If user is not logged in (userId = 0), just clear session locally
        if myUser?.id == nil || myUser?.id == 0 {
            print("User not logged in, clearing session locally")
            logoutProgress = 0.5
            logoutStatusMessage = "Clearing local session..."
            
            DispatchQueue.global(qos: .userInitiated).async { [weak self] in
                SessionManager.shared.clear()
                
                DispatchQueue.main.async {
                    self?.completeLogout()
                }
            }
            return
        }
        
        // Start with API call
        logoutProgress = 0.1
        logoutStatusMessage = "Contacting server..."
        
        let params : [Params: Any] = [.userId : myUser?.id ?? 0]
        NetworkManager.callWebService(url: .logOut, params: params, callbackSuccess: { [weak self] (obj: DeleteAccountModel) in
            print("Logout Response:")
            print("Status: \(obj.status ?? false)")
            print("Message: \(obj.message ?? "")")
            
            if obj.status == true {
                self?.logoutProgress = 0.5
                self?.logoutStatusMessage = "Clearing local data..."
                
                // Clear session asynchronously
                DispatchQueue.global(qos: .userInitiated).async {
                    SessionManager.shared.clear()
                    
                    DispatchQueue.main.async {
                        self?.completeLogout()
                    }
                }
            } else {
                self?.handleLogoutError("Logout failed: \(obj.message ?? "Unknown error")")
            }
        }, callbackFailure: { [weak self] error in
            print("ProfileViewModel logOutMyAc error: \(error)")
            // Even if API fails, clear local session
            self?.logoutProgress = 0.5
            self?.logoutStatusMessage = "Clearing local data..."
            
            DispatchQueue.global(qos: .userInitiated).async {
                SessionManager.shared.clear()
                
                DispatchQueue.main.async {
                    self?.completeLogout()
                }
            }
        })
    }
    
    private func completeLogout() {
        logoutProgress = 1.0
        logoutStatusMessage = "Redirecting..."
        
        // Small delay to show completion
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            self.isLogoutDialogShow = false
            self.isLoggedIn = false
            self.isLoggingOut = false
            self.logoutProgress = 0.0
            self.logoutStatusMessage = ""
            
            // Force app restart
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let window = windowScene.windows.first {
                window.rootViewController = UIHostingController(rootView: ContentView())
                window.makeKeyAndVisible()
            }
        }
    }
    
    private func handleLogoutError(_ message: String) {
        logoutStatusMessage = message
        isLoggingOut = false
        
        // Auto-dismiss error after 2 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            self.isLogoutDialogShow = false
        }
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
