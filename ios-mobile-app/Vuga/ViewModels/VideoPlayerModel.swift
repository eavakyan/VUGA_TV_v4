//
//  VideoPlayerModel.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 06/06/24.
//


import SwiftUI
import Foundation
import Combine
import MobileVLCKit
import AVKit
import Sliders
import SwiftSubtitles
import MediaPlayer
import GoogleCast


struct VideoPlayer : UIViewControllerRepresentable {
    var player : AVPlayer
    var isLive: Bool = false
    func makeUIViewController(context: UIViewControllerRepresentableContext<VideoPlayer>) -> AVPlayerViewController {
        
        let controller = AVPlayerViewController()
        controller.player = player
        controller.player?.play()
        controller.showsPlaybackControls = false
        if !isLive {
            controller.videoGravity = .resizeAspect
            controller.entersFullScreenWhenPlaybackBegins = true
            controller.exitsFullScreenWhenPlaybackEnds = true
            controller.player?.isMuted = false
            controller.updatesNowPlayingInfoCenter = false
            if #available(iOS 16.0, *) {
                controller.allowsVideoFrameAnalysis = false
            }
        }
        return controller
    }
    
    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: UIViewControllerRepresentableContext<VideoPlayer>) {}
    
}

enum PlayerState {
    case playing, stoped, buffering, paused, error, complete
}

struct MyCue {
    public let startTime: Double
    /// The time to dismiss the cue entry
    public let endTime: Double
    /// The text for the cue entry
    public let text: NSAttributedString
}

class PlayerModel: BaseViewModel {
    var view = UIView()
    @Environment(\.presentationMode) var present
    @Published var vlcPlayer: VLCMediaPlayer?
    @Published var player: AVPlayer?
    @Published var currentTime: Double = 0
    @Published var duration: Double = 1.0
    @Published var isMuted: Bool = false
    @Published var volume: Float = 1.0
    @Published var isFullscreen: Bool = false
    @Published var state = PlayerState.buffering
    @Published var isEditing = false
    @Published var timer : Timer?
    @Published var controlsTimer : Timer?
    @Published var skipTimer: Timer?
    @Published var showcontrols = false
    @Published var isVideoComplete : Bool = false
    @Published var showControlsDuration: Int = 0
    @Published var mySubtitles: [MyCue] = []
    @Published var selectedSubtitle: Subtitle?
    @Published var isVideoFaildToPlay: Bool = false
    
    @Published var currentVolume : Float = 0.0
    @Published var currentBritness : Float = 0.0
    @Published var showVolume = false
    @Published var showBrightness = false
    let volumeView = MPVolumeView()
    var isVolume = false
    var volumeViewTimer : Timer?
    var uiPanGeature : UIPanGestureRecognizer!
    var panDirection = PanDirection.horizontal
    @Published var showSeek = false
    @Published var sumTime = 0.0
    @Published var seekDirection = SeekDirection.forward
    var brightnessSlider = UISlider()
    
    
    override init() {
        super.init()
        configureVolAndBrit()
        configurePanRecognizer()
        NotificationCenter.default.addObserver(self, selector: #selector(handleInterruption(notification:)), name: AVAudioSession.interruptionNotification, object: nil)
        
        // Observe Cast state changes
        NotificationCenter.default.addObserver(self, selector: #selector(castStateDidChange), name: NSNotification.Name.gckCastStateDidChange, object: nil)
    }


    deinit {
        controlsTimer?.invalidate()
        controlsTimer = nil
        timer?.invalidate()
        timer = nil
        NotificationCenter.default.removeObserver(self)
    }
    
    func startControlsTimer() {
        controlsTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true, block: { timer in
            withAnimation(.default) {
                if self.showControlsDuration > 0 {
                    self.showControlsDuration -= 1
                } else {
                    self.showcontrols = false
                    self.controlsTimer?.invalidate()
                    self.controlsTimer = nil
                    self.showVolume = false
                    self.showBrightness = false
                }
            }
            print(self.showControlsDuration)
        })
    }
    
    func restartTimer() {
        self.controlsTimer?.invalidate()
        self.controlsTimer = nil
        showControlsDuration = Limits.videoControlsDuration
        startControlsTimer()
    }
    
    func setupPlayer(videoUrl: String, type: Int){
        // Check if Google Cast is connected
        let castContext = GCKCastContext.sharedInstance()
        if castContext.castState == .connected {
            // Handle Google Cast playback - will be called with metadata from VideoPlayerView
            return
        }
        
        if let url = URL(string: videoUrl) {
            if type == 2 || type == 3 || type == 4 {
                // Configure audio session for AirPlay
                do {
                    try AVAudioSession.sharedInstance().setCategory(.playback, mode: .moviePlayback)
                    try AVAudioSession.sharedInstance().setActive(true)
                } catch {
                    print("Failed to configure audio session: \(error)")
                }
                
                player = AVPlayer(url: url)
                // Enable AirPlay
                player?.allowsExternalPlayback = true
                player?.usesExternalPlaybackWhileExternalScreenIsActive = true
            } else if type == 5 || type == 6 || type == 7 {
                vlcPlayer = VLCMediaPlayer()
                vlcPlayer?.media = VLCMedia(url: url)
                vlcPlayer?.delegate = self
            }
            play()
        } else {
            isVideoFaildToPlay = true
        }
    }
    
    func play() {
        if castSession != nil {
            remoteMediaClient?.play()
            state = .playing
            restartTimer()
        } else {
            player?.play()
            vlcPlayer?.play()
            state = .playing
            restartTimer()
            timer = Timer.scheduledTimer(withTimeInterval: player != nil ? 0.1 : 0.3, repeats: true, block: { timer in
                self.state = .playing
                if !self.isEditing {
                    self.getCurrentPosition()
                }
            })
        }
    }
    
    func pause() {
        if castSession != nil {
            remoteMediaClient?.pause()
            restartTimer()
            state = .paused
        } else {
            player?.pause()
            vlcPlayer?.pause()
            restartTimer()
            state = .paused
            timer?.invalidate()
            timer = nil
        }
    }
    
    func stop() {
        player?.pause()
        player?.seek(to: CMTime.zero)
        vlcPlayer?.stop()
    }
    
    func jumpForward() {
        if castSession != nil {
            seekToCast(time: currentTime + Double(Limits.skipVideoSeconds))
        } else {
            player?.seek(to: CMTime(seconds: currentTime + Double(Limits.skipVideoSeconds), preferredTimescale: 10))
            vlcPlayer?.jumpForward(Int32(Limits.skipVideoSeconds))
        }
        restartTimer()
    }
    
    func jumpBackward() {
        if castSession != nil {
            seekToCast(time: max(0, currentTime - Double(Limits.skipVideoSeconds)))
        } else {
            player?.seek(to: CMTime(seconds: currentTime - Double(Limits.skipVideoSeconds), preferredTimescale: 10))
            vlcPlayer?.jumpBackward(Int32(Limits.skipVideoSeconds))
        }
        restartTimer()
    }
    
    func seekVideo(second: Double) {
        restartTimer()
        
        if castSession != nil {
            seekToCast(time: second)
        } else {
            vlcPlayer?.time = VLCTime(int: Int32(currentTime * 1000))
            
            if let player = player {
                player.seek(to: CMTime(seconds: second, preferredTimescale: .max)) { finished in
                    if finished {
                        print("AVPlayer successfully sought to: \(second) seconds")
                    }
                }
            }
        }
    }
    
    func toggleMute(isMute: Bool) {
        restartTimer()
        isMuted.toggle()
        player?.isMuted = isMute
        vlcPlayer?.audio.isMuted = isMute
    }
    
    func setVolume(_ volume: Float) {
        self.volume = volume
        player?.volume = volume
    }
    
    func getCurrentPosition() {
        if let player = player {
            currentTime = player.currentTime().seconds
            duration = player.currentItem?.duration.seconds ?? 0.0
        } else {
            if let vlcPlayer = vlcPlayer {
                    let videoCurrentTime = (Int(truncating: vlcPlayer.time.value ?? 0))
                    let videoDuration = Int(truncating: vlcPlayer.time.value ?? 0) + (-1 * Int(truncating: vlcPlayer.remainingTime.value ?? 0))
                    if videoDuration > 0 {
                        duration = Double(videoDuration / 1000)
                    }
                    currentTime = Double(videoCurrentTime / 1000)
            }
        }
    }
    func setPlaybackSpeed(_ speed: Float) {
        player?.rate = speed
        vlcPlayer?.rate = speed
    }
    
    func addPlayerObserver() {
        let interval = CMTime(seconds: 1, preferredTimescale: CMTimeScale(NSEC_PER_SEC))
        player?.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] time in
            self?.currentTime = time.seconds
            if let duration = self?.player?.currentItem?.duration.seconds {
                self?.duration = duration
            }
        }
    }
    
    @objc func vlcTimeChanged(notification: NSNotification) {
        if let vlcPlayer = vlcPlayer {
            currentTime = Double(vlcPlayer.time.intValue) / 1000.0
            duration = Double(vlcPlayer.media.length.intValue) / 1000.0
        }
    }
    
//    func replay() {
//        stop()
//        isVideoComplete = false
//        state = .playing
//        seekVideo(second: 0)
//        play()
//    }
    
    func loadSubtitles(from urlString: String) {
        mySubtitles.removeAll()
        
        guard let url = URL(string: urlString) else {
            print("Invalid URL")
            return
        }

        DispatchQueue.global(qos: .background).async {
            URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
                guard let self = self else { return }
                
                if let error = error {
                    print(error.localizedDescription)
                    return
                }

                guard let data = data else {
                    print(error?.localizedDescription ?? "Unknown error")
                    return
                }

                do {
                    let subtitleContent = String(data: data, encoding: .utf8) ?? ""
                    let coder = Subtitles.Coder.SRT()
                    let subtitles = try coder.decode(subtitleContent).cues
                    
                    let parsedSubtitles = subtitles.map { subtitle -> MyCue in
                        let text = NSAttributedString(html: subtitle.text, fontSize: 15) ?? NSAttributedString(string: subtitle.text)
                        return MyCue(startTime: subtitle.startTimeInSeconds, endTime: subtitle.endTimeInSeconds, text: text)
                    }

                    DispatchQueue.main.async {
                        self.mySubtitles = parsedSubtitles
                    }
                } catch {
                    print(error.localizedDescription)
                }
            }.resume()
        }
    }

    
    func formatTime(_ time: Subtitles.Time) -> String {
        let minutes = String(format: "%02d", time.minute)
        let seconds = String(format: "%02d", time.second)
        return "\(minutes):\(seconds)"
    }
    
    @objc func handleInterruption(notification: Notification) {
        guard let info = notification.userInfo,
              let typeValue = info[AVAudioSessionInterruptionTypeKey] as? UInt,
              let type = AVAudioSession.InterruptionType(rawValue: typeValue) else {
            return
        }
        
        if type == .began {
            self.pause()
        } else if type == .ended {
            guard let optionsValue =
                    info[AVAudioSessionInterruptionOptionKey] as? UInt else {
                return
            }
            let options = AVAudioSession.InterruptionOptions(rawValue: optionsValue)
            if options.contains(.shouldResume) {
                self.play()
            }
        }
    }
    
    func configureVolAndBrit(){
        volumeView.alpha = 0.00001
        let slider = volumeView.subviews.first(where: { $0 is UISlider }) as? UISlider
        
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.01) {
            self.currentVolume = slider?.value ?? 0.0
        }
        view.addSubview(volumeView)
        
        brightnessSlider.value = Float(UIScreen.main.brightness)
    }
    
    func configurePanRecognizer(){
        uiPanGeature = UIPanGestureRecognizer(target: self, action: #selector(panDirection(_:)))
        uiPanGeature.isEnabled = true
        view.addGestureRecognizer(uiPanGeature)
    }
    
    
    @objc func panDirection(_ pan: UIPanGestureRecognizer) {
        
        let locationPoint = pan.location(in: view)
        
        let velocityPoint = pan.velocity(in: view)
        print("A5")

        switch pan.state {

        case UIGestureRecognizer.State.began:
            let x = abs(velocityPoint.x)
            let y = abs(velocityPoint.y)
            
            if x > y {
                if PlayerConf.enablePlaytimeGestures {
                    self.panDirection = PanDirection.horizontal
                    if let player = player {
                        let time = player.currentTime()
                        self.sumTime = TimeInterval(time.value) / TimeInterval(time.timescale)
                        print("A4")
                    }
                }
            } else {
                volumeViewTimer?.invalidate()
                self.panDirection = PanDirection.vertical
                if locationPoint.x > view.bounds.size.width / 2 {
                    self.isVolume = true
                    print("A2")
                } else {
                    self.isVolume = false
                    print("A3")
                }
            }
            
        case UIGestureRecognizer.State.changed:
            switch self.panDirection {
            case PanDirection.horizontal:
                self.horizontalMoved(velocityPoint.x)
            case PanDirection.vertical:
                self.verticalMoved(velocityPoint.y)
                print(velocityPoint.y)
                print(velocityPoint.x)
            }
            
        case UIGestureRecognizer.State.ended:
            switch (self.panDirection) {
            case PanDirection.horizontal:
                print("A6")

//                self.seekTo(sumTime)
                player?.play()
            case PanDirection.vertical:
                self.isVolume = false
                volumeViewTimer?.invalidate()
                volumeViewTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: false, block: { [self] timer in
                    hideVolumeView()
                    hideSeekView()
                    hideBrightnessView()
                    print("A1")
                })
            }
        default:
            break
        }
    }
    
    func hideSeekView() {
        self.seekDirection = .forward
        self.showSeek = false
    }
    
    func showSeekView(isForward: Bool) {
        self.showSeek = true
        self.seekDirection = isForward ? .forward : .backward
    }
    
    func showVolumeView(){
        self.showVolume = true
    }
    
    func hideVolumeView(){
        self.showVolume = false
    }
    
    func showBrightnessView(){
        self.showBrightness = true
    }
    
    func hideBrightnessView(){
        self.showBrightness = false
    }
    
    func horizontalMoved(_ value: CGFloat) {
        guard PlayerConf.enablePlaytimeGestures else { return }
        
        if let player = player {
            let totalTime = player.currentItem?.duration ?? .zero
            self.sumTime = self.sumTime + Double(value) / 100.0 * ((totalTime.seconds)/400)
            if totalTime.timescale == 0 { return }
            
            let totalDuration = TimeInterval(totalTime.value) / TimeInterval(totalTime.timescale)
            if (self.sumTime >= totalDuration) { self.sumTime = totalDuration }
            if (self.sumTime <= 0) { self.sumTime = 0 }
            showSeekView(isForward: value > 0)
        }
    }
        
    func verticalMoved(_ value: CGFloat) {
        if PlayerConf.enableVolumeGestures && self.isVolume{
            
            hideBrightnessView()
            showVolumeView()
            let slider = volumeView.subviews.first(where: { $0 is UISlider }) as? UISlider
            
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.01) { [self] in
                volumeViewTimer?.invalidate()
                slider?.value -= Float(value / 10000)
                currentVolume = slider?.value ?? 0
            }
        }
        
        else if PlayerConf.enableBrightnessGestures && !self.isVolume{
            hideVolumeView()
            showBrightnessView()
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.01) { [self] in
                brightnessSlider.value -= Float(value / 10000)
                let new = brightnessSlider.value
                UIScreen.main.brightness = CGFloat(new)
                self.currentBritness = Float(UIScreen.main.brightness)
            }
        }
    }
    
    // MARK: - Google Cast Support
    var castSession: GCKCastSession? {
        return GCKCastContext.sharedInstance().sessionManager.currentCastSession
    }
    
    var remoteMediaClient: GCKRemoteMediaClient? {
        return castSession?.remoteMediaClient
    }
    
    func setupCastPlayback(videoUrl: String, title: String? = nil, subtitle: String? = nil, imageUrl: String? = nil) {
        guard castSession != nil else {
            print("❌ Cast: No active cast session")
            return
        }
        
        print("🎬 Cast: Setting up playback for URL: \(videoUrl)")
        
        // Test with a public video first to verify Cast is working
        let testMode = false // Disabled since we're using direct casting from ContentDetailView
        let finalVideoUrl = testMode ? "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" : videoUrl
        
        print("🔍 Cast: Original video URL: \(videoUrl)")
        print("🔍 Cast: Final video URL (test mode: \(testMode)): \(finalVideoUrl)")
        
        // Ensure URL is valid
        guard let mediaURL = URL(string: finalVideoUrl) else {
            print("❌ Cast: Invalid video URL")
            return
        }
        
        // Build media metadata
        let metadata = GCKMediaMetadata(metadataType: .movie)
        metadata.setString(title ?? "Video", forKey: kGCKMetadataKeyTitle)
        if let subtitle = subtitle {
            metadata.setString(subtitle, forKey: kGCKMetadataKeySubtitle)
        }
        
        // Add image if available (ensure HTTPS)
        if let imageUrl = imageUrl, 
           let url = URL(string: imageUrl.replacingOccurrences(of: "http://", with: "https://")) {
            metadata.addImage(GCKImage(url: url, width: 480, height: 360))
        }
        
        // Create media information
        let mediaInfoBuilder = GCKMediaInformationBuilder(contentURL: mediaURL)
        mediaInfoBuilder.streamType = .buffered
        
        // Determine content type from URL
        let pathExtension = mediaURL.pathExtension.lowercased()
        switch pathExtension {
        case "mp4":
            mediaInfoBuilder.contentType = "video/mp4"
        case "m3u8":
            mediaInfoBuilder.contentType = "application/x-mpegURL"
        case "mkv":
            mediaInfoBuilder.contentType = "video/x-matroska"
        case "webm":
            mediaInfoBuilder.contentType = "video/webm"
        case "mov":
            mediaInfoBuilder.contentType = "video/quicktime"
        default:
            mediaInfoBuilder.contentType = "video/mp4" // Default fallback
        }
        
        mediaInfoBuilder.metadata = metadata
        
        // Add custom data for CORS handling
        mediaInfoBuilder.customData = [
            "playbackRate": 1.0,
            "headers": [
                "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148"
            ]
        ]
        
        let mediaInfo = mediaInfoBuilder.build()
        
        // Create load request
        let loadRequestBuilder = GCKMediaLoadRequestDataBuilder()
        loadRequestBuilder.mediaInformation = mediaInfo
        loadRequestBuilder.autoplay = true
        loadRequestBuilder.playbackRate = 1.0
        
        let request = loadRequestBuilder.build()
        
        // Load media on cast device
        let loadRequest = remoteMediaClient?.loadMedia(with: request)
        loadRequest?.delegate = self
        
        print("✅ Cast: Media load request sent")
        print("📹 Cast: Content type: \(mediaInfoBuilder.contentType ?? "unknown")")
        self.state = .playing
        self.startCastProgressTimer()
    }
    
    func startCastProgressTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            if let remoteMediaClient = self.remoteMediaClient {
                self.currentTime = remoteMediaClient.mediaStatus?.streamPosition ?? 0
                self.duration = remoteMediaClient.mediaStatus?.mediaInformation?.streamDuration ?? 1
                
                // Update state based on player state
                switch remoteMediaClient.mediaStatus?.playerState {
                case .playing:
                    self.state = .playing
                case .paused:
                    self.state = .paused
                case .buffering:
                    self.state = .buffering
                case .idle:
                    if remoteMediaClient.mediaStatus?.idleReason == .finished {
                        self.state = .complete
                        self.isVideoComplete = true
                    }
                default:
                    break
                }
            }
        }
    }
    
    func seekToCast(time: Double) {
        if let remoteMediaClient = remoteMediaClient {
            let options = GCKMediaSeekOptions()
            options.interval = time
            remoteMediaClient.seek(with: options)
        }
    }
    
    @objc func castStateDidChange() {
        let castContext = GCKCastContext.sharedInstance()
        print("🎯 Cast state changed: \(castContext.castState.rawValue)")
        
        if castContext.castState == .noDevicesAvailable || castContext.castState == .notConnected {
            // Cast disconnected, stop timer
            timer?.invalidate()
            timer = nil
        }
    }
}

enum PanDirection {
    case vertical
    case horizontal
}

enum SeekDirection {
    case forward
    case backward
}

enum PlayerStatus {
    case unknown
    case playing
    case paused
    case loading
    case error
}



extension Double {
    func formateToString() -> String {
        if self.isNaN || self.isInfinite {
            return "00:00"
        }
        let s:Int = Int(self.truncatingRemainder(dividingBy: 60));
        
        let m:Int = Int((self / 60).truncatingRemainder(dividingBy: 60));
        
        let h:Int = Int(((self / 60) / 60).truncatingRemainder(dividingBy: 60));
        
        if (m >= 59) || h > 0 {
            return String.init(format: "%02d:%02d:%02d", arguments: [h, m, s]);
        }
        return String.init(format:"%02d:%02d", m, s)
    }
}
class PlayerConf {
    static var enablePlaytimeGestures = true
    static var enableVolumeGestures = true
    static var enableBrightnessGestures = true
    static var backgroundColor = Color.black.opacity(0.4)
    static var textColor = Color.white
    static var tintColor = Color.red
    static var maximumSlidColor = Color.white.opacity(0.3)
    static var hideControlAfterSec = 2
}


extension PlayerModel: GCKRequestDelegate {
    func requestDidComplete(_ request: GCKRequest) {
        print("✅ Cast: Request completed successfully")
    }
    
    func request(_ request: GCKRequest, didFailWithError error: GCKError) {
        print("❌ Cast: Request failed with error: \(error.localizedDescription)")
        print("🔍 Cast: Error code: \(error.code)")
        
        // Handle specific error codes
        switch error.code {
        case 1:
            print("📡 Cast: Network error - Check if the Cast device can access the media URL")
        case 2:
            print("⏱ Cast: Request timed out")
            print("💡 Tip: The media URL might be inaccessible from the Cast device")
        case 3:
            print("⚠️ Cast: Invalid request format")
        case 4:
            print("🚫 Cast: Request was cancelled")
        case 5:
            print("🔄 Cast: Request was replaced by another request")
        case 6:
            print("🚫 Cast: Operation not allowed in current state")
        case 7:
            print("♻️ Cast: Duplicate request")
        case 8:
            print("⚠️ Cast: Invalid media player state")
        case 30:
            print("❌ Cast: Media failed to load (Error 30)")
            print("💡 This usually indicates:")
            print("   - Network connectivity issue between Cast device and media server")
            print("   - CORS (Cross-Origin Resource Sharing) not configured on server")
            print("   - Media URL not accessible from Cast device's network")
            print("   - Invalid or unsupported media format")
            print("🔧 Try:")
            print("   - Test with a public URL (like YouTube or Google's sample videos)")
            print("   - Check if the URL loads in a web browser from the same network")
            print("   - Verify CORS headers allow Cast device access")
        case 2100, 2101, 2102, 2103:
            print("❌ Cast: Media load failed - Check URL accessibility and format")
            print("💡 Tip: Ensure the media URL is publicly accessible and CORS-enabled")
            print("🔍 Common issues:")
            print("   - CORS not configured on the server")
            print("   - URL not accessible from Cast device's network")
            print("   - Unsupported media format")
        default:
            print("❓ Cast: Unknown error code: \(error.code)")
        }
        
        // Reset state on error
        self.state = .error
        self.isVideoFaildToPlay = true
        
        // Try fallback: Play locally if Cast fails
        timer?.invalidate()
        timer = nil
    }
}

extension PlayerModel : VLCMediaPlayerDelegate {
    func mediaPlayerStateChanged(_ aNotification: Notification!) {
        print("????????????",aNotification.description)
        
        switch vlcPlayer?.state {
        case .buffering :
            print("buffer")
            state = .buffering
        case .stopped:
            print("stop")
            state = .stoped
        case .opening:
            print("opening")
            state = .playing
            stopLoading()
        case .ended:
            print("ended")
            state = .complete
        case .error:
            print("error")
            state = .error
        case .playing:
            print("playing")
            state = .playing
            stopLoading()
        case .paused:
            print("paused")
            state = .paused
        case .esAdded:
            print("esAdded")
            stopLoading()
        case .none:
            break
        @unknown default:
            print("default")
        }
    }
}

struct SubtitlesView: View {
    @State private var subtitles: [Subtitles.Cue] = []
    @State private var errorMessage: String? = nil

    var body: some View {
        VStack {
            if let error = errorMessage {
                Text(error)
                    .foregroundColor(.red)
                    .padding()
            } else if subtitles.isEmpty {
                Text("Loading subtitles...")
                    .padding()
            } else {
                List(subtitles, id: \.position) { cue in
                    VStack(alignment: .leading) {
                        Text("\(formatTime(cue.startTime)) --> \(formatTime(cue.endTime))")
                            .font(.caption)
                        Text(cue.text)
                            .padding(.vertical, 2)
                    }
                    .padding()
                }
            }
        }
        .onAppear {
            loadSubtitles(from: "http://192.168.0.107/flixy_backend/public/storage/uploads/666bef24baf06.srt")
        }
    }

    func loadSubtitles(from urlString: String) {
        guard let url = URL(string: urlString) else {
            self.errorMessage = "Invalid URL"
            return
        }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                DispatchQueue.main.async {
                    self.errorMessage = "Failed to load subtitles: \(error.localizedDescription)"
                }
                return
            }
            guard let data = data else {
                DispatchQueue.main.async {
                    self.errorMessage = "No data received"
                }
                return
            }
            do {
                let subtitleContent = String(data: data, encoding: .utf8) ?? ""
                let coder = Subtitles.Coder.SRT() // Using the SRT coder
                let subtitles = try coder.decode(subtitleContent)
                DispatchQueue.main.async {
                    self.subtitles = subtitles.cues
                }
            } catch {
                DispatchQueue.main.async {
                    self.errorMessage = "Failed to decode subtitles: \(error.localizedDescription)"
                }
            }
        }.resume()
    }

    func formatTime(_ time: Subtitles.Time) -> String {
        let minutes = String(format: "%02d", time.minute)
        let seconds = String(format: "%02d", time.second)
        return "\(minutes):\(seconds)"
    }
}
