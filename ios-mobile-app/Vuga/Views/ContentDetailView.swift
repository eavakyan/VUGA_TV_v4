//
//  ContentDetailView.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 21/05/24.
//

import SwiftUI
import Kingfisher
import Marquee
import ExpandableText
import AVKit
import AVFoundation
import WebKit
import BranchSDK
import Flow
import WrappingStack
import MediaPlayer
import GoogleCast
import Combine

// These stub views have been replaced with the actual implementations in their respective files
// TrailerPlayerView.swift and TrailerInlinePlayer.swift

// Temporary rating components until import issue is resolved
struct RatingDisplayView: View {
    let rating: Double
    let userRating: Double?
    let onTap: (() -> Void)?
    
    var body: some View {
        HStack(spacing: 8) {
            // Average rating
            HStack(spacing: 4) {
                Image(systemName: "star.fill")
                    .resizable()
                    .frame(width: 16, height: 16)
                    .foregroundColor(.rating)
                Text(String(format: "%.1f", rating))
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
            }
            
            // User rating
            if let userRating = userRating {
                Text("•")
                    .foregroundColor(.gray)
                HStack(spacing: 4) {
                    Image(systemName: "person.fill")
                        .resizable()
                        .frame(width: 12, height: 12)
                        .foregroundColor(.primary)
                    Text(String(format: "%.1f", userRating))
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.primary)
                }
            } else {
                Text("•")
                    .foregroundColor(.gray)
                Text("Rate")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.primary)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(Color.black.opacity(0.3))
        .cornerRadius(20)
        .onTapGesture {
            onTap?()
        }
    }
}

struct RatingBottomSheet: View {
    @Binding var isPresented: Bool
    @Binding var currentRating: Double
    let contentTitle: String
    let contentType: ContentType
    let isEpisode: Bool
    let onSubmit: (Double) -> Void
    
    @State private var tempRating: Double = 0
    
    var body: some View {
        VStack(spacing: 20) {
            // Handle
            RoundedRectangle(cornerRadius: 3)
                .fill(Color.gray.opacity(0.5))
                .frame(width: 40, height: 5)
                .padding(.top, 10)
            
            // Title
            Text("Rate \(isEpisode ? "Episode" : contentType == .movie ? "Movie" : "TV Show")")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
            
            // Content Title
            Text(contentTitle)
                .font(.system(size: 16))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .lineLimit(2)
                .padding(.horizontal, 20)
            
            // Rating Stars (simplified for now)
            HStack(spacing: 4) {
                ForEach(1...5, id: \.self) { index in
                    Image(systemName: index <= Int(tempRating * 2) ? "star.fill" : "star")
                        .resizable()
                        .frame(width: 30, height: 30)
                        .foregroundColor(.yellow)
                        .onTapGesture {
                            tempRating = Double(index) / 2
                        }
                }
            }
            .padding(.vertical, 10)
            
            // Rating Value
            Text(String(format: "%.1f / 10", tempRating * 2))
                .font(.system(size: 24, weight: .medium))
                .foregroundColor(.primary)
            
            // Buttons
            HStack(spacing: 20) {
                Button(action: {
                    isPresented = false
                }) {
                    Text("Cancel")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.gray.opacity(0.3))
                        .cornerRadius(10)
                }
                
                Button(action: {
                    onSubmit(tempRating * 2)
                    isPresented = false
                }) {
                    Text("Submit")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.black)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.primary)
                        .cornerRadius(10)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 20)
        }
        .background(Color.bg)
        .cornerRadius(20, corners: [.topLeft, .topRight])
        .onAppear {
            tempRating = currentRating / 2
        }
    }
}

struct ContentDetailView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    @AppStorage(SessionKeys.hasShownAirPlayGuidance) var hasShownAirPlayGuidance = false
    @StateObject var vm = ContentDetailViewModel()
    var homeVm : HomeViewModel?
    var initialProgress: Double? = nil
    @State var showTrailerSheet = false
    @State private var currentIndex : Int = 0
    @State var episodeIncreaseTotalView = 0
    @State private var isAirPlayConnected = false
    @State private var showAirPlayGuidance = false
    @State private var airPlayObserver: Any?
    @State private var showDownloadProgress = false
    @State private var downloadingSource: Source?
    @State private var showDownloadStartedAlert = false
    @State private var downloadAlertMessage = ""
    @State private var showAgeRestrictionAlert = false
    @State private var ageRestrictionMessage = ""
    @State private var showRatingSheet = false
    @State private var currentUserRating: Double = 0
    @State private var showLoginAlert = false
    @State private var shouldPlayTrailer = true
    @State private var showEpisodeRatingSheet = false
    @State private var currentEpisodeRating: Double = 0
    @State private var selectedEpisodeForRating: Episode?
    @State private var isTrailerMuted = true
    @State private var dragOffset: CGSize = .zero
    @State private var isDragging = false
    @EnvironmentObject var downloadViewModel: DownloadViewModel
    var contentId: Int?
    
    // Helper function to simplify ad visibility logic
    private func shouldShowAd(isPro: Bool, isShowAd: Bool) -> Bool {
        return isPro || SessionManager.shared.getSetting()?.isCustomIos == 0 ? false : isShowAd
    }
    
    // Helper function for AdMob visibility
    private var shouldShowAdMob: Bool {
        return !isPro && SessionManager.shared.getSetting()?.isAdmobIos != 0
    }
    
    // Helper function to check if content has trailers available
    private func hasTrailersAvailable(for content: VugaContent) -> Bool {
        // Check new trailers array
        if let trailers = content.trailers, !trailers.isEmpty {
            return true
        }
        
        // Check legacy trailer URL
        if let trailerURL = content.trailerURL, !trailerURL.isEmpty {
            return true
        }
        
        return false
    }
    
    // Computed properties to avoid complex expressions
    private var posterWidth: CGFloat { Device.width * 0.9 }
    private var posterHeight: CGFloat { Device.width * 1.1 }
    private var posterWidthSmall: CGFloat { Device.width * 0.75 }
    private var pagingMargin: CGFloat { Device.width * 0.33 }
    private var pagingPadding: CGFloat { Device.width * 0.35 }
    private var cardWidth: CGFloat { Device.width * 0.60 }
    private var fullWidth: CGFloat { Device.width }
    private var fullHeight: CGFloat { Device.width * 1.2 }
    private var smallPosterWidth: CGFloat { Device.width * 0.5 }
    private var isArabic: Bool { language == .Arabic }
    
    // Store contentId for loading
    private let contentIdToLoad: Int?
    
    // Initializers
    init() {
        self.contentIdToLoad = nil
    }
    
    init(contentId: Int) {
        self.contentIdToLoad = contentId
    }
    
    init(contentId: Int?) {
        self.contentIdToLoad = contentId
    }
    
    init(content: VugaContent) {
        self.contentIdToLoad = content.id
    }
    
    init(homeVm: HomeViewModel? = nil, contentId: Int) {
        self.homeVm = homeVm
        self.contentIdToLoad = contentId
    }
    
    init(initialProgress: Double? = nil, contentId: Int) {
        self.initialProgress = initialProgress
        self.contentIdToLoad = contentId
    }
    
    init(initialProgress: Double? = nil, homeVm: HomeViewModel? = nil, contentId: Int) {
        self.initialProgress = initialProgress
        self.homeVm = homeVm
        self.contentIdToLoad = contentId
    }
    
    var body: some View {
        ZStack {
            // Black background
            Color.black
                .ignoresSafeArea()
            
            mainContent
                .offset(y: dragOffset.height)
                .opacity(isDragging ? 0.8 : 1.0)
                .animation(.interactiveSpring(), value: dragOffset)
        }
        .navigationBarHidden(true)
        .loaderView(vm.isLoading && vm.content != nil) // Only show loader overlay when content exists
        .onAppear(perform: onAppearHandler)
        .onDisappear(perform: onDisappearHandler)
        .fullScreenCover(item: $vm.selectedSource, content: videoPlayerContent)
        .fullScreenCover(isPresented: $showTrailerSheet, content: trailerContent)
        .customAlert(isPresented: $vm.isShowPremiumDialog) { premiumAlert() }
        .customAlert(isPresented: $vm.isShowAdDialog) { adAlert() }
        .sheet(isPresented: $showRatingSheet) { ratingSheetContent() }
        .sheet(isPresented: $showEpisodeRatingSheet) { episodeRatingSheetContent() }
        .sheet(isPresented: $vm.showDistributorSubscriptionRequired) { subscriptionRequiredContent() }
        .onAppear(perform: onAppearAirPlayHandler)
        .onDisappear(perform: onDisappearAirPlayHandler)
        .gesture(
            DragGesture()
                .onChanged { value in
                    // Only allow downward dragging
                    if value.translation.height > 0 {
                        isDragging = true
                        dragOffset = value.translation
                    }
                }
                .onEnded { value in
                    isDragging = false
                    // If dragged down more than 150 points, dismiss the view
                    if value.translation.height > 150 {
                        withAnimation(.easeOut(duration: 0.3)) {
                            dragOffset.height = UIScreen.main.bounds.height
                        }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                            Navigation.pop()
                        }
                    } else {
                        // Snap back to original position
                        withAnimation(.interactiveSpring()) {
                            dragOffset = .zero
                        }
                    }
                }
        )
    }
    
    private var mainContent: some View {
        VStack(spacing: 0) {
            // Show back button even when loading
            if vm.content == nil {
                HStack {
                    Button(action: {
                        Navigation.pop()
                    }) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 20, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 44, height: 44)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    }
                    .padding(.top, 50)
                    .padding(.leading, 16)
                    
                    Spacer()
                }
                
                // Show loading or error state
                if vm.isLoading {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .scaleEffect(1.5)
                    Text("Loading content...")
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                        .padding(.top, 10)
                    Spacer()
                } else {
                    Spacer()
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 48))
                            .foregroundColor(.white.opacity(0.5))
                        Text("Failed to load content")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(.white)
                        Button(action: {
                            if let idToLoad = contentIdToLoad ?? contentId {
                                vm.fetchContest(contentId: idToLoad)
                            }
                        }) {
                            Text("Try Again")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.black)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 12)
                                .background(Color.white)
                                .cornerRadius(8)
                        }
                    }
                    Spacer()
                }
            } else if let content = vm.content {
                // Full-width trailer player at top with overlay controls
                ZStack(alignment: .topLeading) {
                    // Trailer player
                    trailerPlayerSection(content)
                        .frame(height: AdaptiveContentSizing.featuredContentSize().width * 9/16)
                    
                    // Overlay back button
                    Button(action: {
                        Navigation.pop()
                    }) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 20, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 44, height: 44)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    }
                    .padding(.top, 50)
                    .padding(.leading, 16)
                }
                
                // Scrollable content below trailer
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 12) {
                        // Title and info section
                        contentInfoSection(content)
                        
                        // Action buttons row
                        actionButtonsSection(content)
                        
                        // Icon buttons row
                        iconButtonsSection(content)
                        
                        // Story with expandable MORE button
                        if let description = content.description, !description.isEmpty {
                            ExpandableDescriptionView(description: description)
                                .padding(.horizontal, 16)
                        }
                        
                        // Cast section (3x2 grid)
                        if let cast = content.contentCast, !cast.isEmpty {
                            castSection(cast)
                        }
                        
                        // Episodes section (for TV shows)
                        if content.type == .series {
                            episodesSection
                        }
                        
                        // More Like This section
                        if let moreLikeThis = content.moreLikeThis, !moreLikeThis.isEmpty {
                            moreLikeThisSection(moreLikeThis)
                        }
                    }
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 20)
                .animation(.linear, value: vm.isStoryLineOn)
                .animation(.linear, value: vm.isStarCastOn)
            }
        }
    }
    
    private var headerView: some View {
        VStack(spacing: 0) {
            HStack {
                BackButton()
                Spacer()
                if vm.content != nil {
                    CommonIcon(image: vm.isBookmarked ? .bookmarkFill : .bookmark, onTap: {
                        Function.shared.haptic()
                        vm.toogleBookmark(homeVm: homeVm)
                    })
                    CommonIcon(image: .share) {
                        Function.shared.haptic()
                        shareContent()
                    }
                    // Removed duplicate cast buttons - now only in episodes section
                }
            }
            .padding([.horizontal, .top],15)
        }
    }
    
    private func onAppearHandler() {
        if !vm.isDataLoaded {
            // Use contentIdToLoad from initializer, or contentId if set directly
            let idToLoad = contentIdToLoad ?? contentId ?? 0
            print("ContentDetailView: Loading content with ID: \(idToLoad)")
            
            // Dispatch async to prevent UI freeze
            DispatchQueue.main.async {
                self.vm.fetchContest(contentId: idToLoad)
            }
            
            // Add timeout protection - stop loading after 15 seconds
            DispatchQueue.main.asyncAfter(deadline: .now() + 15) {
                if self.vm.isLoading {
                    print("ContentDetailView: Timeout loading content \(idToLoad)")
                    self.vm.stopLoading()
                }
            }
        }
        // Watchlist state is now set from server response in fetchContest
        if shouldShowAdMob {
            Interstitial.shared.loadInterstitial()
        }
        // Resume trailer playback when returning to content detail screen
        shouldPlayTrailer = true
        // Apply initial progress if provided (e.g., from Recently Watched)
        if let p = initialProgress, p > 0 {
            vm.progress = p
        }
    }
    
    private func onDisappearHandler() {
        if shouldShowAdMob {
            Interstitial.shared.showInterstitialAds()
        }
    }
    
    private func videoPlayerContent(source: Source) -> some View {
        Group {
            if source.type?.rawValue ?? 1 == 1 {
                YoutubeView(youtubeUrl: source.source ?? "")
            } else {
                VideoPlayerView(
                    content: vm.content,
                    episode: vm.selectedEpisode,
                    type: source.type?.rawValue ?? 2,
                    isShowAdView: self.shouldShowAd(isPro: isPro, isShowAd: vm.isShowAd),
                    url: source.sourceURL.absoluteString,
                    progress: vm.progress,
                    sourceId: vm.selectedSource?.id
                )
            }
        }
    }
    
    @ViewBuilder
    private func trailerContent() -> some View {
        if let content = vm.content {
            if content.type == .movie {
                // For movies, use content's trailers
                if hasTrailersAvailable(for: content) {
                    SimpleTrailerView(content: content)
                }
            } else if content.type == .series {
                // For series, use content's trailers or season trailers
                if hasTrailersAvailable(for: content) {
                    SimpleTrailerView(content: content)
                } else if let selectedSeason = vm.selectedSeason,
                         let seasonTrailerUrl = selectedSeason.trailerURL, 
                         !seasonTrailerUrl.isEmpty {
                    SimpleTrailerView(trailerUrl: getFullTrailerUrl(seasonTrailerUrl))
                }
            }
        }
    }
    
    private func premiumAlert() -> some View {
        DialogCard(icon: Image.crown, title: .subScribeToPro, iconColor: .rating, subTitle: .proDialogDes, buttonTitle: .becomeAPro, onClose: {
            vm.isShowPremiumDialog = false
        },onButtonTap: {
            vm.isShowPremiumDialog = false
            Navigation.pushToSwiftUiView(ProView())
        })
    }
    
    private func adAlert() -> some View {
        DialogCard(icon: Image.adLcok, title: .unlokeWithAd, subTitle: .adDialogDes, buttonTitle: .watchAd, onClose: {
            vm.isShowAdDialog = false
        },onButtonTap: {
            vm.isShowAdDialog = false
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                // Show ad and then play video
                if let pickedSource = vm.pickedSource {
                    // After ad is watched, play the video
                    // Don't reset progress - use the existing vm.progress value
                    vm.playSource(pickedSource)
                    vm.isShowAd = false // Don't show ad again in video player
                    if vm.content?.type == .movie {
                        vm.increaseContentView(contentId: vm.content?.id ?? 0)
                    } else if let episode = vm.selectedEpisode {
                        vm.increaseEpisodeView(episodeId: episode.id ?? 0)
                        episodeIncreaseTotalView += 1
                    }
                }
            }
        })
    }
    
    @ViewBuilder
    private func ratingSheetContent() -> some View {
        if let content = vm.content {
            RatingBottomSheet(
                isPresented: $showRatingSheet,
                currentRating: .constant(content.userRating ?? 0),
                contentTitle: content.title ?? "",
                contentType: content.type ?? .movie,
                isEpisode: false,
                onSubmit: { rating in
                    vm.submitRating(rating: rating)
                }
            )
        }
    }
    
    @ViewBuilder
    private func episodeRatingSheetContent() -> some View {
        if let episode = selectedEpisodeForRating {
            RatingBottomSheet(
                isPresented: $showEpisodeRatingSheet,
                currentRating: $currentEpisodeRating,
                contentTitle: episode.title ?? "",
                contentType: .series,
                isEpisode: true,
                onSubmit: { rating in
                    vm.submitEpisodeRating(episode: episode, rating: rating)
                }
            )
        }
    }
    
    @ViewBuilder
    private func subscriptionRequiredContent() -> some View {
        // Temporary simplified subscription required view
        VStack(spacing: 20) {
            Text("Subscription Required")
                .outfitSemiBold(22)
                .foregroundColor(.white)
                .padding(.top, 40)
            
            Text("This content requires a \(vm.content?.distributorName ?? "premium") subscription")
                .outfitRegular(16)
                .foregroundColor(.textLight)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            Button(action: {
                vm.showDistributorSubscriptionRequired = false
                Navigation.pushToSwiftUiView(SubscriptionsView())
            }) {
                Text("View Subscription Options")
                    .outfitSemiBold(16)
                    .foregroundColor(.black)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(Color.base)
                    .cornerRadius(25)
            }
            .padding(.horizontal, 20)
            
            Button(action: {
                vm.showDistributorSubscriptionRequired = false
            }) {
                Text("Maybe Later")
                    .outfitMedium(16)
                    .foregroundColor(.white)
            }
            .padding(.bottom, 20)
            
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color("bgColor"))
    }
    
    private func onAppearAirPlayHandler() {
        // Monitor AirPlay route changes
        airPlayObserver = NotificationCenter.default.addObserver(
            forName: AVAudioSession.routeChangeNotification,
            object: nil,
            queue: .main
        ) { notification in
            if let userInfo = notification.userInfo,
               let reasonValue = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
               let reason = AVAudioSession.RouteChangeReason(rawValue: reasonValue) {
                // Handle route changes if needed
                print("Route change reason: \(reason)")
            }
        }
    }
    
    private func onDisappearAirPlayHandler() {
        // Clean up AirPlay observer
        if let observer = airPlayObserver {
            NotificationCenter.default.removeObserver(observer)
            airPlayObserver = nil
        }
    }
    
    // Helper function to get user-friendly display rating
    private func getDisplayRating(for code: String) -> String {
        switch code {
        case "AG_0_6": return "All Ages"
        case "AG_7_12": return "7+"
        case "AG_13_16": return "13+"
        case "AG_17_18": return "17+"
        case "AG_18_PLUS": return "18+"
        case "NR": return "Not Rated"
        default: return code
        }
    }
    
    // MARK: - Content Detail Section
    private func contentDetailSection(for content: VugaContent) -> some View {
        VStack(spacing: 12) {
            contentPosterSection(content)
            contentInfoSection(content)
            contentRatingSection(content)
            if content.type == .movie {
                movieActionButtons(content)
            }
            // Continue section appears when opened from Recently Watched
            if vm.progress > 0 {
                VStack(spacing: 8) {
                    HStack(spacing: 12) {
                        PlayButton(size: 24)
                        Text("Continue Watching")
                            .outfitRegular(18)
                    }
                    .padding(10)
                    .maxWidthFrame()
                    .background(Color(hexString: "511B1B"))
                    .cornerRadius(12)
                    .addStroke(radius: 12, color: .base.opacity(0.3))
                    .onTap {
                        // Resume from vm.progress
                        handlePlayAction(content)
                    }
                    
                    HStack(spacing: 12) {
                        Image.clock
                            .resizeFitTo(size: 18)
                        Text("Watch from Start")
                            .outfitRegular(18)
                    }
                    .padding(10)
                    .maxWidthFrame()
                    .background(Color.bg)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color.text.opacity(0.3), lineWidth: 1)
                    )
                    .cornerRadius(12)
                    .onTap {
                        vm.progress = 0
                        handlePlayAction(content)
                    }
                }
                .padding(.top, 4)
            }
            if shouldShowAdMob {
                BannerAd()
                    .padding(.horizontal,-10)
                    .padding(.top,10)
            }
        }
    }
    
    @ViewBuilder
    private func contentPosterSection(_ content: VugaContent) -> some View {
        if hasTrailersAvailable(for: content) {
            // Auto-playing trailer at top (no poster below)
            if let trailerUrl = getEffectiveTrailerUrl(for: content) {
                GeometryReader { geometry in
                    let trailerSize = AdaptiveContentSizing.featuredContentSize(for: geometry)
                    // Use 16:9 aspect ratio for video
                    let trailerWidth = trailerSize.width
                    let trailerHeight = trailerWidth * 9/16
                    
                    InlineTrailerView(trailerUrl: trailerUrl, shouldPlay: $shouldPlayTrailer)
                        .frame(width: trailerWidth, height: trailerHeight)
                        .cornerRadius(15)
                        .clipped()
                        .position(x: geometry.size.width / 2, y: trailerHeight / 2)
                }
                .frame(height: AdaptiveContentSizing.featuredContentSize().width * 9/16)
                    .addStroke(radius: 15)
            }
        } else {
            // Show full-size poster when no trailer exists
            KFImage(content.verticalPoster?.addBaseURL())
                .resizeFillTo(width: posterWidth, height: posterHeight, radius: 15)
                .addStroke(radius: 15)
                .maxFrame()
        }
    }
    
    // Helper function to get effective trailer URL - ONLY uses trailer_url field
    private func getEffectiveTrailerUrl(for content: VugaContent) -> String? {
        // Only use the trailer_url field from content_trailer table
        
        // Check primary trailer first
        if let primaryTrailer = content.primaryTrailer,
           let trailerUrl = primaryTrailer.trailerUrl,
           !trailerUrl.isEmpty {
            return trailerUrl
        }
        
        // Check any trailer in the array
        if let trailers = content.trailers {
            for trailer in trailers {
                if let trailerUrl = trailer.trailerUrl, !trailerUrl.isEmpty {
                    return trailerUrl
                }
            }
        }
        
        // No trailer URL found
        return nil
    }
    
    @ViewBuilder
    private func contentInfoSection(_ content: VugaContent) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            // Title (larger font)
            Text(content.title ?? "")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            // Info line: Year, Rating, Duration, Quality, CC
            HStack(spacing: 8) {
                // Release year
                if let year = content.releaseYear {
                    Text(String(year))
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                }
                
                // Rating badge
                if let ageRating = content.ageRating {
                    Text(ageRating)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.white.opacity(0.2))
                        .cornerRadius(4)
                }
                
                // Duration
                if let durationStr = content.duration, let duration = Int(durationStr) {
                    let hours = duration / 3600
                    let minutes = (duration % 3600) / 60
                    let durationText = hours > 0 ? "\(hours)h \(minutes)m" : "\(minutes)m"
                    Text(durationText)
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                }
                
                // Quality
                Text("HD")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .overlay(
                        RoundedRectangle(cornerRadius: 4)
                            .stroke(Color.white.opacity(0.3), lineWidth: 1)
                    )
                
                // Subtitles available
                if let subtitles = content.contentSubtitles, !subtitles.isEmpty {
                    Image(systemName: "captions.bubble")
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                }
                
                Spacer()
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
    }
    
    @ViewBuilder
    private func contentInfoSectionOld(_ content: VugaContent) -> some View {
        VStack(spacing: 5) {
            Text(content.title ?? "")
                .outfitSemiBold(20)
                .foregroundColor(.text)
                .multilineTextAlignment(.center)
                .padding(.top, 5)
            
            WrappingHStack(id: \.self,horizontalSpacing: 8) {
                ForEach(content.genres.indices, id: \.self) { index in
                    HStack(alignment: .center) {
                        Text(content.genres[index].title ?? "")
                            .outfitLight()
                            .foregroundColor(.textLight)
                        if content.genres.count != index + 1{
                            Circle()
                                .fill(.base.opacity(0.6))
                                .frame(width: 5,height: 5)
                        }
                    }
                }
            }
        }
    }
    
    @ViewBuilder
    private func contentRatingSection(_ content: VugaContent) -> some View {
        HStack(spacing: 10) {
            // Rating Display with user rating
            RatingDisplayView(
                rating: content.ratings ?? 0,
                userRating: content.userRating,
                onTap: {
                    if vm.myUser != nil {
                        currentUserRating = content.userRating ?? 0
                        showRatingSheet = true
                    } else {
                        showLoginAlert = true
                    }
                }
            )
            
            verticalDivider
            
            Text(String(content.releaseYear ?? 0))
                .outfitLight()
                .foregroundColor(.textLight)
            
            // Age Rating Badge
            if content.ageRatingCode != "NR" {
                verticalDivider
                AgeRatingBadgeView(
                    ageRatingCode: content.ageRating ?? content.ageRatingCode,
                    minAge: content.minAge
                )
            }
            
            if content.type == .movie {
                verticalDivider
                HStack {
                    Image.clock
                        .resizeFitTo(size: 16)
                    Text(content.duration ?? "")
                }
            }
        }
        .outfitRegular()
        .foregroundColor(.textLight)
        .padding(.vertical, 5)
    }
    
    @ViewBuilder
    private func movieActionButtons(_ content: VugaContent) -> some View {
        VStack(spacing: 16) {
            // Start Watching (always shown)
            VStack(spacing: 6) {
                ZStack {
                    Circle().fill(Color.gray.opacity(0.25))
                        .frame(width: 64, height: 64)
                    Image(systemName: "play.fill")
                        .font(.system(size: 26, weight: .bold))
                        .foregroundColor(.white)
                }
                Text("Start Watching")
                    .outfitRegular(16)
                    .foregroundColor(.white)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
            .background(Color.gray.opacity(0.2))
            .cornerRadius(14)
            .addStroke(radius: 14, color: .white.opacity(0.2))
            .onTap { handlePlayAction(content) }

            // Continue Watching (only if progress)
            if vm.progress > 0 {
                VStack(spacing: 6) {
                    ZStack {
                        Circle().fill(Color.gray.opacity(0.18))
                            .frame(width: 64, height: 64)
                        Circle()
                            .trim(from: 0, to: CGFloat(min(max(vm.progress, 0), 1)))
                            .stroke(Color.white, style: StrokeStyle(lineWidth: 4, lineCap: .round))
                            .rotationEffect(.degrees(-90))
                            .frame(width: 64, height: 64)
                        Image(systemName: "play.fill")
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(.white)
                    }
                    Text("Continue Watching")
                        .outfitRegular(16)
                        .foregroundColor(.white)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(Color.gray.opacity(0.2))
                .cornerRadius(14)
                .addStroke(radius: 14, color: .white.opacity(0.2))
                .onTap { handlePlayAction(content) }
            }

            // Download button
            if content.contentSources?.first?.isDownload == 1 {
                HStack(spacing: 12) {
                    Image.download
                        .resizeFitTo(size: 20, renderingMode: .template)
                        .foregroundColor(.text)
                    Text(String.download.localized(language))
                        .outfitRegular(20)
                }
                .padding(10)
                .maxWidthFrame()
                .background(Color.bg)
                .overlay(
                    RoundedRectangle(cornerRadius: 15)
                        .stroke(Color.text.opacity(0.3), lineWidth: 1)
                )
                .cornerRadius(15)
                .onTap {
                    handleDownload()
                }
            }
        }
    }
    
    // MARK: - New Redesigned Sections
    
    @ViewBuilder
    private func trailerPlayerSection(_ content: VugaContent) -> some View {
        GeometryReader { geometry in
            ZStack {
                if let trailerUrl = getEffectiveTrailerUrl(for: content) {
                    let trailerSize = AdaptiveContentSizing.featuredContentSize(for: geometry)
                    // Use 16:9 aspect ratio for video
                    let trailerWidth = trailerSize.width
                    let trailerHeight = trailerWidth * 9/16
                    
                    MutedTrailerView(
                        trailerUrl: trailerUrl,
                        shouldPlay: $shouldPlayTrailer,
                        isMuted: $isTrailerMuted
                    )
                    .frame(width: trailerWidth, height: trailerHeight)
                    .position(x: geometry.size.width / 2, y: geometry.size.height / 2)
                } else {
                    // Fallback poster if no trailer
                    KFImage(URL(string: content.horizontalPoster ?? content.verticalPoster ?? ""))
                        .resizable()
                        .aspectRatio(16/9, contentMode: .fill)
                        .frame(width: geometry.size.width, height: geometry.size.width * 9/16)
                        .clipped()
                }
            }
        }
        .frame(height: UIScreen.main.bounds.width * 9/16)
    }
    
    @ViewBuilder
    private func actionButtonsSection(_ content: VugaContent) -> some View {
        // Only show play/download buttons for movies, not TV shows
        if content.type == .movie {
            VStack(spacing: 12) {
                if vm.progress > 0 {
                    // Content has been watched before - show Resume and Start from Beginning on same line
                    HStack(spacing: 12) {
                        // Resume button
                        Button(action: {
                            handlePlayAction(content)
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: "play.fill")
                                    .font(.system(size: 14))
                                Text("Resume")
                                    .font(.system(size: 14, weight: .medium))
                            }
                            .foregroundColor(.black)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.white)
                            .cornerRadius(8)
                        }
                        
                        // Start from Beginning button
                        Button(action: {
                            vm.progress = 0
                            handlePlayAction(content)
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: "arrow.counterclockwise")
                                    .font(.system(size: 14))
                                Text("Start Over")
                                    .font(.system(size: 14, weight: .medium))
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.white.opacity(0.2))
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.white.opacity(0.3), lineWidth: 1)
                            )
                            .cornerRadius(8)
                        }
                    }
                } else {
                    // Content has not been watched - show Play button
                    Button(action: {
                        handlePlayAction(content)
                    }) {
                        HStack {
                            Image(systemName: "play.fill")
                            Text("Play")
                        }
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.black)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.white)
                        .cornerRadius(8)
                    }
                }
                
                // Download button (if available) - full width on its own row
                if content.contentSources?.first?.isDownload == 1 {
                    Button(action: {
                        handleDownload()
                    }) {
                        HStack {
                            Image(systemName: "arrow.down.circle")
                            Text("Download")
                        }
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.white.opacity(0.2))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.white.opacity(0.3), lineWidth: 1)
                        )
                        .cornerRadius(8)
                    }
                }
            }
            .padding(.horizontal, 16)
        }
    }
    
    @ViewBuilder
    private func iconButtonsSection(_ content: VugaContent) -> some View {
        HStack(spacing: 0) {
            // Rating
            Button(action: {
                if vm.myUser != nil {
                    currentUserRating = content.userRating ?? 0
                    showRatingSheet = true
                } else {
                    showLoginAlert = true
                }
            }) {
                VStack(spacing: 4) {
                    Image(systemName: "star.fill")
                        .font(.system(size: 20))
                        .foregroundColor(.white)
                        .frame(width: 24, height: 24)
                    Text(content.userRating != nil ? String(format: "%.1f", content.userRating!) : "Rate")
                        .font(.system(size: 10))
                        .foregroundColor(.white.opacity(0.7))
                        .frame(height: 14)
                }
                .frame(maxWidth: .infinity)
            }
            
            // Watchlist
            Button(action: {
                Function.shared.haptic()
                vm.toogleBookmark(homeVm: homeVm)
            }) {
                VStack(spacing: 4) {
                    Image(systemName: vm.isBookmarked ? "checkmark" : "plus")
                        .font(.system(size: 20))
                        .foregroundColor(.white)
                        .frame(width: 24, height: 24)
                    Text("Watchlist")
                        .font(.system(size: 10))
                        .foregroundColor(.white.opacity(0.7))
                        .frame(height: 14)
                }
                .frame(maxWidth: .infinity)
            }
            
            // Share
            Button(action: {
                Function.shared.haptic()
                shareContent()
            }) {
                VStack(spacing: 4) {
                    Image(systemName: "square.and.arrow.up")
                        .font(.system(size: 20))
                        .foregroundColor(.white)
                        .frame(width: 24, height: 24)
                    Text("Share")
                        .font(.system(size: 10))
                        .foregroundColor(.white.opacity(0.7))
                        .frame(height: 14)
                }
                .frame(maxWidth: .infinity)
            }
            
            // Google Cast with label
            Button(action: {
                // Google Cast button has its own tap handling
            }) {
                VStack(spacing: 4) {
                    GoogleCastButton(viewModel: vm)
                        .frame(width: 24, height: 24)
                    Text("Cast")
                        .font(.system(size: 10))
                        .foregroundColor(.white.opacity(0.7))
                        .frame(height: 14)
                }
                .frame(maxWidth: .infinity)
            }
            .allowsHitTesting(true)
            
            // AirPlay with label
            VStack(spacing: 4) {
                ZStack {
                    // Custom AirPlay icon as fallback
                    Image(systemName: "airplayaudio")
                        .font(.system(size: 20, weight: .medium))
                        .foregroundColor(.white)
                        .frame(width: 24, height: 24)
                    
                    // Actual AirPlay picker on top
                    AirPlayRoutePickerView(isConnected: isAirPlayConnected)
                        .frame(width: 24, height: 24)
                        .opacity(0.01) // Make nearly invisible but still tappable
                }
                Text("AirPlay")
                    .font(.system(size: 10))
                    .foregroundColor(.white.opacity(0.7))
                    .frame(height: 14)
            }
            .frame(maxWidth: .infinity)
        }
        .padding(.horizontal, 16)
    }
    
    @ViewBuilder
    private func castSection(_ cast: [Cast]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Cast")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)
                .padding(.horizontal, 16)
            
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHGrid(rows: [GridItem(.fixed(85)), GridItem(.fixed(85))], spacing: 8) {
                    ForEach(cast.prefix(12), id: \.id) { member in
                        VStack(spacing: 2) {
                            KFImage(URL(string: member.actor?.profile_image ?? ""))
                                .placeholder {
                                    Image(systemName: "person.circle.fill")
                                        .resizable()
                                        .foregroundColor(.gray)
                                }
                                .resizable()
                                .aspectRatio(1, contentMode: .fill)
                                .frame(width: 60, height: 60)
                                .clipShape(Circle())
                            
                            Text(member.actor?.fullname ?? "")
                                .font(.system(size: 10))
                                .foregroundColor(.white)
                                .lineLimit(1)
                                .frame(width: 70)
                        }
                        .onTapGesture {
                            Navigation.pushToSwiftUiView(CastView(actorId: member.actorID ?? 0))
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }
    
    @ViewBuilder
    private func moreLikeThisSection(_ movies: [VugaContent]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("More Like This")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)
                .padding(.horizontal, 16)
            
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 12) {
                    ForEach(movies.prefix(10), id: \.id) { movie in
                        VStack(spacing: 0) {
                            KFImage(URL(string: movie.verticalPoster ?? movie.horizontalPoster ?? ""))
                                .placeholder {
                                    Rectangle()
                                        .fill(Color.gray.opacity(0.3))
                                }
                                .resizable()
                                .aspectRatio(2/3, contentMode: .fill)
                                .frame(width: 120, height: 180)
                                .clipped()
                                .cornerRadius(8)
                        }
                        .onTapGesture {
                            if let movieId = movie.id {
                                Navigation.pushToSwiftUiView(ContentDetailView(contentId: movieId))
                            }
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
        .padding(.bottom, 12)
    }
    
    @ViewBuilder
    private var episodesSection: some View {
        if let content = vm.content {
            VStack(spacing: 0) {
                SeasonTags(vm: vm, content: content)
                
                if let selectedSeason = vm.selectedSeason,
                   let episodes = selectedSeason.episodes {
                    ForEach(episodes, id: \.id) { episode in
                        episodeCard(for: episode, content: content)
                    }
                }
            }
        }
    }
    
    private func episodeCard(for episode: Episode, content: VugaContent) -> some View {
        EpisodeCard(
            episode: episode,
            episodeTotalView: calculateEpisodeViews(for: episode),
            onRatingTap: {
                handleEpisodeRatingTap(for: episode)
            },
            isInWatchlist: vm.episodeWatchlistStatus[episode.id ?? 0] ?? false,
            onWatchlistTap: {
                if let episodeId = episode.id {
                    vm.toggleEpisodeWatchlist(episodeId: episodeId)
                }
            }
        )
        .onTapGesture {
            handleEpisodeTap(episode: episode, content: content)
        }
    }
    
    private func calculateEpisodeViews(for episode: Episode) -> Int {
        let baseViews = episode.totalView ?? 0
        let additionalViews = (episode.id == vm.selectedEpisode?.id) ? episodeIncreaseTotalView : 0
        return baseViews + additionalViews
    }
    
    private func handleEpisodeRatingTap(for episode: Episode) {
        if vm.myUser != nil {
            selectedEpisodeForRating = episode
            currentEpisodeRating = episode.userRating ?? 0
            showEpisodeRatingSheet = true
        } else {
            showLoginAlert = true
        }
    }
    
    private func handleEpisodeTap(episode: Episode, content: VugaContent) {
        // Stop trailer playback when episode is clicked
        shouldPlayTrailer = false
        
        // Navigate to Episode Detail View
        Navigation.pushToSwiftUiView(
            EpisodeDetailView(
                episode: episode,
                seriesContent: content
            )
        )
    }
    
    private func handlePlayAction(_ content: VugaContent) {
        // Stop trailer playback immediately when Watch Now is clicked
        shouldPlayTrailer = false
        
        // Check age restrictions first
        let currentProfile = SessionManager.shared.getCurrentProfile()
        if !content.isAppropriateFor(profile: currentProfile) {
            displayAgeRestrictionAlert()
            return
        }
        
        // Check distributor subscription requirement
        if let distributorId = content.contentDistributorId,
           distributorId > 0,
           content.distributorRequiresSubscription == true,
           content.userHasDistributorAccess != true {
            // Show subscription required sheet
            vm.showDistributorSubscriptionRequired = true
            return
        }
        
        // Direct play without source selection
        if let sources = content.contentSources, !sources.isEmpty {
            let firstSource = sources[0]
            if firstSource.accessType == .free || isPro {
                // Reduced delay to minimize audio overlap
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    vm.pickedSource = firstSource
                    // Don't reset progress - use the existing vm.progress value which contains the saved position
                    // vm.progress already contains the correct value from recently watched
                    vm.playSource(firstSource)
                    if content.type == .movie {
                        vm.increaseContentView(contentId: content.id ?? 0)
                    }
                }
            } else if firstSource.accessType == .premium {
                vm.pickedSource = firstSource
                vm.isShowPremiumDialog = true
            } else if firstSource.accessType == .locked {
                vm.pickedSource = firstSource
                vm.isShowAdDialog = true
            }
        }
    }
    
    func handleDownload() {
        guard let content = vm.content,
              let sources = content.contentSources,
              !sources.isEmpty else { return }
        
        // Check age restrictions first
        let currentProfile = SessionManager.shared.getCurrentProfile()
        if !content.isAppropriateFor(profile: currentProfile) {
            displayAgeRestrictionAlert()
            return
        }
        
        let firstSource = sources[0]
        let downloadId = firstSource.sourceDownloadId(contentType: content.type ?? .movie)
        
        // Check if already downloading
        if let existingDownload = downloadViewModel.downloadingContents[downloadId] {
            switch existingDownload.downloadStatus {
            case .downloading:
                downloadAlertMessage = "\(content.title ?? "This content") is already downloading. Progress: \(Int(existingDownload.progress * 100))%"
                showDownloadStartedAlert = true
                return
            case .downloaded:
                downloadAlertMessage = "\(content.title ?? "This content") has already been downloaded."
                showDownloadStartedAlert = true
                return
            case .queued:
                downloadAlertMessage = "\(content.title ?? "This content") is in the download queue."
                showDownloadStartedAlert = true
                return
            default:
                break
            }
        }
        
        // Check if user has access
        if firstSource.accessType == .premium && !isPro {
            vm.pickedSource = firstSource
            vm.isShowPremiumDialog = true
            return
        } else if firstSource.accessType == .locked && !isPro {
            vm.pickedSource = firstSource
            vm.isShowAdDialog = true
            return
        }
        
        // Check storage before downloading
        let sizeInMB = Int(firstSource.size ?? "500") ?? 500
        let estimatedSize: Int64 = Int64(sizeInMB) * 1024 * 1024 // Convert MB to bytes
        
        if !StorageManager.shared.hasEnoughStorage(for: estimatedSize) {
            downloadAlertMessage = "Insufficient storage space. Please free up some space to download this content."
            showDownloadStartedAlert = true
            return
        }
        
        // Show download progress dialog
        downloadingSource = firstSource
        showDownloadProgress = true
        
        // Start download
        downloadViewModel.startDownload(content: content, episode: nil, source: firstSource, seasonNumber: 1)
    }
    
    func displayAgeRestrictionAlert() {
        guard let content = vm.content else { return }
        let currentProfile = SessionManager.shared.getCurrentProfile()
        
        if let profile = currentProfile {
            if profile.effectiveKidsProfile {
                ageRestrictionMessage = "This content is not available on kids profiles. Content rated for older viewers."
            } else if let profileAge = profile.age {
                let displayRating = getDisplayRating(for: content.ageRating ?? content.ageRatingCode)
                ageRestrictionMessage = "This content is rated \(displayRating) and requires viewers to be \(content.minimumAge) or older. Your profile age is set to \(profileAge)."
            } else {
                ageRestrictionMessage = "This content has age restrictions. Please set your profile age in settings to access this content."
            }
        } else {
            ageRestrictionMessage = "This content is age-restricted. Please create and set up a profile to access this content."
        }
        
        showAgeRestrictionAlert = true
    }
    
    func checkAirPlayConnection() {
        let audioSession = AVAudioSession.sharedInstance()
        let currentRoute = audioSession.currentRoute
        
        // Check if any output is AirPlay
        let wasConnected = isAirPlayConnected
        isAirPlayConnected = currentRoute.outputs.contains { output in
            output.portType == .airPlay
        }
        
        // If just connected to AirPlay
        if isAirPlayConnected && !wasConnected {
            // Show guidance if it hasn't been shown before
            if !hasShownAirPlayGuidance {
                showAirPlayGuidance = true
            }
            
            // Auto-play logic for better UX
            if let content = vm.content {
                if content.type == .movie {
                    // For movies, check if we have sources and auto-play the first one
                    if let sources = content.contentSources, !sources.isEmpty {
                        let firstSource = sources[0]
                        if firstSource.accessType == .free || isPro {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                                vm.pickedSource = firstSource
                                vm.progress = 0
                                vm.playSource(firstSource)
                                if content.type == .movie {
                                    vm.increaseContentView(contentId: content.id ?? 0)
                                }
                            }
                        }
                    }
                } else if content.type == .series {
                    // For TV shows, play the first episode of the selected season
                    if let selectedSeason = vm.selectedSeason,
                       let episodes = selectedSeason.episodes,
                       !episodes.isEmpty {
                        let firstEpisode = episodes[0]
                        vm.selectedEpisode = firstEpisode
                        if let sources = firstEpisode.sources, !sources.isEmpty {
                            let firstSource = sources[0]
                            if firstSource.accessType == .free || isPro {
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                                    vm.pickedSource = firstSource
                                    vm.progress = 0
                                    vm.playSource(firstSource)
                                    vm.increaseEpisodeView(episodeId: firstEpisode.id ?? 0)
                                    episodeIncreaseTotalView += 1
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    var verticalDivider : some View {
        Rectangle()
            .frame(width: 1,height: 15)
    }
    
    func shareContent() {
        guard let content = vm.content else { 
            print("ShareContent: No content available to share")
            return 
        }
        
        let contentIdToShare = content.id ?? 0
        print("ShareContent: Sharing content with ID: \(contentIdToShare)")
        
        let buo = BranchUniversalObject(canonicalIdentifier: "\(contentIdToShare)")
        buo.title = content.title ?? ""
        buo.contentDescription = content.description ?? ""
        buo.imageUrl = content.horizontalPoster?.addBaseURL()?.absoluteString
        buo.publiclyIndex = true
        buo.locallyIndex = true
        
        buo.contentMetadata.customMetadata["genre"] = content.genreString
        buo.contentMetadata.customMetadata["rating"] = content.ratingString
        
        let linkProperties: BranchLinkProperties = BranchLinkProperties()
        
        linkProperties.addControlParam(WebService.branchContentID, withValue: "\(contentIdToShare)")
        
        buo.getShortUrl(with: linkProperties) { url, error in
            if let error = error {
                print("ShareContent: Branch error: \(error.localizedDescription)")
                // Fallback to simple sharing without Branch link
                DispatchQueue.main.async {
                    self.shareSimpleContent(content)
                }
            } else if let url = url {
                print("ShareContent: Branch link generated: \(url)")
                DispatchQueue.main.async {
                    self.shareLink(url)
                    self.vm.increaseContentShare(contentId: contentIdToShare)
                }
            }
        }
    }
    
    func shareSimpleContent(_ content: VugaContent) {
        // Fallback sharing without Branch link
        let shareText = "\(content.title ?? "Check out this content")\n\n\(content.description ?? "")"
        shareLink(shareText)
    }
    
    func shareLink(_ url: String) {
        let AV = UIActivityViewController(activityItems: [url], applicationActivities: nil)
        
        // Get the current window scene
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootViewController = windowScene.windows.first?.rootViewController {
            
            // Find the top-most view controller
            var topController = rootViewController
            while let presentedViewController = topController.presentedViewController {
                topController = presentedViewController
            }
            
            topController.present(AV, animated: true, completion: nil)
        }
    }
    
    func getFullTrailerUrl(_ trailerUrl: String) -> String {
        // If it's already a full URL (http/https), return as-is
        if trailerUrl.hasPrefix("http://") || trailerUrl.hasPrefix("https://") {
            return trailerUrl
        }
        
        // Otherwise, prepend the CDN base URL
        return WebService.itemBaseURLs + trailerUrl
    }
    
}

// MARK: - CombinedCastView
struct CombinedCastView: View {
    @ObservedObject var viewModel: ContentDetailViewModel
    
    var body: some View {
        HStack(spacing: 12) {
            // Google Cast button
            GoogleCastButton(viewModel: viewModel)
                .frame(width: 24, height: 24)
            
            // AirPlay button
            AirPlayRoutePickerView(isConnected: false)
                .frame(width: 24, height: 24)
        }
    }
}

// MARK: - Cast Request Delegate
class CastRequestDelegate: NSObject, GCKRequestDelegate {
    func requestDidComplete(_ request: GCKRequest) {
        print("✅ SUCCESS: Media is now playing on Cast device!")
        
        // Check media status
        if let session = GCKCastContext.sharedInstance().sessionManager.currentCastSession,
           let remoteMediaClient = session.remoteMediaClient {
            if let mediaStatus = remoteMediaClient.mediaStatus {
                print("📺 Player State: \(mediaStatus.playerState.rawValue)")
                print("📺 Media loaded successfully")
            }
        }
    }
    
    func request(_ request: GCKRequest, didFailWithError error: GCKError) {
        print("❌ FAILED: Could not load media - Error code: \(error.code)")
        print("❌ Error: \(error.localizedDescription)")
        
        // Detailed error analysis
        switch error.code {
        case 2100:
            print("💡 MEDIA_LOAD_FAILED - The media could not be loaded")
            print("💡 Common causes: CORS issue, unsupported format, network error")
        case 2101:
            print("💡 INVALID_REQUEST - The request is invalid")
        case 2102:
            print("💡 MEDIA_LOAD_CANCELLED - The media load was cancelled")
        case 2103:
            print("💡 MEDIA_LOAD_INTERRUPTED - The media load was interrupted")
        default:
            print("💡 Error details: Check if the Cast receiver app supports the media format")
        }
    }
}

// MARK: - GoogleCastButton
struct GoogleCastButton: UIViewRepresentable {
    @ObservedObject var viewModel: ContentDetailViewModel
    
    func makeUIView(context: Context) -> GCKUICastButton {
        let castButton = GCKUICastButton(frame: CGRect(x: 0, y: 0, width: 24, height: 24))
        castButton.tintColor = UIColor.white
        
        // Log when button is created
        print("🎯 GoogleCastButton: Created Cast button")
        
        // Add target to log taps
        castButton.addTarget(context.coordinator, action: #selector(Coordinator.castButtonTapped), for: .touchUpInside)
        
        return castButton
    }
    
    func updateUIView(_ uiView: GCKUICastButton, context: Context) {
        // Update if needed
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(parentView: self)
    }
    
    class Coordinator: NSObject {
        var castRequestDelegate: CastRequestDelegate?
        var parentView: GoogleCastButton?
        
        init(parentView: GoogleCastButton? = nil) {
            self.parentView = parentView
            super.init()
        }
        
        @objc func castButtonTapped() {
            print("🎯 GoogleCastButton: Cast button tapped!")
            let context = GCKCastContext.sharedInstance()
            print("📱 Cast state: \(context.castState.rawValue)")
            print("📱 Device count: \(context.discoveryManager.deviceCount)")
            print("📱 Discovery active: \(context.discoveryManager.discoveryActive)")
            
            // If already connected, start casting immediately
            if context.castState == .connected {
                print("📺 Already connected to Cast device - starting playback immediately!")
                startCastingCurrentContent()
            } else {
                // Force start discovery
                if !context.discoveryManager.discoveryActive {
                    print("🔄 Starting discovery...")
                    context.discoveryManager.startDiscovery()
                }
                
                // Set up observer to start casting when connection is established
                NotificationCenter.default.addObserver(
                    self,
                    selector: #selector(castStateChanged),
                    name: NSNotification.Name.gckCastStateDidChange,
                    object: nil
                )
            }
        }
        
        @objc func castStateChanged() {
            let context = GCKCastContext.sharedInstance()
            print("🔄 Cast state changed to: \(context.castState.rawValue)")
            
            if context.castState == .connected {
                print("✅ Cast connected - starting playback!")
                // Small delay to ensure session is fully established
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    self.startCastingCurrentContent()
                }
                // Remove observer after use
                NotificationCenter.default.removeObserver(self, name: NSNotification.Name.gckCastStateDidChange, object: nil)
            }
        }
        
        private func startCastingCurrentContent() {
            print("🎬 Starting cast playback...")
            
            guard let parentView = parentView,
                  let content = parentView.viewModel.content else {
                print("❌ No content available to cast")
                return
            }
            
            // Get the first available source URL
            var videoUrl: String?
            var contentType = "video/mp4" // Default
            
            if let sources = content.contentSources, !sources.isEmpty {
                let source = sources[0]
                
                // Get URL based on source type
                if source.type == .file {
                    videoUrl = source.media?.file?.addBaseURL()?.absoluteString
                } else {
                    videoUrl = source.source
                }
                
                // Determine content type from source
                if source.type == .m3u8 {
                    contentType = "application/x-mpegURL" // HLS
                } else if source.type == .mkv {
                    contentType = "video/x-matroska"
                } else if source.type == .webm {
                    contentType = "video/webm"
                } else if source.type == .mov {
                    contentType = "video/quicktime"
                }
            }
            
            // Fallback to test video if no URL available
            if videoUrl == nil || videoUrl?.isEmpty == true {
                print("⚠️ No content URL found, using test video")
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            }
            
            let castContext = GCKCastContext.sharedInstance()
            guard let session = castContext.sessionManager.currentCastSession,
                  let remoteMediaClient = session.remoteMediaClient else {
                print("❌ No active Cast session or media client")
                return
            }
            
            guard let finalVideoUrl = videoUrl,
                  let mediaURL = URL(string: finalVideoUrl) else {
                print("❌ Invalid video URL")
                return
            }
            
            // Log session details
            print("📱 Cast device: \(session.device.friendlyName ?? "Unknown")")
            print("📺 Content: \(content.title ?? "Unknown")")
            print("🎬 URL: \(finalVideoUrl)")
            print("📄 Content type: \(contentType)")
            
            // Create metadata with actual content info
            let metadata = GCKMediaMetadata(metadataType: content.type == .series ? .tvShow : .movie)
            metadata.setString(content.title ?? "Video", forKey: kGCKMetadataKeyTitle)
            
            if let description = content.description {
                metadata.setString(description, forKey: kGCKMetadataKeySubtitle)
            }
            
            // Add poster image if available (use vertical or horizontal poster)
            if let posterPath = content.verticalPoster ?? content.horizontalPoster,
               let posterUrl = posterPath.addBaseURL()?.absoluteString,
               let imageURL = URL(string: posterUrl) {
                metadata.addImage(GCKImage(url: imageURL, width: 480, height: 720))
            }
            
            // Create media info
            let mediaInfoBuilder = GCKMediaInformationBuilder(contentURL: mediaURL)
            mediaInfoBuilder.streamType = .buffered
            mediaInfoBuilder.contentType = contentType
            mediaInfoBuilder.metadata = metadata
            
            let mediaInfo = mediaInfoBuilder.build()
            
            // Create and send load request
            let loadRequestBuilder = GCKMediaLoadRequestDataBuilder()
            loadRequestBuilder.mediaInformation = mediaInfo
            loadRequestBuilder.autoplay = true
            
            let request = loadRequestBuilder.build()
            
            print("📤 Sending cast request...")
            
            let loadRequest = remoteMediaClient.loadMedia(with: request)
            self.castRequestDelegate = CastRequestDelegate()
            loadRequest.delegate = self.castRequestDelegate
            
            print("📤 Cast request sent - content should start playing on TV")
        }
        
        deinit {
            NotificationCenter.default.removeObserver(self)
        }
    }
}

#Preview {
    ContentDetailView()
}



private struct ContentBackgroudView: View {
    var content: VugaContent?
    
    private var fullWidth: CGFloat { Device.width }
    private var fullHeight: CGFloat { Device.width * 1.2 }
    
    var body: some View {
        if let content {
            VStack {
                KFImage(content.verticalPoster?.addBaseURL())
                    .resizeFillTo(width: fullWidth, height: fullHeight)
                    .clipped()
                    .blur(radius: 20)
                
                    .opacity(0.45)
                    .overlay(
                        LinearGradient(colors: [.clear,.bg], startPoint: .top, endPoint: .bottom)
                            .frame(height: 160)
                            .offset(y: 30)
                        ,alignment: .bottom
                    )
                Spacer()
            }
        }
    }
}

private struct StarCastCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var cast: Cast
    
    private var isArabic: Bool { language == .Arabic }
    private var cardWidth: CGFloat { Device.width * 0.60 }
    
    var body: some View {
        HStack(alignment: .center,spacing: 10) {
            KFImage(cast.actor?.profile_image?.addBaseURL())
                .resizeFillTo(width: 55, height: 55, compressSize: 3, radius: 12)
                .addStroke(radius: 12)
            VStack(alignment: .leading) {
                Text(cast.actor?.fullname ?? "")
                    .outfitSemiBold()
                    .foregroundColor(.text)
                    .lineLimit(1)
                    .flipsForRightToLeftLayoutDirection(!isArabic)
                
                Text(cast.charactorName ?? "")
                    .outfitRegular(15)
                    .foregroundColor(.textLight)
                    .lineLimit(1)
                    .flipsForRightToLeftLayoutDirection(!isArabic)
                
            }
            .flipsForRightToLeftLayoutDirection(!isArabic)
            
            Spacer(minLength: 0)
        }
        .frame(width: cardWidth)
        .addStroke(radius: 15)
    }
}

private struct SeasonTags : View {
    @ObservedObject var vm: ContentDetailViewModel
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @State private var showSeasonDropdown = false
    var content: VugaContent
    
    var body: some View {
        if let seasons = content.seasons, !seasons.isEmpty {
            VStack(spacing: 0) {
                divider(topPadding: 0)
                
                if seasons.count > 1 {
                    // Dropdown for multiple seasons
                    Menu {
                        ForEach(0..<seasons.count, id: \.self) { index in
                            let season = seasons[index]
                            Button(action: {
                                vm.selectSeason(season: season)
                                vm.seasonNumber = index + 1
                            }) {
                                HStack {
                                    Text(season.title ?? "Season \(index + 1)")
                                    if vm.selectedSeason?.id == season.id {
                                        Image(systemName: "checkmark")
                                    }
                                }
                            }
                        }
                    } label: {
                        HStack {
                            Text(vm.selectedSeason?.title ?? "Season 1")
                                .outfitMedium(16)
                                .foregroundColor(.text)
                            Spacer()
                            Image(systemName: "chevron.down")
                                .font(.system(size: 14))
                                .foregroundColor(.textLight)
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        .background(Color.text.opacity(0.10))
                    }
                } else {
                    // Single season - show as static label
                    HStack {
                        Text(seasons.first?.title ?? "Season 1")
                            .outfitMedium(16)
                            .foregroundColor(.text)
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color.text.opacity(0.10))
                }
                
                divider(topPadding: 0)
            }
            .padding(.horizontal, -10)
        }
    }
}

private func divider(topPadding: CGFloat = 8) -> some View {
    Rectangle()
        .fill(LinearGradient(colors: [.bg, .primary, .bg], startPoint: .leading, endPoint: .trailing))
        .frame(height: 2)
        .padding(.top, topPadding)
        .opacity(0.2)
}

struct EpisodeCard : View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var episode: Episode
    var episodeTotalView: Int
    var onRatingTap: (() -> Void)?
    var isInWatchlist: Bool = false
    var onWatchlistTap: (() -> Void)?
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack(spacing: 10) {
                KFImage(episode.thumbnail?.addBaseURL())
                    .resizeFillTo(width: 150, height: 105, compressSize: 2,radius: 15)
                    .addStroke(radius: 15)
                
                VStack(alignment: .leading, spacing: 6) {
                    Text(episode.title ?? "")
                        .outfitSemiBold(18)
                        .foregroundColor(.text)
                    
                    HStack(spacing: 10) {
                        Text(episode.duration ?? "")
                            .outfitRegular(16)
                            .foregroundColor(.textLight)
                        
                        // Episode watchlist button
                        Button(action: {
                            onWatchlistTap?()
                        }) {
                            Image(systemName: isInWatchlist ? "checkmark.circle.fill" : "plus.circle")
                                .resizable()
                                .frame(width: 20, height: 20)
                                .foregroundColor(isInWatchlist ? .green : .white)
                        }
                        
                        // Episode rating
                        HStack(spacing: 4) {
                            Image(systemName: "star.fill")
                                .resizable()
                                .frame(width: 12, height: 12)
                                .foregroundColor(.rating)
                            Text(String(format: "%.1f", episode.ratings ?? 0))
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white)
                            
                            if let userRating = episode.userRating {
                                Text("•")
                                    .foregroundColor(.gray)
                                Text(String(format: "%.0f", userRating))
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.primary)
                            } else {
                                Text("•")
                                    .foregroundColor(.gray)
                                Text("Rate")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.primary)
                            }
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.black.opacity(0.3))
                        .cornerRadius(12)
                        .onTapGesture {
                            onRatingTap?()
                        }
                    }
                }
            }
            
            ExpandableText(episode.description ?? "")
                .font(.custom(MyFont.OutfitLight, size: 16))
                .foregroundColor(.textLight)
                .moreButtonText(String.readMore.localized(language))
                .moreButtonFont(.custom(MyFont.OutfitBold, size: 16))
                .moreButtonColor(.text)
                .enableCollapse(true)
                .expandAnimation(.easeInOut(duration: 0))
            
        }
        .padding(.top,5)
    }
}


struct FlowLayout<Content: View>: View {
    let spacing: CGFloat
    let alignment: HorizontalAlignment
    let content: Content
    
    init(spacing: CGFloat = 8, alignment: HorizontalAlignment = .leading, @ViewBuilder content: () -> Content) {
        self.spacing = spacing
        self.alignment = alignment
        self.content = content()
    }
    
    var body: some View {
        GeometryReader { geometry in
            self.createFlowLayout(geometry: geometry)
        }
    }
    
    func createFlowLayout(geometry: GeometryProxy) -> some View {
        var width: CGFloat = 0
        var height: CGFloat = 0
        
        return ZStack(alignment: .topLeading) {
            content
                .background(
                    GeometryReader { innerGeometry in
                        Color.clear.preference(
                            key: ViewSizeKey.self,
                            value: innerGeometry.size
                        )
                    }
                )
                .onPreferenceChange(ViewSizeKey.self) { size in
                    if width + size.width > geometry.size.width {
                        width = 0
                        height -= size.height + spacing
                    }
                    width += size.width + spacing
                }
                .alignmentGuide(.leading) { dimension in
                    if width + dimension.width > geometry.size.width {
                        width = 0
                        height -= dimension.height + spacing
                    }
                    let result = width
                    width += dimension.width + spacing
                    return result
                }
                .alignmentGuide(.top) { _ in
                    let result = height
                    return result
                }
        }
    }
}

struct ViewSizeKey: PreferenceKey {
    typealias Value = CGSize
    static var defaultValue: CGSize = .zero
    static func reduce(value: inout CGSize, nextValue: () -> CGSize) {}
}

// AirPlay Route Picker View
struct AirPlayRoutePickerView: UIViewRepresentable {
    let isConnected: Bool
    
    func makeUIView(context: Context) -> UIView {
        let routePickerView = AVRoutePickerView()
        routePickerView.backgroundColor = UIColor.clear
        routePickerView.tintColor = UIColor.white.withAlphaComponent(0.01) // Make the default icon nearly invisible
        routePickerView.activeTintColor = UIColor(Color.base)
        routePickerView.prioritizesVideoDevices = true
        
        // Create a wrapper view to handle the tap
        let wrapperView = UIView()
        wrapperView.backgroundColor = .clear
        wrapperView.addSubview(routePickerView)
        
        // Setup constraints
        routePickerView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            routePickerView.centerXAnchor.constraint(equalTo: wrapperView.centerXAnchor),
            routePickerView.centerYAnchor.constraint(equalTo: wrapperView.centerYAnchor),
            routePickerView.widthAnchor.constraint(equalToConstant: 44),
            routePickerView.heightAnchor.constraint(equalToConstant: 44)
        ])
        
        return wrapperView
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {
        // Update the tint color based on connection status
        if let routePickerView = uiView.subviews.first as? AVRoutePickerView {
            if isConnected {
                routePickerView.tintColor = UIColor(Color.base)
                routePickerView.activeTintColor = UIColor(Color.base)
            } else {
                routePickerView.tintColor = UIColor(Color.text.opacity(0.8))
                routePickerView.activeTintColor = UIColor(Color.base)
            }
        }
    }
}

// Simple trailer view to avoid import issues
struct SimpleTrailerView: View {
    let content: VugaContent?
    let trailerUrl: String?
    
    @Environment(\.presentationMode) var presentationMode
    
    init(content: VugaContent) {
        self.content = content
        self.trailerUrl = nil
    }
    
    init(trailerUrl: String) {
        self.content = nil
        self.trailerUrl = trailerUrl
    }
    
    var body: some View {
        ZStack {
            Color.black.edgesIgnoringSafeArea(.all)
            
            VStack {
                HStack {
                    Spacer()
                    Button("Done") {
                        presentationMode.wrappedValue.dismiss()
                    }
                    .foregroundColor(.white)
                    .padding()
                }
                
                Spacer()
                
                // Placeholder for trailer content
                if let effectiveUrl = getEffectiveUrl() {
                    Text("Trailer would play here")
                        .foregroundColor(.white)
                        .font(.title2)
                    
                    Text(effectiveUrl)
                        .foregroundColor(.gray)
                        .font(.caption)
                        .padding()
                } else {
                    Text("No trailer URL available")
                        .foregroundColor(.white)
                        .font(.title2)
                }
                
                Spacer()
            }
        }
    }
    
    private func getEffectiveUrl() -> String? {
        if let url = trailerUrl {
            return url
        }
        
        if let content = content {
            // Check for primary trailer first, prioritizing direct URLs over YouTube
            if let trailers = content.trailers, let primaryTrailer = trailers.first(where: { $0.isPrimary == true }) {
                // Prioritize trailer_url (MP4) over YouTube
                if let trailerUrl = primaryTrailer.trailerUrl, !trailerUrl.isEmpty {
                    return trailerUrl
                }
                // Fall back to YouTube if no direct URL
                if let youtubeId = primaryTrailer.youtubeId, !youtubeId.isEmpty {
                    return "https://www.youtube.com/watch?v=\(youtubeId)"
                }
            }
            
            // Fall back to any trailer with a URL
            if let trailers = content.trailers {
                for trailer in trailers {
                    if let trailerUrl = trailer.trailerUrl, !trailerUrl.isEmpty {
                        return trailerUrl
                    }
                }
            }
            
            // Fall back to legacy trailer URL
            return content.trailerURL
        }
        
        return nil
    }
}

// Inline trailer view for auto-playing trailers
struct InlineTrailerView: View {
    let trailerUrl: String
    @Binding var shouldPlay: Bool
    @State private var isYouTubeUrl = false
    @State private var youTubeVideoId: String?
    @State private var player: AVPlayer?
    @State private var isPlaying = false
    @State private var showControls = true
    @State private var hideControlsWorkItem: DispatchWorkItem?
    @State private var isMuted = true  // Always start muted
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                if isYouTubeUrl, let videoId = youTubeVideoId {
                    // YouTube player using WebView
                    YouTubeEmbedView(videoId: videoId, shouldPlay: shouldPlay)
                } else if let player = player {
                    // Regular video player for CDN URLs
                    VideoPlayer(player: player)
                        .disabled(true)  // Disable VideoPlayer's own controls to use our custom tap gesture
                } else {
                    // Loading state
                    Color.black
                        .overlay(
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .scaleEffect(1.5)
                        )
                }
                
                // Controls overlay
                if showControls {
                    VStack {
                        Spacer()
                        
                        // Control bar at bottom
                        HStack(spacing: 20) {
                            // Play/Pause button
                            Button(action: {
                                togglePlayback()
                            }) {
                                Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(.white)
                                    .frame(width: 44, height: 44)
                                    .background(Circle().fill(Color.black.opacity(0.6)))
                            }
                            
                            Spacer()
                            
                            // Mute/Unmute button
                            Button(action: {
                                toggleMute()
                            }) {
                                Image(systemName: isMuted ? "speaker.slash.fill" : "speaker.wave.2.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(.white)
                                    .frame(width: 44, height: 44)
                                    .background(Circle().fill(Color.black.opacity(0.6)))
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 15)
                        .background(
                            LinearGradient(
                                gradient: Gradient(colors: [Color.black.opacity(0), Color.black.opacity(0.7)]),
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )
                    }
                    .transition(.opacity)
                }
                
                // Tap gesture to toggle playback
                Color.clear
                    .contentShape(Rectangle())
                    .onTapGesture {
                        // Toggle playback when tapping the video
                        togglePlayback()
                        
                        // Show controls when tapping
                        withAnimation(.easeInOut(duration: 0.3)) {
                            showControls = true
                        }
                        
                        // Auto-hide controls after 3 seconds if playing
                        if isPlaying {
                            scheduleHideControls()
                        }
                    }
            }
        }
        .onAppear {
            analyzeAndSetupTrailer()
        }
        .onDisappear {
            player?.pause()
            hideControlsWorkItem?.cancel()
        }
        .onChange(of: shouldPlay) { newValue in
            print("InlineTrailerView: onChange shouldPlay: \(newValue)")
            
            // For YouTube videos, control through JavaScript
            if isYouTubeUrl {
                // This will be handled by the YouTubeEmbedView's updateUIView
                print("InlineTrailerView: YouTube onChange - handled by YouTubeEmbedView")
            }
            // For regular videos, only respond to external changes (not from togglePlayback)
            else if player?.timeControlStatus != (newValue ? .playing : .paused) {
                if newValue {
                    print("InlineTrailerView: onChange calling player.play()")
                    player?.play()
                    isPlaying = true
                } else {
                    print("InlineTrailerView: onChange calling player.pause()")
                    player?.pause()
                    isPlaying = false
                }
            }
        }
    }
    
    private func togglePlayback() {
        print("InlineTrailerView: togglePlayback called, current isPlaying: \(isPlaying)")
        
        // For regular videos, control directly
        if !isYouTubeUrl {
            if let player = player {
                print("InlineTrailerView: Player timeControlStatus: \(player.timeControlStatus.rawValue)")
                
                if player.timeControlStatus == .playing {
                    print("InlineTrailerView: Pausing video...")
                    player.pause()
                    isPlaying = false
                    shouldPlay = false
                } else {
                    print("InlineTrailerView: Playing video...")
                    player.play()
                    isPlaying = true
                    shouldPlay = true
                }
                
                print("InlineTrailerView: After toggle - isPlaying: \(isPlaying)")
            } else {
                print("InlineTrailerView: WARNING - player is nil")
            }
        } else {
            // For YouTube, toggle the state and let onChange handle it
            isPlaying.toggle()
            shouldPlay = isPlaying
            print("InlineTrailerView: YouTube video - toggled to isPlaying: \(isPlaying)")
        }
        
        // Show controls when toggling
        withAnimation(.easeInOut(duration: 0.3)) {
            showControls = true
        }
        
        if isPlaying {
            scheduleHideControls()
        } else {
            // Cancel auto-hide when paused
            hideControlsWorkItem?.cancel()
        }
    }
    
    private func toggleMute() {
        isMuted.toggle()
        player?.isMuted = isMuted
        player?.volume = isMuted ? 0.0 : 1.0  // Also control volume for AirPods compatibility
        
        // Show controls when toggling mute
        withAnimation(.easeInOut(duration: 0.3)) {
            showControls = true
        }
        
        // Reschedule auto-hide if playing
        if isPlaying {
            scheduleHideControls()
        }
    }
    
    private func scheduleHideControls() {
        // Cancel any existing work item
        hideControlsWorkItem?.cancel()
        
        // Create new work item to hide controls
        let workItem = DispatchWorkItem {
            withAnimation(.easeInOut(duration: 0.3)) {
                showControls = false
            }
        }
        
        hideControlsWorkItem = workItem
        
        // Schedule to hide after 3 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 3, execute: workItem)
    }
    
    private func analyzeAndSetupTrailer() {
        // Configure audio session for muted playback (important for AirPods)
        configureAudioSessionForMutedPlayback()
        
        // Check if it's a YouTube URL
        if trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be") {
            isYouTubeUrl = true
            youTubeVideoId = extractYouTubeVideoId(from: trailerUrl)
        } else {
            isYouTubeUrl = false
            setupVideoPlayer()
        }
    }
    
    private func configureAudioSessionForMutedPlayback() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .moviePlayback, options: [.mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to configure audio session: \(error)")
        }
    }
    
    private func setupVideoPlayer() {
        guard let url = URL(string: trailerUrl) else { return }
        
        // Load asset asynchronously to avoid blocking main thread
        let asset = AVURLAsset(url: url)
        let keys = ["playable", "hasProtectedContent"]
        
        asset.loadValuesAsynchronously(forKeys: keys) {
            
            var error: NSError?
            let status = asset.statusOfValue(forKey: "playable", error: &error)
            
            DispatchQueue.main.async {
                if status == .loaded && asset.isPlayable {
                    let playerItem = AVPlayerItem(asset: asset)
                    self.player = AVPlayer(playerItem: playerItem)
                    
                    // IMPORTANT: Always start muted for auto-playing trailers
                    // Multiple approaches to ensure muting works with AirPods/Bluetooth
                    self.player?.isMuted = true
                    self.player?.volume = 0.0  // Also set volume to 0
                    self.isMuted = true  // Sync state
                    
                    // Set up looping
                    NotificationCenter.default.addObserver(
                        forName: .AVPlayerItemDidPlayToEndTime,
                        object: playerItem,
                        queue: .main
                    ) { _ in
                        if self.shouldPlay {
                            self.player?.seek(to: .zero)
                            self.player?.play()
                        }
                    }
                    
                    // Auto-start playing if shouldPlay is true
                    if self.shouldPlay {
                        // Ensure it's muted before playing (belt and suspenders for AirPods)
                        self.player?.isMuted = true
                        self.player?.volume = 0.0
                        self.player?.play()
                        self.isPlaying = true
                        // Auto-hide controls when starting playback
                        self.scheduleHideControls()
                    }
                } else {
                    print("TrailerWithControlsView: Asset not playable or error loading")
                }
            }
        }
    }
    
    private func extractYouTubeVideoId(from url: String) -> String? {
        if url.contains("youtube.com/watch?v=") {
            let components = URLComponents(string: url)
            return components?.queryItems?.first(where: { $0.name == "v" })?.value
        } else if url.contains("youtu.be/") {
            let components = url.components(separatedBy: "youtu.be/")
            if components.count > 1 {
                return components[1].components(separatedBy: "?").first
            }
        } else if url.contains("youtube.com/embed/") {
            let components = url.components(separatedBy: "embed/")
            if components.count > 1 {
                return components[1].components(separatedBy: "?").first
            }
        }
        return url
    }
}

// Muted Trailer View for the redesigned detail screen
struct MutedTrailerView: View {
    let trailerUrl: String
    @Binding var shouldPlay: Bool
    @Binding var isMuted: Bool
    @State private var isYouTubeUrl = false
    @State private var youTubeVideoId: String?
    @State private var player: AVPlayer?
    @State private var isPlaying = true  // Start playing by default
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                if isYouTubeUrl, let videoId = youTubeVideoId {
                    // YouTube player
                    YouTubeEmbedView(videoId: videoId, shouldPlay: shouldPlay)
                } else if let player = player {
                    // Regular video player
                    VideoPlayer(player: player)
                        .disabled(true)
                } else {
                    // Loading state
                    Color.black
                        .overlay(
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .scaleEffect(1.5)
                        )
                }
                
                // Control overlay - tap to play/pause
                Color.clear
                    .contentShape(Rectangle())
                    .onTapGesture {
                        togglePlayPause()
                    }
                
                // Controls overlay
                VStack {
                    Spacer()
                    HStack {
                        // Play/Pause button
                        Button(action: {
                            togglePlayPause()
                        }) {
                            Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                                .font(.system(size: 24))
                                .foregroundColor(.white)
                                .frame(width: 44, height: 44)
                                .background(Color.black.opacity(0.5))
                                .clipShape(Circle())
                        }
                        .padding(.leading, 16)
                        
                        Spacer()
                        
                        // Mute/Unmute button
                        Button(action: {
                            toggleMute()
                        }) {
                            Image(systemName: isMuted ? "speaker.slash.fill" : "speaker.wave.2.fill")
                                .font(.system(size: 20))
                                .foregroundColor(.white)
                                .frame(width: 44, height: 44)
                                .background(Color.black.opacity(0.5))
                                .clipShape(Circle())
                        }
                        .padding(.trailing, 16)
                    }
                    .padding(.bottom, 16)
                }
            }
        }
        .onAppear {
            analyzeAndSetupTrailer()
        }
        .onDisappear {
            player?.pause()
        }
        .onChange(of: shouldPlay) { newValue in
            // Stop/start playback based on shouldPlay binding
            if !newValue {
                // Stop the trailer when Play button is pressed
                player?.pause()
                isPlaying = false
            } else {
                // Resume playback
                player?.play()
                isPlaying = true
            }
        }
    }
    
    private func toggleMute() {
        isMuted.toggle()
        player?.isMuted = isMuted
        player?.volume = isMuted ? 0.0 : 1.0  // Also control volume for AirPods
    }
    
    private func togglePlayPause() {
        if let player = player {
            if player.timeControlStatus == .playing {
                player.pause()
                isPlaying = false
            } else {
                player.play()
                isPlaying = true
            }
        }
    }
    
    private func analyzeAndSetupTrailer() {
        // Configure audio session for muted playback (important for AirPods)
        configureAudioSessionForMutedPlayback()
        
        // Check if it's a YouTube URL
        if trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be") {
            isYouTubeUrl = true
            youTubeVideoId = extractYouTubeVideoId(from: trailerUrl)
        } else {
            isYouTubeUrl = false
            setupVideoPlayer()
        }
    }
    
    private func configureAudioSessionForMutedPlayback() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .moviePlayback, options: [.mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to configure audio session: \(error)")
        }
    }
    
    private func setupVideoPlayer() {
        guard let url = URL(string: trailerUrl) else { return }
        
        // Load asset asynchronously to avoid blocking main thread
        let asset = AVURLAsset(url: url)
        let keys = ["playable", "hasProtectedContent"]
        
        asset.loadValuesAsynchronously(forKeys: keys) {
            
            var error: NSError?
            let status = asset.statusOfValue(forKey: "playable", error: &error)
            
            DispatchQueue.main.async {
                if status == .loaded && asset.isPlayable {
                    let playerItem = AVPlayerItem(asset: asset)
                    self.player = AVPlayer(playerItem: playerItem)
                    
                    // IMPORTANT: Set muted BEFORE playing (multiple methods for AirPods)
                    self.player?.isMuted = true  // Always start muted
                    self.player?.volume = 0.0  // Also set volume to 0 for AirPods
                    self.isMuted = true  // Update state to match
                    
                    // Set up looping
                    NotificationCenter.default.addObserver(
                        forName: .AVPlayerItemDidPlayToEndTime,
                        object: playerItem,
                        queue: .main
                    ) { _ in
                        if self.isPlaying {
                            self.player?.seek(to: .zero)
                            self.player?.play()
                        }
                    }
                    
                    // Auto-start playing muted (double ensure for AirPods)
                    self.player?.isMuted = true  // Ensure it's muted
                    self.player?.volume = 0.0  // Also volume 0 for AirPods
                    self.player?.play()
                    self.isPlaying = true
                } else {
                    print("MutedTrailerView: Asset not playable or error loading")
                }
            }
        }
    }
    
    private func extractYouTubeVideoId(from url: String) -> String? {
        if url.contains("youtube.com/watch?v=") {
            let components = URLComponents(string: url)
            return components?.queryItems?.first(where: { $0.name == "v" })?.value
        } else if url.contains("youtu.be/") {
            let components = url.components(separatedBy: "youtu.be/")
            if components.count > 1 {
                let idComponent = components[1]
                return idComponent.components(separatedBy: "?").first
            }
        }
        return nil
    }
}

// YouTube embed view
struct YouTubeEmbedView: UIViewRepresentable {
    let videoId: String
    var shouldPlay: Bool = true
    
    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []
        
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.backgroundColor = .black
        webView.scrollView.isScrollEnabled = false
        
        context.coordinator.webView = webView
        
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        // Only reload HTML if we haven't loaded it yet
        if !context.coordinator.isLoaded {
            let embedHTML = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { margin: 0; padding: 0; background: black; }
                    .video-container { position: relative; padding-bottom: 56.25%; height: 0; }
                    #player { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
                </style>
            </head>
            <body>
                <div class="video-container" id="container">
                    <div id="player"></div>
                </div>
                <script>
                    var tag = document.createElement('script');
                    tag.src = "https://www.youtube.com/iframe_api";
                    var firstScriptTag = document.getElementsByTagName('script')[0];
                    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
                    
                    var player;
                    function onYouTubeIframeAPIReady() {
                        player = new YT.Player('player', {
                            height: '100%',
                            width: '100%',
                            videoId: '\(videoId)',
                            playerVars: {
                                'playsinline': 1,
                                'autoplay': \(shouldPlay ? 1 : 0),
                                'mute': 1,
                                'loop': 1,
                                'playlist': '\(videoId)',
                                'controls': 0,
                                'modestbranding': 1,
                                'rel': 0,
                                'showinfo': 0,
                                'fs': 0
                            },
                            events: {
                                'onReady': onPlayerReady,
                                'onStateChange': onPlayerStateChange
                            }
                        });
                    }
                    
                    function onPlayerReady(event) {
                        if (\(shouldPlay ? "true" : "false")) {
                            event.target.playVideo();
                        }
                        
                        // Click handling is now done at the SwiftUI level
                    }
                    
                    function pauseVideo() {
                        if (player && player.pauseVideo) {
                            player.pauseVideo();
                        }
                    }
                    
                    function playVideo() {
                        if (player && player.playVideo) {
                            player.playVideo();
                        }
                    }
                    
                    function onPlayerStateChange(event) {
                        // State change handling for future use
                    }
                </script>
            </body>
            </html>
            """
            
            webView.loadHTMLString(embedHTML, baseURL: nil)
            context.coordinator.isLoaded = true
        } else {
            // Control playback through JavaScript
            if shouldPlay {
                webView.evaluateJavaScript("playVideo()", completionHandler: nil)
            } else {
                webView.evaluateJavaScript("pauseVideo()", completionHandler: nil)
            }
        }
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator()
    }
    
    class Coordinator: NSObject {
        var webView: WKWebView?
        var isLoaded = false
    }
}
