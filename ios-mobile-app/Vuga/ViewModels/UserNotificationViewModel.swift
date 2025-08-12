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
    
    // Track shown notifications persistently
    @AppStorage("shownNotificationIds") private var shownNotificationIdsData: Data = Data()
    private var shownNotificationIds: Set<Int> {
        get {
            guard let ids = try? JSONDecoder().decode(Set<Int>.self, from: shownNotificationIdsData) else {
                return Set<Int>()
            }
            return ids
        }
        set {
            if let data = try? JSONEncoder().encode(newValue) {
                shownNotificationIdsData = data
            }
        }
    }
    
    private let baseURL = WebService.apiBase
    
    // Check for pending notifications
    func checkPendingNotifications() {
        guard let profileId = SessionManager.shared.currentProfile?.profileId else {
            print("UserNotificationVM: No current profile to check notifications")
            return
        }
        
        print("UserNotificationVM: Checking notifications for profile: \(profileId)")
        isLoading = true
        
        var params: [Params: Any] = [:]
        params[.profileId] = profileId
        params[.platform] = "ios"
        
        print("UserNotificationVM: Making API call to getPendingNotifications")
        print("UserNotificationVM: Parameters: \(params)")
        
        NetworkManager.callWebService(url: .getPendingNotifications, params: params, 
            callbackSuccess: { [weak self] (result: UserNotificationResponse) in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    print("UserNotificationVM: Received response - Status: \(result.status)")
                    if result.status, let notifications = result.data {
                        print("UserNotificationVM: Found \(notifications.count) notifications")
                        
                        // Filter out already shown notifications
                        let unshownNotifications = notifications.filter { notification in
                            !(self?.shownNotificationIds.contains(notification.id) ?? false)
                        }
                        
                        print("UserNotificationVM: \(unshownNotifications.count) unshown notifications")
                        self?.pendingNotifications = unshownNotifications
                        
                        // Show the first unshown notification if any
                        if let firstNotification = unshownNotifications.first {
                            print("UserNotificationVM: Showing first notification: \(firstNotification.title)")
                            self?.showNotification(firstNotification)
                        }
                    } else {
                        print("UserNotificationVM: No notifications or status false")
                    }
                }
            },
            callbackFailure: { error in
                print("UserNotificationVM: Error fetching notifications: \(error)")
                print("UserNotificationVM: Error description: \(error.localizedDescription)")
            }
        )
    }
    
    // Show a notification
    func showNotification(_ notification: UserNotification) {
        currentNotification = notification
        isShowingNotification = true
        
        // Add to shown notifications set
        var currentShownIds = shownNotificationIds
        currentShownIds.insert(notification.id)
        shownNotificationIds = currentShownIds
        
        markNotificationAsShown(notification)
    }
    
    // Mark notification as shown
    private func markNotificationAsShown(_ notification: UserNotification) {
        guard let profileId = SessionManager.shared.currentProfile?.profileId else { return }
        
        var params: [Params: Any] = [:]
        params[.profileId] = profileId
        params[.notificationId] = notification.id
        
        NetworkManager.callWebService(url: .markNotificationShown, params: params, 
            callbackSuccess: { (result: UserNotificationResponse) in
                print("UserNotificationVM: Notification marked as shown")
            },
            callbackFailure: { error in
                print("UserNotificationVM: Error marking notification as shown: \(error)")
            }
        )
    }
    
    // Dismiss current notification
    func dismissNotification() {
        guard let notification = currentNotification,
              let profileId = SessionManager.shared.currentProfile?.profileId else { return }
        
        var params: [Params: Any] = [:]
        params[.profileId] = profileId
        params[.notificationId] = notification.id
        
        NetworkManager.callWebService(url: .dismissNotification, params: params,
            callbackSuccess: { [weak self] (result: UserNotificationResponse) in
                DispatchQueue.main.async {
                    print("UserNotificationVM: Notification dismissed")
                    self?.isShowingNotification = false
                    self?.currentNotification = nil
                    // Remove from pending list
                    self?.pendingNotifications.removeAll { $0.id == notification.id }
                    // Show next notification if any
                    if let nextNotification = self?.pendingNotifications.first {
                        self?.showNotification(nextNotification)
                    }
                }
            },
            callbackFailure: { error in
                print("UserNotificationVM: Error dismissing notification: \(error)")
            }
        )
    }
    
    // Clear shown notifications (call when user logs out or switches profile)
    func clearShownNotifications() {
        shownNotificationIds = Set<Int>()
        print("UserNotificationVM: Cleared shown notifications")
    }
}