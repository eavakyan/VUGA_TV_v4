//
//  RatingView.swift
//  Vuga
//
//

import SwiftUI

struct RatingView: View {
    @Binding var rating: Double
    let maxRating: Int = 5
    let onRatingChanged: ((Double) -> Void)?
    
    @State private var dragRating: Double? = nil
    
    var displayRating: Double {
        dragRating ?? rating
    }
    
    init(rating: Binding<Double>, onRatingChanged: ((Double) -> Void)? = nil) {
        self._rating = rating
        self.onRatingChanged = onRatingChanged
    }
    
    var body: some View {
        HStack(spacing: 2) {
            ForEach(1...maxRating, id: \.self) { index in
                Image(systemName: "star.fill")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .foregroundColor(starColor(for: index))
                    .frame(width: 24, height: 24)
                    .onTapGesture {
                        updateRating(Double(index))
                    }
            }
        }
        .gesture(
            DragGesture()
                .onChanged { value in
                    let width = CGFloat(maxRating) * 26
                    let x = max(0, min(value.location.x, width))
                    let newRating = (x / width) * Double(maxRating)
                    dragRating = max(0.5, min(Double(maxRating), round(newRating * 2) / 2))
                }
                .onEnded { _ in
                    if let dragRating = dragRating {
                        updateRating(dragRating)
                    }
                    dragRating = nil
                }
        )
    }
    
    private func starColor(for index: Int) -> Color {
        let filledCount = Int(displayRating)
        let hasHalfStar = displayRating.truncatingRemainder(dividingBy: 1) >= 0.5
        
        if index <= filledCount {
            return .rating
        } else if index == filledCount + 1 && hasHalfStar {
            return .rating.opacity(0.5)
        } else {
            return .gray.opacity(0.3)
        }
    }
    
    private func updateRating(_ newRating: Double) {
        rating = newRating
        onRatingChanged?(newRating * 2) // Convert to 10-point scale
        Function.shared.haptic()
    }
}

struct RatingDisplayView: View {
    let rating: Double
    let userRating: Double?
    let onTap: (() -> Void)?
    
    var body: some View {
        HStack(spacing: 8) {
            // Average rating
            HStack(spacing: 4) {
                Image(systemName: "star.fill")
                    .resizable()
                    .frame(width: 16, height: 16)
                    .foregroundColor(.rating)
                Text(String(format: "%.1f", rating))
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
            }
            
            // User rating
            if let userRating = userRating {
                Text("•")
                    .foregroundColor(.gray)
                HStack(spacing: 4) {
                    Image(systemName: "person.fill")
                        .resizable()
                        .frame(width: 12, height: 12)
                        .foregroundColor(.primary)
                    Text(String(format: "%.1f", userRating))
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.primary)
                }
            } else {
                Text("•")
                    .foregroundColor(.gray)
                Text("Rate")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.primary)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(Color.black.opacity(0.3))
        .cornerRadius(20)
        .onTapGesture {
            onTap?()
        }
    }
}

struct RatingBottomSheet: View {
    @Binding var isPresented: Bool
    @Binding var currentRating: Double
    let contentTitle: String
    let contentType: ContentType
    let isEpisode: Bool
    let onSubmit: (Double) -> Void
    
    @State private var tempRating: Double = 0
    
    var body: some View {
        VStack(spacing: 20) {
            // Handle
            RoundedRectangle(cornerRadius: 3)
                .fill(Color.gray.opacity(0.5))
                .frame(width: 40, height: 5)
                .padding(.top, 10)
            
            // Title
            Text("Rate \(isEpisode ? "Episode" : contentType == .movie ? "Movie" : "TV Show")")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
            
            // Content Title
            Text(contentTitle)
                .font(.system(size: 16))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .lineLimit(2)
                .padding(.horizontal, 20)
            
            // Rating Stars
            RatingView(rating: $tempRating) { _ in }
                .padding(.vertical, 10)
            
            // Rating Value
            Text(String(format: "%.1f / 10", tempRating * 2))
                .font(.system(size: 24, weight: .medium))
                .foregroundColor(.primary)
            
            // Buttons
            HStack(spacing: 20) {
                Button(action: {
                    isPresented = false
                }) {
                    Text("Cancel")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.gray.opacity(0.3))
                        .cornerRadius(10)
                }
                
                Button(action: {
                    onSubmit(tempRating * 2)
                    isPresented = false
                }) {
                    Text("Submit")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.black)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.primary)
                        .cornerRadius(10)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 20)
        }
        .background(Color.bg)
        .cornerRadius(20, corners: [.topLeft, .topRight])
        .onAppear {
            tempRating = currentRating / 2
        }
    }
}