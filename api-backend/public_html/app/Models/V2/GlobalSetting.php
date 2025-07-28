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
        'is_live_tv_enable' => 'boolean',
        'is_admob_android' => 'boolean',
        'is_admob_ios' => 'boolean',
        'is_custom_android' => 'boolean',
        'is_custom_ios' => 'boolean',
        'videoad_skip_time' => 'integer',
        'storage_type' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
}