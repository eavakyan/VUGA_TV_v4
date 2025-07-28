<?php

namespace App\Models\V2;

class ContentSource extends BaseModel
{
    protected $table = 'content_source';
    protected $primaryKey = 'content_source_id';
    
    protected $fillable = [
        'content_id',
        'title',
        'quality',
        'size',
        'is_download',
        'access_type',
        'type',
        'source'
    ];
    
    protected $casts = [
        'content_id' => 'integer',
        'is_download' => 'integer',
        'access_type' => 'integer',
        'type' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the source's content
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id');
    }
}