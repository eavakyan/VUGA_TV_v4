import SwiftUI

struct SubscriptionsView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject private var viewModel = SubscriptionsViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: {
                    Navigation.pop()
                }) {
                    Image.back
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.white)
                        .padding(10)
                        .contentShape(Rectangle())
                }
                .buttonStyle(PlainButtonStyle())
                
                Spacer()
                
                Text("Subscriptions")
                    .outfitSemiBold(20)
                    .foregroundColor(.text)
                
                Spacer()
                
                // Hidden placeholder for symmetry
                Image.back
                    .font(.system(size: 15, weight: .bold))
                    .padding(10)
                    .opacity(0)
            }
            .padding(.horizontal)
            .frame(height: 50)
            .zIndex(10) // Ensure header is above other content

            ScrollView(showsIndicators: false) {
                VStack(spacing: 20) {
                    // Current Subscriptions Section
                    CurrentSubscriptionsSection(
                        baseSubscription: viewModel.baseSubscription,
                        distributorAccess: viewModel.distributorAccess
                    )
                    
                    // Base Subscription Plans
                    if !viewModel.basePlans.isEmpty {
                        SubscriptionPlansSection(
                            title: "Base Subscription",
                            subtitle: "Required for all content access",
                            plans: viewModel.basePlans,
                            selectedId: viewModel.selectedPlanType == "base" ? viewModel.selectedPlanId : nil,
                            isDisabled: viewModel.hasBaseSubscription
                        ) { planId in
                            viewModel.select(planId: planId, type: "base")
                        }
                    }
                    
                    // Distributor Plans
                    if !viewModel.distributorPlans.isEmpty {
                        VStack(spacing: 16) {
                            Text("Premium Content Distributors")
                                .outfitSemiBold(18)
                                .foregroundColor(.text)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            
                            if !viewModel.hasBaseSubscription {
                                HStack(spacing: 8) {
                                    Image(systemName: "info.circle.fill")
                                        .foregroundColor(.orange)
                                    Text("Base subscription required for premium content")
                                        .outfitLight(13)
                                        .foregroundColor(.textLight)
                                }
                                .padding(12)
                                .background(Color.orange.opacity(0.1))
                                .cornerRadius(8)
                            }
                            
                            ForEach(viewModel.distributorPlans) { distributorPlan in
                                DistributorPlanSection(
                                    distributor: distributorPlan.distributor,
                                    plans: distributorPlan.plans,
                                    selectedId: viewModel.selectedPlanType == "distributor" ? viewModel.selectedPlanId : nil,
                                    hasActiveAccess: viewModel.distributorAccess.contains(where: { $0.distributor.code == distributorPlan.distributor.code && $0.isActiveStatus }),
                                    isDisabled: !viewModel.hasBaseSubscription
                                ) { planId in
                                    viewModel.select(planId: planId, type: "distributor")
                                }
                            }
                        }
                    }
                    
                    CommonButton(
                        title: viewModel.purchaseButtonTitle,
                        isDisable: viewModel.isPurchaseDisabled
                    ) {
                        viewModel.purchaseSelectedPlan()
                    }
                }
                .padding()
            }
        }
        .addBackground()
        .hideNavigationbar()
        .loaderView(viewModel.isLoading)
        .onAppear { viewModel.fetch() }
    }
}

// MARK: - View Model
final class SubscriptionsViewModel: BaseViewModel {
    @Published var baseSubscription: BaseSubscriptionModel? = nil
    @Published var distributorAccess: [DistributorAccessModel] = []
    @Published var basePlans: [SubscriptionPricingModel] = []
    @Published var distributorPlans: [DistributorSubscriptionModel] = []
    @Published var selectedPlanId: Int? = nil
    @Published var selectedPlanType: String = "base" // "base" or "distributor"

    var hasBaseSubscription: Bool {
        baseSubscription?.isActiveStatus ?? false
    }
    
    var purchaseButtonTitle: String { 
        if !hasBaseSubscription && selectedPlanType == "distributor" {
            return "Base subscription required"
        }
        return selectedPlanId == nil ? "Select a plan" : "Continue to Purchase" 
    }
    
    var isPurchaseDisabled: Bool {
        isLoading || selectedPlanId == nil || (!hasBaseSubscription && selectedPlanType == "distributor")
    }

    func fetch() {
        fetchSubscriptions()
        fetchPlans()
    }
    
    private func fetchSubscriptions() {
        guard let userId = myUser?.id else { return }
        startLoading()
        
        let params: [Params: Any] = [.userId: userId]
        NetworkManager.callWebService(url: .getMySubscriptions, params: params) { [weak self] (response: MySubscriptionsResponse) in
            DispatchQueue.main.async {
                self?.baseSubscription = response.data?.baseSubscription
                self?.distributorAccess = response.data?.distributorSubscriptions ?? []
                self?.stopLoading()
            }
        }
    }
    
    private func fetchPlans() {
        startLoading()
        
        NetworkManager.callWebService(url: .getSubscriptionPlans, params: [:]) { [weak self] (response: SubscriptionPlansResponse) in
            DispatchQueue.main.async {
                self?.basePlans = response.data?.base ?? []
                
                // Group distributor plans by distributor
                let distributorPlanMap = Dictionary(grouping: response.data?.distributors ?? [], by: { plan in
                    plan.distributorCode ?? ""
                })
                
                self?.distributorPlans = distributorPlanMap.compactMap { (code, plans) in
                    guard !plans.isEmpty, let firstPlan = plans.first else { return nil }
                    let distributor = DistributorModel(from: firstPlan)
                    return DistributorSubscriptionModel(distributor: distributor, plans: plans)
                }
                
                self?.stopLoading()
            }
        }
    }

    func select(planId: Int, type: String) { 
        selectedPlanId = planId
        selectedPlanType = type
    }
    
    func purchaseSelectedPlan() {
        guard let planId = selectedPlanId else { return }
        
        // Find the selected plan details
        var selectedPlan: SubscriptionPricingModel?
        if selectedPlanType == "base" {
            selectedPlan = basePlans.first(where: { $0.id == planId })
        } else {
            for distributorPlan in distributorPlans {
                if let plan = distributorPlan.plans.first(where: { $0.id == planId }) {
                    selectedPlan = plan
                    break
                }
            }
        }
        
        // Navigate to checkout flow
        Navigation.pushToSwiftUiView(
            CheckoutView(
                planId: planId,
                planType: selectedPlanType,
                planDetails: selectedPlan
            )
        )
    }
}

// MARK: - Network DTOs (lenient)
struct GetUserSubscriptionDTO: Codable {
    let status: Bool?
    let message: String?
    let subscription: SubscriptionDTO?
}

struct SubscriptionDTO: Codable {
    let planId: Int?
    let planName: String?
    let status: String?
    let renewsAt: String?
    let cancelsAt: String?

    enum CodingKeys: String, CodingKey {
        case planId = "plan_id"
        case planName = "plan_name"
        case status
        case renewsAt = "renews_at"
        case cancelsAt = "cancels_at"
    }
}

// MARK: - View Models
struct UserSubscription: Codable {
    let planId: Int
    let planName: String
    let status: String
    let renewsAt: String?
    let cancelsAt: String?
}

struct SubscriptionAddOn: Codable, Identifiable {
    let id: Int
    let name: String
    let description: String
    let price: String
}

struct SubscriptionPlan: Codable, Identifiable {
    let id: Int
    let name: String
    let price: String
    let interval: String // monthly, yearly
    let features: [String]
}

// MARK: - Sections
struct CurrentSubscriptionsSection: View {
    let baseSubscription: BaseSubscriptionModel?
    let distributorAccess: [DistributorAccessModel]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("My Subscriptions")
                .outfitSemiBold(18)
                .foregroundColor(.text)
            
            // Base Subscription Card
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Base Subscription")
                            .outfitMedium(16)
                            .foregroundColor(.white)
                        Text(baseSubscription?.subscriptionType?.capitalized ?? "No active subscription")
                            .outfitLight(13)
                            .foregroundColor(.textLight)
                    }
                    Spacer()
                    if let sub = baseSubscription, sub.isActiveStatus {
                        Label("Active", systemImage: "checkmark.circle.fill")
                            .foregroundColor(.green)
                            .font(.system(size: 13))
                    }
                }
                
                if let endDate = baseSubscription?.formattedEndDate {
                    Text("Renews: \(endDate)")
                        .outfitLight(12)
                        .foregroundColor(.textLight)
                }
            }
            .padding()
            .background(Color("cardBg"))
            .cornerRadius(12)
            
            // Distributor Access Cards
            if !distributorAccess.isEmpty {
                Text("Premium Access")
                    .outfitMedium(16)
                    .foregroundColor(.text)
                    .padding(.top, 8)
                
                ForEach(distributorAccess) { access in
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(access.distributor.name)
                                .outfitMedium(15)
                                .foregroundColor(.white)
                            Text("\(access.subscriptionType?.capitalized ?? "")")
                                .outfitLight(12)
                                .foregroundColor(.textLight)
                        }
                        Spacer()
                        if access.isActiveStatus {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(.green)
                                .font(.system(size: 16))
                        }
                    }
                    .padding()
                    .background(Color("cardBg").opacity(0.8))
                    .cornerRadius(10)
                }
            }
        }
    }
}

struct SubscriptionPlansSection: View {
    let title: String
    let subtitle: String?
    let plans: [SubscriptionPricingModel]
    let selectedId: Int?
    let isDisabled: Bool
    let onSelect: (Int) -> Void
    
    init(title: String, subtitle: String? = nil, plans: [SubscriptionPricingModel], selectedId: Int?, isDisabled: Bool = false, onSelect: @escaping (Int) -> Void) {
        self.title = title
        self.subtitle = subtitle
        self.plans = plans
        self.selectedId = selectedId
        self.isDisabled = isDisabled
        self.onSelect = onSelect
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .outfitSemiBold(18)
                    .foregroundColor(.text)
                if let subtitle = subtitle {
                    Text(subtitle)
                        .outfitLight(13)
                        .foregroundColor(.textLight)
                }
            }
            
            ForEach(plans) { plan in
                PlanCard(
                    plan: plan,
                    isSelected: selectedId == plan.id,
                    isDisabled: isDisabled,
                    onSelect: { onSelect(plan.id) }
                )
            }
        }
    }
}

struct DistributorPlanSection: View {
    let distributor: DistributorModel
    let plans: [SubscriptionPricingModel]
    let selectedId: Int?
    let hasActiveAccess: Bool
    let isDisabled: Bool
    let onSelect: (Int) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(distributor.name)
                        .outfitSemiBold(16)
                        .foregroundColor(.white)
                    if let description = distributor.description {
                        Text(description)
                            .outfitLight(12)
                            .foregroundColor(.textLight)
                            .lineLimit(2)
                    }
                }
                Spacer()
                if hasActiveAccess {
                    Label("Active", systemImage: "checkmark.circle.fill")
                        .foregroundColor(.green)
                        .font(.system(size: 13))
                }
            }
            .padding()
            .background(Color("cardBg").opacity(0.3))
            .cornerRadius(10)
            
            ForEach(plans) { plan in
                PlanCard(
                    plan: plan,
                    isSelected: selectedId == plan.id,
                    isDisabled: isDisabled || hasActiveAccess,
                    onSelect: { onSelect(plan.id) }
                )
                .padding(.leading, 16)
            }
        }
    }
}

struct PlanCard: View {
    let plan: SubscriptionPricingModel
    let isSelected: Bool
    let isDisabled: Bool
    let onSelect: () -> Void
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(plan.displayName)
                    .outfitMedium(15)
                    .foregroundColor(isDisabled ? .textLight : .white)
                HStack(spacing: 8) {
                    Text(plan.formattedPrice)
                        .outfitSemiBold(16)
                        .foregroundColor(isDisabled ? .textLight : .base)
                    Text(plan.intervalText)
                        .outfitLight(13)
                        .foregroundColor(.textLight)
                }
                if let description = plan.description {
                    Text(description)
                        .outfitLight(12)
                        .foregroundColor(.textLight)
                        .lineLimit(2)
                }
            }
            Spacer()
            if !isDisabled {
                Radio(isSelected: isSelected)
            }
        }
        .padding()
        .background(Color("cardBg").opacity(isDisabled ? 0.3 : 0.6))
        .cornerRadius(10)
        .opacity(isDisabled ? 0.6 : 1.0)
        .onTap { 
            if !isDisabled {
                onSelect()
            }
        }
    }
}

struct Radio: View {
    let isSelected: Bool
    var body: some View {
        ZStack {
            Circle().stroke(Color.textLight.opacity(0.4), lineWidth: 2).frame(width: 20, height: 20)
            if isSelected { Circle().fill(Color.base).frame(width: 10, height: 10) }
        }
    }
}
