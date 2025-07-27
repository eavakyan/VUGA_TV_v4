//
//  Function.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import SwiftUI
import AVKit

struct Function {
    static let shared = Function()
    
    func haptic() {
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
    }
}

func makeToast(title : String ,complition: @escaping ()->() = {}) {
    let attributedString = NSAttributedString(string: title, attributes: [
        NSAttributedString.Key.font : UIFont(name: MyFont.OutfitMedium, size: 14) ?? UIFont.systemFont(ofSize: 16), 
        NSAttributedString.Key.foregroundColor : UIColor.black
    ])
    let alert = UIAlertController(title: title, message: nil,  preferredStyle: .actionSheet)
    alert.setValue(attributedString, forKey: "attributedTitle")
    UIApplication.shared.keyWindowPresentedController?.present(alert, animated: true, completion: {
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            UIApplication.shared.keyWindowPresentedController?.dismiss(animated: true, completion: {
                complition()
            })
        }
    })
}
