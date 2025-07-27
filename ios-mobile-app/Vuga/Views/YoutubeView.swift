//
//  YoutubeView.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 25/06/24.
//

import Foundation
import SwiftUI
import YouTubePlayerKit
import AVKit

struct YoutubeView: View {
    @Environment(\.presentationMode) var present
    var youtubeUrl: String
    private let configuration: YouTubePlayer.Configuration = {
        YouTubePlayer.Configuration(
            automaticallyAdjustsContentInsets: true,
            fullscreenMode: .web,
            autoPlay: true,
            showRelatedVideos: false
        )
    }()
    var body: some View {
        ZStack(alignment: .topTrailing) {
            YouTubePlayerView(YouTubePlayer(
                source: .url(WebService.youtubeBaseURL + youtubeUrl),
                configuration: configuration
            ))
            CloseButtonWithbg(onTap: {
                present.wrappedValue.dismiss()
            })
            .padding()
        }
        .hideNavigationbar()
        .onAppear {
            disableNowPlayingInfoCenter()
        }
        .onDisappear {
            enableNowPlayingInfoCenter()
        }
    }
    
    private func disableNowPlayingInfoCenter() {
        do {
            try AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
        } catch {
            print("Failed to disable Now Playing Info Center: \(error)")
        }
    }
    
    private func enableNowPlayingInfoCenter() {
        do {
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to enable Now Playing Info Center: \(error)")
        }
    }
}

struct CloseButtonWithbg : View {
    var onTap : ()->() = {}
    var body: some View {
        Image.close
            .font(.system(size: 12, weight: .bold))
            .makeCloseButtonBg()
            .onTap(completion: onTap)
    }
}

extension View {
    func makeCloseButtonBg() -> some View {
        self.frame(width: 35, height: 35)
            .foregroundColor(.text)
            .background(Color.black)
            .clipShape(Circle())
            .overlay(Circle().stroke(Color.text.opacity(0.2), lineWidth: 1))
    }
}
