//
//  LanguageSheet.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 27/05/24.
//

import SwiftUI

struct LanguageSheet: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    @Binding var isOn: Bool
    @Binding var selectedLanguage: ContentLanguage
    var languages : [ContentLanguage] {
        var langs = SessionManager.shared.getLanguages()
        langs.insert(ContentLanguage(id: 0, title: .all.localized(language), createdAt: "", updatedAt: ""), at: 0)
        return langs
    }
    var body: some View {
        if isOn {
            VStack {
                Text(String.languages.localized(language))
                    .outfitRegular(24)
                    .foregroundColor(.text)
                
                ScrollView(showsIndicators: false) {
                    LazyVStack(spacing: 10) {
                        ForEach(languages, id: \.id) { language in
                            Text(language.title ?? "")
                                .font(.custom(selectedLanguage.id == language.id ? MyFont.OutfitSemiBold : MyFont.OutfitLight, size: 24))
                                .foregroundColor(selectedLanguage.id == language.id ? .text : .textLight)
                                .lineLimit(1)
                                .maxWidthFrame(.leading)
                                .padding(12)
                                .padding(.horizontal, 10)
                                .onTap {
                                    selectedLanguage = language
                                    isOn = false
                                }
                        }
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 100)
                }
                
                .mask(VStack(spacing: 0){
                    Rectangle().fill(LinearGradient(colors: [.clear, .clear,.black], startPoint: .top, endPoint: .bottom))
                        .frame(height: 80)
                    Rectangle()
                    Rectangle().fill(LinearGradient(colors: [.black, .clear, .clear], startPoint: .top, endPoint: .bottom))
                        .frame(height: 80)
                })
                
                BottomXMarkButton {
                    isOn = false
                }
                if !isPro && SessionManager.shared.getSetting()?.isAdmobIos != 0 {
                    BannerAd()
                        .padding(.horizontal,-18)
                        .padding(.top,10)
                }
            }
            .padding(.vertical)
            .background(Color.bg.opacity(0.9))
        }
    }
}

