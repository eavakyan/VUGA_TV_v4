//
//  TabBarView.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import SwiftUI

struct TabBarView: View {
    @EnvironmentObject var vm : DownloadViewModel
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @FetchRequest(sortDescriptors: []) var downloads: FetchedResults<DownloadContent>
    @State var selectedTab = Tab.home
    @State var shouldTab = true
    var body: some View {
        VStack(spacing: 0) {
            switch selectedTab {
            case .home:
                HomeView(selectedTab: $selectedTab)
            case .search:
                SearchView()
            case .liveTV:
                LiveTVsView()
            case .watchlist:
                WatchlistView()
            }
            ZStack {
                if shouldTab {
                    VStack(spacing: 0) {
                        Divider()
                        HStack {
                            tabBtn(title: .home, image: .home, tab: .home)
                            tabBtn(title: .search, image: .search, tab: .search)
                           if SessionManager.shared.getSetting()?.isLiveTvEnable == 1 {
                                tabBtn(title: .liveTV, image: .liveTV, tab: .liveTV)
                            }
                            tabBtn(title: .watchlist, image: .save, tab: .watchlist)
                        }
                        .padding(.horizontal)
                    }
                    .transition(.move(edge: .bottom))
                }
            }
            .opacity(shouldTab ? 1 : 0)
        }
        .animation(.none, value: selectedTab)
        .animation(.default, value: shouldTab)
        .addBackground()
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .onReceive(NotificationCenter.default.publisher(for: .hideTabbar, object: nil)) { _ in
            shouldTab = false
        }
        .onReceive(NotificationCenter.default.publisher(for: .showTabbar, object: nil)) { _ in
            shouldTab = true
        }
        .onAppear(perform: {
            for content in downloads {
                vm.setDownloadContentFromCoredata(content: content)
            }
            if !vm.isDownloading {
                vm.startNextDownload()
            }
        })
    }
    
    func tabBtn(title: String, image: Image, tab: Tab) -> some View {
        VStack {
            image
                .resizeFitTo(size: 23, renderingMode: .template)
            Text(title.localized(language))
                .outfitRegular(12)
        }
        .foregroundColor(selectedTab == tab ? .base : .textLight)
        .maxWidthFrame()
        .frame(height: 65)
        .onTap {
            selectedTab = tab
        }
    }
}

enum Tab : Int {
    case home, search, liveTV, watchlist
}


struct Divider: View {
    var body: some View {
        Rectangle()
            .frame(height: 1)
            .foregroundColor(.text.opacity(0.2))
    }
}
