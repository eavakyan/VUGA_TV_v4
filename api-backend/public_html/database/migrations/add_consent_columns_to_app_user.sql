-- Add SMS and Email consent columns to APP_USER table
-- These columns track user's consent for marketing communications

ALTER TABLE APP_USER 
ADD COLUMN sms_consent BOOLEAN DEFAULT TRUE COMMENT 'User consent for SMS marketing messages',
ADD COLUMN email_consent BOOLEAN DEFAULT TRUE COMMENT 'User consent for email marketing messages',
ADD COLUMN sms_consent_date TIMESTAMP NULL COMMENT 'Date when SMS consent was given/revoked',
ADD COLUMN email_consent_date TIMESTAMP NULL COMMENT 'Date when email consent was given/revoked',
ADD COLUMN consent_ip_address VARCHAR(45) NULL COMMENT 'IP address when consent was last updated';

-- Add indexes for faster filtering by consent status
CREATE INDEX idx_app_user_sms_consent ON APP_USER(sms_consent);
CREATE INDEX idx_app_user_email_consent ON APP_USER(email_consent);

-- Update existing users to have explicit true consent (opt-out approach)
UPDATE APP_USER 
SET sms_consent = TRUE, 
    email_consent = TRUE,
    sms_consent_date = NOW(),
    email_consent_date = NOW()
WHERE sms_consent IS NULL OR email_consent IS NULL;