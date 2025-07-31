<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CustomAd extends Model
{
    use HasFactory;
    protected $table = 'custom_ad';
    protected $primaryKey = 'custom_ad_id';

    public function sources()
    {
        return $this->hasMany(CustomAdSource::class, 'custom_ad_id');
    }

}
