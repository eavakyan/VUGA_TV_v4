//
//  ProfileViewModel.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 31/05/24.
//

import Foundation

class ProfileViewModel : BaseViewModel {
    
    @Published var isLogoutDialogShow = false
    @Published var isDeleteDialogShow = false
    @Published var isTermsURLSheet = false
    @Published var isRatingAppSheet = false
    @Published var isPrivacyURLSheet = false

    
    func deleteMyAc() {
        startLoading()
        let param : [Params : Any] = [.userId: myUser?.id ?? 0]
        NetworkManager.callWebService(url: .deleteMyAccount,params: param) {(obj: DeleteAccountModel) in
            if obj.status == true {
                self.isDeleteDialogShow = false
                SessionManager.shared.clear()
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
            }
            self.stopLoading()
        }
    }
}
