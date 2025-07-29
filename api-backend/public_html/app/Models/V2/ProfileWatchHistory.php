<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ProfileWatchHistory extends Model
{
    protected $table = 'profile_watch_history';
    protected $primaryKey = 'history_id';
    const UPDATED_AT = 'last_watched_at';
    
    protected $fillable = [
        'profile_id',
        'content_id',
        'episode_id',
        'last_watched_position',
        'total_duration',
        'completed'
    ];
    
    protected $casts = [
        'completed' => 'boolean',
        'last_watched_position' => 'integer',
        'total_duration' => 'integer',
        'created_at' => 'datetime',
        'last_watched_at' => 'datetime'
    ];
    
    // Relationships
    public function profile(): BelongsTo
    {
        return $this->belongsTo(AppUserProfile::class, 'profile_id', 'profile_id');
    }
    
    public function content(): BelongsTo
    {
        return $this->belongsTo(Content::class, 'content_id', 'content_id');
    }
    
    public function episode(): BelongsTo
    {
        return $this->belongsTo(Episode::class, 'episode_id', 'episode_id');
    }
    
    // Methods
    public function getProgressPercentageAttribute()
    {
        if ($this->total_duration > 0) {
            return round(($this->last_watched_position / $this->total_duration) * 100, 2);
        }
        return 0;
    }
    
    // Scopes
    public function scopeIncomplete($query)
    {
        return $query->where('completed', 0);
    }
    
    public function scopeCompleted($query)
    {
        return $query->where('completed', 1);
    }
    
    public function scopeRecent($query)
    {
        return $query->orderBy('last_watched_at', 'desc');
    }
}