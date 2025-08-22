//
//  WatchlistViewModel.swift
//  Vuga
//
//

import Foundation

class WatchlistViewModel : BaseViewModel {
    @Published var contents = [VugaContent]()
    @Published var unifiedItems = [UnifiedWatchlistItem]()
    @Published var contentType = ContentType.all
    @Published var isDataFetched = false
    private var currentProfileId: Int? = nil
    @Published var useUnifiedWatchlist = true
    @Published var hasMoreData = true
    private var isFetchingMore = false

    
    func fetchWatchlist(isForRefresh: Bool = false){
        // Prevent multiple simultaneous fetches
        if !isForRefresh && isFetchingMore {
            return
        }
        
        // Don't fetch more if we have no more data
        if !isForRefresh && !hasMoreData {
            return
        }
        
        // Check if profile has changed and force refresh if needed
        let activeProfileId = myUser?.lastActiveProfileId
        if currentProfileId != activeProfileId {
            currentProfileId = activeProfileId
            contents = []
            unifiedItems = []
            isDataFetched = false
            hasMoreData = true
        }
        if isForRefresh {
            contents = []
            unifiedItems = []
            hasMoreData = true
            startLoading()
        } else {
            isFetchingMore = true
        }
        
        if useUnifiedWatchlist {
            fetchUnifiedWatchlist(isForRefresh: isForRefresh)
        } else {
            fetchLegacyWatchlist(isForRefresh: isForRefresh)
        }
    }
    
    private func fetchUnifiedWatchlist(isForRefresh: Bool) {
        var params: [Params: Any] = [
            .userId: myUser?.id ?? 0,
            .start: unifiedItems.count,
            .limit: Limits.pagination
        ]
        
        if contentType != .all {
            // Map ContentType to API types: 1=movies, 2=series, 3=episodes (for "All", don't send type)
            if contentType == .movie {
                params[.type] = 1
            } else if contentType == .series {
                params[.type] = 2
            }
        }
        
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .fetchUnifiedWatchlist, params: params) { [weak self] (obj: UnifiedWatchlistResponse) in
            guard let self = self else { return }
            self.stopLoading()
            
            // Verify profile hasn't changed during network call
            if self.currentProfileId != myUser?.lastActiveProfileId {
                print("WatchlistViewModel: Profile changed during network call, ignoring results")
                return
            }
            
            if isForRefresh {
                self.unifiedItems.removeAll()
            }
            
            if let newItems = obj.data {
                print("WatchlistViewModel: Received \(newItems.count) unified items for profile \(self.currentProfileId ?? 0)")
                self.unifiedItems.append(contentsOf: newItems)
                self.isDataFetched = true
                
                // Check if we have more data
                if newItems.isEmpty {
                    self.hasMoreData = false
                }
            } else {
                // Fallback to legacy if no data
                print("No data from unified watchlist, falling back to legacy")
                self.useUnifiedWatchlist = false
                self.hasMoreData = true // Reset for legacy
                self.fetchLegacyWatchlist(isForRefresh: isForRefresh)
            }
            
            self.isFetchingMore = false
        }
    }
    
    private func fetchLegacyWatchlist(isForRefresh: Bool) {
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
            
            // Check if we have more data
            if newContents.count < Limits.pagination {
                self.hasMoreData = false
            }
            
            self.isFetchingMore = false
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
                self?.unifiedItems.removeAll(where: { $0.contentId == content.id && $0.itemType == "content" })
            }
        }, callbackFailure: { error in
            print("Failed to remove from watchlist: \(error)")
        })
    }
    
    func removeContentFromWatchlist(contentId: Int) {
        var params: [Params: Any] = [
            .appUserId: myUser?.id ?? 0,
            .contentId: contentId
        ]
        
        if let profileId = myUser?.lastActiveProfileId, profileId > 0 {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .toggleWatchlist, params: params, callbackSuccess: { [weak self] (obj: UserModel) in
            if let data = obj.data {
                self?.myUser = data
                self?.contents.removeAll(where: { $0.id == contentId })
                self?.unifiedItems.removeAll(where: { $0.contentId == contentId && $0.itemType == "content" })
            }
        }, callbackFailure: { error in
            print("Failed to remove from watchlist: \(error)")
        })
    }
    
    func removeEpisodeFromWatchlist(item: UnifiedWatchlistItem) {
        guard let episodeId = item.episodeId else { return }
        
        var params: [Params: Any] = [
            .appUserId: myUser?.id ?? 0,
            .episodeId: episodeId
        ]
        
        if let profileId = myUser?.lastActiveProfileId, profileId > 0 {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .toggleEpisodeWatchlist, params: params) { [weak self] (obj: UserModel) in
            self?.unifiedItems.removeAll(where: { $0.episodeId == episodeId && $0.itemType == "episode" })
            print("Episode removed from watchlist successfully")
        }
    }
    
    // Method to refresh watchlist when profile changes
    func refreshForProfileChange() {
        isDataFetched = false
        fetchWatchlist(isForRefresh: true)
    }
}

