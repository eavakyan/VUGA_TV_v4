<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class EpisodeSubtitle extends Model
{
    use HasFactory;
    protected $table = 'episode_subtitle';
    protected $primaryKey = 'episode_subtitle_id';

    public function language()
    {
        return $this->hasOne(Language::class, 'app_language_id', 'language_id');
    }
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->episode_subtitle_id;
    }
}
