//
//  EditProfileViewModel.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 27/05/24.
//

import Foundation
import SwiftUI
import Alamofire


class EditProfileViewModel : BaseViewModel {
    @Published var fullName = ""
    @Published var email = ""
    @Published var isShowImagePicker = false
    @Published var image : UIImage?
    
    func editProfile() {
        startLoading()
        let params : [Params: Any] = [.userId : myUser?.id ?? 0, .fullname: fullName, .email: email, .profileImage: [image]]
        NetworkManager.callWebServiceWithFiles(url: .updateProfile, params: params){(obj: UserModel) in
            self.stopLoading()
            if let user = obj.data {
                DispatchQueue.main.async {
                    self.myUser = user
                    Navigation.pop()
                }
            }
        }
    }
}

