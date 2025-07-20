<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class Genre extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_genre';
    protected $primaryKey  = 'genre_id';
}
