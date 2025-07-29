<?php
// IMPORTANT: Delete this file after running it for security!

echo "<h2>Profile Feature File Check</h2>";
echo "<pre>";

// Check critical files
$files = [
    'routes/api_v2.php' => '../routes/api_v2.php',
    'app/Http/Controllers/Api/V2/ProfileController.php' => '../app/Http/Controllers/Api/V2/ProfileController.php',
    'app/Providers/RouteServiceProvider.php' => '../app/Providers/RouteServiceProvider.php',
    'app/Models/V2/AppUserProfile.php' => '../app/Models/V2/AppUserProfile.php'
];

foreach ($files as $name => $path) {
    if (file_exists($path)) {
        $size = filesize($path);
        $modified = date('Y-m-d H:i:s', filemtime($path));
        echo "✓ $name exists (Size: $size bytes, Modified: $modified)\n";
        
        // Show first few lines of api_v2.php
        if ($name === 'routes/api_v2.php') {
            echo "  First 10 lines:\n";
            $lines = file($path);
            for ($i = 0; $i < min(10, count($lines)); $i++) {
                echo "    " . ($i + 1) . ": " . htmlspecialchars(trim($lines[$i])) . "\n";
            }
            
            // Check if it contains profile routes
            $content = file_get_contents($path);
            if (strpos($content, 'ProfileController') !== false) {
                echo "  ✓ Contains ProfileController routes\n";
            } else {
                echo "  ✗ Does NOT contain ProfileController routes\n";
            }
        }
    } else {
        echo "✗ $name is MISSING!\n";
    }
}

echo "\n<strong style='color: red;'>IMPORTANT: Delete this file (check-files.php) from the server immediately!</strong>";
echo "</pre>";
?>