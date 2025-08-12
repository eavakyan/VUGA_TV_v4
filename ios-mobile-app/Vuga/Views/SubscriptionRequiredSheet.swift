//
//  SubscriptionRequiredSheet.swift
//  Vuga
//
//  Created by Assistant on today's date.
//

import SwiftUI
import Kingfisher

struct SubscriptionRequiredSheet: View {
    @Binding var isPresented: Bool
    let content: VugaContent
    let distributorName: String
    let distributorId: Int?
    let onSubscribe: () -> Void
    
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    var body: some View {
        VStack(spacing: 0) {
            // Handle
            RoundedRectangle(cornerRadius: 3)
                .fill(Color.gray.opacity(0.5))
                .frame(width: 40, height: 5)
                .padding(.top, 10)
            
            // Content Poster and Info
            VStack(spacing: 16) {
                // Poster
                KFImage(content.horizontalPoster?.addBaseURL())
                    .resizable()
                    .aspectRatio(16/9, contentMode: .fill)
                    .frame(height: 180)
                    .clipped()
                    .cornerRadius(12)
                    .overlay(
                        LinearGradient(
                            colors: [.clear, .black.opacity(0.6)],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    )
                
                // Title and Info
                VStack(spacing: 8) {
                    Text(content.title ?? "")
                        .outfitSemiBold(20)
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                    
                    HStack(spacing: 12) {
                        if let year = content.releaseYear {
                            Text("\(year)")
                                .outfitLight(14)
                                .foregroundColor(.textLight)
                        }
                        
                        if let rating = content.ratings {
                            HStack(spacing: 4) {
                                Image.star
                                    .resizeFitTo(size: 12)
                                    .foregroundColor(.rating)
                                Text(String(format: "%.1f", rating))
                                    .outfitLight(14)
                                    .foregroundColor(.textLight)
                            }
                        }
                        
                        if let type = content.type {
                            Text(type.title.localized(language))
                                .outfitLight(14)
                                .foregroundColor(.textLight)
                        }
                    }
                }
                
                // Lock Icon and Message
                VStack(spacing: 16) {
                    Image(systemName: "lock.fill")
                        .font(.system(size: 48))
                        .foregroundColor(.base)
                    
                    Text("Subscription Required")
                        .outfitSemiBold(22)
                        .foregroundColor(.white)
                    
                    Text("This content is available exclusively through \(distributorName)")
                        .outfitRegular(16)
                        .foregroundColor(.textLight)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                    
                    // Features
                    VStack(alignment: .leading, spacing: 12) {
                        FeatureRow(icon: "checkmark.circle.fill", text: "Unlimited access to \(distributorName) content")
                        FeatureRow(icon: "play.rectangle.fill", text: "Watch on all your devices")
                        FeatureRow(icon: "arrow.down.circle.fill", text: "Download for offline viewing")
                    }
                    .padding(.horizontal, 32)
                    .padding(.vertical, 16)
                }
                
                // Action Buttons
                VStack(spacing: 12) {
                    // Subscribe Button
                    Button(action: onSubscribe) {
                        Text("View Subscription Options")
                            .outfitSemiBold(16)
                            .foregroundColor(.black)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.base)
                            .cornerRadius(25)
                    }
                    
                    // Cancel Button
                    Button(action: { isPresented = false }) {
                        Text("Maybe Later")
                            .outfitMedium(16)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.gray.opacity(0.2))
                            .overlay(
                                RoundedRectangle(cornerRadius: 25)
                                    .stroke(Color.white.opacity(0.3), lineWidth: 1)
                            )
                            .cornerRadius(25)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
            .padding(.top, 16)
        }
        .background(Color("bgColor"))
        .cornerRadius(20, corners: [.topLeft, .topRight])
    }
}

struct FeatureRow: View {
    let icon: String
    let text: String
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(.base)
                .frame(width: 24)
            
            Text(text)
                .outfitRegular(14)
                .foregroundColor(.white)
                .lineLimit(2)
            
            Spacer()
        }
    }
}