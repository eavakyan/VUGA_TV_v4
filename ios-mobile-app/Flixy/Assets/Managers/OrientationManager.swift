//
//  OrientationManager.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 04/06/24.
//

import Foundation
import UIKit
import Combine

class OrientationManager: ObservableObject {
    @Published var orientation: UIDeviceOrientation

    private var orientationChangePublisher: AnyCancellable?

    init() {
        self.orientation = UIDevice.current.orientation
        self.orientationChangePublisher = NotificationCenter.default.publisher(for: UIDevice.orientationDidChangeNotification)
            .sink { _ in
                self.orientation = UIDevice.current.orientation
            }
    }
}
