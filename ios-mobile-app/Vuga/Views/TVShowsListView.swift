//
//  TVShowsListView.swift
//  Vuga
//
//

import SwiftUI
import Kingfisher

struct TVShowsListView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject private var vm = TVShowsListViewModel()
    @State private var selectedSortOption = "Latest"
    
    let sortOptions = ["Latest", "Popular", "Top Rated", "A-Z"]
    let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10)
    ]
    
    var body: some View {
        ZStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Header
                    HStack {
                        Button(action: {
                            Navigation.pop()
                        }) {
                            Image.back
                                .resizeFitTo(size: 24, renderingMode: .template)
                                .foregroundColor(.text)
                        }
                        
                        Text("TV Shows")
                            .outfitSemiBold(24)
                            .foregroundColor(.text)
                        
                        Spacer()
                        
                        // Sort menu
                        Menu {
                            ForEach(sortOptions, id: \.self) { option in
                                Button(option) {
                                    selectedSortOption = option
                                    vm.sortContent(by: option)
                                }
                            }
                        } label: {
                            HStack(spacing: 4) {
                                Text(selectedSortOption)
                                    .outfitRegular(14)
                                Image(systemName: "chevron.down")
                                    .font(.system(size: 10))
                            }
                            .foregroundColor(.textLight)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color.searchBg)
                            .cornerRadius(8)
                        }
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)
                    
                    // Content Grid
                    if vm.isLoading && vm.tvShows.isEmpty {
                        // Loading state
                        LazyVGrid(columns: columns, spacing: 15) {
                            ForEach(0..<12, id: \.self) { _ in
                                RoundedRectangle(cornerRadius: 10)
                                    .fill(Color.searchBg)
                                    .aspectRatio(2/3, contentMode: .fit)
                                    .shimmer()
                            }
                        }
                        .padding(.horizontal)
                    } else if vm.tvShows.isEmpty {
                        // Empty state
                        VStack(spacing: 20) {
                            Image.noData
                                .resizeFitTo(size: 100)
                                .opacity(0.5)
                            Text("No TV Shows Available")
                                .outfitRegular(18)
                                .foregroundColor(.textLight)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .padding(.top, 100)
                    } else {
                        // TV Shows Grid
                        LazyVGrid(columns: columns, spacing: 15) {
                            ForEach(vm.tvShows, id: \.id) { content in
                                TVShowGridItem(content: content)
                            }
                        }
                        .padding(.horizontal)
                        
                        // Load more indicator
                        if vm.isLoadingMore {
                            HStack {
                                Spacer()
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .base))
                                Spacer()
                            }
                            .padding(.vertical)
                        }
                    }
                }
                .padding(.bottom, 80) // Space for tab bar
            }
            .refreshable {
                vm.refreshContent()
            }
        }
        .addBackground()
        .hideNavigationbar()
        .onAppear {
            vm.fetchTVShows()
        }
    }
}

struct TVShowGridItem: View {
    let content: VugaContent
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            // Poster
            KFImage(content.verticalPoster?.addBaseURL())
                .resizeFillTo(width: (Device.width - 50) / 3, height: ((Device.width - 50) / 3) * 1.5, radius: 10)
                .addStroke(radius: 10)
                .overlay(
                    // Episodes count badge
                    Group {
                        if let seasons = content.seasons, !seasons.isEmpty {
                            let episodeCount = seasons.compactMap { $0.episodes?.count ?? 0 }.reduce(0, +)
                            Text("\(seasons.count)S • \(episodeCount)E")
                                .outfitRegular(10)
                                .foregroundColor(.text)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.bg.opacity(0.9))
                                .cornerRadius(5)
                                .padding(5)
                        }
                    },
                    alignment: .topTrailing
                )
            
            // Title
            Text(content.title ?? "")
                .outfitMedium(14)
                .foregroundColor(.text)
                .lineLimit(1)
            
            // Rating and Year
            HStack(spacing: 5) {
                HStack(spacing: 3) {
                    Image.star
                        .resizeFitTo(size: 10)
                    Text(content.ratingString)
                        .outfitLight(12)
                }
                .foregroundColor(.rating)
                
                Text("•")
                    .foregroundColor(.textLight)
                
                Text("\(content.releaseYear ?? 0)")
                    .outfitLight(12)
                    .foregroundColor(.textLight)
            }
        }
        .onTap {
            Navigation.pushToSwiftUiView(ContentDetailView(contentId: content.id))
        }
    }
}

// View Model for TV Shows
class TVShowsListViewModel: BaseViewModel {
    @Published var tvShows: [VugaContent] = []
    @Published var isLoadingMore = false
    private var currentPage = 1
    private var hasMorePages = true
    
    func fetchTVShows() {
        if isLoading { return }
        startLoading()
        
        let params: [Params: Any] = [
            .type: ContentType.series.rawValue,
            .page: currentPage
        ]
        
        NetworkManager.callWebService(url: .fetchContents, params: params) { (obj: ContentsModel) in
            self.stopLoading()
            if let contents = obj.data {
                if self.currentPage == 1 {
                    self.tvShows = contents
                } else {
                    self.tvShows.append(contentsOf: contents)
                }
                self.hasMorePages = contents.count >= 20 // Assuming 20 items per page
                self.currentPage += 1
            }
            self.isLoadingMore = false
        }
    }
    
    func refreshContent() {
        currentPage = 1
        hasMorePages = true
        tvShows = []
        fetchTVShows()
    }
    
    func loadMoreIfNeeded(currentItem: VugaContent) {
        guard let lastItem = tvShows.last,
              lastItem.id == currentItem.id,
              hasMorePages,
              !isLoadingMore else { return }
        
        isLoadingMore = true
        fetchTVShows()
    }
    
    func sortContent(by option: String) {
        switch option {
        case "Latest":
            tvShows.sort { ($0.createdAt ?? "") > ($1.createdAt ?? "") }
        case "Popular":
            tvShows.sort { ($0.totalView ?? 0) > ($1.totalView ?? 0) }
        case "Top Rated":
            tvShows.sort { ($0.ratings ?? 0) > ($1.ratings ?? 0) }
        case "A-Z":
            tvShows.sort { ($0.title ?? "") < ($1.title ?? "") }
        default:
            break
        }
    }
}