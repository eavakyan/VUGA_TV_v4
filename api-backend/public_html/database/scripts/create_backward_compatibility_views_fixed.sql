-- Create backward compatibility views for legacy API
-- Only create views for tables that were actually renamed

-- View for users table (renamed to app_user)
CREATE OR REPLACE VIEW users AS 
SELECT 
    app_user_id AS id,
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

-- View for contents table (renamed to content)
CREATE OR REPLACE VIEW contents AS 
SELECT 
    content_id AS id,
    title,
    description,
    type,
    CASE 
        WHEN duration IS NULL THEN NULL
        ELSE CAST(duration AS CHAR)
    END AS duration,
    release_year,
    ratings,
    language_id,
    trailer_url,
    vertical_poster,
    horizontal_poster,
    genre_ids,
    is_featured,
    is_show,
    total_view,
    total_download,
    total_share,
    created_at,
    updated_at
FROM content;

-- View for actors table (renamed to actor)
CREATE OR REPLACE VIEW actors AS 
SELECT 
    actor_id AS id,
    fullname,
    dob,
    bio,
    profile_image,
    created_at,
    updated_at
FROM actor;

-- View for genres table (renamed to genre)
CREATE OR REPLACE VIEW genres AS 
SELECT 
    genre_id AS id,
    title,
    created_at,
    updated_at
FROM genre;

-- View for languages table (renamed to app_language)
CREATE OR REPLACE VIEW languages AS 
SELECT 
    app_language_id AS id,
    title,
    code,
    created_at,
    updated_at
FROM app_language;

-- View for content_sources table (renamed to content_source)
CREATE OR REPLACE VIEW content_sources AS 
SELECT 
    content_source_id AS id,
    content_id,
    title,
    quality,
    size,
    is_download,
    access_type,
    type,
    source,
    created_at,
    updated_at
FROM content_source;

-- View for seasons table (renamed to season)
CREATE OR REPLACE VIEW seasons AS 
SELECT 
    season_id AS id,
    content_id,
    title,
    trailer_url,
    created_at,
    updated_at
FROM season;

-- View for episodes table (renamed to episode)
CREATE OR REPLACE VIEW episodes AS 
SELECT 
    episode_id AS id,
    season_id,
    number,
    thumbnail,
    title,
    description,
    CASE 
        WHEN duration IS NULL THEN NULL
        ELSE CAST(duration AS CHAR)
    END AS duration,
    total_view,
    total_download,
    created_at,
    updated_at
FROM episode;

-- View for episode_sources table (renamed to episode_source)
CREATE OR REPLACE VIEW episode_sources AS 
SELECT 
    episode_source_id AS id,
    episode_id,
    title,
    quality,
    size,
    is_download,
    access_type,
    type,
    source,
    created_at,
    updated_at
FROM episode_source;

-- View for subtitles table (renamed to subtitle)
CREATE OR REPLACE VIEW subtitles AS 
SELECT 
    subtitle_id AS id,
    content_id,
    language_id,
    file,
    created_at,
    updated_at
FROM subtitle;

-- View for episode_subtitles table (renamed to episode_subtitle)
CREATE OR REPLACE VIEW episode_subtitles AS 
SELECT 
    episode_subtitle_id AS id,
    episode_id,
    language_id,
    file,
    created_at,
    updated_at
FROM episode_subtitle;

-- View for custom_ads table (renamed to custom_ad)
CREATE OR REPLACE VIEW custom_ads AS 
SELECT 
    custom_ad_id AS id,
    title,
    brand_name,
    brand_logo,
    button_text,
    is_android,
    android_link,
    is_ios,
    ios_link,
    start_date,
    end_date,
    status,
    views,
    clicks,
    created_at,
    updated_at
FROM custom_ad;

-- View for custom_ad_sources table (renamed to custom_ad_source)
CREATE OR REPLACE VIEW custom_ad_sources AS 
SELECT 
    custom_ad_source_id AS id,
    custom_ad_id,
    type,
    content,
    headline,
    description,
    show_time,
    is_skippable,
    created_at,
    updated_at
FROM custom_ad_source;

-- View for notifications table (renamed to notification)
CREATE OR REPLACE VIEW notifications AS 
SELECT 
    notification_id AS id,
    title,
    description,
    created_at,
    updated_at
FROM notification;

-- View for top_contents table (renamed to top_content)
CREATE OR REPLACE VIEW top_contents AS 
SELECT 
    top_content_id AS id,
    content_index,
    content_id,
    created_at,
    updated_at
FROM top_content;

-- View for tv_categories table (renamed to tv_category)
CREATE OR REPLACE VIEW tv_categories AS 
SELECT 
    tv_category_id AS id,
    title,
    image,
    created_at,
    updated_at
FROM tv_category;

-- View for tv_channels table (renamed to tv_channel)
CREATE OR REPLACE VIEW tv_channels AS 
SELECT 
    tv_channel_id AS id,
    title,
    thumbnail,
    access_type,
    category_ids,
    type,
    source,
    created_at,
    updated_at
FROM tv_channel;

-- View for tv_auth_sessions table (renamed to tv_auth_session)
CREATE OR REPLACE VIEW tv_auth_sessions AS 
SELECT 
    tv_auth_session_id AS id,
    session_token,
    qr_code,
    app_user_id AS user_id,  -- Also rename the foreign key reference
    tv_device_id,
    status,
    created_at,
    expires_at,
    authenticated_at
FROM tv_auth_session;

-- View for global_settings table (renamed to global_setting)
CREATE OR REPLACE VIEW global_settings AS 
SELECT 
    global_setting_id AS id,
    app_name,
    is_live_tv_enable,
    is_admob_android,
    is_admob_ios,
    is_custom_android,
    is_custom_ios,
    videoad_skip_time,
    storage_type,
    created_at,
    updated_at
FROM global_setting;

-- View for admob table (renamed to admob_config)
CREATE OR REPLACE VIEW admob AS 
SELECT 
    admob_config_id AS id,
    banner_id,
    interstitial_id AS intersial_id,  -- Keep the typo for backward compatibility
    rewarded_id,
    type,
    created_at,
    updated_at
FROM admob_config;

-- View for migrations table (renamed to schema_migration)
CREATE OR REPLACE VIEW migrations AS 
SELECT 
    schema_migration_id AS id,
    migration,
    batch
FROM schema_migration;

-- View for failed_jobs table (renamed to failed_job)
CREATE OR REPLACE VIEW failed_jobs AS 
SELECT 
    failed_job_id AS id,
    connection,
    queue,
    payload,
    exception,
    failed_at
FROM failed_job;

-- View for tbl_pages table (renamed to cms_page)
CREATE OR REPLACE VIEW tbl_pages AS 
SELECT 
    cms_page_id AS id,
    privacy,
    termsofuse,
    created_at,
    updated_at
FROM cms_page;

-- Note: The following tables were NOT renamed, so they don't need views:
-- admin_user (already singular)
-- content_cast (already singular)
-- media_gallery (already singular)