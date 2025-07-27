//
//  BannerAd.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 10/08/24.
//

import Foundation
import SwiftUI
import GoogleMobileAds

struct BannerAd : View {
    @StateObject var vm = BannerAdViewModel()
    @AppStorage(SessionKeys.isPro) var isPro = false

    var body: some View {
        if !isPro {
            BannerView(vm: vm)
                .frame(height: 50, alignment: .center)
                .frame(height:vm.isAdLoaded ? 50 : 1, alignment: .center)
        }
    }
}

struct BannerView : UIViewRepresentable {
    var vm : BannerAdViewModel
    var adId: String {
        SessionManager.shared.getAds().first(where: { $0.type == 2 })?.bannerID ?? ""
    }
    func makeUIView(context: UIViewRepresentableContext<BannerView>) -> some GADBannerView {
        let request = GADRequest()
        let banner = GADBannerView(adSize: GADAdSize())
        banner.frame = CGRect(x: 0, y: 0, width: 320, height: 50)
        banner.adUnitID = adId
        banner.rootViewController = UIApplication.shared.windows.first?.rootViewController
        banner.load(request)
        banner.delegate = vm
        return banner
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        //
    }
}
