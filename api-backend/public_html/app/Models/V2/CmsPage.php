<?php

namespace App\Models\V2;

class CmsPage extends BaseModel
{
    protected $table = 'cms_page';
    protected $primaryKey = 'cms_page_id';
    
    protected $fillable = [
        'privacy',
        'termsofuse'
    ];
    
    protected $casts = [
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
}