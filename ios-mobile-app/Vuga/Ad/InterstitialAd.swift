//
//  InterstitialAd.swift
//  Vuga
//
//

import Foundation

import SwiftUI
import GoogleMobileAds


class Interstitial: NSObject, GADFullScreenContentDelegate {
    private var interstitial: GADInterstitialAd?
    static var shared: Interstitial = Interstitial()
    var adId: String {
        SessionManager.shared.getAds().first(where: { $0.type == 2 })?.intersialID ?? ""
    }
    override init() {
        super.init()
        // AdMob disabled - not loading interstitial ads
        // loadInterstitial()
    }
    
    func loadInterstitial() {
        // AdMob disabled - not loading ads
        return
        /*
        let request = GADRequest()
        GADInterstitialAd.load(withAdUnitID: adId, request: request, completionHandler: { [self] ad, error in
            if ad != nil { interstitial = ad }
            interstitial?.fullScreenContentDelegate = self
        }
        )
        */
    }
    
    func showInterstitialAds() {
        // AdMob disabled - not showing ads
        return
        /*
        if interstitial != nil, let root = rootController {
            interstitial?.present(fromRootViewController: root)
        }
        */
    }
    
    func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        loadInterstitial()
    }
}

var rootController: UIViewController? {
    var root = UIApplication.shared.windows.first?.rootViewController
    if let presenter = root?.presentedViewController { root = presenter }
    return root
}

