//
//  SearchViewModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 23/05/24.
//

import SwiftUI

class SearchViewModel : BaseViewModel {
    @Published var contents = [VugaContent]()
    @Published var contentType = ContentType.all
    @Published var selectedLang = ContentLanguage(id: 0, title: .all, createdAt: "", updatedAt: "")
    @Published var selectedGenre = Genre(id: 0, title: "", createdAt: "", updatedAt: "", contents: [])
    @Published var keyword = ""
    @Published var isLanguageSheet = false
    @Published var isGenreSheet = false
    @Published var isDataFetched = false
    
    func searchContent(isForRefresh: Bool = true){
        NetworkManager.cancelAllRequests()
        if isForRefresh {
            contents.removeAll()
        }
        if contents.isEmpty {
            startLoading()
        }
        
        // Calculate page number from current content count
        let currentPage = (contents.count / Limits.pagination) + 1
        
        var params: [Params: Any] = [
            .search: keyword,  // Using 'search' parameter as expected by API
            .page: currentPage,
            .perPage: Limits.pagination
        ]
        
        // Add search type for cast searches
        if contentType == .cast {
            params[.searchType] = "cast"
        } else {
            params[.searchType] = "title"
        }
        
        // Add user and profile IDs if available
        if let userId = SessionManager.shared.currentUser?.id {
            params[.appUserId] = userId
        }
        
        if let profileId = SessionManager.shared.currentProfile?.profileId {
            params[.profileId] = profileId
        }
        
        // Only send type parameter for movie/series filters (not for all or cast)
        if contentType == .movie || contentType == .series {
            params[.type] = contentType.rawValue
        }
        
        if selectedLang.id != 0 {
            params[.languageId] = selectedLang.id ?? 0
        }
        
        if selectedGenre.id != 0 {
            params[.genreId] = selectedGenre.id ?? 0
        }
        
        NetworkManager.callWebService(url: .searchContent, params: params) { (obj: ContentsModel) in
            self.stopLoading()
            if let data = obj.data {
                DispatchQueue.main.async { [weak self] in
                    self?.contents.append(contentsOf: data)
                }
            }
            self.isDataFetched = true
        }
    }
}

