//
//  AgeRatingBadgeView.swift
//  Vuga
//
//  Created for improved age rating display
//

import SwiftUI

struct AgeRatingBadgeView: View {
    let ageRatingCode: String
    let minAge: Int?
    @State private var showDetails = false
    
    // Convert technical codes to user-friendly display
    private var displayText: String {
        switch ageRatingCode {
        case "AG_0_6":
            return "All Ages"
        case "AG_7_12":
            return "7+"
        case "AG_13_16":
            return "13+"
        case "AG_17_18":
            return "17+"
        case "AG_18_PLUS", "NC-17":
            return "18+"
        case "G":
            return "G"
        case "PG":
            return "PG"
        case "PG-13":
            return "PG-13"
        case "R":
            return "R"
        case "NR":
            return "Not Rated"
        default:
            return ageRatingCode
        }
    }
    
    // Get appropriate color for rating
    private var badgeColor: Color {
        switch ageRatingCode {
        case "AG_0_6", "G":
            return Color(hexString: "#4CAF50") // Green
        case "AG_7_12", "PG":
            return Color(hexString: "#8BC34A") // Light Green
        case "AG_13_16", "PG-13":
            return Color(hexString: "#FF9800") // Orange
        case "AG_17_18", "R":
            return Color(hexString: "#F44336") // Red
        case "AG_18_PLUS", "NC-17":
            return Color(hexString: "#9C27B0") // Purple
        default:
            return Color.gray
        }
    }
    
    // Get appropriate icon for rating
    private var ratingIcon: String {
        switch ageRatingCode {
        case "AG_0_6", "G":
            return "person.3.fill" // Family icon
        case "AG_7_12", "PG":
            return "person.2.fill" // Kids icon
        case "AG_13_16", "PG-13":
            return "exclamationmark.triangle.fill" // Teen warning
        case "AG_17_18", "R":
            return "17.circle.fill" // 17+ icon
        case "AG_18_PLUS", "NC-17":
            return "18.circle.fill" // 18+ icon
        default:
            return "questionmark.circle.fill"
        }
    }
    
    // Get detailed description for rating
    private var ratingDescription: String {
        switch ageRatingCode {
        case "AG_0_6", "G":
            return "Suitable for all ages. No content that would be inappropriate for children."
        case "AG_7_12", "PG":
            return "Parental guidance suggested. Some material may not be suitable for young children."
        case "AG_13_16", "PG-13":
            return "Parents strongly cautioned. Some material may be inappropriate for children under 13."
        case "AG_17_18", "R":
            return "Restricted. Under 17 requires accompanying parent or adult guardian."
        case "AG_18_PLUS", "NC-17":
            return "Adults only. No one 17 and under admitted."
        default:
            return "Content has not been rated."
        }
    }
    
    var body: some View {
        Button(action: {
            withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                showDetails.toggle()
            }
        }) {
            HStack(spacing: 4) {
                Image(systemName: ratingIcon)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.white)
                
                Text(displayText)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.white)
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(badgeColor)
            .cornerRadius(6)
            .overlay(
                RoundedRectangle(cornerRadius: 6)
                    .stroke(Color.white.opacity(0.2), lineWidth: 1)
            )
            .shadow(color: badgeColor.opacity(0.4), radius: 4, x: 0, y: 2)
        }
        .sheet(isPresented: $showDetails) {
            AgeRatingDetailSheet(
                ageRatingCode: ageRatingCode,
                displayText: displayText,
                badgeColor: badgeColor,
                ratingIcon: ratingIcon,
                ratingDescription: ratingDescription
            )
        }
    }
}

// Detailed information sheet
struct AgeRatingDetailSheet: View {
    let ageRatingCode: String
    let displayText: String
    let badgeColor: Color
    let ratingIcon: String
    let ratingDescription: String
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                // Large badge display
                VStack(spacing: 16) {
                    Image(systemName: ratingIcon)
                        .font(.system(size: 60, weight: .semibold))
                        .foregroundColor(badgeColor)
                    
                    Text(displayText)
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.white)
                    
                    Text("Content Rating")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.gray)
                }
                .padding(.top, 20)
                
                // Description
                Text(ratingDescription)
                    .font(.system(size: 16))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 20)
                
                // Content warnings (placeholder for future implementation)
                VStack(alignment: .leading, spacing: 12) {
                    Text("Content May Include:")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.gray)
                    
                    // These would be populated from content_rating_reasons
                    if ageRatingCode == "AG_18_PLUS" || ageRatingCode == "R" {
                        ContentWarningRow(icon: "exclamationmark.triangle", text: "Strong violence")
                        ContentWarningRow(icon: "speaker.wave.3", text: "Strong language")
                        ContentWarningRow(icon: "person.2", text: "Adult themes")
                    } else if ageRatingCode == "AG_13_16" || ageRatingCode == "PG-13" {
                        ContentWarningRow(icon: "exclamationmark.triangle", text: "Some violence")
                        ContentWarningRow(icon: "speaker.wave.2", text: "Mild language")
                    } else if ageRatingCode == "AG_7_12" || ageRatingCode == "PG" {
                        ContentWarningRow(icon: "info.circle", text: "Mild themes")
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
                .background(Color.white.opacity(0.05))
                .cornerRadius(12)
                .padding(.horizontal, 20)
                
                Spacer()
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color("bgColor"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        presentationMode.wrappedValue.dismiss()
                    }
                    .foregroundColor(.white)
                }
            }
        }
    }
}

// Content warning row component
struct ContentWarningRow: View {
    let icon: String
    let text: String
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(.orange)
                .frame(width: 20)
            
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(.white)
            
            Spacer()
        }
    }
}

// Compact version for inline display
struct AgeRatingBadgeCompact: View {
    let ageRatingCode: String
    
    private var displayText: String {
        switch ageRatingCode {
        case "AG_0_6": return "All"
        case "AG_7_12": return "7+"
        case "AG_13_16": return "13+"
        case "AG_17_18": return "17+"
        case "AG_18_PLUS": return "18+"
        case "NR": return "NR"
        default: return ageRatingCode
        }
    }
    
    private var badgeColor: Color {
        switch ageRatingCode {
        case "AG_0_6", "G": return Color(hexString: "#4CAF50")
        case "AG_7_12", "PG": return Color(hexString: "#8BC34A")
        case "AG_13_16", "PG-13": return Color(hexString: "#FF9800")
        case "AG_17_18", "R": return Color(hexString: "#F44336")
        case "AG_18_PLUS", "NC-17": return Color(hexString: "#9C27B0")
        default: return Color.gray
        }
    }
    
    var body: some View {
        Text(displayText)
            .font(.system(size: 11, weight: .bold))
            .foregroundColor(.white)
            .padding(.horizontal, 6)
            .padding(.vertical, 3)
            .background(badgeColor)
            .cornerRadius(4)
    }
}

struct AgeRatingBadgeView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            AgeRatingBadgeView(ageRatingCode: "AG_0_6", minAge: nil)
            AgeRatingBadgeView(ageRatingCode: "AG_7_12", minAge: 7)
            AgeRatingBadgeView(ageRatingCode: "AG_13_16", minAge: 13)
            AgeRatingBadgeView(ageRatingCode: "AG_17_18", minAge: 17)
            AgeRatingBadgeView(ageRatingCode: "AG_18_PLUS", minAge: 18)
        }
        .padding()
        .background(Color.black)
    }
}