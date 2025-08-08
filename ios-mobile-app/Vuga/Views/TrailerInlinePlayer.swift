//
//  TrailerInlinePlayer.swift
//  Vuga
//
//  Created by Assistant on iOS Trailer Inline Update
//

import SwiftUI
import AVKit
import WebKit

struct TrailerInlinePlayer: View {
    let trailerUrl: String
    @State private var isYouTubeUrl = false
    @State private var youTubeVideoId: String?
    @State private var player: AVPlayer?
    @State private var isPlaying = false
    @State private var showControls = true
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                if isYouTubeUrl, let videoId = youTubeVideoId {
                    // YouTube player using WebView (inline)
                    YouTubeInlineWebView(videoId: videoId)
                } else if let player = player {
                    // Regular video player for CDN URLs
                    VideoPlayer(player: player)
                        .disabled(false) // Allow interaction
                        .onTapGesture {
                            // Toggle play/pause on tap
                            if player.timeControlStatus == .playing {
                                player.pause()
                            } else {
                                player.play()
                            }
                        }
                } else {
                    // Loading state
                    Color.black
                        .overlay(
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .scaleEffect(2)
                        )
                }
                
                // Play button overlay for non-YouTube videos
                if !isYouTubeUrl && showControls {
                    Button(action: {
                        togglePlayPause()
                    }) {
                        Image(systemName: isPlaying ? "pause.circle.fill" : "play.circle.fill")
                            .resizable()
                            .frame(width: 70, height: 70)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    }
                }
            }
        }
        .onAppear {
            analyzeTrailerUrl()
        }
        .onDisappear {
            player?.pause()
        }
    }
    
    private func analyzeTrailerUrl() {
        // Check if it's a YouTube URL
        if trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be") || trailerUrl.contains("youtube-nocookie.com") {
            isYouTubeUrl = true
            youTubeVideoId = extractYouTubeVideoId(from: trailerUrl)
        } else {
            isYouTubeUrl = false
            setupVideoPlayer()
        }
    }
    
    private func setupVideoPlayer() {
        guard let url = URL(string: trailerUrl) else { return }
        
        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)
        
        // Set up looping
        NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: playerItem,
            queue: .main
        ) { _ in
            self.player?.seek(to: .zero)
            self.player?.play()
        }
        
        // Auto-start playing after player is ready
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.player?.play()
            self.isPlaying = true
        }
        
        // Auto-hide controls after 3 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            withAnimation {
                showControls = false
            }
        }
    }
    
    private func togglePlayPause() {
        guard let player = player else { return }
        
        if player.timeControlStatus == .playing {
            player.pause()
            isPlaying = false
        } else {
            player.play()
            isPlaying = true
            
            // Hide controls after 3 seconds when playing
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                if isPlaying {
                    withAnimation {
                        showControls = false
                    }
                }
            }
        }
        
        // Show controls temporarily
        withAnimation {
            showControls = true
        }
    }
    
    private func extractYouTubeVideoId(from url: String) -> String? {
        // Handle different YouTube URL formats
        if url.contains("youtube.com/watch?v=") {
            let components = URLComponents(string: url)
            return components?.queryItems?.first(where: { $0.name == "v" })?.value
        } else if url.contains("youtu.be/") {
            let components = url.components(separatedBy: "youtu.be/")
            if components.count > 1 {
                let idComponent = components[1]
                return idComponent.components(separatedBy: "?").first
            }
        } else if url.contains("youtube.com/embed/") {
            let components = url.components(separatedBy: "embed/")
            if components.count > 1 {
                let idComponent = components[1]
                return idComponent.components(separatedBy: "?").first
            }
        }
        
        // If URL doesn't match expected patterns, assume it's already a video ID
        return url
    }
}

// YouTube Inline WebView Component
struct YouTubeInlineWebView: UIViewRepresentable {
    let videoId: String
    
    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []
        
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.scrollView.isScrollEnabled = false
        webView.backgroundColor = .black
        webView.isOpaque = false
        
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        let embedHTML = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body { margin: 0; padding: 0; background: black; }
                .video-container { position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; }
                .video-container iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <div class="video-container">
                <iframe 
                    src="https://www.youtube.com/embed/\(videoId)?autoplay=1&loop=1&playlist=\(videoId)&playsinline=1&rel=0&showinfo=0&modestbranding=1&controls=1&mute=1"
                    frameborder="0"
                    allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
                    allowfullscreen>
                </iframe>
            </div>
        </body>
        </html>
        """
        
        webView.loadHTMLString(embedHTML, baseURL: nil)
    }
}