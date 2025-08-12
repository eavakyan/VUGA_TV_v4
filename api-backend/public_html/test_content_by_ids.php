<?php
// Direct test for content by IDs

require __DIR__.'/vendor/autoload.php';
$app = require_once __DIR__.'/bootstrap/app.php';

use Illuminate\Support\Facades\DB;

// Test data
$contentIds = [11, 6, 7, 10, 9, 3];

echo "Testing direct database query for content IDs: " . implode(', ', $contentIds) . "\n\n";

try {
    // Direct query to check column names
    $columns = DB::select("SHOW COLUMNS FROM content");
    echo "Content table columns:\n";
    foreach ($columns as $column) {
        echo "- " . $column->Field . " (" . $column->Type . ")\n";
    }
    echo "\n";
    
    // Direct query for contents
    $contents = DB::select("
        SELECT 
            content_id,
            title,
            horizontal_poster,
            vertical_poster,
            type,
            release_year,
            ratings
        FROM content 
        WHERE content_id IN (" . implode(',', $contentIds) . ") 
        AND content_status = 1
    ");
    
    echo "Found " . count($contents) . " contents:\n";
    foreach ($contents as $content) {
        echo "- ID: " . $content->content_id . ", Title: " . $content->title . "\n";
    }
    
} catch (\Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
?>