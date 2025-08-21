import Foundation
import UIKit

class CreateProfileViewModel: BaseViewModel {
    @Published var showError = false
    @Published var errorMessage = ""
    
    let avatarColors = [
        "#FF5252", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    ]
    
    func createProfile(name: String, color: String, isKids: Bool, age: Int? = nil, completion: @escaping () -> Void) {
        guard let userId = myUser?.id else { return }
        
        startLoading()
        showError = false
        
        // Map color to avatar ID (1-based index, limited to 1-8 range)
        var avatarId = 1
        if let colorIndex = avatarColors.firstIndex(of: color) {
            avatarId = (colorIndex % 8) + 1
        }
        
        var params: [Params: Any] = [
            .userId: userId,
            .name: name,
            .avatarId: avatarId,
            .avatarColor: color,  // Include the selected color
            .avatarType: "color",  // Set avatar type to color for new profiles
            .isKids: isKids ? 1 : 0
        ]
        
        // Add age if provided (required for kids profiles)
        if let age = age {
            params[.age] = age
        }
        
        NetworkManager.callWebService(url: .createProfile, params: params) { [weak self] (obj: ProfileResponse) in
            self?.stopLoading()
            
            if obj.status {
                completion()
            } else {
                self?.showError = true
                self?.errorMessage = obj.message
            }
        }
    }
    
    func updateProfile(profileId: Int, name: String, color: String, isKids: Bool, avatarId: Int? = nil, age: Int? = nil, avatarType: String = "color", shouldRemovePhoto: Bool = false, completion: @escaping () -> Void) {
        guard let userId = myUser?.id else { return }
        
        startLoading()
        showError = false
        
        // Map color to avatar ID if not provided
        var finalAvatarId = avatarId ?? 1
        if avatarId == nil, let colorIndex = avatarColors.firstIndex(of: color) {
            finalAvatarId = (colorIndex % 8) + 1
        }
        
        var params: [Params: Any] = [
            .profileId: profileId,
            .userId: userId,
            .name: name,
            .avatarId: finalAvatarId,
            .avatarType: avatarType,  // Include avatar type to ensure color avatar is used
            .avatarColor: color,  // Include the selected color
            .isKids: isKids ? 1 : 0
        ]
        
        // Add age if provided (required for kids profiles)
        if let age = age {
            params[.age] = age
        }
        
        // Add flag to remove photo if user selected a color to replace it
        if shouldRemovePhoto {
            params[.removePhoto] = true
        }
        
        print("UpdateProfile API Call - Params: \(params)")
        NetworkManager.callWebService(url: .updateProfile, params: params) { [weak self] (obj: ProfileResponse) in
            self?.stopLoading()
            
            if obj.status {
                print("UpdateProfile API Success - Message: \(obj.message)")
                completion()
            } else {
                print("UpdateProfile API Failed - Error: \(obj.message)")
                self?.showError = true
                self?.errorMessage = obj.message
            }
        }
    }
    
    func updateProfileWithImage(profileId: Int, name: String, color: String, isKids: Bool, avatarId: Int? = nil, age: Int? = nil, image: UIImage, completion: @escaping () -> Void) {
        guard let userId = myUser?.id else { return }
        
        startLoading()
        showError = false
        
        // First update the profile details
        var finalAvatarId = avatarId ?? 1
        if avatarId == nil, let colorIndex = avatarColors.firstIndex(of: color) {
            finalAvatarId = (colorIndex % 8) + 1
        }
        
        var params: [Params: Any] = [
            .profileId: profileId,
            .userId: userId,
            .name: name,
            .avatarId: finalAvatarId,
            .avatarType: "custom",  // Set to custom since we're uploading an image
            .avatarColor: color,
            .isKids: isKids ? 1 : 0
        ]
        
        if let age = age {
            params[.age] = age
        }
        
        // Update profile first
        NetworkManager.callWebService(url: .updateProfile, params: params) { [weak self] (obj: ProfileResponse) in
            if obj.status {
                // Profile updated, now upload the image
                self?.uploadProfileAvatar(userId: userId, profileId: profileId, image: image) { success in
                    self?.stopLoading()
                    if success {
                        completion()
                    } else {
                        self?.showError = true
                        self?.errorMessage = "Profile updated but image upload failed"
                        // Still call completion as profile was updated
                        completion()
                    }
                }
            } else {
                self?.stopLoading()
                self?.showError = true
                self?.errorMessage = obj.message
            }
        }
    }
    
    private func uploadProfileAvatar(userId: Int, profileId: Int, image: UIImage, completion: @escaping (Bool) -> Void) {
        // Convert image to base64
        guard let imageData = image.jpegData(compressionQuality: 0.8) else {
            completion(false)
            return
        }
        
        let base64String = imageData.base64EncodedString()
        
        let params: [Params: Any] = [
            .userId: userId,
            .profileId: profileId,
            .imageData: base64String
        ]
        
        NetworkManager.callWebService(url: .uploadProfileAvatar, params: params) { (obj: ProfileResponse) in
            completion(obj.status)
        }
    }
}