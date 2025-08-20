<?php
// Clear all caches including opcache
require __DIR__.'/../vendor/autoload.php';
$app = require_once __DIR__.'/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);

// Clear Laravel caches
$kernel->call('cache:clear');
$kernel->call('config:clear');
$kernel->call('route:clear');
$kernel->call('view:clear');
$kernel->call('optimize:clear');

// Clear opcache if available
if (function_exists('opcache_reset')) {
    opcache_reset();
    echo "OPcache cleared!<br>";
}

// Test the Category model
$category = \App\Category::first();
if ($category) {
    echo "Category test:<br>";
    echo "category_id: " . $category->category_id . "<br>";
    echo "id (accessor): " . $category->id . "<br>";
    echo "JSON: " . json_encode($category->toArray()) . "<br><br>";
}

echo "All caches cleared successfully!<br>";
echo "PHP Version: " . phpversion() . "<br>";
echo "Laravel Version: " . app()->version() . "<br>";