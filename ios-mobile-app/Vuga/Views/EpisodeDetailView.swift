//
//  EpisodeDetailView.swift
//  Vuga
//
//  TV Show Episode Detail View - Matches ContentDetailView structure
//

import SwiftUI
import Kingfisher
import AVKit
import WebKit
import MediaPlayer
import GoogleCast
import Alamofire

struct EpisodeDetailView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.myUser) var myUser: User? = nil
    @AppStorage(SessionKeys.isPro) var isPro = false
    @Environment(\.presentationMode) var presentationMode
    @EnvironmentObject var downloadViewModel: DownloadViewModel
    
    let episode: Episode
    let seriesContent: VugaContent?
    
    @State private var showRatingSheet = false
    @State private var currentUserRating: Double = 0
    @State private var showLoginAlert = false
    @State private var shouldPlayTrailer = true
    @State private var isTrailerMuted = true
    @State private var dragOffset: CGSize = .zero
    @State private var isDragging = false
    @State private var isBookmarked = false
    @State private var isEpisodeInWatchlist = false
    @State private var showDownloadStartedAlert = false
    @State private var downloadAlertMessage = ""
    @State private var showTrailerSheet = false
    @State private var isAirPlayConnected = false
    @State private var isSubmittingRating = false
    @State private var isCheckingWatchlist = false
    @State private var showCastMenu = false
    @State private var castRequestDelegate: EpisodeCastRequestDelegate?
    
    // Helper function to check if content has trailers available
    private func hasTrailersAvailable() -> Bool {
        // Check if parent series has trailer
        if let content = seriesContent {
            if let trailers = content.trailers, !trailers.isEmpty {
                return true
            }
            if let trailerURL = content.trailerURL, !trailerURL.isEmpty {
                return true
            }
        }
        return false
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
        .sheet(isPresented: $showRatingSheet) {
            ratingSheetContent()
        }
        .fullScreenCover(isPresented: $showTrailerSheet) {
            trailerFullScreenContent()
        }
        .alert(isPresented: $showLoginAlert) {
            Alert(
                title: Text("Login Required"),
                message: Text("Please login to rate this episode"),
                dismissButton: .default(Text("OK"))
            )
        }
        .alert(isPresented: $showDownloadStartedAlert) {
            Alert(
                title: Text("Download"),
                message: Text(downloadAlertMessage),
                dismissButton: .default(Text("OK"))
            )
        }
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
        .onAppear {
            checkBookmarkStatus()
            checkEpisodeWatchlistStatus()
        }
    }
    
    private var mainContent: some View {
        VStack(spacing: 0) {
            // Full-width trailer/thumbnail player at top with overlay controls
            ZStack(alignment: .topLeading) {
                // Trailer/thumbnail player
                trailerPlayerSection()
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
                    contentInfoSection()
                    
                    // Icon buttons row
                    iconButtonsSection()
                    
                    // Action buttons row
                    actionButtonsSection()
                    
                    // Story with expandable MORE button
                    if let description = episode.description, !description.isEmpty {
                        ExpandableDescriptionView(description: description)
                            .padding(.horizontal, 16)
                    }
                    
                    // More Episodes section
                    if let seasons = seriesContent?.seasons,
                       let currentSeason = seasons.first(where: { $0.id == episode.seasonID }),
                       let episodes = currentSeason.episodes,
                       episodes.count > 1 {
                        moreEpisodesSection(episodes: episodes.filter { $0.id != episode.id })
                    }
                    
                    // More Like This section (from parent series)
                    if let moreLikeThis = seriesContent?.moreLikeThis, !moreLikeThis.isEmpty {
                        moreLikeThisSection(moreLikeThis)
                    }
                }
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 20)
        }
    }
    
    private func trailerPlayerSection() -> some View {
        ZStack {
            // Show thumbnail as background
            if let thumbnailString = episode.thumbnail,
               let url = thumbnailString.addBaseURL() {
                KFImage(url)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: UIScreen.main.bounds.width, height: AdaptiveContentSizing.featuredContentSize().width * 9/16)
                    .clipped()
            } else if let posterString = seriesContent?.verticalPoster,
                      let url = posterString.addBaseURL() {
                // Fallback to series poster
                KFImage(url)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: UIScreen.main.bounds.width, height: AdaptiveContentSizing.featuredContentSize().width * 9/16)
                    .clipped()
            }
            
            // Gradient overlay
            LinearGradient(
                gradient: Gradient(colors: [Color.clear, Color.black.opacity(0.3)]),
                startPoint: .top,
                endPoint: .bottom
            )
            
            // Play button overlay
            Button(action: playEpisode) {
                Image(systemName: "play.circle.fill")
                    .font(.system(size: 70))
                    .foregroundColor(.white)
                    .shadow(radius: 10)
            }
        }
    }
    
    private func contentInfoSection() -> some View {
        VStack(alignment: .leading, spacing: 8) {
            // Series title
            if let seriesTitle = seriesContent?.title {
                Text(seriesTitle)
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.7))
            }
            
            // Episode title with Season/Episode info
            HStack(spacing: 8) {
                Text("S\(episode.seasonID ?? 0) E\(episode.number ?? 0):")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.white)
                Text(episode.title ?? "Episode \(episode.number ?? 0)")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.white)
                    .lineLimit(1)
            }
            
            // Metadata row: Release date, Age rating, Video format
            HStack(spacing: 12) {
                // Release date
                if let createdAt = episode.createdAt {
                    Text(formatReleaseDate(createdAt))
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                }
                
                // Age rating
                if let ageRating = seriesContent?.ageRating {
                    Text(ageRating)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(getAgeRatingColor(ageRating))
                        .cornerRadius(4)
                } else {
                    Text("NR")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.gray)
                        .cornerRadius(4)
                }
                
                // Video format
                Text("HD")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .overlay(
                        RoundedRectangle(cornerRadius: 4)
                            .stroke(Color.white.opacity(0.5), lineWidth: 1)
                    )
                
                // Duration if available
                if let duration = episode.duration {
                    Text(duration.formatDurationWithUnits())
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                }
                
                Spacer()
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }
    
    private func actionButtonsSection() -> some View {
        VStack(spacing: 10) {
            // Primary Play button
            Button(action: playEpisode) {
                HStack {
                    Image(systemName: "play.fill")
                        .font(.system(size: 16))
                    Text("Play Episode")
                        .font(.system(size: 16, weight: .semibold))
                }
                .foregroundColor(.black)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(Color.white)
                .cornerRadius(8)
            }
            
            // Secondary Download button (if available)
            if episode.sources?.first?.isDownload == 1 {
                Button(action: downloadEpisode) {
                    HStack {
                        Image(systemName: "arrow.down.to.line")
                            .font(.system(size: 16))
                        Text("Download")
                            .font(.system(size: 16, weight: .medium))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.white.opacity(0.15))
                    .cornerRadius(8)
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
    }
    
    private func iconButtonsSection() -> some View {
        HStack(spacing: 30) {
            // Rate button
            VStack(spacing: 5) {
                Button(action: {
                    if myUser != nil {
                        currentUserRating = episode.userRating ?? 0
                        showRatingSheet = true
                    } else {
                        showLoginAlert = true
                    }
                }) {
                    ZStack {
                        Image(systemName: currentUserRating > 0 ? "star.fill" : "star")
                            .font(.system(size: 22))
                            .foregroundColor(currentUserRating > 0 ? .yellow : .white)
                        
                        if isSubmittingRating {
                            ProgressView()
                                .scaleEffect(0.8)
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        }
                    }
                }
                .disabled(isSubmittingRating)
                Text("Rate")
                    .font(.system(size: 11))
                    .foregroundColor(.white)
            }
            
            // Watchlist button
            VStack(spacing: 5) {
                Button(action: toggleEpisodeWatchlist) {
                    ZStack {
                        Image(systemName: isEpisodeInWatchlist ? "checkmark.circle.fill" : "plus")
                            .font(.system(size: 22))
                            .foregroundColor(isEpisodeInWatchlist ? .green : .white)
                        
                        if isCheckingWatchlist {
                            ProgressView()
                                .scaleEffect(0.8)
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        }
                    }
                }
                .disabled(isCheckingWatchlist)
                Text("Watchlist")
                    .font(.system(size: 11))
                    .foregroundColor(.white)
            }
            
            // Share button
            VStack(spacing: 5) {
                Button(action: shareEpisode) {
                    Image(systemName: "square.and.arrow.up")
                        .font(.system(size: 22))
                        .foregroundColor(.white)
                }
                Text("Share")
                    .font(.system(size: 11))
                    .foregroundColor(.white)
            }
            
            // Cast button
            VStack(spacing: 5) {
                EpisodeGoogleCastButton(episode: episode, seriesContent: seriesContent)
                    .frame(width: 24, height: 24)
                Text("Cast")
                    .font(.system(size: 11))
                    .foregroundColor(.white)
            }
            
            // AirPlay button
            VStack(spacing: 5) {
                AirPlayRoutePickerView(isConnected: isAirPlayConnected)
                    .frame(width: 22, height: 22)
                    .foregroundColor(.white)
                Text("AirPlay")
                    .font(.system(size: 11))
                    .foregroundColor(.white)
            }
            
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.top, 16)
    }
    
    private func moreEpisodesSection(episodes: [Episode]) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("More Episodes")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)
                .padding(.horizontal, 16)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(episodes, id: \.id) { ep in
                        VStack(alignment: .leading, spacing: 6) {
                            ZStack(alignment: .bottomLeading) {
                                if let thumbnailString = ep.thumbnail,
                                   let url = thumbnailString.addBaseURL() {
                                    KFImage(url)
                                        .resizable()
                                        .aspectRatio(16/9, contentMode: .fill)
                                        .frame(width: 160, height: 90)
                                        .clipped()
                                        .cornerRadius(8)
                                }
                                
                                // Episode number badge
                                Text("E\(ep.number ?? 0)")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.black.opacity(0.7))
                                    .cornerRadius(4)
                                    .padding(4)
                            }
                            
                            Text(ep.title ?? "Episode \(ep.number ?? 0)")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(.white)
                                .lineLimit(1)
                            
                            if let duration = ep.duration {
                                Text(duration)
                                    .font(.system(size: 10))
                                    .foregroundColor(.white.opacity(0.6))
                            }
                        }
                        .frame(width: 160)
                        .onTapGesture {
                            // Navigate to this episode
                            Navigation.pushToSwiftUiView(
                                EpisodeDetailView(
                                    episode: ep,
                                    seriesContent: seriesContent
                                )
                            )
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
        .padding(.top, 24)
    }
    
    private func moreLikeThisSection(_ contents: [VugaContent]) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("More Like This")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)
                .padding(.horizontal, 16)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(contents, id: \.id) { content in
                        VStack(alignment: .leading, spacing: 6) {
                            if let posterString = content.verticalPoster,
                               let url = posterString.addBaseURL() {
                                KFImage(url)
                                    .resizable()
                                    .aspectRatio(2/3, contentMode: .fill)
                                    .frame(width: 120, height: 180)
                                    .clipped()
                                    .cornerRadius(8)
                            }
                            
                            Text(content.title ?? "")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(.white)
                                .lineLimit(1)
                        }
                        .frame(width: 120)
                        .onTapGesture {
                            // Navigate to content detail
                            Navigation.pushToSwiftUiView(
                                ContentDetailView(contentId: content.id ?? 0)
                            )
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
        .padding(.top, 24)
    }
    
    private func ratingSheetContent() -> some View {
        RatingBottomSheet(
            isPresented: $showRatingSheet,
            currentRating: $currentUserRating,
            contentTitle: episode.title ?? "Episode \(episode.number ?? 0)",
            contentType: .movie,  // Episodes use movie type for rating
            isEpisode: true,
            onSubmit: { rating in
                submitRating(rating)
            }
        )
    }
    
    private func trailerFullScreenContent() -> some View {
        Group {
            if let seriesContent = seriesContent {
                TrailerPlayerView(content: seriesContent)
            }
        }
    }
    
    // MARK: - Actions
    
    private func playEpisode() {
        // Check if there are sources available
        if let sources = episode.sources, !sources.isEmpty {
            let firstSource = sources[0]
            
            // Navigate to video player
            Navigation.pushToSwiftUiView(
                VideoPlayerView(
                    content: seriesContent,
                    episode: episode,
                    type: firstSource.type?.rawValue ?? 2,
                    isShowAdView: false,
                    url: firstSource.sourceURL.absoluteString,
                    progress: 0,
                    sourceId: firstSource.id
                )
            )
        }
    }
    
    private func downloadEpisode() {
        guard let sources = episode.sources, !sources.isEmpty else { return }
        let source = sources[0]
        
        // Check if parent series content exists
        guard let content = seriesContent else { return }
        
        // Start download using the download view model's method
        downloadViewModel.startDownload(
            content: content,
            episode: episode,
            source: source,
            seasonNumber: 1
        )
        
        downloadAlertMessage = "Download started for \(episode.title ?? "Episode")"
        showDownloadStartedAlert = true
    }
    
    private func toggleBookmark() {
        // Toggle bookmark for parent series
        guard let content = seriesContent else { return }
        
        isBookmarked.toggle()
        
        // Make API call to toggle bookmark
        let userId = myUser?.id ?? 0
        let profileId = myUser?.lastActiveProfileId
        let contentId = content.id ?? 0
        
        var params: [Params: Any] = [
            .appUserId: userId,
            .contentId: contentId
        ]
        
        if let profileId = profileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(url: .toggleWatchlist, params: params) { (obj: UserModel) in
            print("Watchlist toggled successfully")
        }
    }
    
    private func checkBookmarkStatus() {
        // Check if parent series is bookmarked
        guard seriesContent != nil else { return }
        
        // For now, we'll check from local user's watchlist if available
        // In the future, this could be fetched from API
        isBookmarked = false
    }
    
    private func checkEpisodeWatchlistStatus() {
        guard let user = myUser, let episodeId = episode.id else { return }
        
        isCheckingWatchlist = true
        
        var params: [Params: Any] = [
            .appUserId: user.id ?? 0,
            .episodeId: episodeId
        ]
        
        if let profileId = user.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(
            url: .checkEpisodeWatchlist,
            params: params
        ) { (response: UserModel) in
            DispatchQueue.main.async {
                self.isCheckingWatchlist = false
                if let data = response.data as? [String: Any],
                   let isInWatchlist = data["is_in_watchlist"] as? Bool {
                    self.isEpisodeInWatchlist = isInWatchlist
                }
            }
        } callbackFailure: { error in
            DispatchQueue.main.async {
                self.isCheckingWatchlist = false
                print("Failed to check episode watchlist status: \(error.localizedDescription)")
            }
        }
    }
    
    private func toggleEpisodeWatchlist() {
        guard let user = myUser, let episodeId = episode.id else {
            showLoginAlert = true
            return
        }
        
        // Optimistically update the UI
        let wasInWatchlist = isEpisodeInWatchlist
        isEpisodeInWatchlist.toggle()
        
        isCheckingWatchlist = true
        
        var params: [Params: Any] = [
            .appUserId: user.id ?? 0,
            .episodeId: episodeId
        ]
        
        if let profileId = user.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(
            url: .toggleEpisodeWatchlist,
            params: params
        ) { (response: UserModel) in
            DispatchQueue.main.async {
                self.isCheckingWatchlist = false
                // Toggle was successful, keep the new state
                print("Episode watchlist toggled successfully")
            }
        } callbackFailure: { error in
            DispatchQueue.main.async {
                self.isCheckingWatchlist = false
                // Revert on failure
                self.isEpisodeInWatchlist = wasInWatchlist
                print("Failed to toggle episode watchlist: \(error.localizedDescription)")
            }
        }
    }
    
    private func shareEpisode() {
        let shareText = "Check out \(episode.title ?? "this episode") on VUGA!"
        let activityVC = UIActivityViewController(activityItems: [shareText], applicationActivities: nil)
        
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true, completion: nil)
        }
    }
    
    
    private func submitRating(_ rating: Double) {
        guard let user = myUser else { return }
        
        isSubmittingRating = true
        
        var params: [Params: Any] = [
            .appUserId: user.id ?? 0,
            .episodeId: episode.id ?? 0,
            .rating: rating
        ]
        
        if let profileId = user.lastActiveProfileId {
            params[.profileId] = profileId
        }
        
        NetworkManager.callWebService(
            url: .rateEpisode,
            params: params
        ) { (response: UserModel) in
            DispatchQueue.main.async {
                self.isSubmittingRating = false
                self.currentUserRating = rating
                self.showRatingSheet = false
                print("Episode rating submitted successfully")
            }
        } callbackFailure: { error in
            DispatchQueue.main.async {
                self.isSubmittingRating = false
                print("Failed to submit episode rating: \(error.localizedDescription)")
            }
        }
    }
    
    private func formatReleaseDate(_ dateString: String) -> String {
        // Parse the date string and format it
        let inputFormatter = DateFormatter()
        inputFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        if let date = inputFormatter.date(from: dateString) {
            let outputFormatter = DateFormatter()
            outputFormatter.dateFormat = "yyyy"
            return outputFormatter.string(from: date)
        }
        
        // If parsing fails, try to extract just the year
        if dateString.count >= 4 {
            return String(dateString.prefix(4))
        }
        
        return dateString
    }
    
    private func getAgeRatingColor(_ rating: String) -> Color {
        switch rating {
        case "G":
            return Color.green
        case "PG":
            return Color.blue
        case "PG-13":
            return Color.orange
        case "R":
            return Color.red
        case "NC-17":
            return Color.purple
        default:
            return Color.gray
        }
    }
}

// MARK: - Episode Cast Request Delegate
class EpisodeCastRequestDelegate: NSObject, GCKRequestDelegate {
    func requestDidComplete(_ request: GCKRequest) {
        print("✅ SUCCESS: Episode is now playing on Cast device!")
        
        // Check media status
        if let session = GCKCastContext.sharedInstance().sessionManager.currentCastSession,
           let remoteMediaClient = session.remoteMediaClient {
            if let mediaStatus = remoteMediaClient.mediaStatus {
                print("📺 Player State: \(mediaStatus.playerState.rawValue)")
                print("📊 Media Duration: \(mediaStatus.mediaInformation?.streamDuration ?? 0) seconds")
            }
        }
    }
    
    func request(_ request: GCKRequest, didFailWithError error: GCKError) {
        print("❌ ERROR: Failed to cast episode")
        print("Error Code: \(error.code)")
        print("Error Domain: \(error.domain)")
        print("Error Description: \(error.localizedDescription)")
        
        // Provide specific error guidance
        switch error.code {
        case 4:
            print("💡 INVALID_REQUEST - Check media URL and metadata")
        case 2001:
            print("💡 MEDIA_LOAD_FAILED - The media could not be loaded")
        case 2100:
            print("💡 MEDIA_LOAD_CANCELLED - The media load was cancelled")
        case 2103:
            print("💡 MEDIA_LOAD_INTERRUPTED - The media load was interrupted")
        default:
            print("💡 Error details: Check if the Cast receiver app supports the media format")
        }
    }
}

// MARK: - EpisodeGoogleCastButton
struct EpisodeGoogleCastButton: UIViewRepresentable {
    let episode: Episode
    let seriesContent: VugaContent?
    
    func makeUIView(context: Context) -> GCKUICastButton {
        let castButton = GCKUICastButton(frame: CGRect(x: 0, y: 0, width: 24, height: 24))
        castButton.tintColor = UIColor.white
        
        // Log when button is created
        print("🎯 EpisodeGoogleCastButton: Created Cast button for episode")
        
        // Add target to log taps
        castButton.addTarget(context.coordinator, action: #selector(Coordinator.castButtonTapped), for: .touchUpInside)
        
        return castButton
    }
    
    func updateUIView(_ uiView: GCKUICastButton, context: Context) {
        // Update if needed
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(episode: episode, seriesContent: seriesContent)
    }
    
    class Coordinator: NSObject {
        var castRequestDelegate: EpisodeCastRequestDelegate?
        let episode: Episode
        let seriesContent: VugaContent?
        
        init(episode: Episode, seriesContent: VugaContent?) {
            self.episode = episode
            self.seriesContent = seriesContent
            super.init()
        }
        
        @objc func castButtonTapped() {
            print("🎯 EpisodeGoogleCastButton: Cast button tapped!")
            let context = GCKCastContext.sharedInstance()
            print("📱 Cast state: \(context.castState.rawValue)")
            print("📱 Device count: \(context.discoveryManager.deviceCount)")
            print("📱 Discovery active: \(context.discoveryManager.discoveryActive)")
            
            // If already connected, start casting immediately
            if context.castState == .connected {
                print("📺 Already connected to Cast device - starting episode playback immediately!")
                startCastingEpisode()
            } else {
                // Force start discovery
                if !context.discoveryManager.discoveryActive {
                    print("🔍 Starting device discovery...")
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
                print("✅ Cast connected - starting episode playback!")
                // Small delay to ensure session is fully established
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    self.startCastingEpisode()
                }
                // Remove observer after use
                NotificationCenter.default.removeObserver(self, name: NSNotification.Name.gckCastStateDidChange, object: nil)
            }
        }
        
        private func startCastingEpisode() {
            print("🎬 Starting episode cast playback...")
            
            // Get video URL from episode sources
            var videoUrl: String?
            var contentType: String = "video/mp4"
            
            if let sources = episode.sources, !sources.isEmpty {
                // Prefer non-YouTube sources for casting
                let nonYouTubeSources = sources.filter { $0.type?.rawValue != 1 }
                if let source = nonYouTubeSources.first ?? sources.first {
                    videoUrl = source.source
                    
                    // Determine content type based on URL
                    if let url = videoUrl {
                        if url.contains(".m3u8") {
                            contentType = "application/x-mpegURL"
                        } else if url.contains(".mpd") {
                            contentType = "application/dash+xml"
                        }
                    }
                    
                    print("📹 Using episode source: \(source.source ?? "nil")")
                }
            }
            
            // Fallback URL if no source available
            let finalVideoUrl = videoUrl ?? "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            
            let castContext = GCKCastContext.sharedInstance()
            guard let session = castContext.sessionManager.currentCastSession,
                  let remoteMediaClient = session.remoteMediaClient else {
                print("❌ No active Cast session or media client")
                return
            }
            
            // Validate URL
            guard let url = URL(string: finalVideoUrl) else {
                print("❌ Invalid video URL: \(finalVideoUrl)")
                return
            }
            
            // Log session details
            print("📱 Cast device: \(session.device.friendlyName ?? "Unknown")")
            print("📺 Episode: \(episode.title ?? "Unknown")")
            print("📺 Series: \(seriesContent?.title ?? "Unknown")")
            print("🎬 URL: \(finalVideoUrl)")
            print("📄 Content type: \(contentType)")
            
            // Create metadata
            let metadata = GCKMediaMetadata(metadataType: .tvShow)
            metadata.setString(episode.title ?? "Episode", forKey: kGCKMetadataKeyTitle)
            if let seriesTitle = seriesContent?.title {
                metadata.setString(seriesTitle, forKey: kGCKMetadataKeySeriesTitle)
            }
            // Episode number is stored in the 'number' field
            if let episodeNumber = episode.number {
                metadata.setString(String(episodeNumber), forKey: kGCKMetadataKeyEpisodeNumber)
            }
            // Season info would need to be passed from parent content
            // For now, we'll skip setting season number
            
            // Add poster images
            if let thumbnailUrl = episode.thumbnail {
                let posterUrl = thumbnailUrl.starts(with: "http") ? thumbnailUrl :
                    "https://iosdev.gossip-stone.com/\(thumbnailUrl)"
                metadata.addImage(GCKImage(url: URL(string: posterUrl)!, width: 480, height: 360))
            }
            
            // Build the media information
            let mediaInfoBuilder = GCKMediaInformationBuilder(contentURL: url)
            mediaInfoBuilder.streamType = .buffered
            mediaInfoBuilder.contentType = contentType
            mediaInfoBuilder.metadata = metadata
            
            let mediaInformation = mediaInfoBuilder.build()
            
            // Build and send the load request
            let loadRequestBuilder = GCKMediaLoadRequestDataBuilder()
            loadRequestBuilder.mediaInformation = mediaInformation
            loadRequestBuilder.autoplay = true
            loadRequestBuilder.startTime = 0
            
            let request = loadRequestBuilder.build()
            
            print("📤 Sending episode cast request...")
            
            let loadRequest = remoteMediaClient.loadMedia(with: request)
            self.castRequestDelegate = EpisodeCastRequestDelegate()
            loadRequest.delegate = self.castRequestDelegate
            
            print("📤 Episode cast request sent - episode should start playing on TV")
        }
        
        deinit {
            NotificationCenter.default.removeObserver(self)
        }
    }
}