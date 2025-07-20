<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Episode extends Model
{
    use HasFactory;
    protected $table = 'episodes';

    public function sources()
    {
        return $this->hasMany(EpisodeSource::class, 'episode_id', 'id');
    }

    public function subtitles()
    {
        return $this->hasMany(EpisodeSubtitle::class, 'episode_id', 'id');
    }
    
    
}
