//
//  APIs.swift
//  Vuga
//
//  Created by Aniket Vaddoriya on 07/05/24.
//

import Foundation

enum APIs : String {
    case userRegistration                   = "userRegistration"
    case updateProfile                      = "profile/update"
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
    case rateContent                        = "user/rate-content"
    case rateEpisode                        = "user/rate-episode"
    case updateWatchProgress                = "watch/update-progress"
    case getContinueWatching                = "watch/continue-watching"
    case markAsCompleted                    = "watch/mark-completed"
    case getWatchHistory                    = "user/watch-history"
    case fetchContentsByGenre               = "fetchContentsByGenre"
    case fetchContentDetails                = "fetchContentDetails"
    case fetchContentsByIds                 = "fetchContentsByIds"
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
    case updateAgeSettings                  = "profile/update-age-settings"
    case getAgeRatings                      = "profile/age-ratings"
    case uploadProfileAvatar                = "profiles/avatar/upload"
    case removeProfileAvatar                = "profiles/avatar/remove"
    
    // Subscription APIs
    case getSubscriptionPlans               = "subscription/plans"
    case getMySubscriptions                 = "subscription/my-subscriptions"
    case validatePromoCode                  = "subscription/validate-promo"
    
    // User Notification APIs (in-app notifications)
    case getPendingNotifications            = "user-notification/pending"
    case markNotificationShown              = "user-notification/mark-shown"
    case dismissNotification                = "user-notification/dismiss"
    
    // Popup System removed - files deleted
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
