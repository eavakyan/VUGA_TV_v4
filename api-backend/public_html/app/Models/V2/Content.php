<?php

namespace App\Models\V2;

class Content extends BaseModel
{
    protected $table = 'content';
    protected $primaryKey = 'content_id';
    
    protected $fillable = [
        'title',
        'description',
        'type',
        'duration',
        'release_year',
        'ratings',
        'language_id',
        'trailer_url',
        'vertical_poster',
        'horizontal_poster',
        'genre_ids',
        'is_featured',
        'is_show',
        'total_view',
        'total_download',
        'total_share'
    ];
    
    protected $casts = [
        'type' => 'integer',
        'duration' => 'integer',
        'release_year' => 'integer',
        'ratings' => 'float',
        'language_id' => 'integer',
        'is_featured' => 'boolean',
        'is_show' => 'boolean',
        'total_view' => 'integer',
        'total_download' => 'integer',
        'total_share' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the content's language
     */
    public function language()
    {
        return $this->belongsTo(AppLanguage::class, 'language_id', 'app_language_id');
    }
    
    /**
     * Get the content's sources
     */
    public function sources()
    {
        return $this->hasMany(ContentSource::class, 'content_id');
    }
    
    /**
     * Get the content's cast members
     */
    public function casts()
    {
        return $this->hasMany(ContentCast::class, 'content_id');
    }
    
    /**
     * Get the content's actors through cast
     */
    public function actors()
    {
        return $this->belongsToMany(Actor::class, 'content_cast', 'content_id', 'actor_id')
                    ->withPivot('character_name');
    }
    
    /**
     * Get the content's subtitles
     */
    public function subtitles()
    {
        return $this->hasMany(Subtitle::class, 'content_id');
    }
    
    /**
     * Get the content's seasons (for series)
     */
    public function seasons()
    {
        return $this->hasMany(Season::class, 'content_id');
    }
    
    /**
     * Get the content's genres
     */
    public function genres()
    {
        return $this->belongsToMany(Genre::class, 'content_genre', 'content_id', 'genre_id');
    }
    
    /**
     * Get the content's age limits
     */
    public function ageLimits()
    {
        return $this->belongsToMany(AgeLimit::class, 'content_age_limits', 'content_id', 'age_limit_id')
                    ->withTimestamps();
    }
    
    /**
     * Get profiles who have this content in their watchlist
     */
    public function watchlistedBy()
    {
        return $this->belongsToMany(AppUserProfile::class, 'app_user_watchlist', 'content_id', 'profile_id')
                    ->withTimestamps();
    }
    
    /**
     * Get profiles who have favorited this content
     */
    public function favoritedBy()
    {
        return $this->belongsToMany(AppUserProfile::class, 'app_profile_favorite', 'content_id', 'profile_id')
                    ->withTimestamps();
    }
    
    /**
     * Get the content's ratings from profiles
     */
    public function profileRatings()
    {
        return $this->belongsToMany(AppUserProfile::class, 'app_profile_rating', 'content_id', 'profile_id')
                    ->withPivot('rating')
                    ->withTimestamps();
    }
    
    /**
     * Get the content's watch history by profiles
     */
    public function profileWatchHistory()
    {
        return $this->hasMany(AppUserWatchHistory::class, 'content_id', 'content_id');
    }
    
    /**
     * Scope for featured content
     */
    public function scopeFeatured($query)
    {
        return $query->where('is_featured', 1);
    }
    
    /**
     * Scope for visible content
     */
    public function scopeVisible($query)
    {
        return $query->where('is_show', 1);
    }
    
    /**
     * Scope for movies
     */
    public function scopeMovies($query)
    {
        return $query->where('type', 1);
    }
    
    /**
     * Scope for series
     */
    public function scopeSeries($query)
    {
        return $query->where('type', 2);
    }
}