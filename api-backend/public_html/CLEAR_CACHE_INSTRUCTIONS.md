# Clear Laravel Cache Instructions

The 404 error indicates that Laravel isn't recognizing the new routes. You need to clear the Laravel cache on your server:

```bash
cd /path/to/api-backend/public_html

# Clear all caches
php artisan cache:clear
php artisan route:clear
php artisan config:clear
php artisan view:clear

# Regenerate route cache (optional, but recommended for production)
php artisan route:cache

# If using OPcache, you may also need to restart PHP-FPM or Apache
# For Apache:
sudo service apache2 restart

# For PHP-FPM:
sudo service php7.4-fpm restart  # Replace 7.4 with your PHP version
```

After clearing the cache, the tv-auth routes should be available.