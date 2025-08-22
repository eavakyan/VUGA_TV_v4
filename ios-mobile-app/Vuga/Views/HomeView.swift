//
//  HomeView.swift
//  Vuga
//
//

import SwiftUI
import Kingfisher
import ScalingHeaderScrollView
import WrappingStack
import CoreData



struct HomeView: View {
    @AppStorage(SessionKeys.language) private var language = LocalizationService.shared.language
    @StateObject private var vm = HomeViewModel()
    @StateObject private var recentlyWatchedVM = RecentlyWatchedViewModel()
    @StateObject private var userNotificationVM = UserNotificationViewModel()
    // @StateObject private var popupViewModel = PopupViewModel() // Removed - popup files deleted
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
                    VStack(spacing: 0) {
                        // Header with Logo and Profile Name
                        headerWithLogoAndProfile
                            .padding(.top, 0) // Moved higher on the page
                            .id("top") // Add ID for scroll anchor
                        
                        // Navigation Menu Row with horizontal category list
                        horizontalCategoryList
                            .padding(.top, 4)
                            .padding(.bottom, 4)
                            .zIndex(10) // Ensure it's above the featured content slider
                        
                        if vm.featured.isNotEmpty {
                            topBar
                                .frame(height: AdaptiveContentSizing.featuredContentHeight())
                        }
                        LazyVStack {
                            if vm.newReleases.isNotEmpty {
                                newReleasesCard
                            }
                            
                            // Always show Recently Watched section to allow data fetching
                            recentlyWatchedFromAPI
                            
                            if vm.wishlists.isNotEmpty {
                                // Watchlist row removed per requirements
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
                    cachedUniqueRecentlyWatched = [] // Clear cache to force refresh
                    fetchRecentlyWatchedContent()
                    recentlyWatchedVM.fetchRecentlyWatchedFromAPI() // Refresh API data
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
                    
                    // Fetch recently watched data when view appears
                    print("DEBUG: HomeView onAppear - calling fetchRecentlyWatchedFromAPI")
                    recentlyWatchedVM.fetchRecentlyWatchedFromAPI()
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
        .onChange(of: vm.selectedRecentlyWatched) { newValue in
            // When video player is dismissed, refresh recently watched
            if newValue == nil {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    recentlyWatchedVM.fetchRecentlyWatchedFromAPI()
                }
            }
        }
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
        .overlay(
            ZStack {
                // User notification overlay
                UserNotificationView(viewModel: userNotificationVM)
                
                // Popup overlay removed - files deleted
            }
        )
        .onAppear {
            // Check for pending notifications when view appears
            print("HomeView: onAppear - checking for notifications")
            userNotificationVM.checkPendingNotifications()
            
            // Popup checking removed - files deleted
        }
        .onChange(of: SessionManager.shared.currentProfile?.profileId) { newProfileId in
            // Clear shown notifications when profile changes
            if newProfileId != nil {
                userNotificationVM.clearShownNotifications()
                userNotificationVM.checkPendingNotifications()
            }
        }
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
            
            // Right side - Download icon only
            Button(action: {
                Navigation.pushToSwiftUiView(DownloadView())
            }) {
                Image.download
                    .resizeFitTo(size: 24, renderingMode: .template)
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
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
        HStack(spacing: 6) {
            // TV Shows button
            Text("TV Shows")
                .outfitMedium(16)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 38)
                .background(Color.clear)
                .contentShape(Rectangle())
                .onTapGesture {
                    print("TV Shows button tapped")
                    // Add haptic feedback
                    let impactFeedback = UIImpactFeedbackGenerator(style: .light)
                    impactFeedback.impactOccurred()
                    // Navigate to grouped view with TV Shows filter
                    Navigation.pushToSwiftUiView(
                        ContentGroupedView(
                            filterType: .tvShows,
                            title: "TV Shows"
                        )
                    )
                }
                    
            // Movies button
            Text("Movies")
                .outfitMedium(16)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 38)
                .background(Color.clear)
                .contentShape(Rectangle())
                .onTapGesture {
                    print("Movies button tapped")
                    // Add haptic feedback
                    let impactFeedback = UIImpactFeedbackGenerator(style: .light)
                    impactFeedback.impactOccurred()
                    // Navigate to grouped view with Movies filter
                    Navigation.pushToSwiftUiView(
                        ContentGroupedView(
                            filterType: .movies,
                            title: "Movies"
                        )
                    )
                }
                    
            // Live TV button
            Text("Live TV")
                .outfitMedium(16)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 38)
                .background(Color.clear)
                .contentShape(Rectangle())
                .onTapGesture {
                    print("Live TV button tapped")
                    // Add haptic feedback
                    let impactFeedback = UIImpactFeedbackGenerator(style: .light)
                    impactFeedback.impactOccurred()
                    // Navigate to Live TV view
                    Navigation.pushToSwiftUiView(LiveTVsView())
                }
                    
            // Categories button with menu
            Menu {
                // All genre categories
                ForEach(vm.genres, id: \.id) { genre in
                    Button(genre.title ?? "") {
                        Navigation.pushToSwiftUiView(
                            CategoryGridView(
                                categoryId: genre.id ?? 0,
                                categoryName: genre.title ?? "Category"
                            )
                        )
                    }
                }
                
                Divider()
                
                // Network/Distributor options
                Button("MediaTeka") {
                    Navigation.pushToSwiftUiView(
                        ContentGroupedView(
                            filterType: .distributor(1, "MediaTeka"),
                            title: "MediaTeka"
                        )
                    )
                }
                Button("HBO") {
                    Navigation.pushToSwiftUiView(
                        ContentGroupedView(
                            filterType: .distributor(2, "HBO"),
                            title: "HBO"
                        )
                    )
                }
            } label: {
                HStack(spacing: 4) {
                    Text("Categories")
                        .outfitMedium(16)
                    Image(systemName: "chevron.down")
                        .font(.system(size: 10))
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 38)
                .background(Color.clear)
            }
            .buttonStyle(PlainButtonStyle())
            .scaleEffect(1.0)
        }
        .padding(.horizontal, 12)
        .allowsHitTesting(true)
        .zIndex(10) // High z-index to ensure it's above everything
        .background(Color("bgColor")) // Black background to match the app
    }
    
    private var recentlyWatchedFromAPI: some View {
        VStack(alignment: .leading) {
            // Always show the section to help with debugging
            Heading(title: .recentlyWatched, content: {
                Text(String.seeAll.localized(language))
                    .outfitMedium(16)
                    .foregroundColor(.white)
                    .onTap {
                        Navigation.pushToSwiftUiView(
                            RecentlyWatchedGroupedView()
                        )
                    }
            })
            .padding(.horizontal, 10)
            
            if recentlyWatchedVM.isLoading {
                HStack {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .scaleEffect(0.8)
                    Spacer()
                }
                .frame(height: 105)
            } else {
                if recentlyWatchedVM.recentlyWatchedContents.isEmpty {
                    Text("No recently watched content")
                        .outfitMedium(14)
                        .foregroundColor(.gray)
                        .padding(.horizontal, 10)
                        .frame(height: 105)
                } else {
                    ScrollView(.horizontal, showsIndicators: false) {
                        LazyHStack(spacing: 10) {
                            ForEach(recentlyWatchedVM.recentlyWatchedContents) { content in
                                RecentlyWatchedAPICard(content: content, onDelete: {
                                    deleteRecentlyWatched(contentId: content.contentId)
                                })
                                .onTapGesture {
                                    navigateToContent(content)
                                }
                            }
                        }
                        .padding(.horizontal, 10)
                    }
                }
            }
        }
        .padding(.top, 10)
    }
    
    private func clearAllRecentlyWatched() {
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = RecentlyWatched.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        
        do {
            try DataController.shared.context.execute(deleteRequest)
            DataController.shared.saveData()
            print("Successfully cleared all Recently Watched data")
        } catch {
            print("Failed to clear Recently Watched data: \(error)")
        }
    }
    
    private func deleteRecentlyWatched(contentId: Int) {
        // Delete from Core Data
        let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "contentID == %d", contentId)
        
        do {
            let items = try DataController.shared.context.fetch(fetchRequest)
            for item in items {
                DataController.shared.context.delete(item)
            }
            DataController.shared.saveData()
            // Refresh the list
            recentlyWatchedVM.fetchRecentlyWatchedFromAPI()
        } catch {
            print("Failed to delete recently watched: \(error)")
        }
    }
    
    private func navigateToContent(_ content: RecentlyWatchedContent) {
        if content.isForDownload {
            // Handle download playback - create a RecentlyWatched object from the content
            let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
            fetchRequest.predicate = NSPredicate(
                format: "contentID == %d AND episodeId == %d", 
                content.contentId, 
                Int16(content.episodeId ?? 0)
            )
            
            do {
                let items = try DataController.shared.context.fetch(fetchRequest)
                if let recentlyWatched = items.first {
                    vm.selectedRecentlyWatched = recentlyWatched
                }
            } catch {
                print("Failed to fetch recently watched for playback: \(error)")
            }
        } else {
            // Navigate to content detail with progress
            let detailView = ContentDetailView(
                initialProgress: content.totalDuration > 0 ? content.progress / content.totalDuration : 0,
                contentId: content.contentId
            )
            Navigation.pushToSwiftUiView(detailView)
        }
    }
    
    private var recentlyWatched: some View {
        VStack(alignment: .leading) {
            Heading(title: .recentlyWatched, content: {
                // Button to clear recently watched (now available in all builds)
                /*
                Button(action: {
                    clearAllRecentlyWatched()
                    cachedUniqueRecentlyWatched = []
                    fetchRecentlyWatchedContent()
                }) {
                    Text("Clear All")
                        .outfitMedium(12)
                        .foregroundColor(.red)
                }
                */
            })
                .padding(.horizontal, 10)
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 10) {
                    ForEach(cachedUniqueRecentlyWatched, id: \.self) { recently in
                        VStack(alignment: .leading, spacing: 4) {
                            ZStack(alignment: .center) {
                                KFImage(recently.type == .movie ? recently.thumbnail.addBaseURL() : recently.episodeHorizontalPoster.addBaseURL())
                                    .resizeFillTo(width: 170, height: 105, radius: 10)
                                
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
                            .cornerRadius(radius: 15)
                            .addStroke(radius: 15)
                            
                            recentlyMetaView(recently)
                                .frame(width: 170, alignment: .topLeading)
                        }
                        .frame(width: 170)
                        .onTap {
                            navigateToDetailFromRecently(recently)
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
    
    @ViewBuilder
    private func recentlyMetaView(_ recently: RecentlyWatched) -> some View {
        let title: String = recently.name ?? ""
        if recently.type == .movie {
            let line1: String = movieLine1(from: recently)
            VStack(alignment: .leading, spacing: 2) {
                Text(line1)
                    .outfitLight(12)
                    .foregroundColor(.textLight)
                    .lineLimit(1)
                Text(title)
                    .lineLimit(2)
                    .outfitMedium(14)
                    .foregroundColor(.white)
                    .padding(.top, 1)
            }
        } else {
            let epTitle: String = seriesEpisodeTitle(from: recently)
            let dateStr: String? = recently.date.map { shortDateFromDate($0) }
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(epTitle)
                        .outfitMedium(12)
                        .foregroundColor(.white)
                        .lineLimit(1)
                    if let d = dateStr, !d.isEmpty {
                        Text("• \(d)")
                            .outfitLight(11)
                            .foregroundColor(.textLight)
                            .lineLimit(1)
                    }
                }
                Text(title)
                    .outfitMedium(14)
                    .foregroundColor(.white)
                    .lineLimit(2)
            }
        }
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
                LazyHStack(alignment: .top, spacing: 10) {
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
                                Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: topContent.contentID ?? 0))
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
                    .outfitMedium(16)
                    .foregroundColor(.white)
                    .onTap {
                        Navigation.pushToSwiftUiView(
                            ContentGridView(
                                filterType: .watchlist,
                                filterValue: nil,
                                navigationTitle: "My Watchlist"
                            )
                        )
                    }
            })
            .padding(.horizontal, 10)
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 10) {
                    ForEach(vm.wishlists, id: \.id) { content in
                        VStack(alignment: .leading,spacing: 0) {
                            KFImage(content.horizontalPoster?.addBaseURL())
                                .resizeFillTo(width: 170, height: 105,radius: 10)
                                .addStroke(radius: 10)
                                .overlay(
                                    TypeTagForVugaContent(content: content)
                                    ,alignment: .topLeading
                                )
                                .cornerRadius(radius: 15)

                            Text(content.title ?? "")
                                .lineLimit(1)
                                .outfitSemiBold(16)
                                .foregroundColor(Color("textColor"))
                                .padding(.top,5)
                                .frame(width: 118,alignment: .leading)
                            HStack(spacing: 7) {
                                HStack(spacing: 5) {
                                    Image.star
                                        .resizeFitTo(size: 12)
                                    Text(content.ratingString)
                                        .outfitLight(14)
                                }
                                .foregroundColor(Color("rating"))
                                Rectangle()
                                    .frame(width: 1, height: 15)
                                    .foregroundColor(.white)
                                Text(String(content.releaseYear ?? 0))
                                    .outfitLight(15)
                                    .foregroundColor(.white)
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
    
    private var newReleasesCard : some View {
        VStack(alignment: .leading) {
            Heading(title: "New Releases", content: {
                Text(String.seeAll.localized(language))
                    .outfitMedium(16)
                    .foregroundColor(.white)
                    .onTap {
                        Navigation.pushToSwiftUiView(
                            ContentGroupedView(
                                filterType: .newReleases,
                                title: "New Releases"
                            )
                        )
                    }
            })
            .padding(.horizontal, 10)
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(alignment: .top, spacing: 10) {
                    ForEach(vm.newReleases, id: \.id) { content in
                        ContentVerticalCard(vm: vm, content: content)
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
        .overlay(Color("bgColor").opacity(progress).allowsHitTesting(false))
    }
    
    private var featuredContentTabView: some View {
        GeometryReader { geometry in
            let contentSize = AdaptiveContentSizing.featuredContentSize(for: geometry)
            
            ZStack {
                ForEach(0..<vm.featured.count, id: \.self) { index in
                    featuredContentCard(feature: vm.featured[index])
                        .frame(width: contentSize.width, height: contentSize.height)
                        .position(x: geometry.size.width / 2, y: geometry.size.height / 2) // Center the poster
                        .offset(x: CGFloat(index - vm.selectedImageIndex) * geometry.size.width) // Move full width for each poster
                        .opacity(index == vm.selectedImageIndex ? 1.0 : 0.0) // Only show current poster
                        .animation(.easeInOut(duration: 0.6), value: vm.selectedImageIndex)
                }
            }
            .frame(width: geometry.size.width, height: geometry.size.height)
            .clipped()
            .contentShape(Rectangle())
            .simultaneousGesture(
                DragGesture()
                    .onEnded { value in
                        // Only respond to horizontal gestures
                        if abs(value.translation.width) > abs(value.translation.height) {
                            let threshold = contentSize.width * 0.2
                            withAnimation(.easeInOut(duration: 0.6)) {
                                if value.translation.width > threshold && vm.selectedImageIndex > 0 {
                                    vm.selectedImageIndex -= 1
                                } else if value.translation.width < -threshold && vm.selectedImageIndex < vm.featured.count - 1 {
                                    vm.selectedImageIndex += 1
                                }
                            }
                        }
                    }
            )
        }
        .frame(height: AdaptiveContentSizing.featuredContentHeight())
    }
    
    private func featuredContentCard(feature: VugaContent) -> some View {
        GeometryReader { geometry in
            ZStack {
                // Background shadow/glow effect
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.black.opacity(0.2))
                    .blur(radius: 10)
                    .scaleEffect(0.95)
                
                // Single full poster image, properly scaled to fit
                featuredPosterImage(feature: feature)
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .cornerRadius(12)
                    .shadow(color: .black.opacity(0.3), radius: 10, x: 0, y: 5)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        // Navigate to content detail when poster is tapped
                        Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: feature.id ?? 0))
                    }
                
                // Gradient overlay for better text visibility
                LinearGradient(
                    colors: [.clear, .black.opacity(0.2), .black.opacity(0.7)],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .allowsHitTesting(false)
                
                // Content overlay centered
                VStack(spacing: 12) {
                    Spacer()
                    
                    // Title with adaptive sizing
                    let fontSizes = AdaptiveContentSizing.featuredContentFontSizes()
                    Text(feature.title ?? "")
                        .outfitBold(fontSizes.title)
                        .foregroundColor(.white)
                        .lineLimit(2)
                        .multilineTextAlignment(.center)
                        .shadow(color: .black.opacity(0.8), radius: 4, x: 0, y: 2)
                    
                    // Action buttons with adaptive sizing
                    HStack(spacing: AdaptiveContentSizing.isIPad ? 15 : 10) {
                        // WATCH NOW button
                        Button(action: {
                            handlePlayAction(feature: feature)
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: "play.fill")
                                    .font(.system(size: fontSizes.subtitle - 2))
                                Text("WATCH NOW")
                                    .outfitSemiBold(fontSizes.subtitle)
                                    .tracking(0.5)
                            }
                            .foregroundColor(Color.gray.opacity(0.9))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, AdaptiveContentSizing.isIPad ? 14 : 12)
                            .background(Color.white)
                            .clipShape(Capsule())
                        }
                        .frame(width: AdaptiveContentSizing.isIPad ? min(geometry.size.width * 0.35, 200) : geometry.size.width * 0.45)
                        
                        // + MY LIST button
                        Button(action: {
                            handleWatchlistAction(feature: feature)
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: isInWatchlist(contentId: feature.id ?? 0) ? "checkmark" : "plus")
                                    .font(.system(size: fontSizes.subtitle - 2, weight: .bold))
                                Text("MY LIST")
                                    .outfitSemiBold(fontSizes.subtitle)
                                    .tracking(0.5)
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, AdaptiveContentSizing.isIPad ? 14 : 12)
                            .background(Color.gray.opacity(0.3))
                            .overlay(
                                Capsule()
                                    .stroke(Color.white.opacity(0.3), lineWidth: 1)
                            )
                            .clipShape(Capsule())
                        }
                        .frame(width: AdaptiveContentSizing.isIPad ? min(geometry.size.width * 0.35, 200) : geometry.size.width * 0.45)
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
            .aspectRatio(contentMode: .fit) // Changed to .fit to show full poster
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
                    .animation(.easeInOut(duration: 0.6), value: vm.selectedImageIndex)
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
    
    // MARK: Recently Watched helpers (scoped to HomeView)
    private func movieLine1(from recently: RecentlyWatched) -> String {
        // RecentlyWatched entity doesn't have releaseYear, so we'll just show duration
        let durationInSeconds = recently.totalDuration
        if durationInSeconds > 0 {
            let minutes = Int(durationInSeconds / 60)
            if minutes >= 60 {
                return String(format: "%d hr %02d min", minutes / 60, minutes % 60)
            }
            return "\(minutes) min"
        }
        return ""
    }
    private func seriesEpisodeTitle(from recently: RecentlyWatched) -> String {
        // Use episodeName which exists in the RecentlyWatched entity
        if let title = recently.episodeName, !title.isEmpty { 
            return title 
        }
        // If no episode name, just show a default
        return "Episode"
    }
    private func shortDateFromDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: date)
    }
    
    private func shortDateString(fromISO iso: String) -> String? {
        let fmts = ["yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"]
        for f in fmts {
            let df = DateFormatter()
            df.locale = Locale(identifier: "en_US_POSIX")
            df.dateFormat = f
            if let d = df.date(from: iso) {
                let out = DateFormatter()
                out.dateStyle = .medium
                out.timeStyle = .none
                return out.string(from: d)
            }
        }
        return nil
    }
    private func formattedDuration(minutesOrHMS: String?) -> String {
        guard let val = minutesOrHMS, !val.isEmpty else { return "" }
        if let minutes = Int(val) {
            if minutes >= 60 { return String(format: "%d hr %02d min", minutes/60, minutes%60) }
            return "\(minutes) min"
        }
        let parts = val.split(separator: ":").compactMap { Int($0) }
        if parts.count == 3 { return String(format: "%d hr %02d min", parts[0], parts[1]) }
        if parts.count == 2 { return String(format: "%d hr %02d min", parts[0]/60, parts[0]%60) }
        return val
    }
    private func progressFraction(for recently: RecentlyWatched) -> Double {
        let total: Double = Double(recently.totalDuration)
        let progress: Double = Double(recently.progress)
        if !(total.isFinite) || total <= 0 { return 0 }
        let frac = progress / max(total, 0.001)
        return min(1.0, max(0.0, frac))
    }
    private func navigateToDetailFromRecently(_ recently: RecentlyWatched) {
        let fraction: Double = progressFraction(for: recently)
        let cid: Int = Int(recently.contentID)
        let detailView = ContentDetailView(initialProgress: fraction, contentId: cid)
        Navigation.pushToSwiftUiView(detailView)
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
                    .outfitMedium(16)
                    .foregroundColor(.white)
                    .onTap {
                        Navigation.pushToSwiftUiView(
                            CategoryGridView(
                                categoryId: genre.id ?? 0,
                                categoryName: genre.title ?? "Category"
                            )
                        )
                    }
            })
            .padding(.horizontal, 10)
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(alignment: .top, spacing: 10) {
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
            .resizeFillTo(width: 125, height: 178, radius: 5)
            .addStroke(radius: 5)
            .frame(width: 125)
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
                .outfitRegular(13)
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
                    .outfitRegular(13)
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

// Embedded RecentlyWatchedCard since file import isn't working
struct RecentlyWatchedAPICard: View {
    let content: RecentlyWatchedContent
    let onDelete: () -> Void
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    private var posterUrl: String? {
        if content.contentType == ContentType.movie.rawValue {
            return content.horizontalPoster
        } else if let episodeThumbnail = content.episodeInfo?.episodeThumbnail {
            return episodeThumbnail
        } else {
            return content.horizontalPoster
        }
    }
    
    private var displayTitle: String {
        content.contentName
    }
    
    private var episodeTitle: String? {
        content.episodeInfo?.episodeTitle
    }
    
    private var progressFraction: Double {
        guard content.totalDuration > 0 else { return 0 }
        return min(1.0, max(0.0, content.progress / content.totalDuration))
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            ZStack(alignment: .center) {
                // Poster image
                KFImage(posterUrl?.addBaseURL())
                    .resizeFillTo(width: 170, height: 105, radius: 10)
                
                // Play button
                Image(systemName: "play.fill")
                    .rotationEffect(.degrees(language == .Arabic ? 180 : 0))
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(.white)
                    .padding(8)
                    .padding(.leading, 2)
                    .background(.white.opacity(0.2))
                    .clipShape(Circle())
                
                // Close button
                VStack {
                    HStack {
                        Spacer()
                        Image.close
                            .resizeFitTo(size: 12, renderingMode: .template)
                            .foregroundStyle(.white)
                            .padding(12)
                            .onTapGesture {
                                onDelete()
                            }
                    }
                    Spacer()
                }
                
                // Progress bar
                VStack {
                    Spacer()
                    ProgressView(value: progressFraction)
                        .progressViewStyle(LinearProgressViewStyle())
                        .tint(Color("baseColor"))
                }
            }
            .cornerRadius(radius: 15)
            .addStroke(radius: 15)
            
            // Metadata
            metadataView
                .frame(width: 170, alignment: .topLeading)
        }
        .frame(width: 170)
    }
    
    @ViewBuilder
    private var metadataView: some View {
        VStack(alignment: .leading, spacing: 2) {
            // First line: Year and Duration in grey
            HStack(spacing: 4) {
                if let year = content.releaseYear {
                    Text(String(year))
                        .outfitLight(12)
                        .foregroundColor(.textLight)
                }
                
                if content.releaseYear != nil && formattedDuration != nil {
                    Text("•")
                        .outfitLight(12)
                        .foregroundColor(.textLight)
                }
                
                if let duration = formattedDuration {
                    Text(duration)
                        .outfitLight(12)
                        .foregroundColor(.textLight)
                }
            }
            .lineLimit(1)
            
            // Second line: Title in white
            Text(displayTitle)
                .lineLimit(2)
                .outfitMedium(14)
                .foregroundColor(.white)
        }
    }
    
    private var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: content.watchedDate)
    }
    
    private var formattedDuration: String? {
        guard let duration = content.contentDuration, duration > 0 else { 
            return nil 
        }
        
        // Duration is stored in seconds in the database
        let totalSeconds = duration
        let hours = totalSeconds / 3600
        let minutes = (totalSeconds % 3600) / 60
        
        if hours > 0 && minutes > 0 {
            return "\(hours) hr \(minutes) min"
        } else if hours > 0 {
            return "\(hours) hr"
        } else if minutes > 0 {
            return "\(minutes) min"
        } else {
            return nil
        }
    }
}
