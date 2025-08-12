<?php
// Simple test for Recently Watched API endpoint

// Test data
$test_data = [
    'content_ids' => [1, 2, 3, 4, 5, 6], // Include content ID 6 which we see exists
    'user_id' => 1,
    'profile_id' => null
];

$api_url = 'https://iosdev.gossip-stone.com/api/v2/content/by-ids';
$api_key = 'jpwc3pny';

// Initialize cURL
$ch = curl_init($api_url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($test_data));
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/json',
    'apikey: ' . $api_key
]);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_VERBOSE, true);

// Execute request
$response = curl_exec($ch);
$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$error = curl_error($ch);
curl_close($ch);

// Display results
echo "API URL: $api_url\n";
echo "HTTP Code: $http_code\n";
if ($error) {
    echo "cURL Error: $error\n";
}
echo "\nResponse:\n";
echo $response;
echo "\n\n";

// Try to decode JSON
$decoded = json_decode($response, true);
if ($decoded) {
    echo "Decoded response:\n";
    print_r($decoded);
} else {
    echo "Failed to decode JSON response\n";
}
?>