//
//  RecentlyWatchedGroupedView.swift
//  Vuga
//
//  View for displaying recently watched content grouped by genre
//

import SwiftUI
import Kingfisher

struct RecentlyWatchedGroupedView: View {
    @StateObject private var vm = RecentlyWatchedGroupedViewModel()
    @StateObject private var recentlyWatchedVM = RecentlyWatchedViewModel()
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    var body: some View {
        ZStack {
            Color("bgColor").ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                headerView
                
                if vm.isLoading && vm.groupedContent.isEmpty {
                    // Loading state
                    loadingView
                } else if vm.groupedContent.isEmpty {
                    // Empty state
                    emptyStateView
                } else {
                    // Grouped content
                    ScrollView(.vertical, showsIndicators: false) {
                        VStack(spacing: 20) {
                            // Content grouped by genre
                            ForEach(vm.groupedContent, id: \.genre.id) { group in
                                if !group.contents.isEmpty {
                                    genreRow(
                                        title: group.genre.title ?? "Genre",
                                        contents: group.contents
                                    )
                                }
                            }
                        }
                        .padding(.bottom, 100)
                    }
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            vm.loadRecentlyWatched(recentlyWatchedVM: recentlyWatchedVM)
        }
    }
    
    private var headerView: some View {
        HStack {
            Button(action: {
                Navigation.pop()
            }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundColor(.white)
            }
            
            Text("Recently Watched")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color("bgColor"))
    }
    
    private var loadingView: some View {
        VStack {
            Spacer()
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                .scaleEffect(1.2)
            Text("Loading...")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .padding(.top, 10)
            Spacer()
        }
    }
    
    private var emptyStateView: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "clock.arrow.circlepath")
                .font(.system(size: 50))
                .foregroundColor(.gray)
            Text("No recently watched content")
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(.gray)
            Text("Content you watch will appear here")
                .font(.system(size: 14))
                .foregroundColor(.gray.opacity(0.8))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            Spacer()
        }
    }
    
    private func genreRow(title: String, contents: [RecentlyWatchedContent]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            // Genre title
            Text(title)
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)
                .padding(.horizontal, 16)
            
            // Horizontal scroll of content
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 12) {
                    ForEach(contents, id: \.contentId) { content in
                        RecentlyWatchedPosterCard(content: content)
                            .onTapGesture {
                                navigateToContent(content)
                            }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }
    
    private func navigateToContent(_ content: RecentlyWatchedContent) {
        // Navigate to content detail with progress
        Navigation.pushToSwiftUiView(
            ContentDetailView(
                initialProgress: content.progress,
                contentId: content.contentId
            )
        )
    }
}

// MARK: - Recently Watched Poster Card
struct RecentlyWatchedPosterCard: View {
    let content: RecentlyWatchedContent
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Poster image with progress overlay
            ZStack(alignment: .bottom) {
                KFImage(URL(string: content.verticalPoster ?? content.horizontalPoster ?? ""))
                    .placeholder {
                        Rectangle()
                            .fill(Color.gray.opacity(0.3))
                    }
                    .resizable()
                    .aspectRatio(2/3, contentMode: .fill)
                    .frame(width: 120, height: 180)
                    .clipped()
                    .cornerRadius(8)
                
                // Progress bar
                if content.progress > 0 && content.totalDuration > 0 {
                    GeometryReader { geometry in
                        VStack {
                            Spacer()
                            ZStack(alignment: .leading) {
                                Rectangle()
                                    .fill(Color.black.opacity(0.5))
                                    .frame(height: 3)
                                
                                Rectangle()
                                    .fill(Color.red)
                                    .frame(width: geometry.size.width * CGFloat(content.progress) / CGFloat(content.totalDuration), height: 3)
                            }
                        }
                    }
                }
            }
            .frame(width: 120, height: 180)
            
            // Title
            Text(content.contentName)
                .font(.system(size: 12))
                .foregroundColor(.white)
                .lineLimit(1)
                .frame(width: 120, alignment: .leading)
            
            // Episode info if series
            if let episodeInfo = content.episodeInfo {
                Text(episodeInfo.episodeTitle)
                    .font(.system(size: 10))
                    .foregroundColor(.gray)
                    .lineLimit(1)
                    .frame(width: 120, alignment: .leading)
            }
        }
    }
}

// MARK: - View Model
class RecentlyWatchedGroupedViewModel: ObservableObject {
    @Published var groupedContent: [(genre: Genre, contents: [RecentlyWatchedContent])] = []
    @Published var isLoading = false
    private var allGenres: [Genre] = []
    private var recentlyWatchedVM: RecentlyWatchedViewModel?
    
    func loadRecentlyWatched(recentlyWatchedVM: RecentlyWatchedViewModel) {
        isLoading = true
        self.recentlyWatchedVM = recentlyWatchedVM
        
        // First fetch genres, then fetch recently watched content
        fetchGenres { [weak self] in
            self?.fetchRecentlyWatchedContent()
        }
    }
    
    private func fetchGenres(completion: @escaping () -> Void) {
        // Fetch genres from home page data
        var params: [Params: Any] = [
            .userId: SessionManager.shared.currentUser?.id ?? 0
        ]
        
        if let profileId = SessionManager.shared.currentProfile?.profileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .fetchHomePageData, params: params) { [weak self] (response: HomeModel) in
            guard let self = self else {
                completion()
                return
            }
            
            if let genres = response.genreContents {
                self.allGenres = genres
            } else {
                self.allGenres = []
            }
            completion()
        } callbackFailure: { [weak self] error in
            print("Failed to fetch genres: \(error)")
            self?.allGenres = []
            completion()
        }
    }
    
    private func fetchRecentlyWatchedContent() {
        // Use the existing RecentlyWatchedViewModel to fetch data
        recentlyWatchedVM?.fetchRecentlyWatchedFromAPI()
        
        // Wait a moment for the data to load, then group it
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
            self?.groupContentByGenre()
        }
    }
    
    private func groupContentByGenre() {
        guard let recentlyWatchedVM = recentlyWatchedVM else { return }
        let contents = recentlyWatchedVM.recentlyWatchedContents
        var grouped: [(genre: Genre, contents: [RecentlyWatchedContent])] = []
        
        // Group content by matching genres
        for genre in allGenres {
            let contentsInGenre = contents.filter { content in
                // Check if content's genres contain this genre
                if let contentGenres = content.genres {
                    return contentGenres.contains { $0.id == genre.id }
                }
                return false
            }
            
            if !contentsInGenre.isEmpty {
                // Sort by watched date (most recent first)
                let sortedContents = contentsInGenre.sorted { (c1, c2) in
                    c1.watchedDate > c2.watchedDate
                }
                grouped.append((genre: genre, contents: sortedContents))
            }
        }
        
        groupedContent = grouped
        isLoading = false
    }
}

