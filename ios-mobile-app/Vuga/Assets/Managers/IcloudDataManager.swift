//
//  IcloudDataManager.swift
//  Vuga
//
//

import Foundation
import SwiftUI

class CloudDataManager {
    static let shared = CloudDataManager()
    @AppStorage(SessionKeys.isPro) var isPro = false
    private let fileManager = FileManager.default


    func copyFileToCloud(url: URL, completion: @escaping (URL?, Error?) -> Void) {

        let uuid = UUID().uuidString
        let fileName = ("\(uuid)\(url.lastPathComponent)")

        guard isCloudEnabled() else {
            let localDocumentUrl = DocumentsDirectory.localDocumentsURL
            let destinationURL = localDocumentUrl.appendingPathComponent("\(uuid)\(url.lastPathComponent)")
            do {
                try fileManager.copyItem(at: url, to: destinationURL)
                completion(destinationURL, nil)
            } catch {
                completion(nil, error)
            }
            return
        }

        guard let iCloudDocumentsURL = DocumentsDirectory.iCloudDocumentsURL else {
            completion(nil, NSError(domain: "com.appexosolutions.PhotoVault", code: 1, userInfo: [NSLocalizedDescriptionKey: "iCloud Documents URL is nil"]))
            return
        }

         let localDocumentUrl = DocumentsDirectory.localDocumentsURL

        let destinationURL = isPro ? iCloudDocumentsURL.appendingPathComponent(fileName) : localDocumentUrl.appendingPathComponent(fileName)

        do {
            try fileManager.copyItem(at: url, to: destinationURL)
            completion(destinationURL, nil)
        } catch {
            completion(nil, error)
        }
    }

    func deleteFileFromCloud(url: URL) {
            do {
                try fileManager.removeItem(at: url)
            } catch {
                print(error.localizedDescription)
            }
        }

    func isCloudEnabled() -> Bool {
        return DocumentsDirectory.iCloudDocumentsURL != nil
    }
}
