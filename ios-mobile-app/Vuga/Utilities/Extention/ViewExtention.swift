//
//  ViewExtention.swift
//  sphere
//
//

import SwiftUI
import Combine

extension View {
    func addStroke(radius: CGFloat, color: Color = Color.stroke,lineWidth: CGFloat = 1) -> some View {
        self
            .overlay(
                RoundedRectangle(cornerRadius: radius, style: .continuous)
                    .inset(by: 0.5)
                    .stroke(color,lineWidth: lineWidth)
            )
    }
    
    func addBackground() -> some View {
        self
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color.bg.ignoresSafeArea())
            .hideNavigationbar()
    }
    
    func loaderView(_ isLoading: Bool, shouldShowBg: Bool = true, isshowNoDataView: Bool = false) -> some View {
        ZStack {
            self
                .overlay(
                    ZStack {
                        if isLoading {
                            Color.black.opacity(isLoading && shouldShowBg ? 0.01 : 0).ignoresSafeArea().onTap {}
                            if !isshowNoDataView {
                                ProgressView()
                                    .tint(.base)
                                    .padding(15)
                                    .background(Color.bg)
                                    .clipShape(Circle())
                            } else {
                                Text(String.videoCantPlay.localized(LocalizationService.shared.language))
                                    .outfitRegular(16)
                                    .foregroundColor(.textLight)
                                    .frame(width: Device.width - 20,height: 100)
                                    .background(Color.black)
                            }
                        }
                    }
                )
        }
    }
    
    func noDataFound(_ shouldShow: Bool, title: String = .noDataFound) -> some View {
        self
            .overlay(
                ZStack {
                    if shouldShow {
                        VStack {
                            Image.noData
                                .resizeFitTo(size: 100,renderingMode: .template)
                                .foregroundColor(.textThin)
                            Text(title.localized(LocalizationService.shared.language))
                                .outfitRegular(24)
                                .foregroundColor(.textThin)
                        }
                    }
                }
            )
    }
    
    func emptySearch(_ shouldShow: Bool) -> some View {
        self
            .overlay(
                ZStack {
                    if shouldShow {
                        Text(String.enterSomeThingToSearch.localized(LocalizationService.shared.language))
                            .outfitBold(18)
                            .foregroundColor(.textLight)
                    }
                }
            )
    }
    
    @ViewBuilder 
    func hideNavigationbar() -> some View {
        if #available(iOS 16, *) {
            self.toolbar(.hidden, for: .navigationBar)
                .navigationBarTitleDisplayMode(.inline)
        } else {
            self.navigationBarHidden(true)
                .navigationBarTitleDisplayMode(.inline)
       }
    }
    
    @ViewBuilder
    func hideHomeIndicator() -> some View {
        if #available(iOS 16, *) {
            self.persistentSystemOverlays(.hidden)
        } else {
            self
        }
    }
    
    func cornerRadius(radius: CGFloat) -> some View {
        self.clipShape(RoundedRectangle(cornerRadius: radius,style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: radius,style: .continuous))
    }
    
    func onTap(completion: @escaping ()->()) -> some View {
        Button(action: {
            completion()
        }, label: {
            self
                .contentShape(Rectangle())
        })
        .buttonStyle(.myPlain)
    }
    
    func maxFrame(_ alignement: Alignment = .center) -> some View {
        self.frame(maxWidth: .infinity,maxHeight: .infinity, alignment: alignement)
    }
    
    func maxWidthFrame(_ alignement: Alignment = .center) -> some View {
        self.frame(maxWidth: .infinity, alignment: alignement)
    }
   
    func maxHeightFrame(_ alignement: Alignment = .center) -> some View {
        self.frame(maxHeight: .infinity, alignment: alignement)
    }
    
    func hidden(_ shouldHide: Bool) -> some View {
        opacity(shouldHide ? 0 : 1)
    }
    
    func makeBgOfButton(bgColor: Color = Color.text.opacity(0.2),size: CGFloat = 35) -> some View {
        self.frame(width: size, height: size)
            .foregroundColor(.text)
            .background(bgColor)
            .clipShape(Circle())
            .overlay(Circle().stroke(Color.text.opacity(0.2),lineWidth: 1))
    }
}

extension UIApplication {
    var keyWindow: UIWindow? {
        // Get connected scenes
        return self.connectedScenes
        // Keep only active scenes, onscreen and visible to the user
            .filter { $0.activationState == .foregroundActive }
        // Keep only the first `UIWindowScene`
            .first(where: { $0 is UIWindowScene })
        // Get its associated windows
            .flatMap({ $0 as? UIWindowScene })?.windows
        // Finally, keep only the key window
            .first(where: \.isKeyWindow)
    }
    
    var keyWindowPresentedController: UIViewController? {
        var viewController = self.keyWindow?.rootViewController
        
        // If root `UIViewController` is a `UITabBarController`
        if let presentedController = viewController as? UITabBarController {
            // Move to selected `UIViewController`
            viewController = presentedController.selectedViewController
        }
        
        // Go deeper to find the last presented `UIViewController`
        while let presentedController = viewController?.presentedViewController {
            // If root `UIViewController` is a `UITabBarController`
            if let presentedController = presentedController as? UITabBarController {
                // Move to selected `UIViewController`
                viewController = presentedController.selectedViewController
            } else {
                // Otherwise, go deeper
                viewController = presentedController
            }
        }
        
        return viewController
    }
}
protocol KeyboardReadable {
    var keyboardPublisher: AnyPublisher<Bool, Never> { get }
}

extension KeyboardReadable {
    var keyboardPublisher: AnyPublisher<Bool, Never> {
        Publishers.Merge(
            NotificationCenter.default
                .publisher(for: UIResponder.keyboardWillShowNotification)
                .map { _ in true },
            
            NotificationCenter.default
                .publisher(for: UIResponder.keyboardWillHideNotification)
                .map { _ in false }
        )
        .eraseToAnyPublisher()
    }
}

extension View where Self: KeyboardReadable {
    func onKeyboardVisibilityChanged(_ action: @escaping (Bool) -> Void) -> some View {
        return self.onReceive(keyboardPublisher) { newIsKeyboardVisible in
            action(newIsKeyboardVisible)
        }
    }
}

extension View {
    func customCornerRadius(radius: CGFloat) -> some View {
        self.clipShape(RoundedRectangle(cornerRadius: radius, style: .continuous))
    }
    
    func addbgToProfileCard() -> some View {
        self.background(Color.bg)
            .customCornerRadius(radius: 16)
            .padding(1)
            .background(Color.text.opacity(0.2))
            .customCornerRadius(radius: 17)
            .padding(.horizontal)
    }
}

extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape( RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {

    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}
