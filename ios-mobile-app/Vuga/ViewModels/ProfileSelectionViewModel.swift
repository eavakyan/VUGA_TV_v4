import Foundation
import SwiftUI

class ProfileSelectionViewModel: BaseViewModel {
    @Published var profiles: [Profile] = []
    @Published var showError = false
    @Published var errorMessage = ""
    @Published var selectedProfile: Profile?
    
    func loadProfiles() {
        guard let userId = myUser?.id else { 
            print("ProfileSelectionViewModel: No user ID found")
            return 
        }
        
        startLoading()
        showError = false
        
        let params: [Params: Any] = [.userId: userId]
        print("ProfileSelectionViewModel: Loading profiles for user \(userId)")
        
        NetworkManager.callWebService(url: .getUserProfiles, params: params) { [weak self] (obj: ProfileResponse) in
            self?.stopLoading()
            
            print("ProfileSelectionViewModel: Received response - status: \(obj.status), profiles count: \(obj.profiles?.count ?? 0)")
            
            if obj.status {
                self?.profiles = obj.profiles ?? []
            } else {
                self?.showError = true
                self?.errorMessage = obj.message
                print("ProfileSelectionViewModel: Error - \(obj.message)")
            }
        }
    }
    
    func selectProfile(_ profile: Profile) {
        guard let userId = myUser?.id else { return }
        
        startLoading()
        let params: [Params: Any] = [
            .userId: userId,
            .profileId: profile.profileId
        ]
        
        NetworkManager.callWebService(url: .selectProfile, params: params) { [weak self] (obj: ProfileResponse) in
            self?.stopLoading()
            
            if obj.status {
                // Update SessionManager's current profile immediately
                SessionManager.shared.currentProfile = profile
                self?.selectedProfile = profile
            } else {
                self?.showError = true
                self?.errorMessage = obj.message
            }
        }
    }
    
    func deleteProfile(_ profile: Profile) {
        guard let userId = myUser?.id else { return }
        
        let params: [Params: Any] = [
            .profileId: profile.profileId,
            .userId: userId
        ]
        
        startLoading()
        NetworkManager.callWebService(url: .deleteProfile, params: params) { [weak self] (obj: StatusAndMessageModel) in
            self?.stopLoading()
            
            if obj.status ?? false {
                self?.loadProfiles()
            } else {
                self?.showError = true
                self?.errorMessage = obj.message ?? "Failed to delete profile"
            }
        }
    }
}