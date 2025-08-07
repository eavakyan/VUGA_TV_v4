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
            ScrollView(showsIndicators: false) {
                VStack {
                    // Header with Logo and Profile Name
                    headerWithLogoAndProfile
                        .padding(.top, 50) // Fixed top padding for proper spacing
                    
                    // Navigation Menu Row with horizontal category list
                    horizontalCategoryList
                        .padding(.bottom, 10)
                    
                    if vm.featured.isNotEmpty {
                        topBar
                            .frame(height: Device.height * 0.6)
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
            TopBar(isBlur: false)
        }
        .onChange(of: selectedTab, perform: { newValue in
                if newValue == Tab.home {
                    progress = 0
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
                    .foregroundColor(.text)
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
                        .foregroundColor(.textLight)
                        .frame(width: 35, height: 35)
                        .background(Color.searchBg)
                        .clipShape(Circle())
                }
                
                // Downloads icon
                Button(action: {
                    Navigation.pushToSwiftUiView(DownloadView())
                }) {
                    Image.download
                        .resizeFitTo(size: 16, renderingMode: .template)
                        .foregroundColor(.textLight)
                        .frame(width: 35, height: 35)
                        .background(Color.searchBg)
                        .clipShape(Circle())
                }
                
                // Search icon
                Button(action: {
                    selectedTab = .search
                }) {
                    Image.search
                        .resizeFitTo(size: 16, renderingMode: .template)
                        .foregroundColor(.textLight)
                        .frame(width: 35, height: 35)
                        .background(Color.searchBg)
                        .clipShape(Circle())
                }
            }
        }
        .padding(.horizontal, 15)
    }
    
    // Horizontal Category List Component
    @State private var selectedGenre: Genre?
    
    private var horizontalCategoryList: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Section title
            HStack {
                Text("Categories")
                    .outfitSemiBold(16)
                    .foregroundColor(.text)
                    .padding(.leading, 15)
                Spacer()
            }
            
            // Horizontal scrollable category list
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 12) {
                    ForEach(vm.genres, id: \.id) { genre in
                        Button(action: {
                            selectedGenre = genre
                            Navigation.pushToSwiftUiView(GenreContentsView(genre: genre))
                        }) {
                            Text(genre.title ?? "")
                                .outfitMedium(14)
                                .foregroundColor(selectedGenre?.id == genre.id ? .text : .textLight)
                                .padding(.horizontal, 20)
                                .padding(.vertical, 8)
                                .background(selectedGenre?.id == genre.id ? Color.base : Color.clear)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 15)
                                        .stroke(selectedGenre?.id == genre.id ? Color.clear : Color.strokeColor, lineWidth: 1)
                                )
                                .clipShape(RoundedRectangle(cornerRadius: 15))
                        }
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
                                        .resizeFillTo(width: 210, height: 130, radius: 10)
                                    
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
                                            .tint(.base)
                                    }
                                }
                            }
                            .cornerRadius(radius: 15)
                            .addStroke(radius: 15)
                            HStack {
                                Text(recently.name ?? "")
                                    .lineLimit(1)
                                    .outfitSemiBold(18)
                                    .foregroundColor(.text)
                                    .padding(.top, 5)
                                Spacer(minLength: 5)
                                Image.info
                                    .resizeFitTo(size: 20, renderingMode: .template)
                                    .foregroundStyle(.white)
                                    .onTap {
                                        Navigation.pushToSwiftUiView(ContentDetailView(contentId: Int(recently.contentID)))
                                    }
                            }
                                .frame(width: 210, alignment: .leading)
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
                                .resizeFillTo(width: 140, height: 200,radius: 5)
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
                    .foregroundColor(.textLight)
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
                                .resizeFillTo(width: 210, height: 130,radius: 10)
                                .addStroke(radius: 10)
                                .overlay(
                                    TypeTagForVugaContent(content: content)
                                    ,alignment: .topLeading
                                )
                                .cornerRadius(radius: 15)

                            Text(content.title ?? "")
                                .lineLimit(1)
                                .outfitSemiBold(18)
                                .foregroundColor(.text)
                                .padding(.top,5)
                                .frame(width: 210,alignment: .leading)
                            HStack(spacing: 7) {
                                HStack(spacing: 5) {
                                    Image.star
                                        .resizeFitTo(size: 12)
                                    Text(content.ratingString)
                                        .outfitLight(16)
                                }
                                .foregroundColor(.rating)
                                Rectangle()
                                    .frame(width: 1, height: 15)
                                    .foregroundColor(.textLight)
                                Text(verbatim: "\(content.releaseYear ?? 0)")
                                    .outfitLight(17)
                                    .foregroundColor(.textLight)
                            }
                            
                            .padding(.top,3)
                        }
                        .onTap {
                            Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: content.id))
                        }
                        
                    }
                }
                .padding(.horizontal, 10)
            }
        }
        .padding(.top,10)
    }
    
    private var topBar : some View {
        ZStack(alignment: .center) {
            TabView(selection: $vm.selectedImageIndex) {
                ForEach(0..<vm.featured.count, id: \.self) { index in
                    let feature = vm.featured[index]
                    KFImage(feature.verticalPoster?.addBaseURL())
                        .resizeFillTo(width: Device.width * 0.75, height: Device.width * 1.03, radius: 15)
                        .addStroke(radius: 15)
                        .maxFrame(.top)
                        .padding(.top)
                        .onTapGesture {
                            Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: feature.id))
                        }
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            if let feature = vm.featured[safe: vm.selectedImageIndex] {
                VStack(alignment: .center, spacing: 3) {
                    Spacer()
                    Text(feature.title ?? "")
                        .outfitSemiBold(24)
                        .foregroundColor(.text)
                        .lineLimit(1)
                        .padding([.top,.horizontal])
                    WrappingHStack(id: \.self,horizontalSpacing: 8) {
                        let fliteredArray = feature.genres.prefix(4)
                        ForEach(fliteredArray.indices, id: \.self) { index in
                            HStack(alignment: .center) {
                                Text(feature.genres[index].title ?? "")
                                    .outfitLight()
                                    .foregroundColor(.textLight)
                                if fliteredArray.count != index + 1{
                                    Circle()
                                        .fill(.textLight.opacity(0.6))
                                        .frame(width: 4,height: 4)
                                }
                            }
                        }
                    }
                    .padding(.bottom,5)
                    HStack {
                        HStack {
                            Image.star
                                .resizeFitTo(size: 16)
                            Text(feature.ratingString)
                                .outfitSemiBold()
                        }
                        .foregroundColor(.rating)
                        Rectangle()
                            .frame(width: 1, height: 15)
                            .foregroundColor(.textLight)
                        
                        Text(verbatim: "\(feature.releaseYear ?? 2020)")
                            .outfitSemiBold()
                            .foregroundColor(.textLight)
                    }
                }
                .animation(.default,value: vm.selectedImageIndex)
            }
        }
        .overlay(Color.bg.opacity(progress).allowsHitTesting(progress == 0 ? false : true))
        .background(
                        KFImage(vm.featured[safe: vm.selectedImageIndex]?.verticalPoster?.addBaseURL())
                            .resizeFillTo(width: Device.width, height: Device.height)
                            .clipped()
                            .blur(radius: 10)
                            .opacity(0.3)
                            .overlay(
                                LinearGradient(colors: [.clear,.clear,.bg], startPoint: .top, endPoint: .bottom)
                                    .frame(height: Device.height)
                                ,alignment: .bottom
                            )
                            .animation(.default,value: vm.selectedImageIndex)
                            .allowsHitTesting(false)
                    )
    }
    
    // Helper function to get profile greeting
    private func getProfileGreeting() -> String {
        guard let user = SessionManager.shared.getUser(),
              let profile = user.lastActiveProfile else {
            return "Hi, User"
        }
        
        let profileName = profile.name?.isEmpty == false ? profile.name! : "User"
        return "Hi, \(profileName)"
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
                    .foregroundColor(.textLight)
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
            .resizeFillTo(width: 140, height: 200, radius: 5)
            .addStroke(radius: 5)
            .onTap {
                Navigation.pushToSwiftUiView(ContentDetailView(homeVm: vm, contentId: content.id))
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
                NSAttributedString.Key.foregroundColor: UIColor.bg.withAlphaComponent(0.4),
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
                .foregroundColor(.text)
                .padding(.trailing, 10)
                .padding(.bottom, 2)
                .padding(.leading,24)
        }
        .frame(width: 68, height: 30)
            .background(Color.bg)
            .clipShape(Capsule(style: .continuous))
            .addStroke(radius: 100)
            .padding(.top, 10)
            .offset(x: -20)
    }
}

struct typeTag: View {
    var content: DownloadContent
    var isForSeriesView: Bool
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var body: some View {
        if !isForSeriesView {
            HStack {
                Text(content.type.title.localized(language))
                    .outfitRegular(12)
                    .foregroundColor(.text)
                    .padding(.trailing, 10)
                    .padding(.bottom, 2)
                    .padding(.leading,24)
            }
            .frame(width: 68, height: 30)
                .background(Color.bg)
                .clipShape(Capsule(style: .continuous))
                .addStroke(radius: 100)
                .padding(.top, 10)
                .offset(x: -20)
        }
    }
}
