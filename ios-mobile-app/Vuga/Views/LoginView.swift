//
//  LoginView.swift
//  Vuga
//
//

import SwiftUI

struct LoginView: View, KeyboardReadable {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm = LoginViewModel()
    @State var shouldShowLogo = true

    var body: some View {
        VStack {
            Spacer()
            if shouldShowLogo {
                Image.logoHorizontal
                    .resizeFitTo(width: Device.width / 3, height: 40)
                Spacer()
            }
            VStack(spacing: 30) {
                if vm.selectedScreen == .mainScreen {
                    Text(String.pleaseSignInUsing.localized(language))
                        .outfitMedium(16)
                        .foregroundColor(.text)
                        .multilineTextAlignment(.center)
                }
                
                Text(title.localized(language))
                    .outfitMedium(24)
                    .foregroundColor(.text)
                
                if !desc.isEmpty {
                    Text(desc.localized(language))
                        .outfitRegular(14)
                        .multilineTextAlignment(.center)
                        .foregroundColor(.textLight)
                }
                
                switch vm.selectedScreen {
                case .mainScreen: main
                case .signUp: signUpView
                case .signIn: signInView
                case .forget: forgotView
                }
                
            }
            .padding(20)
        }
        .onReceive(keyboardPublisher) { newIsKeyboardVisible in
            withAnimation {
                shouldShowLogo = !newIsKeyboardVisible
            }
        }
        .overlay(
            ZStack{
            if vm.selectedScreen != .mainScreen {
                if shouldShowLogo {
                    HStack {
                        BackButton {
                            vm.selectedScreen = .mainScreen
                        }
                        Spacer()
                    }
                }
            }
        }.padding() , alignment: .topLeading)
        .onAppear {
            vm.clearValidationErrors()
        }
        .addBackground()
        .loaderView(vm.isLoading)
    }
    
    var signUpView: some View {
        VStack {
            VStack(spacing: 15) {
                VStack(alignment: .leading, spacing: 5) {
                    MyTextField(placeholder: .fullName, text: $vm.fullname)
                        .onChange(of: vm.fullname) { _ in
                            if vm.showValidationErrors {
                                _ = vm.validateFullName()
                            }
                        }
                    if !vm.fullnameError.isEmpty && vm.showValidationErrors {
                        Text(vm.fullnameError)
                            .font(.system(size: 12))
                            .foregroundColor(.red)
                            .padding(.horizontal, 5)
                            .animation(.easeInOut(duration: 0.2), value: vm.fullnameError)
                    }
                }
                
                VStack(alignment: .leading, spacing: 5) {
                    MyTextField(placeholder: .email, text: $vm.email)
                        .keyboardType(.emailAddress)
                        .onChange(of: vm.email) { _ in
                            if vm.showValidationErrors {
                                _ = vm.validateEmail()
                            }
                        }
                    if !vm.emailError.isEmpty && vm.showValidationErrors {
                        Text(vm.emailError)
                            .font(.system(size: 12))
                            .foregroundColor(.red)
                            .padding(.horizontal, 5)
                            .animation(.easeInOut(duration: 0.2), value: vm.emailError)
                    }
                }
                
                VStack(alignment: .leading, spacing: 5) {
                    MySecuredTextField(placeholder: .password, text: $vm.password)
                        .onChange(of: vm.password) { _ in
                            if vm.showValidationErrors {
                                _ = vm.validatePassword()
                                if !vm.confirmPassword.isEmpty {
                                    _ = vm.validateConfirmPassword()
                                }
                            }
                        }
                    
                    // Password strength indicator
                    if !vm.password.isEmpty {
                        PasswordStrengthView(password: vm.password)
                            .padding(.horizontal, 5)
                            .animation(.easeInOut(duration: 0.2), value: vm.password)
                    }
                    
                    if !vm.passwordError.isEmpty && vm.showValidationErrors {
                        Text(vm.passwordError)
                            .font(.system(size: 12))
                            .foregroundColor(.red)
                            .padding(.horizontal, 5)
                            .animation(.easeInOut(duration: 0.2), value: vm.passwordError)
                    }
                }
                
                VStack(alignment: .leading, spacing: 5) {
                    MySecuredTextField(placeholder: .confirmPassword, text: $vm.confirmPassword)
                        .onChange(of: vm.confirmPassword) { _ in
                            if vm.showValidationErrors {
                                _ = vm.validateConfirmPassword()
                            }
                        }
                    if !vm.confirmPasswordError.isEmpty && vm.showValidationErrors {
                        Text(vm.confirmPasswordError)
                            .font(.system(size: 12))
                            .foregroundColor(.red)
                            .padding(.horizontal, 5)
                            .animation(.easeInOut(duration: 0.2), value: vm.confirmPasswordError)
                    }
                }
                
                // Marketing Consent Section
                VStack(alignment: .leading, spacing: 12) {
                    // Email consent
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: vm.emailConsent ? "checkmark.square.fill" : "square")
                            .font(.system(size: 22))
                            .foregroundColor(vm.emailConsent ? .base : .textLight)
                            .onTapGesture {
                                vm.emailConsent.toggle()
                            }
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Email Updates")
                                .outfitMedium(14)
                                .foregroundColor(.text)
                            Text("Receive news, updates and special offers via email")
                                .outfitRegular(12)
                                .foregroundColor(.textLight)
                                .multilineTextAlignment(.leading)
                        }
                        
                        Spacer()
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        vm.emailConsent.toggle()
                    }
                    
                    // SMS consent
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: vm.smsConsent ? "checkmark.square.fill" : "square")
                            .font(.system(size: 22))
                            .foregroundColor(vm.smsConsent ? .base : .textLight)
                            .onTapGesture {
                                vm.smsConsent.toggle()
                            }
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text("SMS Updates")
                                .outfitMedium(14)
                                .foregroundColor(.text)
                            Text("Receive updates and alerts via SMS")
                                .outfitRegular(12)
                                .foregroundColor(.textLight)
                                .multilineTextAlignment(.leading)
                        }
                        
                        Spacer()
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        vm.smsConsent.toggle()
                    }
                }
                .padding(.top, 8)
            }
            .padding(.bottom)
            
            CommonButton(title: .signUp, isDisable: vm.fullname.isEmpty || vm.email.isEmpty || vm.password.isEmpty || vm.confirmPassword.isEmpty) {
                vm.creatAccount()
            }
            
            Text("\(String.signIn.localized(language))?")
                .outfitMedium(18)
                .foregroundColor(.text)
                .padding(.bottom, 30)
                .padding(.top, 10)
                .onTap {
                    vm.selectedScreen = .signIn
                }
        }
    }
    
    var signInView: some View {
        VStack {
            VStack(spacing: 15) {
                MyTextField(placeholder: .email, text: $vm.email)
                    .keyboardType(.emailAddress)
                MySecuredTextField(placeholder: .password, text: $vm.password)
                HStack {
                    Spacer()
                    Text("\(String.forgotPassword.localized(language))?")
                        .outfitMedium(14)
                        .foregroundColor(.text)
                        .padding(.bottom)
                        .padding(.top, 5)
                        .onTap {
                            vm.selectedScreen = .forget
                        }
                }
            }
            
            CommonButton(title: .signIn, isDisable: vm.password.isEmpty || vm.email.isEmpty) {
                vm.signIn()
            }
            Text("\(String.signUp.localized(language))?")
                .outfitMedium(18)
                .foregroundColor(.text)
                .padding(.bottom, 30)
                .padding(.top, 10)
                .onTap {
                    vm.selectedScreen = .signUp
                }
        }
    }
    
    var forgotView: some View {
        VStack {
            VStack {
                MyTextField(placeholder: .email, text: $vm.email)
                    .keyboardType(.emailAddress)

            }
            .padding(.bottom)
            CommonButton(title: .reset, isDisable: vm.email.isEmpty) {
                vm.resetPassword()
            }
            .padding(.bottom, 30)
        }
    }
    
    var main: some View {
        VStack(spacing: 20) {
            LoginButton(image: .apple, title: .signInWithApple) {
                vm.signInWithApple()
            }
            
            LoginButton(image: .google, title: .signInWithGoogle) {
                vm.googleSignIn()
            }
            
            LoginButton(image: .mail, title: .signInWithEmail) {
                vm.selectedScreen = .signIn
            }
        }
        .padding(20)
    }
    
    var title : String {
        switch vm.selectedScreen {
        case .mainScreen:
            return  .signInToContinue
        case .signUp:
            return .signUp
        case .signIn:
            return .signIn
        case .forget:
            return .forgotPassword
        }
    }
    
    var desc : String {
        switch vm.selectedScreen {
        case .mainScreen:
            return ""
        case .signUp:
            return ""
        case .signIn:
            return ""
        case .forget:
            return .forgotPasswordDisc
        }
    }
}

#Preview {
    LoginView()
}

struct LoginButton: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var image: Image
    var title: String
    var onTap: ()->()
    var body: some View {
        HStack(spacing: 15) {
            image
                .resizeFitTo(size: 25)
            
            Text(title.localized(language))
                .outfitMedium(16)
                .foregroundColor(.text)
            
        }
        .padding(.vertical)
        .maxWidthFrame()
        .background(Color.cardBg)
        .cornerRadius(radius: 15)
        .padding(1)
        .background(Color.cardBorder)
        .cornerRadius(radius: 16)
        .onTap(completion: onTap)
    }
}

struct PasswordStrengthView: View {
    let password: String
    
    var strength: PasswordStrength {
        calculatePasswordStrength(password)
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 4) {
                ForEach(0..<4) { index in
                    RoundedRectangle(cornerRadius: 2)
                        .fill(index < strength.level ? strength.color : Color.gray.opacity(0.3))
                        .frame(height: 4)
                }
            }
            
            Text(strength.text)
                .font(.system(size: 11))
                .foregroundColor(strength.color)
        }
    }
    
    func calculatePasswordStrength(_ password: String) -> PasswordStrength {
        var score = 0
        
        // Length check
        if password.count >= 8 { score += 1 }
        if password.count >= 12 { score += 1 }
        
        // Character variety checks
        let hasUppercase = password.range(of: "[A-Z]", options: .regularExpression) != nil
        let hasLowercase = password.range(of: "[a-z]", options: .regularExpression) != nil
        let hasNumbers = password.range(of: "[0-9]", options: .regularExpression) != nil
        let hasSpecial = password.range(of: "[!@#$%^&*(),.?\":{}|<>]", options: .regularExpression) != nil
        
        if hasUppercase { score += 1 }
        if hasLowercase { score += 1 }
        if hasNumbers { score += 1 }
        if hasSpecial { score += 1 }
        
        // Determine strength based on score
        if score <= 2 {
            return PasswordStrength(level: 1, color: .red, text: "Weak password")
        } else if score <= 4 {
            return PasswordStrength(level: 2, color: .orange, text: "Fair password")
        } else if score <= 5 {
            return PasswordStrength(level: 3, color: .yellow, text: "Good password")
        } else {
            return PasswordStrength(level: 4, color: .green, text: "Strong password")
        }
    }
}

struct PasswordStrength {
    let level: Int
    let color: Color
    let text: String
}
