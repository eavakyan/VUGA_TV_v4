//
//  GenreContentsView.swift
//  Vuga
//
//

import SwiftUI

struct GenreContentsView: View {
    @StateObject var vm = GenreContestViewModel()
    var genre: Genre
    var body: some View {
        VStack(spacing: 0) {
            BackBarView(title: genre.title ?? "")
            Divider()
            ScrollView(showsIndicators: false) {
                LazyVStack {
                    ForEach(vm.contents, id: \.id) { content in
                        ContentHorizontalCard(content: content)
                        .onAppear(perform: {
                            if content.id == vm.contents.last?.id ?? 0 {
                                vm.fetchContests(genreId: genre.id)
                            }
                        })
                    }
                }
                .padding(10)
            }
            .refreshable {
                vm.fetchContests(genreId: genre.id)
            }
        }
        .addBackground()
        .onAppear(perform: {
            vm.fetchContests(genreId: genre.id)
        })
        .loaderView(vm.isLoading && vm.contents.isEmpty)
    }
}

