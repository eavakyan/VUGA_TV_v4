-- Payment and Subscription System Expansion
-- Adds payment history, promo codes, revenue sharing, and subscription pricing

-- 1. Subscription Pricing Table
CREATE TABLE IF NOT EXISTS `subscription_pricing` (
  `pricing_id` int(11) NOT NULL AUTO_INCREMENT,
  `pricing_type` enum('base','distributor') NOT NULL DEFAULT 'base',
  `content_distributor_id` int(11) DEFAULT NULL COMMENT 'NULL for base subscription, otherwise specific distributor',
  `billing_period` enum('monthly','quarterly','yearly','lifetime') NOT NULL DEFAULT 'monthly',
  `price` decimal(10,2) NOT NULL,
  `currency` varchar(3) NOT NULL DEFAULT 'USD',
  `display_name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `sort_order` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`pricing_id`),
  KEY `idx_pricing_type_active` (`pricing_type`, `is_active`),
  KEY `idx_distributor_pricing` (`content_distributor_id`),
  CONSTRAINT `fk_pricing_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. Promo Codes Table
CREATE TABLE IF NOT EXISTS `promo_code` (
  `promo_code_id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL UNIQUE,
  `description` varchar(255) DEFAULT NULL,
  `discount_type` enum('percentage','fixed_amount','free_period') NOT NULL DEFAULT 'percentage',
  `discount_value` decimal(10,2) NOT NULL COMMENT 'Percentage (0-100), fixed amount, or days for free period',
  `applicable_to` enum('base','distributor','all') NOT NULL DEFAULT 'all',
  `content_distributor_id` int(11) DEFAULT NULL COMMENT 'Specific distributor if applicable',
  `minimum_purchase` decimal(10,2) DEFAULT 0.00,
  `usage_limit` int(11) DEFAULT NULL COMMENT 'NULL = unlimited',
  `usage_count` int(11) DEFAULT 0,
  `user_limit` int(11) DEFAULT 1 COMMENT 'Times a single user can use this code',
  `valid_from` timestamp NOT NULL DEFAULT current_timestamp(),
  `valid_until` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_by` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`promo_code_id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_code_active` (`code`, `is_active`),
  KEY `idx_valid_dates` (`valid_from`, `valid_until`),
  KEY `idx_distributor_promo` (`content_distributor_id`),
  CONSTRAINT `fk_promo_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_promo_admin` FOREIGN KEY (`created_by`) REFERENCES `admin_user` (`admin_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 3. Payment Transaction History Table
CREATE TABLE IF NOT EXISTS `payment_transaction` (
  `transaction_id` int(11) NOT NULL AUTO_INCREMENT,
  `app_user_id` int(10) unsigned NOT NULL,
  `transaction_type` enum('subscription','renewal','upgrade','refund') NOT NULL,
  `subscription_type` enum('base','distributor') NOT NULL,
  `content_distributor_id` int(11) DEFAULT NULL,
  `pricing_id` int(11) DEFAULT NULL,
  `promo_code_id` int(11) DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL COMMENT 'stripe, paypal, apple_pay, google_pay, etc',
  `payment_status` enum('pending','completed','failed','refunded','cancelled') NOT NULL DEFAULT 'pending',
  `currency` varchar(3) NOT NULL DEFAULT 'USD',
  `subtotal` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT 0.00,
  `tax_amount` decimal(10,2) DEFAULT 0.00,
  `total_amount` decimal(10,2) NOT NULL,
  `payment_gateway_id` varchar(255) DEFAULT NULL COMMENT 'External payment provider transaction ID',
  `payment_gateway_response` text DEFAULT NULL COMMENT 'JSON response from payment provider',
  `billing_period` enum('monthly','quarterly','yearly','lifetime') DEFAULT NULL,
  `subscription_start_date` timestamp NULL DEFAULT NULL,
  `subscription_end_date` timestamp NULL DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`transaction_id`),
  KEY `idx_user_transactions` (`app_user_id`, `created_at` DESC),
  KEY `idx_payment_status` (`payment_status`, `created_at` DESC),
  KEY `idx_distributor_trans` (`content_distributor_id`),
  KEY `idx_promo_usage` (`promo_code_id`),
  KEY `idx_pricing_trans` (`pricing_id`),
  CONSTRAINT `fk_trans_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_trans_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_trans_promo` FOREIGN KEY (`promo_code_id`) REFERENCES `promo_code` (`promo_code_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_trans_pricing` FOREIGN KEY (`pricing_id`) REFERENCES `subscription_pricing` (`pricing_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 4. Promo Code Usage Tracking
CREATE TABLE IF NOT EXISTS `promo_code_usage` (
  `usage_id` int(11) NOT NULL AUTO_INCREMENT,
  `promo_code_id` int(11) NOT NULL,
  `app_user_id` int(10) unsigned NOT NULL,
  `transaction_id` int(11) DEFAULT NULL,
  `used_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`usage_id`),
  UNIQUE KEY `uk_user_promo_trans` (`app_user_id`, `promo_code_id`, `transaction_id`),
  KEY `idx_promo_usage` (`promo_code_id`),
  KEY `idx_user_promos` (`app_user_id`),
  CONSTRAINT `fk_usage_promo` FOREIGN KEY (`promo_code_id`) REFERENCES `promo_code` (`promo_code_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usage_user` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usage_trans` FOREIGN KEY (`transaction_id`) REFERENCES `payment_transaction` (`transaction_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 5. Revenue Sharing Configuration (Optional)
CREATE TABLE IF NOT EXISTS `revenue_share_config` (
  `revenue_share_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_distributor_id` int(11) NOT NULL,
  `revenue_share_percentage` decimal(5,2) NOT NULL COMMENT 'Percentage to distributor (0-100)',
  `minimum_payout` decimal(10,2) DEFAULT 0.00 COMMENT 'Minimum amount before payout',
  `payment_terms_days` int(11) DEFAULT 30 COMMENT 'Days after period end for payment',
  `is_active` tinyint(1) DEFAULT 1,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`revenue_share_id`),
  UNIQUE KEY `uk_distributor_revshare` (`content_distributor_id`),
  CONSTRAINT `fk_revshare_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 6. Revenue Share Payouts Tracking
CREATE TABLE IF NOT EXISTS `revenue_share_payout` (
  `payout_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_distributor_id` int(11) NOT NULL,
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  `total_revenue` decimal(10,2) NOT NULL,
  `share_percentage` decimal(5,2) NOT NULL,
  `payout_amount` decimal(10,2) NOT NULL,
  `payout_status` enum('pending','processing','completed','failed') DEFAULT 'pending',
  `payout_date` timestamp NULL DEFAULT NULL,
  `payment_reference` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`payout_id`),
  KEY `idx_distributor_payouts` (`content_distributor_id`, `period_start`),
  KEY `idx_payout_status` (`payout_status`),
  CONSTRAINT `fk_payout_distributor` FOREIGN KEY (`content_distributor_id`) REFERENCES `content_distributor` (`content_distributor_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Insert default pricing for base subscription
INSERT INTO `subscription_pricing` (`pricing_type`, `billing_period`, `price`, `currency`, `display_name`, `description`) VALUES
('base', 'monthly', 9.99, 'USD', 'Basic Monthly', 'Access to all basic content library'),
('base', 'yearly', 99.99, 'USD', 'Basic Yearly', 'Save 17% with annual billing');

-- Insert development test promo code (100% off)
INSERT INTO `promo_code` (`code`, `description`, `discount_type`, `discount_value`, `applicable_to`, `usage_limit`, `valid_until`, `created_by`) VALUES
('DEVTEST100', 'Development testing - 100% discount', 'percentage', 100.00, 'all', NULL, DATE_ADD(NOW(), INTERVAL 1 YEAR), 1),
('LAUNCH50', 'Launch promotion - 50% off', 'percentage', 50.00, 'base', 1000, DATE_ADD(NOW(), INTERVAL 3 MONTH), 1),
('PREMIUM30', 'Premium content - 30% off', 'percentage', 30.00, 'distributor', 500, DATE_ADD(NOW(), INTERVAL 2 MONTH), 1);

-- Create view for active user subscriptions with payment info
CREATE OR REPLACE VIEW `v_active_subscriptions` AS
SELECT 
    ubs.app_user_id,
    'base' as subscription_type,
    NULL as content_distributor_id,
    ubs.start_date,
    ubs.end_date,
    ubs.is_active,
    pt.total_amount as last_payment_amount,
    pt.payment_status as last_payment_status,
    pt.created_at as last_payment_date
FROM user_base_subscription ubs
LEFT JOIN (
    SELECT app_user_id, total_amount, payment_status, created_at,
           ROW_NUMBER() OVER (PARTITION BY app_user_id ORDER BY created_at DESC) as rn
    FROM payment_transaction
    WHERE subscription_type = 'base' AND payment_status = 'completed'
) pt ON ubs.app_user_id = pt.app_user_id AND pt.rn = 1
WHERE ubs.is_active = 1

UNION ALL

SELECT 
    uda.app_user_id,
    'distributor' as subscription_type,
    uda.content_distributor_id,
    uda.start_date,
    uda.end_date,
    uda.is_active,
    pt.total_amount as last_payment_amount,
    pt.payment_status as last_payment_status,
    pt.created_at as last_payment_date
FROM user_distributor_access uda
LEFT JOIN (
    SELECT app_user_id, content_distributor_id, total_amount, payment_status, created_at,
           ROW_NUMBER() OVER (PARTITION BY app_user_id, content_distributor_id ORDER BY created_at DESC) as rn
    FROM payment_transaction
    WHERE subscription_type = 'distributor' AND payment_status = 'completed'
) pt ON uda.app_user_id = pt.app_user_id AND uda.content_distributor_id = pt.content_distributor_id AND pt.rn = 1
WHERE uda.is_active = 1;