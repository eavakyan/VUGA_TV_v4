//
//  HomeViewModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 15/05/24.
//

import SwiftUI
import Combine

class HomeViewModel : BaseViewModel {
    @Published var featured = [VugaContent]()
    @Published var topContents = [TopContent]()
    @Published var wishlists = [VugaContent]()
    @Published var genres = [Genre]()
    @Published var newReleases = [VugaContent]()
    @Published var selectedImageIndex = 0
    @Published var selectedRecentlyWatched: RecentlyWatched?
    @Published var deleteSelectedRecentlyWatched: RecentlyWatched?
    @Published var isDeleteRecentlyWatched = false
    @Published var isForRefresh = false
    private var cancellable: AnyCancellable?
    
    override init() {
        super.init()
        fetchData()
    }
    
    deinit {
        cancellable?.cancel()
    }
    
     func fetchData(){
         if !isForRefresh {
             startLoading()
         }
        // Match Android behavior - only send user_id for home page data
        var params: [Params: Any] = [.userId : myUser?.id ?? 0]
        
        // Log profile info for debugging but don't send profile_id
        if let profile = SessionManager.shared.currentProfile {
            print("HomeViewModel - Current profile: ID=\(profile.profileId), name=\(profile.name), isKids=\(profile.isKids), isKidsProfile=\(profile.isKidsProfile ?? false)")
        }
        NetworkManager.callWebService(url: .fetchHomePageData, params: params, callbackSuccess: { [weak self] (obj: HomeModel) in
            guard let self = self else { return }
            
            self.stopLoading()
            
            print("HomeModel Response:")
            print("Status: \(obj.status ?? false)")
            print("Message: \(obj.message ?? "")")
            print("Featured count: \(obj.featured?.count ?? 0)")
            print("Watchlist count: \(obj.watchlist?.count ?? 0)")
            print("GenreContents count: \(obj.genreContents?.count ?? 0)")
            print("TopContents count: \(obj.topContents?.count ?? 0)")
            
            self.featured = obj.featured ?? []
            
//            self.topContents = obj.topContents ?? []
<<<<<<< HEAD
            if self.featured.isNotEmpty {
                self.cancellable = Timer.publish(every: TimeInterval(Limits.featureSecond), on: .main, in: .common)
                    .autoconnect()
                    .sink { [weak self] _ in
                        DispatchQueue.main.async { [weak self] in
                            guard let self = self, self.featured.isNotEmpty else { return }
                            withAnimation(.easeInOut(duration: 2.0)) {
                                self.selectedImageIndex = (self.selectedImageIndex + 1) % self.featured.count
                            }
                        }
                    }
            }            
=======
            // Auto-sliding removed - slider now only responds to user gestures            
>>>>>>> de588a7 (GPT-5 changes to featured slider)
            self.wishlists = obj.watchlist ?? []
            self.genres = obj.genreContents ?? []
            self.topContents = obj.topContents ?? []
            
            // Extract new releases from all genre contents
            self.extractNewReleases(from: obj.genreContents ?? [])
        }, callbackFailure: { error in
            self.stopLoading()
            print("HomeViewModel fetchData error: \(error)")
        })
    }
    
    func toggleWatchlist(contentId: Int, completion: @escaping (Bool, String?) -> Void) {
        guard let userId = myUser?.id else {
            completion(false, "Please login to add to watchlist")
            return
        }
        
        let params: [Params: Any] = [
            .appUserId: userId,
            .contentId: contentId
        ]
        
        if let profileId = SessionManager.shared.currentProfile?.profileId {
            var updatedParams = params
            updatedParams[.profileId] = profileId
            
            NetworkManager.callWebService(url: .toggleWatchlist, params: updatedParams) { [weak self] (obj: UserModel) in
                if let user = obj.data {
                    self?.myUser = user
                    // Also update SessionManager to keep it in sync
                    SessionManager.shared.currentUser = user
                    completion(true, obj.message)
                } else {
                    completion(false, obj.message)
                }
            }
        } else {
            NetworkManager.callWebService(url: .toggleWatchlist, params: params) { [weak self] (obj: UserModel) in
                if let user = obj.data {
                    self?.myUser = user
                    // Also update SessionManager to keep it in sync
                    SessionManager.shared.currentUser = user
                    completion(true, obj.message)
                } else {
                    completion(false, obj.message)
                }
            }
        }
    }
    
    private func extractNewReleases(from genres: [Genre]) {
        var allContent: [VugaContent] = []
        
        // Collect all content from all genres
        for genre in genres {
            if let contents = genre.contents {
                allContent.append(contentsOf: contents)
            }
        }
        
        // Get current date
        let currentDate = Date()
        
        // Filter content by release year only
        let currentYear = Calendar.current.component(.year, from: currentDate)
        
        let filteredContent = allContent.filter { content in
            // Only include content with release year matching current year
            if let releaseYear = content.releaseYear {
                return releaseYear == currentYear
            }
            return false
        }
        
        // Remove duplicates based on content ID
        var uniqueContent: [VugaContent] = []
        var seenIds = Set<Int>()
        
        for content in filteredContent {
            if let id = content.id, !seenIds.contains(id) {
                seenIds.insert(id)
                uniqueContent.append(content)
            }
        }
        
        self.newReleases = uniqueContent.sorted { content1, content2 in
            guard let date1String = content1.createdAt,
                  let date2String = content2.createdAt else { return false }
            
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"
            
            guard let date1 = dateFormatter.date(from: date1String),
                  let date2 = dateFormatter.date(from: date2String) else { return false }
            
            return date1 > date2
        }
        
        // Limit to 20 items for performance
        if self.newReleases.count > 20 {
            self.newReleases = Array(self.newReleases.prefix(20))
        }
    }
}


func loadJSON<T: Codable>(callbackSuccess : @escaping (T) -> ()) {
    if let url = Bundle.main.url(forResource: "sample", withExtension: "json") {
        do {
            let data = try Data(contentsOf: url)
            let decoder = JSONDecoder()
                do {
                    let settings = try decoder.decode(T.self, from: data)
                    callbackSuccess(settings)
                } catch {
                    print("Error decoding JSON data: \(error)")
                    
                }
        } catch {
            print("Error loading JSON data: \(error)")
        }
    }
}
