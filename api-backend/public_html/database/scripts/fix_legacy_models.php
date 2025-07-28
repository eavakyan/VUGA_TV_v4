<?php
// Script to update all legacy models with correct primary key names

$models_to_fix = [
    // Tables that weren't renamed but have new primary key names
    'app/MediaGallery.php' => 'media_gallery_id',
    'app/ContentCast.php' => 'content_cast_id', 
    'app/Admin.php' => 'admin_user_id',
];

// Additional models that might need checking
$models_to_check = [
    'app/Admob.php' => ['table' => 'admob_config', 'primaryKey' => 'admob_config_id'],
    'app/GlobalSettings.php' => ['table' => 'global_setting', 'primaryKey' => 'global_setting_id'],
    'app/Notification.php' => ['table' => 'notification', 'primaryKey' => 'notification_id'],
    'app/TopContent.php' => ['table' => 'top_content', 'primaryKey' => 'top_content_id'],
    'app/Pages.php' => ['table' => 'cms_page', 'primaryKey' => 'cms_page_id'],
];

echo "Legacy models that have been fixed:\n";
echo "==================================\n";
foreach ($models_to_fix as $file => $primaryKey) {
    echo "- $file: primaryKey = '$primaryKey'\n";
}

echo "\nOther models that may need checking:\n";
echo "===================================\n";
foreach ($models_to_check as $file => $info) {
    echo "- $file: table = '{$info['table']}', primaryKey = '{$info['primaryKey']}'\n";
}

echo "\nIMPORTANT: These models need to be manually checked and updated if necessary.\n";