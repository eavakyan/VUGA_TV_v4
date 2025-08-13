//
//  ContentGroupedView.swift
//  Vuga
//
//  View for displaying content grouped by categories in horizontal rows
//  Used for TV Shows, Movies, and Distributor content
//

import SwiftUI
import Kingfisher

struct ContentGroupedView: View {
    @StateObject private var vm = ContentGroupedViewModel()
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    let filterType: ContentFilterType
    let title: String
    
    enum ContentFilterType: Equatable {
        case movies
        case tvShows
        case newReleases
        case distributor(Int, String) // distributorId, distributorName
    }
    
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
                            // New Releases section (if applicable)
                            if !vm.newReleases.isEmpty {
                                categoryRow(
                                    title: "New Releases",
                                    contents: vm.newReleases
                                )
                            }
                            
                            // Content grouped by category
                            ForEach(vm.groupedContent, id: \.category.id) { group in
                                if !group.contents.isEmpty {
                                    categoryRow(
                                        title: group.category.title ?? "Category",
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
            vm.loadContent(for: filterType)
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
            
            Text(title)
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
            Image(systemName: "tv.slash")
                .font(.system(size: 50))
                .foregroundColor(.gray)
            Text("No content available")
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(.gray)
            Spacer()
        }
    }
    
    private func categoryRow(title: String, contents: [VugaContent]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            // Category title
            Text(title)
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)
                .padding(.horizontal, 16)
            
            // Horizontal scroll of content
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 12) {
                    ForEach(contents.prefix(20), id: \.id) { content in
                        ContentPosterCard(content: content)
                            .onTapGesture {
                                Navigation.pushToSwiftUiView(
                                    ContentDetailView(contentId: content.id ?? 0)
                                )
                            }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }
}

// MARK: - Content Poster Card
struct ContentPosterCard: View {
    let content: VugaContent
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Poster image
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
            
            // Title
            Text(content.title ?? "")
                .font(.system(size: 12))
                .foregroundColor(.white)
                .lineLimit(1)
                .frame(width: 120, alignment: .leading)
            
            // Year
            if let year = content.releaseYear {
                Text(String(year))
                    .font(.system(size: 10))
                    .foregroundColor(.gray)
            }
        }
    }
}

// MARK: - View Model
class ContentGroupedViewModel: BaseViewModel {
    @Published var groupedContent: [(category: Genre, contents: [VugaContent])] = []
    @Published var newReleases: [VugaContent] = []
    private var allContent: [VugaContent] = []
    private var categories: [Genre] = []
    private var filterType: ContentGroupedView.ContentFilterType = .movies
    
    func loadContent(for filterType: ContentGroupedView.ContentFilterType) {
        startLoading()
        self.filterType = filterType
        
        // First, fetch all categories
        fetchCategories { [weak self] in
            guard let self = self else { return }
            
            // Then fetch content based on filter type
            switch filterType {
            case .movies:
                self.fetchMovies()
            case .tvShows:
                self.fetchTVShows()
            case .newReleases:
                self.fetchNewReleases()
            case .distributor(let id, _):
                self.fetchDistributorContent(distributorId: id)
            }
        }
    }
    
    private func fetchCategories(completion: @escaping () -> Void) {
        // Fetch from home page data which includes genres
        var params: [Params: Any] = [
            .userId: myUser?.id ?? 0
        ]
        
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .fetchHomePageData, params: params) { [weak self] (response: HomeModel) in
            guard let self = self else { 
                completion()
                return 
            }
            
            if let genres = response.genreContents {
                self.categories = genres
            } else {
                self.categories = []
            }
            completion()
        } callbackFailure: { [weak self] error in
            print("Failed to fetch categories: \(error)")
            self?.categories = []
            completion()
        }
    }
    
    private func fetchMovies() {
        // Use fetchHomePageData to get all movies grouped by genre
        var params: [Params: Any] = [
            .userId: myUser?.id ?? 0
        ]
        
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        // Fetch all genres with their content
        NetworkManager.callWebService(url: .fetchHomePageData, params: params) { [weak self] (response: HomeModel) in
            guard let self = self else { return }
            
            // Group movies by genre directly (similar to newReleases)
            var genreGroups: [(category: Genre, contents: [VugaContent])] = []
            
            if let genreContents = response.genreContents {
                for genre in genreContents {
                    if let contents = genre.contents {
                        let moviesInGenre = contents.filter { $0.type == .movie }
                        
                        if !moviesInGenre.isEmpty {
                            // Sort by release year, newest first
                            let sortedContents = moviesInGenre.sorted { (c1, c2) in
                                (c1.releaseYear ?? 0) > (c2.releaseYear ?? 0)
                            }
                            genreGroups.append((category: genre, contents: sortedContents))
                        }
                    }
                }
            }
            
            // For movies, directly set the grouped content
            self.groupedContent = genreGroups
            self.newReleases = [] // Don't show separate new releases section
            self.stopLoading()
        } callbackFailure: { [weak self] error in
            print("Failed to fetch movies: \(error)")
            self?.stopLoading()
        }
    }
    
    private func fetchNewReleases() {
        // Use fetchHomePageData to get all content and filter for new releases
        var params: [Params: Any] = [
            .userId: myUser?.id ?? 0
        ]
        
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        // Fetch all genres with their content
        NetworkManager.callWebService(url: .fetchHomePageData, params: params) { [weak self] (response: HomeModel) in
            guard let self = self else { return }
            
            // Process genre contents to extract new releases (current year and last year)
            let currentYear = Calendar.current.component(.year, from: Date())
            
            // Instead of processing and losing genre info, group by genre directly
            var genreGroups: [(category: Genre, contents: [VugaContent])] = []
            
            if let genreContents = response.genreContents {
                for genre in genreContents {
                    if let contents = genre.contents {
                        let newReleasesInGenre = contents.filter { content in
                            if let year = content.releaseYear {
                                // Include current year and last year as "new releases"
                                return year >= currentYear - 1
                            }
                            return false
                        }
                        
                        if !newReleasesInGenre.isEmpty {
                            // Sort by release year, newest first
                            let sortedContents = newReleasesInGenre.sorted { (c1, c2) in
                                (c1.releaseYear ?? 0) > (c2.releaseYear ?? 0)
                            }
                            genreGroups.append((category: genre, contents: sortedContents))
                        }
                    }
                }
            }
            
            // For new releases, directly set the grouped content
            self.groupedContent = genreGroups
            self.newReleases = [] // Don't show separate new releases section
            self.stopLoading()
        } callbackFailure: { [weak self] error in
            print("Failed to fetch new releases: \(error)")
            self?.stopLoading()
        }
    }
    
    private func fetchTVShows() {
        // Use fetchHomePageData to get all TV shows grouped by genre
        var params: [Params: Any] = [
            .userId: myUser?.id ?? 0
        ]
        
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        // Fetch all genres with their content
        NetworkManager.callWebService(url: .fetchHomePageData, params: params) { [weak self] (response: HomeModel) in
            guard let self = self else { return }
            
            // Group TV shows by genre directly (similar to newReleases)
            var genreGroups: [(category: Genre, contents: [VugaContent])] = []
            
            if let genreContents = response.genreContents {
                for genre in genreContents {
                    if let contents = genre.contents {
                        let tvShowsInGenre = contents.filter { $0.type == .series }
                        
                        if !tvShowsInGenre.isEmpty {
                            // Sort by release year, newest first
                            let sortedContents = tvShowsInGenre.sorted { (c1, c2) in
                                (c1.releaseYear ?? 0) > (c2.releaseYear ?? 0)
                            }
                            genreGroups.append((category: genre, contents: sortedContents))
                        }
                    }
                }
            }
            
            // For TV shows, directly set the grouped content
            self.groupedContent = genreGroups
            self.newReleases = [] // Don't show separate new releases section
            self.stopLoading()
        } callbackFailure: { [weak self] error in
            print("Failed to fetch TV shows: \(error)")
            self?.stopLoading()
        }
    }
    
    private func fetchDistributorContent(distributorId: Int) {
        // For now, fetch all content from home and filter by distributor
        // TODO: Implement proper distributor filtering when API supports it
        var params: [Params: Any] = [
            .userId: myUser?.id ?? 0
        ]
        
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .fetchHomePageData, params: params) { [weak self] (response: HomeModel) in
            guard let self = self else { return }
            
            // Process all content from genres
            // TODO: Filter by distributor when API supports it
            var allContent: [VugaContent] = []
            if let genreContents = response.genreContents {
                for genre in genreContents {
                    if let contents = genre.contents {
                        allContent.append(contentsOf: contents)
                    }
                }
            }
            
            self.processContent(allContent)
            self.stopLoading()
        } callbackFailure: { [weak self] error in
            print("Failed to fetch distributor content: \(error)")
            self?.stopLoading()
        }
    }
    
    private func processContent(_ contents: [VugaContent]) {
        allContent = contents
        
        // Only show new releases section for movies/tvShows filters
        // For newReleases filter, all content is already new releases
        if filterType == .movies || filterType == .tvShows {
            // Filter new releases (current year and last year)
            let currentYear = Calendar.current.component(.year, from: Date())
            newReleases = contents.filter { content in
                if let year = content.releaseYear {
                    // Include current year and last year as "new releases"
                    return year >= currentYear - 1
                }
                return false
            }
        } else {
            // For newReleases filter, don't show separate new releases section
            newReleases = []
        }
        
        // Group content by category
        var grouped: [(category: Genre, contents: [VugaContent])] = []
        
        for category in categories {
            let categoryContents = contents.filter { content in
                // Check if content belongs to this category
                let contentGenres = content.genres
                return contentGenres.contains { $0.id == category.id }
            }
            
            if !categoryContents.isEmpty {
                // Sort by release year, newest first
                let sortedContents = categoryContents.sorted { (c1, c2) in
                    (c1.releaseYear ?? 0) > (c2.releaseYear ?? 0)
                }
                grouped.append((category: category, contents: sortedContents))
            }
        }
        
        groupedContent = grouped
    }
}

