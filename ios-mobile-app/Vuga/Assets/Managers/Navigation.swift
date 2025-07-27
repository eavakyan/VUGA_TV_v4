//
//  Navigation.swift
//  Document Scanner
//
//  Created by Aniket Vaddoriya on 08/01/23.
//

import SwiftUI


public class Navigation {
    public static func popToRootView(_ animated: Bool = true) {
        if let navigationController = self.getCurrentNavigationController() {
            navigationController.popToRootViewController(animated: animated)
        }
    }
    
    public static func pop(_ animated: Bool = true) {
        if let navigationController = self.getCurrentNavigationController() {
            navigationController.popViewController(animated: animated)
        }
    }
    
    public static func getCurrentNavigationController() -> UINavigationController? {
        
        if let rootViewController = CUStd.getMainUIWindow()?.rootViewController {
            
            for rootViewControllerChild in rootViewController.children {
                
                if let tabBarController = rootViewControllerChild as? UITabBarController {
                    
                    let currentTabIndex = tabBarController.selectedIndex
                    
                    let currentTabViewController = rootViewControllerChild.children[currentTabIndex]
                    
                    for currentTabViewControllerChild in currentTabViewController.children {
                        if currentTabViewControllerChild is UINavigationController {
                            if let navigationController = currentTabViewControllerChild as? UINavigationController {
                                return navigationController
                            }
                        }
                    }
                }else {
                    if let navigationController = rootViewControllerChild as? UINavigationController {
                        return navigationController
                    }else {
                        for rootViewControllerChildChild in rootViewControllerChild.children {
                            if let navigationController = rootViewControllerChildChild as? UINavigationController {
                                return navigationController
                            }
                        }
                    }
                }
            }
        }
        
        return nil
    }
    
    public static func pushToSwiftUiView<Content: View>(_ view: Content, animated: Bool = true, enableBackNavigation: Bool = true){
        if let navigationController = self.getCurrentNavigationController() {
            let viewController = UIHostingController(rootView: view)
            viewController.navigationItem.largeTitleDisplayMode = .never
            navigationController.pushViewController(viewController, animated: animated)
            navigationController.interactivePopGestureRecognizer?.isEnabled = enableBackNavigation
        }
    }
}

public class CUStd {
    public static func getMainUIWindow() -> UIWindow? {
        if let mainWindow = CUGlobal.mainWindow {
            return mainWindow
        }else {
            CUGlobal.mainWindow = UIApplication
                .shared
                .connectedScenes
                .flatMap { ($0 as? UIWindowScene)?.windows ?? [] }
                .first { $0.isKeyWindow }
            
            return CUGlobal.mainWindow
        }
    }
    public static func getArrayIndex<T: Identifiable>(_ array: [T], searchedObject: T) -> Int {
        var currentIndex: Int = 0
        for obj in array
        {
            if obj.id == searchedObject.id {
                return currentIndex
            }
            currentIndex += 1
        }
        return 0
    }
}
public struct CUGlobal {
    public static var mainWindow: UIWindow? = nil
}


extension UINavigationController: UIGestureRecognizerDelegate {
    override open func viewDidLoad() {
        super.viewDidLoad()
        interactivePopGestureRecognizer?.delegate = self
    }
    
    public func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        return viewControllers.count > 1
    }
}
