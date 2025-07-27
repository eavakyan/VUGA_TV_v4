//
//  APIClient.swift
//  Flixy
//
//  Created by TV Authentication on 2025-07-26.
//

import Foundation
import Alamofire
import SwiftUI

class APIClient {
    static let shared = APIClient()
    @AppStorage(SessionKeys.myUser) var myUser: User? = nil
    
    private init() {}
    
    func authenticateTVSession(sessionToken: String, completion: @escaping (Bool, String?) -> Void) {
        let params: [Params: Any] = [
            .sessionToken: sessionToken,
            .userId: myUser?.id ?? 0
        ]
        
        NetworkManager.callWebService(
            url: .tvAuthAuthenticate,
            httpMethod: .post,
            params: params,
            encoding: URLEncoding.default,
            callbackSuccess: { (response: AuthResponse) in
                if response.status == true {
                    completion(true, response.message)
                } else {
                    completion(false, response.message ?? "Authentication failed")
                }
            },
            callbackFailure: { error in
                completion(false, "Network error: \(error.localizedDescription)")
            }
        )
    }
}

// Response model for TV authentication
struct AuthResponse: Codable {
    let status: Bool?
    let message: String?
}