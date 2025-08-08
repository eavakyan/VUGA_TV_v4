#!/bin/bash

# ====================================
# Content Trailer Migration Script
# Purpose: Migrate trailer_url field to content_trailer table
# Date: 2025-01-08
# ====================================

echo "=== Content Trailer Migration Script ==="
echo "This script will migrate trailer data from content.trailer_url to a new content_trailer table"
echo ""

# Set MySQL connection parameters (update these for your environment)
DB_HOST="localhost"
DB_NAME="your_database_name"
DB_USER="your_username"
# DB_PASSWORD will be prompted for security

echo "Please enter your database details:"
read -p "Database Host [$DB_HOST]: " input_host
DB_HOST=${input_host:-$DB_HOST}

read -p "Database Name [$DB_NAME]: " input_db
DB_NAME=${input_db:-$DB_NAME}

read -p "Database User [$DB_USER]: " input_user
DB_USER=${input_user:-$DB_USER}

read -s -p "Database Password: " DB_PASSWORD
echo ""

# Function to execute SQL file
execute_sql_file() {
    local sql_file=$1
    local description=$2
    
    echo ""
    echo "=== $description ==="
    echo "Executing: $sql_file"
    
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$sql_file"
    
    if [ $? -eq 0 ]; then
        echo "✓ Success: $description completed"
    else
        echo "✗ Error: $description failed"
        echo "Please check the error messages above and fix any issues before continuing."
        exit 1
    fi
}

# Step 1: Create the content_trailer table
execute_sql_file "01_create_content_trailer_table.sql" "Creating content_trailer table"

# Step 2: Migrate existing trailer data
execute_sql_file "02_migrate_existing_trailer_data.sql" "Migrating existing trailer data"

# Step 3: Show migration summary
echo ""
echo "=== MIGRATION SUMMARY ==="
echo "Checking migration results..."

mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "
    SELECT 'Original content with trailers' as description, COUNT(*) as count 
    FROM content 
    WHERE trailer_url IS NOT NULL AND trailer_url != '' AND trailer_url != '0';
    
    SELECT 'Migrated trailers' as description, COUNT(*) as count 
    FROM content_trailer;
    
    SELECT 'Contents with multiple trailers' as description, COUNT(*) as count 
    FROM (
        SELECT content_id 
        FROM content_trailer 
        GROUP BY content_id 
        HAVING COUNT(*) > 1
    ) as multi_trailer_contents;
"

echo ""
echo "=== VERIFICATION ==="
echo "Please verify the migration was successful by checking the numbers above."
echo ""
read -p "Does the migration look correct? (y/n): " confirm

if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
    echo ""
    echo "=== OPTIONAL: Remove old trailer_url column ==="
    echo "WARNING: This step will permanently remove the trailer_url column from the content table!"
    echo "Make sure your application code has been updated to use the new content_trailer table."
    echo ""
    read -p "Do you want to remove the old trailer_url column? (y/n): " remove_old

    if [ "$remove_old" = "y" ] || [ "$remove_old" = "Y" ]; then
        execute_sql_file "03_remove_old_trailer_url_column.sql" "Removing old trailer_url column"
        echo ""
        echo "✓ Migration completed successfully!"
        echo "The old trailer_url column has been removed."
    else
        echo ""
        echo "✓ Migration completed successfully!"
        echo "The old trailer_url column has been preserved for backward compatibility."
    fi
else
    echo ""
    echo "Migration verification failed. Please check the data and run the migration again if needed."
    exit 1
fi

echo ""
echo "=== NEXT STEPS ==="
echo "1. Update your application code to use the new content_trailer relationship"
echo "2. Test the trailer functionality in your application"
echo "3. Update your API routes to include the new trailer endpoints"
echo "4. Consider updating your admin interface to manage multiple trailers per content"
echo ""
echo "New API endpoints available:"
echo "- GET /api/v2/content/trailers?content_id={id} - Get all trailers for content"
echo "- GET /api/v2/content/trailer/primary?content_id={id} - Get primary trailer"
echo "- POST /api/v2/content/trailer - Add new trailer"
echo "- PUT /api/v2/content/trailer - Update trailer"
echo "- DELETE /api/v2/content/trailer - Delete trailer"
echo ""
echo "Migration completed successfully!"