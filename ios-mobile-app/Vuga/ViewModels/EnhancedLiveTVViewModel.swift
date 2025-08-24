//
//  EnhancedLiveTVViewModel.swift
//  Vuga
//
//  Enhanced Live TV ViewModel that matches Android functionality
//

import Foundation
import Combine
import SwiftUI

class EnhancedLiveTVViewModel: BaseViewModel {
    @Published var allChannels: [LiveTVChannel] = []
    @Published var filteredChannels: [LiveTVChannel] = []
    @Published var categories: [LiveTVCategory] = []
    @Published var selectedCategory: LiveTVCategory?
    @Published var selectedChannel: LiveTVChannel?
    @Published var searchQuery: String = ""
    @Published var isEmpty: Bool = false
    @Published var hasMoreChannels: Bool = false
    
    // UI State
    @Published var isSearchVisible: Bool = false
    @Published var selectedCategoryId: Int = 0 // 0 = All
    
    private let livetvService = LiveTVService.shared
    private var cancellables = Set<AnyCancellable>()
    
    override init() {
        super.init()
        setupBindings()
        loadLiveTVData()
    }
    
    // MARK: - Public Methods (matches Android LiveTvActivity functionality)
    
    /// Load Live TV channels and categories (matches Android loadLiveTvData)
    func loadLiveTVData() {
        livetvService.loadChannelsAndCategories()
    }
    
    /// Refresh data (matches Android pull-to-refresh)
    func refreshData() {
        livetvService.refreshChannels()
    }
    
    /// Load more channels for pagination (matches Android scroll pagination)
    func loadMoreChannels() {
        livetvService.loadMoreChannels()
    }
    
    /// Handle category selection (matches Android onCategorySelected)
    func selectCategory(_ category: LiveTVCategory) {
        selectedCategory = category
        selectedCategoryId = category.categoryId
        filterChannels()
    }
    
    /// Handle channel selection (matches Android onChannelClicked)
    func selectChannel(_ channel: LiveTVChannel) {
        // Track channel view
        livetvService.trackChannelView(channelId: channel.channelId)
        selectedChannel = channel
    }
    
    /// Handle search query changes (matches Android TextWatcher)
    func updateSearchQuery(_ query: String) {
        searchQuery = query
        filterChannels()
    }
    
    /// Toggle search visibility (matches Android search icon click)
    func toggleSearch() {
        withAnimation(.easeInOut(duration: 0.3)) {
            isSearchVisible.toggle()
            if !isSearchVisible {
                searchQuery = ""
            }
        }
    }
    
    /// Clear search (matches Android clear search button)
    func clearSearch() {
        searchQuery = ""
    }
    
    /// Check if channel can be played without restrictions
    func canPlayChannel(_ channel: LiveTVChannel) -> Bool {
        let isPro = SessionManager.shared.isPro()
        return channel.isChannelFree || isPro
    }
    
    /// Get channel access requirement for UI display
    func getChannelAccessRequirement(_ channel: LiveTVChannel) -> ChannelAccessRequirement {
        let isPro = SessionManager.shared.isPro()
        
        if channel.isChannelFree || isPro {
            return .free
        } else if channel.isChannelPremium {
            return .premium
        } else if channel.channelRequiresAds {
            return .ad
        } else {
            return .premium // Default fallback
        }
    }
    
    // MARK: - Private Methods
    
    /// Setup reactive bindings (similar to Android observeViewModel)
    private func setupBindings() {
        // Bind service data to local properties
        livetvService.$channels
            .receive(on: DispatchQueue.main)
            .sink { [weak self] channels in
                self?.allChannels = channels
                self?.filterChannels()
            }
            .store(in: &cancellables)
        
        livetvService.$categories
            .receive(on: DispatchQueue.main)
            .sink { [weak self] categories in
                self?.categories = self?.setupCategoriesWithAll(categories) ?? []
            }
            .store(in: &cancellables)
        
        livetvService.$isLoading
            .receive(on: DispatchQueue.main)
            .assign(to: \.isLoading, on: self)
            .store(in: &cancellables)
        
        livetvService.$errorMessage
            .receive(on: DispatchQueue.main)
            .sink { [weak self] error in
                if let error = error {
                    self?.showError(error)
                }
            }
            .store(in: &cancellables)
        
        livetvService.$hasMoreChannels
            .receive(on: DispatchQueue.main)
            .assign(to: \.hasMoreChannels, on: self)
            .store(in: &cancellables)
    }
    
    /// Filter channels based on category and search query (matches Android filterChannels)
    private func filterChannels() {
        var filtered = allChannels
        
        // Filter by category
        if selectedCategoryId != 0 {
            filtered = livetvService.getChannelsByCategory(selectedCategoryId)
        }
        
        // Filter by search query
        if !searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            filtered = livetvService.filterChannels(query: searchQuery, in: filtered)
        }
        
        filteredChannels = filtered
        isEmpty = filtered.isEmpty && !isLoading
    }
    
    /// Setup categories with "All" category (matches Android category setup)
    private func setupCategoriesWithAll(_ categories: [LiveTVCategory]) -> [LiveTVCategory] {
        var allCategories: [LiveTVCategory] = []
        
        // Add "All" category
        var allCategory = LiveTVCategory.allCategory
        allCategories.append(allCategory)
        
        // Add regular categories
        allCategories.append(contentsOf: categories)
        
        return allCategories
    }
    
    /// Show error message (matches Android showError)
    private func showError(_ message: String) {
        // Could integrate with toast/alert system
        print("Live TV Error: \(message)")
    }
}

// MARK: - Channel Access Requirement Enum
enum ChannelAccessRequirement {
    case free
    case premium
    case ad
    
    var icon: Image {
        switch self {
        case .free:
            return Image(systemName: "play.circle.fill")
        case .premium:
            return Image.crown
        case .ad:
            return Image.adLcok
        }
    }
    
    var color: Color {
        switch self {
        case .free:
            return .green
        case .premium:
            return .rating
        case .ad:
            return .orange
        }
    }
    
    var title: String {
        switch self {
        case .free:
            return "Free"
        case .premium:
            return "Premium"
        case .ad:
            return "Watch Ad"
        }
    }
}

// MARK: - Extensions for SwiftUI Integration

extension EnhancedLiveTVViewModel {
    /// Get category display name
    func getCategoryDisplayName(_ category: LiveTVCategory) -> String {
        return category.displayName
    }
    
    /// Check if category is selected
    func isCategorySelected(_ category: LiveTVCategory) -> Bool {
        return selectedCategoryId == category.categoryId
    }
    
    /// Get channel count for category
    func getChannelCount(for categoryId: Int) -> Int {
        if categoryId == 0 {
            return allChannels.count
        }
        return livetvService.getChannelsByCategory(categoryId).count
    }
    
    /// Get grid layout configuration based on device
    func getGridColumns() -> [GridItem] {
        let isTablet = UIDevice.current.userInterfaceIdiom == .pad
        let isLandscape = UIDevice.current.orientation.isLandscape
        
        let columnCount: Int
        if isTablet {
            columnCount = isLandscape ? 6 : 4
        } else {
            columnCount = isLandscape ? 3 : 2
        }
        
        return Array(repeating: GridItem(.flexible()), count: columnCount)
    }
    
    /// Format program time for display
    func formatProgramTime(_ startTime: String?, _ endTime: String?) -> String {
        guard let start = startTime, let end = endTime else { return "" }
        // Add time formatting logic here
        return "\(start) - \(end)"
    }
    
    /// Check if search results are empty
    var isSearchEmpty: Bool {
        return searchQuery.isEmpty
    }
    
    /// Check if should show clear search button
    var shouldShowClearSearch: Bool {
        return !searchQuery.isEmpty
    }
}