//
//  NetworkContentView.swift
//  Vuga
//
//  Created by Claude on Network Content Implementation
//

import SwiftUI
import Kingfisher

struct NetworkContentView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject private var vm = NetworkContentViewModel()
    let networkName: String
    
    let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10)
    ]
    
    var body: some View {
        ZStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Header with Network Logo/Name
                    VStack(spacing: 16) {
                        HStack {
                            Button(action: {
                                Navigation.pop()
                            }) {
                                Image.back
                                    .resizeFitTo(size: 24, renderingMode: .template)
                                    .foregroundColor(.text)
                            }
                            
                            Spacer()
                        }
                        
                        // Network branding
                        VStack(spacing: 10) {
                            // Network logo placeholder
                            RoundedRectangle(cornerRadius: 12)
                                .fill(networkName == "HBO" ? Color.purple : Color.blue)
                                .frame(width: 80, height: 80)
                                .overlay(
                                    Text(networkName)
                                        .outfitBold(20)
                                        .foregroundColor(.white)
                                )
                            
                            Text(networkName)
                                .outfitSemiBold(28)
                                .foregroundColor(.text)
                            
                            Text(networkDescription)
                                .outfitRegular(14)
                                .foregroundColor(.textLight)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 40)
                        }
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)
                    
                    // Content sections
                    if vm.isLoading && vm.networkContent.isEmpty {
                        // Loading state
                        VStack(alignment: .leading, spacing: 20) {
                            ForEach(["Featured", "Popular", "New Releases"], id: \.self) { section in
                                VStack(alignment: .leading, spacing: 10) {
                                    Text(section)
                                        .outfitSemiBold(20)
                                        .foregroundColor(.text)
                                        .padding(.horizontal)
                                    
                                    ScrollView(.horizontal, showsIndicators: false) {
                                        HStack(spacing: 10) {
                                            ForEach(0..<5, id: \.self) { _ in
                                                RoundedRectangle(cornerRadius: 10)
                                                    .fill(Color.searchBg)
                                                    .frame(width: 120, height: 180)
                                                    .shimmer()
                                            }
                                        }
                                        .padding(.horizontal)
                                    }
                                }
                            }
                        }
                    } else if vm.networkContent.isEmpty {
                        // Empty state
                        VStack(spacing: 20) {
                            Image.noData
                                .resizeFitTo(size: 100)
                                .opacity(0.5)
                            Text("No Content Available")
                                .outfitRegular(18)
                                .foregroundColor(.textLight)
                            Text("Check back later for \(networkName) content")
                                .outfitRegular(14)
                                .foregroundColor(.textLight.opacity(0.7))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 50)
                    } else {
                        // Content sections
                        VStack(alignment: .leading, spacing: 30) {
                            // Featured content
                            if let featured = vm.featuredContent, !featured.isEmpty {
                                ContentSection(title: "Featured on \(networkName)", contents: featured)
                            }
                            
                            // All content grid
                            VStack(alignment: .leading, spacing: 10) {
                                Text("All \(networkName) Content")
                                    .outfitSemiBold(20)
                                    .foregroundColor(.text)
                                    .padding(.horizontal)
                                
                                LazyVGrid(columns: columns, spacing: 15) {
                                    ForEach(vm.networkContent, id: \.id) { content in
                                        NetworkContentGridItem(content: content)
                                    }
                                }
                                .padding(.horizontal)
                            }
                        }
                    }
                }
                .padding(.bottom, 80) // Space for tab bar
            }
            .refreshable {
                vm.fetchNetworkContent(for: networkName)
            }
        }
        .addBackground()
        .hideNavigationbar()
        .onAppear {
            vm.fetchNetworkContent(for: networkName)
        }
    }
    
    private var networkDescription: String {
        switch networkName {
        case "MediaTeka":
            return "Premium streaming content from MediaTeka"
        case "HBO":
            return "Home of award-winning series, movies, and documentaries"
        default:
            return "Exclusive content from \(networkName)"
        }
    }
}

struct ContentSection: View {
    let title: String
    let contents: [VugaContent]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .outfitSemiBold(20)
                .foregroundColor(.text)
                .padding(.horizontal)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    ForEach(contents, id: \.id) { content in
                        VStack(alignment: .leading, spacing: 5) {
                            KFImage(content.verticalPoster?.addBaseURL())
                                .resizeFillTo(width: 120, height: 180, radius: 10)
                                .addStroke(radius: 10)
                            
                            Text(content.title ?? "")
                                .outfitMedium(12)
                                .foregroundColor(.text)
                                .lineLimit(1)
                                .frame(width: 120, alignment: .leading)
                        }
                        .onTap {
                            Navigation.pushToSwiftUiView(ContentDetailView(contentId: content.id))
                        }
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

struct NetworkContentGridItem: View {
    let content: VugaContent
    
    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            KFImage(content.verticalPoster?.addBaseURL())
                .resizeFillTo(width: (Device.width - 50) / 3, height: ((Device.width - 50) / 3) * 1.5, radius: 10)
                .addStroke(radius: 10)
            
            Text(content.title ?? "")
                .outfitMedium(14)
                .foregroundColor(.text)
                .lineLimit(1)
            
            HStack(spacing: 5) {
                if content.type == .series {
                    Text("Series")
                        .outfitLight(10)
                        .foregroundColor(.base)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.base.opacity(0.2))
                        .cornerRadius(4)
                }
                
                HStack(spacing: 3) {
                    Image.star
                        .resizeFitTo(size: 10)
                    Text(content.ratingString)
                        .outfitLight(12)
                }
                .foregroundColor(.rating)
            }
        }
        .onTap {
            Navigation.pushToSwiftUiView(ContentDetailView(contentId: content.id))
        }
    }
}

// View Model for Network Content
class NetworkContentViewModel: BaseViewModel {
    @Published var networkContent: [VugaContent] = []
    @Published var featuredContent: [VugaContent]?
    private var currentNetwork: String = ""
    
    func fetchNetworkContent(for network: String) {
        if isLoading { return }
        currentNetwork = network
        startLoading()
        
        // TODO: Replace with actual network filtering API
        // For now, we'll fetch all content and filter client-side
        // In production, you'd want a server-side filter by network/studio
        
        let params: [Params: Any] = [
            .page: 1
            // Add network/studio parameter when API supports it
            // .network: network
        ]
        
        NetworkManager.callWebService(url: .fetchContents, params: params) { (obj: ContentsModel) in
            self.stopLoading()
            if let contents = obj.data {
                // Simulate network filtering
                // In production, this would be done server-side
                self.networkContent = contents
                
                // Set featured content (first 5 items with high ratings)
                self.featuredContent = Array(contents
                    .filter { ($0.ratings ?? 0) >= 4.0 }
                    .prefix(5))
            }
        }
    }
}