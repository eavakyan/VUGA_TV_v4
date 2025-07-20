<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class CustomAdImages extends Model
{
    //
    protected $table = 'tbl_customad_images';

    public function ad()
    {
        return $this->hasOne(CustomAds::class, 'id', 'ad_id');
    }
}
