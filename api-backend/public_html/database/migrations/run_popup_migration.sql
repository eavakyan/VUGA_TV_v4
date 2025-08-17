-- Popup System Migration
-- Run this SQL directly on your database

-- Create popup definitions table
CREATE TABLE IF NOT EXISTS popup_definition (
    popup_definition_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    popup_key VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique identifier for popup type',
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    popup_type ENUM('info', 'feature', 'warning', 'promotion', 'onboarding') DEFAULT 'info',
    target_audience JSON NULL COMMENT 'Targeting rules JSON',
    is_active TINYINT(1) DEFAULT 1,
    priority INTEGER DEFAULT 0 COMMENT 'Higher numbers shown first',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_active_priority (is_active, priority DESC),
    INDEX idx_popup_key (popup_key)
);

-- Create user popup status tracking table
CREATE TABLE IF NOT EXISTS app_user_popup_status (
    app_user_popup_status_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    app_user_id BIGINT UNSIGNED NOT NULL,
    popup_definition_id BIGINT UNSIGNED NOT NULL,
    popup_key VARCHAR(100) NOT NULL COMMENT 'Denormalized for fast lookups',
    status ENUM('shown', 'dismissed', 'acknowledged') DEFAULT 'shown',
    shown_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dismissed_at TIMESTAMP NULL,
    device_type VARCHAR(50) NULL COMMENT 'iOS, Android, AndroidTV',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (app_user_id) REFERENCES app_user(app_user_id) ON DELETE CASCADE,
    FOREIGN KEY (popup_definition_id) REFERENCES popup_definition(popup_definition_id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_user_popup (app_user_id, popup_definition_id),
    INDEX idx_user_status (app_user_id, status),
    INDEX idx_popup_key_user (popup_key, app_user_id),
    INDEX idx_shown_at (shown_at),
    INDEX idx_device_type (device_type)
);

-- Create analytics table
CREATE TABLE IF NOT EXISTS popup_analytics (
    popup_analytics_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    popup_definition_id BIGINT UNSIGNED NOT NULL,
    popup_key VARCHAR(100) NOT NULL,
    total_shown INTEGER DEFAULT 0,
    total_dismissed INTEGER DEFAULT 0,
    total_acknowledged INTEGER DEFAULT 0,
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (popup_definition_id) REFERENCES popup_definition(popup_definition_id) ON DELETE CASCADE,
    UNIQUE KEY unique_popup_analytics (popup_definition_id),
    INDEX idx_popup_key (popup_key)
);

-- Insert sample popup definitions
INSERT IGNORE INTO popup_definition (popup_key, title, content, popup_type, target_audience, priority, is_active, created_at, updated_at) VALUES
('welcome_new_user', 'Welcome to VUGA TV!', 'Discover thousands of movies and TV shows. Create profiles for your family and enjoy personalized recommendations.', 'onboarding', '{"user_type": "new"}', 100, 1, NOW(), NOW()),
('create_profile_prompt', 'Create Your Profile', 'Personalize your experience! Create a profile to get better recommendations and track your watching progress.', 'feature', '{"user_type": "new"}', 90, 1, NOW(), NOW()),
('premium_upgrade_prompt', 'Upgrade to Premium', 'Get unlimited access to all content, HD streaming, and exclusive shows with VUGA Premium.', 'promotion', '{"subscription_status": "free"}', 80, 1, NOW(), NOW()),
('parental_controls_info', 'Set Up Parental Controls', 'Keep your family safe with age-appropriate content filters and kids profiles.', 'info', '{"user_type": "existing"}', 70, 1, NOW(), NOW()),
('download_feature_announcement', 'New: Offline Downloads', 'Download your favorite shows and movies to watch offline, anytime, anywhere!', 'feature', '{"device_types": ["iOS", "Android"]}', 60, 1, NOW(), NOW());

-- Initialize analytics for sample popups
INSERT IGNORE INTO popup_analytics (popup_definition_id, popup_key, total_shown, total_dismissed, total_acknowledged, last_calculated_at, created_at, updated_at)
SELECT 
    popup_definition_id, 
    popup_key, 
    0, 
    0, 
    0, 
    NOW(), 
    NOW(), 
    NOW()
FROM popup_definition;