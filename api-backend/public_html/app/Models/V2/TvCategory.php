<?php

namespace App\Models\V2;

class TvCategory extends BaseModel
{
    protected $table = 'tv_category';
    protected $primaryKey = 'tv_category_id';
    
    protected $fillable = [
        'title',
        'image'
    ];
    
    protected $casts = [
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get channels in this category
     */
    public function channels()
    {
        return $this->belongsToMany(TvChannel::class, 'tv_channel_category', 'tv_category_id', 'tv_channel_id');
    }
}