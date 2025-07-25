//
//  BaseViewModel.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import SwiftUI
import RevenueCat

class BaseViewModel : NSObject, ObservableObject {
    @AppStorage(SessionKeys.myUser) var myUser : User? = nil
    @AppStorage(SessionKeys.isPro) var isPro = false
    @Published var isLoading = false
    @Published var dummy = false
    static var shared = BaseViewModel()
    
    func startLoading(){
        DispatchQueue.main.async {
            self.isLoading = true
        }
    }
    
    func stopLoading(){
        DispatchQueue.main.async {
            self.isLoading = false
        }
    }
    
    func reset(){
        DispatchQueue.main.async {
            self.dummy.toggle()
        }
    }
    
    func commonProfileEdit(params: [Params: Any], completion: @escaping ((_ user: User) -> ()) = { _ in}) {
        var params = params
        params[.userId] = myUser?.id ?? 0
        NetworkManager.callWebService(url: .updateProfile, params: params) { (obj: UserModel) in
            if let data = obj.data {
                self.myUser = data
                completion(data)
            }
        }
    }
    
    func hideTabbar(){
        NotificationCenter.default.post(name: .hideTabbar, object: nil)
    }
    
    func showTabbar(){
        NotificationCenter.default.post(name: .showTabbar, object: nil)
    }
    
    func checkUserIsPro(customerInfo: CustomerInfo?) {
        //        self.isPro = customerInfo?.entitlements.all["Pro"]?.isActive == true
        if let date = customerInfo?.latestExpirationDate, date >= Date() {
            isPro = true
        } else {
            isPro =  false
        }
    }
}

