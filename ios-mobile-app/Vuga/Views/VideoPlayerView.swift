//
//  VideoPlayerView.swift
//  Vuga
//
//

import Foundation
import SwiftUI
import AVKit
import Sliders
import CoreData
import GoogleCast

struct VideoPlayerView: View {
    @Environment(\.scenePhase) var scenePhase
    @Environment(\.presentationMode) var present
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var orientationManager = OrientationManager()
    @StateObject var vm = PlayerModel()
    @StateObject var contentModel = ContentDetailViewModel()
    @State var content : VugaContent?
    @State var episode: Episode?
    @State var isplaying = false
    @State var value : Float = 0
    @State var type: Int
    @State var showLoader = true
    @State var isShowAdView = true
    @State var isForDownloads = false
    @State var isLandscape = false
    @State var isPortrait = false
    @State var downloadContent : DownloadContent?
    @State private var showAudioTrackSelection = false
    @State private var showSubtitleTrackSelection = false
    @State private var selectedAudioTrack: AudioTrack?
    @State private var selectedSubtitleTrack: SubtitleTrack?
    var isLiveVideo = false
    var url: String
    @State var progress: Double = 0
    var sourceId: Int?
    var contentTitle: String {
        isForDownloads ? downloadContent?.type == .series ? downloadContent?.episodeTitle ?? "" : downloadContent?.name ?? "" : content?.type == .series ? episode?.title ?? "" : content?.title ?? ""
    }
    
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            if !isShowAdView {
                VStack{
                    ZStack{
                            if let vlc = vm.vlcPlayer {
                                VlcPlayer(player: vlc)
                            }
                            if let player = vm.player {
                                VideoPlayer(player: player,isLive: isLiveVideo)
                            }
                        VStack {
                            Spacer()
                            ForEach(vm.mySubtitles, id: \.startTime){ subtitle in
                                if subtitle.startTime <= vm.currentTime && subtitle.endTime >= vm.currentTime {
                                    Text(AttributedString(subtitle.text))
                                        .foregroundColor(.white)
                                        .multilineTextAlignment(.center)
                                        .padding(10)
                                        .background(Color.black.opacity(0.4))
                                        .customCornerRadius(radius: 12)
                                }
                            }
                            .padding(10)
                            .padding(.bottom,vm.showcontrols ? 50 : 10)
                        }
                        TouchView(model: vm)
                        VStack{
                            Spacer()
                            if self.vm.showcontrols && !isShowAdView {
                                videoProgressBar
                                    .hidden()
                            }
                            ZStack {
                                HStack(spacing: 20) {
                                    if orientationManager.orientation != .portrait {
                                        Spacer(minLength: 0)
                                    }
                                    if self.vm.showcontrols && !isLiveVideo {
                                        videoPauseAndSkipControlles
                                    }
                                    if orientationManager.orientation != .portrait {
                                        Spacer(minLength: 0)
                                    }
                                }
                                HStack {
                                    if vm.showVolume && !vm.showBrightness{
                                        VolumeSlider(imageName: AVAudioSession.sharedInstance().outputVolume < 0.01 ? "volume.slash.fill" : "speaker.wave.3.fill",currentValue: vm.currentVolume)
                                            .hidden(!vm.showVolume)
                                    }
                                    Spacer()
                                    if vm.showBrightness && !vm.showVolume {
                                        VolumeSlider(imageName: "sun.max.fill", currentValue: vm.currentBritness)
                                    }
                                }
                                .padding(.horizontal,orientationManager.orientation == .portrait ? 0 : 10)
                            }
                            .padding(.horizontal)
                            Spacer()
                            if self.vm.showcontrols{
                                videoProgressBar
                            }
                        }
                        .padding(.horizontal,20)
                        .padding(.bottom,2)
                        .background(vm.showcontrols ? Color.black.opacity(0.4) : nil)
                        .onTapGesture {
                            self.vm.showcontrols = false
                            vm.controlsTimer?.invalidate()
                            vm.controlsTimer = nil
                            vm.showVolume = false
                            vm.showBrightness = false
                        }
                    }
                    .gesture(TapGesture().onEnded({ _ in
                        DispatchQueue.main.async {
                            if !self.vm.showcontrols {
                                vm.showControlsDuration = 5
                                self.vm.showcontrols = true
                                vm.restartTimer()
                            }
                        }
                    }))
                }
                .hideHomeIndicator()
                .background(Color.black.edgesIgnoringSafeArea(.all))
                .onReceive(NotificationCenter.default.publisher(for: AVPlayerItem.didPlayToEndTimeNotification, object: vm.player?.currentItem), perform: { _ in
                    AppDelegate.setOrientation(.portrait)
                    print("play end")
                })
                .onAppear {
                    
                    print("ROTATION : \(UIDevice.current.orientation)")
                    UIDevice.current.setValue(UIInterfaceOrientation.portrait.rawValue, forKey: "orientation")
                    AppDelegate.orientationLock = .landscapeRight
                    
                    // Check if Cast is connected - if so, show a message and close the player
                    let castContext = GCKCastContext.sharedInstance()
                    if castContext.castState == .connected {
                        print("📺 Cast is active - media should be playing on TV. Closing local player.")
                        
                        // Show a brief message that casting is active
                        vm.state = .playing
                        
                        // Close the video player since Cast is handling playback
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                            present.wrappedValue.dismiss()
                        }
                    } else {
                        // No Cast connection, setup local player normally
                        vm.setupPlayer(videoUrl: url, type: type)
                    }
                    
                    if showLoader {
                        vm.isLoading = true
                    }
                    
                    vm.seekVideo(second: progress)

                    if type == 2 || type == 3 || type == 4 {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 8) {
                            if !(vm.player?.currentItem?.asset.isPlayable ?? false) {
                                vm.isVideoFaildToPlay = true
                            }
                        }
                    }
                    try? AVAudioSession.sharedInstance().setCategory(AVAudioSession.Category.playback, mode: AVAudioSession.Mode.default, options: [])
                    if !isForDownloads {
                        if  content?.type == .movie {
                            contentModel.increaseContentView(contentId: content?.id ?? 0)
                        } else {
                            contentModel.increaseEpisodeView(episodeId: episode?.id ?? 0)
                        }
                    }
                }
                .onDisappear(perform: {
                    
                    vm.pause()
                    vm.state = .stoped
                })
                .onChange(of: vm.player?.timeControlStatus, perform: { newValue in
                    if vm.player?.timeControlStatus == .playing {
                        vm.stopLoading()
                    }
                })
                .onChange(of: vm.state, perform: { _ in
                    if vm.state == .complete {
                        onBack()
                    }
                    if isForDownloads && vm.state == .buffering{
                        vm.stopLoading()
                    }
                })
                .onChange(of: scenePhase,perform: { newPhase in
                    if newPhase == .background {
                        vm.pause()
                    }
                })
                .onChange(of: orientationManager.orientation,perform: { orientation in
                    print("Changed Called =======================================")
                    print(orientation.isPortrait)
                    AppDelegate.setOrientation(.unknown)
                    if orientation.isLandscape {
                        isLandscape = true
                        isPortrait = false
                    } else {
                        isPortrait = true
                        isLandscape = false
                    }
                })
                .loaderView(vm.state == .buffering || vm.isLoading, isshowNoDataView: vm.isVideoFaildToPlay)
                .hideNavigationbar()
            }
            if self.vm.showcontrols || vm.isLoading || vm.state == .buffering && !isShowAdView {
                VStack {
                    HStack(alignment: .center) {
                        
                        Image.back
                            .font(.system(size: 20,weight: .semibold))
                            .padding(.horizontal)
                            .onTap {
                                onBack()
                            }
                        Spacer()
                    }
                    .padding(.top,16)
                    .padding(.horizontal,20)
                    Spacer()
                }
            }
            if self.vm.showcontrols && !vm.isLoading && vm.state != .buffering && ((content?.type == .movie && content?.contentSubtitles?.count ?? 0 > 0 && content != nil) || (content?.type == .series && episode?.episodeSubtitle?.count ?? 0 > 0  && episode != nil)){
                Menu{
                    Button("Disable", systemImage: vm.selectedSubtitle == nil ? "checkmark" : "") {
                        vm.mySubtitles = []
                        vm.selectedSubtitle = nil
                    }
                    ForEach((content?.type == .movie ? (content?.contentSubtitles)! : (episode?.episodeSubtitle)!), id: \.id) { subtitle in
                        Button(subtitle.language.title ?? "",systemImage: vm.selectedSubtitle?.id == subtitle.id ? "checkmark" : ""){
                            //                            vm.loadSubtitles(from: subtitle.file?.addCommonURL()?.absoluteString ?? "")
                            vm.loadSubtitles(from: subtitle.file?.addBaseURL()?.absoluteString ?? "")
                            vm.selectedSubtitle = subtitle
                        }
                    }
                } label:{
                    Image.subtitles
                        .resizeFitTo(size: 30,renderingMode: .template)
                        .foregroundColor(.text)
                }
                .padding(.horizontal,65)
                
            }
            if isShowAdView && SessionManager.shared.getCustomAds().isNotEmpty {
                CustomAdView(isShowAdView: $isShowAdView)
            }
        }
        .onAppear{
            if SessionManager.shared.getCustomAds().isEmpty {
                isShowAdView = false
            }
            print("dddddddddddddddddd",sourceId)
            isLandscape = true
        }
        .sheet(isPresented: $showAudioTrackSelection) {
            AudioTrackSelectionView(
                tracks: content?.audioTracks ?? episode?.audioTracks ?? [],
                selectedTrack: $selectedAudioTrack,
                onTrackSelected: { track in
                    selectedAudioTrack = track
                    if let audioUrl = track.audioUrl, let url = URL(string: audioUrl) {
                        // Switch audio track in player
                        vm.switchAudioTrack(url: url)
                    }
                    showAudioTrackSelection = false
                }
            )
        }
        .sheet(isPresented: $showSubtitleTrackSelection) {
            SubtitleTrackSelectionView(
                tracks: content?.subtitleTracks ?? episode?.subtitleTracks ?? [],
                selectedTrack: $selectedSubtitleTrack,
                onTrackSelected: { track in
                    selectedSubtitleTrack = track
                    if let subtitleUrl = track?.subtitleUrl {
                        // Load subtitle track
                        vm.loadSubtitleTrack(url: subtitleUrl)
                    }
                    showSubtitleTrackSelection = false
                }
            )
        }
    }
    
    func onBack() {
        if !isLiveVideo {
            storeInRecenyViewed()
        }
        isShowAdView = false
        AppDelegate.setOrientation(.portrait)
        present.wrappedValue.dismiss()
        vm.pause()
        vm.state = .stoped
    }
    
//    func storeInRecenyViewed() {
//        guard let sourceId = sourceId else { return }
//        
//        // Determine the correct content type
//        let contentType = isForDownloads
//            ? Int16(downloadContent?.contentType ?? 1)
//            : content != nil
//                ? Int16(content?.type?.rawValue ?? 1)
//                : episode != nil
//                    ? 2
//                    : Int16(1)
//        
//        let context = DataController.shared.context
//        let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
//        
//        // Correct predicate with parameters in the right order
//        fetchRequest.predicate = NSPredicate(format: "contentSourceId == %d AND contentType == %d", sourceId, contentType)
//        
//        do {
//            let results = try context.fetch(fetchRequest)
//            if let existingEntry = results.first {
//                // Update the existing entry
//                existingEntry.progress = vm.currentTime
//                existingEntry.date = Date()
//                existingEntry.sourceUrl = url
//                existingEntry.isForDownload = isForDownloads
//                existingEntry.downloadId = downloadContent?.downloadId ?? ""
//                
//                print("Updated progress for entry with sourceId: \(sourceId) and contentType: \(contentType)")
//            } else {
//                // Create a new entry
//                let newRecentlyContent = RecentlyWatched(context: context)
//                newRecentlyContent.progress = vm.currentTime
//                newRecentlyContent.contentID = (isForDownloads
//                    ? Int16(downloadContent?.contentId ?? "")
//                    : Int16(content?.id ?? 0)) ?? 0
//                newRecentlyContent.totalDuration = vm.duration
//                newRecentlyContent.name = isForDownloads ? downloadContent?.name : content?.title
//                newRecentlyContent.date = Date()
//                newRecentlyContent.contentType = contentType
//                newRecentlyContent.downloadId = downloadContent?.downloadId ?? ""
//                newRecentlyContent.episodeHorizontalPoster = isForDownloads
//                    ? downloadContent?.episodeHorizontalPoster
//                    : episode?.thumbnail
//                newRecentlyContent.thumbnail = isForDownloads
//                    ? downloadContent?.thumbnail
//                    : content?.horizontalPoster
//                newRecentlyContent.episodeId = Int16(episode?.id ?? 0)
//                newRecentlyContent.sourceUrl = url
//                newRecentlyContent.isForDownload = isForDownloads
//                newRecentlyContent.contentSourceType = Int16(type)
//                newRecentlyContent.contentSourceId = Int16(sourceId)
//                
//                print("Created new entry with sourceId: \(sourceId) and contentType: \(contentType)")
//            }
//            DataController.shared.saveData()
//        } catch {
//            print("Failed to fetch RecentlyWatched: \(error.localizedDescription)")
//        }
//    }

    func storeInRecenyViewed() {
        guard let sourceId = sourceId else { return }

        // Determine the content type for the current object
        let currentContentType = isForDownloads
            ? Int16(downloadContent?.contentType ?? 1)
            : content != nil
                ? Int16(content?.type?.rawValue ?? 1)
                : episode != nil
                    ? 2
                    : Int16(1)

        let context = DataController.shared.context
        let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()

        do {
            // Step 1: Fetch all entries of contentType 1
            fetchRequest.predicate = NSPredicate(format: "contentType == 1")
            let type1Results = try context.fetch(fetchRequest)

            // Step 2: Fetch all entries of contentType 2
            fetchRequest.predicate = NSPredicate(format: "contentType == 2")
            let type2Results = try context.fetch(fetchRequest)

            // Step 3: Determine which group to process based on the current content type
            let relevantResults = currentContentType == 1 ? type1Results : type2Results

            // Step 4: Check for an existing entry with the same sourceId within the relevant group
            if let existingEntry = relevantResults.first(where: { $0.contentSourceId == Int16(sourceId) }) {
                // Update the existing entry
                existingEntry.progress = vm.currentTime
                existingEntry.date = Date()
                existingEntry.sourceUrl = url
                existingEntry.isForDownload = isForDownloads
                existingEntry.downloadId = downloadContent?.downloadId ?? ""

                print("Updated progress for entry with contentID: \(existingEntry.contentID), sourceId: \(sourceId), contentType: \(currentContentType), episodeId: \(existingEntry.episodeId)")
            } else {
                // Create a new entry
                let newRecentlyContent = RecentlyWatched(context: context)
                newRecentlyContent.progress = vm.currentTime
                // For episodes, use the parent content ID
                let contentId: Int16
                if isForDownloads {
                    contentId = Int16(downloadContent?.contentId ?? "") ?? 0
                } else if let content = content {
                    contentId = Int16(content.id ?? 0)
                } else if episode != nil {
                    // For episodes without parent content, log error
                    print("ERROR: Episode playing without parent content reference")
                    contentId = 0
                } else {
                    contentId = 0
                }
                newRecentlyContent.contentID = contentId
                newRecentlyContent.totalDuration = vm.duration
                // Only save essential data - the rest will be fetched from API
                newRecentlyContent.date = Date()
                newRecentlyContent.contentType = currentContentType
                newRecentlyContent.downloadId = downloadContent?.downloadId ?? ""
                newRecentlyContent.episodeId = Int16(episode?.id ?? 0)
                // These fields will be fetched from API:
                // name, episodeHorizontalPoster, thumbnail, episodeName
                newRecentlyContent.sourceUrl = url
                newRecentlyContent.isForDownload = isForDownloads
                newRecentlyContent.contentSourceType = Int16(type)
                newRecentlyContent.contentSourceId = Int16(sourceId)

                print("Created new entry with contentID: \(contentId), sourceId: \(sourceId), contentType: \(currentContentType), episodeId: \(newRecentlyContent.episodeId)")
            }

            // Save changes to the context
            DataController.shared.saveData()
        } catch {
            print("Failed to fetch RecentlyWatched: \(error.localizedDescription)")
        }
    }



    
    var videoProgressBar : some View {
        VStack{
            if !isLiveVideo {
                
                CustomProgressBars(vm: vm, value: $vm.currentTime, content: content, isplaying: self.$isplaying)
                    .padding(.horizontal,10)
            }
            HStack(spacing: 0) {
                if !isLiveVideo {
                    if !vm.currentTime.isNaN && !vm.currentTime.isInfinite{
                        Text("\((Int(vm.currentTime).secondsToTime())) / ")
                            .outfitMedium(14)
                            .foregroundColor(.text)
                    }
                    if !vm.duration.isNaN && !vm.duration.isInfinite{
                        Text("\(Int(vm.duration).secondsToTime())")
                            .outfitMedium(14)
                            .foregroundColor(Color(hexString: "A3A3A3"))
                    }
                }
                Spacer()
                if isLiveVideo {
                    Text(String.live.localized(language))
                        .outfitMedium(12)
                        .padding(5)
                        .padding(.horizontal)
                        .background(Color.base)
                        .cornerRadius(radius: 5)
                        .padding(.horizontal)
                }
                
                // Audio Track Selection Button
                if let audioTracks = content?.audioTracks ?? episode?.audioTracks, !audioTracks.isEmpty {
                    Button(action: {
                        showAudioTrackSelection = true
                    }) {
                        Image(systemName: "speaker.wave.2")
                            .resizeFitTo(size: 22, renderingMode: .template)
                            .foregroundColor(.text)
                    }
                    .padding(.horizontal, 5)
                }
                
                // Subtitle Track Selection Button
                if let subtitleTracks = content?.subtitleTracks ?? episode?.subtitleTracks, !subtitleTracks.isEmpty {
                    Button(action: {
                        showSubtitleTrackSelection = true
                    }) {
                        Image(systemName: "text.bubble")
                            .resizeFitTo(size: 22, renderingMode: .template)
                            .foregroundColor(.text)
                    }
                    .padding(.horizontal, 5)
                }
                
                Image.fullScreen
                    .resizeFitTo(size: 25, renderingMode: .template)
                    .foregroundColor(.text)
                    .onTap {
                        //                        print(orientationManager.orientation.isLandscape)
                        //                        if orientationManager.orientation.isLandscape {
                        //                            isPortrait = true
                        //                            isLandscape = false
                        //                        } else {
                        //                            isLandscape = true
                        //                            isPortrait = false
                        //                        }
                        if isLandscape {
                            AppDelegate.setOrientation(.portrait)
                            isPortrait = true
                            isLandscape = false
                        } else if isPortrait {
                            AppDelegate.setOrientation(.landscapeRight)
                            isPortrait = false
                            isLandscape = true
                        }
                    }
            }
            .padding(.horizontal,10)
        }
    }
    
    var videoPauseAndSkipControlles: some View {
        HStack(spacing: 4) {
            Button(action: {
                vm.pause()
                vm.jumpBackward()
                vm.play()
            }) {
                ZStack {
                    Image.tenR
                        .resizeFitTo(size: 46)
                        .foregroundColor(.text)
                        .padding(20)
                    Text("\(Limits.skipVideoSeconds)")
                        .outfitMedium(10)
                        .foregroundColor(.text)
                }
            }
            Button(action: {
                if vm.state == .playing {
                    vm.pause()
                } else {
                    vm.play()
                }
            }) {
                Image(vm.state == .playing ? "download_pause" : "play")
                    .resizeFitTo(size: 24, renderingMode: .template)
                    .rotationEffect(.degrees(language == .Arabic ? 180 : 0))
                    .foregroundColor(.white)
                    .frame(width: 24,height: 24)
                    .animation(nil,value: vm.state)
            }
            .padding(.horizontal)
            Button(action: {
                vm.pause()
                vm.jumpForward()
                vm.play()
            }) {
                ZStack {
                    Image.tenF
                        .resizeFitTo(size: 46)
                        .foregroundColor(.white)
                        .padding(20)
                    Text("\(Limits.skipVideoSeconds)")
                        .outfitMedium(10)
                        .foregroundColor(.text)
                }
            }
        }
    }
}

struct VolumeSlider : View {
    var width : CGFloat = 5
    var height : CGFloat = 120
    var imageName: String = "speaker.wave.3.fill"
    var currentValue : Float
    var body: some View {
        VStack {
            ZStack(alignment: .bottom) {
                Capsule()
                    .fill(PlayerConf.maximumSlidColor)
                    .frame(width: width, height: height, alignment: .center)
                Capsule()
                    .fill(PlayerConf.tintColor)
                    .frame(width: width, height: height * CGFloat(currentValue), alignment: .center)
            }
            .animation(.default, value: currentValue)
            Image(systemName: imageName)
                .resizeFitTo(size: 20,renderingMode: .template)
                .foregroundColor(.white)
        }
        .padding(12)
        .background(Color.black.opacity(0.3))
        .cornerRadius(radius: 15)
        .addStroke(radius: 16)
    }
}


struct CustomProgressBar : UIViewRepresentable {
    
    func makeCoordinator() -> CustomProgressBar.Coordinator {
        return CustomProgressBar.Coordinator(parent1: self)
    }
    @Binding var value : Float
    @Binding var player : AVPlayer
    @Binding var isplaying : Bool
    
    func makeUIView(context: UIViewRepresentableContext<CustomProgressBar>) ->
    
    UISlider {
        let slider = UISlider()
        slider.minimumTrackTintColor = .red
        slider.maximumTrackTintColor = UIColor.init(Color(hexString: "363636"))
        slider.thumbTintColor = .text
        
        slider.setThumbImage(UIImage(named: "thumb"), for: .normal)
        slider.value = value
        slider.addTarget(context.coordinator, action: #selector(context.coordinator.changed(slider:)), for: .valueChanged)
        return slider
    }
    
    func updateUIView(_ uiView: UISlider, context: UIViewRepresentableContext<CustomProgressBar>) {
        uiView.value = value
    }
    
    class Coordinator : NSObject{
        var parent : CustomProgressBar
        init(parent1 : CustomProgressBar) {
            parent = parent1
        }
        @objc func changed(slider : UISlider){
            if slider.isTracking{
                parent.player.pause()
                let sec = Double(slider.value * Float((parent.player.currentItem?.duration.seconds)!))
                parent.player.seek(to: CMTime(seconds: sec, preferredTimescale: 1))
            }
            else{
                let sec = Double(slider.value * Float((parent.player.currentItem?.duration.seconds)!))
                parent.player.seek(to: CMTime(seconds: sec, preferredTimescale: 1))
                if parent.isplaying{
                    parent.player.play()
                }
            }
        }
    }
}

struct CustomProgressBars: View {
    @StateObject var vm : PlayerModel
    @Binding var value: Double
    @State var content: VugaContent?
    @Binding var isplaying: Bool
    @State var isEditing = false
    @State var height : CGFloat = 5
    var body: some View {
        if !vm.duration.isNaN && !vm.duration.isInfinite {
            ValueSlider(value: $value,in: 0...(vm.duration), step: 0.1) { isEditing in
                withAnimation {
                    vm.isEditing = isEditing
                }
                if isEditing {
                    vm.pause()
                } else if !isEditing {
                    vm.seekVideo(second: vm.currentTime)
                    vm.play()
                }
            }
            .valueSliderStyle(
                HorizontalValueSliderStyle(
                    track: HorizontalTrack(view: Color.base)
                        .frame(height: height)
                        .background(Color(hexString: "363636")),
                    thumb: Color.text
                        .clipShape(.circle),
                    thumbSize: CGSize(width: 0, height: 0),
                    options: .interactiveTrack
                )
            )
            .frame(height: 10)
        }
    }
}

extension Image {
    func playerIcon() -> some View {
        self
            .resizable()
            .scaledToFit()
            .frame(width: 25, height: 25, alignment: .center)
            .foregroundColor(.text)
    }
}

struct TouchView :  UIViewRepresentable {
    var model : PlayerModel
    func makeUIView(context: Context) -> some UIView {
        model.view.backgroundColor = .clear
        return model.view
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        
    }
}
