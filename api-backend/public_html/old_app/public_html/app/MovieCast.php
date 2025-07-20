<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class MovieCast extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_movie_cast';
    protected $primaryKey  = 'movie_cast_id ';
 
}
