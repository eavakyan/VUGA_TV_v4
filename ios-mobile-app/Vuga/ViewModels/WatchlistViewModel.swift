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
    private var currentProfileId: Int? = nil

    
    func fetchWatchlist(isForRefresh: Bool = false){
        // Check if profile has changed and force refresh if needed
        let activeProfileId = myUser?.lastActiveProfileId
        if currentProfileId != activeProfileId {
            currentProfileId = activeProfileId
            contents = []
            isDataFetched = false
        }
        if isForRefresh {
            contents = []
            startLoading()
        }
        var params: [Params: Any] = [.userId : myUser?.id ?? 0, .start: contents.count, .limit: Limits.pagination,.type: contentType.rawValue]
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .fetchWatchList, params: params) { [weak self] (obj: ContentsModel) in
            guard let self = self else { return }
            self.stopLoading()
            
            // Verify profile hasn't changed during network call
            if self.currentProfileId != myUser?.lastActiveProfileId {
                print("WatchlistViewModel: Profile changed during network call, ignoring results")
                return
            }
            
            if isForRefresh {
                self.contents.removeAll()
            }
            let newContents = obj.data ?? []
            print("WatchlistViewModel: Received \(newContents.count) items for profile \(self.currentProfileId ?? 0)")
            print("WatchlistViewModel: Current contents count before append: \(self.contents.count)")
            self.contents.append(contentsOf: newContents)
            print("WatchlistViewModel: Current contents count after append: \(self.contents.count)")
            self.isDataFetched = true
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
    
    // Method to refresh watchlist when profile changes
    func refreshForProfileChange() {
        isDataFetched = false
        fetchWatchlist(isForRefresh: true)
    }
}

