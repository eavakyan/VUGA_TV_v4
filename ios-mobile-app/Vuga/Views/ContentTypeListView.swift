//
//  ContentTypeListView.swift
//  Vuga
//
//  View for displaying content filtered by type (Movies or TV Shows)
//

import SwiftUI

struct ContentTypeListView: View {
    let contentType: ContentType
    
    var body: some View {
        Group {
            switch contentType {
            case .movie:
                MoviesListView()
            case .series:
                TVShowsListView()
            default:
                MoviesListView()
            }
        }
        .hideNavigationbar()
    }
}

// MARK: - View Model
class ContentTypeViewModel: BaseViewModel {
    @Published var contents: [VugaContent] = []
    private var currentPage = 1
    private var contentType: ContentType = .movie
    private var canLoadMore = true
    
    func fetchContents(for type: ContentType) {
        self.contentType = type
        currentPage = 1
        contents = []
        loadContent()
    }
    
    func loadMoreContent() {
        guard !isLoading && canLoadMore else { return }
        currentPage += 1
        loadContent()
    }
    
    private func loadContent() {
        startLoading()
        
        let params: [Params: Any] = [
            .page: currentPage,
            .limit: 20,
            .contentType: contentType.rawValue,
            .userId: myUser?.id ?? 0
        ]
        
        NetworkManager.callWebService(url: .searchContent, params: params) { [weak self] (response: ContentResponse) in
            guard let self = self else { return }
            self.stopLoading()
            
            if let newContents = response.data {
                if self.currentPage == 1 {
                    self.contents = newContents
                } else {
                    self.contents.append(contentsOf: newContents)
                }
                self.canLoadMore = newContents.count >= 20
            }
        } callbackFailure: { [weak self] error in
            self?.stopLoading()
            print("Error fetching content by type: \(error)")
        }
    }
}

// MARK: - Content Response Model
struct ContentResponse: Codable {
    let status: Bool?
    let message: String?
    let data: [VugaContent]?
}