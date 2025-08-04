//
//  WatchlistViewModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 22/05/24.
//

import Foundation

class WatchlistViewModel : BaseViewModel {
    @Published var contents = [VugaContent]()
    @Published var contentType = ContentType.all
    @Published var isDataFetched = false

    
    func fetchWatchlist(isForRefresh: Bool = false){
        if isForRefresh {
            contents = []
            startLoading()
        }
        var params: [Params: Any] = [.userId : myUser?.id ?? 0, .start: contents.count, .limit: Limits.pagination,.type: contentType.rawValue]
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .fetchWatchList, params: params) { [weak self] (obj: ContentsModel) in
            self?.stopLoading()
            if isForRefresh {
                self?.contents.removeAll()
            }
            let newContents = obj.data ?? []
            print("WatchlistViewModel: Received \(newContents.count) items from API")
            print("WatchlistViewModel: Current contents count before append: \(self?.contents.count ?? 0)")
            self?.contents.append(contentsOf: newContents)
            print("WatchlistViewModel: Current contents count after append: \(self?.contents.count ?? 0)")
            self?.isDataFetched = true
        }
    }
    
    func removeFromWatchlist(content: VugaContent) {
        var params: [Params: Any] = [
            .appUserId: myUser?.id ?? 0,
            .contentId: content.id ?? 0
        ]
        
        if let profileId = myUser?.lastActiveProfileId, profileId > 0 {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .toggleWatchlist, params: params, callbackSuccess: { [weak self] (obj: UserModel) in
            if let data = obj.data {
                self?.myUser = data
                self?.contents.removeAll(where: { $0.id == content.id })
            }
        }, callbackFailure: { error in
            print("Failed to remove from watchlist: \(error)")
        })
    }
}

