<?php
if (isset($_GET['url'])) {
    $url = $_GET['url']; // ✅ Already encoded from JS

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url); // ✅ use it directly
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $response = curl_exec($ch);
    curl_close($ch);

    header('Content-Type: application/json');
    echo $response;
} else {
    echo json_encode(['error' => 'Invalid parameters.']);
}
?>
