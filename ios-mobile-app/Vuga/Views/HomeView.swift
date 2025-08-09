//
//  HomeView.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 10/05/24.
//

import SwiftUI
import Kingfisher
import ScalingHeaderScrollView
import WrappingStack
import CoreData



struct HomeView: View {
    @AppStorage(SessionKeys.language) private var language = LocalizationService.shared.language
    @StateObject private var vm = HomeViewModel()
    @Binding var selectedTab : Tab
    @State private var progress: CGFloat = 0
    @State var isVideoStart = false
    @State var startLoading = false
    @State private var scrollToTop = false
    @State private var hasInitiallyScrolled = false
    @State private var selectedFeaturedContent: VugaContent?
    @FetchRequest(
        sortDescriptors: [NSSortDescriptor(keyPath: \RecentlyWatched.date, ascending: false)]
    )
    var recentlyWatchedContents: FetchedResults<RecentlyWatched>
    @State private var cachedUniqueRecentlyWatched: [RecentlyWatched] = []

    func uniqueRecentlyWatched(recently: [RecentlyWatched]) -> [RecentlyWatched] {
        if cachedUniqueRecentlyWatched.isEmpty {
            let grouped = Dictionary(grouping: recently, by: { $0.contentID })
            let unique = grouped.compactMap { _, items in
                items.sorted { (item1, item2) in
                    let date1 = item1.date ?? Date.distantPast
                    let date2 = item2.date ?? Date.distantPast
                    return date1 > date2
                }.first
            }
            cachedUniqueRecentlyWatched = unique.sorted(by: {$0.date! > $1.date!})
        }
        return cachedUniqueRecentlyWatched
    }

    
    var body: some View {
        ZStack(alignment: .top){
            ScrollViewReader { proxy in
                ScrollView(showsIndicators: false) {
                    VStack {
                        // Header with Logo and Profile Name
                        headerWithLogoAndProfile
                            .padding(.top, 10) // Fixed top padding for proper spacing
                            .id("top") // Add ID for scroll anchor
                        
                        // Navigation Menu Row with horizontal category list
                        horizontalCategoryList
                            .padding(.bottom, 10)
                        
                        if vm.featured.isNotEmpty {
                            topBar
                                .frame(height: UIScreen.main.bounds.width * 0.75 * 1.5)
                        }
                        LazyVStack {
                            if recentlyWatchedContents.isNotEmpty && !vm.isLoading && vm.featured.isNotEmpty{
                                recentlyWatched
                            }
                            
                            if vm.wishlists.isNotEmpty {
                                watchlistCard
                            }
                            topTenContent
                            ForEach(vm.genres, id: \.id) { genre in
                                GenreHomeCard(vm: vm, genre: genre)
                            }
                        }
                        .ignoresSafeArea(.all,edges: .top)
                    }
                    .padding(.top, 0) // Remove duplicate top padding
                }
                .refreshable {
                    vm.isForRefresh = true
                    vm.fetchData()
                    vm.selectedRecentlyWatched = nil
                    fetchRecentlyWatchedContent()
                }
                .onAppear {
                    // Initial scroll to top with a delay to ensure content is loaded
                    if !hasInitiallyScrolled {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                            withAnimation(.easeOut(duration: 0.1)) {
                                proxy.scrollTo("top", anchor: .top)
                            }
                            hasInitiallyScrolled = true
                        }
                    } else {
                        // For subsequent appearances, scroll immediately
                        withAnimation(.easeOut(duration: 0.1)) {
                            proxy.scrollTo("top", anchor: .top)
                        }
                    }
                }
                .onChange(of: scrollToTop) { shouldScroll in
                    if shouldScroll {
                        withAnimation(.easeOut(duration: 0.1)) {
                            proxy.scrollTo("top", anchor: .top)
                        }
                        scrollToTop = false
                    }
                }
            }
        }
        .onChange(of: selectedTab, perform: { newValue in
                if newValue == Tab.home {
                    progress = 0
                    // Trigger scroll to top when returning to home tab
                    scrollToTop = true
                }
        })
        .fullScreenCover(item: $vm.selectedRecentlyWatched, content: { _ in
            if let selectedRecentlyWatched = vm.selectedRecentlyWatched {
                VideoPlayerView(type: Int(selectedRecentlyWatched.contentSourceType) == 7 ? 5 : Int(selectedRecentlyWatched.contentSourceType), isShowAdView: false, isForDownloads: selectedRecentlyWatched.isForDownload, url: selectedRecentlyWatched.sourceUrl ?? "", progress: selectedRecentlyWatched.progress, sourceId: Int(selectedRecentlyWatched.contentSourceId))
            }
        })
        .customAlert(isPresented: $vm.isDeleteRecentlyWatched){
            DialogCard(icon: Image.delete, title: .areYouSure, subTitle: .recentlyWatchedDeleteDes, buttonTitle: .remove, onClose: {
                withAnimation {
                    vm.isDeleteRecentlyWatched = false
                }
            },onButtonTap: {
                if let obj = vm.deleteSelectedRecentlyWatched {
                    cachedUniqueRecentlyWatched.removeAll(where: {$0 == obj})
                    DataController.shared.context.delete(obj)
                    DataController.shared.saveData()
                }
                vm.isDeleteRecentlyWatched = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    Navigation.pop(false)
                }
            })
            .animation(.default, value: vm.isDeleteRecentlyWatched)
        }
        .addBackground()
        .loaderView(vm.isLoading)
    }
    
    // Header with Logo and Profile Name
    private var headerWithLogoAndProfile: some View {
        HStack {
            // Left side - Logo and Profile Name
            HStack(spacing: 10) {
                // App Logo
                Image.logo
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 80, height: 23)
                
                Text(getProfileGreeting())
                    .outfitSemiBold(18)
                    .foregroundColor(Color("textColor"))
            }
            
            Spacer()
            
            // Right side - Action icons
            HStack(spacing: 12) {
                // Stream-casting icon
                Button(action: {
                    // Handle casting - placeholder
                    print("Casting tapped")
                }) {
                    Image(systemName: "airplayvideo")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color("textLight"))
                        .frame(width: 35, height: 35)
                        .background(Color("searchBg"))
                        .clipShape(Circle())
                }
                
                // Downloads icon
                Button(action: {
                    Navigation.pushToSwiftUiView(DownloadView())
                }) {
                    Image.download
                        .resizeFitTo(size: 16, renderingMode: .template)
                        .foregroundColor(Color("textLight"))
                        .frame(width: 35, height: 35)
                        .background(Color("searchBg"))
                        .clipShape(Circle())
                }
                
                // Search icon
                Button(action: {
                    selectedTab = .search
                }) {
                    Image.search
                        .resizeFitTo(size: 16, renderingMode: .template)
                        .foregroundColor(Color("textLight"))
                        .frame(width: 35, height: 35)
                        .background(Color("searchBg"))
                        .clipShape(Circle())
                }
                
                // Profile Avatar (matches Android design)
                Button(action: {
                    selectedTab = .profile
                }) {
                    Circle()
                        .fill(Color.textLight)
                        .frame(width: 33, height: 33)
                        .overlay(
                            Text(getProfileFirstLetter())
                                .outfitSemiBold(16)
                                .foregroundColor(Color("bgColor"))
                        )
                }
            }
        }
        .padding(.horizontal, 15)
    }
    
    // Helper function to get profile first letter
    private func getProfileFirstLetter() -> String {
        guard let profile = SessionManager.shared.getCurrentProfile(),
              !profile.name.isEmpty else {
            return "U"
        }
        return String(profile.name.prefix(1)).uppercased()
    }
    
    // Navigation buttons for TV Shows, Movies, Live TV, Networks
    private var horizontalCategoryList: some View {
        VStack(alignment: .leading, spacing: 8) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    // TV Shows button
                    Button(action: {
                        // Switch to search tab and filter for TV shows
                        selectedTab = .search
                        // Post notification to set search filter for TV shows
                        NotificationCenter.default.post(
                            name: .setSearchFilter, 
                            object: nil,
                            userInfo: ["contentType": ContentType.series]
                        )
                    }) {
                        Text("TV Shows")
                            .outfitMedium(14)
                            .foregroundColor(Color("textLight"))
                            .padding(.horizontal, 20)
                            .padding(.vertical, 8)
                            .background(Color.clear)
                            .overlay(
                                RoundedRectangle(cornerRadius: 15)
                                    .stroke(Color("stroke"), lineWidth: 1)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 15))
                    }
                    
                    // Movies button
                    Button(action: {
                        // Switch to search tab and filter for movies
                        selectedTab = .search
                        // Post notification to set search filter for movies
                        NotificationCenter.default.post(
                            name: .setSearchFilter, 
                            object: nil,
                            userInfo: ["contentType": ContentType.movie]
                        )
                    }) {
                        Text("Movies")
                            .outfitMedium(14)
                            .foregroundColor(Color("textLight"))
                            .padding(.horizontal, 20)
                            .padding(.vertical, 8)
                            .background(Color.clear)
                            .overlay(
                                RoundedRectangle(cornerRadius: 15)
                                    .stroke(Color("stroke"), lineWidth: 1)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 15))
                    }
                    
                    // Live TV button
                    Button(action: {
                        // Navigate to existing Live TV view if available
                        Navigation.pushToSwiftUiView(LiveTVsView())
                    }) {
                        Text("Live TV")
                            .outfitMedium(14)
                            .foregroundColor(Color("textLight"))
                            .padding(.horizontal, 20)
                            .padding(.vertical, 8)
                            .background(Color.clear)
                            .overlay(
                                RoundedRectangle(cornerRadius: 15)
                                    .stroke(Color("stroke"), lineWidth: 1)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 15))
                    }
                    
                    // Categories button with menu
                    Menu {
                        // All genre categories
                        ForEach(vm.genres, id: \.id) { genre in
                            Button(genre.title ?? "") {
                                Navigation.pushToSwiftUiView(GenreContentsView(genre: genre))
                            }
                        }
                        
                        Divider()
                        
                        // Network options
                        Button("MediaTeka") {
                            // Navigate to MediaTeka content
                            if let genre = vm.genres.first {
                                Navigation.pushToSwiftUiView(GenreContentsView(genre: genre))
                            }
                        }
                        Button("HBO") {
                            // Navigate to HBO content
                            if let genre = vm.genres.first {
                                Navigation.pushToSwiftUiView(GenreContentsView(genre: genre))
                            }
                        }
                    } label: {
                        HStack(spacing: 4) {
                            Text("Categories")
                                .outfitMedium(14)
                            Image(systemName: "chevron.down")
                                .font(.system(size: 10))
                        }
                        .foregroundColor(Color("textLight"))
                        .padding(.horizontal, 20)
                        .padding(.vertical, 8)
                        .background(Color.clear)
                        .overlay(
                            RoundedRectangle(cornerRadius: 15)
                                .stroke(Color("stroke"), lineWidth: 1)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 15))
                    }
                }
                .padding(.horizontal, 15)
            }
        }
    }
    
    private var recentlyWatched: some View {
        VStack {
            Heading(title: .recentlyWatched, content: {})
                .padding(.horizontal, 10)
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 10) {
                    ForEach(cachedUniqueRecentlyWatched, id: \.self) { recently in
                        VStack(alignment: .leading, spacing: 0) {
                            VStack(spacing: 0) {
                                ZStack(alignment: .center) {
                                    KFImage(recently.type == .movie ? recently.thumbnail.addBaseURL() : recently.episodeHorizontalPoster.addBaseURL())
                                        .resizeFillTo(width: 118, height: 73, radius: 10)
                                    
                                    Image(systemName: "play.fill")
                                        .rotationEffect(.degrees(language == .Arabic ? 180 : 0))
                                        .font(.system(size: 18, weight: .bold))
                                        .foregroundStyle(.white)
                                        .padding(8)
                                        .padding(.leading, 2)
                                        .background(.white.opacity(0.2))
                                        .clipShape(Circle())
                                    
                                    VStack {
                                        HStack {
                                            Spacer()
                                            Image.close
                                                .resizeFitTo(size: 12, renderingMode: .template)
                                                .foregroundStyle(.white)
                                                .padding(12)
                                                .onTap {
                                                    vm.deleteSelectedRecentlyWatched = recently
                                                    vm.isDeleteRecentlyWatched = true
                                                }
                                        }
                                        Spacer()
                                    }
                                    VStack {
                                        Spacer()
                                        ProgressView(value: recently.progress / recently.totalDuration)
                                            .progressViewStyle(LinearProgressViewStyle())
                                            .tint(Color("baseColor"))
                                    }
                                }
                            }
                            .cornerRadius(radius: 15)
                            .addStroke(radius: 15)
                            HStack {
                                Text(recently.name ?? "")
                                    .lineLimit(1)
                                    .outfitSemiBold(18)
                                    .foregroundColor(Color("textColor"))
                                    .padding(.top, 5)
                                Spacer(minLength: 5)
                                Image.info
                                    .resizeFitTo(size: 20, renderingMode: .template)
                                    .foregroundStyle(.white)
                                    .onTap {
                                        Navigation.pushToSwiftUiView(ContentDetailView(contentId: Int(recently.contentID)))
                                    }
                            }
                                .frame(width: 118, alignment: .leading)
                        }
                        .onTap {
                            vm.selectedRecentlyWatched = recently
                        }
                    }
                }
                .padding(.horizontal, 10)
            }
        }
        .padding(.top, 10)
        .onAppear(perform: {
            fetchRecentlyWatchedContent()
        })
    }
    
    func fetchRecentlyWatchedContent() {
        let fetchRequest: NSFetchRequest<RecentlyWatched>
        fetchRequest = RecentlyWatched.fetchRequest()
        
        let context = DataController.shared.context
        cachedUniqueRecentlyWatched = []
        
        do{
            let recentlyWatched = try context.fetch(fetchRequest)
            cachedUniqueRecentlyWatched = uniqueRecentlyWatched(recently: recentlyWatched)
            DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                startLoading = false
            }
        } catch {
            print("")
        }
    }
    
    private var topTenContent: some View {
        VStack {
            if vm.topContents.isNotEmpty {
                Heading(title: .topContents.localized(language))
                    .padding(.horizontal)
            }
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 10) {
                    ForEach(vm.topContents, id: \.id) { topContent in
                        ZStack(alignment: .bottomTrailing) {
                            KFImage(topContent.content?.verticalPoster?.addBaseURL())
                                .resizeFillTo(width: 98, height: 140,radius: 5)
                                .addStroke(radius: 5)
                                .padding(.bottom,45)
                                .overlay(
                                    TypeTagForVugaContent(content: topContent.content)
                                    ,alignment: .topLeading
                                )
                                .customCornerRadius(radius: 5)
                            StrokeTextLabel(text: "\(topContent.contentIndex ?? 0)")
                                    .shadow(color: .bg,radius: 3)
                                    .offset(y: 65)
                            .onTap {
                                Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: topContent.contentID))
                            }
                        }
                    }
                }
                .padding(.horizontal, 10)
            }
        }
        .padding(.top, 10)
    }
    
    private var watchlistCard : some View {
        VStack {
            Heading(title: .watchlist,content: {
                Text(String.seeAll.localized(language))
                    .outfitLight(16)
                    .foregroundColor(Color("textLight"))
                    .onTap {
                        selectedTab = .watchlist
                    }
            })
            .padding(.horizontal, 10)
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 10) {
                    ForEach(vm.wishlists, id: \.id) { content in
                        VStack(alignment: .leading,spacing: 0) {
                            KFImage(content.horizontalPoster?.addBaseURL())
                                .resizeFillTo(width: 118, height: 73,radius: 10)
                                .addStroke(radius: 10)
                                .overlay(
                                    TypeTagForVugaContent(content: content)
                                    ,alignment: .topLeading
                                )
                                .cornerRadius(radius: 15)

                            Text(content.title ?? "")
                                .lineLimit(1)
                                .outfitSemiBold(18)
                                .foregroundColor(Color("textColor"))
                                .padding(.top,5)
                                .frame(width: 118,alignment: .leading)
                            HStack(spacing: 7) {
                                HStack(spacing: 5) {
                                    Image.star
                                        .resizeFitTo(size: 12)
                                    Text(content.ratingString)
                                        .outfitLight(16)
                                }
                                .foregroundColor(Color("rating"))
                                Rectangle()
                                    .frame(width: 1, height: 15)
                                    .foregroundColor(Color("textLight"))
                                Text(verbatim: "\(content.releaseYear ?? 0)")
                                    .outfitLight(17)
                                    .foregroundColor(Color("textLight"))
                            }
                            
                            .padding(.top,3)
                        }
                        .onTap {
                            Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: content.id ?? 0))
                        }
                        
                    }
                }
                .padding(.horizontal, 10)
            }
        }
        .padding(.top,10)
    }
    
    private var topBar : some View {
        ZStack(alignment: .bottom) {
            featuredContentTabView
            pageIndicator
        }
        .overlay(Color("bgColor").opacity(progress).allowsHitTesting(progress == 0 ? false : true))
    }
    
    private var featuredContentTabView: some View {
        GeometryReader { geometry in
            TabView(selection: $vm.selectedImageIndex) {
                ForEach(0..<vm.featured.count, id: \.self) { index in
                    featuredContentCard(feature: vm.featured[index])
                        .frame(width: geometry.size.width * 0.75, height: geometry.size.height * 0.9)
                        .padding(.horizontal, geometry.size.width * 0.125)
                }
            }
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
            .frame(width: geometry.size.width, height: geometry.size.height)
        }
        .frame(height: UIScreen.main.bounds.width * 0.75 * 1.5) // 75% width with 2:3 aspect ratio
    }
    
    private func featuredContentCard(feature: VugaContent) -> some View {
        GeometryReader { geometry in
            ZStack {
                // Poster image with vertical aspect ratio
                featuredPosterImage(feature: feature)
                    .frame(width: geometry.size.width, height: geometry.size.height)
                
                // Gradient overlay for better text visibility
                LinearGradient(
                    colors: [.clear, .black.opacity(0.2), .black.opacity(0.7)],
                    startPoint: .top,
                    endPoint: .bottom
                )
                
                // Content overlay centered
                VStack(spacing: 12) {
                    Spacer()
                    
                    // Title
                    Text(feature.title ?? "")
                        .outfitBold(20)
                        .foregroundColor(.white)
                        .lineLimit(2)
                        .multilineTextAlignment(.center)
                        .shadow(color: .black.opacity(0.8), radius: 4, x: 0, y: 2)
                    
                    // Action buttons
                    HStack(spacing: 10) {
                        // WATCH NOW button
                        Button(action: {
                            handlePlayAction(feature: feature)
                        }) {
                            HStack(spacing: 4) {
                                Image(systemName: "play.fill")
                                    .font(.system(size: 11))
                                Text("WATCH NOW")
                                    .outfitSemiBold(11)
                                    .tracking(0.3)
                            }
                            .foregroundColor(Color.gray.opacity(0.9))
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(Color.white)
                            .clipShape(Capsule())
                        }
                        
                        // + MY LIST button
                        Button(action: {
                            handleWatchlistAction(feature: feature)
                        }) {
                            HStack(spacing: 4) {
                                Image(systemName: isInWatchlist(contentId: feature.id ?? 0) ? "checkmark" : "plus")
                                    .font(.system(size: 11, weight: .bold))
                                Text("MY LIST")
                                    .outfitSemiBold(11)
                                    .tracking(0.3)
                            }
                            .foregroundColor(.white)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(Color.gray.opacity(0.3))
                            .overlay(
                                Capsule()
                                    .stroke(Color.white.opacity(0.3), lineWidth: 1)
                            )
                            .clipShape(Capsule())
                        }
                    }
                    
                    Spacer().frame(height: 20)
                }
                .padding(.horizontal)
            }
            .cornerRadius(12)
            .shadow(color: .black.opacity(0.3), radius: 10, x: 0, y: 5)
        }
    }
    
    private func featuredPosterImage(feature: VugaContent) -> some View {
        KFImage(feature.verticalPoster?.addBaseURL() ?? feature.horizontalPoster?.addBaseURL())
            .resizable()
            .aspectRatio(contentMode: .fill)
            .clipped()
    }
    
    private func handlePlayAction(feature: VugaContent) {
        print("HomeView: handlePlayAction called for content: \(feature.title ?? "Unknown")")
        print("HomeView: Content ID: \(feature.id ?? 0)")
        print("HomeView: Has content sources: \(feature.contentSources != nil)")
        print("HomeView: Content sources count: \(feature.contentSources?.count ?? 0)")
        
        // Check if we already have content sources
        if let sources = feature.contentSources, !sources.isEmpty,
           let firstSource = sources.first {
            print("HomeView: Found existing sources, playing directly")
            playVideoDirectly(source: firstSource, content: feature)
        } else {
            // Need to fetch content details to get sources
            print("HomeView: No sources found, fetching content details")
            fetchAndPlayContent(contentId: feature.id ?? 0, content: feature)
        }
    }
    
    private func playVideoDirectly(source: Source, content: VugaContent) {
        let sourceType = source.type?.rawValue ?? 0
        
        print("HomeView: Playing video directly - sourceType: \(sourceType), sourceId: \(source.id ?? 0)")
        print("HomeView: Source URL: \(source.sourceURL.absoluteString)")
        
        // Navigate to the appropriate view
        if sourceType == 1 {
            // YouTube video
            print("HomeView: Playing YouTube video")
            Navigation.pushToSwiftUiView(YoutubeView(youtubeUrl: source.source ?? ""))
        } else {
            // Regular video player
            print("HomeView: Playing regular video")
            Navigation.pushToSwiftUiView(
                VideoPlayerView(
                    content: content,
                    episode: nil,
                    type: sourceType,
                    isShowAdView: false,
                    url: source.sourceURL.absoluteString,
                    progress: 0,
                    sourceId: source.id
                )
            )
        }
    }
    
    private func fetchAndPlayContent(contentId: Int, content: VugaContent) {
        print("HomeView: Fetching content details for direct play - contentId: \(contentId)")
        
        var params: [Params: Any] = [.contentId: contentId]
        if let userId = vm.myUser?.id {
            params[.appUserId] = userId
        }
        if let profileId = SessionManager.shared.currentProfile?.profileId {
            params[.profileId] = profileId
            print("HomeView: Using profile ID: \(profileId)")
        } else if let profileId = vm.myUser?.lastActiveProfileId {
            params[.profileId] = profileId
            print("HomeView: Using last active profile ID: \(profileId)")
        }
        
        // Show loading indicator
        vm.startLoading()
        
        NetworkManager.callWebService(url: .fetchContentDetails, params: params, callbackSuccess: { (obj: ContentModel) in
            vm.stopLoading()
            
            print("HomeView: Content details response - status: \(obj.status ?? false)")
            print("HomeView: Content title: \(obj.data?.title ?? "nil")")
            print("HomeView: Content sources count: \(obj.data?.contentSources?.count ?? 0)")
            
            if let fullContent = obj.data {
                if let sources = fullContent.contentSources, !sources.isEmpty {
                    print("HomeView: Found \(sources.count) sources")
                    if let firstSource = sources.first {
                        print("HomeView: First source - type: \(firstSource.type?.rawValue ?? -1), url: \(firstSource.source ?? "nil")")
                        // Play the first available source
                        playVideoDirectly(source: firstSource, content: fullContent)
                    }
                } else {
                    // No sources available
                    print("HomeView: No sources found in content details")
                    print("HomeView: Content type: \(fullContent.type?.rawValue ?? -1)")
                    print("HomeView: Has seasons: \(fullContent.seasons != nil)")
                    
                    // For TV shows, we might need to get episode sources
                    if fullContent.type == .series, let seasons = fullContent.seasons, !seasons.isEmpty {
                        print("HomeView: This is a TV show with \(seasons.count) seasons")
                        
                        // Try to play the first episode of the first season
                        if let firstSeason = seasons.first,
                           let episodes = firstSeason.episodes,
                           !episodes.isEmpty,
                           let firstEpisode = episodes.first,
                           let episodeSources = firstEpisode.sources,
                           !episodeSources.isEmpty,
                           let firstSource = episodeSources.first {
                            print("HomeView: Playing first episode of TV show")
                            // Pass the original content - the video player will handle episode info
                            playVideoDirectly(source: firstSource, content: fullContent)
                            return
                        }
                    }
                    
                    // Fallback to content detail view
                    Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: contentId))
                }
            } else {
                print("HomeView: No content data in response")
                Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: contentId))
            }
        }, callbackFailure: { error in
            vm.stopLoading()
            print("HomeView: Failed to fetch content details: \(error)")
            // Fallback to content detail view on error
            Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: contentId))
        })
    }
    
    private func handleWatchlistAction(feature: VugaContent) {
        // Check if user is logged in using vm.myUser instead of SessionManager
        guard vm.myUser != nil else {
            print("HomeView: No user logged in, navigating to LoginView")
            Navigation.pushToSwiftUiView(LoginView())
            return
        }
        
        print("HomeView: Toggling watchlist for content ID: \(feature.id ?? 0)")
        vm.toggleWatchlist(contentId: feature.id ?? 0) { success, message in
            print("HomeView: Watchlist toggle result - success: \(success), message: \(message ?? "nil")")
            if success {
                // Refresh the watchlist data
                vm.fetchData()
            } else {
                // Show error message if toggle failed
                if let message = message {
                    makeToast(title: message)
                }
            }
        }
    }
    
    private func isInWatchlist(contentId: Int) -> Bool {
        return vm.myUser?.checkIsAddedToWatchList(contentId: contentId) ?? false
    }
    
    private var pageIndicator: some View {
        HStack(spacing: 8) {
            ForEach(0..<vm.featured.count, id: \.self) { index in
                Circle()
                    .fill(vm.selectedImageIndex == index ? Color.white : Color.white.opacity(0.5))
                    .frame(width: 8, height: 8)
                    .animation(.easeInOut, value: vm.selectedImageIndex)
            }
        }
        .padding(.bottom, 10)
    }
    
    // Helper function to get profile greeting
    private func getProfileGreeting() -> String {
        guard let profile = SessionManager.shared.getCurrentProfile(),
              !profile.name.isEmpty else {
            return "User"
        }
        
        return profile.name
    }
    
    // Helper function to filter content by type
    private func filterContentByType(_ type: String) {
        print("Filtering content by: \(type)")
        // Implementation for filtering content based on type
        // This could involve calling different API endpoints or filtering existing data
    }
}

struct GenreHomeCard : View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm : HomeViewModel
    var genre: Genre
    var body: some View {
        VStack {
            Heading(title: genre.title ?? "",content: {
                Text(String.seeAll.localized(language))
                    .outfitLight(16)
                    .foregroundColor(Color("textLight"))
                    .onTap {
                        Navigation.pushToSwiftUiView(GenreContentsView(genre: genre))
                    }
            })
            .padding(.horizontal, 10)
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 10) {
                    ForEach(genre.contents ?? [], id: \.id) { content in
                        ContentVerticalCard(vm: vm,content: content)
                    }
                }
                .padding(.horizontal, 10)
                .environment(\.layoutDirection, language == .Arabic ? .rightToLeft : .leftToRight)

            }
            
        }
        .padding(.bottom, 10)
    }
}

struct ContentVerticalCard: View {
     var vm : HomeViewModel?
    var content: VugaContent
    var body: some View {
        KFImage(content.verticalPoster?.addBaseURL())
            .resizeFillTo(width: 98, height: 140, radius: 5)
            .addStroke(radius: 5)
            .onTap {
                Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: content.id ?? 0))
            }
    }
}

struct StrokeTextLabel: UIViewRepresentable {
    var text: String
    
    func makeUIView(context: Context) -> UILabel {
        let attributedStringParagraphStyle = NSMutableParagraphStyle()
        attributedStringParagraphStyle.alignment = NSTextAlignment.right
        
        let attributedString = NSAttributedString(
            string: text,
            attributes:[
                NSAttributedString.Key.paragraphStyle: attributedStringParagraphStyle,
                NSAttributedString.Key.strokeWidth: -1.0,
                NSAttributedString.Key.strokeColor: UIColor.white,
                NSAttributedString.Key.foregroundColor: UIColor(Color("bgColor")).withAlphaComponent(0.4),
                NSAttributedString.Key.font: UIFont(name: "SFUIDisplay-Heavy", size: 110)!
            ]
        )
        let strokeLabel = UILabel(frame: CGRect.zero)
        strokeLabel.attributedText = attributedString
        strokeLabel.backgroundColor = UIColor.clear
        strokeLabel.sizeToFit()
        strokeLabel.center = CGPoint(x: 0, y: 0) 
        return strokeLabel
    }
    func updateUIView(_ uiView: UILabel, context: Context) {}
}


struct TypeTagForVugaContent: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var content: VugaContent?
    var body: some View {
        HStack {
            Text(content?.type?.title.localized(language) ?? "")
                .outfitRegular(12)
                .foregroundColor(Color("textColor"))
                .padding(.trailing, 10)
                .padding(.bottom, 2)
                .padding(.leading,24)
        }
        .frame(width: 68, height: 30)
            .background(Color("bgColor"))
            .clipShape(Capsule(style: .continuous))
            .addStroke(radius: 100)
            .padding(.top, 10)
            .offset(x: -20)
    }
}

struct typeTag: View {
    var content: DownloadContent
    var isForTVShowsView: Bool
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var body: some View {
        if !isForTVShowsView {
            HStack {
                Text(content.type.title.localized(language))
                    .outfitRegular(12)
                    .foregroundColor(Color("textColor"))
                    .padding(.trailing, 10)
                    .padding(.bottom, 2)
                    .padding(.leading,24)
            }
            .frame(width: 68, height: 30)
                .background(Color("bgColor"))
                .clipShape(Capsule(style: .continuous))
                .addStroke(radius: 100)
                .padding(.top, 10)
                .offset(x: -20)
        }
    }
}
