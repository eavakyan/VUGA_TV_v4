//
//  SearchView.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 10/05/24.
//

import SwiftUI

struct SearchView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm = SearchViewModel()
    var body: some View {
        VStack(spacing: 0) {
            SearchTopView(vm: vm)
            ScrollView(showsIndicators: false) {
                LazyVStack {
                    ForEach(vm.contents, id: \.id) { content in
                        ContentHorizontalCard(content: content)
                            .onAppear(perform: {
                                if content.id == vm.contents.last?.id ?? 0 {
                                    vm.searchContent(isForRefresh: false)
                                }
                            })
                    }
                }
                .padding([.horizontal, .bottom], 10)
            }
            .loaderView(vm.isLoading && vm.contents.isEmpty, shouldShowBg: false)
            .noDataFound(!vm.isLoading && vm.contents.isEmpty)
        }
        .addBackground()
        .onAppear(perform: {
            if !vm.isDataFetched {
                vm.searchContent()
            }
        })
        .onChange(of: vm.keyword, perform: { value in
            vm.searchContent()
        })
        .onChange(of: vm.selectedLang.id, perform: { value in
            vm.searchContent()
        })
        .onChange(of: vm.selectedGenre.id, perform: { value in
            vm.searchContent()
        })
        .onChange(of: vm.contentType, perform: { value in
            vm.searchContent()
        })
        .onChange(of: vm.isLanguageSheet, perform: { newValue in
            if newValue {
                vm.hideTabbar()
            } else {
                vm.showTabbar()
            }
        })
        .onChange(of: vm.isGenreSheet, perform: { newValue in
            if newValue {
                vm.hideTabbar()
            } else {
                vm.showTabbar()
            }
        })
        .blur(radius: vm.isGenreSheet || vm.isLanguageSheet ? 7 : 0)
        .overlay(LanguageSheet(isOn: $vm.isLanguageSheet, selectedLanguage: $vm.selectedLang))
        .overlay(GenreSheet(isOn: $vm.isGenreSheet, selectedGenre: $vm.selectedGenre))
        .animation(.default, value: vm.isGenreSheet)
        .animation(.default, value: vm.isLanguageSheet)
        .onReceive(NotificationCenter.default.publisher(for: .setSearchFilter)) { notification in
            if let userInfo = notification.userInfo,
               let contentType = userInfo["contentType"] as? ContentType {
                // Set the content type filter
                vm.contentType = contentType
                // Clear any existing search keyword
                vm.keyword = ""
                // Trigger search
                vm.searchContent()
            }
        }
    }
}

#Preview {
    SearchView()
}

extension View {
    func searchOptionBg() -> some View {
        self
            .frame(height: 50)
            .background(Color.searchBg)
            .cornerRadius(radius: 15)
            .addStroke(radius: 15)
    }
}

private struct SearchSelectedTag: View {
    var text: String
    var onCloseTap: ()->() = {}
    var body: some View {
        HStack {
            Text(text)
                .outfitMedium(18)
                .foregroundColor(.textLight)
            Image.close
                .font(.system(size: 15,weight: .bold))
                .foregroundColor(.textLight)
                .onTap {
                    onCloseTap()
                }
        }
        .padding(.vertical, 4)
        .padding(.horizontal, 18)
        .padding(.bottom, 2)
        .background(Color.searchBg)
        .cornerRadius(radius: 10)
        .addStroke(radius: 11)
    }
}

private struct SearchTopView : View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm: SearchViewModel
    @Namespace var animation
    
    var body: some View {
        VStack(spacing: 10) {
            HStack(spacing: 10) {
                HStack(spacing: 12) {
                    Image.search
                        .resizeFitTo(size: 25, renderingMode: .template)
                        .foregroundColor(.stroke)
                    Capsule()
                        .frame(width: 1,height: 30)
                        .foregroundColor(.stroke)
                    
                    TextField("", text: $vm.keyword, prompt: Text(String.searchHere.localized(language))
                        .font(Font.custom(MyFont.OutfitRegular,size: CGFloat(16)))
                    )
                    .foregroundColor(.text)
                    .outfitRegular()
                    if vm.keyword.isNotEmpty {
                        Image.close
                            .resizeFitTo(size: 15,renderingMode: .template)
                            .foregroundColor(.textLight)
                            .onTap {
                                vm.keyword = ""
                            }
                    }
                    
                }
                .padding(.horizontal)
                .searchOptionBg()
                
                Image.languageSearch
                    .resizeFitTo(size: 32, renderingMode: .template)
                    .maxFrame()
                    .foregroundColor(vm.selectedLang.id != 0 ? .text : .textLight)
                    .background(vm.selectedLang.id != 0 ? Color.base : Color.clear)
                    .cornerRadius(radius: 11)
                    .padding(4)
                    .frame(width: 50)
                    .searchOptionBg()
                    .onTap {
                        vm.isLanguageSheet.toggle()
                    }
            }
            
            HStack(spacing: 10) {
                HStack(spacing: 0) {
                    ForEach(ContentType.allCases, id: \.self) { type in
                        ZStack {
                            if vm.contentType == type {
                                RoundedRectangle(cornerRadius: 11)
                                    .matchedGeometryEffect(id: "search_id", in: animation)
                                    .foregroundColor(.base)
                            }
                            Text(type.title.localized(language))
                                .outfitMedium()
                                .maxFrame()
                                .foregroundColor(vm.contentType == type ? .text : .textLight)
                                .cornerRadius(radius: 11)
                                .onTap {
                                    withAnimation(.spring(response: 0.5, dampingFraction: 0.8, blendDuration: 1)) {
                                        vm.contentType = type
                                    }
                                }
                        }
                    }
                }
                .padding(4)
                .maxWidthFrame()
                .searchOptionBg()
                
                HStack(spacing: 10) {
                    Image.genre
                        .resizeFitTo(size: 28, renderingMode: .template)
                    
                    Text(String.genre.localized(language))
                        .outfitMedium()
                }
                .maxHeightFrame()
                .padding(.horizontal)
                .foregroundColor(vm.selectedGenre.id != 0 ? .text : .textLight)
                .background(vm.selectedGenre.id != 0 ? Color.base : Color.clear)
                .cornerRadius(radius: 11)
                .padding(4)
                .searchOptionBg()
                .onTap {
                    vm.isGenreSheet.toggle()
                }
            }
            
            if vm.selectedLang.id != 0 || vm.selectedGenre.id != 0 {
                HStack {
                    if vm.selectedLang.id != 0 {
                        SearchSelectedTag(text: vm.selectedLang.title ?? ""){
                            withAnimation {
                                vm.selectedLang = ContentLanguage(id: 0, title: .all, createdAt: "", updatedAt: "")
                            }
                        }
                    }
                    
                    if vm.selectedGenre.id != 0 {
                        SearchSelectedTag(text: vm.selectedGenre.title ?? ""){
                            withAnimation {
                                vm.selectedGenre = Genre(id: 0, title: "", createdAt: "", updatedAt: "", contents: [])
                            }
                        }
                    }
                    Spacer()
                }
            }
        }
        .padding(10)
    }
}
