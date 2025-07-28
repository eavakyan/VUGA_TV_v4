<?php

namespace App\Models\V2;

class CustomAd extends BaseModel
{
    protected $table = 'custom_ad';
    protected $primaryKey = 'custom_ad_id';
    
    protected $fillable = [
        'title',
        'redirectlink',
        'ads_image',
        'ads_video',
        'type',
        'status'
    ];
    
    protected $casts = [
        'type' => 'integer',
        'status' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
}