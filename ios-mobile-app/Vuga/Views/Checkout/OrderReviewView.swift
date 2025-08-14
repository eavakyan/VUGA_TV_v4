//
//  OrderReviewView.swift
//  Vuga
//
//  Order review and confirmation step in checkout flow
//

import SwiftUI

struct OrderReviewView: View {
    @ObservedObject var viewModel: CheckoutViewModel
    let planDetails: SubscriptionPricingModel?
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Order Summary Header
                VStack(alignment: .leading, spacing: 8) {
                    Text("Review Your Order")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                    
                    Text("Please review your subscription details before completing purchase")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                
                // Subscription Details
                subscriptionDetailsCard
                
                // Payment Method Summary
                paymentMethodCard
                
                // Billing Address Summary
                if viewModel.selectedPaymentMethod == .creditCard || 
                   viewModel.selectedPaymentMethod == .debitCard {
                    billingAddressCard
                }
                
                // Price Breakdown
                priceBreakdownCard
                
                // Terms and Conditions
                termsAndConditionsSection
                
                // Important Notes
                importantNotesSection
            }
            .padding()
            .padding(.bottom, 100)
        }
    }
    
    // MARK: - Subscription Details Card
    private var subscriptionDetailsCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("Subscription Details")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                Spacer()
            }
            
            VStack(alignment: .leading, spacing: 12) {
                if let plan = planDetails {
                    HStack {
                        Text("Plan:")
                            .foregroundColor(.gray)
                        Spacer()
                        Text(plan.displayName)
                            .foregroundColor(.white)
                            .fontWeight(.medium)
                    }
                    .font(.system(size: 14))
                    
                    HStack {
                        Text("Billing Period:")
                            .foregroundColor(.gray)
                        Spacer()
                        Text(plan.billingPeriod)
                            .foregroundColor(.white)
                    }
                    .font(.system(size: 14))
                    
                    HStack {
                        Text("Auto-renewal:")
                            .foregroundColor(.gray)
                        Spacer()
                        Text("Yes")
                            .foregroundColor(.green)
                    }
                    .font(.system(size: 14))
                }
            }
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }
    
    // MARK: - Payment Method Card
    private var paymentMethodCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("Payment Method")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                Spacer()
                Button("Change") {
                    viewModel.currentStep = 1
                }
                .font(.system(size: 14))
                .foregroundColor(Color("baseColor"))
            }
            
            if let method = viewModel.selectedPaymentMethod {
                HStack(spacing: 12) {
                    Image(systemName: method.icon)
                        .font(.system(size: 20))
                        .foregroundColor(.gray)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(method.title)
                            .font(.system(size: 14))
                            .foregroundColor(.white)
                        
                        if method == .creditCard || method == .debitCard {
                            if !viewModel.cardDetails.cardNumber.isEmpty {
                                Text("•••• \(String(viewModel.cardDetails.cardNumber.suffix(4)))")
                                    .font(.system(size: 12))
                                    .foregroundColor(.gray)
                            }
                        }
                    }
                    
                    Spacer()
                }
            }
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }
    
    // MARK: - Billing Address Card
    private var billingAddressCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("Billing Address")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                Spacer()
                Button("Change") {
                    viewModel.currentStep = 2
                }
                .font(.system(size: 14))
                .foregroundColor(Color("baseColor"))
            }
            
            VStack(alignment: .leading, spacing: 8) {
                if !viewModel.billingAddress.street.isEmpty {
                    Text(viewModel.billingAddress.street)
                        .font(.system(size: 14))
                        .foregroundColor(.white)
                }
                
                if !viewModel.billingAddress.city.isEmpty || !viewModel.billingAddress.state.isEmpty {
                    Text("\(viewModel.billingAddress.city), \(viewModel.billingAddress.state) \(viewModel.billingAddress.zipCode)")
                        .font(.system(size: 14))
                        .foregroundColor(.white)
                }
                
                Text(viewModel.billingAddress.country)
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
            }
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }
    
    // MARK: - Price Breakdown Card
    private var priceBreakdownCard: some View {
        VStack(spacing: 16) {
            HStack {
                Text("Order Summary")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                Spacer()
            }
            
            VStack(spacing: 12) {
                if let plan = planDetails {
                    HStack {
                        Text("Subscription")
                            .foregroundColor(.gray)
                        Spacer()
                        Text("$\(plan.price)")
                            .foregroundColor(.white)
                    }
                    .font(.system(size: 14))
                    
                    HStack {
                        Text("Tax")
                            .foregroundColor(.gray)
                        Spacer()
                        Text("$\(calculateTax())")
                            .foregroundColor(.white)
                    }
                    .font(.system(size: 14))
                    
                    Divider()
                        .background(Color.gray.opacity(0.3))
                    
                    HStack {
                        Text("Total")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                        Spacer()
                        Text("$\(calculateTotal())")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(Color("baseColor"))
                    }
                }
            }
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }
    
    // MARK: - Terms and Conditions
    private var termsAndConditionsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top, spacing: 12) {
                Button(action: {
                    viewModel.agreedToTerms.toggle()
                }) {
                    Image(systemName: viewModel.agreedToTerms ? "checkmark.square.fill" : "square")
                        .font(.system(size: 20))
                        .foregroundColor(viewModel.agreedToTerms ? Color("baseColor") : .gray)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("I agree to the Terms and Conditions")
                        .font(.system(size: 14))
                        .foregroundColor(.white)
                    
                    HStack(spacing: 8) {
                        Button("Terms of Service") {
                            // Open terms
                        }
                        .font(.system(size: 12))
                        .foregroundColor(Color("baseColor"))
                        
                        Text("and")
                            .font(.system(size: 12))
                            .foregroundColor(.gray)
                        
                        Button("Privacy Policy") {
                            // Open privacy policy
                        }
                        .font(.system(size: 12))
                        .foregroundColor(Color("baseColor"))
                    }
                }
                
                Spacer()
            }
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }
    
    // MARK: - Important Notes
    private var importantNotesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "info.circle.fill")
                    .foregroundColor(.orange)
                Text("Important Information")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
            }
            
            VStack(alignment: .leading, spacing: 8) {
                BulletPoint(text: "Your subscription will auto-renew unless cancelled")
                BulletPoint(text: "You can cancel anytime from your account settings")
                BulletPoint(text: "No refunds for partial billing periods")
                BulletPoint(text: "Access begins immediately after purchase")
            }
        }
        .padding()
        .background(Color.orange.opacity(0.1))
        .cornerRadius(12)
    }
    
    // MARK: - Helper Functions
    private func calculateTax() -> String {
        guard let plan = planDetails,
              let price = Double(plan.price) else {
            return "0.00"
        }
        
        let tax = price * 0.08 // 8% tax
        return String(format: "%.2f", tax)
    }
    
    private func calculateTotal() -> String {
        guard let plan = planDetails,
              let price = Double(plan.price) else {
            return "0.00"
        }
        
        let tax = price * 0.08
        let total = price + tax
        return String(format: "%.2f", total)
    }
}

// MARK: - Bullet Point
struct BulletPoint: View {
    let text: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Text("•")
                .foregroundColor(.gray)
            Text(text)
                .font(.system(size: 12))
                .foregroundColor(.gray)
        }
    }
}