//
//  ContentGridView.swift
//  Vuga
//
//  Grid view for displaying filtered content with category grouping
//

import SwiftUI
import Kingfisher

struct ContentGridView: View {
    @StateObject private var viewModel = ContentGridViewModel()
    @AppStorage(SessionKeys.language) private var language = LocalizationService.shared.language
    @Environment(\.presentationMode) var presentationMode
    
    let filterType: ContentFilterType
    let filterValue: String?
    let navigationTitle: String
    
    // Grid layout configuration
    let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]
    
    // Computed property for sorted genre keys
    var sortedGenreKeys: [String] {
        return viewModel.groupedContents.keys.sorted()
    }
    
    // Helper function to create genre section
    @ViewBuilder
    func genreSection(for genre: String) -> some View {
        if let contents = viewModel.groupedContents[genre], !contents.isEmpty {
            VStack(alignment: .leading, spacing: 12) {
                // Section header
                HStack {
                    Text(genre)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    Text("\(contents.count) items")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                }
                .padding(.horizontal)
                
                // Content grid for this genre
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(contents, id: \.id) { content in
                        ContentGridItem(content: content)
                            .onTapGesture {
                                print("ContentGridView: Tapped content - id: \(content.id ?? -1), title: \(content.title ?? "Unknown")")
                                if let contentId = content.id, contentId > 0 {
                                    Navigation.pushToSwiftUiView(
                                        ContentDetailView(contentId: contentId)
                                    )
                                } else {
                                    print("ContentGridView: Invalid content ID: \(content.id ?? -1)")
                                }
                            }
                    }
                }
                .padding(.horizontal)
            }
        }
    }
    
    var body: some View {
        ZStack {
            Color("bgColor")
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Custom navigation header
                HStack {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Image(systemName: "chevron.left")
                            .foregroundColor(.white)
                            .font(.system(size: 20, weight: .medium))
                    }
                    
                    Text(navigationTitle)
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                    
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.vertical, 16)
                .background(Color("bgColor"))
                
                // Content grid
                ScrollView {
                    if viewModel.isLoading && viewModel.groupedContents.isEmpty {
                        // Loading state
                        VStack {
                            Spacer()
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: Color("baseColor")))
                                .scaleEffect(1.5)
                            Text("Loading...")
                                .foregroundColor(.gray)
                                .padding(.top, 10)
                            Spacer()
                        }
                        .frame(height: 400)
                    } else if viewModel.groupedContents.isEmpty {
                        // Empty state
                        VStack(spacing: 20) {
                            Image(systemName: "tv.slash")
                                .font(.system(size: 60))
                                .foregroundColor(.gray)
                            Text("No content available")
                                .font(.title2)
                                .foregroundColor(.white)
                            Text("Check back later for new content")
                                .font(.body)
                                .foregroundColor(.gray)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 100)
                    } else {
                        // Content sections grouped by genre/category
                        LazyVStack(alignment: .leading, spacing: 30) {
                            ForEach(sortedGenreKeys, id: \.self) { genre in
                                genreSection(for: genre)
                            }
                            
                            // Load more indicator
                            if viewModel.canLoadMore {
                                HStack {
                                    Spacer()
                                    ProgressView()
                                        .onAppear {
                                            viewModel.loadMoreContent()
                                        }
                                    Spacer()
                                }
                                .padding(.vertical, 20)
                            }
                        }
                        .padding(.top, 10)
                    }
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            viewModel.fetchContent(filterType: filterType, filterValue: filterValue)
        }
    }
}

// Grid item view
struct ContentGridItem: View {
    let content: VugaContent
    
    var body: some View {
        VStack(spacing: 0) {
            // Poster image
            KFImage(URL(string: content.verticalPoster ?? content.horizontalPoster ?? ""))
                .placeholder {
                    Rectangle()
                        .fill(Color.gray.opacity(0.3))
                        .overlay(
                            Image(systemName: "photo")
                                .foregroundColor(.gray)
                                .font(.system(size: 30))
                        )
                }
                .resizable()
                .aspectRatio(2/3, contentMode: .fill)
                .clipped()
                .cornerRadius(8)
            
            // Duration if available
            if let durationStr = content.duration, let duration = Int(durationStr), duration > 0 {
                let hours = duration / 3600
                let minutes = (duration % 3600) / 60
                let durationText = hours > 0 ? "\(hours) hr \(minutes) min" : "\(minutes) min"
                Text(durationText)
                    .font(.system(size: 11))
                    .foregroundColor(.gray)
                    .padding(.top, 4)
            }
        }
    }
}

// Filter type enum
enum ContentFilterType {
    case contentType(ContentType)
    case genre(Int)
    case category(Int)
    case watchlist
    case recentlyWatched
    case newReleases
    case all
}

// View Model
class ContentGridViewModel: ObservableObject {
    @Published var groupedContents: [String: [VugaContent]] = [:]
    @Published var isLoading = false
    @Published var canLoadMore = true
    
    private var allContents: [VugaContent] = []
    private var currentPage = 1
    private var filterType: ContentFilterType = .all
    private var filterValue: String?
    
    func fetchContent(filterType: ContentFilterType, filterValue: String?) {
        self.filterType = filterType
        self.filterValue = filterValue
        currentPage = 1
        allContents = []
        groupedContents = [:]
        loadContent()
    }
    
    func loadMoreContent() {
        guard !isLoading && canLoadMore else { return }
        currentPage += 1
        loadContent()
    }
    
    private func loadContent() {
        isLoading = true
        
        // Handle special cases first
        switch filterType {
        case .watchlist:
            fetchWatchlist()
            return
        case .recentlyWatched:
            fetchRecentlyWatched()
            return
        case .genre(let genreId):
            fetchContentsByGenre(genreId: genreId)
            return
        default:
            break
        }
        
        // For content type, new releases, and all - use fetchHomePageData
        var params: [Params: Any] = [:]
        
        // Add user ID if available
        if let userId = SessionManager.shared.currentUser?.id {
            params[.userId] = userId
        }
        
        // Fetch home page data which includes all content
        NetworkManager.callWebService(
            url: .fetchHomePageData,
            httpMethod: .post,
            params: params,
            callbackSuccess: { [weak self] (response: HomeModel) in
                DispatchQueue.main.async {
                    self?.handleHomePageDataForFiltering(response)
                }
            },
            callbackFailure: { [weak self] error in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    print("ContentGridView: Error loading content: \(error)")
                }
            }
        )
    }
    
    private func handleContentResponse(_ response: HomeModel) {
        isLoading = false
        
        var newContents: [VugaContent] = []
        
        // Extract contents from different response sections
        if let featured = response.featured {
            newContents.append(contentsOf: featured)
        }
        
        if let watchlist = response.watchlist {
            newContents.append(contentsOf: watchlist)
        }
        
        // Extract from top contents
        if let topContents = response.topContents {
            for topContent in topContents {
                if let content = topContent.content {
                    newContents.append(content)
                }
            }
        }
        
        // Extract from genre contents
        if let genres = response.genreContents {
            for genre in genres {
                if let genreContents = genre.contents {
                    newContents.append(contentsOf: genreContents)
                }
            }
        }
        
        // Update all contents
        if currentPage == 1 {
            allContents = newContents
        } else {
            allContents.append(contentsOf: newContents)
        }
        
        canLoadMore = newContents.count >= 20
        groupContentsByGenre()
    }
    
    private func fetchWatchlist() {
        isLoading = true
        
        guard let userId = SessionManager.shared.currentUser?.id else {
            isLoading = false
            return
        }
        
        let params: [Params: Any] = [
            .userId: userId,
            .start: (currentPage - 1) * 20,
            .limit: 20
        ]
        
        NetworkManager.callWebService(
            url: .fetchWatchList,
            httpMethod: .post,
            params: params,
            callbackSuccess: { [weak self] (response: WatchlistResponse) in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    if let contents = response.data?.contents {
                        if self?.currentPage == 1 {
                            self?.allContents = contents
                        } else {
                            self?.allContents.append(contentsOf: contents)
                        }
                        self?.canLoadMore = contents.count >= 20
                        self?.groupContentsByGenre()
                    }
                }
            },
            callbackFailure: { [weak self] error in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    print("Error loading watchlist: \(error)")
                }
            }
        )
    }
    
    private func fetchRecentlyWatched() {
        isLoading = true
        
        guard let profileId = SessionManager.shared.currentProfile?.profileId else {
            isLoading = false
            return
        }
        
        // First get the recently watched IDs
        let params: [Params: Any] = [
            .profileId: profileId,
            .start: (currentPage - 1) * 20,
            .limit: 20
        ]
        
        NetworkManager.callWebService(
            url: .getContinueWatching,
            httpMethod: .post,
            params: params,
            callbackSuccess: { [weak self] (response: RecentlyWatchedAPIResponse) in
                guard let self = self, let apiContents = response.data, !apiContents.isEmpty else {
                    DispatchQueue.main.async {
                        self?.isLoading = false
                    }
                    return
                }
                
                // Extract content IDs
                let contentIds = apiContents.map { $0.contentId }
                let contentIdsString = contentIds.map { String($0) }.joined(separator: ",")
                
                // Fetch full content details
                NetworkManager.callWebService(
                    url: .fetchContentsByIds,
                    httpMethod: .post,
                    params: [.contentId: contentIdsString],
                    callbackSuccess: { [weak self] (contentResponse: ContentsResponse) in
                        DispatchQueue.main.async {
                            self?.isLoading = false
                            if let contents = contentResponse.data {
                                if self?.currentPage == 1 {
                                    self?.allContents = contents
                                } else {
                                    self?.allContents.append(contentsOf: contents)
                                }
                                self?.canLoadMore = contents.count >= 20
                                self?.groupContentsByGenre()
                            }
                        }
                    },
                    callbackFailure: { [weak self] error in
                        DispatchQueue.main.async {
                            self?.isLoading = false
                            print("Error loading content details: \(error)")
                        }
                    }
                )
            },
            callbackFailure: { [weak self] error in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    print("Error loading recently watched: \(error)")
                }
            }
        )
    }
    
    private func groupContentsByGenre() {
        var grouped: [String: [VugaContent]] = [:]
        
        for content in allContents {
            // Get primary genre or category
            let primaryGenre = content.genres.first?.title ?? "Other"
            
            if grouped[primaryGenre] == nil {
                grouped[primaryGenre] = []
            }
            grouped[primaryGenre]?.append(content)
        }
        
        // Sort contents within each group by title
        for (genre, contents) in grouped {
            grouped[genre] = contents.sorted { ($0.title ?? "") < ($1.title ?? "") }
        }
        
        groupedContents = grouped
    }
    
    private func fetchContentsByGenre(genreId: Int) {
        isLoading = true
        
        let params: [Params: Any] = [
            .genreId: genreId,
            .start: (currentPage - 1) * 20,
            .limit: 20
        ]
        
        NetworkManager.callWebService(
            url: .fetchContentsByGenre,
            httpMethod: .post,
            params: params,
            callbackSuccess: { [weak self] (response: GenreContentResponse) in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    if let contents = response.data {
                        if self?.currentPage == 1 {
                            self?.allContents = contents
                        } else {
                            self?.allContents.append(contentsOf: contents)
                        }
                        self?.canLoadMore = contents.count >= 20
                        self?.groupContentsByGenre()
                    }
                }
            },
            callbackFailure: { [weak self] error in
                DispatchQueue.main.async {
                    self?.isLoading = false
                    print("ContentGridView: Error loading genre content: \(error)")
                }
            }
        )
    }
    
    private func handleHomePageDataForFiltering(_ response: HomeModel) {
        isLoading = false
        
        var newContents: [VugaContent] = []
        
        // Collect all content from the home page response
        if let featured = response.featured {
            print("ContentGridView: Adding \(featured.count) featured items")
            for content in featured {
                print("  - Featured: id=\(content.id ?? -1), title=\(content.title ?? "Unknown")")
            }
            newContents.append(contentsOf: featured)
        }
        
        // Add content from genre sections
        if let genres = response.genreContents {
            for genre in genres {
                if let genreContents = genre.contents {
                    print("ContentGridView: Adding \(genreContents.count) items from genre: \(genre.title ?? "Unknown")")
                    for content in genreContents {
                        print("  - Genre content: id=\(content.id ?? -1), title=\(content.title ?? "Unknown")")
                    }
                    newContents.append(contentsOf: genreContents)
                }
            }
        }
        
        // Filter based on the filter type
        switch filterType {
        case .contentType(let type):
            // Filter by content type (movie or series)
            allContents = newContents.filter { content in
                if type == .movie {
                    return content.type == .movie
                } else {
                    return content.type == .series
                }
            }
        case .newReleases:
            // Filter for new releases (released within last 30 days)
            let currentYear = Calendar.current.component(.year, from: Date())
            allContents = newContents.filter { content in
                // Check if release year is current year
                if let releaseYear = content.releaseYear {
                    return releaseYear >= currentYear
                }
                return false
            }
        default:
            // Show all content
            allContents = newContents
        }
        
        // Remove duplicates based on content ID
        var seen = Set<Int>()
        allContents = allContents.filter { content in
            guard let id = content.id else { return false }
            if seen.contains(id) {
                return false
            }
            seen.insert(id)
            return true
        }
        
        canLoadMore = false // Home page data doesn't support pagination
        groupContentsByGenre()
    }
}

// Response models
struct WatchlistResponse: Codable {
    let status: Bool?
    let message: String?
    let data: WatchlistData?
}

struct WatchlistData: Codable {
    let contents: [VugaContent]?
}

struct ContentsResponse: Codable {
    let status: Bool?
    let message: String?
    let data: [VugaContent]?
}

struct GenreContentResponse: Codable {
    let status: Bool?
    let message: String?
    let data: [VugaContent]?
}