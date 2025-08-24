//
//  EnhancedLiveTVView.swift
//  Vuga
//
//  Enhanced Live TV view that matches Android LiveTvActivity functionality
//

import SwiftUI
import Kingfisher

struct EnhancedLiveTVView: View {
    @StateObject private var viewModel = EnhancedLiveTVViewModel()
    @AppStorage(SessionKeys.isPro) var isPro = false
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    // Dialog states
    @State private var showPremiumDialog = false
    @State private var showAdDialog = false
    @State private var channelForAd: LiveTVChannel?
    
    var body: some View {
        VStack(spacing: 0) {
            // Header with search functionality (matches Android toolbar + search)
            headerView
            
            // Categories horizontal scroll (matches Android rvCategories)
            categoriesView
            
            // Channels grid with pull-to-refresh (matches Android rvChannels + swipeRefresh)
            channelsGridView
        }
        .background(Color.bg)
        .navigationBarHidden(true)
        .fullScreenCover(item: $viewModel.selectedChannel) { channel in
            channelPlayerView(for: channel)
        }
        .customAlert(isPresented: $showPremiumDialog) {
            premiumDialogView
        }
        .customAlert(isPresented: $showAdDialog) {
            adDialogView
        }
        .onAppear {
            viewModel.loadLiveTVData()
        }
    }
    
    // MARK: - Header View (matches Android toolbar setup)
    private var headerView: some View {
        VStack(spacing: 0) {
            HStack {
                // Back button
                Button(action: {
                    Navigation.pop()
                }) {
                    Image.back
                        .resizeFitTo(size: 24, renderingMode: .template)
                        .foregroundColor(.text)
                }
                
                // Title
                Text("Live TV")
                    .outfitSemiBold(24)
                    .foregroundColor(.text)
                
                Spacer()
                
                // Search icon (matches Android ivSearchIcon)
                Button(action: {
                    viewModel.toggleSearch()
                }) {
                    Image.search
                        .resizeFitTo(size: 24, renderingMode: .template)
                        .foregroundColor(.text)
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 10)
            
            // Search input layout (matches Android searchInputLayout)
            if viewModel.isSearchVisible {
                HStack(spacing: 12) {
                    Image.search
                        .resizeFitTo(size: 20, renderingMode: .template)
                        .foregroundColor(.textLight)
                    
                    TextField("Search channels...", text: $viewModel.searchQuery)
                        .outfitRegular(16)
                        .foregroundColor(.text)
                        .onChange(of: viewModel.searchQuery) { query in
                            viewModel.updateSearchQuery(query)
                        }
                    
                    // Clear search button (matches Android ivClearSearch)
                    if viewModel.shouldShowClearSearch {
                        Button(action: {
                            viewModel.clearSearch()
                        }) {
                            Image(systemName: "xmark.circle.fill")
                                .resizeFitTo(size: 20, renderingMode: .template)
                                .foregroundColor(.textLight)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 12)
                .background(Color.stroke.opacity(0.1))
                .cornerRadius(10)
                .padding(.horizontal)
                .padding(.bottom, 10)
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
    }
    
    // MARK: - Categories View (matches Android rvCategories)
    private var categoriesView: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            LazyHStack(spacing: 12) {
                ForEach(viewModel.categories, id: \.categoryId) { category in
                    categoryChip(category: category)
                }
            }
            .padding(.horizontal)
        }
        .frame(height: 50)
    }
    
    // MARK: - Category Chip (matches Android CategoryAdapter items)
    private func categoryChip(category: LiveTVCategory) -> some View {
        HStack(spacing: 8) {
            // Category icon
            if let iconUrl = category.icon {
                KFImage(URL(string: iconUrl.addBaseURL()))
                    .resizable()
                    .renderingMode(.template)
                    .scaledToFit()
                    .frame(width: 16, height: 16)
                    .foregroundColor(viewModel.isCategorySelected(category) ? .white : .text)
            }
            
            // Category name
            Text(viewModel.getCategoryDisplayName(category))
                .outfitRegular(14)
                .foregroundColor(viewModel.isCategorySelected(category) ? .white : .text)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(
            viewModel.isCategorySelected(category) ? Color.base : Color.stroke.opacity(0.1)
        )
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color.stroke.opacity(0.3), lineWidth: 1)
        )
        .onTapGesture {
            viewModel.selectCategory(category)
        }
    }
    
    // MARK: - Channels Grid View (matches Android rvChannels with GridLayoutManager)
    private var channelsGridView: some View {
        ScrollView {
            LazyVGrid(columns: viewModel.getGridColumns(), spacing: 16) {
                ForEach(viewModel.filteredChannels, id: \.channelId) { channel in
                    channelCard(channel: channel)
                        .onAppear {
                            // Load more channels when reaching the end (matches Android scroll listener)
                            if channel.channelId == viewModel.filteredChannels.last?.channelId {
                                viewModel.loadMoreChannels()
                            }
                        }
                }
            }
            .padding(.horizontal)
            .padding(.top, 16)
            .padding(.bottom, 100) // Tab bar spacing
        }
        .refreshable {
            viewModel.refreshData()
        }
        .overlay(
            // Empty state (matches Android tvNoChannels)
            Group {
                if viewModel.isEmpty && !viewModel.isLoading {
                    if viewModel.isSearchEmpty {
                        EmptySearchView()
                    } else {
                        NoDataFoundView(message: "No channels found")
                    }
                }
            }
        )
        .loaderView(viewModel.isLoading)
    }
    
    // MARK: - Channel Card (matches Android ChannelCard in adapter)
    private func channelCard(channel: LiveTVChannel) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            // Channel thumbnail
            ZStack(alignment: .topTrailing) {
                KFImage(URL(string: channel.channelLogo?.addBaseURL() ?? ""))
                    .resizable()
                    .placeholder {
                        Rectangle()
                            .fill(Color.stroke.opacity(0.1))
                            .overlay(
                                Image.tv
                                    .resizeFitTo(size: 30, renderingMode: .template)
                                    .foregroundColor(.textLight)
                            )
                    }
                    .aspectRatio(16/9, contentMode: .fill)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                
                // Access type indicator
                accessTypeIndicator(for: channel)
            }
            
            // Channel info
            VStack(alignment: .leading, spacing: 4) {
                // Channel title
                Text(channel.displayTitle)
                    .outfitSemiBold(14)
                    .foregroundColor(.text)
                    .lineLimit(1)
                
                // Current program (if available)
                if !channel.displayCurrentProgram.isEmpty && channel.displayCurrentProgram != "No program info" {
                    Text(channel.displayCurrentProgram)
                        .outfitRegular(12)
                        .foregroundColor(.textLight)
                        .lineLimit(2)
                }
                
                // Program time (if available)
                if let startTime = channel.currentProgramStart, let endTime = channel.currentProgramEnd {
                    Text(viewModel.formatProgramTime(startTime, endTime))
                        .outfitRegular(10)
                        .foregroundColor(.textLight)
                }
                
                // Quality badge
                HStack {
                    Text(channel.displayQuality)
                        .outfitRegular(10)
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.base)
                        .cornerRadius(4)
                    
                    Spacer()
                }
            }
        }
        .onTapGesture {
            handleChannelTap(channel)
        }
    }
    
    // MARK: - Access Type Indicator
    private func accessTypeIndicator(for channel: LiveTVChannel) -> some View {
        let requirement = viewModel.getChannelAccessRequirement(channel)
        
        return Group {
            if requirement != .free {
                requirement.icon
                    .resizeFitTo(size: 16, renderingMode: .template)
                    .foregroundColor(.white)
                    .padding(4)
                    .background(requirement.color)
                    .clipShape(Circle())
                    .padding(6)
            }
        }
    }
    
    // MARK: - Channel Player View (matches Android VideoPlayerView/YoutubeView)
    private func channelPlayerView(for channel: LiveTVChannel) -> some View {
        Group {
            if channel.playbackUrl.lowercased().contains("youtube") {
                YoutubeView(youtubeUrl: channel.playbackUrl)
            } else {
                let streamType = channel.playbackUrl.lowercased().contains(".mpd") ? 5 : 2
                VideoPlayerView(
                    type: streamType,
                    isShowAdView: false,
                    isLiveVideo: true,
                    url: channel.playbackUrl
                )
            }
        }
    }
    
    // MARK: - Premium Dialog (matches Android CustomDialogBuilder.showPremiumDialog)
    private var premiumDialogView: some View {
        DialogCard(
            icon: Image.crown,
            title: .subScribeToPro,
            iconColor: .rating,
            subTitle: .proDialogDes,
            buttonTitle: .becomeAPro,
            onClose: {
                showPremiumDialog = false
            },
            onButtonTap: {
                showPremiumDialog = false
                Navigation.pushToSwiftUiView(ProView())
            }
        )
    }
    
    // MARK: - Ad Dialog (matches Android CustomDialogBuilder.showUnlockDialog)
    private var adDialogView: some View {
        DialogCard(
            icon: Image.adLcok,
            title: .unlokeWithAd,
            subTitle: .adDialogDes,
            buttonTitle: .watchAd,
            onClose: {
                showAdDialog = false
                channelForAd = nil
            },
            onButtonTap: {
                showAdDialog = false
                if let channel = channelForAd {
                    showRewardedAd(for: channel)
                }
            }
        )
    }
    
    // MARK: - Helper Methods
    
    /// Handle channel tap (matches Android onChannelClicked)
    private func handleChannelTap(_ channel: LiveTVChannel) {
        let requirement = viewModel.getChannelAccessRequirement(channel)
        
        switch requirement {
        case .free:
            viewModel.selectChannel(channel)
        case .premium:
            showPremiumDialog = true
        case .ad:
            channelForAd = channel
            showAdDialog = true
        }
    }
    
    /// Show rewarded ad (matches Android loadRewardedAd)
    private func showRewardedAd(for channel: LiveTVChannel) {
        RewardedAdManager.shared.showAdReward {
            viewModel.selectChannel(channel)
        }
    }
}

// MARK: - Empty Search View
struct EmptySearchView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    var body: some View {
        VStack(spacing: 16) {
            Image.search
                .resizeFitTo(size: 60, renderingMode: .template)
                .foregroundColor(.textLight)
            
            Text("Start typing to search channels")
                .outfitRegular(16)
                .foregroundColor(.textLight)
                .multilineTextAlignment(.center)
        }
        .padding(40)
    }
}

// MARK: - No Data Found View
struct NoDataFoundView: View {
    let message: String
    
    var body: some View {
        VStack(spacing: 16) {
            Image.tv
                .resizeFitTo(size: 60, renderingMode: .template)
                .foregroundColor(.textLight)
            
            Text(message)
                .outfitRegular(16)
                .foregroundColor(.textLight)
                .multilineTextAlignment(.center)
        }
        .padding(40)
    }
}

// MARK: - Preview
#Preview {
    EnhancedLiveTVView()
}