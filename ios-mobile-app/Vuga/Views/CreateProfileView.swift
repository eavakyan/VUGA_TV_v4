import SwiftUI

struct CreateProfileView: View {
    @StateObject private var viewModel = CreateProfileViewModel()
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    @State private var profileName = ""
    @State private var selectedColor = "#FF5252"
    @State private var isKidsProfile = false
    @State private var selectedAvatarId = 1
    @State private var profileAge: Int? = nil
    @State private var showAgeInputDialog = false
    @State private var ageInputText = ""
    
    let profile: Profile?
    let onComplete: () -> Void
    
    // Use the same colors as defined in CreateProfileViewModel
    let avatarColors = [
        "#FF5252", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    ]
    
    init(profile: Profile? = nil, onComplete: @escaping () -> Void) {
        self.profile = profile
        self.onComplete = onComplete
    }
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                HStack {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 20))
                            .foregroundColor(.white)
                    }
                    
                    Spacer()
                    
                    Text(profile == nil ? "Create Profile" : "Edit Profile")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    // Placeholder for alignment
                    Image(systemName: "xmark")
                        .font(.system(size: 20))
                        .foregroundColor(.clear)
                }
                .padding()
                
                ScrollView {
                    VStack(spacing: 30) {
                        // Profile Preview - Show current avatar or initials
                        ZStack {
                            if let profile = profile, let avatarUrl = profile.avatarUrl, !avatarUrl.isEmpty {
                                // Show existing avatar image
                                AsyncImage(url: URL(string: avatarUrl)) { image in
                                    image
                                        .resizable()
                                        .aspectRatio(contentMode: .fill)
                                        .frame(width: 120, height: 120)
                                        .clipShape(Circle())
                                } placeholder: {
                                    // Fallback to initials while loading
                                    ZStack {
                                        Circle()
                                            .fill(Color(hexString: selectedColor))
                                            .frame(width: 120, height: 120)
                                        
                                        Text(profileName.isEmpty ? "P" : String(profileName.prefix(2)).uppercased())
                                            .font(.system(size: 48, weight: .bold))
                                            .foregroundColor(.white)
                                    }
                                }
                            } else {
                                // Show initials in colored circle
                                Circle()
                                    .fill(Color(hexString: selectedColor))
                                    .frame(width: 120, height: 120)
                                
                                Text(profileName.isEmpty ? "P" : String(profileName.prefix(2)).uppercased())
                                    .font(.system(size: 48, weight: .bold))
                                    .foregroundColor(.white)
                            }
                        }
                        .padding(.top, 20)
                        
                        // Profile Name
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Profile Name")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.white)
                            
                            TextField("", text: $profileName)
                                .placeholder(when: profileName.isEmpty) {
                                    Text("Enter profile name")
                                        .foregroundColor(Color.white.opacity(0.3))
                                }
                                .foregroundColor(.white)
                                .padding()
                                .background(Color.white.opacity(0.1))
                                .cornerRadius(8)
                        }
                        .padding(.horizontal)
                        
                        // Avatar Colors
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Choose Avatar Color")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.white)
                                .padding(.horizontal)
                            
                            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 5), spacing: 15) {
                                ForEach(avatarColors, id: \.self) { color in
                                    Circle()
                                        .fill(Color(hexString: color))
                                        .frame(width: 50, height: 50)
                                        .overlay(
                                            Circle()
                                                .stroke(Color.white, lineWidth: selectedColor == color ? 3 : 0)
                                        )
                                        .onTapGesture {
                                            selectedColor = color
                                            // Map color to avatar ID (1-based index, limited to 1-8 range)
                                            if let colorIndex = avatarColors.firstIndex(of: color) {
                                                selectedAvatarId = (colorIndex % 8) + 1
                                            }
                                        }
                                }
                            }
                            .padding(.horizontal)
                        }
                        
                        // Kids Profile Toggle
                        HStack {
                            Text("Kids Profile")
                                .font(.system(size: 16))
                                .foregroundColor(.white)
                            
                            Spacer()
                            
                            Toggle("", isOn: $isKidsProfile)
                                .labelsHidden()
                                .onChange(of: isKidsProfile) { newValue in
                                    if newValue && profile == nil {
                                        // Show age input dialog only for new profile creation
                                        showAgeInputDialog = true
                                        ageInputText = ""
                                    } else if !newValue {
                                        // Clear age when turning off kids profile
                                        profileAge = nil
                                    }
                                }
                        }
                        .padding(.horizontal)
                        
                        Text("Kids profiles only show content rated for children")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.5))
                            .padding(.horizontal)
                        
                        // Create/Update Button
                        Button(action: {
                            // Validate age for Kids Profile
                            if isKidsProfile && (profileAge == nil || profileAge! < 1 || profileAge! >= 18) {
                                viewModel.showError = true
                                viewModel.errorMessage = "Kids Profile requires age between 1 and 17"
                                return
                            }
                            
                            if profile == nil {
                                viewModel.createProfile(name: profileName, color: selectedColor, isKids: isKidsProfile, age: profileAge) {
                                    onComplete()
                                    presentationMode.wrappedValue.dismiss()
                                }
                            } else {
                                // When updating, always use color avatar type since user is selecting from color options
                                // This will replace any custom image with the selected color avatar
                                print("UpdateProfile: Saving - Name: \(profileName), Color: \(selectedColor), AvatarId: \(selectedAvatarId)")
                                viewModel.updateProfile(profileId: profile!.profileId, name: profileName, color: selectedColor, isKids: isKidsProfile, avatarId: selectedAvatarId, age: profileAge, avatarType: "color") {
                                    print("UpdateProfile: Success - Profile updated")
                                    onComplete()
                                    presentationMode.wrappedValue.dismiss()
                                }
                            }
                        }) {
                            Text(profile == nil ? "Create Profile" : "Update Profile")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.red)
                                .cornerRadius(25)
                        }
                        .padding(.horizontal)
                        .padding(.bottom, 40)
                        .disabled(profileName.isEmpty || viewModel.isLoading)
                    }
                }
            }
            
            if viewModel.isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .scaleEffect(1.5)
            }
        }
        .onAppear {
            if let profile = profile {
                print("CreateProfileView: Loading profile - Name: \(profile.name), Color: \(profile.avatarColor ?? "default"), AvatarId: \(profile.avatarId ?? 0)")
                profileName = profile.name
                selectedColor = profile.avatarColor ?? "#FF5252"  // Use default color if nil
                isKidsProfile = profile.isKids
                selectedAvatarId = profile.avatarId ?? 1
                profileAge = profile.age
                
                // If we don't have the exact color in our list, use the avatar ID to determine which color to select
                if !avatarColors.contains(selectedColor) && selectedAvatarId > 0 && selectedAvatarId <= 8 {
                    let colorIndex = (selectedAvatarId - 1) % avatarColors.count
                    if colorIndex >= 0 && colorIndex < avatarColors.count {
                        selectedColor = avatarColors[colorIndex]
                    }
                }
                print("CreateProfileView: After loading - profileName: \(profileName), selectedColor: \(selectedColor)")
            } else {
                print("CreateProfileView: No profile provided, creating new profile")
            }
        }
        .alert("Enter Age", isPresented: $showAgeInputDialog) {
            TextField("Age (1-17)", text: $ageInputText)
                .keyboardType(.numberPad)
            Button("OK") {
                if let age = Int(ageInputText.trimmingCharacters(in: .whitespacesAndNewlines)), age >= 1 && age < 18 {
                    profileAge = age
                    // Age is valid, kids profile remains enabled
                } else {
                    // Invalid age, turn off kids profile
                    isKidsProfile = false
                    profileAge = nil
                    viewModel.showError = true
                    viewModel.errorMessage = "Kids Profile age must be between 1 and 17"
                }
            }
            Button("Cancel", role: .cancel) {
                // Cancel age entry, turn off kids profile
                isKidsProfile = false
                profileAge = nil
            }
        } message: {
            Text("Kids Profile is for children under 18. Please enter the age:")
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

extension View {
    func placeholder<Content: View>(
        when shouldShow: Bool,
        alignment: Alignment = .leading,
        @ViewBuilder placeholder: () -> Content) -> some View {
        
        ZStack(alignment: alignment) {
            placeholder().opacity(shouldShow ? 1 : 0)
            self
        }
    }
}