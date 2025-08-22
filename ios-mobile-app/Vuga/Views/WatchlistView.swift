//
//  WatchlistView.swift
//  Vuga
//
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
                    if vm.useUnifiedWatchlist {
                        // Use unified watchlist items
                        ForEach(vm.unifiedItems) { item in
                            UnifiedWatchlistCardView(vm: vm, item: item)
                        }
                    } else {
                        // Fallback to legacy content-only view
                        ForEach(vm.contents.indices, id: \.self) { index in
                            if index < vm.contents.count {
                                WatchlistCardView(vm: vm, content: vm.contents[index])
                            }
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
        .loaderView(vm.isLoading && vm.unifiedItems.isEmpty && vm.contents.isEmpty)
        .noDataFound(!vm.isLoading && vm.unifiedItems.isEmpty && vm.contents.isEmpty)
    }
}

#Preview {
    WatchlistView()
}

// New unified watchlist card that handles both content and episodes
struct UnifiedWatchlistCardView: View {
    @StateObject var vm: WatchlistViewModel
    let item: UnifiedWatchlistItem
    
    var body: some View {
        Group {
            if item.isEpisode {
                EpisodeWatchlistCard(vm: vm, item: item)
            } else {
                ContentWatchlistCard(vm: vm, item: item)
            }
        }
        .onAppear {
            // Load more when reaching last item
            if item.id == vm.unifiedItems.last?.id {
                vm.fetchWatchlist()
            }
        }
    }
}

// Episode card in watchlist
struct EpisodeWatchlistCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.myUser) var myUser: User? = nil
    @StateObject var vm: WatchlistViewModel
    let item: UnifiedWatchlistItem
    @State private var isLoadingEpisode = false
    @State private var fetchedContent: VugaContent?
    @State private var fetchedEpisode: Episode?
    
    var body: some View {
        HStack(spacing: 9) {
            // Episode thumbnail
            if let thumbnailURL = item.thumbnailURL {
                KFImage(thumbnailURL)
                    .resizeFillTo(width: 150, height: 105, compressSize: 2)
                    .cornerRadius(radius: 15)
                    .addStroke(radius: 15)
                    .cornerRadius(radius: 15)
            } else {
                RoundedRectangle(cornerRadius: 15)
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 150, height: 105)
                    .overlay(
                        Image(systemName: "tv")
                            .font(.system(size: 30))
                            .foregroundColor(.white.opacity(0.5))
                    )
            }
            
            VStack(alignment: .leading, spacing: 6) {
                // Series title with season/episode info
                VStack(alignment: .leading, spacing: 2) {
                    if let seriesTitle = item.seriesTitle {
                        Text(seriesTitle)
                            .outfitMedium(14)
                            .foregroundColor(.textLight)
                            .lineLimit(1)
                    }
                    
                    HStack(spacing: 4) {
                        if let seasonNum = item.seasonNumber,
                           let episodeNum = item.episodeNumber {
                            Text("S\(seasonNum) E\(episodeNum):")
                                .outfitSemiBold(16)
                                .foregroundColor(.text)
                        }
                        Text(item.title ?? "Episode")
                            .outfitSemiBold(16)
                            .foregroundColor(.text)
                            .lineLimit(1)
                    }
                }
                
                // Rating and type badge
                HStack(spacing: 8) {
                    // Episode badge
                    Text("EPISODE")
                        .outfitMedium(10)
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.orange)
                        .cornerRadius(4)
                    
                    if let ratings = item.ratings, let rating = Double(ratings), rating > 0 {
                        HStack(spacing: 5) {
                            Image.star
                                .resizeFitTo(size: 10)
                            Text(String(format: "%.1f", rating))
                                .outfitLight(15)
                        }
                        .foregroundColor(.rating)
                    }
                }
                
                Spacer()
            }
            
            Spacer(minLength: 0)
        }
        .contentShape(Rectangle())
        .onTap {
            if !isLoadingEpisode {
                navigateToEpisode()
            }
        }
        .overlay(
            // Remove from watchlist button
            Button(action: {
                vm.removeEpisodeFromWatchlist(item: item)
            }) {
                Image.bookmarkFill
                    .resizeFitTo(size: 18, renderingMode: .template)
                    .foregroundColor(.text)
                    .frame(width: 35, height: 35)
                    .background(Color.text.opacity(0.2))
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color.text.opacity(0.2), lineWidth: 1))
            }
            .padding(.top, 10)
            .padding(.trailing, 10),
            alignment: .topTrailing
        )
    }
    
    private func navigateToEpisode() {
        guard let contentId = item.contentId,
              let episodeId = item.episodeId else { return }
        
        isLoadingEpisode = true
        
        // Fetch content details first
        var params: [Params: Any] = [.contentId: contentId]
        
        if let user = myUser {
            params[.userId] = user.id ?? 0
            if let profileId = user.lastActiveProfileId {
                params[.profileId] = profileId
            }
        }
        
        NetworkManager.callWebService(url: .fetchContentDetails, params: params) { [self] (obj: ContentModel) in
            if let fetchedContent = obj.data {
                self.fetchedContent = fetchedContent
                
                // Find the episode in the content's seasons
                if let seasons = fetchedContent.seasons {
                    for season in seasons {
                        if let episodes = season.episodes {
                            for ep in episodes {
                                if ep.id == episodeId {
                                    self.fetchedEpisode = ep
                                    break
                                }
                            }
                        }
                        if self.fetchedEpisode != nil {
                            break
                        }
                    }
                }
                
                // Navigate to episode detail
                DispatchQueue.main.async {
                    if let episode = self.fetchedEpisode {
                        Navigation.pushToSwiftUiView(
                            EpisodeDetailView(
                                episode: episode,
                                seriesContent: fetchedContent
                            )
                        )
                    } else {
                        // Fallback to content detail if episode not found
                        Navigation.pushToSwiftUiView(
                            ContentDetailView(contentId: contentId)
                        )
                    }
                    self.isLoadingEpisode = false
                }
            } else {
                // On failure, navigate to content detail as fallback
                DispatchQueue.main.async {
                    Navigation.pushToSwiftUiView(
                        ContentDetailView(contentId: contentId)
                    )
                    self.isLoadingEpisode = false
                }
            }
        }
    }
}

// Content card in watchlist (movies/TV shows)
struct ContentWatchlistCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm: WatchlistViewModel
    let item: UnifiedWatchlistItem
    
    var body: some View {
        HStack(spacing: 9) {
            // Content poster
            if let posterURL = item.thumbnailURL {
                KFImage(posterURL)
                    .resizeFillTo(width: 150, height: 105, compressSize: 2)
                    .cornerRadius(radius: 15)
                    .addStroke(radius: 15)
                    .cornerRadius(radius: 15)
            } else {
                RoundedRectangle(cornerRadius: 15)
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 150, height: 105)
            }
            
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    Text(item.title ?? "")
                        .outfitSemiBold(18)
                        .foregroundColor(.text)
                        .lineLimit(1)
                    Spacer(minLength: 0)
                    CommonIcon(image: .bookmarkFill)
                        .frame(height: 1)
                        .hidden()
                }
                
                HStack(spacing: 8) {
                    if let ratings = item.ratings, let rating = Double(ratings), rating > 0 {
                        HStack(spacing: 5) {
                            Image.star
                                .resizeFitTo(size: 10)
                            Text(String(format: "%.1f", rating))
                                .outfitLight(15)
                        }
                        .foregroundColor(.rating)
                    }
                    
                    if item.type == 1 {
                        Text("MOVIE")
                            .outfitMedium(10)
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.blue)
                            .cornerRadius(4)
                    } else if item.type == 2 {
                        Text("TV SHOW")
                            .outfitMedium(10)
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.green)
                            .cornerRadius(4)
                    }
                }
                
                Spacer()
            }
            
            Spacer(minLength: 0)
        }
        .contentShape(Rectangle())
        .onTap {
            if let contentId = item.contentId, contentId > 0 {
                Navigation.pushToSwiftUiView(ContentDetailView(contentId: contentId))
            }
        }
        .overlay(
            // Remove from watchlist button
            Button(action: {
                if let contentId = item.contentId {
                    vm.removeContentFromWatchlist(contentId: contentId)
                }
            }) {
                Image.bookmarkFill
                    .resizeFitTo(size: 18, renderingMode: .template)
                    .foregroundColor(.text)
                    .frame(width: 35, height: 35)
                    .background(Color.text.opacity(0.2))
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color.text.opacity(0.2), lineWidth: 1))
            }
            .padding(.top, 10)
            .padding(.trailing, 10),
            alignment: .topTrailing
        )
    }
}

// Legacy watchlist card view (kept for compatibility)
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
                            .outfitLight(15)
                    }
                    .foregroundColor(.rating)
                    
                    Rectangle()
                        .frame(width: 1,height: 13)
                        .foregroundColor(.textLight)
                    
                    Text(verbatim: "\(content.releaseYear ?? 2020)")
                        .outfitLight(15)
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