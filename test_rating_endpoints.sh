#!/bin/bash

# API Configuration
API_BASE_URL="https://iosdev.gossip-stone.com/api/v2"
API_KEY="jpwc3pny"

# Test user and profile IDs
USER_ID=1
PROFILE_ID=1
CONTENT_ID=1
EPISODE_ID=1

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}Testing Rating System API Endpoints${NC}"
echo "==================================================="

# Test 1: Rate a movie/content
echo -e "\n${GREEN}Test 1: Rating a Movie/Content${NC}"
echo "Endpoint: POST /user/rate-content"
echo "Rating: 8.5/10"
curl -X POST "$API_BASE_URL/user/rate-content" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"app_user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID,
    \"content_id\": $CONTENT_ID,
    \"rating\": 8.5
  }" \
  | python3 -m json.tool

# Test 2: Get content details to verify rating
echo -e "\n${GREEN}Test 2: Fetching Content Details (check user_rating)${NC}"
echo "Endpoint: POST /content/detail"
curl -X POST "$API_BASE_URL/content/detail" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"content_id\": $CONTENT_ID,
    \"app_user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }" \
  | python3 -m json.tool | grep -E "\"user_rating\"|\"ratings\"|\"title\"" | head -10

# Test 3: Rate a TV episode
echo -e "\n${GREEN}Test 3: Rating a TV Episode${NC}"
echo "Endpoint: POST /user/rate-episode"
echo "Rating: 9.0/10"
curl -X POST "$API_BASE_URL/user/rate-episode" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"app_user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID,
    \"episode_id\": $EPISODE_ID,
    \"rating\": 9.0
  }" \
  | python3 -m json.tool

# Test 4: Update rating for same content
echo -e "\n${GREEN}Test 4: Updating Rating for Same Content${NC}"
echo "Endpoint: POST /user/rate-content"
echo "New Rating: 7.0/10"
curl -X POST "$API_BASE_URL/user/rate-content" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"app_user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID,
    \"content_id\": $CONTENT_ID,
    \"rating\": 7.0
  }" \
  | python3 -m json.tool

# Test 5: Test with different profile
echo -e "\n${GREEN}Test 5: Rating with Different Profile${NC}"
echo "Endpoint: POST /user/rate-content"
echo "Profile ID: 2, Rating: 9.5/10"
curl -X POST "$API_BASE_URL/user/rate-content" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"app_user_id\": $USER_ID,
    \"profile_id\": 2,
    \"content_id\": $CONTENT_ID,
    \"rating\": 9.5
  }" \
  | python3 -m json.tool

# Test 6: Test validation - rating out of range
echo -e "\n${GREEN}Test 6: Testing Validation - Rating Out of Range${NC}"
echo "Endpoint: POST /user/rate-content"
echo "Invalid Rating: 11/10"
curl -X POST "$API_BASE_URL/user/rate-content" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"app_user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID,
    \"content_id\": $CONTENT_ID,
    \"rating\": 11
  }" \
  | python3 -m json.tool

# Test 7: Test validation - missing content
echo -e "\n${GREEN}Test 7: Testing Validation - Non-existent Content${NC}"
echo "Endpoint: POST /user/rate-content"
echo "Invalid Content ID: 99999"
curl -X POST "$API_BASE_URL/user/rate-content" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"app_user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID,
    \"content_id\": 99999,
    \"rating\": 8.0
  }" \
  | python3 -m json.tool

echo -e "\n${BLUE}Rating Endpoint Tests Complete!${NC}"
echo "==================================================="
echo -e "${RED}Note: Adjust USER_ID, PROFILE_ID, CONTENT_ID, and EPISODE_ID variables based on your actual data${NC}"