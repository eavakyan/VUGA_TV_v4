<?php

namespace App\Models\V2;

class EpisodeSubtitle extends BaseModel
{
    protected $table = 'episode_subtitle';
    protected $primaryKey = 'episode_subtitle_id';
    
    protected $fillable = [
        'episode_id',
        'language_id',
        'file'
    ];
    
    protected $casts = [
        'episode_id' => 'integer',
        'language_id' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the subtitle's episode
     */
    public function episode()
    {
        return $this->belongsTo(Episode::class, 'episode_id');
    }
    
    /**
     * Get the subtitle's language
     */
    public function language()
    {
        return $this->belongsTo(AppLanguage::class, 'language_id', 'app_language_id');
    }
}