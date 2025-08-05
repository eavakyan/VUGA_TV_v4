# VUGA TV Landing Page Deployment Guide

## Files Created:

1. **index.html** - Main landing page
2. **.htaccess** - Server configuration (for Apache servers)
3. **robots.txt** - Search engine instructions
4. **images/** - Folder for image assets

## Before Deploying:

### 1. Add Images
Place the following images in the `images/` folder:
- `favicon.png` - App icon (32x32 or 64x64)
- `og-image.jpg` - Social sharing image (1200x630)
- `app-mockup.png` - Phone mockup with app
- `screenshot-1.jpg` to `screenshot-4.jpg` - App screenshots

### 2. Update Links
- Verify the Google Play Store link is correct
- Add Apple App Store link when available
- Update any social media links if needed

### 3. Update Branch Dashboard
In your Branch dashboard:
- Set default URL to: `https://vugatv.gossip-stone.com`
- Configure OG tags for better sharing

## Deployment Steps:

### Option 1: FTP Upload
1. Connect to your server via FTP
2. Navigate to the web root for vugatv.gossip-stone.com
3. Upload all files and folders
4. Ensure images folder has proper permissions (755)

### Option 2: cPanel File Manager
1. Login to cPanel
2. Navigate to File Manager
3. Go to the directory for vugatv.gossip-stone.com
4. Upload the files
5. Extract if uploaded as zip

### Option 3: Command Line (SSH)
```bash
# Connect to server
ssh username@server

# Navigate to web directory
cd /path/to/vugatv.gossip-stone.com

# Upload files (from local machine)
scp -r /Users/gene/Documents/dev/VUGA_TV_v4/app-landing-page/* username@server:/path/to/vugatv.gossip-stone.com/
```

## Post-Deployment:

1. **Test the site**: Visit https://vugatv.gossip-stone.com
2. **Check all links**: Ensure download buttons work
3. **Test on mobile**: The site should be responsive
4. **Verify SSL**: Make sure HTTPS is working
5. **Test sharing**: Share a link on social media to verify OG tags

## SEO Optimization:

1. Submit to Google Search Console
2. Create and submit sitemap.xml
3. Monitor performance with Google Analytics

## Maintenance:

- Update app store links when iOS app is available
- Add new screenshots as app evolves
- Update feature descriptions as needed
- Monitor and optimize based on user feedback

## Need Help?

The landing page is built with vanilla HTML/CSS for maximum compatibility and easy maintenance. No build process required - just edit and upload!