//
//  LiveTVsViewModel.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 28/05/24.
//

import Foundation

class LiveTVsViewModel : BaseViewModel {
    @Published var categories = [TVCategory]()
    @Published var search = ""
    @Published var allLiveTvChannels = [Channel]()
    @Published var filteredChannels = [Channel]()
    @Published var isSearchEmpty = true
    @Published var selectedChannel : Channel?
    @Published var channels = [Channel]()

    
    override init() {
        super.init()
        fetchData()
        isSearchEmpty = true
    }
    
    func fetchData(){
        startLoading()
//        loadJSON { (obj: TVCategoriesModel) in
        NetworkManager.callWebService(url: .fetchLiveTVPageData) { (obj: TVCategoriesModel) in
            self.stopLoading()
            if let data = obj.data {
                self.categories = data
            }
        }
    }
    
    func filterLiveTvs(isForRefresh: Bool = true) {
        if isForRefresh {
            self.filteredChannels.removeAll()
        }
        startLoading()
        let params : [Params : Any] = [.keyword : search,.start: filteredChannels.count,.limit: Limits.pagination]
        NetworkManager.callWebService(url: .searchTVChannel, params: params) { (obj: SearchLiveTvModel) in
            if self.search.isEmpty {
                self.isSearchEmpty = true
                self.filteredChannels.removeAll()
                self.isLoading = false
//                self.filteredChannels = []
            }
            if !self.search.isEmpty && self.filteredChannels.isEmpty {
                self.isSearchEmpty = false
                self.isLoading = false
            }
            if let data = obj.data {
                DispatchQueue.main.async { [weak self] in
                    self?.filteredChannels.append(contentsOf: data)
                }
            }
        }
    }
    
    func fetchData(category: TVCategory){
        startLoading()
        let params : [Params: Any] = [.tvCategoryId: category.id ?? 0, .start: channels.count, .limit: Limits.pagination]
//        loadJSON { (obj: TVCategoryModel) in
        NetworkManager.callWebService(url: .fetchTVChannelByCategory,params: params) { (obj: TVCategoryModel) in
            self.stopLoading()
            if let data = obj.data {
                self.channels.append(contentsOf: data.channels ?? [])
                
            }
        }
    }
    
}
