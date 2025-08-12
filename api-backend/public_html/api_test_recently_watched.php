<?php
// Temporary API endpoint for testing Recently Watched

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type, apikey');

// Check for API key
$headers = getallheaders();
if (!isset($headers['apikey']) || $headers['apikey'] !== 'jpwc3pny') {
    http_response_code(401);
    echo json_encode(['status' => false, 'message' => 'Unauthorized']);
    exit;
}

// Get POST data
$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['content_ids']) || !is_array($input['content_ids'])) {
    echo json_encode(['status' => false, 'message' => 'content_ids array is required']);
    exit;
}

$contentIds = array_map('intval', $input['content_ids']);

// Database connection
$host = 'localhost';
$db = 'your_database_name'; // Update this
$user = 'your_database_user'; // Update this
$pass = 'your_database_password'; // Update this

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Query contents
    $placeholders = implode(',', array_fill(0, count($contentIds), '?'));
    $sql = "
        SELECT 
            content_id,
            title as content_name,
            horizontal_poster,
            vertical_poster,
            type as content_type,
            release_year,
            ratings
        FROM content 
        WHERE content_id IN ($placeholders) 
        AND content_status = 1
    ";
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute($contentIds);
    $contents = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Get genres for each content
    foreach ($contents as &$content) {
        $genreSql = "
            SELECT g.genre_id, g.title
            FROM genre g
            JOIN content_genre cg ON g.genre_id = cg.genre_id
            WHERE cg.content_id = ?
        ";
        $genreStmt = $pdo->prepare($genreSql);
        $genreStmt->execute([$content['content_id']]);
        $content['genres'] = $genreStmt->fetchAll(PDO::FETCH_ASSOC);
        
        // Add empty seasons array for now
        $content['seasons'] = [];
    }
    
    echo json_encode([
        'status' => true,
        'message' => 'Contents fetched successfully',
        'data' => $contents
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'status' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
}
?>