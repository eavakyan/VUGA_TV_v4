//
//  Const.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import Foundation
import UIKit

let APP_NAME = "Vuga"
let APP_ID = "6746174928"
let RevenueCatApiKey = "Your RevenueCat API Key"
let PRIVACY_URL = "\(WebService.base)privacyPolicy"
let TERMS_URL = "\(WebService.base)termsOfUse"
let adUnitID = "Your Add Unit ID"
let rateThisAppURL = "https://apps.apple.com/us/app/id\(APP_ID)?action=write-review"

struct WebService {
    static let branchContentID              = "content_id"
    static let base                         = "https://iosdev.gossip-stone.com/"
    static let apiBase                      = WebService.base + "api/v2/"
    static let itemBaseURLs                  = WebService.base + "public/storage/"
    static let itemBaseURL                  = ""
    static let youtubeBaseURL               = "https://www.youtube.com/watch?v="
    static let headerKey                    = "apikey"
    static let headerValue                  = "jpwc3pny"  // Default: 123, if you want to change, please check Backend documentation.
    static var deviceToken                  = ""
    static var notificationToken            = "-"
    static var notificationTopic            = "vuga_ios"
}

//MARK: - Session Keys
struct SessionKeys {
    static var language = "language"
    static var isLoggedIn = "isLoggedIn"
    static var myUser = "myUser"
    static var isPro = "isPro"
    static var isNotificationOn = "isNotificationOn"
}

//MARK: - Limits
struct Limits {
    static var pagination = 20
    static var featureSecond = 5
    static var videoControlsDuration = 3
    static var skipVideoSeconds = 10
}


class Device {
    static let width = UIScreen.main.bounds.width
    static let height = UIScreen.main.bounds.height
    static let topSafeArea = getTopSafeArea()
    static let bottomSafeArea = getBottomSafeArea()
    
    static var isIPad: Bool {
        return UIDevice.current.userInterfaceIdiom == .pad
    }
    
    static func getTopSafeArea() -> CGFloat {
        UIApplication.shared.keyWindow?.window?.safeAreaInsets.top ?? 0
    }
    
    static func getBottomSafeArea() -> CGFloat {
        UIApplication.shared.keyWindow?.window?.safeAreaInsets.bottom ?? 0
    }
}

extension NSNotification.Name {
    static var hideTabbar = NSNotification.Name.init("hideTabbar")
    static var showTabbar = NSNotification.Name.init("showTabbar")
}

struct DocumentsDirectory {
    static let localDocumentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).last!

    static let iCloudDocumentsURL = FileManager.default.url(forUbiquityContainerIdentifier: nil)?.appendingPathComponent("Library/HiddenDocuments", isDirectory: true)
}
