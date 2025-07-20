//
//  SessionManager.swift
//  thumbsUp
//
//  Created by Aniket Vaddoriya on 11/04/22.
//

import Foundation

struct DownloadData: Hashable,Codable {
    var data: Data?
    var sourceId: String
    var sourceURL: URL
    var contentId: Int
    var episodeId: Int
    var destinationName: String
//    var downloadStatus: DownloadStatus
}

class SessionManager {
    static var shared = SessionManager()
    
    func setSetting(data: Setting){
        do {
            let data = try JSONEncoder().encode(data)
            let dataString = String(decoding: data, as: UTF8.self)
            setStringValue(value: dataString, key: "setting")
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    func getSetting() -> Setting? {
        let dataString = getStringValueForKey(key: "setting")
        let data = Data(dataString.utf8)
        if let loaded = try? JSONDecoder().decode(Setting.self, from: data) {
            return loaded
        }
        return nil
    }
    
    func setAds(data: [Admob]){
        do {
            let data = try JSONEncoder().encode(data)
            let dataString = String(decoding: data, as: UTF8.self)
            setStringValue(value: dataString, key: "ads")
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    func getAds() -> [Admob] {
        let dataString = getStringValueForKey(key: "ads")
        let data = Data(dataString.utf8)
        if let loaded = try? JSONDecoder().decode([Admob].self, from: data) {
            return loaded
        }
        return []
    }
    
    func setLanguages(data: [ContentLanguage]){
        do {
            let data = try JSONEncoder().encode(data)
            let dataString = String(decoding: data, as: UTF8.self)
            setStringValue(value: dataString, key: "languages")
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    func getLanguages() -> [ContentLanguage] {
        let dataString = getStringValueForKey(key: "languages")
        let data = Data(dataString.utf8)
        if let loaded = try? JSONDecoder().decode([ContentLanguage].self, from: data) {
            return loaded
        }
        return []
    }
    
    func setCustomAds(datum: [CustomAd]){
        do {
            let data = try JSONEncoder().encode(datum)
            UserDefaults.standard.set(data, forKey: "custom_ads")
        } catch let error {
            print(error.localizedDescription)
        }
    }
    
    func getCustomAds() -> [CustomAd] {
        if let data = UserDefaults.standard.data(forKey: "custom_ads"){
            if let loaded = try? JSONDecoder().decode([CustomAd].self, from: data){
                return loaded
            }
        }
        return []
    }
    
    
    func setDownloadData(datum: [DownloadData]){
        do {
            let data = try JSONEncoder().encode(datum)
            UserDefaults.standard.set(data, forKey: "download_data")
        } catch let error {
            print(error.localizedDescription)
        }
    }
    
    func getDownloadData() -> [DownloadData] {
        if let data = UserDefaults.standard.data(forKey: "download_data"){
            if let loaded = try? JSONDecoder().decode([DownloadData].self, from: data){
                return loaded
            }
        }
        return []
    }
    
    func setGenres(data: [Genre]){
        do {
            let data = try JSONEncoder().encode(data)
            let dataString = String(decoding: data, as: UTF8.self)
            setStringValue(value: dataString, key: "genres")
        } catch let err {
            print(err.localizedDescription)
        }
    }
    
    func getGenres() -> [Genre]{
        let dataString = getStringValueForKey(key: "genres")
        let data = Data(dataString.utf8)
        if let loaded = try? JSONDecoder().decode([Genre].self, from: data) {
            return loaded
        }
        return []
    }
    
    //MARK: Bool
    func getBooleanValueForKey(key: String) -> Bool {
        return UserDefaults.standard.bool(forKey: key)
    }
    func setBooleanValue(value: Bool, key: String) {
        UserDefaults.standard.set(value, forKey: key)
        UserDefaults.standard.synchronize()
    }
    
    //MARK:  String
    func getStringValueForKey(key: String) -> String {
        if let value = UserDefaults.standard.string(forKey: key) {
            return value
        }
        
        return ""
    }
    func setStringValue(value: String, key: String) {
        UserDefaults.standard.set(value, forKey: key)
        UserDefaults.standard.synchronize()
    }
    
    //MARK: Int
    func getIntegerValueForKey(key: String) -> Int {
        return UserDefaults.standard.integer(forKey: key)
    }
    func setIntegerValue(value: Int, key: String) {
        
        UserDefaults.standard.set(value, forKey: key)
        UserDefaults.standard.synchronize()
    }
    
    //MARK: Float
    func getFloatValueForKey(key: String) -> Float {
        return UserDefaults.standard.float(forKey: key)
    }
    func setFloatValue(value: Float, key: String) {
        UserDefaults.standard.set(value, forKey: key)
        UserDefaults.standard.synchronize()
    }
    
    
    //MARK: Clear
    func clear() {
        let domain = Bundle.main.bundleIdentifier!
        UserDefaults.standard.removePersistentDomain(forName: domain)
        UserDefaults.standard.synchronize()
    }
}
