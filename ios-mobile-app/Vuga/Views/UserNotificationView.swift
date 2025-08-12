//
//  UserNotificationView.swift
//  Vuga
//
//  View for displaying in-app user notifications
//

import SwiftUI

struct UserNotificationView: View {
    @ObservedObject var viewModel: UserNotificationViewModel
    
    var body: some View {
        if viewModel.isShowingNotification, let notification = viewModel.currentNotification {
            ZStack {
                // Dark background overlay
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                    .onTapGesture {
                        // Allow dismissing by tapping outside
                        viewModel.dismissNotification()
                    }
                
                // Notification card
                VStack(spacing: 0) {
                    // Header with type icon and priority
                    HStack {
                        Image(systemName: notification.typeIcon)
                            .foregroundColor(.white)
                            .font(.title2)
                        
                        Spacer()
                        
                        if notification.priority == "urgent" || notification.priority == "high" {
                            Text(notification.priority.uppercased())
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color(notification.priorityColor))
                                .cornerRadius(4)
                        }
                        
                        Button(action: {
                            viewModel.dismissNotification()
                        }) {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.gray)
                                .font(.title2)
                        }
                    }
                    .padding()
                    .background(Color("bgColor"))
                    
                    // Content
                    VStack(alignment: .leading, spacing: 12) {
                        Text(notification.title)
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                        
                        Text(notification.message)
                            .font(.body)
                            .foregroundColor(.gray)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    // Action buttons
                    HStack(spacing: 12) {
                        Button(action: {
                            viewModel.dismissNotification()
                        }) {
                            Text("Dismiss")
                                .fontWeight(.medium)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(Color.gray.opacity(0.3))
                                .cornerRadius(8)
                        }
                        
                        // You can add more action buttons here based on notification type
                        if notification.notificationType == "update" {
                            Button(action: {
                                // Handle update action
                                viewModel.dismissNotification()
                            }) {
                                Text("Update Now")
                                    .fontWeight(.medium)
                                    .foregroundColor(.black)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                                    .background(Color("baseColor"))
                                    .cornerRadius(8)
                            }
                        }
                    }
                    .padding()
                }
                .frame(maxWidth: UIScreen.main.bounds.width - 40)
                .background(Color("cardColor"))
                .cornerRadius(16)
                .shadow(radius: 20)
                .transition(.scale.combined(with: .opacity))
                .animation(.spring(), value: viewModel.isShowingNotification)
            }
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