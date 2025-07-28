<?php

namespace App\Models\V2;

class AppUserWatchHistory extends BaseModel
{
    protected $table = 'app_user_watch_history';
    protected $primaryKey = 'watch_history_id';
    
    protected $fillable = [
        'app_user_id',
        'content_id',
        'episode_id',
        'last_watched_position',
        'total_duration',
        'completed',
        'device_type'
    ];
    
    protected $casts = [
        'app_user_id' => 'integer',
        'content_id' => 'integer',
        'episode_id' => 'integer',
        'last_watched_position' => 'integer',
        'total_duration' => 'integer',
        'completed' => 'boolean',
        'device_type' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the watch history's user
     */
    public function user()
    {
        return $this->belongsTo(AppUser::class, 'app_user_id');
    }
    
    /**
     * Get the watch history's content
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id');
    }
    
    /**
     * Get the watch history's episode
     */
    public function episode()
    {
        return $this->belongsTo(Episode::class, 'episode_id');
    }
}