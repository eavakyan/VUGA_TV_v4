//
//  ExtraViews.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import SwiftUI
import Kingfisher

struct MyTextField: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var placeholder: String
    @Binding var text: String
    var body: some View {
        VStack {
            let placeholder = Text(placeholder.localized(language))
                .foregroundColor(Color.textLight)
            TextField("", text: $text, prompt: placeholder)
                .outfitMedium()
                .foregroundColor(.text)
                .padding()
                .maxWidthFrame()
                .background(Color.cardBg)
                .cornerRadius(radius: 15)
                .padding(1)
                .background(Color.cardBorder)
                .cornerRadius(radius: 16)
        }
    }
}

struct MySecuredTextField: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @State var isShown = false
    var placeholder: String
    @Binding var text: String
    var body: some View {
        VStack{
            let placeholder = Text(placeholder.localized(language))
                .foregroundColor(Color.textLight)
            HStack {
                ZStack {
                    if isShown {
                        TextField("", text: $text, prompt: placeholder)
                    } else {
                        SecureField("", text: $text, prompt: placeholder)
                    }
                }
                .outfitMedium()
                
                Button {
                    isShown.toggle()
                } label: {
                    Image(systemName: isShown ? "eye.slash.fill" : "eye.fill")
                        .resizeFitTo(size: 20)
                        .animation(.none, value: isShown)
                }
                .padding(.bottom,3)
            }
            
            .foregroundColor(.text)
            .padding()
            .maxWidthFrame()
            .background(Color.cardBg)
            .cornerRadius(radius: 15)
            .padding(1)
            .background(Color.cardBorder)
            .cornerRadius(radius: 16)
        }
    }
}

struct CommonButton: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var title: String
    var isDisable: Bool = false
    var vPadding : CGFloat = 18
    var borderRadius: CGFloat = 16
    var fontSize: CGFloat = 20
    var onTap: ()->() = {}
    var body: some View {
        Text(title.localized(language))
            .outfitMedium(fontSize)
            .foregroundColor(.text)
            .padding(.vertical,vPadding)
            .maxWidthFrame()
            .background(Color.base)
            .cornerRadius(radius: borderRadius)
            .onTap(completion: onTap)
            .disabled(isDisable)
            .opacity(isDisable ? 0.5 : 1)
    }
}

struct BackButton: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @Environment(\.dismiss) var dismiss
    var onTap : (() -> ())? = nil
    var iconSize : CGFloat = 12
    var body: some View {
        Image.back
            .font(.system(size: iconSize, weight: .bold))
            .rotationEffect(.degrees(language == .Arabic ? 180 : 0))
            .makeBgOfButton()
            .onTap {
                onTap?() ?? dismiss()
            }
    }
}

struct SimpleBackButton: View {
    @Environment(\.dismiss) var dismiss
    var onTap : (() -> ())? = nil
    var iconSize : CGFloat = 12
    var body: some View {
        Image.back
            .font(.system(size: iconSize, weight: .bold))
            .padding(10)
            .onTap {
                onTap?() ?? dismiss()
            }
    }
}

struct SearchButton: View {
    var onTap : () -> () = {}
    var iconSize : CGFloat = 18
    var body: some View {
        Image.search
            .resizeFitTo(size: iconSize,renderingMode: .template)
            .makeBgOfButton(bgColor: .bg)
            .onTap(completion: onTap)
    }
}

struct CommonIcon: View {
    @Environment(\.dismiss) var dismiss
    var image: Image
    var onTap : (() -> ())? = nil
    var body: some View {
        image
            .resizeFitTo(size: 18, renderingMode: .template)
            .foregroundColor(.text)
            .frame(width: 35, height: 35)
            .background(Color.text.opacity(0.2))
            .clipShape(Circle())
            .overlay(Circle().stroke(Color.text.opacity(0.2),lineWidth: 1))
            .onTap {
                onTap?() ?? dismiss()
            }
    }
}

struct TopBar: View {
    @AppStorage(SessionKeys.myUser) var myUser : User? = nil
    @ObservedObject private var sessionManager = SessionManager.shared
    var shouldShowColor = true
    var isBlur = false
    var isLiveTvView = false
    var body: some View {
        HStack {
            Image.logoHorizontal
                .resizeFitTo(width: 75, height: 22)
            
            Spacer()
            if isLiveTvView {
                SearchButton(){
                    Navigation.pushToSwiftUiView(LiveTvSearchView())
                }
                .padding(.trailing, 10)
            }
            ZStack {
                // Check for current profile's avatar first
                if let currentProfile = sessionManager.currentProfile {
                    if currentProfile.avatarType == "default" || currentProfile.avatarType == "color" {
                        // Show color avatar
                        ZStack {
                            Circle()
                                .fill(Color(hexString: currentProfile.avatarColor))
                                .frame(width: 35, height: 35)
                            
                            Text(currentProfile.initial)
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                        }
                        .addStroke(radius: 50, lineWidth: 1)
                    } else if currentProfile.avatarType == "custom", 
                              let avatarUrl = currentProfile.avatarUrl, 
                              !avatarUrl.isEmpty {
                        // Show custom image avatar
                        KFImage(URL(string: avatarUrl))
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(width: 35, height: 35)
                            .clipShape(.circle)
                            .addStroke(radius: 50, lineWidth: 1)
                    } else {
                        // Fallback to color avatar
                        ZStack {
                            Circle()
                                .fill(Color(hexString: currentProfile.avatarColor))
                                .frame(width: 35, height: 35)
                            
                            Text(currentProfile.initial)
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                        }
                        .addStroke(radius: 50, lineWidth: 1)
                    }
                } else if myUser?.profileImage != nil && myUser?.profileImage != "" {
                    // Fallback to user's profile image if no current profile
                    KFImage(myUser?.profileImage?.addBaseURL())
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 35,height: 35)
                        .clipShape(.circle)
                        .addStroke(radius: 50,lineWidth: 1)
                } else {
                    // Default person icon
                    Image.person
                        .resizeFitTo(size: 35, renderingMode: .template)
                        .foregroundColor(.text)
                }
            }
            .onTap {
                Navigation.pushToSwiftUiView(ProfileView())
            }
        }
        
        .padding()
        .frame(height: 60)
    //        .background((shouldShowColor ? Color.bg : Color.bg.opacity(0.01)).ignoresSafeArea())
//        .background(
//            (isBlur ? AnyView(Blur().opacity(shouldShowColor ? 1 : 0.01)) : AnyView(Color.bg))
//                .ignoresSafeArea())
}
}

struct Heading<Content: View>: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var title: String
    let content: (() -> Content)?
    
    init(title: String, @ViewBuilder content: @escaping () -> Content) {
        self.title = title
        self.content = content
    }
    init(title: String) where Content == EmptyView {
        self.title = title
        self.content = nil
    }
    
    var body: some View {
        HStack {
            Text(title.localized(language))
                .outfitMedium(16)
                .foregroundColor(.white)
            Spacer()
            if content != nil {
                (self.content!)()
            }
        }
    }
}

struct Blur: UIViewRepresentable {
    var style: UIBlurEffect.Style = .dark
    func makeUIView(context: Context) -> UIVisualEffectView {
        return UIVisualEffectView(effect: UIBlurEffect(style: style))
    }
    func updateUIView(_ uiView: UIVisualEffectView, context: Context) {
        uiView.effect = UIBlurEffect(style: style)
    }
}

struct PlayButton: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var size: CGFloat = 24
    var color: Color = .base
    var body: some View {
        Image(systemName: "play.circle.fill")
            .rotationEffect(.degrees(language == .Arabic ? 180 : 0))
            .font(.system(size: size, weight: .bold))
            .foregroundStyle(.white, color)
            .background(color)
            .clipShape(Circle())
    }
}

struct BackBarView<Content: View> : View {
    var title: String
    let content: (() -> Content)?
    var showContentOnMaxSize : Bool
    
    init(title: String, @ViewBuilder content: @escaping () -> Content, showContentOnMaxSize : Bool = false) {
        self.title = title
        self.content = content
        self.showContentOnMaxSize = showContentOnMaxSize
    }
    init(title: String,showContentOnMaxSize : Bool = false) where Content == EmptyView {
        self.title = title
        self.content = nil
        self.showContentOnMaxSize = showContentOnMaxSize
    }
    
    var body: some View {
        HStack {
            BackButton()
            Spacer()
            Text(title)
                .outfitSemiBold(20)
                .foregroundColor(.text)
            Spacer()
            BackButton()
                .hidden()
                .overlay(!showContentOnMaxSize ? content?() : nil)
            if showContentOnMaxSize {
                content?()
            }
        }
        .frame(height: 50)
        .padding(.horizontal)
    }
}

struct BottomXMarkButton: View {
    var onTap: ()->()
    var body: some View {
        Image.xamrk
            .font(.system(size: 20, weight: .light))
            .padding(12)
            .foregroundColor(.black)
            .background(Color.text)
            .clipShape(Circle())
            .onTap(completion: onTap)
    }
}

struct CloseButton :View {
    var onTap: ()->() = {}
    var size: CGFloat = 12
    var body: some View {
        Image.close
            .font(.system(size: size,weight: .bold))
            .makeBgOfButton()
            .onTap(completion: onTap)
    }
}
