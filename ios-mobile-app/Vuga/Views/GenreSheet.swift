//
//  GenreSheet.swift
//  Vuga
//
//

import SwiftUI

struct GenreSheet: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    @Binding var isOn: Bool
    @Binding var selectedGenre: Genre
    var genres : [Genre] {
        var genres = SessionManager.shared.getGenres()
        genres.insert(Genre(id: 0, title: .all.localized(language), createdAt: "", updatedAt: "", contents: []), at: 0)
        return genres
    }
    var body: some View {
        if isOn {
            VStack {
                Text(String.genres.localized(language))
                    .outfitRegular(24)
                    .foregroundColor(.text)
                
                ScrollView(showsIndicators: false) {
                    LazyVStack(spacing: 10) {
                        ForEach(genres, id: \.id) { genre in
                            Text(genre.title ?? "")
                                .font(.custom(selectedGenre.id == genre.id ? MyFont.OutfitSemiBold : MyFont.OutfitLight, size: 24))
                                .foregroundColor(selectedGenre.id == genre.id ? .text : .textLight)
                                .lineLimit(1)
                                .maxWidthFrame(.leading)
                                .padding(12)
                                .padding(.horizontal, 10)
                                .onTap {
                                    selectedGenre = genre
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
                        .padding(.top,10)
                        .padding(.horizontal,-18)
                }
            }
            .padding(.vertical)
            .background(Color.bg.opacity(0.9))
        }
    }
}

