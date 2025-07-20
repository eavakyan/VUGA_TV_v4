//
//  LoginView.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 07/05/24.
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
        .addBackground()
        .loaderView(vm.isLoading)
    }
    
    var signUpView: some View {
        VStack {
            VStack(spacing: 15) {
                MyTextField(placeholder: .fullName, text: $vm.fullname)
                MyTextField(placeholder: .email, text: $vm.email)
                    .keyboardType(.emailAddress)
                MySecuredTextField(placeholder: .password, text: $vm.password)
                MySecuredTextField(placeholder: .confirmPassword, text: $vm.confirmPassword)
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
