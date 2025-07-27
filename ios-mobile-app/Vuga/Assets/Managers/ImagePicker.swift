//
//  ImagePicker.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 30/05/24.
//

import Foundation
import SwiftUI

struct ImagePicker : UIViewControllerRepresentable {
    @Binding var image : UIImage?
    
    class Coordinator : NSObject,UIImagePickerControllerDelegate,UINavigationControllerDelegate {
        var parent : ImagePicker
        
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let uiImage = info[.originalImage] as? UIImage{
                parent.image = uiImage
            }
            picker.dismiss(animated: true)
        }
    }
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {
        //
    }
   
}
