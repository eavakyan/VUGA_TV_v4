#!/bin/bash

# API Configuration
API_BASE_URL="https://iosdev.gossip-stone.com/api/v2"
API_KEY="jpwc3pny"

# Test user and profile IDs - you may need to adjust these based on your actual data
USER_ID=1
PROFILE_ID=1

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}Testing Age-Based Content Restriction API Endpoints${NC}"
echo "=================================================="

# Test 1: Get Age Ratings
echo -e "\n${GREEN}Test 1: Fetching Age Ratings${NC}"
echo "Endpoint: POST /profile/age-ratings"
curl -X POST "$API_BASE_URL/profile/age-ratings" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{\"apikey\": \"$API_KEY\"}" \
  | python3 -m json.tool

# Test 2: Get User Profiles (to see current age settings)
echo -e "\n${GREEN}Test 2: Fetching User Profiles${NC}"
echo "Endpoint: POST /getUserProfiles"
curl -X POST "$API_BASE_URL/getUserProfiles" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"user_id\": $USER_ID
  }" \
  | python3 -m json.tool

# Test 3: Update Age Settings - Set as adult (25 years old)
echo -e "\n${GREEN}Test 3: Update Profile Age Settings (Adult - 25 years)${NC}"
echo "Endpoint: POST /profile/update-age-settings"
curl -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"age\": 25,
    \"is_kids_profile\": false
  }" \
  | python3 -m json.tool

# Test 4: Update Age Settings - Set as kids profile
echo -e "\n${GREEN}Test 4: Update Profile as Kids Profile${NC}"
echo "Endpoint: POST /profile/update-age-settings"
curl -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"is_kids_profile\": true
  }" \
  | python3 -m json.tool

# Test 5: Fetch Home Page Data with Profile Filter
echo -e "\n${GREEN}Test 5: Fetch Home Page Data (with age filtering)${NC}"
echo "Endpoint: POST /fetchHomePageData"
curl -X POST "$API_BASE_URL/fetchHomePageData" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }" \
  | python3 -m json.tool | head -50

# Test 6: Test Content Detail Access with Age Restriction
echo -e "\n${GREEN}Test 6: Test Content Details (check age restriction)${NC}"
echo "Endpoint: POST /fetchContentDetails"
# Using content_id 1 as example - adjust based on your content
curl -X POST "$API_BASE_URL/fetchContentDetails" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"content_id\": 1,
    \"user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }" \
  | python3 -m json.tool

# Test 7: Search Content with Age Filtering
echo -e "\n${GREEN}Test 7: Search Content (with age filtering)${NC}"
echo "Endpoint: POST /searchContent"
curl -X POST "$API_BASE_URL/searchContent" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"search\": \"movie\",
    \"user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }" \
  | python3 -m json.tool | head -50

# Test 8: Update Age Settings - Set as teenager (15 years old)
echo -e "\n${GREEN}Test 8: Update Profile Age Settings (Teenager - 15 years)${NC}"
echo "Endpoint: POST /profile/update-age-settings"
curl -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"age\": 15,
    \"is_kids_profile\": false
  }" \
  | python3 -m json.tool

# Test 9: Create a new kids profile
echo -e "\n${GREEN}Test 9: Create a New Kids Profile${NC}"
echo "Endpoint: POST /createProfile"
curl -X POST "$API_BASE_URL/createProfile" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"user_id\": $USER_ID,
    \"name\": \"Kids Test\",
    \"avatar_id\": 3,
    \"is_kids\": 1,
    \"is_kids_profile\": 1,
    \"age\": 8
  }" \
  | python3 -m json.tool

echo -e "\n${BLUE}Age Endpoint Tests Complete!${NC}"
echo "=================================================="
echo -e "${RED}Note: Adjust USER_ID and PROFILE_ID variables based on your actual data${NC}"