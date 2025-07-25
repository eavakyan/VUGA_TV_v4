//
//  ContentDetailView.swift
//  Flixy
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

struct ContentDetailView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    @StateObject var vm = ContentDetailViewModel()
    var homeVm : HomeViewModel?
    @State var showTrailerSheet = false
    @State private var currentIndex : Int = 0
    @State var episodeIncreaseTotalView = 0
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
                                verticalDivider
                                TotalWatchTag(totalViews: ((content.totalView ?? 0) + episodeIncreaseTotalView).roundedWithAbbreviations)
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
                                vm.selectSources(data: content.contentSources ?? [])
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
                                            vm.selectSources(data: episode.sources ?? [])
                                            vm.selectedEpisode = episode
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
        .blur(radius: vm.isSourceSheetOn ? 7 : 0)
        .overlay(AvailableSourcesView(vm: vm, episodeIncreaseTotalView: $episodeIncreaseTotalView, content: vm.content, seasonNumber: vm.seasonNumber))
        .background(ContentBackgroudView(content: vm.content))
        .addBackground()
        .loaderView(vm.isLoading)
        .onAppear(perform: {
            if !vm.isDataLoaded {
                vm.fetchContest(contentId: contentId ?? 0)
            }
            vm.isBookmarked = vm.myUser?.checkIsAddedToWatchList(contentId: vm.content?.id ?? 0) ?? false
            print("ppppppppp........",vm.isBookmarked)
            if !isPro && SessionManager.shared.getSetting()?.isAdmobIos != 0{
                Interstitial.shared.loadInterstitial()
            }
        })
        .onDisappear {
            if !isPro && SessionManager.shared.getSetting()?.isAdmobIos != 0 {
                Interstitial.shared.showInterstitialAds()
            }
        }
        .onChange(of: vm.isDataLoaded, perform: { _ in
            if vm.isDataLoaded == true {
                vm.isBookmarked = vm.myUser?.checkIsAddedToWatchList(contentId: vm.content?.id ?? 0) ?? false
                print("pppppppppppp",vm.isBookmarked)
            }
        })
        .animation(.default, value: vm.isSourceSheetOn)
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

struct TotalWatchTag : View {
    var totalViews: String
    var body: some View {
        HStack {
            Image.eye
                .resizeFitTo(size: 16, renderingMode: .template)
            Text(totalViews)
                .outfitLight(16)
        }
        .foregroundColor(.base)
    }
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
                    TotalWatchTag(totalViews: episodeTotalView.roundedWithAbbreviations)
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
