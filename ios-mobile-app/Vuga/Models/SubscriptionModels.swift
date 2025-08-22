//
//  SubscriptionModels.swift
//  Vuga
//
//

import Foundation

// MARK: - API Response Models

struct SubscriptionPlansResponse: Codable {
    let status: Bool?
    let message: String?
    let data: SubscriptionPlanData?
}

struct SubscriptionPlanData: Codable {
    let base: [SubscriptionPricingModel]?
    let distributors: [SubscriptionPricingModel]?
}

struct MySubscriptionsResponse: Codable {
    let status: Bool?
    let message: String?
    let data: UserSubscriptionData?
}

struct UserSubscriptionData: Codable {
    let baseSubscription: BaseSubscriptionModel?
    let distributorSubscriptions: [DistributorAccessModel]?
    let hasActiveBase: Bool?
    let activeDistributorCount: Int?
    
    enum CodingKeys: String, CodingKey {
        case baseSubscription = "base_subscription"
        case distributorSubscriptions = "distributor_subscriptions"
        case hasActiveBase = "has_active_base"
        case activeDistributorCount = "active_distributor_count"
    }
}

// MARK: - Subscription Models

struct SubscriptionPricingModel: Codable, Identifiable {
    let pricingId: Int
    let pricingType: String
    let billingPeriod: String
    let price: String
    let currency: String
    let displayName: String
    let description: String?
    let distributorName: String?
    let distributorCode: String?
    let distributorLogo: String?
    
    var id: Int { pricingId }
    
    enum CodingKeys: String, CodingKey {
        case pricingId = "pricing_id"
        case pricingType = "pricing_type"
        case billingPeriod = "billing_period"
        case price
        case currency
        case displayName = "display_name"
        case description
        case distributorName = "distributor_name"
        case distributorCode = "distributor_code"
        case distributorLogo = "distributor_logo"
    }
    
    var formattedPrice: String {
        let currencySymbol = currency == "USD" ? "$" : currency
        return "\(currencySymbol)\(price)"
    }
    
    var intervalText: String {
        switch billingPeriod {
        case "daily": return "per day"
        case "weekly": return "per week"
        case "monthly": return "per month"
        case "quarterly": return "per quarter"
        case "yearly": return "per year"
        case "lifetime": return "lifetime"
        default: return billingPeriod
        }
    }
}

struct DistributorModel: Codable, Identifiable {
    let distributorName: String
    let distributorCode: String
    let distributorLogo: String?
    let description: String?
    
    var id: String { distributorCode }
    var name: String { distributorName }
    var code: String { distributorCode }
    var logoUrl: String? { distributorLogo }
    
    init(distributorName: String, distributorCode: String, distributorLogo: String?, description: String? = nil) {
        self.distributorName = distributorName
        self.distributorCode = distributorCode
        self.distributorLogo = distributorLogo
        self.description = description
    }
    
    init(from plan: SubscriptionPricingModel) {
        self.distributorName = plan.distributorName ?? ""
        self.distributorCode = plan.distributorCode ?? ""
        self.distributorLogo = plan.distributorLogo
        self.description = nil // Distributor description not provided by API
    }
}

struct DistributorSubscriptionModel: Identifiable {
    let distributor: DistributorModel
    let plans: [SubscriptionPricingModel]
    
    var id: String { distributor.id }
}

struct BaseSubscriptionModel: Codable {
    let subscriptionId: Int?
    let appUserId: Int?
    let startDate: String?
    let endDate: String?
    let isActive: Int?
    let subscriptionType: String?
    let autoRenew: Int?
    let createdAt: String?
    let updatedAt: String?
    
    var isActiveStatus: Bool {
        isActive == 1
    }
    
    enum CodingKeys: String, CodingKey {
        case subscriptionId = "subscription_id"
        case appUserId = "app_user_id"
        case startDate = "start_date"
        case endDate = "end_date"
        case isActive = "is_active"
        case subscriptionType = "subscription_type"
        case autoRenew = "auto_renew"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}

struct DistributorAccessModel: Codable, Identifiable {
    let accessId: Int?
    let appUserId: Int?
    let contentDistributorId: Int?
    let startDate: String?
    let endDate: String?
    let isActive: Int?
    let subscriptionType: String?
    let autoRenew: Int?
    let createdAt: String?
    let updatedAt: String?
    let distributorName: String?
    let distributorCode: String?
    let distributorLogo: String?
    
    var id: Int { accessId ?? 0 }
    
    var isActiveStatus: Bool {
        isActive == 1
    }
    
    var distributor: DistributorModel {
        DistributorModel(
            distributorName: distributorName ?? "",
            distributorCode: distributorCode ?? "",
            distributorLogo: distributorLogo,
            description: nil
        )
    }
    
    enum CodingKeys: String, CodingKey {
        case accessId = "access_id"
        case appUserId = "app_user_id"
        case contentDistributorId = "content_distributor_id"
        case startDate = "start_date"
        case endDate = "end_date"
        case isActive = "is_active"
        case subscriptionType = "subscription_type"
        case autoRenew = "auto_renew"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case distributorName = "distributor_name"
        case distributorCode = "distributor_code"
        case distributorLogo = "distributor_logo"
    }
}

// MARK: - Helper Extensions

extension BaseSubscriptionModel {
    var statusText: String {
        isActiveStatus ? "Active" : "Inactive"
    }
    
    var formattedEndDate: String? {
        guard let endDate = endDate else { return nil }
        // TODO: Format date properly
        return endDate
    }
}

extension DistributorAccessModel {
    var statusText: String {
        isActiveStatus ? "Active" : "Inactive"
    }
}