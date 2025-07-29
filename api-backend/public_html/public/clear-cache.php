<?php
// IMPORTANT: Delete this file after running it for security!

// Go up one directory to reach the Laravel root
chdir('..');

// Load Laravel
require __DIR__.'/../vendor/autoload.php';
$app = require_once __DIR__.'/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

echo "<h2>Laravel Cache Clear Script</h2>";
echo "<pre>";

try {
    // Clear route cache
    echo "Clearing route cache...\n";
    Artisan::call('route:clear');
    echo Artisan::output();
    
    // Clear config cache
    echo "\nClearing config cache...\n";
    Artisan::call('config:clear');
    echo Artisan::output();
    
    // Clear application cache
    echo "\nClearing application cache...\n";
    Artisan::call('cache:clear');
    echo Artisan::output();
    
    // Clear compiled views
    echo "\nClearing compiled views...\n";
    Artisan::call('view:clear');
    echo Artisan::output();
    
    echo "\n<strong>All caches cleared successfully!</strong>\n";
    echo "\n<strong style='color: red;'>IMPORTANT: Delete this file (clear-cache.php) from the server immediately!</strong>";
    
} catch (Exception $e) {
    echo "Error: " . $e->getMessage();
}

echo "</pre>";

// Test if profile route is now available
echo "<hr>";
echo "<h3>Testing Profile Route:</h3>";
echo "<pre>";
$routes = app()->router->getRoutes();
$found = false;
foreach ($routes as $route) {
    if (strpos($route->uri(), 'getUserProfiles') !== false) {
        echo "Found route: " . $route->methods()[0] . " " . $route->uri() . "\n";
        $found = true;
    }
}
if (!$found) {
    echo "Profile routes not found. Make sure api_v2.php is properly included.\n";
}
echo "</pre>";
?>