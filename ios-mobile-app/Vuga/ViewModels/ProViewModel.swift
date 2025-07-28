//
//  ProViewModel.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 10/06/24.
//

import Foundation
// import RevenueCat - Disabled temporarily
import SwiftUI

class ProViewModel: BaseViewModel {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @Published var selectedPackage : Any? // Changed from Package
    @Published var allPackages = [Any]() // Changed from [Package]

    func getOffering() {
        // RevenueCat disabled - no packages available
        stopLoading()
    }
    
    func makePurchases() {
        // RevenueCat disabled - cannot make purchases
        stopLoading()
    }
    
    func restorePurchases() {
        // RevenueCat disabled - cannot restore purchases
        stopLoading()
    }
    
    func passUserIdToRevenueCat() {
        // RevenueCat disabled
        stopLoading()
    }
}
