import SwiftUI

struct SubscriptionsView: View {
    @AppStorage(SessionKeys.language) var language = LocalizationService.shared.language
    @StateObject private var viewModel = SubscriptionsViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                SimpleBackButton(onTap: { Navigation.pop() }, iconSize: 15)
                Spacer()
                Text("Subscriptions")
                    .outfitSemiBold(20)
                    .foregroundColor(.text)
                Spacer()
                SimpleBackButton(onTap: {}, iconSize: 15)
                    .hidden()
            }
            .padding(.horizontal)
            .frame(height: 50)

            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    CurrentPlanCard(current: viewModel.currentSubscription)
                    if !viewModel.addOns.isEmpty {
                        AddOnsSection(addOns: viewModel.addOns)
                    }
                    PlansSection(plans: viewModel.availablePlans, selected: $viewModel.selectedPlanId) {
                        viewModel.select(planId: $0)
                    }
                    CommonButton(title: viewModel.purchaseButtonTitle, isDisable: viewModel.isLoading || viewModel.selectedPlanId == nil) {
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
    @Published var currentSubscription: UserSubscription? = nil
    @Published var addOns: [SubscriptionAddOn] = []
    @Published var availablePlans: [SubscriptionPlan] = []
    @Published var selectedPlanId: Int? = nil

    var purchaseButtonTitle: String { selectedPlanId == nil ? "Select a plan" : "Continue to Purchase" }

    func fetch() {
        guard let userId = myUser?.id else { seedFallbackPlans(); return }
        startLoading()
        let params: [Params: Any] = [.userId: userId]
        NetworkManager.callWebService(url: .getUserSubscription, params: params) { [weak self] (obj: GetUserSubscriptionDTO) in
            self?.stopLoading()
            // Map to view state
            if let sub = obj.subscription {
                self?.currentSubscription = UserSubscription(planId: sub.planId ?? 0,
                                                             planName: sub.planName ?? "",
                                                             status: (sub.status ?? "").lowercased(),
                                                             renewsAt: sub.renewsAt,
                                                             cancelsAt: sub.cancelsAt)
                self?.selectedPlanId = sub.planId
            }
            // API may not provide plans/addons yet; seed if empty
            if (self?.availablePlans.isEmpty ?? true) { self?.seedFallbackPlans() }
        }
    }

    private func seedFallbackPlans() {
        // Basic Netflix-like tiers
        availablePlans = [
            SubscriptionPlan(id: 1, name: "Basic", price: "$6.99", interval: "monthly", features: ["Good video quality", "Watch on 1 device", "Ad-supported"]),
            SubscriptionPlan(id: 2, name: "Standard", price: "$15.49", interval: "monthly", features: ["HD video quality", "Watch on 2 devices", "No ads"]),
            SubscriptionPlan(id: 3, name: "Premium", price: "$22.99", interval: "monthly", features: ["Ultra HD", "Watch on 4 devices", "No ads"])
        ]
        if selectedPlanId == nil { selectedPlanId = availablePlans.first?.id }
    }

    func select(planId: Int) { selectedPlanId = planId }
    func purchaseSelectedPlan() {
        guard let planId = selectedPlanId else { return }
        print("Purchasing plan id: \(planId)")
        // TODO: integrate purchase flow
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
struct CurrentPlanCard: View {
    let current: UserSubscription?
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Current Plan")
                .outfitMedium(16)
                .foregroundColor(.text)
            if let c = current {
                Text(c.planName)
                    .outfitSemiBold(18)
                    .foregroundColor(.white)
                HStack(spacing: 8) {
                    Label(c.status.capitalized, systemImage: "checkmark.seal.fill")
                        .foregroundColor(.green)
                        .font(.system(size: 13))
                    if let renew = c.renewsAt, !renew.isEmpty {
                        Text("Renews on \(renew)")
                            .outfitLight(13)
                            .foregroundColor(.textLight)
                    }
                    if let cancels = c.cancelsAt, !cancels.isEmpty {
                        Text("Cancels on \(cancels)")
                            .outfitLight(13)
                            .foregroundColor(.textLight)
                    }
                }
            } else {
                Text("No active subscription")
                    .outfitLight(14)
                    .foregroundColor(.textLight)
            }
        }
        .padding()
        .background(Color("cardBg"))
        .cornerRadius(12)
    }
}

struct AddOnsSection: View {
    let addOns: [SubscriptionAddOn]
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Add-ons")
                .outfitMedium(16)
                .foregroundColor(.text)
            ForEach(addOns) { addOn in
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(addOn.name)
                            .outfitMedium(15)
                        Text(addOn.description)
                            .outfitLight(13)
                            .foregroundColor(.textLight)
                            .lineLimit(2)
                    }
                    Spacer()
                    Text(addOn.price)
                        .outfitSemiBold(16)
                }
                .padding()
                .background(Color("cardBg").opacity(0.6))
                .cornerRadius(12)
            }
        }
    }
}

struct PlansSection: View {
    let plans: [SubscriptionPlan]
    @Binding var selected: Int?
    var onSelect: (Int) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Available Plans")
                .outfitMedium(16)
                .foregroundColor(.text)
            ForEach(plans) { plan in
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(plan.name)
                                .outfitSemiBold(16)
                            Text("\(plan.interval.capitalized) • \(plan.price)")
                                .outfitLight(13)
                                .foregroundColor(.textLight)
                        }
                        Spacer()
                        Radio(isSelected: selected == plan.id)
                    }
                    if !plan.features.isEmpty {
                        VStack(alignment: .leading, spacing: 4) {
                            ForEach(plan.features, id: \.self) { feature in
                                HStack(spacing: 6) {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 12, weight: .bold))
                                    Text(feature)
                                        .outfitLight(13)
                                }
                            }
                        }
                    }
                }
                .padding()
                .background(Color("cardBg").opacity(0.6))
                .cornerRadius(12)
                .onTap { selected = plan.id; onSelect(plan.id) }
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
