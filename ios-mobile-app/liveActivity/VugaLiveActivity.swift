//
//  liveActivityLiveActivity.swift
//  liveActivity
//
//  Created by Arpit Kakdiya on 25/06/24.
//

import ActivityKit
import WidgetKit
import SwiftUI
import Kingfisher

@main
struct Widgets: WidgetBundle {
    var body: some Widget {
            VugaLiveActivityApp()
    }
}

var destination = FileManager.default.containerURL(
    forSecurityApplicationGroupIdentifier: liveActivityGroupID)!


struct VugaLiveActivityApp: Widget {
    
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: VugaLiveActivityAttributes.self) { context in
            LockScreenView(context: context)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    dynamicIslandExpandedLeadingView(context: context)
                }
                
                DynamicIslandExpandedRegion(.trailing) {
                    dynamicIslandExpandedTrailingView(context: context)
                }
                
                DynamicIslandExpandedRegion(.center) {
                    dynamicIslandExpandedCenterView(context: context)
                }
                
                DynamicIslandExpandedRegion(.bottom) {
                    dynamicIslandExpandedBottomView(context: context)
                        .onChange(of: context.state.progress){
                            print(context.state.progress)
                        }
                }
            } compactLeading: {
                compactLeadingView(context: context)
            } compactTrailing: {
                compactTrailingView(context: context)
            } minimal: {
                minimalView(context: context)
            }
            .keylineTint(.base)
        }
    }
    
    
    //MARK: Expanded Views
    func dynamicIslandExpandedLeadingView(context: ActivityViewContext<VugaLiveActivityAttributes>) -> some View {
        let imageContainer = destination.appendingPathComponent(context.attributes.imageUrl)
        return VStack(alignment: .leading,spacing: 8) {
            Image(.logoHorizontal)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 40,height: 15)
                .padding(.leading,5)
            if let uiImage = UIImage(contentsOfFile: imageContainer.path()) {
                Image(uiImage: uiImage)
                    .resizable()
                    .aspectRatio(contentMode: .fill
                    )
                    .frame(width: 70, height: 50)
                    .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
            }
            Image(.logo)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 1,height: 1)
        }
    }
    
    func dynamicIslandExpandedTrailingView(context: ActivityViewContext<VugaLiveActivityAttributes>) -> some View {
        VStack {
            Text(context.state.status)
                .outfitRegular(12)
                .foregroundColor(.textLight)
                .lineLimit(1)
                .padding(.trailing,2)
            Spacer()
        }
    }
    
    func dynamicIslandExpandedBottomView(context: ActivityViewContext<VugaLiveActivityAttributes>) -> some View {
        return VStack {
            ProgressView(value: context.state.progress)
                .tint(.base)
        }
    }
    
    func dynamicIslandExpandedCenterView(context: ActivityViewContext<VugaLiveActivityAttributes>) -> some View {
        VStack(alignment: .leading,spacing: 4) {
            Text(context.attributes.contentName)
                .outfitSemiBold(15)
                .lineLimit(1)
            HStack {
                Text(context.attributes.contentResolution)
                    .outfitMedium(12)
                    .foregroundColor(.base)
                Text(context.attributes.contentType)
                    .outfitRegular(12)
                    .foregroundColor(.textLight)
                Spacer()
            }
            if context.attributes.contentType == "series" {
                Text(context.attributes.seasonEpisodeName)
                    .outfitMedium(12)
                    .foregroundColor(.textLight)
            }
        }
    }
    
    //MARK: Compact Views
    func compactLeadingView(context: ActivityViewContext<VugaLiveActivityAttributes>) -> some View {
        Image(.logo)
            .resizable()
            .aspectRatio(contentMode: .fit)
            .frame(width: 25,height: 25)
            .clipShape(.circle)
    }
    
    func compactTrailingView(context: ActivityViewContext<VugaLiveActivityAttributes>) -> some View {
        CircularProgressView(progress: context.state.progress)
            .padding(.leading,5)
    }
    
    func minimalView(context: ActivityViewContext<VugaLiveActivityAttributes>) -> some View {
        CircularProgressView(progress: context.state.progress)
    }
}


struct LockScreenView: View {
    var context: ActivityViewContext<VugaLiveActivityAttributes>
    var body: some View {
        let imageContainer = destination.appendingPathComponent(context.attributes.imageUrl)
        VStack(alignment: .leading,spacing: 0) {
            VStack(alignment: .leading) {
                VStack {
                    HStack {
                        Image(.logoHorizontal)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 50,height: 15)
                        Spacer()
                        Text(context.state.status)
                            .outfitRegular(14)
                            .foregroundColor(.textLight)
                    }
                    HStack{
                        if let uiImage = UIImage(contentsOfFile: imageContainer.path()) {
                            Image(uiImage: uiImage)
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(width: 80, height: 50)
                                .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                        }
                        VStack(alignment: .leading) {
                            HStack {
                                Text(context.attributes.contentName)
                                    .outfitMedium(16)
                                    .foregroundColor(.text)
                                Text(context.attributes.contentType)
                                    .outfitRegular(12)
                                    .foregroundColor(.textLight)
                            }
                            .padding(.bottom,1)
                            HStack {
                                Text(context.attributes.contentResolution)
                                    .outfitMedium(14)
                                    .foregroundColor(.base)
                                if context.attributes.contentType == "series" {
                                    Text(context.attributes.seasonEpisodeName)
                                        .outfitMedium(14)
                                        .foregroundColor(.textLight)
                                }
                            }
                        }
                        Spacer()
                    }
                    .padding(.bottom,5)
                }
            }
            .padding([.horizontal,.top],15)
            .padding(.bottom,10)
            ProgressView(value: context.state.progress)
                .tint(.base)
        }
        .background(Color.black)
    }
}

struct BottomLineView: View {
    var time: Date
    var body: some View {
        HStack {
            Divider().frame(width: 50,
                            height: 10)
            .overlay(.gray).cornerRadius(5)
            Image("delivery")
            VStack {
                RoundedRectangle(cornerRadius: 5)
                    .stroke(style: StrokeStyle(lineWidth: 1,
                                               dash: [4]))
                    .frame(height: 10)
                    .overlay(Text(time, style: .timer).font(.system(size: 8)).multilineTextAlignment(.center))
            }
            Image("home-address")
        }
    }
}

extension View {
    func cornerRadius(radius: CGFloat) -> some View {
        self.clipShape(RoundedRectangle(cornerRadius: radius,style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: radius,style: .continuous))
    }
}

