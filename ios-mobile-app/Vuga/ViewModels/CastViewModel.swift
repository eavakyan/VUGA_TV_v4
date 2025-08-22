//
//  CastViewModel.swift
//  Vuga
//
//

import Foundation

class CastViewModel: BaseViewModel {
    @Published var isInformationSheetOpen = false
    @Published var actor : ActorDetail?
    
    @Published var isDataLoaded = false

    
    func fetchActorDetail(actorId: Int) {
        let params: [Params: Any] = [.actorId: actorId]
        startLoading()
        NetworkManager.callWebService(url: .fetchActorDetails, params: params) { [weak self] (obj: ActorDetailModel) in
            self?.stopLoading()
            if let data = obj.data {
                self?.actor = data
                self?.isDataLoaded = true
            }
        }
    }
}
