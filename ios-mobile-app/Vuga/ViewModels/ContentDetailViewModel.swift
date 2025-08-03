//
//  ContentDetailViewModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 21/05/24.
//

import Foundation

class ContentDetailViewModel : BaseViewModel {
    @Published var content: FlixyContent?
    @Published var isStoryLineOn = true
    @Published var isStarCastOn = true
    @Published var isSourceSheetOn = false
    @Published var isBookmarked = false
    @Published var selectedSeason: Season?
    @Published var sources = [Source]()
    @Published var selectedSource : Source?
    @Published var pickedSource: Source?
    @Published var selectedEpisode : Episode?
    @Published var isShow = true
    @Published var isShowPremiumDialog = false
    @Published var isShowAdDialog = false
    @Published var isShowDownloadAdDialog = false
    @Published var isDataLoaded = false
    @Published var selectedRecentlyViewed : RecentlyWatched?
    @Published var progress = 0.0
    @Published var seasonNumber = 0
    @Published var isShowAd = true
    
    func fetchContest(contentId: Int) {
        var params: [Params: Any] = [.userId : myUser?.id ?? 0,
                                     .contentId: contentId]
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        print("ContentDetailViewModel fetchContest - contentId: \(contentId), userId: \(myUser?.id ?? 0), profileId: \(myUser?.lastActiveProfileId ?? 0)")
        startLoading()
        NetworkManager.callWebService(url: .fetchContentDetails, params: params, callbackSuccess: { [weak self] (obj: ContentModel) in
            self?.stopLoading()
            print("ContentDetail Response:")
            print("Status: \(obj.status ?? false)")
            print("Message: \(obj.message ?? "")")
            print("Content title: \(obj.data?.title ?? "nil")")
            print("Content id: \(obj.data?.id ?? 0)")
            
            self?.content = obj.data
            self?.selectedSeason = self?.content?.seasons?.first
            if let selectedSeason = self?.selectedSeason {
                self?.seasonNumber = (self?.content?.seasons?.firstIndex(where: {$0.id == selectedSeason.id}) ?? 0) + 1 // 0
                print(self?.seasonNumber ?? 0)
            }

            // Use the server's isWatchlist value if available, otherwise fall back to local check
            self?.isBookmarked = obj.data?.isWatchlist ?? self?.myUser?.checkIsAddedToWatchList(contentId: self?.content?.id ?? 0) ?? false
            self?.isDataLoaded = true
        }, callbackFailure: { error in
            self.stopLoading()
            print("ContentDetailViewModel fetchContest error: \(error)")
            self.isDataLoaded = true
        })
    }
    
    func increaseContentView(contentId: Int){
        let params: [Params: Any] = [.contentId: contentId]
        startLoading()
        NetworkManager.callWebService(url: .increaseContentView,params: params){ (obj: IncreaseContentViewModel) in
            self.stopLoading()
            print(obj.status!)
        }
    }
    
    func increaseContentShare(contentId: Int){
        let params: [Params: Any] = [.contentId: contentId]
        startLoading()
        NetworkManager.callWebService(url: .increaseContentShare,params: params){ (obj: IncreaseContentViewModel) in
            self.stopLoading()
            print(obj.status!)
        }
    }
    
    func increaseEpisodeView(episodeId: Int){
        let params: [Params: Any] = [.episodeId: episodeId]
        startLoading()
        NetworkManager.callWebService(url: .increaseEpisodeView,params: params){ (obj: IncreaseEpisodeViewsModel) in
            self.stopLoading()
            print(obj.status!)
        }
    }
    
    func toogleBookmark(homeVm: HomeViewModel?){
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            
            let wasBookmarked = self.isBookmarked
            self.isBookmarked.toggle()
            
            var params: [Params: Any] = [
                .appUserId: self.myUser?.id ?? 0,
                .contentId: self.content?.id ?? 0
            ]
            
            if let profileId = self.myUser?.lastActiveProfileId, profileId > 0 {
                params[.profileId] = profileId
            }
            
            NetworkManager.callWebService(url: .toggleWatchlist, params: params, callbackSuccess: { [weak self] (obj: UserModel) in
                if let data = obj.data {
                    self?.myUser = data
                    
                    // Update home view model's wishlist
                    if self?.isBookmarked == true {
                        if let content = self?.content {
                            homeVm?.wishlists.append(content)
                        }
                    } else {
                        if let content = self?.content {
                            homeVm?.wishlists.removeAll(where: {$0.id == content.id})
                        }
                    }
                }
            }, callbackFailure: { [weak self] error in
                // If toggle failed, revert the UI state
                print("Failed to toggle watchlist: \(error)")
                DispatchQueue.main.async {
                    self?.isBookmarked = wasBookmarked
                }
            })
        }
    }
}

extension ContentDetailViewModel {
    func toggleStoryLine(){
        DispatchQueue.main.async { [weak self] in
            self?.isStoryLineOn.toggle()
        }
    }
    
    func toggleCastOn(){
        DispatchQueue.main.async { [weak self] in
            self?.isStarCastOn.toggle()
        }
    }
    
    func closeSourceSheet(){
        DispatchQueue.main.async { [weak self] in
            self?.isSourceSheetOn = false
            self?.sources = []
        }
    }
    
    func selectSeason(season: Season) {
        DispatchQueue.main.async { [weak self] in
            self?.selectedSeason = season
        }
    }
    
    func selectSources(data: [Source]) {
        DispatchQueue.main.async { [weak self] in
            self?.isSourceSheetOn.toggle()
            self?.sources = data
        }
    }
    
    func playSource(_ source: Source) {
        DispatchQueue.main.async { [weak self] in
            self?.selectedSource = source
        }
    }
    
    func submitRating(rating: Double) {
        guard let content = content, let userId = myUser?.id else { return }
        
        var params: [Params: Any] = [
            .appUserId: userId,
            .contentId: content.id ?? 0,
            .rating: rating
        ]
        
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .rateContent, params: params, callbackSuccess: { [weak self] (obj: StatusAndMessageModel) in
            if obj.status == true {
                // Refresh content details to get updated ratings
                if let contentId = content.id {
                    self?.fetchContest(contentId: contentId)
                }
            }
        }) { error in
            print("Failed to submit rating: \(error)")
        }
    }
    
    func submitEpisodeRating(episode: Episode, rating: Double) {
        guard let userId = myUser?.id else { return }
        
        var params: [Params: Any] = [
            .appUserId: userId,
            .episodeId: episode.id ?? 0,
            .rating: rating
        ]
        
        if let profileId = myUser?.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .rateEpisode, params: params, callbackSuccess: { [weak self] (obj: StatusAndMessageModel) in
            if obj.status == true {
                // Refresh content details to get updated episode ratings
                if let contentId = self?.content?.id {
                    self?.fetchContest(contentId: contentId)
                }
            }
        }) { error in
            print("Failed to submit episode rating: \(error)")
        }
    }
}
