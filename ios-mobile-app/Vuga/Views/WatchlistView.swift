//
//  WatchlistView.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 10/05/24.
//

import SwiftUI
import Kingfisher

struct WatchlistView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm = WatchlistViewModel()
    @Namespace var animation

    var body: some View {
        VStack(spacing: 0) {
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
                .padding(.horizontal,10)
                .padding(.vertical,12)
            ScrollView(showsIndicators: false) {
                LazyVStack(spacing: 10) {
                    ForEach(vm.contents.indices, id: \.self) { index in
                        if index < vm.contents.count {
                            WatchlistCardView(vm: vm, content: vm.contents[index])
                        }
                    }
                }
                .padding([.horizontal,.bottom],10)
            }
        }
        .addBackground()
        .onAppear(perform: {
            if !vm.isDataFetched {
                vm.fetchWatchlist(isForRefresh: true)
            }
        })
        .onChange(of: vm.contentType, perform: { value in
            vm.fetchWatchlist(isForRefresh: true)
        })
        .onReceive(NotificationCenter.default.publisher(for: .profileChanged)) { _ in
            print("WatchlistView: Profile changed, refreshing watchlist")
            vm.refreshForProfileChange()
        }
        .loaderView(vm.isLoading && vm.contents.isEmpty)
        .noDataFound(!vm.isLoading && vm.contents.isEmpty)
    }
}

#Preview {
    WatchlistView()
}


struct WatchlistCardView: View {
    @StateObject var vm : WatchlistViewModel
    @State var isShowWatchlistDialog = false
    var content: VugaContent

    var body: some View {
        ContentHorizontalCard(content: content) {
            CommonIcon(image: .bookmarkFill) {
                vm.removeFromWatchlist(content: content)
            }
            .padding(.top, 10)
        }
        .onAppear(perform: {
            if content.id == vm.contents.last?.id {
                vm.fetchWatchlist()
            }
        })
    }
}


struct ContentHorizontalCard<ContentV: View>: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var view: (() -> ContentV)?
    var content: VugaContent
    init(content: VugaContent, @ViewBuilder view: @escaping () -> ContentV) {
        self.content = content
        self.view = view
    }
    init(content: VugaContent) where ContentV == EmptyView {
        self.content = content
        self.view = nil
    }
    var body: some View {
        HStack(spacing: 9) {
            KFImage(content.horizontalPoster?.addBaseURL())
                .resizeFillTo(width: 150, height: 105, compressSize: 2)
                .cornerRadius(radius: 15)
                .addStroke(radius: 15)
                .cornerRadius(radius: 15)
            VStack(alignment: .leading, spacing: 6,content: {
                HStack {
                    Text(content.title ?? "")
                        .outfitSemiBold(18)
                        .foregroundColor(.text)
                        .lineLimit(1)
                    Spacer(minLength: 0)
                    CommonIcon(image: .bookmarkFill)
                        .frame(height: 1)
                        .hidden()
                }
                
                HStack(spacing: 8) {
                    HStack(spacing: 5) {
                        Image.star
                            .resizeFitTo(size: 10)
                        Text(content.ratingString)
                            .outfitLight(14)
                    }
                    .foregroundColor(.rating)
                    
                    Rectangle()
                        .frame(width: 1,height: 13)
                        .foregroundColor(.textLight)
                    
                    Text(verbatim: "\(content.releaseYear ?? 2020)")
                        .outfitLight(14)
                        .foregroundColor(.textLight)
                }
                
                Text(content.genreString)
                    .outfitLight()
                    .foregroundColor(.textLight)
                    .lineLimit(1)
            })
            Spacer(minLength: 0)
        }
        .overlay(ZStack{
            if view != nil {
                (self.view!)()
            }
        }, alignment: .topTrailing)
        .onTap {
            if let id = content.id, id > 0 {
                Navigation.pushToSwiftUiView(ContentDetailView(contentId: id))
            } else {
                print("WatchlistView: Invalid content ID, cannot open detail view")
            }
        }
    }
}
