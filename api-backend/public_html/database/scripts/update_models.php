<?php
// Script to update all model files with new table names and primary keys

$updates = [
    'Actor.php' => ['table' => 'actor', 'pk' => 'actor_id'],
    'Admin.php' => ['table' => 'admin_user', 'pk' => 'admin_user_id'],
    'Admob.php' => ['table' => 'admob_config', 'pk' => 'admob_config_id'],
    'Content.php' => ['table' => 'content', 'pk' => 'content_id'],
    'ContentCast.php' => ['table' => 'content_cast', 'pk' => 'content_cast_id'],
    'ContentSource.php' => ['table' => 'content_source', 'pk' => 'content_source_id'],
    'ContentSubtitles.php' => ['table' => 'subtitle', 'pk' => 'subtitle_id'],
    'CustomAd.php' => ['table' => 'custom_ad', 'pk' => 'custom_ad_id'],
    'CustomAdSource.php' => ['table' => 'custom_ad_source', 'pk' => 'custom_ad_source_id'],
    'Episode.php' => ['table' => 'episode', 'pk' => 'episode_id'],
    'EpisodeSource.php' => ['table' => 'episode_source', 'pk' => 'episode_source_id'],
    'EpisodeSubtitle.php' => ['table' => 'episode_subtitle', 'pk' => 'episode_subtitle_id'],
    'Genre.php' => ['table' => 'genre', 'pk' => 'genre_id'],
    'GlobalSettings.php' => ['table' => 'global_setting', 'pk' => 'global_setting_id'],
    'Language.php' => ['table' => 'language', 'pk' => 'language_id'],
    'MediaGallery.php' => ['table' => 'media_gallery', 'pk' => 'media_gallery_id'],
    'Notification.php' => ['table' => 'notification', 'pk' => 'notification_id'],
    'Pages.php' => ['table' => 'page', 'pk' => 'page_id'],
    'Season.php' => ['table' => 'season', 'pk' => 'season_id'],
    'Subtitle.php' => ['table' => 'subtitle', 'pk' => 'subtitle_id'],
    'TopContent.php' => ['table' => 'top_content', 'pk' => 'top_content_id'],
    'TVCategory.php' => ['table' => 'tv_category', 'pk' => 'tv_category_id'],
    'TVChannel.php' => ['table' => 'tv_channel', 'pk' => 'tv_channel_id'],
    'User.php' => ['table' => 'user', 'pk' => 'user_id'],
];

foreach ($updates as $file => $config) {
    $path = __DIR__ . '/app/' . $file;
    if (file_exists($path)) {
        $content = file_get_contents($path);
        
        // Update table name
        $content = preg_replace(
            "/protected\s+\\\$table\s*=\s*['\"][^'\"]+['\"]\s*;/",
            "protected \$table = '{$config['table']}';",
            $content
        );
        
        // Add or update primary key
        if (strpos($content, 'protected $primaryKey') !== false) {
            $content = preg_replace(
                "/protected\s+\\\$primaryKey\s*=\s*['\"][^'\"]+['\"]\s*;/",
                "protected \$primaryKey = '{$config['pk']}';",
                $content
            );
        } else {
            // Add primary key after table definition
            $content = preg_replace(
                "/(protected\s+\\\$table\s*=\s*['\"][^'\"]+['\"]\s*;)/",
                "$1\n    protected \$primaryKey = '{$config['pk']}';",
                $content
            );
        }
        
        file_put_contents($path, $content);
        echo "Updated: $file\n";
    }
}

echo "\nDone updating model files!\n";