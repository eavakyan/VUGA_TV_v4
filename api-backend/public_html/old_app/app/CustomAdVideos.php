<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class CustomAdVideos extends Model
{
    //
    protected $table = 'tbl_customad_videos';
    public function ad()
    {
        return $this->hasOne(CustomAds::class, 'id', 'ad_id');
    }
}
