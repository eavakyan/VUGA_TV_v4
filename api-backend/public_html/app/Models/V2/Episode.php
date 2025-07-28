<?php

namespace App\Models\V2;

class Episode extends BaseModel
{
    protected $table = 'episode';
    protected $primaryKey = 'episode_id';
    
    protected $fillable = [
        'season_id',
        'number',
        'thumbnail',
        'title',
        'description',
        'duration',
        'total_view',
        'total_download'
    ];
    
    protected $casts = [
        'season_id' => 'integer',
        'number' => 'integer',
        'duration' => 'integer',
        'total_view' => 'integer',
        'total_download' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the episode's season
     */
    public function season()
    {
        return $this->belongsTo(Season::class, 'season_id');
    }
    
    /**
     * Get the episode's sources
     */
    public function sources()
    {
        return $this->hasMany(EpisodeSource::class, 'episode_id');
    }
    
    /**
     * Get the episode's subtitles
     */
    public function subtitles()
    {
        return $this->hasMany(EpisodeSubtitle::class, 'episode_id');
    }
    
    /**
     * Get the episode's watch history
     */
    public function watchHistory()
    {
        return $this->hasMany(AppUserWatchHistory::class, 'episode_id');
    }
}