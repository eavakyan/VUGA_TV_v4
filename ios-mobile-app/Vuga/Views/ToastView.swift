//
//  ToastView.swift
//  Vuga
//
//  Toast notification view for success/error messages
//

import SwiftUI

struct ToastView: View {
    let message: String
    let isSuccess: Bool
    @Binding var isShowing: Bool
    
    var body: some View {
        VStack {
            Spacer()
            
            HStack(spacing: 12) {
                Image(systemName: isSuccess ? "checkmark.circle.fill" : "exclamationmark.circle.fill")
                    .font(.system(size: 20))
                    .foregroundColor(isSuccess ? .green : .red)
                
                Text(message)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.leading)
                
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color("bgColor"))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color.white.opacity(0.1), lineWidth: 1)
                    )
            )
            .shadow(color: .black.opacity(0.3), radius: 10, x: 0, y: 5)
            .padding(.horizontal, 20)
            .padding(.bottom, 50)
        }
        .transition(.move(edge: .bottom).combined(with: .opacity))
        .animation(.easeInOut(duration: 0.3), value: isShowing)
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                withAnimation {
                    isShowing = false
                }
            }
        }
    }
}

// Toast modifier for easy use
struct ToastModifier: ViewModifier {
    @Binding var isShowing: Bool
    let message: String
    let isSuccess: Bool
    
    func body(content: Content) -> some View {
        ZStack {
            content
            
            if isShowing {
                ToastView(message: message, isSuccess: isSuccess, isShowing: $isShowing)
                    .zIndex(1000)
            }
        }
    }
}

extension View {
    func toast(isShowing: Binding<Bool>, message: String, isSuccess: Bool = true) -> some View {
        self.modifier(ToastModifier(isShowing: isShowing, message: message, isSuccess: isSuccess))
    }
}