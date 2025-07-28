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
        let params: [Params: Any] = [.userId : myUser?.id ?? 0,
                                     .contentId: contentId]
        print("ContentDetailViewModel fetchContest - contentId: \(contentId), userId: \(myUser?.id ?? 0)")
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

            self?.isBookmarked = self?.myUser?.checkIsAddedToWatchList(contentId: self?.content?.id ?? 0) ?? false
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
            self?.isBookmarked.toggle()
            
            var watchlist = self?.myUser?.watchlistIds ?? []
            
            if self?.isBookmarked == true {
                watchlist.append(self?.content?.id ?? 0)
                if let content = self?.content {
                    homeVm?.wishlists.append(content)
                }
            } else {
                watchlist.removeAll(where: { $0 == self?.content?.id ?? 0 })
                if let content = self?.content {
                    homeVm?.wishlists.removeAll(where: {$0.id == content.id})
                }
            }
            
            let params = [Params.watchlistContentIds: watchlist.map({ "\($0)" }).joined(separator: ",")]
            
            self?.commonProfileEdit(params: params)
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
}
