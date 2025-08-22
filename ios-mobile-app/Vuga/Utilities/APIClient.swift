//
//  APIClient.swift
//  Vuga
//
//

import Foundation
import Alamofire
import SwiftUI

class APIClient {
    static let shared = APIClient()
    @AppStorage(SessionKeys.myUser) var myUser: User? = nil
    
    private init() {}
    
    func authenticateTVSession(sessionToken: String, completion: @escaping (Bool, String?) -> Void) {
        print("APIClient: Starting TV authentication")
        print("APIClient: Session token: \(sessionToken)")
        print("APIClient: User ID: \(myUser?.id ?? 0)")
        
        let params: [Params: Any] = [
            .sessionToken: sessionToken,
            .userId: myUser?.id ?? 0
        ]
        
        print("APIClient: Calling API endpoint: \(APIs.tvAuthAuthenticate.rawValue)")
        
        NetworkManager.callWebService(
            url: .tvAuthAuthenticate,
            httpMethod: .post,
            params: params,
            encoding: URLEncoding.default,
            callbackSuccess: { (response: AuthResponse) in
                print("APIClient: Success response - status: \(response.status), message: \(response.message ?? "nil")")
                if response.status == true {
                    completion(true, response.message)
                } else {
                    completion(false, response.message ?? "Authentication failed")
                }
            },
            callbackFailure: { error in
                print("APIClient: API call failed with error: \(error.localizedDescription)")
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