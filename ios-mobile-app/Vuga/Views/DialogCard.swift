//
//  DialogCard.swift
//  Vuga
//
//

import Foundation
import SwiftUI


struct DialogCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    let icon: Image
    let title: String
    var iconColor: Color = .base
    let subTitle: String
    let buttonTitle: String
    var isLoading: Bool = false
    var onClose: ()->() = {}
    var onButtonTap: ()->() = {}
    
    var body: some View {
        ZStack(alignment: .topTrailing) {
            VStack {
                icon
                    .resizeFitTo(width: 60,height: 60, renderingMode: .template)
                    .foregroundColor(iconColor)
                Text(title.localized(language))
                    .outfitSemiBold(20)
                    .foregroundColor(.text)
                    .padding(.top,10)
                    .padding(.bottom,8)
                Text(subTitle.localized(language))
                    .outfitMedium(15)
                    .multilineTextAlignment(.center)
                    .foregroundColor(.textLight)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.bottom,12)
                if isLoading {
                    HStack {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(0.8)
                        Text("Deleting...")
                            .outfitMedium(16)
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 13)
                    .background(Color.base.opacity(0.7))
                    .cornerRadius(12)
                } else {
                    CommonButton(title: buttonTitle.localized(language),vPadding: 13,borderRadius: 12,fontSize: 16, onTap: onButtonTap)
                }
            }
            .padding(25)
            .background(Color.bg)
            .customCornerRadius(radius: 35)
            .padding(1)
            .background(Color.text.opacity(0.2))
            .customCornerRadius(radius: 36)
            .padding(.horizontal,10)
            .padding(.vertical,10)
            
            Image.close
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.base)
                .padding(7)
                .background(Color.bg)
                .clipShape(.circle)
                .padding(1)
                .background(Color.text.opacity(0.2))
                .clipShape(.circle)
                .onTap(completion: {
                    if !isLoading {
                        onClose()
                    }
                })
                .opacity(isLoading ? 0.5 : 1.0)
                .padding(5)
                .padding(.top,3)
                .padding(.leading,20)
        }
        .maxFrame()        
    }
}

struct DialogOptionCard: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    let heading: String
    let firstButtonTitle: String
    let secondButtonTitle: String = .deleteDownload
    let firstButtonIcon: Image
    let secondButtonIcon: Image = .delete
    var onFirstButtonTap: ()->() = {}
    var onSecondButtonTap: ()->() = {}
    var onClose: ()->() =  {}
    var progress: Float?
    var isForPauseDialog = false
    
    var body: some View {
        ZStack(alignment: .topTrailing) {
            VStack {
                Text(heading)
                    .outfitSemiBold(18)
                    .foregroundColor(.text)
                    .padding(.bottom,25)
                HStack {
                    if !isForPauseDialog {
                        firstButtonIcon
                            .resizeFitTo(size: 17,renderingMode: .template)
                            .foregroundColor(.text)
                    } else {
                        PieProgress(progress: progress ?? 0)
                            .frame(width: 18,height: 18)
                    }
                    Text(firstButtonTitle.localized(language))
                        .outfitSemiBold(14)
                        .foregroundColor(.text)
                }
                .padding(.vertical,13)
                .maxWidthFrame()
                .background(Color.base)
                .cornerRadius(radius: 13)
                .onTap(completion: onFirstButtonTap)
                
                HStack {
                    secondButtonIcon
                        .resizeFitTo(size: 20,renderingMode: .template)
                        .foregroundColor(.base)
                    Text(secondButtonTitle.localized(language))
                        .outfitSemiBold(14)
                        .foregroundColor(.base)
                }
                .padding(.vertical,13)
                .onTap(completion: onSecondButtonTap)

                
            }
            .padding([.horizontal,.top],25)
            .padding(.bottom,10)
            .background(Color.bg)
            .customCornerRadius(radius: 35)
            .padding(1)
            .background(Color.text.opacity(0.2))
            .customCornerRadius(radius: 36)
            .padding(.horizontal,10)
            .padding(.top,10)
            
            Image.close
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.base)
                .padding(7)
                .background(Color.bg)
                .clipShape(.circle)
                .padding(1)
                .background(Color.text.opacity(0.2))
                .clipShape(.circle)
                .onTap(completion: onClose)
                .padding(5)
                .padding(.top,3)
                .padding(.leading,20)
        }
        .maxFrame()        
    }
}

