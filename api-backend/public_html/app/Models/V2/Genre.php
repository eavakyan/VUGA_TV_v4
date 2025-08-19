<?php

namespace App\Models\V2;

class Genre extends BaseModel
{
    protected $table = 'genre';
    protected $primaryKey = 'genre_id';
    
    protected $fillable = [
        'title'
    ];
    
    protected $casts = [
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    protected $appends = ['id'];
    
    /**
     * Get the id attribute (iOS expects 'id' not 'genre_id')
     */
    public function getIdAttribute()
    {
        return $this->genre_id;
    }
    
    /**
     * Get the genre's contents
     */
    public function contents()
    {
        return $this->belongsToMany(Content::class, 'content_genre', 'genre_id', 'content_id');
    }
}