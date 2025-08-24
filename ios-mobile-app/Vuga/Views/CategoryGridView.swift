//
//  CategoryGridView.swift
//  Vuga
//
//  Grid view for displaying content from a specific category
//  Shows content in a 3-column grid with newest items first
//

import SwiftUI
import Kingfisher

struct CategoryGridView: View {
    @StateObject private var vm = CategoryGridViewModel()
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    let categoryId: Int
    let categoryName: String
    
    let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10)
    ]
    
    var body: some View {
        ZStack {
            Color("bgColor").ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                headerView
                
                if vm.isLoading && vm.contents.isEmpty {
                    // Loading state
                    loadingView
                } else if vm.contents.isEmpty {
                    // Empty state
                    emptyStateView
                } else {
                    // Content grid
                    ScrollView(.vertical, showsIndicators: false) {
                        LazyVGrid(columns: columns, spacing: 15) {
                            ForEach(vm.contents, id: \.id) { content in
                                CategoryGridItem(content: content)
                                    .onTapGesture {
                                        Navigation.pushToSwiftUiView(
                                            ContentDetailView(contentId: content.id ?? 0)
                                        )
                                    }
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 10)
                        .padding(.bottom, 100)
                        
                        // Load more indicator
                        if vm.isLoadingMore {
                            HStack {
                                Spacer()
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                Spacer()
                            }
                            .padding(.vertical, 20)
                        }
                    }
                    .onAppear {
                        // Load more when reaching the end
                        if !vm.contents.isEmpty {
                            vm.checkForMoreContent()
                        }
                    }
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            vm.loadContent(for: categoryId)
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
            
            Text(categoryName)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            Spacer()
            
            // Sort button
            Menu {
                Button("Newest First") {
                    vm.sortContent(by: .newest)
                }
                Button("Oldest First") {
                    vm.sortContent(by: .oldest)
                }
                Button("A-Z") {
                    vm.sortContent(by: .alphabetical)
                }
                Button("Z-A") {
                    vm.sortContent(by: .reverseAlphabetical)
                }
            } label: {
                HStack(spacing: 4) {
                    Image(systemName: "arrow.up.arrow.down")
                        .font(.system(size: 14))
                    Text("Sort")
                        .font(.system(size: 14))
                }
                .foregroundColor(.white.opacity(0.8))
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(Color.white.opacity(0.1))
                .cornerRadius(8)
            }
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
            Text("Check back later for new content in this category")
                .font(.system(size: 14))
                .foregroundColor(.gray.opacity(0.8))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            Spacer()
        }
    }
}

// MARK: - Grid Item View
struct CategoryGridItem: View {
    let content: VugaContent
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            // Poster image
            ZStack(alignment: .topTrailing) {
                KFImage(URL(string: content.verticalPoster ?? content.horizontalPoster ?? ""))
                    .placeholder {
                        Rectangle()
                            .fill(Color.gray.opacity(0.3))
                    }
                    .resizable()
                    .aspectRatio(2/3, contentMode: .fill)
                    .clipped()
                    .cornerRadius(8)
                
                // Type badge (Movie/Series)
                if content.type == .series {
                    Text("SERIES")
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 3)
                        .background(Color.blue.opacity(0.8))
                        .cornerRadius(4)
                        .padding(6)
                }
            }
            
            // Title
            Text(content.title ?? "")
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(.white)
                .lineLimit(2)
            
            // Year and rating
            HStack(spacing: 4) {
                if let year = content.releaseYear {
                    Text(String(year))
                        .font(.system(size: 9))
                        .foregroundColor(.gray)
                }
                
                if let rating = content.ratings, rating > 0 {
                    HStack(spacing: 2) {
                        Image(systemName: "star.fill")
                            .font(.system(size: 8))
                            .foregroundColor(.yellow)
                        Text(String(format: "%.1f", rating))
                            .font(.system(size: 9))
                            .foregroundColor(.gray)
                    }
                }
            }
        }
    }
}

// MARK: - View Model
class CategoryGridViewModel: BaseViewModel {
    @Published var contents: [VugaContent] = []
    @Published var isLoadingMore = false
    private var currentPage = 1
    private var canLoadMore = true
    private var categoryId: Int = 0
    
    enum SortOption {
        case newest
        case oldest
        case alphabetical
        case reverseAlphabetical
    }
    
    func loadContent(for categoryId: Int) {
        self.categoryId = categoryId
        currentPage = 1
        contents = []
        fetchContent()
    }
    
    func checkForMoreContent() {
        if !isLoadingMore && canLoadMore {
            loadMoreContent()
        }
    }
    
    func loadMoreContent() {
        guard !isLoadingMore && canLoadMore else { return }
        currentPage += 1
        isLoadingMore = true
        fetchContent(append: true)
    }
    
    func sortContent(by option: SortOption) {
        switch option {
        case .newest:
            contents.sort { ($0.releaseYear ?? 0) > ($1.releaseYear ?? 0) }
        case .oldest:
            contents.sort { ($0.releaseYear ?? 0) < ($1.releaseYear ?? 0) }
        case .alphabetical:
            contents.sort { ($0.title ?? "") < ($1.title ?? "") }
        case .reverseAlphabetical:
            contents.sort { ($0.title ?? "") > ($1.title ?? "") }
        }
    }
    
    private func fetchContent(append: Bool = false) {
        if !append {
            startLoading()
        }
        
        var params: [Params: Any] = [
            .genreId: categoryId,
            .start: (currentPage - 1) * 30,
            .limit: 30
        ]
        
        NetworkManager.callWebService(url: .fetchContentsByGenre, httpMethod: .post, params: params) { [weak self] (response: GenreContentResponse) in
            guard let self = self else { return }
            
            if append {
                self.isLoadingMore = false
            } else {
                self.stopLoading()
            }
            
            if let newContents = response.data {
                if append {
                    self.contents.append(contentsOf: newContents)
                } else {
                    self.contents = newContents
                }
                self.canLoadMore = newContents.count >= 30
                
                // Sort by newest first (default)
                if !append {
                    self.sortContent(by: .newest)
                }
            }
        } callbackFailure: { [weak self] error in
            print("Failed to fetch category content: \(error)")
            if append {
                self?.isLoadingMore = false
            } else {
                self?.stopLoading()
            }
        }
    }
}

