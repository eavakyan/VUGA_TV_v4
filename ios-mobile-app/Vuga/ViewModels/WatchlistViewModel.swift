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
        var params: [Params: Any] = [.userId : myUser?.id ?? 0, .start: contents.count, .limit: Limits.pagination,.type: contentType.rawValue]
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
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

