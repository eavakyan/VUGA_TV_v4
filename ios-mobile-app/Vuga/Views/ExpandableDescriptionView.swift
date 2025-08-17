//
//  ExpandableDescriptionView.swift
//  Vuga
//
//  Created for expandable content description
//

import SwiftUI

struct ExpandableDescriptionView: View {
    let description: String
    @State private var isExpanded = false
    @State private var showFullDescriptionSheet = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(description)
                .font(.system(size: 14))
                .foregroundColor(.white.opacity(0.8))
                .lineLimit(3)
            
            // MORE button
            Button(action: {
                showFullDescriptionSheet = true
            }) {
                Text("MORE")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Color.white.opacity(0.2))
                    .cornerRadius(4)
            }
        }
        .sheet(isPresented: $showFullDescriptionSheet) {
            FullDescriptionSheet(description: description)
        }
    }
}

struct FullDescriptionSheet: View {
    let description: String
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Synopsis")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.top, 20)
                    
                    Text(description)
                        .font(.system(size: 16))
                        .foregroundColor(.white.opacity(0.9))
                        .lineSpacing(6)
                    
                    Spacer(minLength: 40)
                }
                .padding(.horizontal, 20)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color("bgColor"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        presentationMode.wrappedValue.dismiss()
                    }
                    .foregroundColor(.white)
                }
            }
        }
    }
}