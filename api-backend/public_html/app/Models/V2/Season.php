<?php

namespace App\Models\V2;

class Season extends BaseModel
{
    protected $table = 'season';
    protected $primaryKey = 'season_id';
    
    protected $fillable = [
        'content_id',
        'title',
        'trailer_url'
    ];
    
    protected $casts = [
        'content_id' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the season's content
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id');
    }
    
    /**
     * Get the season's episodes
     */
    public function episodes()
    {
        return $this->hasMany(Episode::class, 'season_id');
    }
}