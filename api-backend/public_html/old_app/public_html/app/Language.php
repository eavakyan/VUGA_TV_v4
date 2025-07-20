<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class Language extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_language';
    protected $primaryKey  = 'language_id';
}
