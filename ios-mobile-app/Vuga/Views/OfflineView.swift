//
//  OfflineView.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 11/06/24.
//

import Foundation
import SwiftUI

struct OfflineView : View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                LottieView(filename: "NoInternet")
                    .frame(width: Device.width - 40)
                Text(String.noConnection.localized(language))
                    .outfitSemiBold(22)
                    .foregroundColor(.text)
                Text(String.noConnectionDes.localized(language))
                    .outfitMedium(15)
                    .foregroundColor(.textLight)
                    .multilineTextAlignment(.center)
                NavigationLink(destination: {
                    DownloadView(isOffLineView: true)
                }, label: {
                    Text(String.goToDownloads.localized(language))
                        .outfitMedium(20)
                        .foregroundColor(.text)
                        .padding(.vertical,18)
                        .maxWidthFrame()
                        .background(Color.base)
                        .cornerRadius(radius: 16)
                })
                .buttonStyle(.myPlain)
                .padding(.vertical)
            }
            .padding()
            .addBackground()
        }
    }
}

#Preview {
    OfflineView()
}

