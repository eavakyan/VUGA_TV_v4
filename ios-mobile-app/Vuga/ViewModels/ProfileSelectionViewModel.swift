import Foundation
import SwiftUI

class ProfileSelectionViewModel: BaseViewModel {
    @Published var profiles: [Profile] = []
    @Published private var _showError = false
    @Published var errorMessage = ""
    @Published var selectedProfile: Profile?
    private var isAutoCreatingProfile = false
    private var isLoadingProfiles = false
    
    // Computed property to control when errors should actually be shown
    var showError: Bool {
        get { _showError && !isAutoCreatingProfile && !isLoading }
        set { _showError = newValue }
    }
    
    func loadProfiles(forceReload: Bool = false) {
        // Prevent multiple simultaneous loads
        guard !isLoadingProfiles else { return }
        
        // If we already have profiles loaded, don't reload unless forced
        if !profiles.isEmpty && !forceReload {
            print("ProfileSelectionViewModel: Profiles already loaded, count: \(profiles.count)")
            stopLoading()
            return
        }
        
        guard let userId = myUser?.id else { 
            print("ProfileSelectionViewModel: No user ID found - user might not be logged in")
            // If there's no user, we can't load or create profiles
            // Don't show error during initial load
            return 
        }
        
        // Check if we're completely offline (not just poor connection)
        let connectionMonitor = ConnectionMonitor.shared
        print("ProfileSelectionViewModel: Connection status - isConnected: \(connectionMonitor.isConnected), quality: \(connectionMonitor.connectionQuality)")
        
        if !connectionMonitor.isConnected {
            print("ProfileSelectionViewModel: Device is offline - checking for cached profiles")
            
            // Try to load cached profiles from UserDefaults
            if let cachedProfiles = getCachedProfiles(), !cachedProfiles.isEmpty {
                print("ProfileSelectionViewModel: Found \(cachedProfiles.count) cached profiles")
                self.profiles = cachedProfiles
                self.stopLoading()
                return
            } else {
                print("ProfileSelectionViewModel: No cached profiles found in offline mode")
                // If no cached profiles and offline, still try network request
                // in case connection detection is wrong or gets restored
            }
        }
        
        isLoadingProfiles = true
        startLoading()
        showError = false
        errorMessage = ""
        
        let params: [Params: Any] = [.userId: userId]
        print("ProfileSelectionViewModel: Loading profiles for user \(userId)")
        
        NetworkManager.callWebService(url: .getUserProfiles, params: params, timeout: 8, 
            callbackSuccess: { [weak self] (obj: ProfileResponse) in
                self?.isLoadingProfiles = false
                self?.stopLoading()
                
                // Always ensure no error is shown during profile loading
                self?.showError = false
                
                print("ProfileSelectionViewModel: Received response - status: \(obj.status), profiles count: \(obj.profiles?.count ?? 0), message: \(obj.message)")
                
                if obj.status && !(obj.profiles?.isEmpty ?? true) {
                    // We have profiles, cache them for offline use
                    self?.profiles = obj.profiles ?? []
                    self?.cacheProfiles(self?.profiles ?? [])
                    print("ProfileSelectionViewModel: Cached \(self?.profiles.count ?? 0) profiles for offline use")
                } else {
                    // No profiles or error - check cache first
                    if let cachedProfiles = self?.getCachedProfiles(), !cachedProfiles.isEmpty {
                        print("ProfileSelectionViewModel: Using cached profiles after API error")
                        self?.profiles = cachedProfiles
                    } else {
                        // No cache and no profiles - create default
                        print("ProfileSelectionViewModel: No profiles or cache. Creating default profile.")
                        self?.isAutoCreatingProfile = true
                        self?.profiles = []
                        self?.createDefaultProfile()
                    }
                }
            },
            callbackFailure: { [weak self] error in
                print("ProfileSelectionViewModel: Failed to load profiles (timeout or error): \(error)")
                self?.isLoadingProfiles = false
                self?.stopLoading()
                self?.showError = false
                
                // Try cached profiles first
                if let cachedProfiles = self?.getCachedProfiles(), !cachedProfiles.isEmpty {
                    print("ProfileSelectionViewModel: Using cached profiles after network failure")
                    self?.profiles = cachedProfiles
                } else {
                    // Create default profile on network failure with no cache
                    self?.isAutoCreatingProfile = true
                    self?.profiles = []
                    self?.createDefaultProfile()
                }
            }
        )
    }
    
    func selectProfile(_ profile: Profile) {
        guard let userId = myUser?.id else { 
            print("ProfileSelectionViewModel: selectProfile - No user ID found")
            return 
        }
        
        print("ProfileSelectionViewModel: Selecting profile \(profile.name) with ID \(profile.profileId)")
        
        // Check if we're offline
        let connectionMonitor = ConnectionMonitor.shared
        if !connectionMonitor.isConnected {
            print("ProfileSelectionViewModel: Offline - selecting profile locally")
            // Update SessionManager's current profile immediately for offline use
            SessionManager.shared.currentProfile = profile
            self.selectedProfile = profile
            stopLoading()
            return
        }
        
        startLoading()
        let params: [Params: Any] = [
            .userId: userId,
            .profileId: profile.profileId
        ]
        
        NetworkManager.callWebService(url: .selectProfile, params: params, timeout: 8, 
            callbackSuccess: { [weak self] (obj: ProfileResponse) in
                self?.stopLoading()
                
                print("ProfileSelectionViewModel: Select profile response - status: \(obj.status), message: \(obj.message)")
                
                if obj.status {
                    // Update SessionManager's current profile immediately
                    SessionManager.shared.currentProfile = profile
                    self?.selectedProfile = profile
                    print("ProfileSelectionViewModel: Profile selected successfully")
                } else {
                    // Even if server fails, select locally for better UX
                    SessionManager.shared.currentProfile = profile
                    self?.selectedProfile = profile
                    print("ProfileSelectionViewModel: Server error but selected locally - \(obj.message)")
                }
            },
            callbackFailure: { [weak self] error in
                self?.stopLoading()
                print("ProfileSelectionViewModel: Network error selecting profile - using locally")
                // Select locally on network failure
                SessionManager.shared.currentProfile = profile
                self?.selectedProfile = profile
            }
        )
    }
    
    func deleteProfile(_ profile: Profile) {
        guard let userId = myUser?.id else { return }
        
        let params: [Params: Any] = [
            .profileId: profile.profileId,
            .userId: userId
        ]
        
        startLoading()
        NetworkManager.callWebService(url: .deleteProfile, params: params, timeout: 8) { [weak self] (obj: StatusAndMessageModel) in
            self?.stopLoading()
            
            if obj.status ?? false {
                // Force reload profiles after successful deletion
                print("ProfileSelectionViewModel: Profile deleted successfully, refreshing profiles list")
                self?.loadProfiles(forceReload: true)
            } else {
                self?.showError = true
                self?.errorMessage = obj.message ?? "Failed to delete profile"
            }
        }
    }
    
    private func createDefaultProfile() {
        guard let userId = myUser?.id else { return }
        
        // Ensure no error is shown during auto-creation
        showError = false
        
        // Get user's name or use a default
        let profileName = myUser?.fullname ?? "Profile 1"
        
        startLoading()
        
        let params: [Params: Any] = [
            .userId: userId,
            .name: profileName,
            .avatarId: 1, // Default avatar
            .isKids: 0 // Not a kids profile
        ]
        
        print("ProfileSelectionViewModel: Creating default profile with name: \(profileName)")
        
        NetworkManager.callWebService(url: .createProfile, params: params, timeout: 10) { [weak self] (obj: ProfileResponse) in
            self?.stopLoading()
            self?.isAutoCreatingProfile = false
            
            // Always ensure no error is shown
            self?.showError = false
            
            print("ProfileSelectionViewModel: Create profile response - status: \(obj.status), message: \(obj.message)")
            
            if obj.status {
                // Profile created successfully
                if let newProfile = obj.profile {
                    // Add the new profile to the list
                    self?.profiles = [newProfile]
                    // Don't auto-select during creation - let user choose
                } else if let newProfiles = obj.profiles, !newProfiles.isEmpty {
                    // Some APIs return profiles array instead of single profile
                    self?.profiles = newProfiles
                    // Don't auto-select during creation - let user choose
                } else {
                    // If no profile returned, just create a temporary one locally
                    print("ProfileSelectionViewModel: No profile returned from create API, showing empty state")
                    self?.profiles = []
                }
            } else {
                // If creation failed, log it but don't show error popup during auto-creation
                print("ProfileSelectionViewModel: Failed to create default profile - \(obj.message)")
                // Just show empty profiles so user can manually create one
                self?.profiles = []
            }
        }
    }
    
    // MARK: - Caching Methods
    private func cacheProfiles(_ profiles: [Profile]) {
        do {
            let data = try JSONEncoder().encode(profiles)
            UserDefaults.standard.set(data, forKey: "cachedProfiles")
        } catch {
            print("ProfileSelectionViewModel: Failed to cache profiles: \(error)")
        }
    }
    
    private func getCachedProfiles() -> [Profile]? {
        guard let data = UserDefaults.standard.data(forKey: "cachedProfiles") else {
            return nil
        }
        
        do {
            let profiles = try JSONDecoder().decode([Profile].self, from: data)
            return profiles
        } catch {
            print("ProfileSelectionViewModel: Failed to decode cached profiles: \(error)")
            return nil
        }
    }
}