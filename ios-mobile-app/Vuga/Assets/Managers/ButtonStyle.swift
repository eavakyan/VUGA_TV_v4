//
//  ButtonStyle.swift
//  ToDoList
//
//  Created by Aniket Vaddoriya on 31/08/22.
//

import Foundation
import SwiftUI


struct MyPlainButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .opacity(1)
    }
}

extension ButtonStyle where Self == MyPlainButtonStyle  {
    static var myPlain: Self {
        return .init()
    }
}

extension View {
    func myButtonStyle() -> some View {
        self
    }
}
