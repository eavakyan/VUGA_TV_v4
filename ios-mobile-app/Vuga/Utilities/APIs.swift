//
//  APIs.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import Foundation

enum APIs : String {
    case userRegistration                   = "userRegistration"
    case updateProfile                      = "updateProfile"
    case fetchProfile                       = "fetchProfile"
    case logOut                             = "logOut"
    case getUserSubscription                = "getUserSubscription"
    case deleteMyAccount                    = "deleteMyAccount"
    
    case fetchCustomAds                     = "fetchCustomAds"
    case increaseAdView                     = "increaseAdView"
    case increaseAdClick                    = "increaseAdClick"
    case increaseAdMetric                   = "increaseAdMetric"
    
    case increaseContentView                = "increaseContentView"
    case increaseContentDownload            = "increaseContentDownload"
    case increaseContentShare               = "increaseContentShare"
    case increaseEpisodeView                = "increaseEpisodeView"
    case increaseEpisodeDownload            = "increaseEpisodeDownload"
    case fetchHomePageData                  = "fetchHomePageData"
    case fetchWatchList                     = "fetchWatchList"
    case toggleWatchlist                    = "user/toggle-watchlist"
    case toggleFavorite                     = "user/toggle-favorite"
    case fetchContentsByGenre               = "fetchContentsByGenre"
    case fetchContentDetails                = "fetchContentDetails"
    case searchContent                      = "searchContent"
    
    case fetchActorDetails                  = "fetchActorDetails"
    
    case fetchLiveTVPageData                = "fetchLiveTVPageData"
    case fetchTVChannelByCategory           = "fetchTVChannelByCategory"
    case searchTVChannel                    = "searchTVChannel"
    case increaseTVChannelView              = "increaseTVChannelView"
    case increaseTVChannelShare             = "increaseTVChannelShare"
    
    case fetchSettings                      = "fetchSettings"
    case getAllNotification                 = "getAllNotification"
    
    // TV Authentication
    case tvAuthAuthenticate                 = "TV/authenticateSession"
    
    // Profile Management
    case getUserProfiles                    = "getUserProfiles"
    case createProfile                      = "createProfile"
    case deleteProfile                      = "deleteProfile"
    case selectProfile                      = "selectProfile"
}

extension String {
    func addBaseURL() -> URL? {
        URL(string: WebService.itemBaseURL + self)
    }
}

extension String {
    func addCommonURL() -> URL? {
        URL(string: WebService.itemBaseURLs + self)
    }
}

extension Optional<String> {
    func addBaseURL() -> URL? {
        URL(string: WebService.itemBaseURL + (self ?? ""))
    }
}
