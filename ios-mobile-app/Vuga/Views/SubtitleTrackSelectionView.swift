//
//  SubtitleTrackSelectionView.swift
//  Vuga
//
//  Created by Claude on Today
//

import SwiftUI

struct SubtitleTrackSelectionView: View {
    let tracks: [SubtitleTrack]
    @Binding var selectedTrack: SubtitleTrack?
    let onTrackSelected: (SubtitleTrack?) -> Void
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            List {
                // Off option
                HStack {
                    Text("Off")
                        .font(.headline)
                    
                    Spacer()
                    
                    if selectedTrack == nil {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(.blue)
                    }
                }
                .contentShape(Rectangle())
                .onTapGesture {
                    onTrackSelected(nil)
                }
                .padding(.vertical, 4)
                
                // Available subtitle tracks
                ForEach(tracks, id: \.id) { track in
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(track.title ?? track.languageCode ?? "Unknown")
                                .font(.headline)
                            if track.isForced == true {
                                Text("Forced")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        
                        Spacer()
                        
                        if selectedTrack?.id == track.id {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(.blue)
                        }
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        onTrackSelected(track)
                    }
                    .padding(.vertical, 4)
                }
            }
            .navigationTitle("Subtitles")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
        }
    }
}