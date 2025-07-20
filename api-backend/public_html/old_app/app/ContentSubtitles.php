<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class ContentSubtitles extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_content_subtitles';
    protected $primaryKey  = 'subtitles_id ';
 
}
