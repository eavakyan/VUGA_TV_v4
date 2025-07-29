#!/bin/bash

# Test Profile API endpoints
API_BASE="https://iosdev.gossip-stone.com/api/v2"
API_KEY="jpwc3pny"

echo "Testing Profile API endpoints..."

# Test getUserProfiles
echo -e "\n1. Testing getUserProfiles endpoint:"
curl -X POST "$API_BASE/getUserProfiles" \
  -H "apikey: $API_KEY" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "user_id=1" \
  --silent | jq '.'

# Test V2 API test endpoint
echo -e "\n2. Testing V2 API test endpoint:"
curl -X GET "$API_BASE/test" \
  -H "apikey: $API_KEY" \
  --silent | jq '.'