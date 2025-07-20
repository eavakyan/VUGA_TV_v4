<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class MovieReview extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_movie_review';
    protected $primaryKey  = 'review_id ';
 
}
