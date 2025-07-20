//
//  CustomAdViewModel.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 17/06/24.
//

import Foundation
import SwiftUI
import AVKit

class CustomAdViewModel : BaseViewModel {
    @Published var customAd = SessionManager.shared.getCustomAds().randomElement()
    @Published var currentTime: Double = 0
    @Published var player: AVPlayer?
    @Published var duration: Double = 1.0
    @Published var timer : Timer?
    @Published var adSource : AdSource?

    override init() {
        super.init()
        DispatchQueue.main.asyncAfter(deadline: .now()) {
            self.adSource = self.customAd?.sources?.randomElement()
            if self.adSource?.type == .video {
                if let url = self.adSource?.content?.addBaseURL() {
                    self.player = AVPlayer(url: url)
                }
            }
        }
    }
    
    func increaseAdMetric(customAdId: Int, metric: CustomAdMetric) {
        let params: [Params: Any] = [.customAdId: customAdId, .metric: metric.rawValue]
        NetworkManager.callWebService(url: .increaseAdMetric, params: params, callbackSuccess: { (obj: AdMetricModel) in
            if obj.status == true {
                print("Increase Ad Metric")
            }
        })
    }
    
    func getCurrentPosition() {
        if let player = player {
            currentTime = player.currentTime().seconds
            duration = player.currentItem?.duration.seconds ?? 0.0
        }
    }
    
    func play() {
        player?.play()
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true, block: { timer in
            self.getCurrentPosition()
        })
    }
    
    func startImageTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true, block: { timer in
            self.currentTime += 0.1
            print(self.currentTime)
        })
    }
    
}

enum CustomAdMetric : String {
    case click, view
}
