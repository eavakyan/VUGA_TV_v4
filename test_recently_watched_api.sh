#!/bin/bash

# Test the Recently Watched API endpoint

# Set your API base URL (update this to match your server)
API_BASE="https://iosdev.gossip-stone.com/api/v2/"
API_KEY="jpwc3pny"

# Test content IDs (replace with actual content IDs from your database)
CONTENT_IDS='{"content_ids": [1, 2, 3]}'

echo "Testing Recently Watched API endpoint..."
echo "URL: ${API_BASE}content/by-ids"
echo "Request body: $CONTENT_IDS"
echo ""

# Make the API request
curl -X POST "${API_BASE}content/by-ids" \
  -H "Content-Type: application/json" \
  -H "apikey: $API_KEY" \
  -d "$CONTENT_IDS" \
  -v

echo ""
echo ""
echo "If you see a 404 error, the route might not be registered."
echo "If you see a 500 error, check Laravel logs at: storage/logs/laravel.log"