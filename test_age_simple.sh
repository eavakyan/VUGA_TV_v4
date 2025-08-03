#!/bin/bash

# API Configuration
API_BASE_URL="https://iosdev.gossip-stone.com/api/v2"
API_KEY="jpwc3pny"

# Test user and profile IDs
USER_ID=1
PROFILE_ID=1

echo "Testing Age Restriction Endpoints"
echo "================================="

# Test 1: Get Age Ratings
echo -e "\n1. Getting Age Ratings:"
curl -s -X POST "$API_BASE_URL/profile/age-ratings" \
  -H "Content-Type: application/json" \
  -d "{\"apikey\": \"$API_KEY\"}" \
  | python3 -m json.tool | grep -E '"name"|"code"|"min_age"|"max_age"' | head -20

# Test 2: Update as Kids Profile
echo -e "\n2. Setting profile as Kids Profile:"
curl -s -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"is_kids_profile\": true
  }" \
  | python3 -m json.tool | grep -E '"status"|"message"|"age"|"is_kids_profile"'

# Test 3: Update with specific age (15 years)
echo -e "\n3. Setting profile age to 15:"
curl -s -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"age\": 15,
    \"is_kids_profile\": false
  }" \
  | python3 -m json.tool | grep -E '"status"|"message"|"age"|"is_kids_profile"'

# Test 4: Get updated profile
echo -e "\n4. Checking updated profile:"
curl -s -X POST "$API_BASE_URL/getUserProfiles" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"user_id\": $USER_ID
  }" \
  | python3 -m json.tool | grep -A 5 "profile_id.*$PROFILE_ID" | grep -E '"name"|"age"|"is_kids_profile"'

echo -e "\n================================="
echo "Tests Complete!"