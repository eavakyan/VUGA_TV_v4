<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class ContentSource extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_content_source';
    protected $primaryKey  = 'source_id ';
}
