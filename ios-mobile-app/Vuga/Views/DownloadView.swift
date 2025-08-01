//
//  DownloadView.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 10/06/24.
//

import SwiftUI
import Kingfisher
import CoreData
import ActivityKit

struct DownloadView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @EnvironmentObject var vm : DownloadViewModel
    @FetchRequest(sortDescriptors: []) var downloads : FetchedResults<DownloadContent>
    @State var showAllDeleteDialog = false
    @State var filterdDownloads : [DownloadContent] = []
    var isOffLineView = false
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                BackButton()
                Text(String.deleteAll.localized(language))
                    .outfitMedium(15)
                    .foregroundColor(.base)
                    .hidden()
                Spacer()
                Text(String.downloads.localized(language))
                    .outfitSemiBold(20)
                    .foregroundColor(.text)
                Spacer()
                BackButton()
                    .hidden()
                if downloads.isNotEmpty && downloads.count > 1 {
                    Text(String.deleteAll.localized(language))
                        .outfitMedium(15)
                        .foregroundColor(.base)
                        .onTap {
                            showAllDeleteDialog = true
                        }
                } else {
                    Text(String.deleteAll.localized(language))
                        .outfitMedium(15)
                        .hidden()
                }
            }
            .padding(.horizontal)
            .padding(.vertical,10)
            
            ScrollView(showsIndicators: false) {
                LazyVStack(spacing: 15) {
                    ForEach(filterdDownloads, id: \.id) { content in
                        DownloadCardView(content: content,isOffLineView: isOffLineView)
                    }
                }
                .padding(.vertical, 15)
                .padding(.horizontal,5)
            }
        }
        .hideNavigationbar()
        .onAppear(perform: {
            AppDelegate.setOrientation(.portrait)
            filterdDownloads = removeDuplicateElements(downloads: downloads)
        })
        .customAlert(isPresented: $showAllDeleteDialog){
            DialogCard(icon: Image.delete, title: .areYouSure, subTitle: .deleteAllDes, buttonTitle: .delete, onClose: {
                withAnimation {
                    showAllDeleteDialog = false
                }
            },onButtonTap: {
                showAllDeleteDialog = false
                for download in downloads {
                    let downloadContent = vm.downloadingContents[download.downloadId ?? ""]
                    let id = download.downloadId
                    if downloadContent?.downloadStatus == .downloading {
                        vm.pauseDownload(sourceId: id ?? "", isForDelete: true)
                    }
                    if  downloadContent?.downloadStatus == .downloading || downloadContent?.downloadStatus == .paused || downloadContent?.downloadStatus == .queued {
                        vm.downloadingContents[id ?? ""]?.downloadStatus = .notStarted
                    }
                    if let activity = downloadContent?.activity {
                        vm.end(activity: activity)
                    }
                    if let videoUrl = URL(string: DocumentsDirectory.localDocumentsURL.absoluteString + (download.videoName ?? "")) {
                        CloudDataManager.shared.deleteFileFromCloud(url: videoUrl)
                    }
                    DataController.shared.context.delete(download)
                    DataController.shared.saveData()
                }
                vm.downloadingContents = [:]
                vm.downloadQueue = []
                vm.startNextDownload()
                vm.isDownloading = false
            })
        }
        .addBackground()
        .noDataFound(!vm.isLoading && downloads.isEmpty)
    }
    func removeDuplicateElements(downloads: FetchedResults<DownloadContent>) -> [DownloadContent] {
        var uniquePosts = [DownloadContent]()
        for download in downloads {
            if !uniquePosts.contains(where: {$0.contentId == download.contentId && $0.type == .series}) {
                uniquePosts.append(download)
            }
        }
        return uniquePosts
    }
}

struct DownloadCardView : View {
    @EnvironmentObject var downloadViewModel : DownloadViewModel
    @FetchRequest(sortDescriptors: []) var downloads : FetchedResults<DownloadContent>
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @State var isDeleteDialogShow : Bool = false
    @State var selectedRecentlyViewed : RecentlyWatched?
    @State var isVideoStart = false
    @State var showPauseDialog = false
    @State var isShowResumeDialog = false
    @State var isShowWatchNowDialog = false
    @State var isShowDialog = false
    var isForSeriesView = false
    var content: DownloadContent
    @State var process : Double = 0
    var isOffLineView = false
    var downloadContent: DownloadingContent? {
        downloadViewModel.downloadingContents[content.downloadId ?? ""]
    }
    var downloadStatus: DownloadStatus? {
        downloadViewModel.downloadingContents[content.downloadId ?? ""]?.downloadStatus
    }
    
    func seriesDownloadCount() -> String {
       let filterDownloads = downloads.filter({$0.contentId == content.contentId})
        return String(filterDownloads.count)
    }
    
    var body: some View {
        VStack {
            if isOffLineView && content.type == .series && !isForSeriesView {
                NavigationLink(destination: content.type == .series ? SeriesDownloadView(content: content) : nil, label: {
                    downloadCard
                }).buttonStyle(.myPlain)
            } else if (isOffLineView && (content.type == .movie || isForSeriesView)) || !isOffLineView {
                downloadCard
                    .onTap {
                        onCardTap()
                    }
            }
        }
        .onAppear{
          process = fetchRecentlyWatched()
        }
        .fullScreenCover(isPresented: $isVideoStart, content: {
            VideoPlayerView(type: Int(content.contentSourceType) == 7 ? 5 : Int(content.contentSourceType), isShowAdView: false, isForDownloads: true, downloadContent: content, url: DocumentsDirectory.localDocumentsURL.absoluteString + (content.videoName ?? ""), progress: selectedRecentlyViewed?.progress ?? 0, sourceId: Int(content.contentSourceId))
        })
        .customAlert(isPresented: $isDeleteDialogShow){
            DialogCard(icon: Image.delete, title: .areYouSure, subTitle: .deleteDownloadDialogDes, buttonTitle: .delete, onClose: {
                withAnimation {
                    isDeleteDialogShow = false
                }
            },onButtonTap: {
                onDeleteTap()
            })
        }
        .customAlert(isPresented: $isShowResumeDialog){
            DialogOptionCard(heading: content.name ?? "" , firstButtonTitle: .resumeDownload, firstButtonIcon: .download_Pause, onFirstButtonTap: {
                isShowResumeDialog = false
                downloadViewModel.resumeDownload(for: content.downloadId ?? "")
            },onSecondButtonTap: {
                isShowResumeDialog = false
                isDeleteDialogShow = true
            },onClose: {
                isShowResumeDialog = false
            }
            )}
        .customAlert(isPresented: $showPauseDialog){
            DialogOptionCard(heading: content.name ?? "" , firstButtonTitle: .pauseDownload, firstButtonIcon: .downloadPause, onFirstButtonTap: {
                showPauseDialog = false
                downloadViewModel.pauseDownload(sourceId: content.downloadId ?? "")
                downloadViewModel.downloadingContents[content.downloadId ?? ""]?.downloadStatus = .paused
            },onSecondButtonTap: {
                showPauseDialog = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                    isDeleteDialogShow = true
                }
            },onClose: {
                showPauseDialog = false
            },progress: downloadContent?.progress ?? 0.0,isForPauseDialog: true
            )}
//        .customAlert(isPresented: $isShowDialog){
//                DialogOptionCard(heading: content.name ?? "" , firstButtonTitle: isShowResumeDialog ? .resumeDownload : showPauseDialog ? .pauseDownload : .watchNow ,firstButtonIcon: isShowResumeDialog ? .download_Pause : showPauseDialog ? .downloadPause : .play, onFirstButtonTap: {
//                    handleDownloadViewActions()
//                    resetAllDialog()
//                },onSecondButtonTap: {
//                    resetAllDialog()
//                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
//                        isDeleteDialogShow = true
//                    }
//                },onClose: {
//                    resetAllDialog()
//                } ,progress: downloadContent?.progress ?? 0.0,isForPauseDialog: showPauseDialog ? true : false
//                )}
        .customAlert(isPresented: $isShowWatchNowDialog){
            DialogOptionCard(heading: content.name ?? "" , firstButtonTitle: .watchNow, firstButtonIcon: .play, onFirstButtonTap: {
                isShowWatchNowDialog = false
                isVideoStart = true
            },onSecondButtonTap: {
                isShowWatchNowDialog = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                    isDeleteDialogShow = true
                }
            },onClose: {
                isShowWatchNowDialog = false
            }
        )}
    }
    func fetchRecentlyWatched() -> Double {
        let context = DataController.shared.context
        let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "downloadId == %@", content.downloadId ?? "")
        print("hhhhhhhhhhhhhhhh",content.downloadId)
        do {
            selectedRecentlyViewed = try context.fetch(fetchRequest).first
            let progress = (selectedRecentlyViewed?.progress ?? 0) / (selectedRecentlyViewed?.totalDuration ?? 0)
            return progress
        } catch {
            print("Failed to fetch RecentlyWatched: \(error)")
            return 0.0
        }
    }

    
    var downloadCard : some View {
        HStack(spacing: 9) {
            ZStack() {
                ZStack {
                    if content.type == .series && !isForSeriesView {
                        KFImage(isForSeriesView ? content.episodeHorizontalPoster.addBaseURL() : content.thumbnail.addBaseURL())
                            .resizeFillTo(width: 140, height: 90, compressSize: 2)
                            .cornerRadius(radius: 12)
                            .opacity(1)
                            .addStroke(radius: 12)
                            .offset(y: 12)
                    }
                    if content.type != .all {
                        ZStack {
                            KFImage(isForSeriesView ? content.episodeHorizontalPoster.addBaseURL() : content.thumbnail.addBaseURL())
                                .resizeFillTo(width: 150, height: 105, compressSize: 2)
                                .overlay(
                                    typeTag(content: content, isForSeriesView: isForSeriesView)
                                    ,alignment: .topLeading
                                )
                            VStack {
                                Spacer()
                                if selectedRecentlyViewed != nil && content.downloadId == selectedRecentlyViewed?.downloadId {
                                    ProgressView(value: process)
                                        .progressViewStyle(LinearProgressViewStyle())
                                        .tint(.base)
                                }
                            }
                        }
                        .cornerRadius(radius: 15)
                        .addStroke(radius: 15)
                        .frame(width: 150)
                                            }
                }
                if isForSeriesView {
                    Image.play
                        .resizeFitTo(size: 12)
                        .rotationEffect(.degrees(language == .Arabic ? 180 : 0))
                        .foregroundColor(.text)
                        .padding(.leading,2)
                        .padding(12)
                        .background(Color.black)
                        .clipShape(.circle)
                        .addStroke(radius: 50,color: .text.opacity(0.2),lineWidth: 1.2)
                }
            }
            VStack(alignment: .leading, spacing: 6,content: {
                HStack {
                    Text(content.name ?? "")
                        .outfitSemiBold(18)
                        .foregroundColor(.text)
                        .lineLimit(1)
                }
                if content.type == .movie {
                    VStack{
                        Text(content.contentDuration ?? "")
                            .outfitLight()
                            .foregroundColor(.textLight)
                        Text(content.sourceSize ?? "")
                            .outfitLight()
                            .foregroundColor(.textLight)
                            .padding(.bottom,8)
                    }
                } else if !isForSeriesView && content.type == .series{
                    Text("\(seriesDownloadCount()) Episode\(seriesDownloadCount() == "1" ? "" : "s")")
                        .outfitLight()
                        .foregroundColor(.textLight)
                }
                
                if isForSeriesView {
                    VStack(alignment: .leading) {
                        HStack {
                            Text("S\(content.seasonNo ?? "") E\(content.episodeNo ?? "")")
                            Circle()
                                .fill(.textLight)
                                .frame(width: 5,height: 5)
                            Text(content.episodeDuration ?? "")
                        }
                        .padding(.bottom,8)
                        Text(content.sourceSize ?? "")
                    }
                    .outfitLight(18)
                    .foregroundColor(.textLight)
                    .lineLimit(1)
                    .padding(.bottom,8)
                }
            })
            Spacer(minLength: 0)
            ZStack {
                let downloadStatus = downloadContent?.downloadStatus ?? .notStarted
                switch downloadStatus {
                case .notStarted:
                    VStack{
                    }
                case .downloading:
                    if let progress = downloadContent?.progress, content.type == .movie || isForSeriesView {
                        PieProgress(progress: progress)
                            .frame(width: 25,height: 25)
                            .onTap {
                                showPauseDialog = true
                            }
                    }
                case .paused:
                    if content.type == .movie || isForSeriesView {
                        Image(.downloadPause)
                            .resizeFitTo(size: 16)
                            .padding([.vertical,.leading])
                            .onTap{
                                isShowResumeDialog = true
                            }
                    }
                case .downloaded:
                    VStack{
                        
                    }
                case .queued:
                    if content.type == .movie || isForSeriesView {
                        Image.timer
                            .resizeFitTo(size: 16, renderingMode: .template)
                            .foregroundColor(.text)
                    }
                }
                if content.type == .movie || isForSeriesView {
                    if downloadStatus != .downloading && downloadStatus != .paused && downloadStatus != .queued {
                        Image.options
                            .font(.system(size: 16))
                            .onTap {
                                showWatchVideoDialog()
                            }
                    }
                } else if content.type == .series && !isForSeriesView {
                    Image.back
                        .resizeFitTo(size: 13)
                        .foregroundColor(.text)
                        .rotationEffect(Angle(degrees: 180))
                        .onTap {
                            nvToSeriesDownload()
                        }
                }
            }
        }
    }
    
    func onCardTap() {
        if downloadStatus != .downloading && downloadStatus != .paused && downloadStatus != .queued && (content.type == .movie || content.type == .series && isForSeriesView){
            isVideoStart = true
        } else if content.type == .series && !isForSeriesView {
            nvToSeriesDownload()
            print("Download is not complete")
        }
    }
    
    func nvToSeriesDownload() {
        Navigation.pushToSwiftUiView(SeriesDownloadView(content: content))
    }
    
//    func resetAllDialog() {
//        isShowResumeDialog = false
//        isShowWatchNowDialog = false
//        showPauseDialog = false
//        isShowDialog = false
//    }
    
    func onDeleteTap() {
        let id = content.downloadId ?? ""
        var downloadData = SessionManager.shared.getDownloadData()
        if downloadStatus != .queued && downloadStatus != .downloaded {
            downloadViewModel.isDownloading = false
            downloadViewModel.startNextDownload()
        }
        downloadData.removeAll(where: {$0.sourceId == id})
        downloadViewModel.downloadQueue.removeAll(where: {$0.id == id})
        SessionManager.shared.setDownloadData(datum: downloadData)
        if downloadStatus == .downloading {
            downloadViewModel.pauseDownload(sourceId: id, isForDelete: true)
        }
        if  downloadStatus == .downloading || downloadStatus == .paused || downloadStatus == .queued {
            downloadViewModel.downloadingContents[id]?.downloadStatus = .notStarted
        }
        if let activity = downloadContent?.activity {
            downloadViewModel.end(activity: activity)
        }
        if let videoUrl = URL(string: DocumentsDirectory.localDocumentsURL.absoluteString + (content.videoName ?? "")) {
            CloudDataManager.shared.deleteFileFromCloud(url: videoUrl)
        }
        DataController.shared.context.delete(content)
        DataController.shared.saveData()
        
        isDeleteDialogShow = false
        //        downloadViewModel.isDownloading = false
        //        downloadViewModel.startNextDownload()
    }
    
    private func handleDownloadViewActions() {
        if isShowResumeDialog {
            downloadViewModel.resumeDownload(for: content.downloadId ?? "")
        } else if showPauseDialog {
            downloadViewModel.pauseDownload(sourceId: content.downloadId ?? "")
            downloadViewModel.downloadingContents[content.downloadId ?? ""]?.downloadStatus = .paused
        } else if isShowWatchNowDialog {
            isVideoStart = true
        }
    }
    
    private func showWatchVideoDialog() {
        if downloadStatus != .downloading && downloadStatus != .paused && downloadStatus != .queued {
            print("]]]]]]]]]",DocumentsDirectory.localDocumentsURL.absoluteString + (content.videoName ?? ""))
            isShowWatchNowDialog = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                isShowDialog = true
            }
        } else {
            print("Download is not complete")
        }
    }
    
    private func handleDownloadAction() {
        let status = downloadStatus ?? .notStarted
        let sourceId = content.downloadId ?? ""
        switch status {
        case .notStarted:
            break
        case .downloading:
            downloadViewModel.pauseDownload(sourceId: sourceId)
            downloadViewModel.downloadingContents[sourceId]?.downloadStatus = .paused
        case .paused:
            downloadViewModel.resumeDownload(for: sourceId)
        case .downloaded:
            break
        case .queued:
            break
        }
    }
}

