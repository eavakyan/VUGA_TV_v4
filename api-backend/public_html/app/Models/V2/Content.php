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
     * Get the content's subtitles (old system)
     */
    public function subtitles()
    {
        return $this->hasMany(Subtitle::class, 'content_id');
    }
    
    /**
     * Get the content's audio tracks (new multi-language system)
     */
    public function audioTracks()
    {
        return $this->hasMany(\App\Models\ContentAudioTrack::class, 'content_id')
                    ->orderBy('sort_order')
                    ->orderBy('is_default', 'desc');
    }
    
    /**
     * Get the content's subtitle tracks (new multi-language system)
     */
    public function subtitleTracks()
    {
        return $this->hasMany(\App\Models\ContentSubtitleTrack::class, 'content_id')
                    ->orderBy('sort_order')
                    ->orderBy('is_default', 'desc');
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
        return $this->belongsToMany(AgeLimit::class, 'content_age_limit', 'content_id', 'age_limit_id');
    }
    
    /**
     * Get the content's rating reasons
     */
    public function ratingReasons()
    {
        return $this->hasMany(ContentRatingReason::class, 'content_id', 'content_id');
    }
    
    /**
     * Get the content's trailers
     */
    public function trailers()
    {
        return $this->hasMany(ContentTrailer::class, 'content_id')
                    ->orderBy('is_primary', 'desc')
                    ->orderBy('sort_order');
    }
    
    /**
     * Get the primary trailer for this content
     */
    public function primaryTrailer()
    {
        return $this->hasOne(ContentTrailer::class, 'content_id')
                    ->where('is_primary', true)
                    ->orderBy('sort_order');
    }
    
    // Backward compatibility getters removed - use trailers relationship instead
    
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
    
    /**
     * Filter by trailer count.
     */
    public function scopeWithTrailers($query)
    {
        return $query->has('trailers');
    }

    /**
     * Check if user has access to this content based on subscriptions
     */
    public function userHasAccess($userId)
    {
        // If no distributor assigned, it's accessible (backward compatibility)
        if (!$this->content_distributor_id) {
            return true;
        }

        // Get distributor info
        $distributor = \DB::table('content_distributor')
            ->where('content_distributor_id', $this->content_distributor_id)
            ->where('is_active', 1)
            ->first();

        if (!$distributor) {
            return false;
        }

        // If content is included in base subscription
        if ($distributor->is_base_included) {
            // Check if user has active base subscription
            $hasBase = \DB::table('user_base_subscription')
                ->where('app_user_id', $userId)
                ->where('is_active', 1)
                ->where(function($q) {
                    $q->whereNull('end_date')
                      ->orWhere('end_date', '>', now());
                })
                ->exists();

            if ($hasBase) {
                return true;
            }
        }

        // Check if user has specific distributor access
        if ($distributor->is_premium) {
            $hasAccess = \DB::table('user_distributor_access')
                ->where('app_user_id', $userId)
                ->where('content_distributor_id', $this->content_distributor_id)
                ->where('is_active', 1)
                ->where(function($q) {
                    $q->whereNull('end_date')
                      ->orWhere('end_date', '>', now());
                })
                ->exists();

            return $hasAccess;
        }

        return false;
    }

    /**
     * Get distributor relationship
     */
    public function distributor()
    {
        return $this->belongsTo(\App\Models\V2\ContentDistributor::class, 'content_distributor_id');
    }
}