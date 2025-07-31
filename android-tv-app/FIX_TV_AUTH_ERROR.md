# Fix for Android TV APIBKEND07 Error

The error is occurring because the `tv_auth_sessions` table doesn't exist in your database. 

## Steps to fix:

1. **Run the database migration** on your server:
   ```bash
   cd /path/to/api-backend/public_html
   php artisan migrate
   ```

2. **Clear the app cache** on your Android TV (if the error persists):
   - Go to Settings > Apps > Your App > Clear Cache
   - Or uninstall and reinstall the app

## What was fixed:

1. **API Endpoint corrections**:
   - Using v1 TV authentication endpoints at `../TV/` that have proper middleware
   - Fixed test endpoint from `GET(".")` to `GET("test")`

2. **Response field mismatches**:
   - Added default value for `expires_in_seconds` field
   - Fixed `auth_status` to `session_status` in response model

3. **Backend updates**:
   - Added `expires_in_seconds` field to generateSession response
   - Created database migration for `tv_auth_sessions` table

The app should now be able to:
- Generate QR codes for TV authentication
- Poll for authentication status
- Complete the authentication flow

After running the migration, the APIBKEND07 error should be resolved.