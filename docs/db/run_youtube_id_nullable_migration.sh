#!/bin/bash

# Script to make youtube_id column nullable in content_trailer table
# This allows the system to handle MP4, HLS, and other non-YouTube trailer URLs

echo "==================================="
echo "YouTube ID Nullable Migration"
echo "==================================="
echo ""

# Check if MySQL credentials are provided
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
    echo "Usage: $0 <hostname> <username> <database_name>"
    echo "You will be prompted for the password."
    echo ""
    echo "Example: $0 localhost root vuga_tv_db"
    exit 1
fi

HOSTNAME="$1"
USERNAME="$2"
DATABASE="$3"

echo "Database: $DATABASE"
echo "Host: $HOSTNAME"
echo "User: $USERNAME"
echo ""

# Function to execute SQL file
execute_sql_file() {
    local file="$1"
    local description="$2"
    
    echo "=== $description ==="
    echo "Executing: $file"
    
    if [ ! -f "$file" ]; then
        echo "ERROR: File $file not found!"
        return 1
    fi
    
    mysql -h "$HOSTNAME" -u "$USERNAME" -p "$DATABASE" < "$file"
    
    if [ $? -eq 0 ]; then
        echo "✅ SUCCESS: $description completed"
        echo ""
        return 0
    else
        echo "❌ ERROR: $description failed"
        echo ""
        return 1
    fi
}

# Main execution
echo "This script will make the youtube_id column nullable in the content_trailer table."
echo "This allows the system to handle non-YouTube video URLs (MP4, HLS, etc.)"
echo ""
read -p "Do you want to continue? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    execute_sql_file "04_make_youtube_id_nullable.sql" "Making youtube_id column nullable"
    
    if [ $? -eq 0 ]; then
        echo "==================================="
        echo "✅ Migration completed successfully!"
        echo "==================================="
        echo ""
        echo "The youtube_id column is now nullable, allowing the system to support:"
        echo "• YouTube videos (youtube_id will be extracted)"
        echo "• MP4 files (youtube_id will be null)"
        echo "• HLS/M3U8 streams (youtube_id will be null)"
        echo "• Other video formats (youtube_id will be null)"
        echo ""
    else
        echo "==================================="
        echo "❌ Migration failed!"
        echo "==================================="
        exit 1
    fi
else
    echo "Migration cancelled by user."
    exit 0
fi