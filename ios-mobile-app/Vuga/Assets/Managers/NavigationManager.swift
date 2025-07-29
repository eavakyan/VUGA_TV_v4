import SwiftUI

class NavigationManager: ObservableObject {
    static let shared = NavigationManager()
    
    @Published var showProfileSelection = false
    @Published var currentTab: Tab = .home
    
    enum Tab {
        case home
        case search
        case watchlist
        case profile
    }
    
    private init() {}
    
    func navigateToHome() {
        showProfileSelection = false
        currentTab = .home
    }
    
    func showProfiles() {
        showProfileSelection = true
    }
}