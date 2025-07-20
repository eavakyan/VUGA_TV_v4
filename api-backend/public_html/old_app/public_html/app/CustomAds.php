<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class CustomAds extends Model
{
    //
    protected $table = 'tbl_custom_ads';

    public function adImages()
    {
        return $this->hasMany(CustomAdImages::class, 'ad_id', 'id');
    }
    public function adVideos()
    {
        return $this->hasMany(CustomAdVideos::class, 'ad_id', 'id');
    }
}
