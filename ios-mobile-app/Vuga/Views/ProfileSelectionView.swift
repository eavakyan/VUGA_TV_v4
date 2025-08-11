import SwiftUI

struct ProfileSelectionView: View {
    @StateObject private var viewModel = ProfileSelectionViewModel()
    @EnvironmentObject var sessionManager: SessionManager
    @Environment(\.dismiss) var dismiss
    @State private var showCreateProfile = false
    @State private var isEditMode = false
    @State private var selectedProfile: Profile?
    var onProfileSelected: (() -> Void)?
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                Text("Who's watching?")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.top, 60)
                    .padding(.bottom, 40)
                
                // Profiles Grid
                ScrollView {
                    LazyVGrid(columns: [
                        GridItem(.flexible()),
                        GridItem(.flexible())
                    ], spacing: 20) {
                        ForEach(viewModel.profiles, id: \.profileId) { profile in
                            ProfileItem(profile: profile, isEditMode: isEditMode) {
                                print("ProfileSelectionView: Profile tapped - \(profile.name)")
                                if isEditMode {
                                    selectedProfile = profile
                                    showCreateProfile = true
                                } else {
                                    viewModel.selectProfile(profile)
                                }
                            } onDelete: {
                                if viewModel.profiles.count > 1 {
                                    viewModel.deleteProfile(profile)
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .top)
                        }
                        
                        // Add Profile button
                        if viewModel.profiles.count < 4 && !isEditMode {
                            AddProfileButton {
                                selectedProfile = nil
                                showCreateProfile = true
                            }
                            .frame(maxWidth: .infinity, alignment: .top)
                        }
                    }
                    .padding(.horizontal, 40)
                }
                
                Spacer()
                
                // Bottom buttons
                HStack(spacing: 10) {
                    Button(action: {
                        isEditMode.toggle()
                    }) {
                        Text(isEditMode ? "Done" : "Manage Profiles")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.white)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 25)
                                    .stroke(Color.white.opacity(0.3), lineWidth: 1)
                            )
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 40)
            }
            
            if viewModel.isLoading {
                LoadingOverlayView(title: "Loading profiles...")
            }
        }
        .onAppear {
            viewModel.loadProfiles()
        }
        .sheet(isPresented: $showCreateProfile) {
            CreateProfileView(profile: selectedProfile) {
                viewModel.loadProfiles()
            }
        }
        .onChange(of: viewModel.selectedProfile) { profile in
            if profile != nil {
                // Notify that a profile was selected
                onProfileSelected?()
                // Dismiss the view if presented as a sheet
                dismiss()
            }
        }
        .alert(isPresented: $viewModel.showError) {
            Alert(
                title: Text("Error"),
                message: Text(viewModel.errorMessage),
                dismissButton: .default(Text("OK"))
            )
        }
    }
}

struct LoadingOverlayView: View {
    var title: String
    var body: some View {
        ZStack {
            Color.black.opacity(0.6).ignoresSafeArea()
            VStack(spacing: 12) {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .scaleEffect(1.4)
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white.opacity(0.9))
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
            .background(Color.black.opacity(0.7))
            .cornerRadius(12)
            .shadow(radius: 8)
        }
        .allowsHitTesting(true)
    }
}

struct ProfileItem: View {
    let profile: Profile
    let isEditMode: Bool
    let onTap: () -> Void
    let onDelete: () -> Void
    
    var body: some View {
        VStack(alignment: .center, spacing: 10) {
            // Content starts at top, no spacer needed
            ZStack(alignment: .topTrailing) {
                // Profile Avatar
                if profile.avatarType == "default" || profile.avatarType == "color" {
                    // Color avatar
                    ZStack {
                        Circle()
                            .fill(Color(profile.color))
                            .frame(width: 100, height: 100)
                        
                        Text(profile.initial)
                            .font(.system(size: 40, weight: .bold))
                            .foregroundColor(.white)
                    }
                } else if profile.avatarType == "custom", let avatarUrl = profile.avatarUrl, !avatarUrl.isEmpty {
                    // Custom image avatar
                    AsyncImage(url: URL(string: avatarUrl)) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(width: 100, height: 100)
                            .clipShape(Circle())
                    } placeholder: {
                        // Fallback to color avatar while loading
                        ZStack {
                            Circle()
                                .fill(Color(profile.color))
                                .frame(width: 100, height: 100)
                            
                            Text(profile.initial)
                                .font(.system(size: 40, weight: .bold))
                                .foregroundColor(.white)
                        }
                    }
                } else {
                    // Fallback to color avatar
                    ZStack {
                        Circle()
                            .fill(Color(profile.color))
                            .frame(width: 100, height: 100)
                        
                        Text(profile.initial)
                            .font(.system(size: 40, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                
                // Delete button
                if isEditMode {
                    Button(action: onDelete) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.red)
                            .background(Circle().fill(Color.black))
                    }
                    .offset(x: 10, y: -10)
                }
            }
            
            Text(profile.name)
                .font(.system(size: 16))
                .foregroundColor(.white)
                .lineLimit(1)
            
            if profile.isKids {
                Text("KIDS")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.yellow)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(Capsule().fill(Color.yellow.opacity(0.2)))
            }
        }
        .onTapGesture {
            onTap()
        }
    }
}

struct AddProfileButton: View {
    let onTap: () -> Void
    
    var body: some View {
        VStack(spacing: 10) {
            ZStack {
                Circle()
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 100, height: 100)
                
                Image(systemName: "plus")
                    .font(.system(size: 40))
                    .foregroundColor(.white.opacity(0.7))
            }
            
            Text("Add Profile")
                .font(.system(size: 16))
                .foregroundColor(.white.opacity(0.7))
        }
        .onTapGesture {
            onTap()
        }
    }
}