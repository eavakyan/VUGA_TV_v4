<?php
/**
 * Test script for the user notification system
 * Run this script to create a test notification and verify the API endpoints
 */

$baseUrl = 'http://localhost/api/v2';  // Update this to your API URL

// Test data
$testNotification = [
    'title' => 'Welcome to VUGA TV!',
    'message' => 'Thank you for joining VUGA TV. Enjoy unlimited streaming of your favorite movies and shows.',
    'notification_type' => 'system',
    'target_platforms' => ['all'],
    'priority' => 'high'
];

$testProfileId = 1; // Update with a valid profile ID from your database
$testPlatform = 'ios';

echo "VUGA TV User Notification System Test\n";
echo "=====================================\n\n";

// Function to make API calls
function makeApiCall($url, $method = 'POST', $data = null) {
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    
    if ($data) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    }
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    return [
        'code' => $httpCode,
        'response' => json_decode($response, true)
    ];
}

// Test 1: Create a notification (Admin)
echo "1. Creating a test notification...\n";
$result = makeApiCall("$baseUrl/user-notification/admin/create", 'POST', $testNotification);
echo "   Status Code: {$result['code']}\n";
echo "   Response: " . json_encode($result['response'], JSON_PRETTY_PRINT) . "\n\n";

if ($result['code'] == 200 && $result['response']['status']) {
    $notificationId = $result['response']['data']['notification_id'];
    echo "   ✓ Notification created with ID: $notificationId\n\n";
} else {
    echo "   ✗ Failed to create notification\n\n";
    exit(1);
}

// Test 2: Get pending notifications for a profile
echo "2. Getting pending notifications for profile $testProfileId...\n";
$result = makeApiCall("$baseUrl/user-notification/pending", 'POST', [
    'profile_id' => $testProfileId,
    'platform' => $testPlatform
]);
echo "   Status Code: {$result['code']}\n";
echo "   Response: " . json_encode($result['response'], JSON_PRETTY_PRINT) . "\n\n";

// Test 3: Mark notification as shown
echo "3. Marking notification as shown...\n";
$result = makeApiCall("$baseUrl/user-notification/mark-shown", 'POST', [
    'profile_id' => $testProfileId,
    'notification_id' => $notificationId,
    'platform' => $testPlatform
]);
echo "   Status Code: {$result['code']}\n";
echo "   Response: " . json_encode($result['response'], JSON_PRETTY_PRINT) . "\n\n";

// Test 4: Get analytics for the notification
echo "4. Getting notification analytics...\n";
$result = makeApiCall("$baseUrl/user-notification/admin/analytics/$notificationId", 'GET');
echo "   Status Code: {$result['code']}\n";
echo "   Response: " . json_encode($result['response'], JSON_PRETTY_PRINT) . "\n\n";

// Test 5: List all notifications (Admin)
echo "5. Listing all notifications...\n";
$result = makeApiCall("$baseUrl/user-notification/admin/list", 'POST', [
    'per_page' => 10
]);
echo "   Status Code: {$result['code']}\n";
echo "   Response: " . json_encode($result['response'], JSON_PRETTY_PRINT) . "\n\n";

echo "\nTest completed!\n";
echo "===============\n";
echo "You can now integrate the notification system into your iOS, Android, and Android TV apps.\n";
echo "\nAPI Endpoints:\n";
echo "- POST /api/v2/user-notification/pending - Get pending notifications for a profile\n";
echo "- POST /api/v2/user-notification/mark-shown - Mark notification as shown\n";
echo "- POST /api/v2/user-notification/dismiss - Dismiss a notification\n";
echo "\nAdmin Endpoints:\n";
echo "- POST /api/v2/user-notification/admin/create - Create a new notification\n";
echo "- POST /api/v2/user-notification/admin/list - List all notifications\n";
echo "- POST /api/v2/user-notification/admin/update/{id} - Update a notification\n";
echo "- DELETE /api/v2/user-notification/admin/delete/{id} - Delete a notification\n";
echo "- GET /api/v2/user-notification/admin/analytics/{id} - Get notification analytics\n";