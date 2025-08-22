//
//  ProView.swift
//  Vuga
//
//

import SwiftUI
// import RevenueCat - Disabled temporarily

struct ProView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject var vm = ProViewModel()
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                SimpleBackButton(onTap: {
                    Navigation.pop()
                }, iconSize: 15)
                Spacer()
                Text(String.restore.localized(language))
                    .outfitMedium(16)
                    .foregroundColor(.textLight)
                    .onTap {
                        vm.restorePurchases()
                    }
            }
            .padding(.trailing)
            .padding(.leading,8)
            ScrollView(showsIndicators: false) {
                VStack {
                    Image.logoHorizontal
                        .resizeFitTo(width: Device.width / 3, height: 35)
                    HStack {
                        Text(String.subscribeTo.localized(language))
                            .outfitMedium(16)
                            .foregroundColor(.text)
                        Text(String.pro.localized(language))
                            .outfitBold(16)
                            .foregroundColor(.base)
                    }
                    .padding(.top)
                    Text(String.subscribeToDes.localized(language))
                        .outfitRegular(14)
                        .foregroundColor(.textLight)
                        .multilineTextAlignment(.center)
                        .padding(.top,1)
                        .padding(.bottom,30)
                        .frame(width: 250)
                    VStack(alignment: .leading) {
                        HStack(spacing: 4) {
                            Text(String.whyGoWithPro.localized(language))
                                .outfitMedium(16)
                                .foregroundColor(.text)
                            Text(String.proQ.localized(language))
                                .outfitBold(16)
                                .foregroundColor(.base)
                            Spacer()
                        }
                        PurchaseBenefitsField(title: .fullyAdFree)
                        PurchaseBenefitsField(title: .accessToAllPremium)
                        PurchaseBenefitsField(title: .technicalSupport)
                        PurchaseBenefitsField(title: .cancelAnyTime)
                    }
                    .padding(.bottom)
                    .padding(.horizontal,20)
                    VStack(spacing: 4) {
                        // RevenueCat disabled - no packages to display
                        Text("In-app purchases are currently disabled")
                            .outfitRegular(14)
                            .foregroundColor(.textLight)
                            .padding()
                    }
                    Text(String.proDes.localized(language))
                        .outfitRegular(10)
                        .foregroundColor(.textLight)
                        .multilineTextAlignment(.center)
                        .padding(.vertical)
                    
                }
                .padding(.top,40)
            }
            .padding(.horizontal)
            CommonButton(title: .subscribe, onTap: {
                vm.makePurchases()
            })
            .padding([.horizontal,.bottom])

        }
        .loaderView(vm.isLoading)
        .hideNavigationbar()
        .onAppear(perform: {
            vm.getOffering()
        })
    }
}

#Preview {
    ProView()
}

struct PurchaseBenefitsField: View {
    let title: String
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var body: some View {
        HStack{
            Image.checkbox
                .resizeFitTo(size: 16,renderingMode: .template)
                .foregroundColor(.text)
            Text(title.localized(language))
                .outfitRegular(12)
                .foregroundColor(.text)
        }
    }
}

// RevenueCat disabled - PurchaseOptionCard commented out
/*
struct PurchaseOptionCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var package : Package
    var isSelected = false
    var isMonthlyCard = false
    
    var body: some View {
        HStack() {
            HStack {
                VStack(alignment: .leading) {
                    Text(package.storeProduct.localizedTitle)
                        .outfitBold(18)
                        .foregroundColor(.text)
                    Text(isMonthlyCard ? String.monthPurchaseCardDes.localized(language) : String.yearPurchaseCardDes.localized(language))
                        .outfitRegular(13)
                        .foregroundColor(.textLight)
                        .multilineTextAlignment(.leading)
                        .lineLimit(2)
                }
                Spacer(minLength: 10)
                Text(package.storeProduct.localizedPriceString)
                    .outfitBold(23)
                    .foregroundColor(isSelected ? Color.base : Color.textLight)
                    .frame(width: 120,alignment: .trailing)
            }
            .padding(.horizontal,15)
        }
        .padding(.vertical)
        .maxWidthFrame()
        .overlay(
            RoundedRectangle(cornerRadius: 18,style: .continuous)
                .stroke(isSelected ?  Color.base : Color.stroke, lineWidth: 1.2)
        )
        .padding(.vertical,8)
        .padding(.horizontal,5)
    }
}
*/
