<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class SeasonEpisode extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_series_season_episode';
    protected $primaryKey  = 'episode_id ';

    public function sources()
    {
        return $this->hasMany('App\EpisodeSource', 'episode_id', 'episode_id')->select( 'episode_id','source_title','source_quality','source_size','downloadable','access_type','source_type','source');
    }

    public function subtitles()
    {
        return $this->hasMany('App\EpisodeSubtitles', 'episode_id', 'episode_id')->select( 'episode_id','subtitles_id','language_id','subtitle_file');
    }
 
}
