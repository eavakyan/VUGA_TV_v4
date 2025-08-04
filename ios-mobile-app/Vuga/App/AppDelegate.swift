//
//  AppDelegate.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import SwiftUI
import FirebaseMessaging
import Firebase
import GoogleMobileAds
import GoogleCast

import SwiftUI
// import RevenueCat - Disabled temporarily
import BranchSDK
import ActivityKit

class AppDelegate : NSObject,UIApplicationDelegate { // PurchasesDelegate {
    @AppStorage(SessionKeys.isNotificationOn) var isNotificationOn = true
    @AppStorage(SessionKeys.myUser) var myUser : User? = nil
    static let shared = AppDelegate()
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        // AdMob disabled - not initializing
        // GADMobileAds.sharedInstance().start(completionHandler: nil)

        // Initialize Google Cast
        initializeGoogleCast()

        // Temporarily disabled RevenueCat - configure valid API key in Const.swift
        // Purchases.logLevel = .debug
        // Purchases.configure(withAPIKey: RevenueCatApiKey, appUserID: "\(myUser?.id ?? 0)")
        // Purchases.shared.delegate = self
        self.registerForPushNotifications()
        Branch.getInstance().checkPasteboardOnInstall()
        
        Branch.getInstance().initSession(launchOptions: launchOptions) { (params, error) in
            if let id = params?[WebService.branchContentID], let contentId = id as? String {
                Navigation.pushToSwiftUiView(ContentDetailView(contentId: Int(contentId)))
            }
        }
        return true
    }
    
    private func initializeGoogleCast() {
        // Use Default Media Receiver for testing - it supports all standard video formats
        // let appId = "4F8B3483" // Custom receiver - may have issues with video playback
        let appId = kGCKDefaultMediaReceiverApplicationID // "CC1AD845" - Default receiver
        
        print("🚀 GoogleCast: Initializing with app ID: \(appId)")
        print("🔧 Using Default Media Receiver for better video compatibility")
        
        let criteria = GCKDiscoveryCriteria(applicationID: appId)
        let options = GCKCastOptions(discoveryCriteria: criteria)
        options.physicalVolumeButtonsWillControlDeviceVolume = true
        options.disableDiscoveryAutostart = false
        options.startDiscoveryAfterFirstTapOnCastButton = false
        
        GCKCastContext.setSharedInstanceWith(options)
        
        let castContext = GCKCastContext.sharedInstance()
        castContext.useDefaultExpandedMediaControls = true
        
        // Enable logging for debugging
        GCKLogger.sharedInstance().loggingEnabled = true
        
        // Start discovery immediately
        castContext.discoveryManager.startDiscovery()
        
        print("✅ GoogleCast: Context initialized and discovery started")
        print("🔍 GoogleCast: Discovery manager state: \(castContext.discoveryManager.discoveryState.rawValue)")
        print("🔍 GoogleCast: Discovery active: \(castContext.discoveryManager.discoveryActive)")
        print("🔍 GoogleCast: Has discovered devices: \(castContext.discoveryManager.hasDiscoveredDevices)")
        
        // Add observer for discovery state changes
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name.gckCastStateDidChange,
            object: castContext,
            queue: OperationQueue.main
        ) { _ in
            print("📡 GoogleCast: Cast state changed to: \(castContext.castState.rawValue)")
            print("🔍 GoogleCast: Discovery state: \(castContext.discoveryManager.discoveryState.rawValue)")
            print("📱 GoogleCast: Device count: \(castContext.discoveryManager.deviceCount)")
        }
        
        // Log devices after a short delay to allow discovery
        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
            let deviceCount = castContext.discoveryManager.deviceCount
            print("🔥 GoogleCast: Current device count after 3 seconds: \(deviceCount)")
            if deviceCount == 0 {
                print("❗ No Cast devices found. Make sure:")
                print("   - Your Android TV is on the same WiFi network")
                print("   - Cast is enabled on your Android TV")
                print("   - Your WiFi allows device-to-device communication")
            } else {
                for i in 0..<deviceCount {
                    let device = castContext.discoveryManager.device(at: i)
                    print("📺 Device \(i): \(device.friendlyName ?? "Unknown") - ID: \(device.deviceID)")
                }
            }
        }
        
        print("✅ GoogleCast: Initialization complete")
    }
    
    // func purchases(_ purchases: Purchases, receivedUpdated customerInfo: CustomerInfo) {
    //     BaseViewModel.shared.checkUserIsPro(customerInfo: customerInfo)
    // }
}

extension AppDelegate {
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        // Handle TV authentication deep links
        if url.scheme == "vuga" && url.host == "auth" && url.pathComponents.count >= 3 && url.pathComponents[1] == "tv" {
            let sessionToken = url.pathComponents[2]
            // Navigate to QR scanner with the session token
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                Navigation.pushToSwiftUiView(QRScannerView(sessionToken: sessionToken))
            }
            return true
        }
        
        Branch.getInstance().application(app, open: url, options: options)
        return true
    }
    
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        Branch.getInstance().continue(userActivity)
        return true
    }
}

extension AppDelegate : MessagingDelegate, UNUserNotificationCenterDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let fcmToken = fcmToken {
            let dataDict: [String: String] = ["token": fcmToken]
            WebService.deviceToken = fcmToken
            if isNotificationOn {
                self.subscribeTopic()
            } else {
                self.unSubscribeTopic()
            }
            print(dataDict)
        }
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        completionHandler(.newData)
        Branch.getInstance().handlePushNotification(userInfo)
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                    willPresent notification: UNNotification,
                                    withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions)
                                    -> Void) {
            let userInfo = notification.request.content.userInfo
            print(userInfo)
            Messaging.messaging().appDidReceiveMessage(userInfo)
            if let messageID = userInfo["gcm.message_id"] as? String  {
                let center = UNUserNotificationCenter.current()
                center.removeDeliveredNotifications(withIdentifiers: [messageID])
            }
            completionHandler([[.banner, .badge, .sound]])
        }
        
        func userNotificationCenter(_ center: UNUserNotificationCenter,
                                       didReceive response: UNNotificationResponse,
                                       withCompletionHandler completionHandler: @escaping () -> Void) {
               let userInfo = response.notification.request.content.userInfo
               
               if let messageID = userInfo["gcm.message_id"] {
                   print("Message ID: \(messageID)")
               }
               UIApplication.shared.applicationIconBadgeNumber = -1
               print(response.notification.request.content)
               
               if let messageID = userInfo["gcm.message_id"] {
                   print("Message ID: \(messageID)")
               }
            let appState = UIApplication.shared.applicationState

            if let id = userInfo["content_id"] as? String, let contentId = Int(id) {
                let delay: TimeInterval = appState == .active ? 0.2 : 1
                DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                        Navigation.pushToSwiftUiView(ContentDetailView(contentId: contentId))
                    }
                }
               
               completionHandler()
           }
    
//    func userNotificationCenter(_ center: UNUserNotificationCenter,
//                                willPresent notification: UNNotification,
//                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions)
//                                -> Void) {
//        let userInfo = notification.request.content.userInfo
//        Messaging.messaging().appDidReceiveMessage(userInfo)
//        print(userInfo)
//        completionHandler([[.banner, .badge, .sound]])
//    }
//    
//    func userNotificationCenter(_ center: UNUserNotificationCenter,
//                                didReceive response: UNNotificationResponse,
//                                withCompletionHandler completionHandler: @escaping () -> Void) {
//        print("Notification Sent")
//        UIApplication.shared.applicationIconBadgeNumber = -1
//        completionHandler()
//    }
    
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print(error.localizedDescription)
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
        Messaging.messaging().setAPNSToken(deviceToken, type: .unknown)
    }
    
    func registerForPushNotifications() {
        Messaging.messaging().delegate = self
        UIApplication.shared.applicationIconBadgeNumber = 0
        
        UNUserNotificationCenter.current().removeAllDeliveredNotifications()
        UNUserNotificationCenter.current().delegate = self
        
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { status, err in }
        UIApplication.shared.registerForRemoteNotifications()
    }
    
    func subscribeTopic() {
        Messaging.messaging().subscribe(toTopic: WebService.notificationTopic) { (error) in
            if error != nil {
                print("error for subscribe topic",error?.localizedDescription as Any)
            } else {
                print("Subscribed Topic: \(WebService.notificationTopic)")
            }
        }
    }
    func unSubscribeTopic() {
        Messaging.messaging().unsubscribe(fromTopic: WebService.notificationTopic) { (error) in
            if error != nil {
                print("error for unSubscribe topic",error?.localizedDescription as Any)
            } else {
                print("UnSubscribed Topic")
            }
        }
    }
}

extension AppDelegate {
    static var orientationLock = UIInterfaceOrientationMask.portrait
    
    func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
        // Check if device is iPad
        if UIDevice.current.userInterfaceIdiom == .pad {
            // Allow all orientations on iPad
            return .all
        } else {
            // Keep portrait lock on iPhone
            return AppDelegate.orientationLock
        }
    }
    
    static func setOrientation(_ orientation: UIInterfaceOrientation) {
        UIDevice.current.setValue(orientation.rawValue, forKey: "orientation")
        AppDelegate.orientationLock = UIInterfaceOrientationMask(rawValue: UInt(1 << orientation.rawValue))
        if #available(iOS 16.0, *) {
            UIApplication.shared.connectedScenes.forEach { scene in
                if let windowScene = scene as? UIWindowScene {
                    windowScene.requestGeometryUpdate(.iOS(interfaceOrientations: AppDelegate.orientationLock))
                }
            }
        } else {
            UIViewController.attemptRotationToDeviceOrientation()
        }
    }
}


extension AppDelegate {
    func application(_ application: UIApplication, handleEventsForBackgroundURLSession identifier: String, completionHandler: @escaping () -> Void) {
    }
}



//class SceneDelegate: UIResponder, UIWindowSceneDelegate {
//    
//    var window: UIWindow?
//    
//    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
//        if let windowScene = scene as? UIWindowScene {
//            let window = UIWindow(windowScene: windowScene)
//            let contentView = ContentView()
//            window.rootViewController = UIHostingController(rootView: contentView)
//            self.window = window
//            window.makeKeyAndVisible()
//            if let userActivity = connectionOptions.userActivities.first {
//                BranchScene.shared().scene(scene, continue: userActivity)
//            } else if !connectionOptions.urlContexts.isEmpty {
//                BranchScene.shared().scene(scene, openURLContexts: connectionOptions.urlContexts)
//            }
//        }
//    }
//    
//    func windowScene(_ windowScene: UIWindowScene, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
//        return AppDelegate.orientationLock
//    }
//    
//    func sceneDidDisconnect(_ scene: UIScene) {
//    }
//    
//    func sceneDidBecomeActive(_ scene: UIScene) {
//    }
//    
//    func sceneWillResignActive(_ scene: UIScene) {
//    }
//    
//    func sceneWillEnterForeground(_ scene: UIScene) {
//    }
//    
//    
//    func sceneDidEnterBackground(_ scene: UIScene) {
//    }
//    
//    func scene(_ scene: UIScene, willContinueUserActivityWithType userActivityType: String) {
//        scene.userActivity = NSUserActivity(activityType: userActivityType)
//        scene.delegate = self
//    }
//    
//    func scene(_ scene: UIScene, continue userActivity: NSUserActivity) {
//        BranchScene.shared().scene(scene, continue: userActivity)
//    }
//    
//    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
//        BranchScene.shared().scene(scene, openURLContexts: URLContexts)
//    }
//    
//}
