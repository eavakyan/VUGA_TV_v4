//
//  ScrollViewExtension.swift
//  Vuga
//
//

import SwiftUI

// ScrollView extension to ensure proper initial positioning
extension View {
    /// Ensures the ScrollView starts at the top position when it appears
    func scrollToTopOnAppear() -> some View {
        ScrollViewReader { proxy in
            self
                .onAppear {
                    // Use a small delay to ensure the view is fully loaded
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                        withAnimation(.easeOut(duration: 0.1)) {
                            proxy.scrollTo("scrollTop", anchor: .top)
                        }
                    }
                }
                .overlay(
                    // Add an invisible marker at the top
                    Color.clear
                        .frame(height: 0)
                        .id("scrollTop"),
                    alignment: .top
                )
        }
    }
}

// Extension to handle safe area and initial scroll position
extension View {
    func safeAreaScrollView() -> some View {
        GeometryReader { geometry in
            ScrollView(showsIndicators: false) {
                VStack(spacing: 0) {
                    // Add safe area padding at the top
                    Color.clear
                        .frame(height: geometry.safeAreaInsets.top)
                        .id("safeTop")
                    
                    self
                }
            }
        }
    }
}