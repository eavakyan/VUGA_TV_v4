<?php

namespace App\Models\V2;

class TvChannel extends BaseModel
{
    protected $table = 'tv_channel';
    protected $primaryKey = 'tv_channel_id';
    
    protected $fillable = [
        'title',
        'thumbnail',
        'access_type',
        'category_ids',
        'type',
        'source'
    ];
    
    protected $casts = [
        'access_type' => 'integer',
        'type' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get categories for this channel
     */
    public function categories()
    {
        return $this->belongsToMany(TvCategory::class, 'tv_channel_category', 'tv_channel_id', 'tv_category_id');
    }
    
    /**
     * Get category IDs as array
     */
    public function getCategoryIdsArrayAttribute()
    {
        if (empty($this->category_ids)) {
            return [];
        }
        return array_map('intval', explode(',', $this->category_ids));
    }
}