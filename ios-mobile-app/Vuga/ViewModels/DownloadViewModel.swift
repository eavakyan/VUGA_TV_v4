//
//  DownloadViewModel.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 10/06/24.
//

import Foundation
import SwiftUI
import CoreData
import ActivityKit

enum DownloadStatus: Int, Codable {
    case notStarted = 0
    case downloading = 1
    case paused = 2
    case downloaded = 3
    case queued = 4
}

enum LiveActivityStatus: Int {
    case downloading = 0
    case paused = 1
    case pending = 2
    
    var title: String {
        switch self {
        case .downloading:
            "Downloading..."
        case .paused:
            "Paused"
        case .pending:
            "In Queue"
        }
    }
}

struct DownloadingContent {
    var id: String // episode_1 || movie_1
    var progress: Float = 0
    var downloadStatus: DownloadStatus = .notStarted
    var task: URLSessionDownloadTask?
    var resumeData: Data?
    var activity : Activity<VugaLiveActivityAttributes>?
    var content : DownloadContent?
    var source: Source?
    var episode: Episode?
    var flixyContent: VugaContent?
    var sourceUrl: URL?
    var totalBytesWritten: Int64 = 0
    var totalBytesExpectedToWrite: Int64 = 0
}


class DownloadViewModel: BaseViewModel, URLSessionDownloadDelegate {
    var session: URLSession!
    @Published var downloadingContents : [String : DownloadingContent] = [:]
    @Published var downloadContents : [DownloadContent] = []
    @Published var showPauseDialog = false
    @Published var isShowResumeDialog = false
    @Published var isShowWatchNowDialog = false
    @Published var isDeleteDialogShow : Bool = false

    var progressIntervals: [Double] = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
    var progressDictionary: [URLSessionDownloadTask: (currentProgressIndex: Int, progress: Double)] = [:]
    var timer = Timer()
    var activities = Activity<VugaLiveActivityAttributes>.activities
    var isDownloading = false
    var downloadQueue: [DownloadingContent] = []
    override init() {
        super.init()
        let configuration = URLSessionConfiguration.default
        self.session = URLSession(configuration: configuration, delegate: self, delegateQueue: nil)
        loadDownloadsForCurrentProfile()
        
        // Listen for profile changes to reload downloads
        NotificationCenter.default.addObserver(
            forName: .profileChanged,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            self?.loadDownloadsForCurrentProfile()
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self, name: .profileChanged, object: nil)
    }
    
    func loadDownloadsForCurrentProfile() {
        guard let currentProfileId = SessionManager.shared.currentProfile?.profileId else {
            downloadContents = []
            return
        }
        
        let fetchRequest: NSFetchRequest<DownloadContent>
        fetchRequest = DownloadContent.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "profileId == %d", currentProfileId)
        let context = DataController.shared.context
        do{
            downloadContents = try context.fetch(fetchRequest)
            print("Loaded \(downloadContents.count) downloads for profile \(currentProfileId)")
        } catch {
            print(error.localizedDescription)
        }
    }
    
    func increaseContentDownloads(contentId: Int) {
        let params: [Params: Any] = [.contentId: contentId]
        startLoading()
        NetworkManager.callWebService(url: .increaseContentDownload, params: params) { (obj: IncreaseContentViewModel) in
            self.stopLoading()
            print(obj.status!)
        }
    }
    
    func increaseEpisodeDownloads(episodeId: Int) {
        let params: [Params: Any] = [.episodeId: episodeId]
        startLoading()
        NetworkManager.callWebService(url: .increaseEpisodeDownload, params: params) { (obj: IncreaseEpisodeViewsModel) in
            self.stopLoading()
            print(obj.status!)
        }
    }
    
    func startDownload(content: VugaContent, episode: Episode?, source: Source,seasonNumber: Int) {
        
        // Check storage before downloading
        let sizeInMB = Int(source.size ?? "500") ?? 500
        let estimatedSize: Int64 = Int64(sizeInMB) * 1024 * 1024 // Convert MB to bytes
        
        if !StorageManager.shared.hasEnoughStorage(for: estimatedSize) {
            // Show storage error - in iOS we typically don't use toast, we'll let the UI handle this
            print("Insufficient storage space. Please free up some space to download this content.")
            return
        }
        
        DispatchQueue.main.async {
            let id = source.sourceDownloadId(contentType: content.type ?? .movie)
            let downloadingContent = DownloadingContent(id: id, progress: 0.0, downloadStatus: .queued, source: source, episode: episode, flixyContent: content, sourceUrl: source.sourceURL)
            self.downloadQueue.append(downloadingContent)
            self.downloadingContents[id] = downloadingContent
            self.addContentToDownload(content: content, episode: episode, source: source, seasonNumber: seasonNumber)
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                if let activity = self.downloadingContents[id]?.activity {
                    self.updateStatus(activity: activity, status: LiveActivityStatus.pending.title, downloadContentId: id)
                }
            }
            if !self.isDownloading {
                self.startNextDownload()
            }
        }
    }
    
    func startNextDownload() {
        guard !downloadQueue.isEmpty else { return }
        
        isDownloading = true
        var nextContent = downloadQueue.removeFirst()
        nextContent.downloadStatus = .downloading
        DispatchQueue.main.async {
            if let url = nextContent.sourceUrl {
                let task = self.session.downloadTask(with: url)
                nextContent.task = task
                nextContent.activity = self.downloadingContents[nextContent.id]?.activity
                nextContent.content = self.downloadingContents[nextContent.id]?.content
                if let activity = nextContent.activity {
                    self.updateStatus(activity: activity, status: LiveActivityStatus.downloading.title, downloadContentId: nextContent.id)
                }
                self.downloadingContents[nextContent.id] = nextContent
                self.progressDictionary[task] = (currentProgressIndex: 0, progress: 0.0)
                task.resume()
                if nextContent.resumeData != nil {
                    self.resumeDownload(for: nextContent.id)
                }
            }
        }
    }
    
    func setDownloadContentFromCoredata(content: DownloadContent){
        let downloadId = content.downloadId ?? ""
        let downloadContent = downloadingContents[downloadId]
        
        if downloadContent?.downloadStatus != .downloading && downloadContent?.downloadStatus != .paused && downloadContent?.downloadStatus != .queued {
            if let download = SessionManager.shared.getDownloadDataForCurrentProfile().first(where: { $0.sourceId == downloadId }) {
                let activity = activities.first(where: {$0.attributes.downloadId == download.sourceId})
                //                let task = session.downloadTask(with: download.sourceURL)
                if activity == nil {
                    createActivity(contentDownload: content)
                    
                }
                let downloadingContent = DownloadingContent(id: downloadId, progress: 0.0, downloadStatus: content.status, resumeData: download.data, activity: activity, content: content, sourceUrl: content.sourceUrl)
                downloadingContents[downloadId] = downloadingContent
                downloadQueue.append(downloadingContent)
                if let activity = downloadingContent.activity {
                    self.updateStatus(activity: activity, status: LiveActivityStatus.pending.title, downloadContentId: downloadingContent.id)
                }
                //                resumeDownload(for: downloadId)
            }
        }
    }
    
    func pauseDownload(sourceId: String, isForDelete: Bool = false) {
        var content = self.downloadingContents[sourceId]
        var downloadData = SessionManager.shared.getDownloadData()
        if let task = downloadingContents[sourceId]?.task {
            self.progressDictionary.removeValue(forKey: task)
        }
        downloadingContents[sourceId]?.task?.cancel(byProducingResumeData: { data in
            DispatchQueue.main.async {
                content?.resumeData = data
                content?.downloadStatus = .paused
            }
            guard let resumeData = data else { return }
            DispatchQueue.main.async {
                self.downloadingContents[sourceId]?.resumeData = resumeData
            }
            if let activity = content?.activity {
                self.updateStatus(activity: activity, status: LiveActivityStatus.paused.title, downloadContentId: sourceId)
            }
            //            if isForDelete {
            //                DispatchQueue.main.async {
            //                    content?.resumeData = nil
            //                    content?.downloadStatus = .notStarted
            //                }
            //            }
            DispatchQueue.main.async {
                downloadData.removeAll(where: {$0.sourceId == sourceId})
                if let sourceUrl = ((self.downloadingContents[sourceId]?.source?.sourceURL) ?? self.downloadingContents[sourceId]?.content?.sourceUrl) {
                    downloadData.append(DownloadData(data: resumeData, sourceId: sourceId, sourceURL: sourceUrl, contentId: Int(content?.flixyContent?.id ?? 0), episodeId: content?.episode?.id ?? 0, destinationName: sourceId + sourceUrl.lastPathComponent, profileId: SessionManager.shared.currentProfile?.profileId ?? 0))
                }
                SessionManager.shared.setDownloadData(datum: downloadData)
            }
        })
    }
    
    func resumeDownload(for sourceId: String) {
        //        var content = self.downloadingContents[sourceId]
        print(sourceId)
        guard let resumeData = downloadingContents[sourceId]?.resumeData else { return }
        let task = session.downloadTask(withResumeData: resumeData)
        DispatchQueue.main.async {
            self.downloadingContents[sourceId]?.task = task
            self.downloadingContents[sourceId]?.downloadStatus = .downloading
            self.progressDictionary[task] = (currentProgressIndex: Int((self.downloadingContents[sourceId]?.progress ?? 0) * 10), progress: Double(self.downloadingContents[sourceId]?.progress ?? 0))
            task.resume()
            self.downloadingContents[sourceId]?.resumeData = nil
            if let activity = self.downloadingContents[sourceId]?.activity {
                self.updateStatus(activity: activity, status: LiveActivityStatus.downloading.title,downloadContentId: sourceId)
            }
        }
    }
    
    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: (any Error)?) {
        print("]]]]]]]]]",error?.localizedDescription ?? "")
////        guard let id = downloadingContents.first(where: { $0.value.task == task })?.key else { return }
//        
////        if ((error?.localizedDescription.contains("No such file or directory")) != nil) {
//            let fetchRequest: NSFetchRequest<DownloadContent>
//            fetchRequest = DownloadContent.fetchRequest()
//            let context = DataController.shared.context
//            DispatchQueue.main.async {
//                do{
//                    self.downloadContents = try context.fetch(fetchRequest)
//                    for content in self.downloadContents {
//                        self.setDownloadContentFromCoredata(content: content)
//                    }
////                    self.startNextDownload()
//                    
//                } catch {
//                    print(error.localizedDescription)
//                }
//            }
////        }
    }
    
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didWriteData bytesWritten: Int64, totalBytesWritten: Int64, totalBytesExpectedToWrite: Int64) {
        guard let id = downloadingContents.first(where: { $0.value.task == downloadTask })?.key else { return }
        
        let content = self.downloadingContents[id]
        var downloadData = SessionManager.shared.getDownloadData()
        
        if self.downloadingContents[id] != nil {
            // Update byte counts
            DispatchQueue.main.async {
                self.downloadingContents[id]?.totalBytesWritten = totalBytesWritten
                self.downloadingContents[id]?.totalBytesExpectedToWrite = totalBytesExpectedToWrite
                self.downloadingContents[id]?.progress = Float(totalBytesWritten) / Float(totalBytesExpectedToWrite)
            }
            
            guard var progressInfo = progressDictionary[downloadTask] else { return }
            let progress = Double(totalBytesWritten) / Double(totalBytesExpectedToWrite)
            progressInfo.progress = progress
            if progressInfo.currentProgressIndex < progressIntervals.count && progress >= progressIntervals[progressInfo.currentProgressIndex] {
                print("Progress for task \(downloadTask): \(progressInfo.currentProgressIndex) \(self.progressIntervals[progressInfo.currentProgressIndex] * 100)%")
                progressDictionary[downloadTask]?.currentProgressIndex += 1
                DispatchQueue.main.async {
                    if let activity = self.downloadingContents[id]?.activity {
                        self.update(activity: activity, progress: progress)
                    }
                }
                if let task = downloadingContents[id]?.task {
                    self.progressDictionary.removeValue(forKey: task)
                }
                downloadTask.cancel(byProducingResumeData: { resumeDataOrNil in
                    guard let resumeDataOrNil = resumeDataOrNil else { return }
                    DispatchQueue.main.async {
                        self.downloadingContents[id]?.resumeData = resumeDataOrNil
                        print("Call Data")
                        DispatchQueue.main.async {
                            downloadData.removeAll(where: {$0.sourceId == id})
                            print(downloadData)
                            
                            if let sourceUrl = ((content?.source?.sourceURL) ?? content?.content?.sourceUrl) {
                                downloadData.append(DownloadData(data: resumeDataOrNil, sourceId: id, sourceURL: sourceUrl, contentId: Int(content?.flixyContent?.id ?? 0), episodeId: content?.episode?.id ?? 0, destinationName: id + sourceUrl.lastPathComponent, profileId: SessionManager.shared.currentProfile?.profileId ?? 0))
                            }
                            SessionManager.shared.setDownloadData(datum: downloadData)
                            self.resumeDownload(for: id)
                        }
                    }
                })
            }
            progressDictionary[downloadTask] = progressInfo
        }
    }
    
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
        guard let id = downloadingContents.first(where: { $0.value.task == downloadTask })?.key else { return }
        
        let fileManager = FileManager.default
        let destinationURL =  DocumentsDirectory.localDocumentsURL.appendingPathComponent(id + (downloadTask.response?.suggestedFilename ?? ""))
        do {
            try fileManager.moveItem(at: location, to: destinationURL)
            var downloadingContent = self.downloadingContents[id]
            print("File saved to: \(destinationURL)")
            pushLocalNotification(name: downloadingContent?.content?.name ?? "", episodeNo: "\(downloadingContent?.episode?.number ?? 0)", seasonNo: downloadingContent?.content?.seasonNo ?? "",contentType: downloadingContent?.content?.type ?? .movie)
            let destination = FileManager.default.containerURL(
                forSecurityApplicationGroupIdentifier: liveActivityGroupID)!
            if let imageURL = URL(string: destination.absoluteString + id) {
                CloudDataManager.shared.deleteFileFromCloud(url: imageURL)
            }
            if downloadingContent?.content?.type == .movie {
                increaseContentDownloads(contentId: (downloadingContent?.flixyContent != nil ? downloadingContent?.flixyContent?.id ?? 0 : Int(downloadingContent?.content?.contentId ?? "0")) ?? 0)
            } else {
                increaseEpisodeDownloads(episodeId: downloadingContent?.flixyContent != nil ? downloadingContent?.episode?.id ?? 0 : Int(downloadingContent?.content?.episodeId ?? 0))
            }
            
            DispatchQueue.main.async {
                downloadingContent?.downloadStatus = .downloaded
                self.downloadingContents[id]?.downloadStatus = .downloaded
            }
            if let activity = downloadingContent?.activity {
                end(activity: activity)
            }
            var downloadData = SessionManager.shared.getDownloadData()
            DispatchQueue.main.async {
                downloadData.removeAll(where: { $0.sourceId == id })
                SessionManager.shared.setDownloadData(datum: downloadData)
            }
            isDownloading = false
            startNextDownload()
        } catch {
            print("Error saving file: \(error)")
        }
    }
    
    func addContentToDownload(content: VugaContent, episode: Episode?, source: Source,seasonNumber: Int) {
        guard let currentProfileId = SessionManager.shared.currentProfile?.profileId else {
            print("No current profile found, cannot download content")
            return
        }
        
        let newDownloadContent = DownloadContent(context: DataController.shared.context)
        let downloadId = source.sourceDownloadId(contentType: content.type ?? .movie)
        let videoName = "\(content.type?.title ?? "")_\(source.id ?? 0)" + source.sourceURL.lastPathComponent
        newDownloadContent.downloadId = downloadId
        newDownloadContent.releaseYear = "\(content.releaseYear ?? 0)"
        newDownloadContent.name = content.title
        newDownloadContent.thumbnail = content.horizontalPoster
        newDownloadContent.rating = content.ratingString
        newDownloadContent.genres = content.genreString
        newDownloadContent.videoName = videoName
        newDownloadContent.contentType = Int16(content.type?.rawValue ?? 1)
        newDownloadContent.contentSourceType = Int16(source.type?.rawValue ?? 2)
        newDownloadContent.contentSourceId = Int16(source.id ?? 0)
        newDownloadContent.sourceQuality = source.quality
        newDownloadContent.episodeNo = String(episode?.number ?? 0)
        newDownloadContent.sourceSize = source.size
        newDownloadContent.contentId = String(content.id ?? 0)
        newDownloadContent.episodeId = Int16(episode?.id ?? 0)
        newDownloadContent.sourceUrl = source.sourceURL
        newDownloadContent.downloadStatus = Int16(DownloadStatus.queued.rawValue)
        newDownloadContent.contentDuration = content.type == .movie ? content.duration : episode?.duration
        newDownloadContent.episodeDuration = episode?.duration
        newDownloadContent.seasonNo = String(seasonNumber)
        newDownloadContent.episodeHorizontalPoster = episode?.thumbnail
        newDownloadContent.episodeTitle = episode?.title
        newDownloadContent.profileId = Int32(currentProfileId)
        downloadContents.append(newDownloadContent)
        self.downloadingContents[downloadId]?.content = newDownloadContent
        self.createActivity(contentDownload: newDownloadContent)

        DataController.shared.saveData()
        var downloadData = SessionManager.shared.getDownloadData()
        downloadData.append(DownloadData(data: nil, sourceId: downloadId, sourceURL: source.sourceURL, contentId: content.id ?? 0, episodeId: episode?.id ?? 0, destinationName: videoName, profileId: currentProfileId))
        SessionManager.shared.setDownloadData(datum: downloadData)
    }
    
    private func downloadImage(from url: URL, name: String)  {
        var destination = FileManager.default.containerURL(
            forSecurityApplicationGroupIdentifier: liveActivityGroupID)!
        
        destination = destination.appendingPathComponent(name)
        //                let url = URL(string: "https://picsum.photos/300/100")!
        let imageView = UIImageView()
        imageView.kf.setImage(with: url) { result in
            switch result {
            case .success(let value):
                var image =  value.image
                if let imgs = image.cgImage?.utType as String? {
                    if ((imgs.contains("webp")) || (imgs.contains("avif"))) {
                        if let imgdata = image.pngData() {
                            if let imaged = UIImage(data:imgdata) {
                                image = imaged
                            }
                        }
                    }
                }
                image = image.resized(toWidth: 800)!
                try? image.jpegData(compressionQuality: 1)?.write(to: destination)
                print("Downloaded: \(destination)")
            case .failure(let error):
                print("Job failed: \(error.localizedDescription)")
            }
        }
    }
    
    func createActivity( contentDownload: DownloadContent?) {
        let id = contentDownload?.downloadId ?? ""
        if let imageURL = contentDownload?.thumbnail?.addBaseURL() {
            downloadImage(from: imageURL, name: id)
        }
        
        let contentState = VugaLiveActivityAttributes.LiveDeliveryData(status: LiveActivityStatus.pending.title, progress: 0.0)
        do {
            let newActivity = try Activity<VugaLiveActivityAttributes>.request(
                attributes: VugaLiveActivityAttributes(
                    imageUrl: id,
                    downloadId: id,
                    contentName: contentDownload?.name ?? "",
                    contentId: Int(contentDownload?.contentId ?? "") ?? 0,
                    contentResolution: contentDownload?.sourceQuality ?? "",
                    contentSize: contentDownload?.sourceSize ?? "",
                    contentType: contentDownload?.type.title ?? "",
                    seasonEpisodeName: "Season \(contentDownload?.seasonNo ?? "") Episode \(contentDownload?.episodeNo ?? "")"
                ),
                contentState: contentState,
                pushType: nil
            )
            DispatchQueue.main.async {
                self.downloadingContents[id]?.activity = newActivity
            }
        } catch {
            print("Error creating activity: \(error.localizedDescription)")
        }
    }
    
    func update(activity: Activity<VugaLiveActivityAttributes>, progress: Double) {
        Task {
            let contentState = VugaLiveActivityAttributes.LiveDeliveryData(status: "Downloading...", progress: progress)
            await activity.update(using: contentState)
        }
    }
    
    func updateStatus(activity: Activity<VugaLiveActivityAttributes>, status: String, downloadContentId: String) {
        Task {
            let contentState = VugaLiveActivityAttributes.LiveDeliveryData(status: status, progress: Double(downloadingContents[downloadContentId]?.progress ?? 0))
            await activity.update(using: contentState)
        }
    }
    
    func end(activity: Activity<VugaLiveActivityAttributes>) {
        Task {
            let contentState = VugaLiveActivityAttributes.LiveDeliveryData(status: "Download completed", progress: 1.0)
            await activity.end(using: contentState, dismissalPolicy: .immediate)
        }
    }
    
    func checkSourceIsDownloaded(sourceId: String) {
        if downloadingContents[sourceId]?.downloadStatus != .downloading && downloadingContents[sourceId]?.downloadStatus != .paused && downloadingContents[sourceId]?.downloadStatus != .queued {
            let contentSourceIds = downloadContents.map { $0.downloadId }
            print(contentSourceIds)
            if contentSourceIds.contains(sourceId) {
                downloadingContents[sourceId] = DownloadingContent(id: sourceId, downloadStatus: .downloaded)
                print(sourceId,"True,True,True,True,True,True")
            } else {
                downloadingContents[sourceId]?.downloadStatus = .notStarted
                print(sourceId)
            }
        }
    }
    
    func pushLocalNotification(name: String, episodeNo: String?, seasonNo: String?,contentType : ContentType) {
        let notificationContent = UNMutableNotificationContent()
        notificationContent.title = "Download Completed"
        notificationContent.body = contentType == .series ? "Great news! S\(seasonNo ?? "") E\(episodeNo ?? "") of \(name) has been successfully downloaded. Enjoy!" : "\(name) is now available to watch offline. Enjoy the show!"
        notificationContent.sound = UNNotificationSound.default
        // App icon will be shown automatically by iOS from the app bundle
        
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: notificationContent, trigger: nil)
        UNUserNotificationCenter.current().add(request)
    }
}


extension UIImage {
    func resized(withPercentage percentage: CGFloat, isOpaque: Bool = true) -> UIImage? {
        let newWidth = size.width * percentage
        let newHeight = size.height * percentage
        let canvas = CGSize(width: newWidth, height: newHeight)
        let format = imageRendererFormat
        format.opaque = isOpaque
        return UIGraphicsImageRenderer(size: canvas, format: format).image {
            _ in draw(in: CGRect(origin: .zero, size: canvas))
        }
    }
    func resized(toWidth width: CGFloat, isOpaque: Bool = true) -> UIImage? {
        let scale = width / size.width
        let newHeight = size.height * scale
        let canvas = CGSize(width: width, height: ceil(newHeight))
        let format = imageRendererFormat
        format.opaque = isOpaque
        return UIGraphicsImageRenderer(size: canvas, format: format).image {
            _ in draw(in: CGRect(origin: .zero, size: canvas))
        }
    }
}

class NotificationDelegate: NSObject, ObservableObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.badge, .banner, .sound])
    }
}
