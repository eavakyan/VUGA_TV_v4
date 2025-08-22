//
//  ContentView.swift
//  Vuga
//
//

import SwiftUI
import BranchSDK
import Alamofire
import Network

struct ContentView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isLoggedIn) var isLoggedIn = false
    @StateObject var downloadModel = DownloadViewModel()
    @StateObject var vm = SplashViewModel()
    let sharedInstance = NetworkReachabilityManager()!
    var isConnectedToInternet: Bool {
            return self.sharedInstance.isReachable
    }
    @State private var notificationAuthorizationStatus: UNAuthorizationStatus = .notDetermined

    
    var body: some View {
        ZStack {
            NavigationStack {
                SplashView(vm: vm)
                    .hideNavigationbar()
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .ignoresSafeArea(.container, edges: [])
            
            if !isConnectedToInternet {
                OfflineView()
            }
            
            // Removed global tab bar for now - will implement differently
        }
        .onOpenURL(perform: { url in
            Branch.getInstance().handleDeepLink(url)
        })
        .onChange(of: isConnectedToInternet, perform: { newValue in
                vm.isConnectedToInternet = isConnectedToInternet
        })
        .hideNavigationbar()
        .environment(\.managedObjectContext, DataController.shared.context)
        .environmentObject(downloadModel)
        .environment(\.layoutDirection, language == .Arabic ? .rightToLeft : .leftToRight)
    }
    func authorizationStatusString() -> String {
            switch notificationAuthorizationStatus {
            case .authorized:
                return "Authorized"
            case .denied:
                return "Denied"
            case .notDetermined:
                return "Not Determined"
            case .provisional:
                return "Provisional"
            case .ephemeral:
                return "Ephemeral"
            @unknown default:
                return "Unknown"
            }
        }

        func checkNotificationAuthorizationStatus() {
            UNUserNotificationCenter.current().getNotificationSettings { settings in
                DispatchQueue.main.async {
                    self.notificationAuthorizationStatus = settings.authorizationStatus
                }
            }
        }

        func requestNotificationAuthorization() {
            UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
                if let error = error {
                    print("Notification authorization request failed: \(error.localizedDescription)")
                } else {
                    DispatchQueue.main.async {
                        checkNotificationAuthorizationStatus()
                    }
                }
            }
        }
}

class NetworkMonitor: ObservableObject {
    private let networkMonitor = NWPathMonitor()
    private let workerQueue = DispatchQueue(label: "Monitor")
    var isConnected = false
    
    init() {
        self.networkMonitor.pathUpdateHandler = { path in
            DispatchQueue.main.async {
                self.isConnected = path.status == .satisfied
            }
            Task {
                await MainActor.run {
                    self.objectWillChange.send()
                }
            }
        }
        self.networkMonitor.start(queue: self.workerQueue)
    }
}
