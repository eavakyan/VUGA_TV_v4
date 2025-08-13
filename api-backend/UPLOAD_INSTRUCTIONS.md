# Actor Details API Fix - Upload Instructions

## Issue
The fetchActorDetails API endpoint was returning HTML error instead of JSON due to trying to load a non-existent relationship `contentCast.actor` in the Content model.

## Fix Required
Replace the file on the production server:

**File to replace:**
`app/Http/Controllers/Api/V2/ActorController.php`

**With the fixed version:**
`ActorController_fix.php` (provided in this directory)

## Specific Change
Line 117 in the `fetchActorDetails` method needs to be changed:

**FROM:**
```php
->with(['language', 'contentCast.actor', 'contentGenres.genre', 'ageLimits'])
```

**TO:**
```php
->with(['language', 'genres', 'ageLimits'])
```

## Testing
After uploading, test the actor details by:
1. Open the iOS app
2. Navigate to any content detail page
3. Click on any actor card in the Cast section
4. The actor detail page should now load correctly showing:
   - Actor's photo
   - Name and date of birth
   - Bio text
   - List of content they appeared in

## Server Path
The file should be uploaded to:
`/path/to/your/production/app/Http/Controllers/Api/V2/ActorController.php`

## Note
Make sure to clear any Laravel cache after uploading:
```bash
php artisan cache:clear
php artisan config:clear
```