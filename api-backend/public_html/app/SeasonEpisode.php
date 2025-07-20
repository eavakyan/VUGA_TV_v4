<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class SeasonEpisode extends Model
{
    use HasFactory;

    protected $table = 'episodes';
    // protected $primaryKey  = 'episode_id ';

    public function sources()
    {
        return $this->hasMany('App\EpisodeSource', 'episode_id', 'episode_id')->select( 'episode_id','source_title','source_quality','source_size','downloadable','access_type','source_type','source');
    }

    public function subtitles()
    {
        return $this->hasMany('App\EpisodeSubtitles', 'episode_id', 'episode_id')->select( 'episode_id','subtitles_id','language_id','subtitle_file');
    }
 
}
