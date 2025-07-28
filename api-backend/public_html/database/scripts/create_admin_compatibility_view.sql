-- Create a view for 'user' table that admin panel expects
-- This maps to our app_user table

CREATE OR REPLACE VIEW user AS 
SELECT 
    app_user_id AS user_id,
    fullname,
    email,
    login_type,
    identity,
    profile_image,
    watchlist_content_ids,
    device_type,
    device_token,
    created_at,
    updated_at
FROM app_user;