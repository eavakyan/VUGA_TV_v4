<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class EpisodeSubtitles extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_episode_subtitles';
    protected $primaryKey  = 'subtitles_id ';
 
}
