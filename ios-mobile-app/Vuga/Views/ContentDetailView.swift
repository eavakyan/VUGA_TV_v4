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
import BranchSDK
import Flow
import WrappingStack
import MediaPlayer

struct ContentDetailView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    @AppStorage(SessionKeys.hasShownAirPlayGuidance) var hasShownAirPlayGuidance = false
    @StateObject var vm = ContentDetailViewModel()
    var homeVm : HomeViewModel?
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
    @EnvironmentObject var downloadViewModel: DownloadViewModel
    var contentId: Int?
    var body: some View {
        VStack {
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
                        // AirPlay button
                        AirPlayRoutePickerView(isConnected: isAirPlayConnected)
                            .frame(width: 44, height: 44)
                    }
                }
                .padding([.horizontal, .top],15)
            }
            ScrollView(showsIndicators: false) {
                LazyVStack(spacing: 12) {
                    if let content = vm.content {
                        KFImage(content.verticalPoster?.addBaseURL())
                            .resizeFillTo(width: Device.width * 0.75, height: Device.width * 1.1, radius: 15)
                            .addStroke(radius: 15)
                            .maxFrame()
                            .overlay(
                                HStack {
                                    PlayButton()
                                    Text(String.trailer.localized(language))
                                        .outfitMedium(20)
                                        .padding(.horizontal, 5)
                                        .padding(.trailing, 40)
                                }
                                    .padding(10)
                                    .background(Color.darkBg)
                                    .clipShape(Capsule())
                                    .addStroke(radius: 100,color: .base.opacity(0.5))
                                    .onTap {
                                        showTrailerSheet = true
                                    }
                                    .padding(.bottom)
                                    .offset(x: 40)
                                ,alignment: .bottomTrailing
                            )
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
                        HStack(spacing: 10) {
                            HStack {
                                Image.star
                                    .resizeFitTo(size: 16,renderingMode: .template)
                                Text(content.ratingString)
                                    .outfitLight()
                            }
                            .foregroundColor(.rating)
                            
                            verticalDivider
                            
                            Text(String(content.releaseYear ?? 0))
                                .outfitLight()
                                .foregroundColor(.textLight)
                            
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
                        
                        if content.type == .movie {
                            HStack(spacing: 12) {
                                PlayButton(size: 30)
                                Text(String.watchNow.localized(language))
                                    .outfitRegular(20)
                            }
                            .padding(10)
                            .maxWidthFrame()
                            .background(Color(hexString: "511B1B"))
                            .cornerRadius(15)
                            .addStroke(radius: 15, color: .base.opacity(0.3))
                            .onTap {
                                // Direct play without source selection
                                if let sources = content.contentSources, !sources.isEmpty {
                                    let firstSource = sources[0]
                                    if firstSource.accessType == .free || isPro {
                                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                                            vm.pickedSource = firstSource
                                            vm.progress = 0 // Start from beginning, could be fetched from recently watched
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
                            
                            // Download button for movies
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
                                .padding(.top, 10)
                                .onTap {
                                    handleDownload()
                                }
                            }
                        }
                        if !isPro && SessionManager.shared.getSetting()?.isAdmobIos != 0 {
                            BannerAd()
                                .padding(.horizontal,-10)
                                .padding(.top,10)
                        }
                        divider()
                        VStack(spacing: 8) {
                            Heading(title: .storyLine) {
                                Image.back
                                    .rotationEffect(.degrees(vm.isStoryLineOn ? 90 : 270))
                                    .onTap {
                                        vm.toggleStoryLine()
                                    }
                            }
                            
                            if vm.isStoryLineOn {
                                Text(content.description ?? "")
                                    .outfitLight(16)
                                    .foregroundColor(.textLight)
                                    .maxWidthFrame(.leading)
                            }
                        }
                        .animation(.linear, value: vm.isStoryLineOn)
                                                if content.contentCast?.isNotEmpty == true {
                            divider()
                            VStack(spacing: 8) {
                                Heading(title: .starCast) {
                                    Image.back
                                        .rotationEffect(.degrees(vm.isStarCastOn ? 90 : 270))
                                        .onTap {
                                            vm.toggleCastOn()
                                        }
                                }
                                if vm.isStarCastOn {
                                    PagingView(config: .init(margin: Device.width * 0.33,constrainedDeceleration: false), page: $currentIndex, {
                                        ForEach(0..<(content.contentCast ?? []).count, id: \.self) { index in
                                            StarCastCard(cast: (content.contentCast ?? [])[index])
                                                .onTapGesture {
                                                    Navigation.pushToSwiftUiView(CastView(actorId: content.contentCast?[index].actorID ?? 0))
                                                }
                                        }
                                    })
                                    .padding(.leading, -Device.width * 0.35)
                                    .frame(height: 55)
                                    .flipsForRightToLeftLayoutDirection(language == .Arabic ? true : false)
                                }
                            }
                        }
                        if content.type == .movie {
                            divider()
                            VStack(spacing: 8) {
                                Heading(title: .moreLikeThis)
                                ScrollView(.horizontal,showsIndicators: false, content: {
                                    LazyHStack(content: {
                                        ForEach(content.moreLikeThis ?? [], id: \.id) { content in
                                            ContentVerticalCard(vm: homeVm, content: content)
                                        }
                                    })
                                    .padding(.horizontal, 10)
                                })
                                .padding(.horizontal, -10)
                            }
                        }
                        if content.type == .series {
                            SeasonTags(vm: vm, content: content)
                            if let selectedSeason = vm.selectedSeason {
                                ForEach(selectedSeason.episodes ?? [], id: \.id) { episode in
                                    EpisodeCard(episode: episode, episodeTotalView: (episode.totalView ?? 0) + (episode.id == vm.selectedEpisode?.id ? episodeIncreaseTotalView : 0))
                                        .onTap {
                                            vm.selectedEpisode = episode
                                            // Direct play without source selection
                                            if let sources = episode.sources, !sources.isEmpty {
                                                let firstSource = sources[0]
                                                if firstSource.accessType == .free || isPro {
                                                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                                                        vm.pickedSource = firstSource
                                                        vm.progress = 0 // Start from beginning, could be fetched from recently watched
                                                        vm.playSource(firstSource)
                                                        vm.increaseEpisodeView(episodeId: episode.id ?? 0)
                                                        episodeIncreaseTotalView += 1
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
                                }
                            }
                        }
                    }
                }
                .padding(.horizontal, 10)
                .animation(.linear, value: vm.isStoryLineOn)
                .animation(.linear, value: vm.isStarCastOn)
                .padding(.vertical, 20)
            }
            .mask(VStack(spacing: 0){
                Rectangle().fill(LinearGradient(colors: [.clear, .black, .black], startPoint: .top, endPoint: .bottom))
                    .frame(height: 50)
                Rectangle()
                    .ignoresSafeArea()
            })
            .onChange(of: vm.isLoading, perform: { _ in
                
            })
        }
        .background(ContentBackgroudView(content: vm.content))
        .addBackground()
        .loaderView(vm.isLoading)
        .onAppear(perform: {
            if !vm.isDataLoaded {
                vm.fetchContest(contentId: contentId ?? 0)
            }
            // Watchlist state is now set from server response in fetchContest
            if !isPro && SessionManager.shared.getSetting()?.isAdmobIos != 0{
                Interstitial.shared.loadInterstitial()
            }
        })
        .onDisappear {
            if !isPro && SessionManager.shared.getSetting()?.isAdmobIos != 0 {
                Interstitial.shared.showInterstitialAds()
            }
        }
        // Removed onChange - watchlist state is now handled by server response
        .fullScreenCover(item: $vm.selectedSource, content: { source in
            if source.type?.rawValue ?? 1 == 1 {
                YoutubeView(youtubeUrl: source.source ?? "")
            } else {
                VideoPlayerView(content: vm.content,
                                episode: vm.selectedEpisode,
                                type: source.type?.rawValue ?? 2,
                                isShowAdView: isPro || SessionManager.shared.getSetting()?.isCustomIos == 0 ? false : vm.isShowAd,
                                url: source.sourceURL.absoluteString,progress: vm.progress,sourceId: vm.selectedSource?.id)
            }
        })
        .fullScreenCover(isPresented: $showTrailerSheet, content: {
            if let content = vm.content {
                if content.type == .movie {
                    YoutubeView(youtubeUrl: content.trailerURL ?? "")
                } else if content.type == .series {
                    YoutubeView(youtubeUrl: vm.selectedSeason?.trailerURL ?? "")
                }
            }
        })
        .customAlert(isPresented: $vm.isShowPremiumDialog){
            DialogCard(icon: Image.crown ,title: .subScribeToPro, iconColor: .rating, subTitle: .proDialogDes, buttonTitle: .becomeAPro, onClose: {
                withAnimation {
                    vm.isShowPremiumDialog = false
                }
            },onButtonTap: {
                vm.isShowPremiumDialog = false
                Navigation.pushToSwiftUiView(ProView())
            })
        }
        .customAlert(isPresented: $vm.isShowAdDialog){
            DialogCard(icon: Image.adLcok, title: .unlokeWithAd, subTitle: .adDialogDes, buttonTitle: .watchAd, onClose: {
                withAnimation {
                    vm.isShowAdDialog = false
                }
            },onButtonTap: {
                vm.isShowAdDialog = false
                // Show ad and then play video
                if let pickedSource = vm.pickedSource {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                        // After ad is watched, play the video
                        vm.progress = 0 // Start from beginning, could be fetched from recently watched
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
        .alert("Stream to TV", isPresented: $showAirPlayGuidance) {
            Button("Got it") {
                hasShownAirPlayGuidance = true
            }
        } message: {
            Text("Your TV is now connected! Simply press the Play button to stream the video to your TV.")
        }
        .alert("Download Started", isPresented: $showDownloadStartedAlert) {
            Button("OK") { }
        } message: {
            Text(downloadAlertMessage)
        }
        .onAppear {
            // Monitor AirPlay route changes
            airPlayObserver = NotificationCenter.default.addObserver(
                forName: AVAudioSession.routeChangeNotification,
                object: nil,
                queue: .main
            ) { notification in
                checkAirPlayConnection()
            }
            checkAirPlayConnection()
        }
        .onDisappear {
            // Clean up observer
            if let observer = airPlayObserver {
                NotificationCenter.default.removeObserver(observer)
            }
        }
        // TODO: Add DownloadProgressView to Xcode project
        // .overlay(
        //     // Download progress dialog
        //     Group {
        //         if showDownloadProgress, let source = downloadingSource, let content = vm.content {
        //             DownloadProgressView(
        //                 isShowing: $showDownloadProgress,
        //                 content: content,
        //                 source: source,
        //                 downloadId: source.sourceDownloadId(contentType: content.type ?? .movie)
        //             )
        //             .environmentObject(downloadViewModel)
        //         }
        //     }
        // )
    }
    
    func handleDownload() {
        guard let content = vm.content,
              let sources = content.contentSources,
              !sources.isEmpty else { return }
        
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
        
        // TODO: Add StorageManager to Xcode project for storage checking
        // if !StorageManager.shared.hasEnoughStorage(for: estimatedSize) {
        //     // Show storage error alert - could add an alert here
        //     return
        // }
        
        // Show download started alert since progress dialog is not available yet
        downloadAlertMessage = "Download started for \(content.title ?? "content"). You can continue browsing while it downloads in the background. Check your downloads in Profile → Downloads."
        showDownloadStartedAlert = true
        
        // Start download
        downloadViewModel.startDownload(content: content, episode: nil, source: firstSource, seasonNumber: 1)
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
                    // For series, play the first episode of the selected season
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
        guard let content = vm.content else { return }
        
        let buo = BranchUniversalObject(canonicalIdentifier: "\(content.id ?? 0)")
        buo.title = content.title ?? ""
        buo.contentDescription = content.description ?? ""
        buo.imageUrl = content.horizontalPoster?.addBaseURL()?.absoluteString
        buo.publiclyIndex = true
        buo.locallyIndex = true
        
        buo.contentMetadata.customMetadata["genre"] = content.genreString
        buo.contentMetadata.customMetadata["rating"] = content.ratingString
        
        let linkProperties: BranchLinkProperties = BranchLinkProperties()
        
        linkProperties.addControlParam(WebService.branchContentID, withValue: "\(contentId ?? 0)")
        
        buo.getShortUrl(with: linkProperties) { url, error in
            if let error = error {
                print(error.localizedDescription)
            } else if let url = url {
                print("Branch link: \(url)")
                shareLink(url)
                vm.increaseContentShare(contentId: content.id ?? 0)
            }
        }
    }
    
    func shareLink(_ url: String) {
        let AV = UIActivityViewController(activityItems: [url], applicationActivities: nil)
        UIApplication.shared.windows.first?.rootViewController?.present(AV, animated: true, completion: nil)
    }
    
}

#Preview {
    ContentDetailView()
}



private struct ContentBackgroudView: View {
    var content: FlixyContent?
    var body: some View {
        if let content {
            VStack {
                KFImage(content.verticalPoster?.addBaseURL())
                    .resizeFillTo(width: Device.width, height: Device.width * 1.2)
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
                    .flipsForRightToLeftLayoutDirection(language == .Arabic ? false : true)
                
                Text(cast.charactorName ?? "")
                    .outfitRegular(14)
                    .foregroundColor(.textLight)
                    .lineLimit(1)
                    .flipsForRightToLeftLayoutDirection(language == .Arabic ? false : true)
                
            }
            .flipsForRightToLeftLayoutDirection(language == .Arabic ? false : true)
            
            Spacer(minLength: 0)
        }
        .frame(width: Device.width * 0.60)
        .addStroke(radius: 15)
    }
}

private struct SeasonTags : View {
    @ObservedObject var vm: ContentDetailViewModel
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var content: FlixyContent
    var body: some View {
        if vm.content?.seasons?.isNotEmpty == true {
            VStack(spacing: 0) {
                divider(topPadding: 0)
                ScrollView(.horizontal, showsIndicators: false) {
                    LazyHStack {
                        ForEach(0..<(content.seasons ?? []).count, id: \.self) { index in
                            let season = (content.seasons ?? [])[index]
                            VStack(spacing: 0) {
                                Text("\(content.seasons?[index].title ?? "")")
                                    .outfitMedium(14)
                                    .frame(width: 70, alignment: .center)
                                    .padding(.vertical)
                                    .onTap {
                                        vm.selectSeason(season: season)
                                        vm.seasonNumber = index + 1
                                        print(vm.seasonNumber)
                                    }
                                    .foregroundColor(vm.selectedSeason?.id == season.id ? .text : .textLight)
                                Rectangle()
                                    .frame(width: 55, height: 1, alignment: .leading)
                                    .foregroundColor(vm.selectedSeason?.id == season.id ? .text : .clear)
                            }
                        }
                    }
                    .padding(.horizontal, 10)
                }
                
                divider(topPadding: 0)
            }
            .background(Color.text.opacity(0.10))
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
    var body: some View {
        VStack(alignment: .leading) {
            HStack(spacing: 10) {
                KFImage(episode.thumbnail?.addBaseURL())
                    .resizeFillTo(width: 150, height: 105, compressSize: 2,radius: 15)
                    .addStroke(radius: 15)
                    .overlay(
                        PlayButton(size: 30, color: .bg)
                            .addStroke(radius: 100)
                    )
                
                VStack(alignment: .leading, spacing: 6) {
                    Text(episode.title ?? "")
                        .outfitSemiBold(18)
                        .foregroundColor(.text)
                    
                    Text(episode.duration ?? "")
                        .outfitRegular(18)
                        .foregroundColor(.textLight)
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
        routePickerView.tintColor = UIColor(Color.text.opacity(0.8))
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
