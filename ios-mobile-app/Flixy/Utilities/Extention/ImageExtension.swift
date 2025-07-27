//
//  ImageExtensions.swift
//  QRCode
//
//  Created by Aniket Vaddoriya on 20/05/23.
//

import SwiftUI
import Kingfisher
import Shimmer

extension Image {
    static let apple                = Image(systemName: "applelogo")
    static let mail                 = Image(systemName: "envelope.fill")
    static let back                 = Image(systemName: "chevron.left")
    static let xamrkFill            = Image(systemName: "xmark.circle.fill")
    static let xamrk                = Image(systemName: "xmark")
    static let logo                 = Image("logo")
    static let logoHorizontal       = Image("logo_horizontal")
    static let google               = Image("google")
    static let home                 = Image("home")
    static let search               = Image("search")
    static let liveTV               = Image("liveTV")
    static let save                 = Image("save")
    static let person               = Image("person")
    static let star                 = Image("star")
    static let share                = Image("share")
    static let bookmark             = Image("bookmark")
    static let bookmarkFill         = Image("bookmark_fill")
    static let eye                  = Image("eye")
    static let clock                = Image("clock")
    static let downloaded           = Image("downloaded")
    static let download             = Image("download")
    static let downloadPlay         = Image("download_play")
    static let downloadPause        = Image("download_pause")
    static let crown                = Image("crown")
    static let lock                 = Image("lock")
    static let language             = Image("language")
    static let genre                = Image("genre")
    static let delete               = Image("delete")
    static let logout               = Image("logout")
    static let rate                 = Image("rate")
    static let terms                = Image("terms")
    static let downloads            = Image("downloads")
    static let privacy              = Image("privacy")
    static let notification         = Image("notification")
    static let edit                 = Image("edit")
    static let tv                   = Image(systemName: "tv")
    static let premium              = Image("premium")
    static let premium_bg           = Image("premium_bg")
    static let camera               = Image("camera")
    static let checkbox             = Image("checkbox")
    static let close                = Image(systemName: "xmark")
    static let soundOn              = Image("soundOn")
    static let soundOff             = Image("soundOff")
    static let fullScreen           = Image("fullScreen")
    static let tenR                 = Image("tenR")
    static let tenF                 = Image("tenF")
    static let adLcok               = Image("adLock")
    static let replay               = Image("replay")
    static let subtitles            = Image("subtitles")
    static let removeWatchlist      = Image("removeWatchlist")
    static let timer                = Image("timer")
    static let cake                 = Image("cake")
    static let download_Pause       = Image("downloadPause")
    static let play                 = Image("play")
    static let options              = Image(systemName: "ellipsis")
    static let noData               = Image("noData")
    static let languageSearch       = Image("language_search")
    static let info                 = Image(systemName: "info.circle")
    
    func resizeFitTo(size: CGFloat,renderingMode: TemplateRenderingMode = .original ,radius: CGFloat = 0) -> some View {
        self.resizable()
            .renderingMode(renderingMode)
            .scaledToFit()
            .frame(width: size,height: size)
            .clipped()
            .contentShape(Rectangle())
            .cornerRadius(radius)
    }
    
    func resizeFitTo(width: CGFloat,height: CGFloat,renderingMode: TemplateRenderingMode = .original ,radius: CGFloat = 0) -> some View {
        self.resizable()
            .renderingMode(renderingMode)
            .scaledToFit()
            .frame(width: width, height: height)
            .clipped()
            .contentShape(Rectangle())
            .cornerRadius(radius)
    }
    
    func resizeFillTo(size: CGFloat,renderingMode: TemplateRenderingMode = .original ,radius: CGFloat = 0) -> some View {
        self.resizable()
            .renderingMode(renderingMode)
            .scaledToFill()
            .frame(width: size,height: size)
            .clipped()
            .contentShape(Rectangle())
            .cornerRadius(radius)  
    }
    
    func resizeFillTo(width: CGFloat,height: CGFloat,renderingMode: TemplateRenderingMode = .original ,radius: CGFloat = 0) -> some View {
        self.resizable()
            .renderingMode(renderingMode)
            .scaledToFill()
            .frame(width: width, height: height)
            .clipped()
            .contentShape(Rectangle())
            .cornerRadius(radius)
    }
}

extension KFImage {
    func myPlaceholder() -> KFImage {
        self.placeholder {
            Rectangle()
//                .cornerRadius(radius: 20)
                .shimmering(gradient: Gradient(colors: [
                    .black.opacity(0.1), // translucent
                    .black.opacity(0.05) , // opaque
                    .black.opacity(0.1) // translucent
                ]))
//                .cornerRadius(radius: 20)
        }
    }
    
    func resizeFitTo(width: CGFloat,height: CGFloat,  compressSize: CGFloat = 1.3,radius: CGFloat = 0) -> some View {
        self.resizable()
            .myPlaceholder()
            .setProcessor(DownsamplingImageProcessor(size: CGSize(width: width * compressSize, height: height * compressSize)))
            .loadDiskFileSynchronously(true)
            .scaledToFit()
            .frame(width: width, height: height)
            .clipped()
            .contentShape(Rectangle())
            .cornerRadius(radius)
    }
    
    func resizeFillTo(width: CGFloat,height: CGFloat, compressSize: CGFloat = 1.3,radius: CGFloat = 0) -> some View {
        self.resizable()
            .myPlaceholder()
            .setProcessor(DownsamplingImageProcessor(size: CGSize(width: width * compressSize, height: height * compressSize)))
            .loadDiskFileSynchronously(true)
            .scaledToFill()
            .frame(width: width, height: height)
            .clipped()
            .contentShape(Rectangle())
            .cornerRadius(radius)
    }
    
}
