//
//  GenreContentsViewModel.swift
//  Vuga
//
//

import Foundation

class GenreContestViewModel : BaseViewModel {
    @Published var contents = [VugaContent]()
    
    func fetchContests(genreId: Int?){
        let params: [Params: Any] = [.genreId : genreId ?? 0, .start: contents.count, .limit: Limits.pagination]
        startLoading()
        NetworkManager.callWebService(url: .fetchContentsByGenre, params: params) { [weak self] (obj: ContentsModel) in
            self?.stopLoading()
            self?.contents.append(contentsOf: obj.data ?? [])
        }
    }
}

