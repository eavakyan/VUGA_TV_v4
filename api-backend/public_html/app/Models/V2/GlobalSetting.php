<?php

namespace App\Models\V2;

class GlobalSetting extends BaseModel
{
    protected $table = 'global_setting';
    protected $primaryKey = 'global_setting_id';
    
    protected $fillable = [
        'app_name',
        'is_live_tv_enable',
        'is_admob_android',
        'is_admob_ios',
        'is_custom_android',
        'is_custom_ios',
        'videoad_skip_time',
        'storage_type'
    ];
    
    protected $casts = [
        'is_live_tv_enable' => 'integer',
        'is_admob_android' => 'integer',
        'is_admob_ios' => 'integer',
        'is_custom_android' => 'integer',
        'is_custom_ios' => 'integer',
        'videoad_skip_time' => 'integer',
        'storage_type' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
}