<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CustomAd extends Model
{
    use HasFactory;

    public function sources()
    {
        return $this->hasMany(CustomAdSource::class, 'custom_ad_id');
    }

}
