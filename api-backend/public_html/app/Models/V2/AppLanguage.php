<?php

namespace App\Models\V2;

class AppLanguage extends BaseModel
{
    protected $table = 'app_language';
    protected $primaryKey = 'app_language_id';
    
    protected $fillable = [
        'title',
        'code'
    ];
    
    protected $casts = [
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    protected $appends = ['id'];
    
    /**
     * Get the id attribute (iOS expects 'id' not 'app_language_id')
     */
    public function getIdAttribute()
    {
        return $this->app_language_id;
    }
    
    /**
     * Get the language's contents
     */
    public function contents()
    {
        return $this->hasMany(Content::class, 'language_id', 'app_language_id');
    }
    
    /**
     * Get the language's subtitles
     */
    public function subtitles()
    {
        return $this->hasMany(Subtitle::class, 'language_id', 'app_language_id');
    }
    
    /**
     * Get the language's episode subtitles
     */
    public function episodeSubtitles()
    {
        return $this->hasMany(EpisodeSubtitle::class, 'language_id', 'app_language_id');
    }
}