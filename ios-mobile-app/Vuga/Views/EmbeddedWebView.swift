//
//  EmbeddedWebView.swift
//  Vuga
//
//

import Foundation
import SwiftUI

struct EmbeddedWebView : View {
    var url: URL
    var body: some View {
        ZStack(alignment: .topLeading) {
            EmbeddedWeb(url: url)
                .ignoresSafeArea()
            BackButton()
                .padding(.horizontal)
        }
        .hideNavigationbar()
    }
}
