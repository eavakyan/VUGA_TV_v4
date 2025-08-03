<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;
use Illuminate\Database\Eloquent\Relations\HasMany;

class UserNotification extends Model
{
    protected $table = 'user_notification';
    protected $primaryKey = 'notification_id';
    
    protected $fillable = [
        'title', 'message', 'notification_type', 'target_platforms',
        'priority', 'scheduled_at', 'expires_at',
        'is_active', 'created_by'
    ];
    
    protected $casts = [
        'target_platforms' => 'array',
        'target_user_types' => 'array',
        'is_active' => 'boolean',
        'scheduled_at' => 'datetime',
        'expires_at' => 'datetime',
    ];
    
    public function analytics(): HasOne
    {
        return $this->hasOne(NotificationAnalytics::class, 'notification_id', 'notification_id');
    }
    
    public function statuses(): HasMany
    {
        return $this->hasMany(ProfileNotificationStatus::class, 'notification_id', 'notification_id');
    }
    
    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }
    
    public function scopeScheduled($query)
    {
        return $query->where(function ($q) {
            $q->whereNull('scheduled_at')
              ->orWhere('scheduled_at', '<=', now());
        });
    }
    
    public function scopeNotExpired($query)
    {
        return $query->where(function ($q) {
            $q->whereNull('expires_at')
              ->orWhere('expires_at', '>', now());
        });
    }
    
    public function scopeForPlatform($query, $platform)
    {
        return $query->where(function ($q) use ($platform) {
            $q->whereJsonContains('target_platforms', 'all')
              ->orWhereJsonContains('target_platforms', $platform);
        });
    }
}