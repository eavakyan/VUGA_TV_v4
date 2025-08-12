<?php
// Test script to verify API endpoint

// Include Laravel bootstrap
require __DIR__.'/vendor/autoload.php';
$app = require_once __DIR__.'/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Http\Kernel::class);

// Test if route exists
$router = app()->make('router');
$routes = $router->getRoutes();

echo "Checking for /api/v2/content/by-ids route...\n";

$found = false;
foreach ($routes as $route) {
    $uri = $route->uri();
    if (strpos($uri, 'content/by-ids') !== false) {
        echo "Found route: " . $route->methods()[0] . " " . $uri . "\n";
        echo "Action: " . $route->getActionName() . "\n";
        $found = true;
    }
}

if (!$found) {
    echo "ERROR: Route /api/v2/content/by-ids not found!\n";
    echo "\nAll content routes:\n";
    foreach ($routes as $route) {
        $uri = $route->uri();
        if (strpos($uri, 'api/v2/content') !== false) {
            echo $route->methods()[0] . " " . $uri . "\n";
        }
    }
}

// Test content IDs
echo "\n\nTesting with sample content IDs...\n";
$contentIds = [1, 2, 3];

try {
    $contents = \App\Models\V2\Content::whereIn('content_id', $contentIds)
        ->where('content_status', 1)
        ->select(['content_id', 'content_name'])
        ->get();
    
    echo "Found " . $contents->count() . " contents in database\n";
    foreach ($contents as $content) {
        echo "- ID: " . $content->content_id . ", Name: " . $content->content_name . "\n";
    }
} catch (\Exception $e) {
    echo "Database error: " . $e->getMessage() . "\n";
}