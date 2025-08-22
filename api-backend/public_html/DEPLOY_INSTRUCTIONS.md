# TV Authentication API Fix - Deployment Instructions

## Quick Deploy (Run these commands on production server)

SSH to the production server and run:

```bash
cd /home/ubuntu/api
git pull origin main
composer install --no-dev --optimize-autoloader
php artisan cache:clear
php artisan route:clear
php artisan config:clear
php artisan route:cache
php artisan config:cache
sudo chmod -R 755 storage bootstrap/cache
sudo chown -R www-data:www-data storage bootstrap/cache
```

## What was fixed:

1. Added TV authentication routes to `/api-backend/public_html/routes/api_v2.php`:
   - `/api/v2/TV/authenticateSession` 
   - `/api/v2/TV/checkAuthStatus`
   - `/api/v2/TV/generateAuthSession`
   - `/api/v2/TV/completeAuth`

2. Created `/api-backend/public_html/app/Http/Controllers/Api/V2/TvAuthController.php` with:
   - Legacy endpoint support for mobile app compatibility
   - Proper parameter mapping (user_id → app_user_id)

## Testing the fix:

After deployment, test with:

```bash
curl -X POST "https://iosdev.gossip-stone.com/api/v2/TV/authenticateSession" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "apikey: jpwc3pny" \
  -d "session_token=test&user_id=1"
```

You should get a JSON response (not HTML 404).

## Alternative: Using the deployment script

From your local machine:
```bash
cd /Users/gene/Documents/dev/VUGA_TV_v4/api-backend/public_html
chmod +x deploy_tv_auth_fix.sh
./deploy_tv_auth_fix.sh
```

This will push changes and SSH to deploy automatically.