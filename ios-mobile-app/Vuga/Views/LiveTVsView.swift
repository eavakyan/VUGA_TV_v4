//
//  LiveTVsView.swift
//  Vuga
//
//

import SwiftUI
import Kingfisher

struct LiveTVsView: View {
    @AppStorage(SessionKeys.isPro) var isPro = false
    @StateObject var vm = LiveTVsViewModel()
    var body: some View {
        VStack(spacing: 0) {
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
            .padding(.vertical, 10)
            
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack {
                    ForEach(vm.categories, id: \.id) { category in
                        HStack {
                            KFImage(category.image?.addBaseURL())
                                .resizable()
                                .renderingMode(.template)
                                .myPlaceholder()
                                .loadDiskFileSynchronously(true)
                                .foregroundColor(.text)
                                .scaledToFit()
                                .frame(width: 20, height: 20)
                                .clipped()
                                .contentShape(Rectangle())
                            
                            Text(category.title ?? "")
                                .outfitRegular(18)
                        }
                        .foregroundColor(.text)
                        .padding(10)
                        .padding(.horizontal, 8)
                        .background(Color.stroke)
                        .cornerRadius(radius: 10)
                        .addStroke(radius: 10)
                        .onTap {
                            Navigation.pushToSwiftUiView(LiveTvView(vm: vm, tvCategory: category))
                        }
                    }
                }
                
                .padding(10)
            }
            .frame(height: 75)
            .padding(.vertical,-10)
            ScrollView(showsIndicators: false, content: {
                LazyVStack(content: {
                    
                    ForEach(vm.categories, id: \.id) { category in
                        TVCategoryCard(vm: vm, category: category)
                    }
                })
                .padding(10)
                .padding(.bottom, 20) // Reduced padding since global tab bar handles spacing
            })
            .refreshable {
                // Refresh Live TV data
                vm.fetchData()
            }
        }
        .loaderView(vm.isLoading)
        .noDataFound(!vm.isLoading && vm.categories.isEmpty)
        .addBackground()
        .hideNavigationbar()
        .fullScreenCover(item: $vm.selectedChannel, content: {_ in
            if vm.selectedChannel?.type?.rawValue ?? 0 == 1 {
                YoutubeView(youtubeUrl: vm.selectedChannel?.source ?? "")
            } else {
                // Check if it's an MPD stream
                let streamUrl = vm.selectedChannel?.source ?? ""
                let streamType = streamUrl.lowercased().contains(".mpd") ? 5 : 2
                VideoPlayerView(type: streamType,isShowAdView: false, isLiveVideo: true,url: streamUrl)
            }
        })
    }
}

#Preview {
    LiveTVsView()
}

struct TVCategoryCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm : LiveTVsViewModel
    var category: TVCategory
    var body: some View {
        if category.channels?.isNotEmpty == true {
            VStack {
                Heading(title: category.title ?? "") {
                    Text(String.seeAll.localized(language))
                        .outfitLight(16)
                        .foregroundColor(.textLight)
                        .onTap {
                            Navigation.pushToSwiftUiView(LiveTvView(vm: vm, tvCategory: category))
                        }
                }
                ScrollView(.horizontal,showsIndicators: false) {
                    LazyHStack(alignment: .lastTextBaseline,content: {
                        ForEach(category.channels ?? [], id: \.id) { channel in
                            ChannelCard(vm: vm, channel: channel)
                        }
                    })
                    .padding(.bottom, 5)
                    .padding(10)
                    .frame(alignment: .top)
                }
                .padding(-10)
            }
        }
    }
}

struct ChannelCard: View {
    @AppStorage(SessionKeys.isPro) var isPro = false
    @StateObject var vm : LiveTVsViewModel
    @State var isShowPremiumDialog = false
    @State var isShowAdDialod = false
    @State var isShowVideoSheet = false
    var lineLimit : Int = 1
    var channel: Channel
    var size: CGFloat = Device.width / 3 - 12
    
    var body: some View {
        VStack(alignment: .center) {
            KFImage(channel.thumbnail?.addBaseURL())
                .resizeFillTo(width: size, height: size)
                .addStroke(radius: 0)
            Text(channel.title ?? "")
                .outfitRegular()
                .foregroundColor(.textLight)
                .frame(width: size)
                .lineLimit(lineLimit)
        }
        .onTap {
            if channel.accessType == .free || isPro {
                vm.selectedChannel = channel
            } else if channel.accessType == .premium {
                isShowPremiumDialog = true
            } else if channel.accessType == .locked {
                isShowAdDialod = true
            }
        }
        .customAlert(isPresented: $isShowPremiumDialog){
            DialogCard(icon: Image.crown ,title: .subScribeToPro, iconColor: .rating, subTitle: .proDialogDes, buttonTitle: .becomeAPro, onClose: {
                withAnimation {
                    isShowPremiumDialog = false
                }
            },onButtonTap: {
                isShowPremiumDialog = false
                Navigation.pushToSwiftUiView(ProView())
            })
        }
        .customAlert(isPresented: $isShowAdDialod){
            DialogCard(icon: Image.adLcok, title: .unlokeWithAd, subTitle: .adDialogDes, buttonTitle: .watchAd, onClose: {
                withAnimation {
                    isShowAdDialod = false
                }
            },onButtonTap: {
                print("]]]]]]]]]")
                isShowAdDialod = false
                RewardedAdManager.shared.showAdReward(completion:{
                    vm.selectedChannel = channel
                })
            })
        }
        
    }
}

struct LiveTvView : View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm : LiveTVsViewModel
    @State var isSearchExpanded = false
    var tvCategory: TVCategory
    var body: some View {
        VStack(spacing: 0) {
            BackBarView(title: tvCategory.title ?? "")
            ScrollView(showsIndicators: false) {
                LazyVGrid(columns: Array(repeating: .init(.flexible(),alignment: .top), count: 3), content: {
                    ForEach(vm.channels, id: \.id) { channel in
                        ChannelCard(vm: vm, lineLimit: 3,channel: channel,size: Device.width / 3 - 20)
                            .onAppear(perform: {
                                if channel.id == vm.channels.last?.id ?? 0 {
                                    vm.fetchData(category: tvCategory)
                                }
                            })
                    }
                })
                .padding(.horizontal)
                .padding(.vertical,10)
            }
            .refreshable {
                // Clear channels and refresh for this category
                vm.channels.removeAll()
                vm.fetchData(category: tvCategory)
            }
        }
        .onAppear(perform: {
            vm.fetchData(category: tvCategory)
        })
        .loaderView(vm.isLoading)
        .addBackground()
        .noDataFound(!vm.isLoading && vm.channels.isEmpty)
        .hideNavigationbar()
    }
}

struct LiveTvSearchView : View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @FocusState var focusField : Bool
    @StateObject var vm = LiveTVsViewModel()
    
    var body: some View {
        VStack {
            HStack {
                BackButton()
                HStack(spacing: 12) {
                    Image.search
                        .resizeFitTo(size: 25, renderingMode: .template)
                        .foregroundColor(.stroke)
                    Capsule()
                        .frame(width: 1,height: 30)
                        .foregroundColor(.stroke)
                    
                    TextField("", text: $vm.search, prompt: Text(String.searchHere.localized(language))
                        .font(Font.custom(MyFont.OutfitRegular,size: CGFloat(16)))
                    )
                    .focused($focusField)
                    .foregroundColor(.text)
                    .outfitRegular()
                }
                .padding(.horizontal)
                .searchOptionBg()
            }
            ScrollView(showsIndicators: false) {
                LazyVGrid(columns: Array(repeating: .init(.flexible()), count: 3), content: {
                    ForEach(vm.filteredChannels, id: \.id) { channel in
                        ChannelCard(vm: vm, channel: channel, size: Device.width / 3 - 20)
                            .onAppear(perform: {
                                if channel.id == vm.filteredChannels.last?.id ?? 0 {
                                    vm.filterLiveTvs(isForRefresh: false)
                                    print("Fetch Live Tv Data")
                                }
                            })
                    }
                })
                .padding(.vertical,10)
            }
        }
        .onChange(of: vm.search, perform: { _ in
            vm.filterLiveTvs()
        })
        .onAppear{
            focusField = true
        }
        .noDataFound(!vm.isLoading && vm.filteredChannels.isEmpty && !vm.isSearchEmpty)
        .emptySearch(vm.isSearchEmpty && !vm.isLoading)
        .padding(.horizontal)
        .addBackground()
        .hideNavigationbar()
    }
}
