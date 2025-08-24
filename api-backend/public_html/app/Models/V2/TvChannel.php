<?php

namespace App\Models\V2;

use Carbon\Carbon;

class TvChannel extends BaseModel
{
    protected $table = 'tv_channel';
    protected $primaryKey = 'tv_channel_id';
    
    protected $fillable = [
        'title',
        'thumbnail',
        'logo_url',
        'stream_url',
        'channel_number',
        'access_type',
        'is_active',
        'category_ids',
        'type',
        'source',
        'epg_url',
        'total_views',
        'total_shares',
        'language',
        'country_code',
        'description',
        'streaming_qualities'
    ];
    
    protected $casts = [
        'access_type' => 'integer',
        'type' => 'integer',
        'is_active' => 'boolean',
        'channel_number' => 'integer',
        'total_views' => 'integer',
        'total_shares' => 'integer',
        'streaming_qualities' => 'array',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get categories for this channel
     */
    public function categories()
    {
        return $this->belongsToMany(TvCategory::class, 'tv_channel_category', 'tv_channel_id', 'tv_category_id');
    }
    
    /**
     * Get schedule entries for this channel
     */
    public function schedules()
    {
        return $this->hasMany(LiveTvSchedule::class, 'tv_channel_id', 'tv_channel_id');
    }
    
    /**
     * Get view analytics for this channel
     */
    public function analytics()
    {
        return $this->hasMany(LiveTvViewAnalytics::class, 'tv_channel_id', 'tv_channel_id');
    }
    
    /**
     * Get category IDs as array
     */
    public function getCategoryIdsArrayAttribute()
    {
        if (empty($this->category_ids)) {
            return [];
        }
        return array_map('intval', explode(',', $this->category_ids));
    }
    
    /**
     * Get current program for this channel
     */
    public function getCurrentProgram()
    {
        $now = Carbon::now();
        
        return $this->schedules()
                   ->where('start_time', '<=', $now)
                   ->where('end_time', '>', $now)
                   ->first();
    }
    
    /**
     * Get upcoming programs for this channel
     */
    public function getUpcomingPrograms($hours = 24, $limit = 10)
    {
        $now = Carbon::now();
        $endTime = $now->copy()->addHours($hours);
        
        return $this->schedules()
                   ->where('start_time', '>', $now)
                   ->where('start_time', '<=', $endTime)
                   ->orderBy('start_time')
                   ->limit($limit)
                   ->get();
    }
    
    /**
     * Get next program for this channel
     */
    public function getNextProgram()
    {
        $now = Carbon::now();
        
        return $this->schedules()
                   ->where('start_time', '>', $now)
                   ->orderBy('start_time')
                   ->first();
    }
    
    /**
     * Get programs for a specific date
     */
    public function getProgramsForDate($date)
    {
        $startOfDay = Carbon::parse($date)->startOfDay();
        $endOfDay = Carbon::parse($date)->endOfDay();
        
        return $this->schedules()
                   ->whereBetween('start_time', [$startOfDay, $endOfDay])
                   ->orderBy('start_time')
                   ->get();
    }
    
    /**
     * Scope to get only active channels
     */
    public function scopeActive($query)
    {
        // Check if is_active column exists before applying filter
        $columns = \Schema::getColumnListing($this->getTable());
        if (in_array('is_active', $columns)) {
            return $query->where('is_active', true);
        }
        return $query; // Return query unchanged if column doesn't exist
    }
    
    /**
     * Scope to get channels by language
     */
    public function scopeByLanguage($query, $language)
    {
        $columns = \Schema::getColumnListing($this->getTable());
        if (in_array('language', $columns)) {
            return $query->where('language', $language);
        }
        return $query; // Return query unchanged if column doesn't exist
    }
    
    /**
     * Scope to get channels by country
     */
    public function scopeByCountry($query, $country)
    {
        $columns = \Schema::getColumnListing($this->getTable());
        if (in_array('country_code', $columns)) {
            return $query->where('country_code', $country);
        }
        return $query; // Return query unchanged if column doesn't exist
    }
    
    /**
     * Scope to get channels by access type
     */
    public function scopeByAccessType($query, $accessType)
    {
        return $query->where('access_type', $accessType);
    }
    
    /**
     * Get formatted channel number with padding
     */
    public function getFormattedChannelNumberAttribute()
    {
        if (!$this->channel_number) {
            return null;
        }
        return str_pad($this->channel_number, 3, '0', STR_PAD_LEFT);
    }
    
    /**
     * Get logo URL with fallback to thumbnail
     */
    public function getLogoAttribute()
    {
        return $this->logo_url ?: $this->thumbnail;
    }
    
    /**
     * Check if channel has EPG data
     */
    public function getHasEpgAttribute()
    {
        return !empty($this->epg_url) || $this->schedules()->count() > 0;
    }
    
    /**
     * Increment view count
     */
    public function incrementViews($count = 1)
    {
        $this->increment('total_views', $count);
    }
    
    /**
     * Increment share count
     */
    public function incrementShares($count = 1)
    {
        $this->increment('total_shares', $count);
    }
}