//
//  CheckoutView.swift
//  Vuga
//
//  Comprehensive checkout flow for subscription purchases
//

import SwiftUI

struct CheckoutView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject private var viewModel = CheckoutViewModel()
    @Environment(\.dismiss) private var dismiss
    
    let planId: Int
    let planType: String // "base" or "distributor"
    let planDetails: SubscriptionPricingModel?
    
    var body: some View {
        ZStack {
            Color("bgColor").ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header with progress indicator
                checkoutHeader
                
                // Progress bar
                ProgressBar(currentStep: viewModel.currentStep, totalSteps: 3)
                    .padding(.horizontal)
                    .padding(.vertical, 10)
                
                // Main content
                Group {
                    switch viewModel.currentStep {
                    case 1:
                        PaymentMethodSelectionView(viewModel: viewModel)
                    case 2:
                        PaymentDetailsView(viewModel: viewModel)
                    case 3:
                        OrderReviewView(viewModel: viewModel, planDetails: planDetails)
                    default:
                        EmptyView()
                    }
                }
                .transition(.asymmetric(
                    insertion: .move(edge: .trailing),
                    removal: .move(edge: .leading)
                ))
                .animation(.easeInOut(duration: 0.3), value: viewModel.currentStep)
                
                Spacer()
                
                // Bottom action buttons
                bottomButtons
            }
        }
        .navigationBarHidden(true)
        .loaderView(viewModel.isProcessing)
        .alert("Payment Successful", isPresented: $viewModel.showSuccessAlert) {
            Button("OK") {
                dismiss()
            }
        } message: {
            Text("Your subscription has been activated successfully!")
        }
        .alert("Payment Failed", isPresented: $viewModel.showErrorAlert) {
            Button("Try Again") {
                viewModel.showErrorAlert = false
            }
            Button("Cancel") {
                dismiss()
            }
        } message: {
            Text(viewModel.errorMessage)
        }
        .onAppear {
            viewModel.initializeCheckout(planId: planId, planType: planType)
        }
    }
    
    private var checkoutHeader: some View {
        HStack {
            Button(action: {
                if viewModel.currentStep > 1 {
                    viewModel.previousStep()
                } else {
                    dismiss()
                }
            }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundColor(.white)
            }
            
            Spacer()
            
            Text(viewModel.stepTitle)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
            
            Spacer()
            
            Button(action: { dismiss() }) {
                Image(systemName: "xmark")
                    .font(.system(size: 18))
                    .foregroundColor(.white.opacity(0.7))
            }
        }
        .padding()
        .background(Color("bgColor"))
    }
    
    private var bottomButtons: some View {
        HStack(spacing: 12) {
            if viewModel.currentStep > 1 {
                Button(action: {
                    viewModel.previousStep()
                }) {
                    Text("Back")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(Color.gray.opacity(0.3))
                        .cornerRadius(12)
                }
            }
            
            Button(action: {
                if viewModel.currentStep < 3 {
                    viewModel.nextStep()
                } else {
                    viewModel.processPayment()
                }
            }) {
                Text(viewModel.currentStep == 3 ? "Complete Purchase" : "Continue")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(viewModel.canProceed ? .white : .gray)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(
                        viewModel.canProceed ? 
                        Color("baseColor") : 
                        Color.gray.opacity(0.3)
                    )
                    .cornerRadius(12)
            }
            .disabled(!viewModel.canProceed)
        }
        .padding()
    }
}

// MARK: - Progress Bar
struct ProgressBar: View {
    let currentStep: Int
    let totalSteps: Int
    
    var body: some View {
        HStack(spacing: 8) {
            ForEach(1...totalSteps, id: \.self) { step in
                if step > 1 {
                    Rectangle()
                        .fill(step <= currentStep ? Color("baseColor") : Color.gray.opacity(0.3))
                        .frame(height: 2)
                }
                
                ZStack {
                    Circle()
                        .fill(step <= currentStep ? Color("baseColor") : Color.gray.opacity(0.3))
                        .frame(width: 30, height: 30)
                    
                    if step < currentStep {
                        Image(systemName: "checkmark")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                    } else {
                        Text("\(step)")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(step <= currentStep ? .white : .gray)
                    }
                }
            }
        }
    }
}

// MARK: - Checkout View Model
class CheckoutViewModel: ObservableObject {
    @Published var currentStep = 1
    @Published var selectedPaymentMethod: PaymentMethod?
    @Published var cardDetails = CardDetails()
    @Published var billingAddress = BillingAddress()
    @Published var isProcessing = false
    @Published var showSuccessAlert = false
    @Published var showErrorAlert = false
    @Published var errorMessage = ""
    @Published var agreedToTerms = false
    
    private var planId: Int = 0
    private var planType: String = ""
    
    var stepTitle: String {
        switch currentStep {
        case 1: return "Payment Method"
        case 2: return "Payment Details"
        case 3: return "Review Order"
        default: return ""
        }
    }
    
    var canProceed: Bool {
        switch currentStep {
        case 1:
            return selectedPaymentMethod != nil
        case 2:
            return isPaymentDetailsValid()
        case 3:
            return agreedToTerms
        default:
            return false
        }
    }
    
    func initializeCheckout(planId: Int, planType: String) {
        self.planId = planId
        self.planType = planType
    }
    
    func nextStep() {
        if currentStep < 3 {
            currentStep += 1
        }
    }
    
    func previousStep() {
        if currentStep > 1 {
            currentStep -= 1
        }
    }
    
    func processPayment() {
        isProcessing = true
        
        // Simulate payment processing
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) { [weak self] in
            self?.isProcessing = false
            
            // For demo purposes, randomly succeed or fail
            if Bool.random() {
                self?.showSuccessAlert = true
                self?.submitPurchaseToBackend()
            } else {
                self?.errorMessage = "Payment was declined. Please check your payment details and try again."
                self?.showErrorAlert = true
            }
        }
    }
    
    private func submitPurchaseToBackend() {
        // TODO: Submit successful purchase to backend
        guard let userId = SessionManager.shared.currentUser?.id else { return }
        
        let params: [String: Any] = [
            "user_id": userId,
            "plan_id": planId,
            "plan_type": planType,
            "payment_method": selectedPaymentMethod?.rawValue ?? "",
            "transaction_id": UUID().uuidString
        ]
        
        // NetworkManager.callWebService(url: .createSubscription, params: params) { response in
        //     // Handle response
        // }
    }
    
    private func isPaymentDetailsValid() -> Bool {
        guard let method = selectedPaymentMethod else { return false }
        
        switch method {
        case .creditCard, .debitCard:
            return cardDetails.isValid
        case .paypal:
            return true // PayPal will handle validation
        case .applePay:
            return true // Apple Pay handles validation
        case .googlePay:
            return true // Google Pay handles validation
        }
    }
}

// MARK: - Payment Models
enum PaymentMethod: String, CaseIterable {
    case creditCard = "credit_card"
    case debitCard = "debit_card"
    case paypal = "paypal"
    case applePay = "apple_pay"
    case googlePay = "google_pay"
    
    var title: String {
        switch self {
        case .creditCard: return "Credit Card"
        case .debitCard: return "Debit Card"
        case .paypal: return "PayPal"
        case .applePay: return "Apple Pay"
        case .googlePay: return "Google Pay"
        }
    }
    
    var icon: String {
        switch self {
        case .creditCard: return "creditcard.fill"
        case .debitCard: return "creditcard"
        case .paypal: return "p.circle.fill"
        case .applePay: return "applelogo"
        case .googlePay: return "g.circle.fill"
        }
    }
    
    var isAvailable: Bool {
        switch self {
        case .applePay:
            #if os(iOS)
            return true
            #else
            return false
            #endif
        case .googlePay:
            return false // Not available on iOS
        default:
            return true
        }
    }
}

struct CardDetails {
    var cardNumber = ""
    var cardholderName = ""
    var expiryMonth = ""
    var expiryYear = ""
    var cvv = ""
    
    var isValid: Bool {
        !cardNumber.isEmpty &&
        cardNumber.count >= 15 &&
        !cardholderName.isEmpty &&
        !expiryMonth.isEmpty &&
        !expiryYear.isEmpty &&
        !cvv.isEmpty &&
        cvv.count >= 3
    }
}

struct BillingAddress {
    var street = ""
    var city = ""
    var state = ""
    var zipCode = ""
    var country = "United States"
}