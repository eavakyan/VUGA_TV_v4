<?php

namespace App\Models\V2;

class AdmobConfig extends BaseModel
{
    protected $table = 'admob_config';
    protected $primaryKey = 'admob_config_id';
    
    protected $fillable = [
        'banner_id',
        'interstitial_id',
        'rewarded_id',
        'type'
    ];
    
    protected $casts = [
        'type' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
}