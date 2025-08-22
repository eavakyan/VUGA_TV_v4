//
//  AudioTrackSelectionView.swift
//  Vuga
//
//

import SwiftUI

struct AudioTrackSelectionView: View {
    let tracks: [AudioTrack]
    @Binding var selectedTrack: AudioTrack?
    let onTrackSelected: (AudioTrack) -> Void
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            List {
                ForEach(tracks, id: \.id) { track in
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(track.title ?? track.languageCode ?? "Unknown")
                                .font(.headline)
                            if let format = track.audioFormat, let channels = track.audioChannels {
                                Text("\(format) • \(channels)")
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
            .navigationTitle("Audio Tracks")
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