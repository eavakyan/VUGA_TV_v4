import SwiftUI

struct AgeSettingsView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @ObservedObject var viewModel: ProfileViewModel
    @Environment(\.presentationMode) var presentationMode
    
    let profile: Profile
    
    init(profile: Profile, viewModel: ProfileViewModel) {
        self.profile = profile
        self.viewModel = viewModel
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: {
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Image.back
                        .resizeFitTo(size: 24, renderingMode: .template)
                        .foregroundColor(.text)
                }
                
                Spacer()
                
                Text("Age Settings")
                    .outfitSemiBold(20)
                    .foregroundColor(.text)
                
                Spacer()
                
                // Save button
                Button("Save") {
                    saveAgeSettings()
                }
                .outfitMedium(16)
                .foregroundColor(.base)
            }
            .padding(.horizontal, 15)
            .padding(.top, 15)
            
            ScrollView {
                VStack(spacing: 20) {
                    // Profile info
                    VStack(spacing: 10) {
                        Circle()
                            .fill(Color(hexString: profile.avatarColor ?? "#FF5252"))
                            .frame(width: 80, height: 80)
                            .overlay(
                                Text(profile.initial)
                                    .outfitBold(24)
                                    .foregroundColor(.white)
                            )
                        
                        Text(profile.name)
                            .outfitSemiBold(18)
                            .foregroundColor(.text)
                    }
                    .padding(.top, 20)
                    
                    // Kids Profile Toggle
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Kids Profile")
                            .outfitSemiBold(16)
                            .foregroundColor(.text)
                        
                        Text("Kids profiles can only access content for ages 12 and under")
                            .outfitRegular(14)
                            .foregroundColor(.textLight)
                        
                        Toggle("", isOn: $viewModel.isKidsProfile)
                            .toggleStyle(SwitchToggleStyle(tint: .base))
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 15)
                    .background(Color.bg.opacity(0.3))
                    .cornerRadius(12)
                    
                    // Age Selection (disabled for kids profiles)
                    if !viewModel.isKidsProfile {
                        VStack(alignment: .leading, spacing: 15) {
                            Text("Age")
                                .outfitSemiBold(16)
                                .foregroundColor(.text)
                            
                            Text("Set the age to filter content appropriately")
                                .outfitRegular(14)
                                .foregroundColor(.textLight)
                            
                            // Age picker
                            HStack {
                                Text("Age:")
                                    .outfitMedium(14)
                                    .foregroundColor(.text)
                                
                                Picker("Age", selection: $viewModel.selectedAge) {
                                    Text("Not Set").tag(nil as Int?)
                                    ForEach(1...100, id: \.self) { age in
                                        Text("\(age)").tag(age as Int?)
                                    }
                                }
                                .pickerStyle(MenuPickerStyle())
                                .accentColor(.base)
                                
                                Spacer()
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 15)
                        .background(Color.bg.opacity(0.3))
                        .cornerRadius(12)
                    }
                    
                    // Age Ratings Info
                    if !viewModel.ageRatings.isEmpty {
                        VStack(alignment: .leading, spacing: 15) {
                            Text("Content Ratings")
                                .outfitSemiBold(16)
                                .foregroundColor(.text)
                            
                            ForEach(viewModel.ageRatings) { rating in
                                AgeRatingInfoRow(rating: rating, 
                                               isAccessible: canAccessRating(rating))
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 15)
                        .background(Color.bg.opacity(0.3))
                        .cornerRadius(12)
                    }
                    
                    Spacer(minLength: 50)
                }
                .padding(.horizontal, 15)
            }
        }
        .background(Color.darkBg)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.loadCurrentProfileSettings()
            if viewModel.ageRatings.isEmpty {
                viewModel.fetchAgeRatings()
            }
        }
        .loaderView(viewModel.isLoading)
    }
    
    private func canAccessRating(_ rating: AgeRating) -> Bool {
        if viewModel.isKidsProfile {
            return rating.isKidsFriendly
        }
        
        guard let selectedAge = viewModel.selectedAge else {
            return true // No age restriction if not set
        }
        
        return selectedAge >= rating.minAge
    }
    
    private func saveAgeSettings() {
        viewModel.updateAgeSettings(
            profileId: profile.profileId,
            age: viewModel.isKidsProfile ? nil : viewModel.selectedAge,
            isKidsProfile: viewModel.isKidsProfile
        )
    }
}

struct AgeRatingInfoRow: View {
    let rating: AgeRating
    let isAccessible: Bool
    
    var body: some View {
        HStack {
            // Rating badge
            Text(rating.code)
                .outfitBold(12)
                .foregroundColor(.white)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color(hexString: rating.displayColor))
                .cornerRadius(6)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(rating.name)
                    .outfitMedium(14)
                    .foregroundColor(isAccessible ? .text : .textLight)
                
                if let description = rating.description {
                    Text(description)
                        .outfitRegular(12)
                        .foregroundColor(.textLight)
                }
            }
            
            Spacer()
            
            if isAccessible {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(.green)
                    .font(.system(size: 16))
            } else {
                Image(systemName: "lock.circle.fill")
                    .foregroundColor(.red)
                    .font(.system(size: 16))
            }
        }
        .opacity(isAccessible ? 1.0 : 0.6)
    }
}

#Preview {
    // Create a sample profile using JSON decoding
    let profileJSON = """
    {
        "profile_id": 1,
        "app_user_id": 1,
        "name": "John",
        "avatar_type": "default",
        "avatar_color": "#FF6B6B",
        "avatar_id": 1,
        "is_kids": 0,
        "is_kids_profile": 0,
        "age": 25,
        "is_active": true
    }
    """
    
    let profileData = profileJSON.data(using: .utf8)!
    let sampleProfile = try! JSONDecoder().decode(Profile.self, from: profileData)
    
    AgeSettingsView(profile: sampleProfile, viewModel: ProfileViewModel())
}