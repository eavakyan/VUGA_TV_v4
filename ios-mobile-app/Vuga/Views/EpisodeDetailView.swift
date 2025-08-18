//
//  EpisodeDetailView.swift
//  Vuga
//
//  TV Show Episode Detail View
//

import SwiftUI
import Kingfisher

struct EpisodeDetailView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.myUser) var myUser: User? = nil
    @Environment(\.presentationMode) var presentationMode
    
    let episode: Episode
    let seriesContent: VugaContent?
    
    @State private var showRatingSheet = false
    @State private var currentUserRating: Double = 0
    @State private var showLoginAlert = false
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 0) {
                    headerView
                    episodeThumbnail
                    episodeInfo
                    actionButtons
                    descriptionView
                    moreEpisodesView
                }
            }
        }
        .hideNavigationbar()
        .sheet(isPresented: $showRatingSheet) {
            ratingSheet
        }
        .alert(isPresented: $showLoginAlert) {
            Alert(
                title: Text("Login Required"),
                message: Text("Please login to rate this episode"),
                dismissButton: .default(Text("OK"))
            )
        }
    }
    
    private var headerView: some View {
        HStack {
            Button(action: {
                presentationMode.wrappedValue.dismiss()
            }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .background(Color.white.opacity(0.1))
                    .clipShape(Circle())
            }
            
            Spacer()
            
            VStack(spacing: 2) {
                Text(seriesContent?.title ?? "")
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.7))
                Text("Episode \(episode.number ?? 0)")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
            }
            
            Spacer()
            
            Button(action: {}) {
                Image(systemName: "square.and.arrow.up")
                    .font(.system(size: 18))
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .background(Color.white.opacity(0.1))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
    }
    
    private var episodeThumbnail: some View {
        ZStack {
            if let thumbnailUrl = episode.thumbnail?.addBaseURL(),
               let url = URL(string: thumbnailUrl) {
                KFImage(url)
                    .resizable()
                    .aspectRatio(16/9, contentMode: .fit)
                    .frame(maxWidth: .infinity)
                    .clipped()
            }
            
            Button(action: playEpisode) {
                Image(systemName: "play.circle.fill")
                    .font(.system(size: 60))
                    .foregroundColor(.white)
                    .background(Circle().fill(Color.black.opacity(0.3)))
            }
        }
    }
    
    private var episodeInfo: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(episode.title ?? "Episode \(episode.number ?? 0)")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            HStack(spacing: 16) {
                // Rating
                HStack(spacing: 4) {
                    Image(systemName: "star.fill")
                        .font(.system(size: 14))
                        .foregroundColor(.yellow)
                    Text(String(format: "%.1f", episode.ratings ?? 0))
                        .font(.system(size: 14))
                        .foregroundColor(.white)
                }
                
                // Duration
                if let duration = episode.duration {
                    Text(duration)
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                }
                
                // Views
                HStack(spacing: 4) {
                    Image(systemName: "eye")
                        .font(.system(size: 14))
                    Text("\(episode.totalView ?? 0)")
                        .font(.system(size: 14))
                }
                .foregroundColor(.white.opacity(0.7))
                
                Spacer()
            }
        }
        .padding(.horizontal)
        .padding(.top, 16)
    }
    
    private var actionButtons: some View {
        VStack(spacing: 12) {
            Button(action: playEpisode) {
                HStack {
                    Image(systemName: "play.fill")
                        .font(.system(size: 18))
                    Text("Play Episode")
                        .font(.system(size: 16, weight: .semibold))
                }
                .foregroundColor(.black)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(Color.white)
                .cornerRadius(8)
            }
            
            HStack(spacing: 12) {
                Button(action: {
                    if myUser != nil {
                        currentUserRating = episode.userRating ?? 0
                        showRatingSheet = true
                    } else {
                        showLoginAlert = true
                    }
                }) {
                    HStack {
                        Image(systemName: episode.userRating != nil ? "star.fill" : "star")
                            .font(.system(size: 16))
                        Text(episode.userRating != nil ? "Rated" : "Rate")
                            .font(.system(size: 14, weight: .medium))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.white.opacity(0.15))
                    .cornerRadius(8)
                }
                
                if episode.sources?.first?.isDownload == 1 {
                    Button(action: {}) {
                        HStack {
                            Image(systemName: "arrow.down.circle")
                                .font(.system(size: 16))
                            Text("Download")
                                .font(.system(size: 14, weight: .medium))
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.white.opacity(0.15))
                        .cornerRadius(8)
                    }
                }
            }
        }
        .padding(.horizontal)
        .padding(.top, 16)
    }
    
    private var descriptionView: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Synopsis")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)
            
            Text(episode.description ?? "No description available")
                .font(.system(size: 14))
                .foregroundColor(.white.opacity(0.8))
                .lineLimit(nil)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(.horizontal)
        .padding(.top, 24)
    }
    
    private var moreEpisodesView: some View {
        Group {
            if let seasons = seriesContent?.seasons,
               let currentSeason = seasons.first(where: { $0.id == episode.seasonID }),
               let episodes = currentSeason.episodes,
               episodes.count > 1 {
                VStack(alignment: .leading, spacing: 12) {
                    Text("More Episodes")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .padding(.horizontal)
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(episodes.filter { $0.id != episode.id }, id: \.id) { ep in
                                VStack(alignment: .leading, spacing: 6) {
                                    if let thumbnailUrl = ep.thumbnail?.addBaseURL(),
                                       let url = URL(string: thumbnailUrl) {
                                        KFImage(url)
                                            .resizable()
                                            .aspectRatio(16/9, contentMode: .fill)
                                            .frame(width: 150, height: 84)
                                            .clipped()
                                            .cornerRadius(8)
                                    }
                                    
                                    Text("Episode \(ep.number ?? 0)")
                                        .font(.system(size: 12, weight: .medium))
                                        .foregroundColor(.white)
                                    
                                    if let duration = ep.duration {
                                        Text(duration)
                                            .font(.system(size: 10))
                                            .foregroundColor(.white.opacity(0.6))
                                    }
                                }
                                .onTapGesture {
                                    // Navigate to this episode
                                    Navigation.pushToSwiftUiView(
                                        EpisodeDetailView(
                                            episode: ep,
                                            seriesContent: seriesContent
                                        )
                                    )
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.top, 24)
            }
        }
    }
    
    private var ratingSheet: some View {
        RatingBottomSheet(
            isPresented: $showRatingSheet,
            currentRating: $currentUserRating,
            contentTitle: episode.title ?? "Episode \(episode.number ?? 0)",
            contentType: .movie,  // Episodes use movie type for rating
            isEpisode: true,
            onSubmit: { rating in
                // Submit rating
                showRatingSheet = false
            }
        )
    }
    
    private func playEpisode() {
        // Check if there are sources available
        if let sources = episode.sources, !sources.isEmpty {
            let firstSource = sources[0]
            
            // Navigate to video player
            Navigation.pushToSwiftUiView(
                VideoPlayerView(
                    content: nil,
                    episode: episode,
                    type: firstSource.type?.rawValue ?? 2,
                    isShowAdView: false,
                    url: firstSource.sourceURL ?? "",
                    progress: 0,
                    sourceId: firstSource.id
                )
            )
        }
    }
}