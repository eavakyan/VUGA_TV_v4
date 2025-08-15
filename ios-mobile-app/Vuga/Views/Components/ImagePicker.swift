//
//  ImagePicker.swift
//  Vuga
//
//  Image picker wrapper for camera and photo library
//

import SwiftUI
import UIKit
import AVFoundation
import Photos

struct ImagePicker: UIViewControllerRepresentable {
    let sourceType: UIImagePickerController.SourceType
    let selectedImage: (UIImage) -> Void
    @Environment(\.presentationMode) private var presentationMode
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        
        // Check camera availability for camera source
        if sourceType == .camera {
            if !UIImagePickerController.isSourceTypeAvailable(.camera) {
                print("Camera not available on this device")
                // Fall back to photo library
                picker.sourceType = .photoLibrary
            } else {
                picker.sourceType = sourceType
            }
        } else {
            picker.sourceType = sourceType
        }
        
        picker.delegate = context.coordinator
        picker.allowsEditing = true // Allow user to crop image to square
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: ImagePicker
        
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            // Try to get edited image first, fallback to original
            if let image = info[.editedImage] as? UIImage {
                parent.selectedImage(image)
            } else if let image = info[.originalImage] as? UIImage {
                parent.selectedImage(image)
            }
            parent.presentationMode.wrappedValue.dismiss()
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.presentationMode.wrappedValue.dismiss()
        }
    }
}