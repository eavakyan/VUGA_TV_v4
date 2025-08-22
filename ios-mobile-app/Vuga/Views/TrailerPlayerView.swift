//
//  TrailerPlayerView.swift
//  Vuga
//
//

import SwiftUI
import AVKit
import WebKit

struct TrailerPlayerView: View {
    let content: VugaContent?
    let trailerUrl: String?
    let specificTrailer: TrailerModel?
    
    @Environment(\.presentationMode) var presentationMode
    @State private var isYouTubeUrl = false
    @State private var youTubeVideoId: String?
    @State private var effectiveTrailerUrl: String = ""
    @State private var player: AVPlayer?
    @State private var isLoading = true
    
    // Multiple initializers for different use cases
    init(content: VugaContent) {
        self.content = content
        self.trailerUrl = nil
        self.specificTrailer = nil
    }
    
    init(trailerUrl: String) {
        self.content = nil
        self.trailerUrl = trailerUrl
        self.specificTrailer = nil
    }
    
    init(trailer: TrailerModel) {
        self.content = nil
        self.trailerUrl = nil
        self.specificTrailer = trailer
    }
    
    var body: some View {
        ZStack {
            Color.black.edgesIgnoringSafeArea(.all)
            
            if isYouTubeUrl, let videoId = youTubeVideoId {
                // YouTube player using WebView
                YouTubeWebView(videoId: videoId)
                    .edgesIgnoringSafeArea(.all)
            } else if let player = player {
                // Regular video player for CDN URLs
                VideoPlayer(player: player)
                    .edgesIgnoringSafeArea(.all)
            } else if isLoading && !effectiveTrailerUrl.isEmpty {
                // Loading state
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .scaleEffect(1.5)
            } else {
                // No trailer available
                VStack {
                    Image(systemName: "video.slash")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("No trailer available")
                        .foregroundColor(.gray)
                        .padding()
                }
            }
            
            // Close button
            VStack {
                HStack {
                    Spacer()
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .resizable()
                            .frame(width: 30, height: 30)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    }
                    .padding()
                }
                Spacer()
            }
        }
        .onAppear {
            setupTrailerUrl()
            analyzeTrailerUrl()
            if !isYouTubeUrl && !effectiveTrailerUrl.isEmpty {
                setupVideoPlayer()
            }
        }
    }
    
    private func setupTrailerUrl() {
        // Determine effective trailer URL based on input
        if let trailer = specificTrailer {
            // Use specific trailer
            effectiveTrailerUrl = trailer.effectiveTrailerUrl
        } else if let content = content {
            // Use content's primary trailer
            effectiveTrailerUrl = TrailerUtils.getEffectiveTrailerUrl(for: content) ?? ""
        } else if let trailerUrl = trailerUrl {
            // Use provided trailer URL
            effectiveTrailerUrl = trailerUrl
        }
    }
    
    private func analyzeTrailerUrl() {
        // Check if it's a YouTube URL
        if effectiveTrailerUrl.contains("youtube.com") || effectiveTrailerUrl.contains("youtu.be") || effectiveTrailerUrl.contains("youtube-nocookie.com") {
            isYouTubeUrl = true
            isLoading = false
            // Try to get YouTube ID from trailer object first
            if let trailer = specificTrailer, !trailer.effectiveYoutubeId.isEmpty {
                youTubeVideoId = trailer.effectiveYoutubeId
            } else if let content = content, let youtubeId = TrailerUtils.getEffectiveYouTubeId(for: content) {
                youTubeVideoId = youtubeId
            } else {
                youTubeVideoId = extractYouTubeVideoId(from: effectiveTrailerUrl)
            }
        } else {
            isYouTubeUrl = false
        }
    }
    
    private func setupVideoPlayer() {
        guard let url = URL(string: effectiveTrailerUrl) else {
            isLoading = false
            return
        }
        
        // Load asset asynchronously to avoid blocking main thread
        let asset = AVURLAsset(url: url)
        let keys = ["playable", "hasProtectedContent"]
        
        asset.loadValuesAsynchronously(forKeys: keys) {
            var error: NSError?
            let status = asset.statusOfValue(forKey: "playable", error: &error)
            
            DispatchQueue.main.async {
                if status == .loaded && asset.isPlayable {
                    let playerItem = AVPlayerItem(asset: asset)
                    self.player = AVPlayer(playerItem: playerItem)
                    self.player?.play() // Auto-play trailer
                    self.isLoading = false
                } else {
                    print("TrailerPlayerView: Asset not playable or error loading")
                    self.isLoading = false
                }
            }
        }
    }
    
    private func extractYouTubeVideoId(from url: String) -> String? {
        // Handle different YouTube URL formats
        if url.contains("youtube.com/watch?v=") {
            // Standard YouTube URL
            let components = URLComponents(string: url)
            return components?.queryItems?.first(where: { $0.name == "v" })?.value
        } else if url.contains("youtu.be/") {
            // Shortened YouTube URL
            let components = url.components(separatedBy: "youtu.be/")
            if components.count > 1 {
                let idComponent = components[1]
                // Remove any query parameters
                return idComponent.components(separatedBy: "?").first
            }
        } else if url.contains("youtube.com/embed/") {
            // Embedded YouTube URL
            let components = url.components(separatedBy: "embed/")
            if components.count > 1 {
                let idComponent = components[1]
                // Remove any query parameters
                return idComponent.components(separatedBy: "?").first
            }
        }
        
        // If URL doesn't match expected patterns, assume it's already a video ID
        return url
    }
}

// YouTube WebView Component
struct YouTubeWebView: UIViewRepresentable {
    let videoId: String
    
    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []
        
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.scrollView.isScrollEnabled = false
        webView.backgroundColor = .black
        
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
                iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <iframe 
                src="https://www.youtube.com/embed/\(videoId)?autoplay=1&playsinline=1&rel=0&showinfo=0&modestbranding=1"
                frameborder="0"
                allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen>
            </iframe>
        </body>
        </html>
        """
        
        webView.loadHTMLString(embedHTML, baseURL: nil)
    }
}