//
//  LiveTVService.swift
//  Vuga
//
//  Live TV service layer that handles API calls and data management
//

import Foundation
import Combine

class LiveTVService: ObservableObject {
    static let shared = LiveTVService()
    
    @Published var channels: [LiveTVChannel] = []
    @Published var categories: [LiveTVCategory] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var hasMoreChannels = false
    
    private var cancellables = Set<AnyCancellable>()
    private var currentPage = 1
    private let perPage = 20
    
    private init() {}
    
    // MARK: - Public API Methods
    
    /// Load Live TV channels and categories (matches Android loadChannelsAndCategories)
    func loadChannelsAndCategories() {
        isLoading = true
        errorMessage = nil
        currentPage = 1
        
        // For now, use existing API until V2 endpoints are available
        loadLiveTVPageData()
    }
    
    /// Refresh channels (matches Android refreshChannels)
    func refreshChannels() {
        currentPage = 1
        loadLiveTVPageData()
    }
    
    /// Load more channels for pagination (matches Android loadMoreChannels)
    func loadMoreChannels() {
        guard !isLoading && hasMoreChannels else { return }
        currentPage += 1
        loadChannelsWithPrograms(append: true)
    }
    
    /// Search channels (matches Android search functionality)
    func searchChannels(query: String, completion: @escaping ([LiveTVChannel]) -> Void) {
        guard !query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            completion([])
            return
        }
        
        let userId = SessionManager.shared.getUser()?.id ?? 0
        let params = LiveTVAPIParams.searchChannels(query: query, userId: userId)
        
        NetworkManager.callWebService(url: .searchTVChannel, params: params) { (result: SearchLiveTvModel) in
            let channels = result.data?.map { channel -> LiveTVChannel in
                // Convert Channel to LiveTVChannel
                return LiveTVChannel(
                    id: channel.id,
                    tvChannelId: nil,
                    title: channel.title,
                    streamUrl: channel.source,
                    source: channel.source,
                    logoUrl: channel.thumbnail,
                    thumbnail: channel.thumbnail,
                    categoryIds: channel.categoryID,
                    channelNumber: nil,
                    accessType: channel.accessType?.rawValue,
                    isPremium: channel.accessType == .premium,
                    isFree: channel.accessType == .free,
                    requiresAds: channel.accessType == .locked,
                    currentProgramTitle: nil,
                    currentProgramDescription: nil,
                    currentProgramStart: nil,
                    currentProgramEnd: nil,
                    nextProgramTitle: nil,
                    nextProgramStart: nil,
                    isActive: true,
                    quality: nil,
                    description: nil
                )
            } ?? []
            
            DispatchQueue.main.async {
                completion(channels)
            }
        }
    }
    
    /// Track channel view (matches Android trackChannelView)
    func trackChannelView(channelId: Int) {
        let userId = SessionManager.shared.getUser()?.id ?? 0
        let params = LiveTVAPIParams.trackChannelView(channelId: channelId, userId: userId)
        
        // Fire and forget API call - matches Android behavior
        NetworkManager.callWebService(url: .searchTVChannel, params: params) { (_: StatusAndMessageModel) in
            // Success - no action needed, matches Android implementation
        }
    }
    
    /// Get channels by category (matches Android category filtering)
    func getChannelsByCategory(_ categoryId: Int) -> [LiveTVChannel] {
        guard categoryId != 0 else { return channels } // Return all channels for "All" category
        
        return channels.filter { channel in
            guard let categoryIds = channel.categoryIds else { return false }
            return categoryIds.contains(String(categoryId))
        }
    }
    
    /// Filter channels by search query (local filtering)
    func filterChannels(query: String, in channelList: [LiveTVChannel]) -> [LiveTVChannel] {
        guard !query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return channelList
        }
        
        let lowercaseQuery = query.lowercased()
        return channelList.filter { channel in
            let titleMatch = channel.displayTitle.lowercased().contains(lowercaseQuery)
            let programMatch = channel.displayCurrentProgram.lowercased().contains(lowercaseQuery)
            return titleMatch || programMatch
        }
    }
    
    // MARK: - Private API Methods
    
    /// Load Live TV page data using existing API (backward compatibility)
    private func loadLiveTVPageData() {
        NetworkManager.callWebService(url: .fetchLiveTVPageData) { (result: TVCategoriesModel) in
            DispatchQueue.main.async {
                self.isLoading = false
                
                if let data = result.data {
                    // Convert to new model structure
                    self.categories = data.map { tvCategory -> LiveTVCategory in
                        return LiveTVCategory(
                            id: tvCategory.id,
                            tvCategoryId: nil,
                            name: tvCategory.title,
                            title: tvCategory.title,
                            slug: nil,
                            icon: tvCategory.image,
                            order: nil,
                            isActive: true,
                            channelCount: tvCategory.channels?.count,
                            channels: tvCategory.channels?.map { channel -> LiveTVChannel in
                                return LiveTVChannel(
                                    id: channel.id,
                                    tvChannelId: nil,
                                    title: channel.title,
                                    streamUrl: channel.source,
                                    source: channel.source,
                                    logoUrl: channel.thumbnail,
                                    thumbnail: channel.thumbnail,
                                    categoryIds: channel.categoryID,
                                    channelNumber: nil,
                                    accessType: channel.accessType?.rawValue,
                                    isPremium: channel.accessType == .premium,
                                    isFree: channel.accessType == .free,
                                    requiresAds: channel.accessType == .locked,
                                    currentProgramTitle: nil,
                                    currentProgramDescription: nil,
                                    currentProgramStart: nil,
                                    currentProgramEnd: nil,
                                    nextProgramTitle: nil,
                                    nextProgramStart: nil,
                                    isActive: true,
                                    quality: nil,
                                    description: nil
                                )
                            }
                        )
                    }
                    
                    // Extract all channels from all categories
                    var allChannels: [LiveTVChannel] = []
                    for category in self.categories {
                        allChannels.append(contentsOf: category.channels ?? [])
                    }
                    self.channels = allChannels
                    
                    // Update pagination info
                    self.hasMoreChannels = false // Current API doesn't support pagination
                    self.errorMessage = nil
                } else {
                    self.errorMessage = result.message ?? "Failed to load Live TV data"
                }
            }
        }
    }
    
    /// Load channels with programs using V2 API (when available)
    private func loadChannelsWithPrograms(append: Bool = false) {
        let userId = SessionManager.shared.getUser()?.id ?? 0
        let params = LiveTVAPIParams.channelsWithPrograms(userId: userId, page: currentPage, perPage: perPage)
        
        // This would use the new V2 API when available
        // For now, fallback to existing implementation
        if !append {
            loadLiveTVPageData()
        }
    }
    
    // MARK: - Utility Methods
    
    /// Get predefined categories for UI (matches Android PredefinedCategories)
    func getPredefinedCategories() -> [LiveTVCategory] {
        var predefined: [LiveTVCategory] = [LiveTVCategory.allCategory]
        
        if !categories.isEmpty {
            predefined.append(contentsOf: categories)
        }
        
        return predefined
    }
    
    /// Check if channel requires premium subscription
    func channelRequiresPremium(_ channel: LiveTVChannel) -> Bool {
        return channel.isChannelPremium
    }
    
    /// Check if channel requires ad viewing
    func channelRequiresAd(_ channel: LiveTVChannel) -> Bool {
        return channel.channelRequiresAds
    }
    
    /// Check if channel is free to watch
    func channelIsFree(_ channel: LiveTVChannel) -> Bool {
        return channel.isChannelFree
    }
    
    /// Get channel access type for UI display
    func getChannelAccessType(_ channel: LiveTVChannel) -> AccessType {
        if channel.isChannelFree {
            return .free
        } else if channel.isChannelPremium {
            return .premium
        } else {
            return .locked
        }
    }
    
    /// Clear all data (useful for logout/profile switching)
    func clearAllData() {
        channels.removeAll()
        categories.removeAll()
        isLoading = false
        errorMessage = nil
        hasMoreChannels = false
        currentPage = 1
    }
}

// MARK: - Live TV Service Extensions for SwiftUI Integration

extension LiveTVService {
    /// Get channels for specific category with reactive updates
    func channelsPublisher(for categoryId: Int) -> AnyPublisher<[LiveTVChannel], Never> {
        $channels
            .map { [weak self] allChannels in
                guard let self = self else { return [] }
                return self.getChannelsByCategory(categoryId)
            }
            .eraseToAnyPublisher()
    }
    
    /// Get filtered channels publisher for search functionality
    func filteredChannelsPublisher(query: String, categoryId: Int = 0) -> AnyPublisher<[LiveTVChannel], Never> {
        $channels
            .map { [weak self] allChannels in
                guard let self = self else { return [] }
                let categoryChannels = self.getChannelsByCategory(categoryId)
                return self.filterChannels(query: query, in: categoryChannels)
            }
            .eraseToAnyPublisher()
    }
    
    /// Loading state publisher
    var loadingPublisher: AnyPublisher<Bool, Never> {
        $isLoading.eraseToAnyPublisher()
    }
    
    /// Error message publisher
    var errorPublisher: AnyPublisher<String?, Never> {
        $errorMessage.eraseToAnyPublisher()
    }
    
    /// Categories publisher
    var categoriesPublisher: AnyPublisher<[LiveTVCategory], Never> {
        $categories.eraseToAnyPublisher()
    }
}