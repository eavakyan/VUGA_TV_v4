<?php
// Test script for updateProfile API with consent fields

$apiUrl = 'https://iosdev.gossip-stone.com/api/v2/updateProfile';

// Test data
$postData = [
    'app_user_id' => 1, // Replace with a valid user ID
    'email_consent' => 1,
    'sms_consent' => 0
];

// Initialize cURL
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $apiUrl);
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($postData));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/x-www-form-urlencoded',
    'apikey: jpwc3pny' // API key from the code
]);

// Execute request
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

// Display results
echo "HTTP Status Code: $httpCode\n";
echo "Response:\n";
$jsonResponse = json_decode($response, true);
echo json_encode($jsonResponse, JSON_PRETTY_PRINT) . "\n";

// Check if consent fields are in the response
if (isset($jsonResponse['data'])) {
    echo "\n--- Consent Fields Check ---\n";
    echo "email_consent: " . (isset($jsonResponse['data']['email_consent']) ? $jsonResponse['data']['email_consent'] : 'NOT FOUND') . "\n";
    echo "sms_consent: " . (isset($jsonResponse['data']['sms_consent']) ? $jsonResponse['data']['sms_consent'] : 'NOT FOUND') . "\n";
    echo "email_consent_date: " . (isset($jsonResponse['data']['email_consent_date']) ? $jsonResponse['data']['email_consent_date'] : 'NOT FOUND') . "\n";
    echo "sms_consent_date: " . (isset($jsonResponse['data']['sms_consent_date']) ? $jsonResponse['data']['sms_consent_date'] : 'NOT FOUND') . "\n";
}