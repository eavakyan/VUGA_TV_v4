# Database Data Extraction Guide

## Step 1: Connect to Your MySQL Database

Use your preferred MySQL client (phpMyAdmin, MySQL Workbench, or command line) to connect to your database.

## Step 2: Run the Extraction Queries

Execute the queries in `extract_sample_data.sql` in your MySQL environment. Each query will give you specific data:

1. **Movie Data**: Basic movie information with language
2. **Movie Genres**: All genres associated with the movie
3. **Movie Sources**: Video sources for the movie
4. **Movie Subtitles**: Subtitle files for the movie
5. **Movie Cast**: Actors and their characters
6. **TV Series Data**: Basic series information
7. **Seasons**: Season information for the series
8. **Episodes**: Episode data for the series
9. **Episode Sources**: Video sources for episodes
10. **Episode Subtitles**: Subtitle files for episodes
11. **Actor Data**: Complete actor profiles

## Step 3: Export Results

Copy the results from each query and save them as CSV or JSON files, or just copy the data and share it with me.

## Step 4: I'll Convert to MongoDB Format

Once you provide the real data results, I'll create the proper MongoDB documents with your actual content.

## Alternative: Database Connection

If you can provide database connection details, I can:
```bash
# Connect directly to your database
mysql -h your_host -u your_username -p your_database

# Or provide connection string like:
# Host: localhost or your_server_ip
# Username: your_db_username  
# Password: your_db_password
# Database: your_database_name
# Port: 3306 (or your custom port)
```

## Quick Test Queries

If you want to quickly see what data is available, run these simple queries first:

```sql
-- Check what content you have
SELECT id, title, type, is_show FROM contents LIMIT 5;

-- Check actors
SELECT id, fullname FROM actors LIMIT 5;

-- Check genres
SELECT id, title FROM genres LIMIT 5;
```

Let me know which approach works best for you! 