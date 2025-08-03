-- Age Limit Tables for Content Restrictions

-- Age limit categories
CREATE TABLE IF NOT EXISTS age_limits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    min_age INT NOT NULL,
    description VARCHAR(255),
    code VARCHAR(10) UNIQUE NOT NULL, -- e.g., 'G', 'PG', 'PG-13', 'R', 'NC-17'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default age ratings
INSERT INTO age_limits (name, min_age, code, description) VALUES
('General Audiences', 0, 'G', 'All ages admitted'),
('Parental Guidance', 7, 'PG', 'Some material may not be suitable for children'),
('Parents Strongly Cautioned', 13, 'PG-13', 'Some material may be inappropriate for children under 13'),
('Restricted', 17, 'R', 'Under 17 requires accompanying parent or adult guardian'),
('Adults Only', 18, 'NC-17', 'No one 17 and under admitted');

-- Content age limit mapping
CREATE TABLE IF NOT EXISTS content_age_limits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    content_id INT NOT NULL,
    age_limit_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE,
    FOREIGN KEY (age_limit_id) REFERENCES age_limits(id) ON DELETE CASCADE,
    UNIQUE KEY unique_content_age (content_id, age_limit_id)
);

-- Add age columns to app_user_profile if not exists
ALTER TABLE app_user_profile 
ADD COLUMN IF NOT EXISTS age INT DEFAULT NULL,
ADD COLUMN IF NOT EXISTS is_kids_profile BOOLEAN DEFAULT FALSE;

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_content_age_limits_content ON content_age_limits(content_id);
CREATE INDEX IF NOT EXISTS idx_content_age_limits_age ON content_age_limits(age_limit_id);
CREATE INDEX IF NOT EXISTS idx_app_user_profile_age ON app_user_profile(age);