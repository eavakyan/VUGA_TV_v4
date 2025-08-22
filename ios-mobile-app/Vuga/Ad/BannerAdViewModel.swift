//
//  BannerAdViewModel.swift
//  Vuga
//
//

import Foundation
import GoogleMobileAds

class BannerAdViewModel: BaseViewModel, GADBannerViewDelegate{
    @Published var isAdLoaded = false
    
    func bannerViewDidReceiveAd(_ bannerView: GADBannerView) {
        isAdLoaded = true
      print("bannerViewDidReceiveAd")
    }
    
    func bannerView(_ bannerView: GADBannerView, didFailToReceiveAdWithError error: Error) {
        isAdLoaded = false
      print("bannerView:didFailToReceiveAdWithError: \(error.localizedDescription)")
    }
}
