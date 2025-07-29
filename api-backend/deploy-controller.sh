#!/bin/bash

# Deploy ContentController.php to production server

echo "Deploying ContentController.php to production server..."

# Upload the file
scp public_html/app/Http/Controllers/Api/V2/ContentController.php root@157.245.184.16:/root/app_backend_v4/app/Http/Controllers/Api/V2/

echo "Deployment complete!"