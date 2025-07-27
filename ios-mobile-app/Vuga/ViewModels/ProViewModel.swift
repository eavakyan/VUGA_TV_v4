//
//  ProViewModel.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 10/06/24.
//

import Foundation
import RevenueCat
import SwiftUI

class ProViewModel: BaseViewModel {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @Published var selectedPackage : Package?
    @Published var allPackages = [Package]()

    func getOffering() {
        startLoading()
        Purchases.shared.getOfferings { (offerings, error) in
            self.stopLoading()
            if let offering = offerings?.current , error == nil {
                self.allPackages = offering.availablePackages
                self.selectedPackage = self.allPackages.first
            }
        }
    }
    
    func makePurchases() {
        startLoading()
        if let package = selectedPackage {
            Purchases.shared.purchase(package: package) { (transaction, customerInfo, error, userCancelled) in
                self.stopLoading()
                self.checkUserIsPro(customerInfo: customerInfo)
                if self.isPro {
                    Navigation.popToRootView()
//                    makeToast(title: .youHaveSuccessfullyBecomePro.localized(self.language))
                }
            }
        }
    }
    
    func restorePurchases() {
        startLoading()
        Purchases.shared.restorePurchases { customerInfo, error in
            self.stopLoading()
            self.checkUserIsPro(customerInfo: customerInfo)
            if self.isPro {
                Navigation.popToRootView()
//                makeToast(title: .youHaveSuccessfullyBecomePro.localized(self.language))
            }
        }
    }
    
    func passUserIdToRevenueCat() {
        startLoading()
        Purchases.shared.logIn("\(myUser?.id ?? 0)") { (customerInfo, created, error) in
            self.stopLoading()
            self.checkUserIsPro(customerInfo: customerInfo)
        }
    }
}
