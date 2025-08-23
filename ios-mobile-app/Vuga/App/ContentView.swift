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
            
            // Show offline banner at the top instead of blocking the whole app
            if !isConnectedToInternet && isLoggedIn {
                VStack {
                    HStack(spacing: 8) {
                        Image(systemName: "wifi.slash")
                            .foregroundColor(.white)
                            .font(.system(size: 12, weight: .medium))
                        Text("Offline Mode")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(.white)
                        Text("•")
                            .foregroundColor(.white.opacity(0.6))
                            .font(.system(size: 10))
                        Text("Using cached content")
                            .font(.system(size: 11))
                            .foregroundColor(.white.opacity(0.9))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [Color.orange, Color.red.opacity(0.9)]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(20)
                    .shadow(color: .black.opacity(0.3), radius: 5, x: 0, y: 2)
                    .padding(.top, 50) // Account for status bar
                    
                    Spacer()
                }
                .transition(.asymmetric(
                    insertion: .move(edge: .top).combined(with: .opacity),
                    removal: .move(edge: .top).combined(with: .opacity)
                ))
                .animation(.spring(response: 0.5, dampingFraction: 0.8), value: isConnectedToInternet)
                .zIndex(100) // Ensure it appears on top
            }
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
