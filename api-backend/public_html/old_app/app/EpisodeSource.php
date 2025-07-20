<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class EpisodeSource extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_episode_source';
    protected $primaryKey  = 'source_id ';
 
}
