//
//  FontExtention.swift
//  service
//
//

import Foundation
import SwiftUI

//MARK: -  My Fonts
struct MyFont {
    static var OutfitBlack             = "Outfit-Black"
    static var OutfitBold              = "Outfit-Bold"
    static var OutfitLight             = "Outfit-Light"
    static var OutfitMedium            = "Outfit-Medium"
    static var OutfitRegular           = "Outfit-Regular"
    static var OutfitThin              = "Outfit-Thin"
    static var OutfitExtraBold         = "Outfit-ExtraBold"
    static var OutfitExtraLight        = "Outfit-ExtraLight"
    static var OutfitSemiBold          = "Outfit-SemiBold"
    static var latoBold                = "Lato-Bold"
    static var sfUi                    = "SF-UI-Display-Heavy"
}

extension Text {
    func outfitBlack(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitBlack,size: CGFloat(size)))
    }
    func outfitBold(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitBold,size: CGFloat(size)))
    }
    func outfitLight(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitLight,size: CGFloat(size)))
    }
    func outfitMedium(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitMedium,size: CGFloat(size)))
    }
    func outfitRegular(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitRegular,size: CGFloat(size)))
    }
    func outfitThin(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitThin,size: CGFloat(size)))
    }
    func outfitExtraBold(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitExtraBold,size: CGFloat(size)))
    }
    func outfitExtraLight(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitExtraLight,size: CGFloat(size)))
    }
    func outfitSemiBold(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitSemiBold,size: CGFloat(size)))
    }
}


extension View {
    func outfitBlack(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitBlack,size: CGFloat(size)))
    }
    func outfitBold(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitBold,size: CGFloat(size)))
    }
    func outfitLight(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitLight,size: CGFloat(size)))
    }
    func outfitMedium(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitMedium,size: CGFloat(size)))
    }
    func outfitRegular(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitRegular,size: CGFloat(size)))
    }
    func outfitThin(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitThin,size: CGFloat(size)))
    }
    func outfitExtraBold(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitExtraBold,size: CGFloat(size)))
    }
    func outfitExtraLight(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitExtraLight,size: CGFloat(size)))
    }
    func outfitSemiBold(_ size : CGFloat = 16) -> some View {
        return self.font(Font.custom(MyFont.OutfitSemiBold,size: CGFloat(size)))
    }
}
