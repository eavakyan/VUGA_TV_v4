//
//  LiveTVHelpers.swift
//  Vuga
//
//  Helper components and utilities for Live TV functionality
//

import SwiftUI
import Foundation

// MARK: - Live TV Navigation Helper
extension Navigation {
    /// Navigate to Live TV search view
    static func pushToLiveTVSearch() {
        pushToSwiftUiView(LiveTVSearchView())
    }
    
    /// Navigate to Live TV category view
    static func pushToLiveTVCategory(_ category: LiveTVCategory) {
        pushToSwiftUiView(LiveTVCategoryView(category: category))
    }
    
    /// Navigate to main Live TV view
    static func pushToLiveTV() {
        pushToSwiftUiView(EnhancedLiveTVView())
    }
    
    /// Switch to Live TV tab
    static func switchToLiveTVTab() {
        NotificationCenter.default.post(name: .tabSelected, object: Tab.liveTV)
    }
}

// MARK: - Live TV Quick Access Button
struct LiveTVQuickAccessButton: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    var body: some View {
        Button(action: {
            Navigation.pushToLiveTV()
        }) {
            HStack(spacing: 8) {
                Image.tv
                    .resizeFitTo(size: 20, renderingMode: .template)
                    .foregroundColor(.white)
                
                Text("Live TV")
                    .outfitSemiBold(14)
                    .foregroundColor(.white)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(
                LinearGradient(
                    colors: [Color.red, Color.red.opacity(0.8)],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(20)
            .overlay(
                // Live indicator pulse effect
                HStack(spacing: 4) {
                    Circle()
                        .fill(Color.white)
                        .frame(width: 6, height: 6)
                        .scaleEffect(1.0)
                        .animation(
                            Animation.easeInOut(duration: 1.0).repeatForever(autoreverses: true),
                            value: UUID()
                        )
                    
                    Spacer()
                }
                .padding(.leading, 12),
                alignment: .leading
            )
        }
    }
}

// MARK: - Live TV Channel Badge
struct LiveTVChannelBadge: View {
    let channel: LiveTVChannel
    @AppStorage(SessionKeys.isPro) var isPro = false
    
    var body: some View {
        HStack(spacing: 6) {
            // Live indicator
            Circle()
                .fill(Color.red)
                .frame(width: 8, height: 8)
            
            Text("LIVE")
                .outfitSemiBold(10)
                .foregroundColor(.red)
            
            // Access type
            if !channel.isChannelFree && !isPro {
                if channel.isChannelPremium {
                    Image.crown
                        .resizeFitTo(size: 10, renderingMode: .template)
                        .foregroundColor(.rating)
                } else if channel.channelRequiresAds {
                    Image.adLcok
                        .resizeFitTo(size: 10, renderingMode: .template)
                        .foregroundColor(.orange)
                }
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(Color.black.opacity(0.7))
        .cornerRadius(12)
    }
}

// MARK: - Live TV Program Info Card
struct LiveTVProgramInfoCard: View {
    let channel: LiveTVChannel
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            // Current program
            if !channel.displayCurrentProgram.isEmpty && channel.displayCurrentProgram != "No program info" {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Now Playing")
                        .outfitSemiBold(11)
                        .foregroundColor(.textLight)
                        .textCase(.uppercase)
                    
                    Text(channel.displayCurrentProgram)
                        .outfitSemiBold(14)
                        .foregroundColor(.text)
                        .lineLimit(2)
                    
                    if let description = channel.currentProgramDescription, !description.isEmpty {
                        Text(description)
                            .outfitRegular(12)
                            .foregroundColor(.textLight)
                            .lineLimit(3)
                    }
                }
            }
            
            // Time slot
            if let startTime = channel.currentProgramStart, let endTime = channel.currentProgramEnd {
                HStack {
                    Image(systemName: "clock")
                        .resizeFitTo(size: 10, renderingMode: .template)
                        .foregroundColor(.textLight)
                    
                    Text("\(startTime) - \(endTime)")
                        .outfitRegular(11)
                        .foregroundColor(.textLight)
                }
            }
            
            // Next program
            if let nextProgram = channel.nextProgramTitle, !nextProgram.isEmpty {
                Divider()
                    .padding(.vertical, 4)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text("Up Next")
                        .outfitSemiBold(11)
                        .foregroundColor(.textLight)
                        .textCase(.uppercase)
                    
                    Text(nextProgram)
                        .outfitRegular(12)
                        .foregroundColor(.textLight)
                        .lineLimit(1)
                    
                    if let nextTime = channel.nextProgramStart {
                        Text("at \(nextTime)")
                            .outfitRegular(10)
                            .foregroundColor(.textLight)
                    }
                }
            }
        }
        .padding(12)
        .background(Color.stroke.opacity(0.05))
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color.stroke.opacity(0.1), lineWidth: 1)
        )
    }
}

// MARK: - Live TV Category Pill
struct LiveTVCategoryPill: View {
    let category: LiveTVCategory
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                // Category icon
                if let iconUrl = category.icon {
                    AsyncImage(url: URL(string: iconUrl.addBaseURL())) { image in
                        image
                            .resizable()
                            .renderingMode(.template)
                            .scaledToFit()
                    } placeholder: {
                        Circle()
                            .fill(Color.clear)
                    }
                    .frame(width: 16, height: 16)
                    .foregroundColor(isSelected ? .white : .text)
                }
                
                Text(category.displayName)
                    .outfitRegular(14)
                    .foregroundColor(isSelected ? .white : .text)
                
                // Channel count
                if let count = category.channelCount, count > 0 {
                    Text("\(count)")
                        .outfitRegular(12)
                        .foregroundColor(isSelected ? .white.opacity(0.8) : .textLight)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background((isSelected ? Color.white : Color.stroke).opacity(0.2))
                        .clipShape(Capsule())
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(
                isSelected ? Color.base : Color.stroke.opacity(0.1)
            )
            .cornerRadius(22)
            .overlay(
                RoundedRectangle(cornerRadius: 22)
                    .stroke(Color.stroke.opacity(isSelected ? 0 : 0.3), lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// MARK: - Live TV Empty State Views
struct LiveTVEmptyStateView: View {
    let title: String
    let message: String
    let actionTitle: String?
    let action: (() -> Void)?
    
    var body: some View {
        VStack(spacing: 20) {
            Image.tv
                .resizeFitTo(size: 80, renderingMode: .template)
                .foregroundColor(.textLight)
            
            VStack(spacing: 8) {
                Text(title)
                    .outfitSemiBold(20)
                    .foregroundColor(.text)
                
                Text(message)
                    .outfitRegular(16)
                    .foregroundColor(.textLight)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)
            }
            
            if let actionTitle = actionTitle, let action = action {
                Button(action: action) {
                    Text(actionTitle)
                        .outfitSemiBold(14)
                        .foregroundColor(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.base)
                        .cornerRadius(25)
                }
                .padding(.top, 16)
            }
        }
        .padding(40)
    }
}

// MARK: - Live TV Grid Layout Helper
struct LiveTVGridLayout {
    static func columns(for deviceType: UIUserInterfaceIdiom = UIDevice.current.userInterfaceIdiom) -> [GridItem] {
        let isTablet = deviceType == .pad
        let isLandscape = UIDevice.current.orientation.isLandscape
        
        let columnCount: Int
        if isTablet {
            columnCount = isLandscape ? 6 : 4
        } else {
            columnCount = isLandscape ? 3 : 2
        }
        
        return Array(repeating: GridItem(.flexible(), spacing: 16), count: columnCount)
    }
    
    static func adaptiveColumns(minWidth: CGFloat = 150) -> [GridItem] {
        return [GridItem(.adaptive(minimum: minWidth, maximum: .infinity), spacing: 16)]
    }
}

// MARK: - Live TV Settings Helper
struct LiveTVSettings {
    static func isLiveTVEnabled() -> Bool {
        // Check app settings or user preferences
        return SessionManager.shared.getAppSettings()?.settings?.liveTvEnable ?? true
    }
    
    static func shouldShowPremiumChannels() -> Bool {
        return SessionManager.shared.isPro()
    }
    
    static func canWatchChannel(_ channel: LiveTVChannel) -> Bool {
        let isPro = SessionManager.shared.isPro()
        return channel.isChannelFree || isPro
    }
}

// MARK: - Live TV Analytics Helper  
struct LiveTVAnalytics {
    static func trackChannelView(_ channel: LiveTVChannel) {
        // Track channel view for analytics
        LiveTVService.shared.trackChannelView(channelId: channel.channelId)
        
        // Could also integrate with Firebase Analytics or other services
        // Analytics.logEvent("live_tv_channel_view", parameters: [
        //     "channel_id": channel.channelId,
        //     "channel_name": channel.displayTitle,
        //     "access_type": channel.isChannelFree ? "free" : "premium"
        // ])
    }
    
    static func trackCategoryView(_ category: LiveTVCategory) {
        // Track category selection
        // Analytics.logEvent("live_tv_category_view", parameters: [
        //     "category_id": category.categoryId,
        //     "category_name": category.displayName
        // ])
    }
    
    static func trackSearch(_ query: String, resultsCount: Int) {
        // Track search queries
        // Analytics.logEvent("live_tv_search", parameters: [
        //     "search_query": query,
        //     "results_count": resultsCount
        // ])
    }
}