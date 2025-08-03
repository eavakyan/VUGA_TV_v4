<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class NotificationAnalytics extends Model
{
    protected $table = 'notification_analytics';
    protected $primaryKey = 'notification_id';
    public $timestamps = false;
    
    protected $fillable = [
        'notification_id', 'total_eligible_profiles', 'total_shown', 
        'total_dismissed', 'ios_shown', 'android_shown', 'android_tv_shown'
    ];
    
    protected $casts = [
        'total_eligible_profiles' => 'integer',
        'total_shown' => 'integer',
        'total_dismissed' => 'integer',
        'ios_shown' => 'integer',
        'android_shown' => 'integer',
        'android_tv_shown' => 'integer',
    ];
    
    public function notification(): BelongsTo
    {
        return $this->belongsTo(UserNotification::class, 'notification_id', 'notification_id');
    }
}