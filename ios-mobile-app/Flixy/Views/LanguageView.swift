//
//  LanguageView.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 18/07/24.
//

import SwiftUI

struct LanguageView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @State private var selectedLanguage: Language = LocalizationService.shared.language

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 0) {
                BackBarView(title: .language.localized(language))
                ScrollView(showsIndicators: false) {
                    ForEach(appLanguages, id: \.language) { lang in
                        HStack {
                            VStack(alignment: .leading, spacing: 5) {
                                Text(lang.localName)
                                    .outfitMedium(16)
                                    .foregroundColor(.text)
                                
                                Text(lang.nameInEnglish)
                                    .outfitMedium(14)
                                    .foregroundColor(.textLight)
                            }
                            .padding(.horizontal)
                            Spacer()
                        }
                        .padding(.vertical,10)
                        .frame(maxWidth: .infinity,alignment: .leading)
                        .background(Color.bg)
                        .customCornerRadius(radius: 16)
                        .padding(1)
                        .background(Color.text.opacity(0.2))
                        .overlay(
                            RoundedRectangle(cornerRadius: 17, style: .continuous)
                                .stroke(selectedLanguage == lang.language ? Color.base : Color.text.opacity(0.2), lineWidth: 1)
                        )
                        .customCornerRadius(radius: 17)
                        .padding(.horizontal)
                        .onTapGesture {
                            selectedLanguage = lang.language
                        }
                        .padding([.vertical, .horizontal], 1.5)
                    }
                    .padding(.bottom, Device.bottomSafeArea + 110)
                    .padding(.top,20)
                }
            }
            CommonButton(title: .change,onTap: {
                changeLanguage()
            })
            .padding()
            .padding(.bottom)
        }
        .padding(.bottom, Device.bottomSafeArea)
        .addBackground()
        .hideNavigationbar()
        .edgesIgnoringSafeArea(.bottom)
    }
    
    func changeLanguage() {
        LocalizationService.shared.language = selectedLanguage
        Navigation.pop()
    }
}

#Preview {
    LanguageView()
}
