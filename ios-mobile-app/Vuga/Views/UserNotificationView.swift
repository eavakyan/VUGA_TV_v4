//
//  UserNotificationView.swift
//  Vuga
//
//  View for displaying in-app user notifications
//

import SwiftUI

struct UserNotificationView: View {
    @ObservedObject var viewModel: UserNotificationViewModel
    @State private var animateIn = false
    
    var body: some View {
        if viewModel.isShowingNotification, let notification = viewModel.currentNotification {
            ZStack {
                // Dark background overlay with blur
                Color.black.opacity(0.7)
                    .ignoresSafeArea()
                    .background(
                        BlurView(style: .dark)
                            .ignoresSafeArea()
                    )
                    .onTapGesture {
                        withAnimation(.easeOut(duration: 0.2)) {
                            viewModel.dismissNotification()
                        }
                    }
                
                // Notification card
                VStack(spacing: 0) {
                    // Priority indicator bar
                    if notification.priority == "urgent" || notification.priority == "high" {
                        Rectangle()
                            .fill(LinearGradient(
                                gradient: Gradient(colors: [
                                    Color(notification.priorityColor),
                                    Color(notification.priorityColor).opacity(0.7)
                                ]),
                                startPoint: .leading,
                                endPoint: .trailing
                            ))
                            .frame(height: 4)
                    }
                    
                    // Main content container
                    VStack(spacing: 0) {
                        // Header section
                        HStack(alignment: .top) {
                            // Icon with background
                            ZStack {
                                Circle()
                                    .fill(Color("baseColor").opacity(0.2))
                                    .frame(width: 44, height: 44)
                                
                                Image(systemName: notification.typeIcon)
                                    .foregroundColor(Color("baseColor"))
                                    .font(.system(size: 20, weight: .medium))
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(notification.title)
                                    .font(.system(size: 20, weight: .bold))
                                    .foregroundColor(.white)
                                    .lineLimit(2)
                                
                                if notification.priority == "urgent" || notification.priority == "high" {
                                    HStack(spacing: 4) {
                                        Circle()
                                            .fill(Color(notification.priorityColor))
                                            .frame(width: 6, height: 6)
                                        Text(notification.priority.capitalized)
                                            .font(.system(size: 12, weight: .semibold))
                                            .foregroundColor(Color(notification.priorityColor))
                                    }
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            
                            // Close button
                            Button(action: {
                                withAnimation(.easeOut(duration: 0.2)) {
                                    viewModel.dismissNotification()
                                }
                            }) {
                                ZStack {
                                    Circle()
                                        .fill(Color.white.opacity(0.1))
                                        .frame(width: 32, height: 32)
                                    
                                    Image(systemName: "xmark")
                                        .foregroundColor(.white.opacity(0.7))
                                        .font(.system(size: 14, weight: .semibold))
                                }
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        .padding(20)
                        
                        // Message content
                        ScrollView {
                            Text(notification.message)
                                .font(.system(size: 15, weight: .regular))
                                .foregroundColor(.white.opacity(0.9))
                                .lineSpacing(4)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.horizontal, 20)
                        }
                        .frame(maxHeight: 150)
                        
                        // Divider
                        Rectangle()
                            .fill(Color.white.opacity(0.1))
                            .frame(height: 1)
                            .padding(.top, 16)
                        
                        // Action buttons
                        HStack(spacing: 12) {
                            // Secondary button
                            Button(action: {
                                withAnimation(.easeOut(duration: 0.2)) {
                                    viewModel.dismissNotification()
                                }
                            }) {
                                Text("Later")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(.white.opacity(0.8))
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 44)
                                    .background(
                                        RoundedRectangle(cornerRadius: 10)
                                            .fill(Color.white.opacity(0.1))
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 10)
                                                    .stroke(Color.white.opacity(0.2), lineWidth: 1)
                                            )
                                    )
                            }
                            .buttonStyle(PlainButtonStyle())
                            
                            // Primary action button
                            Button(action: {
                                // Handle primary action based on notification type
                                handlePrimaryAction(for: notification)
                                withAnimation(.easeOut(duration: 0.2)) {
                                    viewModel.dismissNotification()
                                }
                            }) {
                                Text(primaryActionText(for: notification))
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(.black)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 44)
                                    .background(
                                        RoundedRectangle(cornerRadius: 10)
                                            .fill(Color("baseColor"))
                                    )
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        .padding(20)
                    }
                    .background(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(Color("cardColor"))
                    )
                }
                .frame(maxWidth: min(UIScreen.main.bounds.width - 40, 400))
                .shadow(color: Color.black.opacity(0.3), radius: 30, x: 0, y: 10)
                .scaleEffect(animateIn ? 1 : 0.8)
                .opacity(animateIn ? 1 : 0)
                .onAppear {
                    withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                        animateIn = true
                    }
                }
                .onDisappear {
                    animateIn = false
                }
            }
        }
    }
    
    private func primaryActionText(for notification: UserNotification) -> String {
        switch notification.notificationType {
        case "update":
            return "Update Now"
        case "promotional":
            return "View Offer"
        case "maintenance":
            return "Got It"
        default:
            return "OK"
        }
    }
    
    private func handlePrimaryAction(for notification: UserNotification) {
        // Handle different notification types
        switch notification.notificationType {
        case "update":
            // Handle app update
            print("Handle update action")
        case "promotional":
            // Handle promotional offer
            print("Handle promotional action")
        default:
            break
        }
    }
}

// Preview
struct UserNotificationView_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            Color.black
            UserNotificationView(viewModel: UserNotificationViewModel())
        }
    }
}