<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ProfileNotificationStatus extends Model
{
    protected $table = 'profile_notification_status';
    public $timestamps = false;
    
    protected $fillable = [
        'profile_id', 'notification_id', 'shown_at', 'dismissed_at', 'platform'
    ];
    
    protected $casts = [
        'shown_at' => 'datetime',
        'dismissed_at' => 'datetime',
    ];
    
    public function notification(): BelongsTo
    {
        return $this->belongsTo(UserNotification::class, 'notification_id', 'notification_id');
    }
    
    public function profile(): BelongsTo
    {
        return $this->belongsTo(AppUserProfile::class, 'profile_id', 'profile_id');
    }
}