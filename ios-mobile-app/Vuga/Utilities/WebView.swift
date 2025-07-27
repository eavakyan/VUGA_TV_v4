//
//  WebView.swift
//  Vuga
//
//  Created by Arpit Kakdiya on 06/06/24.
//

import Foundation
import SwiftUI
import WebKit

struct WebView: UIViewRepresentable {
    @State var text: String
    var bgColor: Color
    @Binding var dynamicHeight: CGFloat
    var webview: WKWebView = WKWebView()
    var opacity: CGFloat

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webview.scrollView.bounces = false
        webview.navigationDelegate = context.coordinator
        webView.navigationDelegate = context.coordinator
        webView.scrollView.isScrollEnabled = false
        webView.isOpaque = false
        webView.backgroundColor = UIColor.bg.withAlphaComponent(0.2)
        
        return webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        let bgColor = UIColor(Color.bg).toHex() ?? ""
        let setText = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no'></head><body style=\"background-color: \(bgColor); color: #ffffff\">\(text)</body></html>"
        uiView.loadHTMLString(setText, baseURL: nil)
    }
    
    class Coordinator : NSObject,WKNavigationDelegate {
        var parent : WebView
        init(parent: WebView) {
            self.parent = parent
        }
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            
            if self.parent.dynamicHeight == .zero {
                
                self.parent.dynamicHeight = 300
                
                webView.evaluateJavaScript("document.documentElement.scrollHeight", completionHandler: { (height, error) in
                    DispatchQueue.main.asyncAfter(deadline: .now()) {
                        self.parent.dynamicHeight = height as! CGFloat
                    }
                })
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                let js = "document.getElementsByTagName('body')[0].style.webkitTextSizeAdjust='200%'"
                webView.evaluateJavaScript(js, completionHandler: nil)
            }
        }
    }
    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }
}

class WebViewModel: ObservableObject {
    @Published var url: String?
    @Published var isLoading: Bool = true
}

struct WebUrlView: UIViewRepresentable {
    func updateUIView(_ uiView: UIView, context: Context) {
        
    }
    
    @StateObject var vm: WebViewModel
    let webView = WKWebView()
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self.vm)
    }
    
    func goBack() {
        webView.goBack()
    }
    
    class Coordinator: NSObject, WKNavigationDelegate {
        private var vm: WebViewModel
        
        init(_ viewModel: WebViewModel) {
            self.vm = viewModel
        }
        
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            self.vm.isLoading = false
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                let js = "document.getElementsByTagName('body')[0].style.webkitTextSizeAdjust='200%'"
                webView.evaluateJavaScript(js, completionHandler: nil)
            }
        }
    }
    
    
    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<WebView>) { }
    
    func makeUIView(context: Context) -> UIView {
        self.webView.navigationDelegate = context.coordinator
        
        if let url = URL(string: self.vm.url ?? "") {
            self.webView.load(URLRequest(url: url))
        }
        
        return self.webView
    }
}


struct WebUrl: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @AppStorage(SessionKeys.isPro) var isPro = false
    @StateObject var model = WebViewModel()
    var url: String
    var title: String
    var onClose: ()->() = {}
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                CloseButton(onTap: onClose)
                Spacer()
                Text(title)
                    .outfitSemiBold(18)
                    .foregroundColor(.text)
                Spacer()
                if model.isLoading {
                    ProgressView()
                        .progressViewStyle(.circular)
                        .tint(.text)
                        .frame(width: 25,height: 25)

                } else {
                    Rectangle()
                        .fill(Color.bg)
                        .frame(width: 25,height: 25)
                }
            }
            .padding(.horizontal)
            .padding(.vertical)
            .background(Color.bg)
            if model.url != nil {
                WebUrlView(vm: model)
            } else {
                ZStack {
                    Color.bg
                }
                .maxFrame()
            }
            if !isPro && SessionManager.shared.getSetting()?.isAdmobIos != 0 {
                BannerAd()
                    .padding(.bottom, Device.getBottomSafeArea() + 30)
                    .padding(.top,10)
            }
        }
        .background(Color.bg)
        .onAppear {
            model.url = url
        }
    }
}

extension UIColor {
    func toHex(alpha: Bool = false) -> String? {
            guard let components = cgColor.components, components.count >= 3 else {
                return nil
            }

            let r = Float(components[0])
            let g = Float(components[1])
            let b = Float(components[2])
        var a = Float(0.3)

            if components.count >= 4 {
                a = Float(components[3])
            }

            if alpha {
                return String(format: "%02lX%02lX%02lX%02lX", lroundf(r * 255), lroundf(g * 255), lroundf(b * 255), lroundf(a * 255))
            } else {
                return String(format: "%02lX%02lX%02lX", lroundf(r * 255), lroundf(g * 255), lroundf(b * 255))
            }
        }
}

extension UIColor {
    func toHexWithAlpha() -> String? {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        
        guard self.getRed(&red, green: &green, blue: &blue, alpha: &alpha) else { return nil }
        
        return String(format: "#%02lX%02lX%02lX%02lX", lround(Double(red * 255)), lround(Double(green * 255)), lround(Double(blue * 255)), lround(Double(alpha * 255)))
    }
}

extension UIColor {
    func toRGBA() -> String {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        
        self.getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        
        return String(format: "rgba(%d, %d, %d, %.2f)", Int(red * 255), Int(green * 255), Int(blue * 255), alpha)
    }
}

struct EmbeddedWeb: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.backgroundColor = .bg
        let request = URLRequest(url: url)
        webView.load(request)
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}
