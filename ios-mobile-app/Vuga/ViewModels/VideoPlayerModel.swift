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
    }


    deinit {
        controlsTimer?.invalidate()
        controlsTimer = nil
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
        if let url = URL(string: videoUrl) {
            if type == 2 || type == 3 || type == 4 {
                player = AVPlayer(url: url)
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
    
    func pause() {
        player?.pause()
        vlcPlayer?.pause()
        restartTimer()
        state = .paused
        timer?.invalidate()
        timer = nil
    }
    
    func stop() {
        player?.pause()
        player?.seek(to: CMTime.zero)
        vlcPlayer?.stop()
    }
    
    func jumpForward() {
        player?.seek(to: CMTime(seconds: currentTime + Double(Limits.skipVideoSeconds), preferredTimescale: 10))
        vlcPlayer?.jumpForward(Int32(Limits.skipVideoSeconds))
        restartTimer()
    }
    
    func jumpBackward() {
        player?.seek(to: CMTime(seconds: currentTime - Double(Limits.skipVideoSeconds), preferredTimescale: 10))
        vlcPlayer?.jumpBackward(Int32(Limits.skipVideoSeconds))
        restartTimer()
    }
    
    func seekVideo(second: Double) {
        restartTimer()
        
        vlcPlayer?.time = VLCTime(int: Int32(currentTime * 1000))
        
        if let player = player {
            player.seek(to: CMTime(seconds: second, preferredTimescale: .max)) { finished in
                if finished {
                    print("AVPlayer successfully sought to: \(second) seconds")
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
    
    @objc private func vlcTimeChanged(notification: NSNotification) {
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
}

extension PlayerModel {
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
    
    
    @objc fileprivate func panDirection(_ pan: UIPanGestureRecognizer) {
        
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
    
    private func hideSeekView() {
        self.seekDirection = .forward
        self.showSeek = false
    }
    
    private func showSeekView(isForward: Bool) {
        self.showSeek = true
        self.seekDirection = isForward ? .forward : .backward
    }
    
    private func showVolumeView(){
        self.showVolume = true
    }
    
    private func hideVolumeView(){
        self.showVolume = false
    }
    
    private func showBrightnessView(){
        self.showBrightness = true
    }
    
    private func hideBrightnessView(){
        self.showBrightness = false
    }
    
    fileprivate func horizontalMoved(_ value: CGFloat) {
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
        
    fileprivate func verticalMoved(_ value: CGFloat) {
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

