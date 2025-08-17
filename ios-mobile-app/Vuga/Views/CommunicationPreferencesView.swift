//
//  CommunicationPreferencesView.swift
//  Vuga
//
//  Unified communication preferences management
//

import SwiftUI

struct CommunicationPreferencesView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.myUser) var myUser: User? = nil
    @AppStorage(SessionKeys.isNotificationOn) var pushNotificationsOn = true
    @StateObject private var viewModel = MarketingPreferencesViewModel()
    @State private var emailConsent = false
    @State private var smsConsent = false
    @State private var hasChanges = false
    @State private var showToast = false
    @State private var toastMessage = ""
    @State private var toastIsSuccess = true
    
    var body: some View {
        VStack(spacing: 0) {
            // Custom header bar
            HStack {
                BackButton(onTap: {
                    Navigation.pop()
                })
                Spacer()
                Text("Communication Settings")
                    .outfitSemiBold(20)
                    .foregroundColor(Color("textColor"))
                Spacer()
                BackButton()
                    .opacity(0)
            }
            .padding(.horizontal)
            .padding(.vertical, 10)
            
            ScrollView {
                VStack(spacing: 20) {
                    
                    // Section 1: Instant Notifications
                    VStack(alignment: .leading, spacing: 12) {
                        Text("INSTANT NOTIFICATIONS")
                            .outfitMedium(12)
                            .foregroundColor(Color("textLight"))
                            .tracking(1.2)
                        
                        // Push Notifications toggle
                        HStack {
                            HStack(spacing: 12) {
                                ZStack {
                                    Circle()
                                        .fill(Color("baseColor").opacity(0.15))
                                        .frame(width: 40, height: 40)
                                    Image(systemName: "bell.fill")
                                        .font(.system(size: 18))
                                        .foregroundColor(Color("baseColor"))
                                }
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Push Notifications")
                                        .outfitMedium(16)
                                        .foregroundColor(Color("textColor"))
                                    Text("Get instant alerts on this device")
                                        .outfitRegular(12)
                                        .foregroundColor(Color("textLight"))
                                }
                            }
                            
                            Spacer()
                            
                            Toggle("", isOn: $pushNotificationsOn)
                                .toggleStyle(SwitchToggleStyle(tint: Color("baseColor")))
                                .onChange(of: pushNotificationsOn) { _ in
                                    // Apply immediately (local only)
                                    if pushNotificationsOn {
                                        AppDelegate.shared.subscribeTopic()
                                        toastMessage = "Push notifications enabled"
                                    } else {
                                        AppDelegate.shared.unSubscribeTopic()
                                        toastMessage = "Push notifications disabled"
                                    }
                                    toastIsSuccess = true
                                    showToast = true
                                }
                        }
                        .padding()
                        .background(Color("cardBg"))
                        .cornerRadius(12)
                    }
                    
                    // Section 2: Marketing Communications
                    VStack(alignment: .leading, spacing: 12) {
                        Text("MARKETING COMMUNICATIONS")
                            .outfitMedium(12)
                            .foregroundColor(Color("textLight"))
                            .tracking(1.2)
                        
                        VStack(spacing: 12) {
                            // Email Marketing toggle
                            HStack {
                                HStack(spacing: 12) {
                                    ZStack {
                                        Circle()
                                            .fill(Color("baseColor").opacity(0.15))
                                            .frame(width: 40, height: 40)
                                        Image(systemName: "envelope.fill")
                                            .font(.system(size: 18))
                                            .foregroundColor(Color("baseColor"))
                                    }
                                    
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text("Email Marketing")
                                            .outfitMedium(16)
                                            .foregroundColor(Color("textColor"))
                                        Text("Newsletters & special offers")
                                            .outfitRegular(12)
                                            .foregroundColor(Color("textLight"))
                                    }
                                }
                                
                                Spacer()
                                
                                Toggle("", isOn: $emailConsent)
                                    .toggleStyle(SwitchToggleStyle(tint: Color("baseColor")))
                                    .onChange(of: emailConsent) { _ in
                                        hasChanges = true
                                    }
                            }
                            .padding()
                            .background(Color("cardBg"))
                            .cornerRadius(12)
                            
                            // SMS Marketing toggle
                            HStack {
                                HStack(spacing: 12) {
                                    ZStack {
                                        Circle()
                                            .fill(Color("baseColor").opacity(0.15))
                                            .frame(width: 40, height: 40)
                                        Image(systemName: "message.fill")
                                            .font(.system(size: 18))
                                            .foregroundColor(Color("baseColor"))
                                    }
                                    
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text("SMS Marketing")
                                            .outfitMedium(16)
                                            .foregroundColor(Color("textColor"))
                                        Text("Text offers & account updates")
                                            .outfitRegular(12)
                                            .foregroundColor(Color("textLight"))
                                    }
                                }
                                
                                Spacer()
                                
                                Toggle("", isOn: $smsConsent)
                                    .toggleStyle(SwitchToggleStyle(tint: Color("baseColor")))
                                    .onChange(of: smsConsent) { _ in
                                        hasChanges = true
                                    }
                            }
                            .padding()
                            .background(Color("cardBg"))
                            .cornerRadius(12)
                        }
                    }
                    
                    // Save button for marketing preferences
                    if hasChanges {
                        CommonButton(title: "Save Marketing Preferences", isDisable: viewModel.isLoading) {
                            viewModel.updateMarketingConsent(emailConsent: emailConsent, smsConsent: smsConsent) { success, message in
                                hasChanges = false
                                toastMessage = message
                                toastIsSuccess = success
                                showToast = true
                            }
                        }
                        .padding(.top, 10)
                    }
                    
                    // Info card
                    VStack(alignment: .leading, spacing: 12) {
                        HStack(spacing: 8) {
                            Image(systemName: "info.circle.fill")
                                .font(.system(size: 14))
                                .foregroundColor(Color("textLight"))
                            Text("About These Settings")
                                .outfitMedium(14)
                                .foregroundColor(Color("textColor"))
                        }
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("• Push Notifications are device-specific and take effect immediately")
                                .outfitRegular(12)
                                .foregroundColor(Color("textLight"))
                            Text("• Marketing preferences are saved to your account and apply across all devices")
                                .outfitRegular(12)
                                .foregroundColor(Color("textLight"))
                            Text("• You can update these settings at any time")
                                .outfitRegular(12)
                                .foregroundColor(Color("textLight"))
                        }
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
        .toast(isShowing: $showToast, message: toastMessage, isSuccess: toastIsSuccess)
        .onAppear {
            // Initialize with current user preferences
            emailConsent = myUser?.emailConsent ?? true
            smsConsent = myUser?.smsConsent ?? true
            hasChanges = false
        }
    }
}