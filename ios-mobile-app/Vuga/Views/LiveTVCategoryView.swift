//
//  LiveTVCategoryView.swift
//  Vuga
//
//  Live TV category detail view (matches Android category drill-down functionality)
//

import SwiftUI
import Kingfisher

struct LiveTVCategoryView: View {
    let category: LiveTVCategory
    @StateObject private var viewModel = EnhancedLiveTVViewModel()
    @AppStorage(SessionKeys.isPro) var isPro = false
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    // Dialog states
    @State private var showPremiumDialog = false
    @State private var showAdDialog = false
    @State private var channelForAd: LiveTVChannel?
    
    // Search state
    @State private var searchQuery = ""
    @State private var isSearchVisible = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Header with back button and search
            headerView
            
            // Search bar (if visible)
            if isSearchVisible {
                searchBarView
            }
            
            // Channels grid
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
            setupCategoryView()
        }
    }
    
    // MARK: - Header View
    private var headerView: some View {
        HStack {
            // Back button
            Button(action: {
                Navigation.pop()
            }) {
                Image.back
                    .resizeFitTo(size: 24, renderingMode: .template)
                    .foregroundColor(.text)
            }
            
            // Category title
            Text(category.displayName)
                .outfitSemiBold(20)
                .foregroundColor(.text)
            
            Spacer()
            
            // Channel count badge
            if let count = category.channelCount, count > 0 {
                Text("\(count) channels")
                    .outfitRegular(12)
                    .foregroundColor(.textLight)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.stroke.opacity(0.1))
                    .cornerRadius(12)
            }
            
            // Search toggle button
            Button(action: {
                withAnimation(.easeInOut(duration: 0.3)) {
                    isSearchVisible.toggle()
                    if !isSearchVisible {
                        searchQuery = ""
                    }
                }
            }) {
                Image.search
                    .resizeFitTo(size: 24, renderingMode: .template)
                    .foregroundColor(.text)
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 16)
    }
    
    // MARK: - Search Bar View
    private var searchBarView: some View {
        HStack(spacing: 12) {
            Image.search
                .resizeFitTo(size: 20, renderingMode: .template)
                .foregroundColor(.textLight)
            
            TextField("Search in \(category.displayName)...", text: $searchQuery)
                .outfitRegular(16)
                .foregroundColor(.text)
            
            if !searchQuery.isEmpty {
                Button(action: {
                    searchQuery = ""
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
    
    // MARK: - Channels Grid View
    private var channelsGridView: some View {
        ScrollView {
            LazyVGrid(columns: viewModel.getGridColumns(), spacing: 16) {
                ForEach(filteredChannels, id: \.channelId) { channel in
                    categoryChannelCard(channel: channel)
                        .onAppear {
                            // Load more if needed
                            if channel.channelId == filteredChannels.last?.channelId {
                                viewModel.loadMoreChannels()
                            }
                        }
                }
            }
            .padding(.horizontal)
            .padding(.top, 16)
            .padding(.bottom, 100)
        }
        .refreshable {
            viewModel.refreshData()
        }
        .overlay(
            // Empty state
            Group {
                if filteredChannels.isEmpty && !viewModel.isLoading {
                    if searchQuery.isEmpty {
                        NoDataFoundView(message: "No channels available in this category")
                    } else {
                        SearchEmptyView(query: searchQuery, category: category.displayName)
                    }
                }
            }
        )
        .loaderView(viewModel.isLoading)
    }
    
    // MARK: - Category Channel Card
    private func categoryChannelCard(channel: LiveTVChannel) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            // Channel thumbnail with overlay info
            ZStack(alignment: .bottomLeading) {
                ZStack(alignment: .topTrailing) {
                    KFImage(URL(string: channel.channelLogo?.addBaseURL() ?? ""))
                        .resizable()
                        .placeholder {
                            Rectangle()
                                .fill(Color.stroke.opacity(0.1))
                                .overlay(
                                    Image.tv
                                        .resizeFitTo(size: 40, renderingMode: .template)
                                        .foregroundColor(.textLight)
                                )
                        }
                        .aspectRatio(16/9, contentMode: .fill)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    
                    // Access type indicator
                    accessTypeIndicator(for: channel)
                }
                
                // Channel number badge (if available)
                if let channelNumber = channel.channelNumber, channelNumber > 0 {
                    Text("\(channelNumber)")
                        .outfitSemiBold(12)
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 3)
                        .background(Color.black.opacity(0.7))
                        .cornerRadius(6)
                        .padding(8)
                }
            }
            
            // Channel details
            VStack(alignment: .leading, spacing: 6) {
                // Channel title
                Text(channel.displayTitle)
                    .outfitSemiBold(16)
                    .foregroundColor(.text)
                    .lineLimit(1)
                
                // Current program info
                if !channel.displayCurrentProgram.isEmpty && channel.displayCurrentProgram != "No program info" {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Now Playing:")
                            .outfitRegular(11)
                            .foregroundColor(.textLight)
                        
                        Text(channel.displayCurrentProgram)
                            .outfitRegular(13)
                            .foregroundColor(.text)
                            .lineLimit(2)
                    }
                }
                
                // Next program (if available)
                if let nextProgram = channel.nextProgramTitle, !nextProgram.isEmpty {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Up Next:")
                            .outfitRegular(11)
                            .foregroundColor(.textLight)
                        
                        Text(nextProgram)
                            .outfitRegular(12)
                            .foregroundColor(.textLight)
                            .lineLimit(1)
                    }
                }
                
                // Quality and status indicators
                HStack {
                    // Quality badge
                    Text(channel.displayQuality)
                        .outfitRegular(10)
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.base)
                        .cornerRadius(4)
                    
                    Spacer()
                    
                    // Live indicator
                    HStack(spacing: 4) {
                        Circle()
                            .fill(Color.red)
                            .frame(width: 6, height: 6)
                        
                        Text("LIVE")
                            .outfitSemiBold(10)
                            .foregroundColor(.red)
                    }
                }
            }
            .padding(.horizontal, 4)
        }
        .padding(8)
        .background(Color.stroke.opacity(0.05))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.stroke.opacity(0.1), lineWidth: 1)
        )
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
                    .resizeFitTo(size: 18, renderingMode: .template)
                    .foregroundColor(.white)
                    .padding(6)
                    .background(requirement.color)
                    .clipShape(Circle())
                    .padding(8)
            }
        }
    }
    
    // MARK: - Channel Player View
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
    
    // MARK: - Premium Dialog
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
    
    // MARK: - Ad Dialog
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
    
    // MARK: - Computed Properties
    
    private var filteredChannels: [LiveTVChannel] {
        let categoryChannels = viewModel.allChannels.filter { channel in
            guard let categoryIds = channel.categoryIds else { return false }
            return categoryIds.contains(String(category.categoryId))
        }
        
        if searchQuery.isEmpty {
            return categoryChannels
        }
        
        let lowercaseQuery = searchQuery.lowercased()
        return categoryChannels.filter { channel in
            let titleMatch = channel.displayTitle.lowercased().contains(lowercaseQuery)
            let programMatch = channel.displayCurrentProgram.lowercased().contains(lowercaseQuery)
            return titleMatch || programMatch
        }
    }
    
    // MARK: - Helper Methods
    
    private func setupCategoryView() {
        viewModel.loadLiveTVData()
        viewModel.selectCategory(category)
    }
    
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
    
    private func showRewardedAd(for channel: LiveTVChannel) {
        RewardedAdManager.shared.showAdReward {
            viewModel.selectChannel(channel)
        }
    }
}

// MARK: - Search Empty View
struct SearchEmptyView: View {
    let query: String
    let category: String
    
    var body: some View {
        VStack(spacing: 16) {
            Image.search
                .resizeFitTo(size: 60, renderingMode: .template)
                .foregroundColor(.textLight)
            
            Text("No results found")
                .outfitSemiBold(18)
                .foregroundColor(.text)
            
            Text("No channels in \(category) match '\(query)'")
                .outfitRegular(14)
                .foregroundColor(.textLight)
                .multilineTextAlignment(.center)
        }
        .padding(40)
    }
}

// MARK: - Preview
#Preview {
    LiveTVCategoryView(category: LiveTVCategory.allCategory)
}