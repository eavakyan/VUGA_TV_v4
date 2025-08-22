//
//  AvailableSourceView.swift
//  Vuga
//
//

import Foundation
import SwiftUI
import AVKit
import ActivityKit
import CoreData

struct AvailableSourcesView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm: ContentDetailViewModel
    @Binding var episodeIncreaseTotalView : Int
    var content: VugaContent?
    var seasonNumber: Int
    
    var body: some View {
        if vm.isSourceSheetOn {
            VStack {
                Text(String.availableSources.localized(language))
                    .outfitRegular(24)
                    .foregroundColor(.text)
                
                ScrollView(showsIndicators: false) {
                    LazyVStack(spacing: 10) {
                        ForEach(vm.sources, id: \.id) { source in
                                DownloadableSourceView(contentModel: vm, episodeIncreaseTotalView: $episodeIncreaseTotalView, source: source,content: content,seasonNumber: seasonNumber)
                        }
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 100)
                }
                .mask(VStack(spacing: 0) {
                    Rectangle().fill(LinearGradient(colors: [.clear, .clear, .black], startPoint: .top, endPoint: .bottom))
                        .frame(height: 80)
                    Rectangle()
                    Rectangle().fill(LinearGradient(colors: [.black, .clear, .clear], startPoint: .top, endPoint: .bottom))
                        .frame(height: 80)
                })
                BottomXMarkButton {
                    vm.closeSourceSheet()
                }
            }
            .padding(.vertical)
            .background(Color.bg.opacity(0.9))
            .onAppear {
                UIDevice.current.setValue(UIInterfaceOrientation.portrait.rawValue, forKey: "orientation")
                AppDelegate.orientationLock = .portrait
            }
        }
    }
}

struct DownloadableSourceView: View {
    @AppStorage(SessionKeys.isPro) var isPro = false
    @State private var recentlyWatchedItems: [RecentlyWatched] = [] // To store fetched recently watched items.
    @FetchRequest(sortDescriptors: []) var downloads: FetchedResults<DownloadContent>
    @EnvironmentObject var downloadViewModel: DownloadViewModel
    @StateObject var contentModel: ContentDetailViewModel
    @Binding var episodeIncreaseTotalView: Int

    var source: Source
    var content: VugaContent?
    var seasonNumber: Int
    
    var downloadContent: DownloadingContent? {
        downloadViewModel.downloadingContents[source.sourceDownloadId(contentType: content?.type ?? .movie)]
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 20) {
                PlayButton(size: 30)
                VStack(alignment: .leading, spacing: 3) {
                    Text(source.title ?? "")
                        .outfitSemiBold(18)
                        .foregroundColor(.text)
                    HStack(alignment: .bottom) {
                        Text(source.quality ?? "")
                            .outfitRegular(16)
                            .foregroundColor(.base)
                        Text(source.size ?? "")
                            .outfitRegular(12)
                            .foregroundColor(.textLight)
                    }
                }
                .lineLimit(1)
                Spacer(minLength: 0)
                HStack(spacing: 10) {
                    if source.accessType == .premium && !isPro {
                        Image.crown
                            .resizeFitTo(size: 18, renderingMode: .template)
                            .frame(width: 38, height: 38, alignment: .center)
                            .foregroundColor(.rating)
                            .background(Color.bg)
                            .cornerRadius(radius: 12)
                            .addStroke(radius: 12, color: .rating)
                            .background(
                                Color.rating
                                    .opacity(0.8)
                                    .scaleEffect(0.8)
                                    .blur(radius: 10)
                            )
                    } else {
                        if source.isDownload == 1 {
                            ZStack {
                                let downloadStatus = downloadContent?.downloadStatus ?? .notStarted
                                switch downloadStatus {
                                case .notStarted:
                                    Image.download
                                        .resizeFitTo(size: 15, renderingMode: .template)
                                case .downloading:
                                    Image.downloadPause
                                        .resizeFitTo(size: 15, renderingMode: .template)
                                case .paused:
                                    Image.downloadPlay
                                        .resizeFitTo(size: 15, renderingMode: .template)
                                case .downloaded:
                                    Image.downloaded
                                        .resizeFitTo(size: 18, renderingMode: .template)
                                        .foregroundColor(.bg)
                                        .frame(width: 38, height: 38, alignment: .center)
                                        .background(Color.text)
                                        .cornerRadius(radius: 12)
                                        .addStroke(radius: 12)
                                case .queued:
                                    Image.timer
                                        .resizeFitTo(size: 18, renderingMode: .template)
                                }
                            }
                            .foregroundColor(.text)
                            .frame(width: 38, height: 38, alignment: .center)
                            .background(Color.bg)
                            .cornerRadius(radius: 12)
                            .addStroke(radius: 12)
                            .onTap {
                                handleDownloadAction()
                            }
                        }
                    }
                    if source.accessType == .locked && !isPro {
                        Image.lock
                            .resizeFitTo(size: 18, renderingMode: .template)
                            .frame(width: 38, height: 38, alignment: .center)
                            .foregroundColor(.text)
                            .background(Color.bg)
                            .cornerRadius(radius: 12)
                            .addStroke(radius: 12)
                    }
                }
            }
            .padding()
            .padding(.leading, 10)
            .padding(.trailing, 5)
            if let progress = downloadContent?.progress,
               downloadContent?.downloadStatus == .downloading || downloadContent?.downloadStatus == .paused || downloadContent?.downloadStatus == .queued {
                ProgressView(value: progress)
                    .progressViewStyle(LinearProgressViewStyle())
                    .tint(.base)
            } else if let recentlyWatched = recentlyWatchedItems.first {
                ProgressView(value: (recentlyWatched.progress ?? 0) / (recentlyWatched.totalDuration ?? 1))
                    .progressViewStyle(LinearProgressViewStyle())
                    .tint(.base)
            }
        }
        .onAppear {
            fetchRecentlyWatched()
        }
        .background(Color.bg)
        .cornerRadius(radius: 18)
        .addStroke(radius: 18)
        .onTap {
            if source.accessType == .free || isPro {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    contentModel.pickedSource = source
                    contentModel.progress = recentlyWatchedItems.first?.progress ?? 0
                    startVideo()
                    increaceTotalView()
                }
            } else if source.accessType == .premium {
                contentModel.isShowPremiumDialog = true
            } else if source.accessType == .locked {
                contentModel.isShowAdDialog = true
                contentModel.pickedSource = source
                contentModel.progress = recentlyWatchedItems.first?.progress ?? 0
            }
        }
    }
    
    // MARK: Fetch Recently Watched Data
    private func fetchRecentlyWatched() {
        let context = DataController.shared.context
        let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "contentSourceId == %d AND contentType == %d", source.id ?? 0, Int16(content?.type?.rawValue ?? 1))
        
        do {
            recentlyWatchedItems = try context.fetch(fetchRequest)
        } catch {
            print("Error fetching recently watched items: \(error.localizedDescription)")
            recentlyWatchedItems = []
        }
    }

    private func increaceTotalView() {
        if contentModel.content?.type == .movie {
            episodeIncreaseTotalView += 1
        } else if contentModel.content?.type == .series {
            episodeIncreaseTotalView += 1
        }
    }

    private func startVideo(isShowAd: Bool = true) {
        if contentModel.pickedSource?.type == .embeddedURL {
            if let url = contentModel.pickedSource?.sourceURL {
                Navigation.pushToSwiftUiView(EmbeddedWebView(url: url))
            }
        } else {
            contentModel.selectedSource = contentModel.pickedSource
            contentModel.isShowAd = isShowAd
        }
    }

    private func handleDownloadAction() {
        let status = downloadContent?.downloadStatus ?? .notStarted
        let sourceId = source.sourceDownloadId(contentType: content?.type ?? .movie)
        switch status {
        case .notStarted:
            if let content = self.content {
                if source.accessType == .locked && !isPro {
                    contentModel.isShowDownloadAdDialog = true
                    contentModel.pickedSource = source
                } else {
                    downloadViewModel.startDownload(content: content, episode: contentModel.selectedEpisode, source: source, seasonNumber: seasonNumber)
                }
            }
        case .downloading:
            print("Downloading...")
            downloadViewModel.pauseDownload(sourceId: sourceId)
            downloadViewModel.downloadingContents[sourceId]?.downloadStatus = .paused
        case .paused:
            downloadViewModel.resumeDownload(for: sourceId)
        case .downloaded:
            print("Downloaded")
        case .queued:
            print("Queue")
        }
    }
}

