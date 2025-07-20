<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class SeriesSeason extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_series_season';
    protected $primaryKey  = 'season_id ';
 
    public function episodes()
    {
        return $this->hasMany('App\SeasonEpisode', 'season_id', 'season_id')->select( 'season_id','episode_id', 'episode_title', 'episode_thumb','episode_description','episode_duration','access_type');
    }
    
}
