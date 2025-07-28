<?php

namespace App\Models\V2;

class EpisodeSource extends BaseModel
{
    protected $table = 'episode_source';
    protected $primaryKey = 'episode_source_id';
    
    protected $fillable = [
        'episode_id',
        'title',
        'quality',
        'size',
        'is_download',
        'access_type',
        'type',
        'source'
    ];
    
    protected $casts = [
        'episode_id' => 'integer',
        'is_download' => 'integer',
        'access_type' => 'integer',
        'type' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the source's episode
     */
    public function episode()
    {
        return $this->belongsTo(Episode::class, 'episode_id');
    }
}