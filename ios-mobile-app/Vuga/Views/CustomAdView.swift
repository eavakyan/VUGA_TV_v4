//
//  CustomAdView.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 17/06/24.
//

import SwiftUI
import AVKit
import Kingfisher
import ExpandableText
import Sliders

struct CustomAdView: View {
    @Environment(\.scenePhase) var scenePhase
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm = CustomAdViewModel()
    @Environment(\.presentationMode) var present
    @Binding var isShowAdView : Bool
    var body: some View {
        if let customAd = vm.customAd, let adSource = vm.adSource {
            ZStack {
                if adSource.type == .video {
                    ZStack {
                        if let player = vm.player {
                            VideoPlayer(player: player)
                                .ignoresSafeArea()
                        }
                        VStack {
                            HStack {
                                KFImage(customAd.brandLogo?.addBaseURL())
                                    .resizeFillTo(width: 50, height: 50,radius: 10)
                                Text(customAd.brandName ?? "")
                                    .outfitMedium(14)
                                    .foregroundColor(.text)
                                Spacer()
                                adButton
                            }
                            Spacer(minLength: 0)
                            HStack {
                                Text(customAd.buttonText ?? "")
                                    .outfitLight(13)
                                    .foregroundStyle(.black)
                                    .frame(width: 120,height: 40)
                                    .background(Color.text)
                                    .customCornerRadius(radius: 10)
                                    .onTap{
                                        if let url = URL(string: customAd.iosLink ?? "") {
                                            UIApplication.shared.open(url)
                                        }
                                        vm.increaseAdMetric(customAdId: customAd.id ?? 0, metric: .click)
                                        
                                    }
                                Spacer()
                                if adSource.isSkippable == 1 || adSource.type == .image {
                                    let remainingSecForVideo = Int(SessionManager.shared.getSetting()?.videoadSkipTime ?? 0) - (Int(vm.currentTime))
                                    let remainingSecForImage = Int(adSource.showTime ?? 0) - Int(vm.currentTime)
                                    
                                    let commomRemainings = adSource.type == .image ? remainingSecForImage : remainingSecForVideo
                                    
                                    //                            if (remainingSecForVideo >= 1 && adSource.type == .video) || (adSource.type == .image && remainingSecForImage >= 1) {
                                    if (commomRemainings >= 1) {
                                        
                                        //                                Text("\(String.adSkip.localized(language)) : \((adSource.type == .image ? remainingSecForImage : remainingSecForVideo).secondsToTime())")
                                        Text("\(String.adSkip.localized(language)) : \(commomRemainings.secondsToTime())")
                                            .outfitRegular(13)
                                            .foregroundColor(.white)
                                            .padding(10)
                                            .background(Blur())
                                            .customCornerRadius(radius: 10)
                                    } else {
                                        Text(String.skipAd.localized(language))
                                            .outfitRegular(13)
                                            .foregroundColor(.white)
                                            .padding(10)
                                            .background(Blur())
                                            .customCornerRadius(radius: 10)
                                            .onTap {
                                                dismissAd()
                                            }
                                    }
                                }
                            }
                            ExpandableText(adSource.description ?? "")
                                .font(.custom(MyFont.OutfitMedium, size: 14))
                                .foregroundColor(.textLight)
                                .moreButtonText(String.readMore.localized(language))
                                .moreButtonFont(.custom(MyFont.OutfitBold, size: 14))
                                .moreButtonColor(.text)
                                .enableCollapse(true)
                                .expandAnimation(.easeInOut(duration: 0))
                            if adSource.type == .video && !vm.isLoading && vm.currentTime >= 0.2 {
                                AdProgressBar(vm: vm, value: $vm.currentTime)
                            }
                        }
                        .padding()
                    }
                } else if adSource.type == .image {
                    HStack(spacing: 0) {
                        KFImage(adSource.content?.addBaseURL())
                            .resizeFillTo(width: Device.height / 2, height: Device.width)
                            .ignoresSafeArea()
                             .frame(width: Device.height / 2, height: Device.width)

                        VStack(alignment: .leading) {
                            VStack(alignment: .leading,spacing: 10) {
                                HStack {
                                    Spacer()
                                    adButton
                                }
                                .padding(.top,30)
                                Spacer()
                                HStack {
                                    KFImage(customAd.brandLogo?.addBaseURL())
                                        .resizeFillTo(width: 50, height: 50,radius: 10)
                                    Text(customAd.brandName ?? "")
                                        .outfitMedium(14)
                                        .foregroundColor(.black)
                                    Spacer()
                                }
                                Text(adSource.headline ?? "")
                                    .outfitBold(20)
                                    .foregroundColor(.black)
                                Text(adSource.description ?? "")
                                    .font(.custom(MyFont.OutfitMedium, size: 14))
                                    .lineLimit(4)
                                    .foregroundColor(.gray)
//                                ExpandableText(adSource.description ?? "")
//                                    .font(.custom(MyFont.OutfitMedium, size: 14))
//                                    .lineLimit(4)
//                                    .foregroundColor(.gray)
//                                    .moreButtonText(String.readMore.localized(language))
//                                    .moreButtonFont(.custom(MyFont.OutfitBold, size: 14))
//                                    .moreButtonColor(.black)
//                                    .enableCollapse(true)
//                                    .expandAnimation(.easeInOut(duration: 0))
                                
                                HStack {
                                    Text(customAd.buttonText ?? "")
                                        .outfitSemiBold(14)
                                        .foregroundStyle(.text)
                                        .frame(width: Device.width / 2,height: 40)
                                        .background(Color.bg)
                                        .customCornerRadius(radius: 10)
                                        .onTap{
                                            if let url = URL(string: customAd.iosLink ?? "") {
                                                UIApplication.shared.open(url)
                                            }
                                            vm.increaseAdMetric(customAdId: customAd.id ?? 0, metric: .click)
                                        }
                                    Spacer()
                                }
                                Spacer()
                            }
                            .padding()
                            ProgressView(value: (vm.currentTime / Double(adSource.showTime ?? 4)) * 100 ,total: 100)
                                .progressViewStyle(LinearProgressViewStyle(tint: .black))
                                .frame(height: 2)
                                .padding([.bottom,.horizontal])
                        }
                        .frame(width: Device.height / 2, height: Device.width)
                        .background(Color.white)
                    }
                }
            }
            .onChange(of: vm.currentTime,perform: { newValue in
                if vm.currentTime > vm.duration - 1 && adSource.type != .image {
                    dismissAd()
                }
                if adSource.type == .image && vm.currentTime > Double(adSource.showTime ?? 4) - 0.1 {
                    dismissAd()
                }
            })
            .onChange(of: vm.player?.timeControlStatus, perform: { newValue in
                if vm.player?.timeControlStatus == .playing {
                    vm.stopLoading()
                }
            })
            .loaderView(vm.isLoading)
            .onAppear(perform: {
                vm.play()
                UIDevice.current.setValue(UIInterfaceOrientation.portrait.rawValue, forKey: "orientation")
                AppDelegate.orientationLock = .landscapeRight
                DispatchQueue.main.asyncAfter(deadline: .now() + 1){
                    if adSource.type == .image {
                        vm.startImageTimer()
                    }
                    print(vm.duration)
                }
            })
            .onDisappear(perform: {
                vm.timer?.invalidate()
                vm.timer = nil
                vm.stopLoading()
            })
            .hideNavigationbar()
            .onChange(of: scenePhase,perform: { newPhase in
                print("////////",newPhase)
                if newPhase == .active {
                    vm.player?.play()
                }
            })
        }
    }
    func dismissAd(){
        isShowAdView = false
        vm.timer?.invalidate()
        vm.timer = nil
        vm.stopLoading()
        vm.increaseAdMetric(customAdId: vm.customAd?.id ?? 0, metric: .view)
    }
    
    var adButton : some View = Text("Ad")
        .outfitMedium(12)
        .foregroundColor(.white)
        .padding(.horizontal,10)
        .padding(.vertical,5)
        .background(Color.darkYellow)
        .cornerRadius(6, corners: [.bottomLeft,.topLeft])
        .padding(.trailing, -18)
    
}

struct AdProgressBar: View {
    @StateObject var vm : CustomAdViewModel
    @Binding var value: Double
    @State var isEditing = false
    @State var height : CGFloat = 5
    var body: some View {
        
        ValueSlider(value: $value,in: 0...(vm.duration), step: 0.1)
            .valueSliderStyle(
                HorizontalValueSliderStyle(
                    track: HorizontalTrack(view: Color.text)
                        .frame(height: height)
                        .background(Color(hexString: "363636")),
                    thumb: Color.text
                        .clipShape(.circle),
                    thumbSize: CGSize(width: 0, height: 0),
                    options: .interactiveTrack
                )
            )
            .frame(height: 10)
    }
}
