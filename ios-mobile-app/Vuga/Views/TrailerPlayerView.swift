//
//  TrailerPlayerView.swift
//  Vuga
//
//  Created by Assistant on iOS Trailer Update
//

import SwiftUI
import AVKit
import WebKit

struct TrailerPlayerView: View {
    let trailerUrl: String
    @Environment(\.presentationMode) var presentationMode
    @State private var isYouTubeUrl = false
    @State private var youTubeVideoId: String?
    
    var body: some View {
        ZStack {
            Color.black.edgesIgnoringSafeArea(.all)
            
            if isYouTubeUrl, let videoId = youTubeVideoId {
                // YouTube player using WebView
                YouTubeWebView(videoId: videoId)
                    .edgesIgnoringSafeArea(.all)
            } else {
                // Regular video player for CDN URLs
                VideoPlayer(player: AVPlayer(url: URL(string: trailerUrl)!))
                    .edgesIgnoringSafeArea(.all)
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
            analyzeTrailerUrl()
        }
    }
    
    private func analyzeTrailerUrl() {
        // Check if it's a YouTube URL
        if trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be") || trailerUrl.contains("youtube-nocookie.com") {
            isYouTubeUrl = true
            youTubeVideoId = extractYouTubeVideoId(from: trailerUrl)
        } else {
            isYouTubeUrl = false
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