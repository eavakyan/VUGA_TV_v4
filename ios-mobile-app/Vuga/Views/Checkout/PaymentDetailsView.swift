//
//  PaymentDetailsView.swift
//  Vuga
//
//  Payment details input step in checkout flow
//

import SwiftUI

struct PaymentDetailsView: View {
    @ObservedObject var viewModel: CheckoutViewModel
    @FocusState private var focusedField: Field?
    
    enum Field {
        case cardNumber, cardholderName, expiryMonth, expiryYear, cvv
        case street, city, state, zipCode
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Payment method specific content
                if let method = viewModel.selectedPaymentMethod {
                    switch method {
                    case .creditCard, .debitCard:
                        cardDetailsSection
                        billingAddressSection
                    case .paypal:
                        payPalSection
                    case .applePay:
                        applePaySection
                    case .googlePay:
                        googlePaySection
                    }
                }
            }
            .padding()
            .padding(.bottom, 100)
        }
    }
    
    // MARK: - Card Details Section
    private var cardDetailsSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("Card Information")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            
            VStack(spacing: 16) {
                // Card Number
                CustomTextField(
                    title: "Card Number",
                    text: $viewModel.cardDetails.cardNumber,
                    placeholder: "1234 5678 9012 3456",
                    keyboardType: .numberPad
                )
                .focused($focusedField, equals: .cardNumber)
                .onChange(of: viewModel.cardDetails.cardNumber) { newValue in
                    viewModel.cardDetails.cardNumber = formatCardNumber(newValue)
                }
                
                // Cardholder Name
                CustomTextField(
                    title: "Cardholder Name",
                    text: $viewModel.cardDetails.cardholderName,
                    placeholder: "John Doe",
                    keyboardType: .default
                )
                .focused($focusedField, equals: .cardholderName)
                
                // Expiry and CVV
                HStack(spacing: 16) {
                    HStack(spacing: 8) {
                        CustomTextField(
                            title: "MM",
                            text: $viewModel.cardDetails.expiryMonth,
                            placeholder: "MM",
                            keyboardType: .numberPad
                        )
                        .focused($focusedField, equals: .expiryMonth)
                        .frame(width: 60)
                        .onChange(of: viewModel.cardDetails.expiryMonth) { newValue in
                            if newValue.count > 2 {
                                viewModel.cardDetails.expiryMonth = String(newValue.prefix(2))
                            }
                        }
                        
                        Text("/")
                            .foregroundColor(.gray)
                        
                        CustomTextField(
                            title: "YY",
                            text: $viewModel.cardDetails.expiryYear,
                            placeholder: "YY",
                            keyboardType: .numberPad
                        )
                        .focused($focusedField, equals: .expiryYear)
                        .frame(width: 60)
                        .onChange(of: viewModel.cardDetails.expiryYear) { newValue in
                            if newValue.count > 2 {
                                viewModel.cardDetails.expiryYear = String(newValue.prefix(2))
                            }
                        }
                    }
                    
                    Spacer()
                    
                    CustomTextField(
                        title: "CVV",
                        text: $viewModel.cardDetails.cvv,
                        placeholder: "123",
                        keyboardType: .numberPad,
                        isSecure: true
                    )
                    .focused($focusedField, equals: .cvv)
                    .frame(width: 80)
                    .onChange(of: viewModel.cardDetails.cvv) { newValue in
                        if newValue.count > 4 {
                            viewModel.cardDetails.cvv = String(newValue.prefix(4))
                        }
                    }
                }
            }
        }
    }
    
    // MARK: - Billing Address Section
    private var billingAddressSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("Billing Address")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            
            VStack(spacing: 16) {
                CustomTextField(
                    title: "Street Address",
                    text: $viewModel.billingAddress.street,
                    placeholder: "123 Main St",
                    keyboardType: .default
                )
                .focused($focusedField, equals: .street)
                
                HStack(spacing: 16) {
                    CustomTextField(
                        title: "City",
                        text: $viewModel.billingAddress.city,
                        placeholder: "New York",
                        keyboardType: .default
                    )
                    .focused($focusedField, equals: .city)
                    
                    CustomTextField(
                        title: "State",
                        text: $viewModel.billingAddress.state,
                        placeholder: "NY",
                        keyboardType: .default
                    )
                    .focused($focusedField, equals: .state)
                    .frame(width: 80)
                }
                
                HStack(spacing: 16) {
                    CustomTextField(
                        title: "ZIP Code",
                        text: $viewModel.billingAddress.zipCode,
                        placeholder: "10001",
                        keyboardType: .numberPad
                    )
                    .focused($focusedField, equals: .zipCode)
                    .frame(width: 120)
                    
                    Spacer()
                }
                
                // Country selector
                VStack(alignment: .leading, spacing: 8) {
                    Text("Country")
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                    
                    HStack {
                        Text(viewModel.billingAddress.country)
                            .foregroundColor(.white)
                        Spacer()
                        Image(systemName: "chevron.down")
                            .foregroundColor(.gray)
                    }
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(8)
                }
            }
        }
    }
    
    // MARK: - PayPal Section
    private var payPalSection: some View {
        VStack(spacing: 20) {
            Image(systemName: "p.circle.fill")
                .font(.system(size: 80))
                .foregroundColor(.blue)
            
            Text("PayPal Checkout")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            Text("You will be redirected to PayPal to complete your purchase securely.")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text("No need to enter card details")
                        .foregroundColor(.white)
                }
                
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text("PayPal Buyer Protection")
                        .foregroundColor(.white)
                }
                
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text("Fast and secure checkout")
                        .foregroundColor(.white)
                }
            }
            .font(.system(size: 14))
            .padding(.top, 20)
        }
        .padding(.top, 40)
    }
    
    // MARK: - Apple Pay Section
    private var applePaySection: some View {
        VStack(spacing: 20) {
            Image(systemName: "applelogo")
                .font(.system(size: 80))
                .foregroundColor(.white)
            
            Text("Apple Pay")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            Text("Complete your purchase with Touch ID or Face ID")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            // Show saved cards if available
            VStack(alignment: .leading, spacing: 12) {
                Text("Your Cards")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                
                HStack {
                    Image(systemName: "creditcard.fill")
                        .foregroundColor(.gray)
                    Text("•••• 1234")
                        .foregroundColor(.white)
                    Spacer()
                    Text("Visa")
                        .foregroundColor(.gray)
                }
                .padding()
                .background(Color.gray.opacity(0.1))
                .cornerRadius(8)
            }
            .padding(.top, 20)
        }
        .padding(.top, 40)
    }
    
    // MARK: - Google Pay Section
    private var googlePaySection: some View {
        VStack(spacing: 20) {
            Image(systemName: "g.circle.fill")
                .font(.system(size: 80))
                .foregroundColor(.blue)
            
            Text("Google Pay")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            Text("This payment method is not available on iOS devices")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
        }
        .padding(.top, 40)
    }
    
    // MARK: - Helper Functions
    private func formatCardNumber(_ number: String) -> String {
        let cleaned = number.replacingOccurrences(of: " ", with: "")
        let limited = String(cleaned.prefix(16))
        
        var formatted = ""
        for (index, character) in limited.enumerated() {
            if index > 0 && index % 4 == 0 {
                formatted += " "
            }
            formatted.append(character)
        }
        return formatted
    }
}

// MARK: - Custom Text Field
struct CustomTextField: View {
    let title: String
    @Binding var text: String
    let placeholder: String
    var keyboardType: UIKeyboardType = .default
    var isSecure: Bool = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 12))
                .foregroundColor(.gray)
            
            if isSecure {
                SecureField(placeholder, text: $text)
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(8)
                    .foregroundColor(.white)
            } else {
                TextField(placeholder, text: $text)
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(8)
                    .foregroundColor(.white)
                    .keyboardType(keyboardType)
            }
        }
    }
}