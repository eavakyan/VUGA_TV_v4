//
//  NetworkManager.swift
//  Woomeout
//
//  Created by Aniket Vaddoriya on 24/11/23.
//

import SwiftUI
import Alamofire

struct NetworkManager {
    static let headers = HTTPHeaders([WebService.headerKey: WebService.headerValue])
    static func cancelRequest(url: String) {
        AF.session.getAllTasks { tasks in
            let task = tasks.first(where: { $0.currentRequest?.url?.absoluteString == url })
            task?.cancel()
        }
    }
    
    static func cancelAllRequests(){
        AF.cancelAllRequests()
    }
    
    static func callWebService<T : Codable>(url : APIs,
                                            httpMethod : HTTPMethod = .post,
                                            params: [Params: Any] = [:],
                                            encoding: ParameterEncoding = URLEncoding.default,
                                            callbackSuccess : @escaping (T) -> (),
                                            callbackFailure : @escaping (_ err : Error) -> () = {_ in}) {
        
        let convertedParams = params.toSimpleDict()
        AF.request(WebService.apiBase + url.rawValue, method: httpMethod, parameters: convertedParams, encoding: encoding, headers: headers)
            .responseString { response in
                print("==================================================================")
                print(response.request ?? "")  // original URL request
                print(response.request?.headers ?? "")  // original URL request
                print("Time Duration in second:", response.metrics?.taskInterval.duration ?? 0)  // Duration of request
                print("parameters = \(String(describing: convertedParams))")
                print("header = \(String(describing: headers))")
                response.value.decode(callbackSuccess: callbackSuccess, callbackFailure: callbackFailure)
            }
    }
    
    static func callWebServiceWithFiles<T : Codable>(url: APIs,
                                              params:[Params: Any] = [:],
                                              callbackSuccess : @escaping (T) -> (),
                                              callbackFailure : @escaping (_ err : Error) -> () = {_ in}) {
        AF.upload(multipartFormData: { multipartFormData in
            params.forEach { (key, value) in
                if let values = value as? [URL] {
                    values.forEach { value in
                        multipartFormData.append(value, withName: key.rawValue,fileName: "video.\(value.pathExtension)", mimeType: value.pathExtension)
                    }
                } else if let values = value as? [UIImage]{
                    values.forEach { uiImage in
                        if let data = uiImage.jpegData(compressionQuality: 1) {
                            multipartFormData.append(data, withName: key.rawValue,fileName: "image.jpg", mimeType: "image/jpg")
                        }
                    }
                } else {
                    multipartFormData.append("\(value)".data(using: String.Encoding.utf8)!, withName: key.rawValue)
                }
            }
            print(multipartFormData)
        }, to: WebService.apiBase + url.rawValue,headers: headers)
        .uploadProgress { progress in
            print(progress)
        }
        .responseString { response in
            print("==================================================================")
            print(response.request ?? "")  // original URL request
            print("Time Duration in second:", response.metrics?.taskInterval.duration ?? 0)  // Duration of request
            print("parameters = \(String(describing: params))")
            response.value.decode(callbackSuccess: callbackSuccess, callbackFailure: callbackFailure)
        }
    }
    
//    static func callOtherAPI<T : Codable>(url : String,
//                             httpMethod : HTTPMethod = .post,
//                             params: [String: Any] = [:],
//                             callbackSuccess : @escaping (T) -> (),
//                             callbackFailure : @escaping (_ err : Error) -> () = {_ in}) {
//        AF.request(url, method: httpMethod, parameters: params, headers: HTTPHeaders())
//            .responseString { response in
//                print("==================================================================")
//                print(response.request ?? "")  // original URL request
//                print("Time Duration in second:", response.metrics?.taskInterval.duration ?? 0)  // Duration of request
//                print("parameters = \(String(describing: params))")
//                response.value.decode(callbackSuccess: callbackSuccess, callbackFailure: callbackFailure)
//            }
//    }
}

extension Dictionary where Key == Params {
    func toSimpleDict() -> [String: Any] {
        var convertedParams: [String: Any] = [:]
        
        for (key, value) in self {
            convertedParams[key.rawValue] = value
        }
        return convertedParams
    }
}


extension Optional where Wrapped == String {
    func decode<T : Codable>(callbackSuccess : @escaping (T) -> (),callbackFailure : @escaping (_ err : Error) -> ()){
        guard let value = self else {
            return
        }
        if let data = value.data(using: .utf8) {
            data.decode(callbackSuccess: callbackSuccess, callbackFailure: callbackFailure)
        }
    }
}

extension Data {
    func decode<T : Codable>(callbackSuccess : @escaping (T) -> (),callbackFailure : @escaping (_ err : Error) -> ()){
        let decoder = JSONDecoder()
        do {
            print("success: \(String(describing: self.parseJSONString))")
            let jsonData = try decoder.decode(T.self, from: self)
            callbackSuccess(jsonData)
        } catch let DecodingError.dataCorrupted(context) {
            print(context)
        } catch let DecodingError.keyNotFound(key, context) {
            print("Key '\(key)' not found:", context.debugDescription)
            print("codingPath:", context.codingPath)
        } catch let DecodingError.valueNotFound(value, context) {
            print("Value '\(value)' not found:", context.debugDescription)
            print("codingPath:", context.codingPath)
        } catch let DecodingError.typeMismatch(type, context)  {
            print("Type '\(type)' mismatch:", context.debugDescription)
            print("codingPath:", context.codingPath)
        } catch {
            print("error: ", error)
        }
    }
    
    var parseJSONString: AnyObject? {
        let json = try? JSONSerialization.jsonObject(with: self, options: .mutableContainers) as AnyObject
        return json
    }
}
