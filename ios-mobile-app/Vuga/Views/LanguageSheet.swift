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
    @State private var isLoadingLanguages = false
    
    var languages : [ContentLanguage] {
        var langs = SessionManager.shared.getLanguages()
        print("LanguageSheet: Retrieved \(langs.count) languages from SessionManager")
        
        // If no languages are stored, try to fetch them again
        if langs.isEmpty {
            print("LanguageSheet: No languages found. This might be a first-run issue.")
            print("LanguageSheet: Please restart the app to fetch languages from the server.")
            
            // For debugging: Check what's actually stored
            let storedString = UserDefaults.standard.string(forKey: "languages") ?? "nil"
            print("LanguageSheet: Raw stored languages string: '\(storedString)'")
        }
        
        for lang in langs {
            print("  - \(lang.title ?? "nil") (id: \(lang.id ?? 0))")
        }
        langs.insert(ContentLanguage(id: 0, title: .all.localized(language), createdAt: "", updatedAt: ""), at: 0)
        print("LanguageSheet: Total languages after adding 'All': \(langs.count)")
        return langs
    }
    func fetchLanguages() {
        print("LanguageSheet: Manually fetching languages from API")
        isLoadingLanguages = true
        
        NetworkManager.callWebService(url: .fetchSettings,
            timeout: 10,
            callbackSuccess: { (obj: SettingModel) in
                print("LanguageSheet: Settings fetched successfully")
                if let fetchedLanguages = obj.languages, !fetchedLanguages.isEmpty {
                    print("LanguageSheet: Received \(fetchedLanguages.count) languages")
                    SessionManager.shared.setLanguages(data: fetchedLanguages)
                } else {
                    print("LanguageSheet: No languages in API response")
                }
                
                if let fetchedGenres = obj.genres, !fetchedGenres.isEmpty {
                    SessionManager.shared.setGenres(data: fetchedGenres)
                }
                
                DispatchQueue.main.async {
                    self.isLoadingLanguages = false
                }
            },
            callbackFailure: { error in
                print("LanguageSheet: Failed to fetch settings: \(error)")
                DispatchQueue.main.async {
                    self.isLoadingLanguages = false
                }
            }
        )
    }
    
    var body: some View {
        if isOn {
            VStack {
                Text(String.languages.localized(language))
                    .outfitRegular(24)
                    .foregroundColor(.text)
                
                // Show refresh button if no languages
                if languages.count <= 1 && !isLoadingLanguages {
                    Button(action: {
                        fetchLanguages()
                    }) {
                        HStack {
                            Image(systemName: "arrow.clockwise")
                            Text("Tap to load languages")
                        }
                        .foregroundColor(.base)
                        .padding()
                    }
                }
                
                if isLoadingLanguages {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .base))
                        .padding()
                }
                
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

