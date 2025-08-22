#!/bin/bash

# Run Episode Watchlist Migration
# Date: 2025-08-21
# Purpose: Create episode watchlist table and unified view

echo "Running Episode Watchlist Migration..."

# Check if MySQL is accessible
if ! command -v mysql &> /dev/null; then
    echo "MySQL command not found. Please ensure MySQL is installed and accessible."
    exit 1
fi

# Source database credentials from .env if available
if [ -f "../.env" ]; then
    export $(grep -E '^(DB_HOST|DB_PORT|DB_DATABASE|DB_USERNAME|DB_PASSWORD)=' ../.env | xargs)
fi

# Default values if not set in .env
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_DATABASE=${DB_DATABASE:-tv_project}
DB_USERNAME=${DB_USERNAME:-root}
DB_PASSWORD=${DB_PASSWORD:-""}

# Build MySQL connection string
MYSQL_CMD="mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USERNAME}"
if [ ! -z "$DB_PASSWORD" ]; then
    MYSQL_CMD="${MYSQL_CMD} -p${DB_PASSWORD}"
fi
MYSQL_CMD="${MYSQL_CMD} ${DB_DATABASE}"

echo "Connecting to database: ${DB_DATABASE} on ${DB_HOST}:${DB_PORT}"

# Run the migration
echo "Creating episode watchlist table and unified view..."
$MYSQL_CMD < migrations/2025_08_21_create_episode_watchlist_table.sql

if [ $? -eq 0 ]; then
    echo "Migration completed successfully!"
    echo ""
    echo "New features added:"
    echo "- Episodes can now be added to user profile watchlists"
    echo "- Unified watchlist view combining movies, TV shows, and episodes"
    echo "- New API endpoints for episode watchlist management"
    echo ""
    echo "API Endpoints:"
    echo "- POST /api/v2/user/toggle-episode-watchlist"
    echo "- POST /api/v2/user/check-episode-watchlist" 
    echo "- POST /api/v2/user/fetch-unified-watchlist"
else
    echo "Migration failed! Please check the error messages above."
    exit 1
fi