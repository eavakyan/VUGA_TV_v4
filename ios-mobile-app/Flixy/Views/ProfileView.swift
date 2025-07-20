//
//  ProfileView.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 30/05/24.
//

import SwiftUI
import WebKit
import Kingfisher

struct ProfileView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    @FetchRequest(sortDescriptors: []) var downloads : FetchedResults<DownloadContent>
    @StateObject var vm = ProfileViewModel()
    var body: some View {
        VStack(spacing: 0) {
            BackBarView(title: String.profile.localized(language))
            ScrollView(showsIndicators: false) {
                VStack(spacing: 12) {
                    HStack(alignment: .top) {
                        Image.edit
                            .resizeFitTo(size: 25)
                            .hidden()
                            .padding(.horizontal)
                        Spacer()
                        if vm.myUser?.profileImage != nil && vm.myUser?.profileImage != "" {
                            KFImage(vm.myUser?.profileImage?.addBaseURL())
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(width: 100,height: 100)
                                .clipShape(.circle)
                                .addStroke(radius: 100)
                        } else {
                            Image.person
                                .resizeFitTo(size: 110,renderingMode: .template)
                                .foregroundColor(.text)
                        }
                        Spacer()
                        Image.edit
                            .resizeFitTo(size: 25)
                            .padding(.horizontal)
                            .onTap {
                                Navigation.pushToSwiftUiView(EditProfileVIew())
                            }
                    }
                    Text(vm.myUser?.fullname ?? "Anderson coper")
                        .outfitMedium(20)
                        .padding(.vertical,10)
                    if !isPro {
                        ProfileProCard{
                            Navigation.pushToSwiftUiView(ProView())
                        }
                    }
                    MySpaceFieldCardWithSwitch(icon: .notification, title: .notifications,isNotificationCard: true)
                    
                    ProfileFieldCard(icon: .downloads, title: .downloads){
                            Navigation.pushToSwiftUiView(DownloadView())
                    }
                    ProfileFieldCard(icon: .language, title: .language){
                        Navigation.pushToSwiftUiView(LanguageView())
                    }
                    ProfileFieldCard(icon: .privacy, title: .privacyPolicy){
                        vm.isPrivacyURLSheet = true
                    }
                    ProfileFieldCard(icon: .terms, title: .termsNuses){
                        vm.isTermsURLSheet = true
                    }
                    ProfileFieldCard(icon: .rate, title: .rateThisApp){
                        if let url = URL(string: rateThisAppURL) {
                            UIApplication.shared.open(url)
                        }
                    }
                    ProfileFieldCard(icon: .logout, title: .logout){
                        vm.isLogoutDialogShow = true
                    }
                    ProfileFieldCard(icon: .delete, title: .deleteMyAccount,titleColor: .base){
                        vm.isDeleteDialogShow = true
                    }
                }
                .padding(.vertical,10)
            }
        }
        .hideNavigationbar()
        .customAlert(isPresented: $vm.isLogoutDialogShow){
            DialogCard(icon: Image.logout, title: .areYouSure, subTitle: .logoutDes, buttonTitle: .logout, onClose: {
                withAnimation {
                    vm.isLogoutDialogShow = false
                }
            },onButtonTap: {
                deleteDownloadData()
                vm.isLogoutDialogShow = false
                vm.logOutMyAc()
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    Navigation.pop(false)
                }
            })
            .animation(.default, value: vm.isLogoutDialogShow)
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
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    Navigation.pop(false)
                }
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

struct ProfileProCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    var onTap : ()->() = {}
    var body: some View {
        ZStack {
            HStack {
                Image.premium
                    .resizeFitTo(width: 26, height: 26)
                HStack(spacing: 5) {
                    Text(String.becomeA.localized(language))
                        .outfitRegular(14)
                        .foregroundColor(.text)
                    Text(String.pro.localized(language))
                        .outfitSemiBold(14)
                        .foregroundColor(.base)
                }
                Spacer()
            }
            .padding(17)
            .background(
                HStack {
                    Spacer()
                        .maxWidthFrame()
                    Image.premium_bg
                        .resizable()
                        .scaledToFill()
                        .maxWidthFrame()
                }
                
            )
            .clipped()
            .contentShape(Rectangle())
            .background(Color.bg)
            .customCornerRadius(radius: 16)
            .overlay(content: {
                RoundedRectangle(cornerRadius: 17)
                    .stroke(.base, lineWidth: 1)
            })
            .padding(.horizontal)
        }
        .onTap(completion: onTap)
    }
}

struct ProfileFieldCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    let icon: Image
    let title: String
    var titleColor : Color = .text
    var onTap: ()->() = {}
    
    var body: some View {
        ZStack {
            HStack(spacing: 12) {
                icon
                    .resizeFitTo(width: 25, height: 25,renderingMode: .template)
                    .foregroundColor(titleColor)
                    .frame(width: 25)
                Text(title.localized(language))
                    .foregroundColor(titleColor)
                    .outfitRegular(14)
                    .frame(width: Device.width / 1.5,alignment: .leading)
                    .fixedSize(horizontal: true, vertical: false)
            }
            .frame(maxWidth: .infinity,alignment: .leading)
            .onTap(completion: onTap)
        }
        .padding(17)
        .addbgToProfileCard()
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
                        .resizeFitTo(width: 20, height: 20, renderingMode: .template)
                        .foregroundColor(.text)
                        .frame(width: 20)
                    VStack(alignment: .leading) {
                        Text(title.localized(language))
                            .foregroundColor(.text)
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

