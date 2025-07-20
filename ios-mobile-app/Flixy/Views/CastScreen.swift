//
//  CastScreen.swift
//  Flixy
//
//  Created by Arpit Kakdiya on 05/07/24.
//

import Foundation
import SwiftUI
import Kingfisher
import ExpandableText

struct CastView : View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    var actorId : Int
    @StateObject var vm = CastViewModel()
    
    var body: some View {
        VStack(alignment: .leading,spacing: 0) {
            if !vm.isLoading {
                BackBarView(title: .starCast.localized(language))
                Divider()
                Spacer()
                HStack {
                    VStack(alignment: .leading,spacing: 7) {
                        Text(vm.actor?.fullname ?? "")
                            .outfitSemiBold(20)
                        HStack {
                            Image.cake
                                .resizeFitTo(size: 20,renderingMode: .template)
                                .foregroundColor(.textLight)
                            
                            Text(vm.actor?.dob?.toFormattedDateString() ?? "")
                                .outfitRegular(16)
                                .foregroundColor(.textLight)
                            
                        }
                    }
                    Spacer()
                    KFImage(vm.actor?.profileImage.addBaseURL())
                        .resizeFillTo(width: 70, height: 70)
                        .cornerRadius(radius: 14)
                        .addStroke(radius: 15)
                }
                .padding(.horizontal)
                .padding(.bottom,5)
                var replaceLine = vm.actor?.bio?.replacingOccurrences(of: "\n", with: "")
                let replaced = replaceLine?.replacingOccurrences(of: "\r", with: " ")
                ExpandableTextTwo(text: replaced ?? "")
                    .font(.custom(MyFont.OutfitRegular, size: 14))
                    .foregroundColor(.textLight)
                    .lineLimit(3)
                    .padding(.horizontal)
                    .onTap {
                        withAnimation {
                            vm.isInformationSheetOpen = true
                        }
                    }
                Divider()
                    .padding(.top,10)
                
                ScrollView(showsIndicators: false) {
                    ForEach(vm.actor?.actorContent ?? [], id: \.id) { content in
                        ContentHorizontalCard(content: content)
                    }
                    .padding(.horizontal)
                    .padding(.vertical,10)
                }
            }
        }
        .overlay(
            CastInformationSheet(vm: vm)
        )
        .onAppear(perform: {
            if !vm.isDataLoaded {
                vm.fetchActorDetail(actorId: actorId)
            }
        })
        .loaderView(vm.isLoading)
        .hideNavigationbar()
    }
}

struct CastInformationSheet: View {
    @StateObject var vm : CastViewModel
    var body: some View {
        if vm.isInformationSheetOpen {
            VStack(spacing: 0) {
                Text(vm.actor?.fullname ?? "")
                    .outfitRegular(24)
                    .foregroundColor(.text)
                ScrollView(showsIndicators: false) {
                    LabelAlignment(text: vm.actor?.bio ?? "", textAlignmentStyle: .justified, width: UIScreen.main.bounds.width - 20)
                        .padding(.vertical,25)
                }
                .mask(VStack(spacing: 0) {
                    Rectangle().fill(LinearGradient(colors: [.clear, .clear, .black], startPoint: .top, endPoint: .bottom))
                        .frame(height: 30)
                    Rectangle()
                    Rectangle().fill(LinearGradient(colors: [.black, .clear, .clear], startPoint: .top, endPoint: .bottom))
                        .frame(height: 30)
                })
                .padding(.horizontal)
                BottomXMarkButton {
                    withAnimation {
                        vm.isInformationSheetOpen = false
                    }
                }
            }
            .padding(.vertical)
            .background(Color.bg)
            .animation(.default,value: vm.isInformationSheetOpen)
            .hideNavigationbar()
        }
    }
}


public struct ExpandableTextTwo: View {
    var text : String
    
    var font: Font = .body
    var lineLimit: Int = 3
    var foregroundColor: Color = .primary
    
    var expandButton: TextSet = TextSet(text: "More", font: .callout, color: .blue)
    var collapseButton: TextSet? = TextSet(text: "collapse", font: .callout, color: .blue)
    
    var animation: Animation? = .none
    
    @State private var expand : Bool = false
    @State private var truncated : Bool = false
    @State private var fullSize: CGFloat = 0
    
    public init(text: String) {
        self.text = text
    }
    public var body: some View {
        ZStack(alignment: .bottomTrailing){
            Group {
                Text(text)
            }
            .font(font)
            .foregroundColor(foregroundColor)
            .lineLimit(expand == true ? nil : lineLimit)
            .animation(animation, value: expand)
            .mask(
                VStack(spacing: 0){
                    Rectangle()
                        .foregroundColor(.black)
                    
                    HStack(spacing: 0){
                        Rectangle()
                            .foregroundColor(.black)
                        if truncated{
                            if !expand {
                                HStack(alignment: .bottom,spacing: 0){
                                    LinearGradient(
                                        gradient: Gradient(stops: [
                                            Gradient.Stop(color: .black, location: 0),
                                            Gradient.Stop(color: .clear, location: 0.8)]),
                                        startPoint: .leading,
                                        endPoint: .trailing)
                                    .frame(width: 32, height: expandButton.text.height(usingFont: fontToUIFont(font: expandButton.font)))
                                    
                                    Rectangle()
                                        .foregroundColor(.clear)
                                        .frame(width: expandButton.text.width(usingFont: fontToUIFont(font: expandButton.font)), alignment: .center)
                                }
                            }
                            else if let collapseButton = collapseButton {
                                HStack(alignment: .bottom,spacing: 0){
                                    LinearGradient(
                                        gradient: Gradient(stops: [
                                            Gradient.Stop(color: .black, location: 0),
                                            Gradient.Stop(color: .clear, location: 0.8)]),
                                        startPoint: .leading,
                                        endPoint: .trailing)
                                    .frame(width: 32, height: collapseButton.text.height(usingFont: fontToUIFont(font: collapseButton.font)))
                                    
                                    Rectangle()
                                        .foregroundColor(.clear)
                                        .frame(width: collapseButton.text.width(usingFont: fontToUIFont(font: collapseButton.font)), alignment: .center)
                                }
                            }
                        }
                    }
                    .frame(height: expandButton.text.height(usingFont: fontToUIFont(font: font)))
                }
            )
            
            if truncated {
                if let collapseButton = collapseButton {
                    Text(expand == false ? expandButton.text : collapseButton.text)
                        .font(.custom(MyFont.OutfitSemiBold, size: 14))
                        .foregroundColor(.text)
                }
                else if !expand {
                    Text(expandButton.text)
                        .font(.custom(MyFont.OutfitSemiBold, size: 14))
                        .foregroundColor(.text)
                }
            }
        }
        .background(
            ZStack{
                if !truncated {
                    if fullSize != 0 {
                        Text(text)
                            .font(font)
                            .lineLimit(lineLimit)
                            .background(
                                GeometryReader { geo in
                                    Color.clear
                                        .onAppear {
                                            if fullSize > geo.size.height {
                                                self.truncated = true
                                                print(geo.size.height)
                                            }
                                        }
                                }
                            )
                    }
                    
                    Text(text)
                        .font(font)
                        .lineLimit(999)
                        .fixedSize(horizontal: false, vertical: true)
                        .background(GeometryReader { geo in
                            Color.clear
                                .onAppear() {
                                    self.fullSize = geo.size.height
                                }
                        })
                }
            }
                .hidden()
        )
    }
}

extension ExpandableTextTwo {
    public func font(_ font: Font) -> Self {
        var result = self
        result.font = font
        return result
    }
    
    public func lineLimit(_ lineLimit: Int) -> Self {
        var result = self
        result.lineLimit = lineLimit
        return result
    }
    
    public func foregroundColor(_ color: Color) -> Self {
        var result = self
        result.foregroundColor = color
        return result
    }
    
    public func expandButton(_ expandButton: TextSet) -> Self {
        var result = self
        result.expandButton = expandButton
        return result
    }
    
    public func collapseButton(_ collapseButton: TextSet) -> Self {
        var result = self
        result.collapseButton = collapseButton
        return result
    }
    
    public func expandAnimation(_ animation: Animation?) -> Self {
        var result = self
        result.animation = animation
        return result
    }
}

extension String {
    func height(usingFont font: UIFont) -> CGFloat {
        let fontAttributes = [NSAttributedString.Key.font: font]
        return self.size(withAttributes: fontAttributes).height
    }
    
    func width(usingFont font: UIFont) -> CGFloat {
        let fontAttributes = [NSAttributedString.Key.font: font]
        return self.size(withAttributes: fontAttributes).width
    }
}

public struct TextSet {
    var text: String
    var font: Font
    var color: Color
    
    public init(text: String, font: Font, color: Color) {
        self.text = text
        self.font = font
        self.color = color
    }
}

func fontToUIFont(font: Font) -> UIFont {
    switch font {
    case .largeTitle: return .preferredFont(forTextStyle: .largeTitle)
    case .title: return .preferredFont(forTextStyle: .title1)
    case .title2, .title3: return .preferredFont(forTextStyle: .title2)
    case .headline: return .preferredFont(forTextStyle: .headline)
    case .subheadline: return .preferredFont(forTextStyle: .subheadline)
    case .callout: return .preferredFont(forTextStyle: .callout)
    case .caption: return .preferredFont(forTextStyle: .caption1)
    case .caption2, .footnote, .body: return .preferredFont(forTextStyle: .caption2)
    default: return .preferredFont(forTextStyle: .caption2)
    }
}

struct TextView: UIViewRepresentable {
    var text: String
    
    func makeUIView(context: Context) -> UITextView {
        let textView = UITextView()
        textView.textAlignment = .justified
        textView.textColor = .lightText
        textView.font = UIFont(name: MyFont.OutfitRegular, size: 14)
        textView.backgroundColor = .clear
        textView.isScrollEnabled = true
        return textView
    }
    
    func updateUIView(_ uiView: UITextView, context: Context) {
        uiView.text = text
    }
}

struct LabelAlignment: UIViewRepresentable {
    var text: String
    var textAlignmentStyle : TextAlignmentStyle
    var width: CGFloat
    
    func makeUIView(context: Context) -> UILabel {
        let label = UILabel()
        label.textAlignment = NSTextAlignment(rawValue: textAlignmentStyle.rawValue)!
        label.numberOfLines = 0
        label.preferredMaxLayoutWidth = width
        label.font = UIFont(name: MyFont.OutfitRegular, size: 14)
        label.textColor = .textLight
        label.setContentHuggingPriority(.required, for: .horizontal)
        label.setContentHuggingPriority(.required, for: .vertical)
        
        return label
    }
    
    func updateUIView(_ uiView: UILabel, context: Context) {
        uiView.text = text
    }
}
enum TextAlignmentStyle : Int{
    case left = 0 ,center = 1 , right = 2 ,justified = 3 ,natural = 4
}
