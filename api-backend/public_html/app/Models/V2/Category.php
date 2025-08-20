<?php

namespace App\Models\V2;

class Category extends BaseModel
{
    protected $table = 'category';
    protected $primaryKey = 'category_id';
    
    protected $fillable = [
        'title'
    ];
    
    protected $casts = [
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    protected $appends = ['id'];
    
    /**
     * Get the id attribute (iOS expects 'id' not 'category_id')
     */
    public function getIdAttribute()
    {
        return $this->category_id;
    }
    
    /**
     * Get the category's contents
     */
    public function contents()
    {
        return $this->belongsToMany(Content::class, 'content_category', 'category_id', 'content_id');
    }
}