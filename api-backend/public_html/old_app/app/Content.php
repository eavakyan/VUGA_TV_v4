<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class Content extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'tbl_content';
    protected $primaryKey  = 'id';
    /**
     * The attributes that are mass assignable.
     *
     * @var array
     */
  
    public static function get_random_string($field_code='content_id')
	  {
        $random_unique  =  sprintf('%04X%04X', mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(16384, 20479), mt_rand(32768, 49151), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535));

        $content = Content::where('content_id', '=', $random_unique)->first();
        if ($content != null) {
            $this->get_random_string();
        }
        return $random_unique;
    }
}
