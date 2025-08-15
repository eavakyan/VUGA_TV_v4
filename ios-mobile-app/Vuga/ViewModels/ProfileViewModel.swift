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
import Kingfisher

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
    
    // MARK: - Avatar Upload
    
    func uploadAvatar(image: UIImage) {
        guard let currentProfile = SessionManager.shared.getCurrentProfile() else {
            print("No current profile to upload avatar")
            return
        }
        
        // Resize and compress image
        let maxSize: CGFloat = 500
        let resizedImage = resizeImage(image: image, targetSize: CGSize(width: maxSize, height: maxSize))
        
        guard let imageData = resizedImage.jpegData(compressionQuality: 0.7) else {
            print("Failed to compress image")
            return
        }
        
        // Convert to base64
        let base64String = imageData.base64EncodedString()
        
        startLoading()
        
        let params: [Params: Any] = [
            .userId: myUser?.id ?? 0,
            .profileId: currentProfile.profileId,
            .imageData: base64String
        ]
        
        NetworkManager.callWebService(url: .uploadProfileAvatar, params: params, callbackSuccess: { [weak self] (response: ProfileAvatarUploadResponse) in
            DispatchQueue.main.async {
                self?.stopLoading()
                
                if response.status {
                    print("Upload response - avatarUrl: \(response.avatarUrl ?? "nil")")
                    print("Upload response - profile: \(response.profile?.avatarUrl ?? "nil profile or avatarUrl")")
                    print("Upload response - profile avatarType: \(response.profile?.avatarType ?? "nil")")
                    
                    // Update the current profile with the one from server response
                    if let updatedProfile = response.profile {
                        print("Using profile from response - avatarUrl: \(updatedProfile.avatarUrl ?? "nil")")
                        SessionManager.shared.setCurrentProfile(updatedProfile)
                        
                        // Clear cached image to force reload
                        if let avatarUrl = updatedProfile.avatarUrl {
                            KingfisherManager.shared.cache.removeImage(forKey: avatarUrl)
                        }
                        
                        // Force UI refresh
                        self?.objectWillChange.send()
                        
                        // Verify the update
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            let verifyProfile = SessionManager.shared.getCurrentProfile()
                            print("Verification - Profile avatarUrl: \(verifyProfile?.avatarUrl ?? "nil")")
                            print("Verification - Profile avatarType: \(verifyProfile?.avatarType ?? "nil")")
                        }
                    } else if let avatarUrl = response.avatarUrl {
                        // Fallback to manual update if profile not in response
                        print("Using avatarUrl directly from response: \(avatarUrl)")
                        var updatedProfile = currentProfile
                        updatedProfile.avatarType = "custom"
                        updatedProfile.avatarUrl = avatarUrl
                        SessionManager.shared.setCurrentProfile(updatedProfile)
                        
                        // Clear cached image to force reload
                        KingfisherManager.shared.cache.removeImage(forKey: avatarUrl)
                        
                        // Force UI refresh
                        self?.objectWillChange.send()
                        
                        // Verify the update
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            let verifyProfile = SessionManager.shared.getCurrentProfile()
                            print("Verification - Profile avatarUrl: \(verifyProfile?.avatarUrl ?? "nil")")
                            print("Verification - Profile avatarType: \(verifyProfile?.avatarType ?? "nil")")
                        }
                    }
                    
                    print("Avatar uploaded successfully")
                } else {
                    print("Failed to upload avatar: \(response.message ?? "Unknown error")")
                }
            }
        }, callbackFailure: { [weak self] error in
            DispatchQueue.main.async {
                self?.stopLoading()
                print("Error uploading avatar: \(error)")
            }
        })
    }
    
    func removeCustomAvatar() {
        guard let currentProfile = SessionManager.shared.getCurrentProfile() else {
            print("No current profile to remove avatar")
            return
        }
        
        startLoading()
        
        let params: [Params: Any] = [
            .userId: myUser?.id ?? 0,
            .profileId: currentProfile.profileId
        ]
        
        NetworkManager.callWebService(url: .removeProfileAvatar, params: params, callbackSuccess: { [weak self] (response: BaseResponse) in
            DispatchQueue.main.async {
                self?.stopLoading()
                
                if response.status == true {
                    // Revert to color avatar
                    var updatedProfile = currentProfile
                    updatedProfile.avatarType = "color"
                    updatedProfile.avatarUrl = nil
                    SessionManager.shared.setCurrentProfile(updatedProfile)
                    
                    // Force UI refresh
                    self?.objectWillChange.send()
                    
                    print("Custom avatar removed successfully")
                } else {
                    print("Failed to remove avatar: \(response.message ?? "Unknown error")")
                }
            }
        }, callbackFailure: { [weak self] error in
            DispatchQueue.main.async {
                self?.stopLoading()
                print("Error removing avatar: \(error)")
            }
        })
    }
    
    private func resizeImage(image: UIImage, targetSize: CGSize) -> UIImage {
        let size = image.size
        
        let widthRatio  = targetSize.width  / size.width
        let heightRatio = targetSize.height / size.height
        
        // Figure out what our orientation is, and use that to form the rectangle
        let ratio = min(widthRatio, heightRatio)
        
        let newSize = CGSize(width: size.width * ratio, height: size.height * ratio)
        
        // This is the rect that we've calculated out and this is what is actually used below
        let rect = CGRect(origin: .zero, size: newSize)
        
        // Actually do the resizing to the rect using the ImageContext stuff
        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        image.draw(in: rect)
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return newImage ?? image
    }
}
