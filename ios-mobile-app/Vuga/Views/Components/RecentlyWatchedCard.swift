//
//  RecentlyWatchedCard.swift
//  Vuga
//
//  Card view for Recently Watched content fetched from API
//

import SwiftUI
import Kingfisher

struct RecentlyWatchedCard: View {
    let content: RecentlyWatchedContent
    let onDelete: () -> Void
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    private var posterUrl: String? {
        if content.contentType == ContentType.movie.rawValue {
            return content.horizontalPoster
        } else if let episodeThumbnail = content.episodeInfo?.episodeThumbnail {
            return episodeThumbnail
        } else {
            return content.horizontalPoster
        }
    }
    
    private var displayTitle: String {
        content.contentName
    }
    
    private var episodeTitle: String? {
        content.episodeInfo?.episodeTitle
    }
    
    private var progressFraction: Double {
        guard content.totalDuration > 0 else { return 0 }
        return min(1.0, max(0.0, content.progress / content.totalDuration))
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            ZStack(alignment: .center) {
                // Poster image
                KFImage(posterUrl?.addBaseURL())
                    .resizeFillTo(width: 170, height: 105, radius: 10)
                
                // Play button
                Image(systemName: "play.fill")
                    .rotationEffect(.degrees(language == .Arabic ? 180 : 0))
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(.white)
                    .padding(8)
                    .padding(.leading, 2)
                    .background(.white.opacity(0.2))
                    .clipShape(Circle())
                
                // Close button
                VStack {
                    HStack {
                        Spacer()
                        Image.close
                            .resizeFitTo(size: 12, renderingMode: .template)
                            .foregroundStyle(.white)
                            .padding(12)
                            .onTapGesture {
                                onDelete()
                            }
                    }
                    Spacer()
                }
                
                // Progress bar
                VStack {
                    Spacer()
                    ProgressView(value: progressFraction)
                        .progressViewStyle(LinearProgressViewStyle())
                        .tint(Color("baseColor"))
                }
            }
            .cornerRadius(radius: 15)
            .addStroke(radius: 15)
            
            // Metadata
            metadataView
                .frame(width: 170, alignment: .topLeading)
        }
        .frame(width: 170)
    }
    
    @ViewBuilder
    private var metadataView: some View {
        if content.contentType == ContentType.movie.rawValue {
            // Movie metadata
            VStack(alignment: .leading, spacing: 2) {
                if let year = content.releaseYear {
                    Text("\(year)")
                        .outfitLight(14)
                        .foregroundColor(.textLight)
                        .lineLimit(1)
                }
                Text(displayTitle)
                    .lineLimit(2)
                    .outfitMedium(16)
                    .foregroundColor(.white)
                    .padding(.top, 1)
            }
        } else {
            // Series metadata
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    if let episodeTitle = episodeTitle {
                        Text(episodeTitle)
                            .outfitMedium(14)
                            .foregroundColor(.white)
                            .lineLimit(1)
                    }
                    
                    Text("• \(formattedDate)")
                        .outfitLight(13)
                        .foregroundColor(.textLight)
                        .lineLimit(1)
                }
                Text(displayTitle)
                    .outfitMedium(16)
                    .foregroundColor(.white)
                    .lineLimit(2)
            }
        }
    }
    
    private var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: content.watchedDate)
    }
}