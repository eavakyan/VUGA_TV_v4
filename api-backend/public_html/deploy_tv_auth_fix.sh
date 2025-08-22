#!/bin/bash

# Deployment script for TV authentication API fix
# Run this from your local machine to deploy to production

echo "========================================="
echo "Deploying TV Authentication API Fix"
echo "========================================="

# First, commit and push if there are any uncommitted changes
echo "1. Checking git status..."
git status

echo ""
echo "2. Pushing to remote repository..."
git push origin main

echo ""
echo "3. SSH to production server and deploy..."
echo "========================================"
ssh iosdev.gossip-stone.com << 'ENDSSH'
cd /home/ubuntu/api
echo "On production server: $(pwd)"
echo ""
echo "Pulling latest changes..."
git pull origin main
echo ""
echo "Installing/updating composer dependencies..."
composer install --no-dev --optimize-autoloader
echo ""
echo "Clearing Laravel caches..."
php artisan cache:clear
php artisan route:clear
php artisan config:clear
php artisan view:clear
echo ""
echo "Caching routes and config for production..."
php artisan route:cache
php artisan config:cache
echo ""
echo "Setting proper permissions..."
sudo chmod -R 755 storage bootstrap/cache
sudo chown -R www-data:www-data storage bootstrap/cache
ENDSSH

echo "========================================="
echo "Deployment complete!"
echo "========================================="

echo "Testing the endpoint..."
curl -X POST "https://iosdev.gossip-stone.com/api/v2/TV/authenticateSession" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "apikey: jpwc3pny" \
  -d "session_token=test&user_id=1" 2>/dev/null | python3 -m json.tool || echo "Test complete"

echo ""
echo "If you see a JSON response above, the deployment was successful!"