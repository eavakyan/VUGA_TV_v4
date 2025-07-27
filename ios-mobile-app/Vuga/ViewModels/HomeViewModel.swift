//
//  HomeViewModel.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 15/05/24.
//

import SwiftUI
import Combine

class HomeViewModel : BaseViewModel {
    @Published var featured = [FlixyContent]()
    @Published var topContents = [TopContent]()
    @Published var wishlists = [FlixyContent]()
    @Published var genres = [Genre]()
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
        let params: [Params: Any] = [.userId : myUser?.id ?? 0]
        NetworkManager.callWebService(url: .fetchHomePageData, params: params) { [weak self] (obj: HomeModel) in
            guard let self = self else { return }
            
            self.stopLoading()
            self.featured = obj.featured ?? []
            
//            self.topContents = obj.topContents ?? []
            if self.featured.isNotEmpty {
                self.cancellable = Timer.publish(every: TimeInterval(Limits.featureSecond), on: .main, in: .common)
                    .autoconnect()
                    .sink { [weak self] _ in
                        DispatchQueue.main.async { [weak self] in
                            guard let self = self, self.featured.isNotEmpty else { return }
                            withAnimation {
                                self.selectedImageIndex = (self.selectedImageIndex + 1) % self.featured.count
                            }
                        }
                    }
            }            
            self.wishlists = obj.watchlist ?? []
            self.genres = obj.genreContents ?? []
            self.topContents = obj.topContents ?? []
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
