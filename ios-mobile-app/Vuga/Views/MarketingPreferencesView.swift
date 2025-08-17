//
//  MarketingPreferencesView.swift
//  Vuga
//
//  Marketing consent preferences management
//

import SwiftUI

struct MarketingPreferencesView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.myUser) var myUser: User? = nil
    @StateObject private var viewModel = MarketingPreferencesViewModel()
    @State private var emailConsent = false
    @State private var smsConsent = false
    @State private var hasChanges = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Custom header bar with working back button
            HStack {
                BackButton(onTap: {
                    Navigation.pop()
                })
                Spacer()
                Text("Contact Preferences")
                    .outfitSemiBold(20)
                    .foregroundColor(Color("textColor"))
                Spacer()
                BackButton()
                    .hidden()
            }
            .frame(height: 50)
            .padding(.horizontal)
            
            ScrollView(showsIndicators: false) {
                VStack(spacing: 20) {
                    // Info text
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Communication Preferences")
                            .outfitSemiBold(18)
                            .foregroundColor(Color("textColor"))
                        
                        Text("Choose how you'd like to receive updates and special offers from us.")
                            .outfitRegular(14)
                            .foregroundColor(Color("textLight"))
                            .multilineTextAlignment(.leading)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                    .background(Color("cardBg"))
                    .cornerRadius(12)
                    
                    // Email consent toggle
                    VStack(spacing: 16) {
                        HStack {
                            VStack(alignment: .leading, spacing: 8) {
                                HStack {
                                    Image.mail
                                        .resizeFitTo(size: 24)
                                        .foregroundColor(Color("baseColor"))
                                    Text("Email Updates")
                                        .outfitMedium(16)
                                        .foregroundColor(Color("textColor"))
                                }
                                
                                Text("Receive news, updates and special offers via email")
                                    .outfitRegular(13)
                                    .foregroundColor(Color("textLight"))
                                    .multilineTextAlignment(.leading)
                            }
                            
                            Spacer()
                            
                            Toggle("", isOn: $emailConsent)
                                .labelsHidden()
                                .onChange(of: emailConsent) { _ in
                                    hasChanges = true
                                }
                        }
                        .padding()
                        .background(Color("cardBg"))
                        .cornerRadius(12)
                        
                        // SMS consent toggle
                        HStack {
                            VStack(alignment: .leading, spacing: 8) {
                                HStack {
                                    Image(systemName: "message.fill")
                                        .resizeFitTo(size: 24)
                                        .foregroundColor(Color("baseColor"))
                                    Text("SMS Updates")
                                        .outfitMedium(16)
                                        .foregroundColor(Color("textColor"))
                                }
                                
                                Text("Receive updates and alerts via SMS")
                                    .outfitRegular(13)
                                    .foregroundColor(Color("textLight"))
                                    .multilineTextAlignment(.leading)
                            }
                            
                            Spacer()
                            
                            Toggle("", isOn: $smsConsent)
                                .labelsHidden()
                                .onChange(of: smsConsent) { _ in
                                    hasChanges = true
                                }
                        }
                        .padding()
                        .background(Color("cardBg"))
                        .cornerRadius(12)
                    }
                    
                    // Last updated info
                    if let emailConsentDate = myUser?.emailConsentDate ?? myUser?.smsConsentDate {
                        Text("Last updated: \(formatDate(emailConsentDate))")
                            .outfitRegular(12)
                            .foregroundColor(Color("textLight"))
                            .padding(.top, 8)
                    }
                    
                    // Save button
                    if hasChanges {
                        CommonButton(title: "Save Preferences", isDisable: viewModel.isLoading) {
                            viewModel.updateMarketingConsent(emailConsent: emailConsent, smsConsent: smsConsent) {
                                hasChanges = false
                            }
                        }
                        .padding(.top, 20)
                    }
                    
                    // Privacy info
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Privacy Information")
                            .outfitMedium(14)
                            .foregroundColor(Color("textColor"))
                        
                        Text("We respect your privacy and will only send you communications you've opted in to receive. You can update your preferences at any time.")
                            .outfitRegular(12)
                            .foregroundColor(Color("textLight"))
                            .multilineTextAlignment(.leading)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                    .background(Color("cardBg").opacity(0.5))
                    .cornerRadius(12)
                    .padding(.top, 20)
                }
                .padding()
            }
        }
        .addBackground()
        .hideNavigationbar()
        .loaderView(viewModel.isLoading)
        .onAppear {
            // Initialize with current user preferences
            emailConsent = myUser?.emailConsent ?? true
            smsConsent = myUser?.smsConsent ?? true
            hasChanges = false
        }
    }
    
    func formatDate(_ dateString: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        if let date = formatter.date(from: dateString) {
            formatter.dateStyle = .medium
            formatter.timeStyle = .none
            return formatter.string(from: date)
        }
        
        return dateString
    }
}

// ViewModel for handling marketing preferences
class MarketingPreferencesViewModel: BaseViewModel {
    func updateMarketingConsent(emailConsent: Bool, smsConsent: Bool, completion: @escaping () -> Void) {
        guard let userId = myUser?.id else { return }
        
        startLoading()
        
        // Use both userId and appUserId for compatibility
        let params: [Params: Any] = [
            .userId: userId,
            .emailConsent: emailConsent ? 1 : 0,
            .smsConsent: smsConsent ? 1 : 0
        ]
        
        NetworkManager.callWebService(url: .userUpdateProfile, params: params) { [weak self] (obj: UserModel) in
            self?.stopLoading()
            
            if let user = obj.data {
                // Update the stored user with new consent values
                self?.myUser = user
                SessionManager.shared.currentUser = user
                completion()
                makeToast(title: "Preferences updated successfully")
            } else {
                // Attempt to re-fetch profile to reflect latest server state
                let fetchParams: [Params: Any] = [.userId: userId]
                NetworkManager.callWebService(url: .fetchProfile, params: fetchParams) { [weak self] (profileObj: UserModel) in
                    if let refreshed = profileObj.data {
                        self?.myUser = refreshed
                        SessionManager.shared.currentUser = refreshed
                    }
                    makeToast(title: obj.message ?? "Failed to update preferences")
                }
            }
        }
    }
}