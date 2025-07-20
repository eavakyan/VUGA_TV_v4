//
//  GenreContentsViewModel.swift
//  Flixy
//
//  Created by Aniket Vaddoriya on 22/05/24.
//

import Foundation

class GenreContestViewModel : BaseViewModel {
    @Published var contents = [FlixyContent]()
    
    func fetchContests(genreId: Int?){
        let params: [Params: Any] = [.genreId : genreId ?? 0, .start: contents.count, .limit: Limits.pagination]
        startLoading()
        NetworkManager.callWebService(url: .fetchContentsByGenre, params: params) { [weak self] (obj: ContentsModel) in
            self?.stopLoading()
            self?.contents.append(contentsOf: obj.data ?? [])
        }
    }
}

