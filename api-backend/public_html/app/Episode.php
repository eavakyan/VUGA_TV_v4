<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Episode extends Model
{
    use HasFactory;
    protected $table = 'episode';
    protected $primaryKey = 'episode_id';

    public function sources()
    {
        return $this->hasMany(EpisodeSource::class, 'episode_id', 'episode_id');
    }

    public function subtitles()
    {
        return $this->hasMany(EpisodeSubtitle::class, 'episode_id', 'episode_id');
    }
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->episode_id;
    }
    
    
}
