#!/bin/bash

# API Configuration
API_BASE_URL="https://iosdev.gossip-stone.com/api/v2"
API_KEY="jpwc3pny"

# Test user and profile IDs
USER_ID=1
PROFILE_ID=1

echo "Testing Content Filtering by Age"
echo "================================="

# Function to count content
count_content() {
    local response="$1"
    echo "$response" | grep -o '"content_id"' | wc -l
}

# Test 1: Adult Profile (25 years) - Should see all content
echo -e "\n1. Testing Adult Profile (25 years):"
curl -s -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"age\": 25,
    \"is_kids_profile\": false
  }" > /dev/null

ADULT_CONTENT=$(curl -s -X POST "$API_BASE_URL/fetchHomePageData" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }")

echo "Featured content count: $(echo "$ADULT_CONTENT" | grep -o '"featured"' | wc -l)"
echo "Content titles found: $(echo "$ADULT_CONTENT" | grep -o '"title"' | wc -l)"

# Test 2: Kids Profile - Should see limited content
echo -e "\n2. Testing Kids Profile:"
curl -s -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"is_kids_profile\": true
  }" > /dev/null

KIDS_CONTENT=$(curl -s -X POST "$API_BASE_URL/fetchHomePageData" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }")

echo "Featured content count: $(echo "$KIDS_CONTENT" | grep -o '"featured"' | wc -l)"
echo "Content titles found: $(echo "$KIDS_CONTENT" | grep -o '"title"' | wc -l)"

# Test 3: Teen Profile (15 years) - Should see teen-appropriate content
echo -e "\n3. Testing Teen Profile (15 years):"
curl -s -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"age\": 15,
    \"is_kids_profile\": false
  }" > /dev/null

TEEN_CONTENT=$(curl -s -X POST "$API_BASE_URL/fetchHomePageData" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }")

echo "Featured content count: $(echo "$TEEN_CONTENT" | grep -o '"featured"' | wc -l)"
echo "Content titles found: $(echo "$TEEN_CONTENT" | grep -o '"title"' | wc -l)"

# Test 4: Check specific content access
echo -e "\n4. Testing Content Access (Content ID: 1):"
echo "Adult (25 years) access:"
curl -s -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"age\": 25,
    \"is_kids_profile\": false
  }" > /dev/null

ADULT_ACCESS=$(curl -s -X POST "$API_BASE_URL/fetchContentDetails" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"content_id\": 1,
    \"user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }")

if echo "$ADULT_ACCESS" | grep -q '"status":true'; then
    echo "✓ Can access content"
else
    echo "✗ Cannot access content: $(echo "$ADULT_ACCESS" | grep -o '"message":"[^"]*"' | head -1)"
fi

echo -e "\nKids profile access:"
curl -s -X POST "$API_BASE_URL/profile/update-age-settings" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"profile_id\": $PROFILE_ID,
    \"user_id\": $USER_ID,
    \"is_kids_profile\": true
  }" > /dev/null

KIDS_ACCESS=$(curl -s -X POST "$API_BASE_URL/fetchContentDetails" \
  -H "Content-Type: application/json" \
  -d "{
    \"apikey\": \"$API_KEY\",
    \"content_id\": 1,
    \"user_id\": $USER_ID,
    \"profile_id\": $PROFILE_ID
  }")

if echo "$KIDS_ACCESS" | grep -q '"status":true'; then
    echo "✓ Can access content"
    echo "Content title: $(echo "$KIDS_ACCESS" | grep -o '"title":"[^"]*"' | head -1)"
else
    echo "✗ Cannot access content: $(echo "$KIDS_ACCESS" | grep -o '"message":"[^"]*"' | head -1)"
fi

echo -e "\n================================="
echo "Content Filtering Tests Complete!"