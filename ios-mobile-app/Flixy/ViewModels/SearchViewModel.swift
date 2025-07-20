//
//  SearchViewModel.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 23/05/24.
//

import SwiftUI

class SearchViewModel : BaseViewModel {
    @Published var contents = [FlixyContent]()
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
        var params: [Params: Any] = [.start : contents.count,
                                     .limit: Limits.pagination,
                                     .keyword: keyword]
        
        if contentType != .all {
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

