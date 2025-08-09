//
//  TVShowsDownloadView.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 11/07/24.
//

import SwiftUI

struct TVShowsDownloadView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @SectionedFetchRequest private var downloads: SectionedFetchResults<String, DownloadContent>
    var content : DownloadContent
    init(content: DownloadContent) {
        self.content = content
        let currentProfileId = SessionManager.shared.currentProfile?.profileId ?? 0
        let tvShowsSection = SectionedFetchRequest<String,DownloadContent>(sectionIdentifier: \.seasonNo!, sortDescriptors: [SortDescriptor(\.seasonNo,order: .forward)],predicate: NSPredicate(format: "contentId == %@ AND profileId == %d", content.contentId ?? "", currentProfileId))
        self._downloads = tvShowsSection
    }
    var body: some View {
        VStack {
            BackBarView(title: content.name ?? "")
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading,spacing: 12) {
                    ForEach(downloads) { download in
                        Text("Season \(download.first?.seasonNo ?? "")")
                            .outfitSemiBold(20)
                            .foregroundColor(Color("textColor"))
                            .padding(.horizontal)
                        ForEach(download) { down in
                                DownloadCardView(isForTVShowsView: true, content: down)
                                    .padding(.horizontal,5)
                        }
                    }
                }
                .padding(.vertical,12)
            }
        }
        .addBackground()
        .noDataFound(downloads.isEmpty)
    }
}

