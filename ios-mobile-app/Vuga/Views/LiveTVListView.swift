//
//  LiveTVListView.swift
//  Vuga
//
//  Created by Claude on Live TV List Implementation
//

import SwiftUI

struct LiveTVListView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    
    var body: some View {
        ZStack {
            VStack {
                // Header
                HStack {
                    Button(action: {
                        Navigation.pop()
                    }) {
                        Image.back
                            .resizeFitTo(size: 24, renderingMode: .template)
                            .foregroundColor(.text)
                    }
                    
                    Text("Live TV")
                        .outfitSemiBold(24)
                        .foregroundColor(.text)
                    
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.top, 10)
                
                Spacer()
                
                // Coming Soon Content
                VStack(spacing: 20) {
                    Image.liveTV
                        .resizeFitTo(size: 80, renderingMode: .template)
                        .foregroundColor(.base)
                        .opacity(0.7)
                    
                    Text("Coming Soon")
                        .outfitSemiBold(28)
                        .foregroundColor(.text)
                    
                    Text("Live TV channels will be available here soon.\nStay tuned for updates!")
                        .outfitRegular(16)
                        .foregroundColor(.textLight)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 40)
                    
                    // Optional: Add notification button
                    Button(action: {
                        // Handle notification signup
                    }) {
                        HStack {
                            Image(systemName: "bell")
                                .font(.system(size: 16))
                            Text("Notify Me")
                                .outfitMedium(16)
                        }
                        .foregroundColor(.bg)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.base)
                        .cornerRadius(25)
                    }
                    .padding(.top, 20)
                }
                
                Spacer()
                Spacer() // Extra spacer to center content better
            }
            .padding(.bottom, 80) // Space for tab bar
        }
        .addBackground()
        .hideNavigationbar()
    }
}