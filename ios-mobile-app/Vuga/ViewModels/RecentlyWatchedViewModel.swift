//
//  RecentlyWatchedViewModel.swift
//  Vuga
//
//  ViewModel for fetching Recently Watched content from API
//

import Foundation
import CoreData
import SwiftUI

class RecentlyWatchedViewModel: ObservableObject {
    @Published var recentlyWatchedContents: [RecentlyWatchedContent] = []
    @Published var isLoading = false
    
    private let context = DataController.shared.context
    
    func fetchRecentlyWatchedFromAPI() {
        print("DEBUG: fetchRecentlyWatchedFromAPI called")
        
        // First, fetch local data from Core Data
        let fetchRequest: NSFetchRequest<RecentlyWatched> = RecentlyWatched.fetchRequest()
        fetchRequest.sortDescriptors = [NSSortDescriptor(keyPath: \RecentlyWatched.date, ascending: false)]
        
        do {
            let localRecentlyWatched = try context.fetch(fetchRequest)
            print("DEBUG: Found \(localRecentlyWatched.count) items in Core Data")
            
            // Get unique content IDs
            let uniqueContents = Dictionary(grouping: localRecentlyWatched, by: { $0.contentID })
                .compactMap { _, items in
                    items.sorted { (item1, item2) in
                        let date1 = item1.date ?? Date.distantPast
                        let date2 = item2.date ?? Date.distantPast
                        return date1 > date2
                    }.first
                }
                .sorted(by: { $0.date! > $1.date! })
            
            // Extract content IDs
            let contentIds = uniqueContents.map { Int($0.contentID) }
            print("DEBUG: Unique content IDs: \(contentIds)")
            
            if contentIds.isEmpty {
                print("DEBUG: No content IDs found, clearing recently watched")
                self.recentlyWatchedContents = []
                return
            }
            
            // Fetch content details from API
            fetchContentDetails(contentIds: contentIds, localData: uniqueContents)
            
        } catch {
            print("Failed to fetch recently watched: \(error)")
        }
    }
    
    private func fetchContentDetails(contentIds: [Int], localData: [RecentlyWatched]) {
        print("DEBUG: fetchContentDetails called with \(contentIds.count) IDs")
        isLoading = true
        
        var params: [String: Any] = [
            "content_ids": contentIds
        ]
        
        if let userId = SessionManager.shared.currentUser?.id {
            params["user_id"] = userId
            print("DEBUG: Using user_id: \(userId)")
        }
        
        if let profileId = SessionManager.shared.currentProfile?.profileId {
            params["profile_id"] = profileId
            print("DEBUG: Using profile_id: \(profileId)")
        }
        
        // Make API call to fetch content details
        let urlString = "\(WebService.apiBase)content/by-ids"
        print("DEBUG: API URL: \(urlString)")
        guard let url = URL(string: urlString) else { 
            print("DEBUG: Failed to create URL")
            return 
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Add API key header if needed
        request.setValue(WebService.headerValue, forHTTPHeaderField: WebService.headerKey)
        
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: params)
            request.httpBody = jsonData
            print("DEBUG: Request body: \(String(data: jsonData, encoding: .utf8) ?? "nil")")
        } catch {
            print("DEBUG: Failed to serialize params: \(error)")
            return
        }
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                
                if let error = error {
                    print("API Error: \(error)")
                    return
                }
                
                if let httpResponse = response as? HTTPURLResponse {
                    print("DEBUG: HTTP Status Code: \(httpResponse.statusCode)")
                }
                
                guard let data = data else { 
                    print("DEBUG: No data received")
                    return 
                }
                
                print("DEBUG: Received data: \(String(data: data, encoding: .utf8) ?? "nil")")
                
                do {
                    let decoder = JSONDecoder()
                    let response = try decoder.decode(RecentlyWatchedResponse.self, from: data)
                    
                    print("DEBUG: Decoded response - status: \(response.status), data count: \(response.data?.count ?? 0)")
                    
                    if response.status, let apiData = response.data {
                        // Map API data with local data
                        self?.mapRecentlyWatchedContent(apiContents: apiData, localData: localData)
                    } else {
                        print("DEBUG: API response status false or no data")
                    }
                } catch {
                    print("Decoding error: \(error)")
                    // Try to print raw response for debugging
                    if let jsonString = String(data: data, encoding: .utf8) {
                        print("DEBUG: Raw response: \(jsonString)")
                    }
                }
            }
        }.resume()
    }
    
    private func mapRecentlyWatchedContent(apiContents: [RecentlyWatchedAPIContent], localData: [RecentlyWatched]) {
        print("DEBUG: mapRecentlyWatchedContent called with \(apiContents.count) API items and \(localData.count) local items")
        var mappedContents: [RecentlyWatchedContent] = []
        
        for local in localData {
            // Find matching API content
            guard let apiContent = apiContents.first(where: { $0.contentId == Int(local.contentID) }) else {
                print("DEBUG: No API content found for local content ID: \(local.contentID)")
                continue
            }
            
            // Extract episode info if it's a series
            var episodeInfo: EpisodeInfo?
            if local.contentType == Int16(ContentType.series.rawValue) && local.episodeId > 0 {
                // Find the episode in seasons
                for (seasonIndex, season) in (apiContent.seasons ?? []).enumerated() {
                    if let episode = season.episodes?.first(where: { $0.id == Int(local.episodeId) }) {
                        episodeInfo = EpisodeInfo(
                            episodeId: episode.id ?? 0,
                            episodeTitle: episode.title ?? "",
                            episodeThumbnail: episode.thumbnail,
                            seasonNumber: seasonIndex + 1, // Season number based on index
                            episodeNumber: episode.number
                        )
                        break
                    }
                }
            }
            
            // Debug: Log duration data
            print("DEBUG: Content \(apiContent.contentName) - Duration from API: \(apiContent.duration ?? -1)")
            
            let recentlyWatched = RecentlyWatchedContent(
                contentId: apiContent.contentId,
                contentName: apiContent.contentName,
                horizontalPoster: apiContent.horizontalPoster,
                verticalPoster: apiContent.verticalPoster,
                contentType: apiContent.contentType,
                releaseYear: apiContent.releaseYear,
                ratings: apiContent.ratings,
                contentDuration: apiContent.duration,
                genres: apiContent.genres,
                episodeInfo: episodeInfo,
                watchedDate: local.date ?? Date(),
                progress: local.progress,
                totalDuration: local.totalDuration,
                episodeId: Int(local.episodeId),
                contentSourceId: Int(local.contentSourceId),
                contentSourceType: Int(local.contentSourceType),
                sourceUrl: local.sourceUrl ?? "",
                isForDownload: local.isForDownload
            )
            
            mappedContents.append(recentlyWatched)
        }
        
        // Sort mapped contents by watched date (latest first)
        mappedContents.sort { $0.watchedDate > $1.watchedDate }
        
        print("DEBUG: Mapped \(mappedContents.count) contents")
        self.recentlyWatchedContents = mappedContents
        print("DEBUG: Updated recentlyWatchedContents with \(self.recentlyWatchedContents.count) items")
    }
}