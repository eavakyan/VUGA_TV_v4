import Foundation

class CreateProfileViewModel: BaseViewModel {
    @Published var showError = false
    @Published var errorMessage = ""
    
    func createProfile(name: String, color: String, isKids: Bool, completion: @escaping () -> Void) {
        guard let userId = myUser?.id else { return }
        
        startLoading()
        showError = false
        
        let params: [Params: Any] = [
            .userId: userId,
            .name: name,
            .avatarType: "color",
            .avatarUrl: "",
            .avatarColor: color,
            .isKids: isKids ? 1 : 0
        ]
        
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
    
    func updateProfile(profileId: Int, name: String, color: String, isKids: Bool, completion: @escaping () -> Void) {
        startLoading()
        showError = false
        
        let params: [Params: Any] = [
            .profileId: profileId,
            .name: name,
            .avatarType: "color",
            .avatarUrl: "",
            .avatarColor: color,
            .isKids: isKids ? 1 : 0
        ]
        
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