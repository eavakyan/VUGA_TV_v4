//
//  ContentDetailViewModel.swift
//  Vuga
//
//

import Foundation
import CoreData

class ContentDetailViewModel : BaseViewModel {
    @Published var content: VugaContent?
    @Published var isStoryLineOn = true
    @Published var isStarCastOn = true
    @Published var isSourceSheetOn = false
    @Published var isBookmarked = false
    @Published var selectedSeason: Season? {
        didSet {
            // When season changes, check watchlist status for new episodes
            checkAllEpisodesWatchlistStatus()
        }
    }
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
    @Published var showDistributorSubscriptionRequired = false
    @Published var episodeWatchlistStatus: [Int: Bool] = [:] // Track watchlist status for each episode by ID
    
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
            
            // Load recently watched progress for this content after a small delay to ensure CoreData is ready
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
                self?.loadRecentlyWatchedProgress(contentId: contentId)
            }
            
            self?.isDataLoaded = true
            
            // Check watchlist status for all episodes if it's a TV series
            if obj.data?.type == .series {
                self?.checkAllEpisodesWatchlistStatus()
            }
        }, callbackFailure: { error in
            self.stopLoading()
            print("ContentDetailViewModel fetchContest error: \(error)")
            self.isDataLoaded = true
        })
    }
    
    func loadRecentlyWatchedProgress(contentId: Int) {
        // Safely fetch recently watched progress from local CoreData
        do {
            let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
            fetchRequest.predicate = NSPredicate(format: "contentID == %d", contentId)
            fetchRequest.sortDescriptors = [NSSortDescriptor(key: "date", ascending: false)]
            fetchRequest.fetchLimit = 1
            
            let recentlyWatched = try DataController.shared.context.fetch(fetchRequest)
            if let recent = recentlyWatched.first {
                self.progress = recent.progress
                print("ContentDetailViewModel: Loaded progress from recently watched: \(self.progress)")
            } else {
                print("ContentDetailViewModel: No recently watched data found for content \(contentId)")
                self.progress = 0.0
            }
        } catch {
            print("ContentDetailViewModel: Error loading recently watched progress: \(error)")
            self.progress = 0.0
        }
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
    
    // MARK: - Episode Watchlist Methods
    
    func checkEpisodeWatchlistStatus(episodeId: Int) {
        guard let user = myUser, let userId = user.id else { return }
        
        var params: [Params: Any] = [
            .appUserId: userId,
            .episodeId: episodeId
        ]
        
        if let profileId = user.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .checkEpisodeWatchlist, params: params) { [weak self] (response: UserModel) in
            if let data = response.data as? [String: Any],
               let isInWatchlist = data["is_in_watchlist"] as? Bool {
                DispatchQueue.main.async {
                    self?.episodeWatchlistStatus[episodeId] = isInWatchlist
                }
            }
        }
    }
    
    func toggleEpisodeWatchlist(episodeId: Int) {
        guard let user = myUser, let userId = user.id else { return }
        
        // Optimistically update UI
        let wasInWatchlist = episodeWatchlistStatus[episodeId] ?? false
        episodeWatchlistStatus[episodeId] = !wasInWatchlist
        
        var params: [Params: Any] = [
            .appUserId: userId,
            .episodeId: episodeId
        ]
        
        if let profileId = user.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .toggleEpisodeWatchlist, params: params) { [weak self] (response: UserModel) in
            // Success - watchlist was toggled
            print("Episode watchlist toggled successfully")
        }
    }
    
    func checkAllEpisodesWatchlistStatus() {
        // Check watchlist status for all episodes in the current season
        guard let episodes = selectedSeason?.episodes else { return }
        
        for episode in episodes {
            if let episodeId = episode.id {
                checkEpisodeWatchlistStatus(episodeId: episodeId)
            }
        }
    }
    
    func toogleBookmark(homeVm: HomeViewModel?){
        DispatchQueue.main.async { [weak self] in
            guard let self = self, let content = self.content else { return }
            
            let wasBookmarked = self.isBookmarked
            self.isBookmarked.toggle()
            
            // Capture user data on main thread
            let userId = self.myUser?.id ?? 0
            let profileId = self.myUser?.lastActiveProfileId
            let contentId = content.id ?? 0
            let contentTitle = content.title ?? ""
            
            var params: [Params: Any] = [
                .appUserId: userId,
                .contentId: contentId
            ]
            
            if let profileId = profileId, profileId > 0 {
                params[.profileId] = profileId
            }
            
            NetworkManager.callWebService(url: .toggleWatchlist, params: params, callbackSuccess: { [weak self] (obj: UserModel) in
                if obj.status == true {
                    // Success - the bookmark state was successfully toggled
                    print("Watchlist toggled successfully for content: \(contentTitle)")
                    
                    // DON'T update myUser here as it's @AppStorage and causes app-wide updates
                    // The bookmark state is already updated locally via self.isBookmarked
                    // HomeView will fetch fresh data when needed
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
