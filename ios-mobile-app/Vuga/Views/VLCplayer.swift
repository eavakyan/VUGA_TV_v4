//
//  VLCplayer.swift
//  Vuga
//
//

import SwiftUI
import MobileVLCKit

struct VlcPlayer: UIViewRepresentable{
	var player : VLCMediaPlayer
    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<VlcPlayer>) {
         
	}
	
	func makeUIView(context: Context) -> UIView {
        return PlayerUIView(frame: .init(x: 0, y: 0, width: Device.width, height: Device.height), player: player)
	}
}

class PlayerUIView: UIView {
    var mediaPlayer : VLCMediaPlayer
    init(frame: CGRect,player : VLCMediaPlayer) {
        mediaPlayer = player
        super.init(frame: frame)
        mediaPlayer.drawable = self
    }
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    override func layoutSubviews() {
        super.layoutSubviews()
    }
}
