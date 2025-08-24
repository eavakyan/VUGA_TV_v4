//
//  LiveTVSearchView.swift
//  Vuga
//
//  Live TV search view (matches Android LiveTvSearchView functionality)
//

import SwiftUI
import Kingfisher

struct LiveTVSearchView: View {
    @StateObject private var viewModel = EnhancedLiveTVViewModel()
    @AppStorage(SessionKeys.isPro) var isPro = false
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @FocusState private var isSearchFocused: Bool
    
    // Dialog states
    @State private var showPremiumDialog = false
    @State private var showAdDialog = false
    @State private var channelForAd: LiveTVChannel?
    
    var body: some View {
        VStack(spacing: 0) {
            // Header with back button and search field
            headerView
            
            // Search results grid
            searchResultsView
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
            isSearchFocused = true
        }
    }
    
    // MARK: - Header View
    private var headerView: some View {
        HStack(spacing: 12) {
            // Back button
            Button(action: {
                Navigation.pop()
            }) {
                Image.back
                    .resizeFitTo(size: 24, renderingMode: .template)
                    .foregroundColor(.text)
            }
            
            // Search field with icon
            HStack(spacing: 12) {
                Image.search
                    .resizeFitTo(size: 20, renderingMode: .template)
                    .foregroundColor(.textLight)
                
                Rectangle()
                    .frame(width: 1, height: 30)
                    .foregroundColor(.stroke)
                
                TextField("", text: $viewModel.searchQuery, prompt: Text("Search Live TV channels...")
                    .font(.custom("Outfit-Regular", size: 16))
                    .foregroundColor(.textLight)
                )
                .focused($isSearchFocused)
                .foregroundColor(.text)
                .outfitRegular(16)
                .onChange(of: viewModel.searchQuery) { query in
                    viewModel.updateSearchQuery(query)
                }
                
                // Clear button
                if !viewModel.searchQuery.isEmpty {
                    Button(action: {
                        viewModel.clearSearch()
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .resizeFitTo(size: 20, renderingMode: .template)
                            .foregroundColor(.textLight)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color.stroke.opacity(0.1))
            .cornerRadius(25)
            .overlay(
                RoundedRectangle(cornerRadius: 25)
                    .stroke(Color.stroke.opacity(0.3), lineWidth: 1)
            )
        }
        .padding(.horizontal)
        .padding(.vertical, 16)
    }
    
    // MARK: - Search Results View
    private var searchResultsView: some View {
        ScrollView {
            if viewModel.isSearchEmpty && !viewModel.isLoading {
                // Empty search state
                emptySearchView
            } else if viewModel.filteredChannels.isEmpty && !viewModel.searchQuery.isEmpty && !viewModel.isLoading {
                // No results found
                noResultsView
            } else {
                // Search results grid
                LazyVGrid(columns: viewModel.getGridColumns(), spacing: 16) {
                    ForEach(viewModel.filteredChannels, id: \.channelId) { channel in
                        searchChannelCard(channel: channel)
                            .onAppear {
                                // Trigger pagination if needed
                                if channel.channelId == viewModel.filteredChannels.last?.channelId {
                                    // Implement search pagination if API supports it
                                }
                            }
                    }
                }
                .padding(.horizontal)
                .padding(.top, 16)
                .padding(.bottom, 100)
            }
        }
        .loaderView(viewModel.isLoading)
    }
    
    // MARK: - Search Channel Card
    private func searchChannelCard(channel: LiveTVChannel) -> some View {
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
                // Channel title (with search highlighting if possible)
                Text(channel.displayTitle)
                    .outfitSemiBold(14)
                    .foregroundColor(.text)
                    .lineLimit(1)
                
                // Current program
                if !channel.displayCurrentProgram.isEmpty && channel.displayCurrentProgram != "No program info" {
                    Text(channel.displayCurrentProgram)
                        .outfitRegular(12)
                        .foregroundColor(.textLight)
                        .lineLimit(2)
                }
                
                // Quality and live indicator
                HStack {
                    Text(channel.displayQuality)
                        .outfitRegular(10)
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.base)
                        .cornerRadius(4)
                    
                    Spacer()
                    
                    HStack(spacing: 4) {
                        Circle()
                            .fill(Color.red)
                            .frame(width: 4, height: 4)
                        
                        Text("LIVE")
                            .outfitSemiBold(8)
                            .foregroundColor(.red)
                    }
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
                    .resizeFitTo(size: 14, renderingMode: .template)
                    .foregroundColor(.white)
                    .padding(4)
                    .background(requirement.color)
                    .clipShape(Circle())
                    .padding(6)
            }
        }
    }
    
    // MARK: - Empty Search View
    private var emptySearchView: some View {
        VStack(spacing: 20) {
            Image.search
                .resizeFitTo(size: 80, renderingMode: .template)
                .foregroundColor(.textLight)
            
            VStack(spacing: 8) {
                Text("Search Live TV")
                    .outfitSemiBold(20)
                    .foregroundColor(.text)
                
                Text("Enter channel name or program title to find live TV channels")
                    .outfitRegular(16)
                    .foregroundColor(.textLight)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)
            }
        }
        .padding(.top, 100)
    }
    
    // MARK: - No Results View
    private var noResultsView: some View {
        VStack(spacing: 20) {
            Image.tv
                .resizeFitTo(size: 80, renderingMode: .template)
                .foregroundColor(.textLight)
            
            VStack(spacing: 8) {
                Text("No channels found")
                    .outfitSemiBold(20)
                    .foregroundColor(.text)
                
                Text("No live TV channels match '\(viewModel.searchQuery)'")
                    .outfitRegular(16)
                    .foregroundColor(.textLight)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)
                
                Button(action: {
                    viewModel.clearSearch()
                    isSearchFocused = true
                }) {
                    Text("Try different keywords")
                        .outfitRegular(14)
                        .foregroundColor(.base)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 8)
                        .background(Color.base.opacity(0.1))
                        .cornerRadius(20)
                }
                .padding(.top, 16)
            }
        }
        .padding(.top, 80)
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
    
    // MARK: - Helper Methods
    
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

// MARK: - Preview
#Preview {
    LiveTVSearchView()
}