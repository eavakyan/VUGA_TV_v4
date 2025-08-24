//
//  TabBarView.swift
//  Vuga
//
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
            case .subscriptions:
                SubscriptionsView()
            case .watchlist:
                WatchlistView()
            case .profile:
                ProfileView(selectedTab: $selectedTab)
            }
            ZStack {
                if shouldTab {
                    VStack(spacing: 0) {
                        Divider()
                        HStack {
                            tabBtn(title: .home, image: .home, tab: .home)
                            tabBtn(title: .search, image: .search, tab: .search)
                            tabBtn(title: "Live TV", image: .tv, tab: .liveTV)
                            tabBtn(title: .subscriptions, image: .grid, tab: .subscriptions)
                            tabBtn(title: .watchlist, image: .save, tab: .watchlist)
                            profileTabBtn()
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
        .onReceive(NotificationCenter.default.publisher(for: .tabSelected, object: nil)) { notification in
            if let tab = notification.object as? Tab {
                selectedTab = tab
            }
        }
        .onAppear(perform: {
            for content in downloads {
                vm.setDownloadContentFromCoredata(content: content)
            }
            if !vm.isDownloading {
                vm.startNextDownload()
            }
            
            // Ensure proper initial state
            DispatchQueue.main.async {
                // Force a refresh to ensure views are properly positioned
                shouldTab = true
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
    
    // Profile tab with avatar (matches Android design)
    func profileTabBtn() -> some View {
        VStack {
            // Profile avatar circle with first letter
            Circle()
                .fill(selectedTab == .profile ? Color.base : Color.textLight)
                .frame(width: 20, height: 20)
                .overlay(
                    Text(getProfileFirstLetter())
                        .outfitSemiBold(10)
                        .foregroundColor(.bg)
                )
            
            Text("Profile")
                .outfitRegular(12)
        }
        .foregroundColor(selectedTab == .profile ? .base : .textLight)
        .maxWidthFrame()
        .frame(height: 65)
        .onTap {
            selectedTab = .profile
        }
    }
    
    // Helper function to get profile first letter
    private func getProfileFirstLetter() -> String {
        guard let profile = SessionManager.shared.getCurrentProfile(),
              !profile.name.isEmpty else {
            return "U"
        }
        return String(profile.name.prefix(1)).uppercased()
    }
}

enum Tab : Int {
    case home, search, liveTV, subscriptions, watchlist, profile
}


struct Divider: View {
    var body: some View {
        Rectangle()
            .frame(height: 1)
            .foregroundColor(.text.opacity(0.2))
    }
}
