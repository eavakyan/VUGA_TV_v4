////
////  RewardedAdManager.swift
////  sphere
////
////  Created by Aniket Vaddoriya on 02/05/24.
////
//
import Foundation
import GoogleMobileAds
import SwiftUI


class RewardedAdManager: NSObject {
    static var shared = RewardedAdManager()
    var rewardedAd: GADRewardedAd?
    var adCompletion: () -> () = {}
    var adId: String {
        SessionManager.shared.getAds().first(where: { $0.type == 2 })?.rewardedID ?? ""
    }

    func loadRewardAd() {
        let request = GADRequest()

        GADRewardedAd.load(withAdUnitID: adId, request: request) { [weak self] ad, error in
            if let error = error {
                print("Failed to load rewarded ad with error: \(error.localizedDescription)")
                return
            }
            self?.rewardedAd = ad
            print("Rewarded ad loaded.")
            self?.rewardedAd?.fullScreenContentDelegate = self
        }
    }

    func showAdReward(completion: @escaping () -> ()) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            guard let self = self,
                  let uiViewController = UIApplication.shared.keyWindowPresentedController,
                  let rewardedAd = self.rewardedAd else {
                completion()
                return
            }
            self.adCompletion = completion
            rewardedAd.present(fromRootViewController: uiViewController) {
                print("Rewarded")
            }
        }
    }
}

extension RewardedAdManager: GADFullScreenContentDelegate {
    func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        print("Ad did fail to present full screen content.")
        print(error.localizedDescription)
        self.adCompletion()
        self.loadRewardAd()
    }

    func adWillPresentFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        print("Ad will present full screen content.")
    }

    func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        print("Ad did dismiss full screen content.")
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            self.adCompletion()
            self.loadRewardAd()
        }
    }

    func adWillDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        print("Ad will dismiss full screen content.")
    }

    func adDidRecordImpression(_ ad: GADFullScreenPresentingAd) {
        print(#function)
    }
}


struct RewardAd: View {
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 20) {
                Image("admob")
                    .resizable()
                    .scaledToFill()
                    .frame(height: 200)
                Button(action: {
                    RewardedAdManager.shared.showAdReward(completion:{
                        print("Get reward")
                    })
                }) {
                    Text("Show Reward")
                        .font(.headline)
                        .foregroundColor(.white)
                        .fontWeight(.bold)
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(Color.blue)
                        .cornerRadius(10)
                        .shadow(radius: 10)
                }
                
                Spacer()
            }
            .padding()
        }
        .onAppear {
            RewardedAdManager.shared.loadRewardAd()
        }
    }
}

