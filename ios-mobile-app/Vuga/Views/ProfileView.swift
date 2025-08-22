//
//  ProfileView.swift
//  Vuga
//
//

import SwiftUI
import WebKit
import Kingfisher
import PhotosUI

struct ProfileView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    @FetchRequest(sortDescriptors: []) var downloads : FetchedResults<DownloadContent>
    @StateObject var vm = ProfileViewModel()
    @Binding var selectedTab: Tab
    @State private var showImagePicker = false
    @State private var showImageSourceMenu = false
    @State private var imagePickerSourceType: UIImagePickerController.SourceType = .photoLibrary
    
    init(selectedTab: Binding<Tab> = .constant(.profile)) {
        self._selectedTab = selectedTab
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Custom header bar with working back button
            HStack {
                BackButton(onTap: {
                    selectedTab = .home
                })
                Spacer()
                HStack(spacing: 10) {
                    Text(String.profile.localized(language))
                        .outfitSemiBold(20)
                        .foregroundColor(Color("textColor"))
                    
                    // Show current profile name without avatar
                    Text(SessionManager.shared.currentProfile?.name ?? "")
                        .outfitMedium(16)
                        .foregroundColor(Color("textColor"))
                }
                Spacer()
                BackButton()
                    .hidden()
            }
            .frame(height: 50)
            .padding(.horizontal)
            ScrollView(showsIndicators: false) {
                VStack(spacing: 12) {
                    // Large Avatar Section for Editing
                    VStack(spacing: 16) {
                        Button(action: {
                            showImageSourceMenu = true
                        }) {
                            ZStack(alignment: .bottomTrailing) {
                                if let currentProfile = SessionManager.shared.currentProfile {
                                    let _ = print("ProfileView large - avatarUrl: \(currentProfile.avatarUrl ?? "nil"), avatarType: \(currentProfile.avatarType)")
                                    // Only show photo if avatarType is "custom" AND we have a valid URL
                                    if currentProfile.avatarType == "custom",
                                       let avatarUrl = currentProfile.avatarUrl,
                                       !avatarUrl.isEmpty,
                                       avatarUrl.hasPrefix("http") {
                                        // Use profile avatar URL
                                        KFImage(URL(string: avatarUrl))
                                            .resizable()
                                            .aspectRatio(contentMode: .fill)
                                            .frame(width: 100, height: 100)
                                            .clipShape(.circle)
                                    } else {
                                        // Show initials in colored circle for color avatars or invalid URLs
                                        let profileColor = currentProfile.avatarColor?.isEmpty ?? true ? "#FF6B6B" : (currentProfile.avatarColor ?? "#FF6B6B")
                                        let profileInitials = String(currentProfile.name.prefix(2)).uppercased()
                                        
                                        ZStack {
                                            Circle()
                                                .fill(Color(hexString: profileColor))
                                                .frame(width: 100, height: 100)
                                            Text(profileInitials)
                                                .font(.system(size: 40, weight: .bold))
                                                .foregroundColor(.white)
                                        }
                                    }
                                } else {
                                    // No profile selected - show default with initials
                                    ZStack {
                                        Circle()
                                            .fill(Color(hexString: "#999999"))
                                            .frame(width: 100, height: 100)
                                        Image.person
                                            .resizable()
                                            .renderingMode(.template)
                                            .scaledToFit()
                                            .frame(width: 50, height: 50)
                                            .foregroundColor(.white)
                                    }
                                }
                                
                                // Camera icon overlay
                                ZStack {
                                    Circle()
                                        .fill(Color.base)
                                        .frame(width: 32, height: 32)
                                    Image(systemName: "camera.fill")
                                        .font(.system(size: 16))
                                        .foregroundColor(.white)
                                }
                                .offset(x: 5, y: 5)
                            }
                        }
                        
                        // Add Manage Profile link instead of "Tap to change profile photo"
                        Button(action: {
                            vm.showProfileSelection = true
                        }) {
                            Text("Manage Profile")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(Color.base)
                                .underline()
                        }
                    }
                    .padding(.vertical, 20)
                    /*
                    Image.edit
                        .resizable()
                        .renderingMode(.template)
                        .scaledToFit()
                        .frame(width: 25, height: 25)
                        .padding(.horizontal)
                        .onTap {
                            Navigation.pushToSwiftUiView(EditProfileVIew())
                        }
                    */
                    ProfileFieldCard(icon: Image.notification, title: "Communication Settings"){
                        Navigation.pushToSwiftUiView(CommunicationPreferencesView())
                    }
                    
                    ProfileFieldCard(icon: Image.downloads, title: .downloads){
                            Navigation.pushToSwiftUiView(DownloadView())
                    }
                    ProfileFieldCard(icon: Image.language, title: .language){
                        Navigation.pushToSwiftUiView(LanguageView())
                    }
                    if vm.myUser != nil {
                        ProfileFieldCard(icon: Image.tv, title: "Connect TV"){
                            Navigation.pushToSwiftUiView(QRScannerView())
                        }
                        ProfileFieldCard(icon: Image.person, title: "Switch Profile"){
                            vm.showProfileSelection = true
                        }
                        
                        // Age Settings - only show if there's a current profile
                        if let currentProfile = SessionManager.shared.getCurrentProfile() {
                            ProfileFieldCard(icon: Image.settings, title: "Age Settings"){
                                Navigation.pushToSwiftUiView(AgeSettingsView(profile: currentProfile, viewModel: vm))
                            }
                        }
                    }
                    ProfileFieldCard(icon: Image.privacy, title: .privacyPolicy){
                        vm.isPrivacyURLSheet = true
                    }
                    ProfileFieldCard(icon: Image.terms, title: .termsNuses){
                        vm.isTermsURLSheet = true
                    }
                    ProfileFieldCard(icon: Image.rate, title: .rateThisApp){
                        if let url = URL(string: rateThisAppURL) {
                            UIApplication.shared.open(url)
                        }
                    }
                    if vm.myUser != nil {
                        ProfileFieldCard(icon: Image.logout, title: .logout){
                            vm.isLogoutDialogShow = true
                        }
                        ProfileFieldCard(icon: Image.delete, title: .deleteMyAccount,titleColor: Color("baseColor")){
                            vm.isDeleteDialogShow = true
                        }
                    } else {
                        ProfileFieldCard(icon: Image.person, title: "Login"){
                            Navigation.pushToSwiftUiView(LoginView())
                        }
                    }
                }
                .padding(.vertical,10)
            }
        }
        .hideNavigationbar()
        .customAlert(isPresented: $vm.isLogoutDialogShow){
            if vm.isLoggingOut {
                // Show progress dialog during logout
                LogoutProgressView(progress: vm.logoutProgress, statusMessage: vm.logoutStatusMessage)
                    .animation(.default, value: vm.isLogoutDialogShow)
            } else {
                // Show confirmation dialog
                DialogCard(icon: Image.logout, title: .areYouSure, subTitle: .logoutDes, buttonTitle: .logout, onClose: {
                    withAnimation {
                        vm.isLogoutDialogShow = false
                    }
                },onButtonTap: {
                    // Start logout process without clearing downloads first
                    // Downloads will be preserved for offline viewing
                    vm.logOutMyAc()
                })
                .animation(.default, value: vm.isLogoutDialogShow)
            }
        }
        .customAlert(isPresented: $vm.isDeleteDialogShow){
            DialogCard(icon: Image.delete, title: .areYouSure, subTitle: .deleteAccountDes, buttonTitle: .deleteMyAccount, onClose: {
                withAnimation {
                    vm.isDeleteDialogShow = false
                }
            },onButtonTap: {
                deleteDownloadData()
                vm.isDeleteDialogShow = false
                vm.deleteMyAc()
            })
        }
        .onAppear(perform: {
            let docDir = try! FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            let filePath = try! FileManager.default.contentsOfDirectory(at: docDir, includingPropertiesForKeys: nil)
            var downloadData = SessionManager.shared.getDownloadData()
            for downloads in downloadData {
                let filterdVideoName = filePath.first(where: {$0.lastPathComponent == downloads.destinationName})
                downloadData.removeAll(where: {$0.destinationName == filterdVideoName?.lastPathComponent})
            }
        })
        .sheet(isPresented: $vm.isPrivacyURLSheet) {
            WebUrl(url: PRIVACY_URL, title: .privacyPolicy.localized(language)){
                vm.isPrivacyURLSheet = false
            }
            .ignoresSafeArea()
        }
        .sheet(isPresented: $vm.isTermsURLSheet) {
            WebUrl(url: TERMS_URL, title: .termsNuses.localized(language)){
                vm.isTermsURLSheet = false
            }
            .ignoresSafeArea()
        }
        .sheet(isPresented: $vm.showProfileSelection) {
            ProfileSelectionView()
                .environmentObject(SessionManager.shared)
        }
        .confirmationDialog("Choose Photo Source", isPresented: $showImageSourceMenu, titleVisibility: .visible) {
            Button("Take Photo") {
                imagePickerSourceType = .camera
                showImagePicker = true
            }
            Button("Choose from Library") {
                imagePickerSourceType = .photoLibrary
                showImagePicker = true
            }
            if SessionManager.shared.currentProfile?.avatarType == "custom" {
                Button("Remove Custom Photo", role: .destructive) {
                    vm.removeCustomAvatar()
                }
            }
            Button("Cancel", role: .cancel) {}
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(sourceType: imagePickerSourceType, selectedImage: { image in
                vm.uploadAvatar(image: image)
            })
        }
        .loaderView(vm.isLoading)
        .addBackground()
    }
    
    func deleteDownloadData() {
        vm.startLoading()
        for download in downloads {
            if let videoUrl = URL(string: DocumentsDirectory.localDocumentsURL.absoluteString + (download.videoName ?? "")) {
                CloudDataManager.shared.deleteFileFromCloud(url: videoUrl)
            }
            DataController.shared.context.delete(download)
            DataController.shared.saveData()
        }
        vm.stopLoading()
    }
}

#Preview {
    ProfileView()
}

struct ProfileFieldCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @State private var isPressed = false
    let icon: Image
    let title: String
    var titleColor : Color = Color("textColor")
    var onTap: ()->() = {}
    
    var body: some View {
        ZStack {
            HStack(spacing: 12) {
                icon
                    .resizable()
                    .renderingMode(.template)
                    .scaledToFit()
                    .frame(width: 25, height: 25)
                    .foregroundColor(titleColor)
                Text(title.localized(language))
                    .foregroundColor(titleColor)
                    .outfitRegular(14)
                    .frame(width: Device.width / 1.5,alignment: .leading)
                    .fixedSize(horizontal: true, vertical: false)
            }
            .frame(maxWidth: .infinity,alignment: .leading)
        }
        .padding(17)
        .addbgToProfileCard()
        .scaleEffect(isPressed ? 0.95 : 1.0)
        .opacity(isPressed ? 0.6 : 1.0)
        .animation(.easeInOut(duration: 0.1), value: isPressed)
        .onTapGesture {
            // Provide immediate visual feedback
            isPressed = true
            
            // Haptic feedback
            let impactFeedback = UIImpactFeedbackGenerator(style: .light)
            impactFeedback.impactOccurred()
            
            // Reset visual state and execute action
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                isPressed = false
                onTap()
            }
        }
    }
}

struct MySpaceFieldCardWithSwitch: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isNotificationOn) var isNotificationOn = true

    let icon: Image
    let title: String
    var isNotificationCard: Bool = false
    var appDelegateModel = AppDelegate.shared
    var onTap: ()->() = {}
    
    var body: some View {
        ZStack(alignment: .trailing) {
            HStack {
                HStack(spacing: 12) {
                    icon
                        .resizable()
                        .renderingMode(.template)
                        .scaledToFit()
                        .frame(width: 20, height: 20)
                        .foregroundColor(Color("textColor"))
                    VStack(alignment: .leading) {
                        Text(title.localized(language))
                            .foregroundColor(Color("textColor"))
                            .outfitRegular(14)
                            .fixedSize(horizontal: true, vertical: false)
                    }
                    .frame(maxHeight: 30)
                    Spacer()
                }
            }
            .onTap(completion: onTap)
            if isNotificationCard {
                Toggle("", isOn: $isNotificationOn)
                    .toggleStyle(SwitchToggleStyle(tint: .base))
                    .onChange(of: isNotificationOn) { _ in
                        if isNotificationOn {
                            appDelegateModel.subscribeTopic()
                        } else {
                            appDelegateModel.unSubscribeTopic()
                        }
                    }
            }
        }
        .padding(.vertical,15)
        .padding(.horizontal,20)
        .addbgToProfileCard()
    }
}

struct HtmlWebView: UIViewRepresentable {
    
    @State var text: String
    var bgColor: Color
    @Binding var dynamicHeight: CGFloat
    var webview: WKWebView = WKWebView()
    
    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webview.scrollView.bounces = false
        webview.navigationDelegate = context.coordinator
        webView.navigationDelegate = context.coordinator
        webView.scrollView.isScrollEnabled = false
        return webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
            let uiColor = UIColor(bgColor)
            let rgbaColor = uiColor.toRGBA() 
            let setText = """
            <html>
                <head>
                    <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no'>
                </head>
                <body style="background-color: \(rgbaColor); color: #ffffff">
                    \(text)
                </body>
            </html>
            """
            uiView.loadHTMLString(setText, baseURL: nil)
        }
    
    class Coordinator : NSObject,WKNavigationDelegate {
        var parent : HtmlWebView
        init(parent: HtmlWebView) {
            self.parent = parent
        }
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            
            if self.parent.dynamicHeight == .zero {
                
                self.parent.dynamicHeight = 300
                
                webView.evaluateJavaScript("document.documentElement.scrollHeight", completionHandler: { (height, error) in
                    DispatchQueue.main.asyncAfter(deadline: .now()) {
                        self.parent.dynamicHeight = height as! CGFloat
                    }
                })
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                let js = "document.getElementsByTagName('body')[0].style.webkitTextSizeAdjust='100%'"
                webView.evaluateJavaScript(js, completionHandler: nil)
            }
        }
    }
    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }
}

// MARK: - Logout Progress View
struct LogoutProgressView: View {
    let progress: Float
    let statusMessage: String
    
    var body: some View {
        VStack(spacing: 20) {
            // Logo or icon
            Image.logout
                .resizable()
                .renderingMode(.template)
                .scaledToFit()
                .frame(width: 50, height: 50)
                .foregroundColor(Color("baseColor"))
            
            // Status message
            Text(statusMessage)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(Color("textColor"))
                .multilineTextAlignment(.center)
                .animation(.easeInOut(duration: 0.3), value: statusMessage)
            
            // Progress bar
            ProgressView(value: progress)
                .progressViewStyle(LinearProgressViewStyle(tint: .base))
                .frame(height: 6)
                .scaleEffect(x: 1, y: 1.5, anchor: .center)
                .animation(.linear(duration: 0.3), value: progress)
            
            // Percentage text
            Text("\(Int(progress * 100))%")
                .font(.system(size: 14, weight: .regular))
                .foregroundColor(.textLight)
        }
        .padding(30)
        .background(Color("bgColor"))
        .cornerRadius(20)
        .frame(maxWidth: 300)
        .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
    }
}

