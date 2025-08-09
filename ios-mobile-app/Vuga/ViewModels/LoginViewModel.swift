//
//  LoginViewModel.swift
//  Vuga
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
    @Published var emailConsent = true  // Default to true (opt-out approach)
    @Published var smsConsent = true    // Default to true (opt-out approach)
    @StateObject var proModel = ProViewModel()
    
    // Validation error messages
    @Published var fullnameError = ""
    @Published var emailError = ""
    @Published var passwordError = ""
    @Published var confirmPasswordError = ""
    @Published var showValidationErrors = false
    
    func registerUser(identity: String, email: String, fullName: String, loginType: LoginType, shouldLogin: Bool = true, emailConsent: Bool = false, smsConsent: Bool = false, completion: @escaping (User)->() = { _ in }){
        
        let params: [Params: Any] = [.identity : identity,
                                     .email: email,
                                     .fullname: fullName,
                                     .loginType: loginType.rawValue,
                                     .deviceType: DeviceType.iOS.rawValue,
                                     .deviceToken : WebService.deviceToken.isEmpty ? "---" : WebService.deviceToken,
                                     .emailConsent: emailConsent ? 1 : 0,
                                     .smsConsent: smsConsent ? 1 : 0
        ]
        
        NetworkManager.callWebService(url: .userRegistration, params: params) { (obj: UserModel) in
            print("LoginViewModel: User registration response - status: \(obj.status ?? false)")
            print("LoginViewModel: Response message: \(obj.message ?? "")")
            
            // Handle both new registration and existing user cases
            // API returns status=false with "User already exists" but still provides user data
            if let user = obj.data {
                DispatchQueue.main.async {
                    print("LoginViewModel: Setting user and login state - userId: \(user.id ?? 0), shouldLogin: \(shouldLogin)")
                    print("LoginViewModel: User has \(user.profiles?.count ?? 0) profiles")
                    self.myUser = user
                    self.isLoggedIn = shouldLogin
                    completion(user)
                    self.proModel.passUserIdToRevenueCat()
                }
            } else {
                print("LoginViewModel: No user data in response - message: \(obj.message ?? "no message")")
                self.stopLoading()
            }
        }
    }
    
    // MARK: - Validation Methods
    
    func validateFullName() -> Bool {
        fullnameError = ""
        
        if fullname.isEmpty {
            fullnameError = "Full name is required"
            return false
        }
        
        if fullname.count < 2 {
            fullnameError = "Full name must be at least 2 characters"
            return false
        }
        
        if fullname.count > 50 {
            fullnameError = "Full name must be less than 50 characters"
            return false
        }
        
        // Check for valid characters (letters and spaces only)
        let nameRegex = "^[a-zA-Z ]+$"
        let namePredicate = NSPredicate(format: "SELF MATCHES %@", nameRegex)
        if !namePredicate.evaluate(with: fullname) {
            fullnameError = "Full name can only contain letters and spaces"
            return false
        }
        
        return true
    }
    
    func validateEmail() -> Bool {
        emailError = ""
        
        if email.isEmpty {
            emailError = "Email is required"
            return false
        }
        
        // Email regex validation
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        if !emailPredicate.evaluate(with: email) {
            emailError = "Please enter a valid email address"
            return false
        }
        
        return true
    }
    
    func validatePassword() -> Bool {
        passwordError = ""
        
        if password.isEmpty {
            passwordError = "Password is required"
            return false
        }
        
        if password.count < 8 {
            passwordError = "Password must be at least 8 characters"
            return false
        }
        
        if password.count > 100 {
            passwordError = "Password is too long"
            return false
        }
        
        // Check for at least one uppercase letter
        let uppercaseRegex = ".*[A-Z]+.*"
        if !NSPredicate(format: "SELF MATCHES %@", uppercaseRegex).evaluate(with: password) {
            passwordError = "Password must contain at least one uppercase letter"
            return false
        }
        
        // Check for at least one lowercase letter
        let lowercaseRegex = ".*[a-z]+.*"
        if !NSPredicate(format: "SELF MATCHES %@", lowercaseRegex).evaluate(with: password) {
            passwordError = "Password must contain at least one lowercase letter"
            return false
        }
        
        // Check for at least one number
        let numberRegex = ".*[0-9]+.*"
        if !NSPredicate(format: "SELF MATCHES %@", numberRegex).evaluate(with: password) {
            passwordError = "Password must contain at least one number"
            return false
        }
        
        // Check for at least one special character
        let specialRegex = ".*[!@#$%^&*(),.?\":{}|<>]+.*"
        if !NSPredicate(format: "SELF MATCHES %@", specialRegex).evaluate(with: password) {
            passwordError = "Password must contain at least one special character (!@#$%^&*)"
            return false
        }
        
        return true
    }
    
    func validateConfirmPassword() -> Bool {
        confirmPasswordError = ""
        
        if confirmPassword.isEmpty {
            confirmPasswordError = "Please confirm your password"
            return false
        }
        
        if password != confirmPassword {
            confirmPasswordError = "Passwords do not match"
            return false
        }
        
        return true
    }
    
    func validateAllFields() -> Bool {
        let isFullNameValid = validateFullName()
        let isEmailValid = validateEmail()
        let isPasswordValid = validatePassword()
        let isConfirmPasswordValid = validateConfirmPassword()
        
        showValidationErrors = true
        
        return isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
    }
    
    func clearValidationErrors() {
        fullnameError = ""
        emailError = ""
        passwordError = ""
        confirmPasswordError = ""
        showValidationErrors = false
    }
    
    func creatAccount(){
        let language = LocalizationService.shared.language
        
        // Validate all fields first
        guard validateAllFields() else {
            // Show validation errors inline - no toast needed
            return
        }
        
        startLoading()
        Auth.auth().createUser(withEmail: email, password: password) { [self] result, err in
            if let err {
                stopLoading()
                self.toast(title: err.localizedDescription)
                return
            }
            registerUser(identity: email, email: email, fullName: fullname, loginType: .email, shouldLogin: false, emailConsent: self.emailConsent, smsConsent: self.smsConsent) { user in
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

