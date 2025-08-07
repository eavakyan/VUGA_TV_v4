import Foundation

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
    
    func updateProfile(profileId: Int, name: String, color: String, isKids: Bool, avatarId: Int? = nil, age: Int? = nil, completion: @escaping () -> Void) {
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
            .isKids: isKids ? 1 : 0
        ]
        
        // Add age if provided (required for kids profiles)
        if let age = age {
            params[.age] = age
        }
        
        NetworkManager.callWebService(url: .updateProfile, params: params) { [weak self] (obj: ProfileResponse) in
            self?.stopLoading()
            
            if obj.status {
                completion()
            } else {
                self?.showError = true
                self?.errorMessage = obj.message
            }
        }
    }
}