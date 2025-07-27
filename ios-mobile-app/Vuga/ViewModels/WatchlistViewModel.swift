//
//  WatchlistViewModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 22/05/24.
//

import Foundation

class WatchlistViewModel : BaseViewModel {
    @Published var contents = [FlixyContent]()
    @Published var contentType = ContentType.all
    @Published var isDataFetched = false

    
    func fetchWatchlist(isForRefresh: Bool = false){
        if isForRefresh {
            contents = []
            startLoading()
        }
        let params: [Params: Any] = [.userId : myUser?.id ?? 0, .start: contents.count, .limit: Limits.pagination,.type: contentType.rawValue]
        
        NetworkManager.callWebService(url: .fetchWatchList, params: params) { [weak self] (obj: ContentsModel) in
            self?.stopLoading()
            if isForRefresh {
                self?.contents.removeAll()
            }
            self?.contents.append(contentsOf: obj.data ?? [])
            self?.isDataFetched = true
        }
    }
    
    func removeFromWatchlist(content: FlixyContent) {
        var watchlist = self.myUser?.watchlistIds ?? []
        watchlist.removeAll(where: { $0 == content.id ?? 0 })
        
        let params = [Params.watchlistContentIds: watchlist.map({ "\($0)" }).joined(separator: ",")]
        
        commonProfileEdit(params: params) { user in
            self.contents.removeAll(where: { $0.id == content.id })
        }
    }
}

