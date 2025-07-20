//
//  LoginViewModel.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import SwiftUI
import GoogleSignIn
import Firebase
import AuthenticationServices
import FirebaseAuth
import FirebaseCore

class LoginViewModel : BaseViewModel, ASAuthorizationControllerDelegate {
    @AppStorage(SessionKeys.isLoggedIn) var isLoggedIn = false
    @Published var fullname = ""
    @Published var email = ""
    @Published var password = ""
    @Published var confirmPassword = ""
    @Published var selectedScreen = LoginScreenType.mainScreen
    @StateObject var proModel = ProViewModel()
    
    func registerUser(identity: String, email: String, fullName: String, loginType: LoginType, shouldLogin: Bool = true,completion: @escaping (User)->() = { _ in }){
        
        let params: [Params: Any] = [.identity : identity,
                                     .email: email,
                                     .fullname: fullName,
                                     .loginType: loginType.rawValue,
                                     .deviceType: DeviceType.iOS.rawValue,
                                     .deviceToken : WebService.deviceToken.isEmpty ? "---" : WebService.deviceToken,
        ]
        
        NetworkManager.callWebService(url: .userRegistration, params: params) { (obj: UserModel) in
            if let user = obj.data {
                DispatchQueue.main.async {
                    self.myUser = user
                    self.isLoggedIn = shouldLogin
                    completion(user)
                    self.proModel.passUserIdToRevenueCat()
                }
            }
        }
    }
    
    func creatAccount(){
        let language = LocalizationService.shared.language
        startLoading()
        if password != confirmPassword {
            toast(title: .passwordMismatched.localized(language))
            stopLoading()
            return
        }
        Auth.auth().createUser(withEmail: email, password: password) { [self] result, err in
            if let err {
                stopLoading()
                self.toast(title: err.localizedDescription)
                return
            }
            registerUser(identity: email, email: email, fullName: fullname, loginType: .email, shouldLogin: false) { user in
                self.stopLoading()
                result?.user.sendEmailVerification(completion: { [self] err in
                    if let err {
                        toast(title: err.localizedDescription)
                        return
                    }
                    toast(title: .verification_link_sent.localized(language))
                    withAnimation {
                        selectedScreen = .signIn
                    }
                })
            }
        }
    }
    
    func signIn(){
        let language = LocalizationService.shared.language
        startLoading()
        Auth.auth().signIn(withEmail: email, password: password) { [self] result, err in
            if let err {
                toast(title: err.localizedDescription)
                stopLoading()
                return
            }
            if result?.user.isEmailVerified == false {
                toast(title: .please_verify.localized(language))
                stopLoading()
                return
            }
            registerUser(identity: email, email: email, fullName: "", loginType: .email)
        }
    }
    
    func resetPassword(){
        let language = LocalizationService.shared.language
        startLoading()
        Auth.auth().sendPasswordReset(withEmail: email) { [self] err in
            if let err {
                toast(title: err.localizedDescription)
                stopLoading()
                return
            }
            stopLoading()
            toast(title: .password_reset_link_sent.localized(language))
            withAnimation {
                selectedScreen = .signIn
            }
        }
    }
    
    func googleSignIn() {
        guard let clientID = FirebaseApp.app()?.options.clientID,let controller = UIApplication.shared.keyWindow?.rootViewController else { return }
        startLoading()
        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config
        
        GIDSignIn.sharedInstance.signIn(withPresenting: controller) {  result, error in
            if let error {
                print(error.localizedDescription)
                self.stopLoading()
                return
            }
            
            guard let user = result?.user.profile else {
                self.stopLoading()
                return
            }
            
            self.registerUser(identity: user.email, email: user.email, fullName: "\(user.givenName ?? "") \(user.familyName ?? "")", loginType: .gmail)
        }
    }
    
    //MARK: - Signin with Apple
    func signInWithApple() {
        self.startLoading()
        let provider = ASAuthorizationAppleIDProvider()
        
        let request = provider.createRequest()
        
        request.requestedScopes = [.fullName,.email]
        
        let controller = ASAuthorizationController(authorizationRequests: [request])
        
        controller.performRequests()
        controller.delegate = self
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        switch authorization.credential {
        case let credential as ASAuthorizationAppleIDCredential :
            self.startLoading()
            let userID = credential.user
            
//            let email = credential.email
            let firstName = credential.fullName?.givenName
            let lastName = credential.fullName?.familyName
            let fullName = "\(firstName ?? "John") \(lastName ?? "Deo")"
            self.registerUser(identity: userID, email: credential.email ?? "\(userID)@apple.com", fullName: fullName, loginType: .apple)
            
        default:
            break
        }
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: any Error) {
        self.stopLoading()
    }
    
    func toast(title: String) {
        makeToast(title: title)
    }
    
    enum LoginScreenType {
        case mainScreen, signUp, signIn, forget
    }
}

