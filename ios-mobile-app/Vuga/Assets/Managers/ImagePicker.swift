//
//  ImagePicker.swift
//  Vuga
//
//

import Foundation
import SwiftUI
import UIKit
import Photos

struct ImagePicker: UIViewControllerRepresentable {
    // Selected image handler used by both initializers
    private let selectedImageHandler: (UIImage) -> Void
    // Source for camera or photo library
    private let sourceType: UIImagePickerController.SourceType

    // Keep the legacy initializer for binding-based usage
    init(image: Binding<UIImage?>, sourceType: UIImagePickerController.SourceType = .photoLibrary) {
        self.sourceType = sourceType
        self.selectedImageHandler = { pickedImage in
            image.wrappedValue = pickedImage
        }
    }

    // New initializer to support closure-based usage
    init(sourceType: UIImagePickerController.SourceType = .photoLibrary, selectedImage: @escaping (UIImage) -> Void) {
        self.sourceType = sourceType
        self.selectedImageHandler = selectedImage
    }

    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: ImagePicker

        init(_ parent: ImagePicker) {
            self.parent = parent
        }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            // Prefer edited image if available
            if let image = info[.editedImage] as? UIImage {
                parent.selectedImageHandler(image)
            } else if let image = info[.originalImage] as? UIImage {
                parent.selectedImageHandler(image)
            }
            picker.dismiss(animated: true)
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            picker.dismiss(animated: true)
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        // Configure requested source type; fall back if camera unavailable
        if sourceType == .camera && !UIImagePickerController.isSourceTypeAvailable(.camera) {
            picker.sourceType = .photoLibrary
        } else {
            picker.sourceType = sourceType
        }
        picker.allowsEditing = true
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {
        // No dynamic updates needed
    }
}
