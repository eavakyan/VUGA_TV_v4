#!/bin/bash

# Database Migration Script for Profile-Based System
# This script runs the complete migration to profile-based storage

echo "==================================="
echo "Database Migration Script"
echo "==================================="
echo ""
echo "This script will migrate your database from user-based to profile-based storage."
echo "It will migrate:"
echo "- Watchlist"
echo "- Favorites" 
echo "- Ratings"
echo "- Watch History"
echo ""
echo "WARNING: This will modify your database structure!"
echo "Make sure you have a backup before proceeding."
echo ""

read -p "Have you backed up your database? (yes/no): " backup_confirm

if [ "$backup_confirm" != "yes" ]; then
    echo "Please backup your database first!"
    echo "You can use: mysqldump -u [username] -p [database_name] > backup_$(date +%Y%m%d_%H%M%S).sql"
    exit 1
fi

echo ""
read -p "Enter MySQL username: " db_user
read -sp "Enter MySQL password: " db_pass
echo ""
read -p "Enter database name: " db_name

echo ""
echo "Running migration..."
echo ""

# Run the migration
mysql -u "$db_user" -p"$db_pass" "$db_name" < migrate_to_profile_based_complete.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration completed successfully!"
    echo ""
    echo "Please verify your data:"
    echo "1. Check that watchlist items have been migrated to profile-based storage"
    echo "2. Check that favorites have been migrated"
    echo "3. Check that ratings have been migrated"
    echo "4. Check that watch history has been updated with profile_id"
    echo ""
    echo "After verification, you can run the cleanup commands manually:"
    echo "- ALTER TABLE app_user DROP COLUMN watchlist_content_ids;"
    echo "- ALTER TABLE app_user_watch_history DROP COLUMN app_user_id;"
    echo "- DROP TABLE IF EXISTS user_favorite;"
    echo "- DROP TABLE IF EXISTS user_rating;"
else
    echo ""
    echo "❌ Migration failed! Please check the error messages above."
    echo "Your database has not been modified."
fi