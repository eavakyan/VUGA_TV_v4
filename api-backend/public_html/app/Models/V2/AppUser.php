<?php

namespace App\Models\V2;

class AppUser extends BaseModel
{
    protected $table = 'app_user';
    protected $primaryKey = 'app_user_id';
    
    protected $fillable = [
        'fullname',
        'email',
        'login_type',
        'identity',
        'profile_image',
        'watchlist_content_ids',
        'device_type',
        'device_token'
    ];
    
    protected $casts = [
        'login_type' => 'integer',
        'device_type' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the user's watchlist items (legacy - now profile-based)
     * Returns empty collection since watchlists are profile-based
     */
    public function watchlist()
    {
        // Watchlists are now profile-based, not user-based
        // Return empty collection for backward compatibility
        return $this->belongsToMany(Content::class, 'app_user_watchlist', 'profile_id', 'content_id')
                    ->whereRaw('1 = 0'); // Always return empty
    }
    
    /**
     * Get the user's favorites
     */
    public function favorites()
    {
        return $this->belongsToMany(Content::class, 'app_user_favorite', 'app_user_id', 'content_id')
                    ->withPivot('added_at');
    }
    
    /**
     * Get the user's watch history
     */
    public function watchHistory()
    {
        return $this->hasMany(AppUserWatchHistory::class, 'app_user_id');
    }
    
    /**
     * Get the user's ratings
     */
    public function ratings()
    {
        return $this->belongsToMany(Content::class, 'app_user_rating', 'app_user_id', 'content_id')
                    ->withPivot('rating', 'created_at', 'updated_at');
    }
    
    /**
     * Get the user's TV authentication sessions
     */
    public function tvAuthSessions()
    {
        return $this->hasMany(TvAuthSession::class, 'app_user_id');
    }
    
    /**
     * Get the user's profiles
     */
    public function profiles()
    {
        return $this->hasMany(AppUserProfile::class, 'app_user_id', 'app_user_id');
    }
    
    /**
     * Get the user's last active profile
     */
    public function lastActiveProfile()
    {
        return $this->belongsTo(AppUserProfile::class, 'last_active_profile_id', 'profile_id');
    }
}