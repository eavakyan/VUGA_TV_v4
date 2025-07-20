# TV App PHP Laravel Backend

A comprehensive PHP Laravel backend API application for a TV/Movie streaming platform with MySQL database integration, featuring user management, content catalog, live TV channels, custom advertisements, and administrative dashboard. This API serves as the backend for a multi-platform streaming service supporting iOS mobile, Android mobile, and Android TV applications.

## 🌐 Application Overview

This Laravel-based backend provides a complete streaming platform with:

- **User Management**: Registration, authentication, profile management, and subscription handling
- **Content Management**: Movies, TV series, documentaries with metadata, sources, and subtitles
- **Live TV**: Live TV channels with categories and streaming capabilities
- **Custom Advertising**: Branded advertisements with analytics and click tracking
- **Admin Dashboard**: Comprehensive web-based administration panel
- **Analytics**: User engagement tracking, view counts, and performance metrics
- **Multi-Platform Support**: API designed to serve iOS, Android, and Android TV applications

## 🚀 Key Features

### Core Functionality
- **User Management**: Complete user profiles with social login support (Google, Facebook, Apple, Email)
- **Content Catalog**: Movies, TV series, documentaries with rich metadata
- **Live TV Streaming**: Live TV channels with category organization
- **Custom Advertisements**: Branded ads with platform-specific targeting
- **Subscription Management**: User subscription handling and package management
- **Analytics Dashboard**: Comprehensive user and content analytics
- **Multi-Platform Support**: API designed to serve iOS, Android, and Android TV applications

### Technical Features
- **Laravel Framework**: Built on Laravel 9 with modern PHP practices
- **MySQL Database**: Full database connectivity with Eloquent ORM
- **RESTful API Design**: Standard HTTP endpoints with consistent response formats
- **File Management**: AWS S3 and DigitalOcean Spaces integration for media storage
- **Security Features**: Input validation, CSRF protection, secure file uploads
- **Admin Dashboard**: Web-based administration panel with DataTables integration
- **Push Notifications**: Firebase Cloud Messaging integration for mobile notifications
- **TMDB Integration**: The Movie Database API integration for content metadata

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- **PHP** (^7.4|^8.0 or higher)
- **Composer** (for dependency management)
- **MySQL** (v5.7 or higher)
- **Web Server** (Apache/Nginx)
- **Git** (for version control)

## 🛠️ Installation & Setup

### 1. Clone and Install Dependencies

```bash
# Clone the repository (if not already done)
git clone <repository-url>
cd TV_App_PHP_Backend/public_html

# Install PHP dependencies
composer install
```

### 2. Environment Configuration

Create a `.env` file in the `public_html` directory by copying the example:

```bash
cp .env.example .env
```

Edit the `.env` file with your configuration:

```env
# Application Configuration
APP_NAME="TV Streaming App"
APP_ENV=local
APP_KEY=base64:your-app-key-here
APP_DEBUG=true
APP_URL=http://localhost

# Database Configuration
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=tv_app_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

# File Storage Configuration
FILESYSTEM_DISK=local
# For AWS S3
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_DEFAULT_REGION=us-east-1
AWS_BUCKET=your_bucket_name
# For DigitalOcean Spaces
DO_SPACE_ACCESS_KEY_ID=your_do_key
DO_SPACE_SECRET_ACCESS_KEY=your_do_secret
DO_SPACE_REGION=nyc3
DO_SPACE_BUCKET=your_bucket_name

# TMDB API Configuration
TMDB_API_KEY=your_tmdb_api_key

# Firebase Configuration (for push notifications)
FIREBASE_SERVER_KEY=your_firebase_server_key
```

### 3. Database Setup

```bash
# Generate application key
php artisan key:generate

# Run database migrations
php artisan migrate

# Seed the database with sample data (optional)
php artisan db:seed

# Create storage link for public access to uploaded files
php artisan storage:link
```

### 4. Web Server Configuration

#### Apache Configuration
Ensure your Apache configuration points to the `public_html/public` directory and has proper rewrite rules.

#### Nginx Configuration
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/TV_App_PHP_Backend/public_html/public;

    add_header X-Frame-Options "SAMEORIGIN";
    add_header X-Content-Type-Options "nosniff";

    index index.php;

    charset utf-8;

    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }

    location = /favicon.ico { access_log off; log_not_found off; }
    location = /robots.txt  { access_log off; log_not_found off; }

    error_page 404 /index.php;

    location ~ \.php$ {
        fastcgi_pass unix:/var/run/php/php8.1-fpm.sock;
        fastcgi_param SCRIPT_FILENAME $realpath_root$fastcgi_script_name;
        include fastcgi_params;
    }

    location ~ /\.(?!well-known).* {
        deny all;
    }
}
```

## 📁 Project Structure

```
public_html/
├── app/
│   ├── Http/
│   │   ├── Controllers/
│   │   │   ├── Api/                    # API Controllers
│   │   │   │   ├── UserController.php
│   │   │   │   ├── ContentController.php
│   │   │   │   ├── ContentDetailsController.php
│   │   │   │   ├── AnalyticsController.php
│   │   │   │   ├── WatchlistController.php
│   │   │   │   ├── TVController.php
│   │   │   │   └── SettingsController.php
│   │   │   ├── Admin/                  # Admin Panel Controllers
│   │   │   │   ├── AdminController.php
│   │   │   │   ├── ContentController.php
│   │   │   │   ├── UserController.php
│   │   │   │   ├── SettingsController.php
│   │   │   │   ├── TVController.php
│   │   │   │   ├── GenreController.php
│   │   │   │   ├── LanguageController.php
│   │   │   │   ├── ActorController.php
│   │   │   │   └── SubscriptionController.php
│   │   │   ├── CustomAdsController.php # Custom Advertisement Management
│   │   │   ├── TVController.php        # Live TV Management
│   │   │   ├── ActorController.php     # Actor/Cast Management
│   │   │   ├── GenreController.php     # Genre Management
│   │   │   ├── LanguageController.php  # Language Management
│   │   │   ├── NotificationController.php # Push Notifications
│   │   │   ├── SettingController.php   # Application Settings
│   │   │   ├── MediaGalleryController.php # Media File Management
│   │   │   ├── AdmobController.php     # AdMob Integration
│   │   │   └── LoginController.php     # Authentication
│   │   ├── Middleware/                 # Custom Middleware
│   │   ├── Requests/                   # Form Request Validation
│   │   └── Resources/                  # API Response Resources
│   ├── Models/                         # Eloquent Models
│   ├── Services/                       # Business Logic Services
│   ├── Repositories/                   # Data Access Layer
│   └── Providers/                      # Service Providers
├── config/                             # Configuration Files
├── database/
│   ├── migrations/                     # Database Migrations
│   ├── seeders/                        # Database Seeders
│   └── factories/                      # Model Factories
├── public/                             # Public Assets
├── resources/
│   ├── views/                          # Blade Templates
│   ├── js/                             # JavaScript Files
│   └── css/                            # Stylesheets
├── routes/
│   ├── api.php                         # API Routes
│   ├── web.php                         # Web Routes
│   └── admin.php                       # Admin Routes
├── storage/                            # File Storage
├── vendor/                             # Composer Dependencies
├── artisan                             # Laravel Artisan CLI
├── composer.json                       # PHP Dependencies
└── .env                                # Environment Configuration
```

## 🔌 API Endpoints

### Base URL Structure
- **API Base**: `/api/`
- **Authentication**: Header-based verification system
- **Response Format**: JSON

### Common Response Structure
```json
{
  "status": true/false,
  "message": "Response message",
  "data": {} // Response data (varies by endpoint)
}
```

### 1. User Management (`/api/User/`)

#### User Registration
- **Endpoint**: `POST /api/User/registration`
- **Parameters**:
  ```json
  {
    "identity": "string (required) - unique user identifier",
    "email": "string (required) - user email",
    "login_type": "integer (required) - 1=Google, 2=Facebook, 3=Apple, 4=Email",
    "device_type": "integer (required) - 1=Android, 2=iOS",
    "device_token": "string (required) - FCM/push notification token",
    "fullname": "string (optional) - user full name"
  }
  ```

#### Get User Profile
- **Endpoint**: `POST /api/User/getProfile`
- **Parameters**: `{"user_id": "integer (required)"}`

#### Update User Profile
- **Endpoint**: `POST /api/User/updateProfile`
- **Parameters**: User data with optional profile image upload

#### User Logout
- **Endpoint**: `POST /api/User/Logout`
- **Parameters**: `{"user_id": "integer (required)"}`

#### Delete Account
- **Endpoint**: `POST /api/User/deleteMyAccount`
- **Parameters**: `{"user_id": "integer (required)"}`

### 2. Content Management (`/api/Content/`)

#### Get Home Page Content
- **Endpoint**: `POST /api/Content/GetHomeContentList`
- **Parameters**: `{"user_id": "integer (required)"}`
- **Response**: Featured content, watchlist, top contents, genre contents

#### Get All Content List
- **Endpoint**: `POST /api/Content/getAllContentList`
- **Parameters**: Pagination (start, limit) and filters (type, genre, language)

#### Search Content
- **Endpoint**: `POST /api/Content/searchContent`
- **Parameters**: Search criteria with pagination

#### Get Content Details
- **Endpoint**: `POST /api/Content/getContentDetailsByID`
- **Parameters**: `{"content_id": "integer (required)", "user_id": "integer (required)"}`

#### Get Content Sources
- **Endpoint**: `POST /api/Content/getSourceByContentID`
- **Parameters**: `{"content_id": "integer (required)"}`

#### Get Content Subtitles
- **Endpoint**: `POST /api/Content/getSubtitlesByContentID`
- **Parameters**: `{"content_id": "integer (required)"}`

#### Series-Specific Endpoints
- **Get Seasons**: `POST /api/Content/getSeasonByContentID`
- **Get Episodes**: `POST /api/Content/getEpisodeBySeasonID`
- **Get Episode Sources**: `POST /api/Content/getSourceByEpisodeID`
- **Get Episode Subtitles**: `POST /api/Content/getSubtitlesByEpisodeID`

### 3. Live TV (`/api/TV/`)

#### Get TV Categories
- **Endpoint**: `POST /api/TV/GetTvCategoryist`

#### Get All TV Channels
- **Endpoint**: `POST /api/TV/getAllTvChannelList`
- **Parameters**: Pagination (start, limit)

#### Get TV Channels by Category
- **Endpoint**: `POST /api/TV/getTvChannelListByCategoryID`
- **Parameters**: `{"tv_category_id": "integer (required)", "start": "integer", "limit": "integer"}`

#### TV Analytics
- **Increase View**: `POST /api/TV/increaseTVChannelView`
- **Increase Share**: `POST /api/TV/increaseTVChannelShare`

### 4. Custom Advertisements (`/api/Ads/`)

#### Fetch Custom Ads
- **Endpoint**: `POST /api/Ads/fetchCustomAds`
- **Parameters**: Platform-specific targeting

#### Ad Analytics
- **Increase View**: `POST /api/Ads/increaseAdView`
- **Increase Click**: `POST /api/Ads/increaseAdClick`

### 5. Watchlist Management (`/api/Content/`)

#### Add to Watchlist
- **Endpoint**: `POST /api/Content/addToWatchList`
- **Parameters**: `{"user_id": "integer", "content_id": "integer"}`

#### Remove from Watchlist
- **Endpoint**: `POST /api/Content/removeFromWatchList`
- **Parameters**: `{"user_id": "integer", "content_id": "integer"}`

#### Get Watchlist
- **Endpoint**: `POST /api/Content/getWatchlist`
- **Parameters**: `{"user_id": "integer"}`

### 6. Analytics (`/api/Content/`)

#### Content Analytics
- **Increase View**: `POST /api/Content/increaseContentView`
- **Increase Download**: `POST /api/Content/increaseContentDownload`
- **Increase Share**: `POST /api/Content/increaseContentShare`

#### Episode Analytics
- **Increase View**: `POST /api/Content/increaseEpisodeView`
- **Increase Download**: `POST /api/Content/increaseEpisodeDownload`

### 7. Settings (`/api/`)

#### Get Application Settings
- **Endpoint**: `GET /api/getSettings`

#### Get Notifications
- **Endpoint**: `POST /api/getAllNotification`

#### Get Subscription Packages
- **Endpoint**: `GET /api/getSubscriptionPackage`

## 🖥️ Admin Dashboard

### Access
- **URL**: `/admin/login`
- **Default Credentials**: Configured in database

### Features

#### 1. Dashboard Overview
- User statistics and analytics
- Content management overview
- System health monitoring

#### 2. Content Management
- **Movies & Series**: CRUD operations for content
- **Content Sources**: Video file management
- **Content Subtitles**: Multi-language subtitle support
- **Content Cast**: Actor/actress management
- **Series Seasons & Episodes**: TV series organization

#### 3. User Management
- User listing and search
- User profile management
- User deletion and updates

#### 4. Live TV Management
- **TV Categories**: Category creation and management
- **TV Channels**: Channel management with sources
- **Channel Analytics**: View and share tracking

#### 5. Advertisement Management
- **Custom Ads**: Branded advertisement creation
- **AdMob Integration**: Google AdMob configuration
- **Ad Analytics**: View and click tracking

#### 6. System Settings
- **Application Settings**: App configuration
- **Storage Settings**: AWS S3/DigitalOcean Spaces configuration
- **Notification Settings**: Push notification configuration

#### 7. Content Metadata
- **Genres**: Genre management
- **Languages**: Language support configuration
- **Actors**: Actor/actress database management

#### 8. Subscription Management
- **Subscription Packages**: Package creation and pricing
- **User Subscriptions**: Subscription tracking and management

## 📊 Data Models

### User Model
```php
{
  id: integer,
  fullname: string,
  email: string,
  identity: string (unique),
  login_type: integer (1=Google, 2=Facebook, 3=Apple, 4=Email),
  device_type: integer (1=Android, 2=iOS),
  device_token: string,
  profile_image: string,
  watchlist_content_ids: text,
  created_at: timestamp,
  updated_at: timestamp
}
```

### Content Model
```php
{
  id: integer,
  title: string,
  description: text,
  type: integer (1=Movie, 2=Series, 3=Live TV),
  genre_ids: text,
  language_id: integer,
  release_year: integer,
  ratings: decimal,
  is_featured: boolean,
  created_at: timestamp,
  updated_at: timestamp
}
```

### TV Channel Model
```php
{
  id: integer,
  title: string,
  description: text,
  category_ids: text,
  total_view: integer,
  total_share: integer,
  created_at: timestamp,
  updated_at: timestamp
}
```

### Custom Ad Model
```php
{
  id: integer,
  title: string,
  brand_name: string,
  brand_logo: string,
  button_text: string,
  start_date: date,
  end_date: date,
  is_android: boolean,
  android_link: string,
  is_ios: boolean,
  ios_link: string,
  views: integer,
  clicks: integer,
  status: integer,
  created_at: timestamp,
  updated_at: timestamp
}
```

## 🔒 Security Features

### Built-in Security
- **CSRF Protection**: Laravel's built-in CSRF token protection
- **Input Validation**: Comprehensive request validation
- **File Upload Security**: Secure file upload handling with validation
- **SQL Injection Protection**: Eloquent ORM with parameterized queries
- **XSS Protection**: Output escaping and sanitization
- **Authentication**: Session-based authentication for admin panel

### File Storage Security
- **Secure File Uploads**: File type and size validation
- **Cloud Storage**: AWS S3 and DigitalOcean Spaces integration
- **File Access Control**: Proper file access permissions

## 📈 Analytics Capabilities

### User Analytics
- User registration and login tracking
- Device type and platform analytics
- User engagement metrics

### Content Analytics
- Content view counts and trends
- Download and share tracking
- Episode-specific analytics for series

### Advertisement Analytics
- Custom ad view and click tracking
- Platform-specific ad performance
- Ad engagement metrics

### Live TV Analytics
- Channel view counts
- Share tracking
- Category performance analysis

## 🚀 Production Deployment

### Recommended Production Setup
- **Web Server**: Nginx with PHP-FPM
- **Database**: MySQL with proper indexing
- **File Storage**: AWS S3 or DigitalOcean Spaces
- **Caching**: Redis for session and cache storage
- **SSL/TLS**: HTTPS encryption for all endpoints
- **CDN**: Content Delivery Network for media files
- **Monitoring**: Application performance monitoring
- **Backup**: Automated database and file backups

### Environment Variables for Production
```env
APP_ENV=production
APP_DEBUG=false
APP_URL=https://yourdomain.com

# Database
DB_HOST=your_db_host
DB_DATABASE=your_db_name
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# File Storage
FILESYSTEM_DISK=s3
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_BUCKET=your_bucket_name

# Security
SESSION_SECURE_COOKIE=true
SESSION_HTTP_ONLY=true
```

### Performance Optimization
- **Database Optimization**: Proper indexing and query optimization
- **Caching**: Redis caching for frequently accessed data
- **File Optimization**: Image compression and video optimization
- **CDN Integration**: Global content delivery
- **Load Balancing**: Multiple server instances

## 🔧 Development Notes

### Extending the API
1. **Add New Endpoints**: Create new controllers in `app/Http/Controllers/Api/`
2. **Database Changes**: Create migrations for new tables
3. **New Features**: Follow existing patterns and conventions
4. **Validation**: Use Form Request classes for validation
5. **Testing**: Write unit and feature tests

### API Versioning
- Current version: v1
- All endpoints are under `/api/` prefix
- Consider versioning for future updates

### Error Handling
- Consistent error response format
- Proper HTTP status codes
- Detailed error messages for debugging

## 🧪 Testing the API

### Quick Start - Test All Endpoints

```bash
# Health check
curl -X POST http://your-domain.com/api/User/registration \
  -H "Content-Type: application/json" \
  -d '{"identity":"test_user","email":"test@example.com","login_type":4,"device_type":1,"device_token":"test_token","fullname":"Test User"}'

# Get home content
curl -X POST http://your-domain.com/api/Content/GetHomeContentList \
  -H "Content-Type: application/json" \
  -d '{"user_id":1}'

# Search content
curl -X POST http://your-domain.com/api/Content/searchContent \
  -H "Content-Type: application/json" \
  -d '{"start":0,"limit":10,"keyword":"movie"}'
```

### Testing Admin Panel
1. Access `/admin/login`
2. Use configured admin credentials
3. Navigate through different sections
4. Test CRUD operations

## 📝 API Documentation

### Complete API Documentation
- **OpenAPI Specification**: Available in `openapi.yaml`
- **API Requirements**: Detailed in `API_requirements.md`
- **Sample Data**: MongoDB samples in `real_mongodb_samples.md`

### Response Examples

#### Successful Response
```json
{
  "status": true,
  "message": "Operation completed successfully",
  "data": {
    "id": 1,
    "title": "Sample Content",
    "description": "Content description"
  }
}
```

#### Error Response
```json
{
  "status": false,
  "message": "Error description",
  "error": "Detailed error information"
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and test thoroughly
4. Commit changes: `git commit -am 'Add feature'`
5. Push to branch: `git push origin feature-name`
6. Submit a pull request with detailed description

### Development Guidelines
- Follow Laravel coding standards
- Add appropriate error handling
- Include tests for new features
- Update documentation for API changes
- Ensure backward compatibility

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Check the troubleshooting section above
- Review the API documentation
- Test with the provided sample endpoints
- Check the deployment guides for production issues

---

**Ready to power your TV streaming platform!** 🎬

*This Laravel backend is designed to serve as the foundation for a multi-platform streaming service, supporting iOS mobile, Android mobile, and Android TV applications with a comprehensive content management system and administrative dashboard.*
