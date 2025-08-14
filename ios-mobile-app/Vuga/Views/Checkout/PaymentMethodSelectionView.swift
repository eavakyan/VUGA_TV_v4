//
//  PaymentMethodSelectionView.swift
//  Vuga
//
//  Payment method selection step in checkout flow
//

import SwiftUI

struct PaymentMethodSelectionView: View {
    @ObservedObject var viewModel: CheckoutViewModel
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Header text
                VStack(alignment: .leading, spacing: 8) {
                    Text("Select Payment Method")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                    
                    Text("Choose how you'd like to pay for your subscription")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal)
                .padding(.top, 20)
                
                // Payment method options
                VStack(spacing: 12) {
                    ForEach(PaymentMethod.allCases, id: \.self) { method in
                        if method.isAvailable {
                            PaymentMethodCard(
                                method: method,
                                isSelected: viewModel.selectedPaymentMethod == method,
                                action: {
                                    withAnimation(.easeInOut(duration: 0.2)) {
                                        viewModel.selectedPaymentMethod = method
                                    }
                                }
                            )
                        }
                    }
                }
                .padding(.horizontal)
                
                // Security notice
                HStack {
                    Image(systemName: "lock.shield.fill")
                        .font(.system(size: 14))
                        .foregroundColor(.green)
                    
                    Text("Your payment information is encrypted and secure")
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                    
                    Spacer()
                }
                .padding()
                .background(Color.green.opacity(0.1))
                .cornerRadius(8)
                .padding(.horizontal)
                .padding(.top, 10)
                
                // Accepted cards
                VStack(alignment: .leading, spacing: 12) {
                    Text("Accepted Cards")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.gray)
                    
                    HStack(spacing: 16) {
                        PaymentBrandIcon(name: "visa")
                        PaymentBrandIcon(name: "mastercard")
                        PaymentBrandIcon(name: "amex")
                        PaymentBrandIcon(name: "discover")
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal)
                .padding(.top, 20)
            }
            .padding(.bottom, 100)
        }
    }
}

// MARK: - Payment Method Card
struct PaymentMethodCard: View {
    let method: PaymentMethod
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                // Icon
                Image(systemName: method.icon)
                    .font(.system(size: 24))
                    .foregroundColor(isSelected ? Color("baseColor") : .gray)
                    .frame(width: 40)
                
                // Title and description
                VStack(alignment: .leading, spacing: 4) {
                    Text(method.title)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                    
                    if let description = methodDescription {
                        Text(description)
                            .font(.system(size: 12))
                            .foregroundColor(.gray)
                    }
                }
                
                Spacer()
                
                // Selection indicator
                ZStack {
                    Circle()
                        .stroke(isSelected ? Color("baseColor") : Color.gray.opacity(0.3), lineWidth: 2)
                        .frame(width: 24, height: 24)
                    
                    if isSelected {
                        Circle()
                            .fill(Color("baseColor"))
                            .frame(width: 12, height: 12)
                    }
                }
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.gray.opacity(0.1))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(isSelected ? Color("baseColor") : Color.clear, lineWidth: 2)
                    )
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var methodDescription: String? {
        switch method {
        case .creditCard:
            return "Pay with Visa, Mastercard, or other credit cards"
        case .debitCard:
            return "Pay directly from your bank account"
        case .paypal:
            return "Fast and secure payment with PayPal"
        case .applePay:
            return "Quick checkout with Touch ID or Face ID"
        case .googlePay:
            return "Pay with your saved Google payment methods"
        }
    }
}

// MARK: - Payment Brand Icon
struct PaymentBrandIcon: View {
    let name: String
    
    var body: some View {
        RoundedRectangle(cornerRadius: 4)
            .fill(Color.white)
            .frame(width: 50, height: 32)
            .overlay(
                Text(name.uppercased())
                    .font(.system(size: 8, weight: .bold))
                    .foregroundColor(.black)
            )
    }
}