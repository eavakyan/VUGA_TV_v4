//
//  UserNotificationViewModel.swift
//  Vuga
//
//  ViewModel for handling in-app user notifications
//

import Foundation
import SwiftUI

class UserNotificationViewModel: ObservableObject {
    @Published var pendingNotifications: [UserNotification] = []
    @Published var currentNotification: UserNotification?
    @Published var isShowingNotification = false
    @Published var isLoading = false
    
    private let baseURL = WebService.apiBase
    
    // Check for pending notifications
    func checkPendingNotifications() {
        guard let profileId = SessionManager.shared.currentProfile?.profileId else {
            print("No current profile to check notifications")
            return
        }
        
        isLoading = true
        
        var params: [Params: Any] = [:]
        params[.profileId] = profileId
        params[.platform] = "ios"
        
        NetworkManager.callWebService(url: .getPendingNotifications, params: params) { [weak self] (result: UserNotificationResponse) in
            DispatchQueue.main.async {
                self?.isLoading = false
                if result.status, let notifications = result.data {
                    self?.pendingNotifications = notifications
                    // Show the first notification if any
                    if let firstNotification = notifications.first {
                        self?.showNotification(firstNotification)
                    }
                }
            }
        }
    }
    
    // Show a notification
    func showNotification(_ notification: UserNotification) {
        currentNotification = notification
        isShowingNotification = true
        markNotificationAsShown(notification)
    }
    
    // Mark notification as shown
    private func markNotificationAsShown(_ notification: UserNotification) {
        guard let profileId = SessionManager.shared.currentProfile?.profileId else { return }
        
        var params: [Params: Any] = [:]
        params[.profileId] = profileId
        params[.notificationId] = notification.id
        
        NetworkManager.callWebService(url: .markNotificationShown, params: params) { (result: UserNotificationResponse) in
            print("Notification marked as shown")
        }
    }
    
    // Dismiss current notification
    func dismissNotification() {
        guard let notification = currentNotification,
              let profileId = SessionManager.shared.currentProfile?.profileId else { return }
        
        var params: [Params: Any] = [:]
        params[.profileId] = profileId
        params[.notificationId] = notification.id
        
        NetworkManager.callWebService(url: .dismissNotification, params: params) { [weak self] (result: UserNotificationResponse) in
            DispatchQueue.main.async {
                self?.isShowingNotification = false
                self?.currentNotification = nil
                // Remove from pending list
                self?.pendingNotifications.removeAll { $0.id == notification.id }
                // Show next notification if any
                if let nextNotification = self?.pendingNotifications.first {
                    self?.showNotification(nextNotification)
                }
            }
        }
    }
}