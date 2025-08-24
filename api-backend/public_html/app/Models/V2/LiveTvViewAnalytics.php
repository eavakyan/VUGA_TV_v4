<?php

namespace App\Models\V2;

class LiveTvViewAnalytics extends BaseModel
{
    protected $table = 'live_tv_view_analytics';
    protected $primaryKey = 'analytics_id';
    
    public $timestamps = false; // Only has created_at
    
    protected $fillable = [
        'tv_channel_id',
        'app_user_id',
        'profile_id',
        'action_type',
        'device_type',
        'user_agent',
        'ip_address',
        'country',
        'watch_duration',
        'metadata'
    ];
    
    protected $casts = [
        'tv_channel_id' => 'integer',
        'app_user_id' => 'integer',
        'profile_id' => 'integer',
        'watch_duration' => 'integer',
        'metadata' => 'array',
        'created_at' => 'datetime'
    ];
    
    /**
     * Get the channel this analytics entry belongs to
     */
    public function channel()
    {
        return $this->belongsTo(TvChannel::class, 'tv_channel_id', 'tv_channel_id');
    }
    
    /**
     * Get the user this analytics entry belongs to
     */
    public function user()
    {
        return $this->belongsTo(AppUser::class, 'app_user_id', 'app_user_id');
    }
    
    /**
     * Scope to filter by action type
     */
    public function scopeByAction($query, $actionType)
    {
        return $query->where('action_type', $actionType);
    }
    
    /**
     * Scope to filter by channel
     */
    public function scopeByChannel($query, $channelId)
    {
        return $query->where('tv_channel_id', $channelId);
    }
    
    /**
     * Scope to filter by user
     */
    public function scopeByUser($query, $userId)
    {
        return $query->where('app_user_id', $userId);
    }
    
    /**
     * Scope to filter by profile
     */
    public function scopeByProfile($query, $profileId)
    {
        return $query->where('profile_id', $profileId);
    }
    
    /**
     * Scope to filter by date range
     */
    public function scopeByDateRange($query, $startDate, $endDate)
    {
        return $query->whereBetween('created_at', [$startDate, $endDate]);
    }
    
    /**
     * Scope to get views only
     */
    public function scopeViews($query)
    {
        return $query->where('action_type', 'view');
    }
    
    /**
     * Scope to get shares only
     */
    public function scopeShares($query)
    {
        return $query->where('action_type', 'share');
    }
    
    /**
     * Scope to get favorites only
     */
    public function scopeFavorites($query)
    {
        return $query->where('action_type', 'favorite');
    }
    
    /**
     * Scope to filter by country
     */
    public function scopeByCountry($query, $country)
    {
        return $query->where('country', $country);
    }
    
    /**
     * Scope to filter by device type
     */
    public function scopeByDevice($query, $deviceType)
    {
        return $query->where('device_type', $deviceType);
    }
    
    /**
     * Create analytics entry for view tracking
     */
    public static function trackView($channelId, $userId = null, $profileId = null, $metadata = [])
    {
        return self::create([
            'tv_channel_id' => $channelId,
            'app_user_id' => $userId,
            'profile_id' => $profileId,
            'action_type' => 'view',
            'device_type' => request()->header('User-Agent-Device'),
            'user_agent' => request()->header('User-Agent'),
            'ip_address' => request()->ip(),
            'country' => request()->header('CF-IPCountry'),
            'metadata' => $metadata
        ]);
    }
    
    /**
     * Create analytics entry for share tracking
     */
    public static function trackShare($channelId, $userId = null, $profileId = null, $metadata = [])
    {
        return self::create([
            'tv_channel_id' => $channelId,
            'app_user_id' => $userId,
            'profile_id' => $profileId,
            'action_type' => 'share',
            'device_type' => request()->header('User-Agent-Device'),
            'user_agent' => request()->header('User-Agent'),
            'ip_address' => request()->ip(),
            'country' => request()->header('CF-IPCountry'),
            'metadata' => $metadata
        ]);
    }
}