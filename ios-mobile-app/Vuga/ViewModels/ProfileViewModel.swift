//
//  ProfileViewModel.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 31/05/24.
//

import Foundation
import SwiftUI
import RevenueCat
import UIKit

class ProfileViewModel : BaseViewModel {
    
    @Published var isLogoutDialogShow = false
    @Published var isDeleteDialogShow = false
    @Published var isTermsURLSheet = false
    @Published var isRatingAppSheet = false
    @Published var isPrivacyURLSheet = false
    @AppStorage(SessionKeys.isLoggedIn) var isLoggedIn = false

    
    func deleteMyAc() {
        startLoading()
        let param : [Params : Any] = [.userId: myUser?.id ?? 0]
        NetworkManager.callWebService(url: .deleteMyAccount,params: param) {(obj: DeleteAccountModel) in
            if obj.status == true {
                self.isDeleteDialogShow = false
                SessionManager.shared.clear()
                // Force app restart by dispatching to main queue
                DispatchQueue.main.async {
                    // Clear RevenueCat
                    Purchases.shared.logOut { (customerInfo, error) in
                        // Force navigation to root and restart app flow
                        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                           let window = windowScene.windows.first {
                            window.rootViewController = UIHostingController(rootView: ContentView())
                            window.makeKeyAndVisible()
                        }
                    }
                }
            }
            self.stopLoading()
        }
    }

    func logOutMyAc() {
        startLoading()
        let params : [Params: Any] = [.userId : myUser?.id ?? 0]
        NetworkManager.callWebService(url: .logOut,params: params) {(obj: DeleteAccountModel) in
            if obj.status == true {
                self.isLogoutDialogShow = false
                SessionManager.shared.clear()
                // Force app restart by dispatching to main queue
                DispatchQueue.main.async {
                    // Clear RevenueCat
                    Purchases.shared.logOut { (customerInfo, error) in
                        // Force navigation to root and restart app flow
                        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                           let window = windowScene.windows.first {
                            window.rootViewController = UIHostingController(rootView: ContentView())
                            window.makeKeyAndVisible()
                        }
                    }
                }
            }
            self.stopLoading()
        }
    }
}
